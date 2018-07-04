package com.kuhrusty.scrapple.dict;

import android.content.res.Resources;
import android.util.Log;

import com.kuhrusty.scrapple.Definition;
import com.kuhrusty.scrapple.Dictionary;
import com.kuhrusty.scrapple.R;
import com.kuhrusty.scrapple.Util;
import com.kuhrusty.scrapple.dict.bogo.Entry;
import com.kuhrusty.scrapple.dict.bogo.PatternFileParser;
import com.kuhrusty.scrapple.dict.bogo.ReEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This is the Dictionary implementation which makes up definitions.
 */
public class BogoDictionary implements Dictionary {
    private static final String LOGBIT = "BogoDictionary";

    private Resources res;

    public BogoDictionary(Resources res) {
        this.res = res;
    }

    /**
     * See the comment on performSubstitutions().
     */
    public static class PartialDefinition {
        public String def;
        public String fl;  //  whatever "fl" stands for in MW's XML; may be null
        public PartialDefinition(String def, String fl) {
            this.def = def;
            this.fl = fl;
        }
    }

    private List<ReEntry> reEntries;
    private Map<String, Entry> madLibEntries;

    private DocumentBuilder docBuilder = null;
    private TransformerFactory transformerFactory = null;


    private Element addElement(Document doc, Element parent, String tag) {
        Element rv = doc.createElement(tag);
        parent.appendChild(rv);
        return rv;
    }
    private Element addTextNode(Document doc, Element parent, String tag, String text) {
        Element rv = addElement(doc, parent, tag);
        rv.appendChild(doc.createTextNode(text));
        return rv;
    }

    private class GenerateWordTask extends DictionaryAsyncTask {
        private String word;
        public GenerateWordTask(String word, Iterator<Dictionary> next,
                             DefinitionListener listener) {
            super(word, next, listener);
            this.word = word;
        }

        @Override
        protected Definition doInBackground(String... strings) {
            if (isCancelled()) {
                Log.i(LOGBIT, "doInBackground bailing, isCancelled() " + isCancelled());
                return null;
            }

            PartialDefinition pd = defineSynchronously(word);

            Document doc = pdToDocument(word, pd);
            if (doc == null) {
                return new Definition(R.string.error_unknown);
            }

            if (transformerFactory == null) {
                transformerFactory = TransformerFactory.newInstance();
            }
            Transformer transformer = null;
            Log.d(LOGBIT, "building Transformer");
            try {
                transformer = transformerFactory.newTransformer();
//                transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
//transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                Log.d(LOGBIT, "transformer == " + transformer);
            } catch (TransformerConfigurationException tce) {
                Log.e(LOGBIT, "couldn't create Transformer", tce);
                return new Definition(R.string.error_unknown);
            }

            String xml = null;
            Log.d(LOGBIT, "transforming Document");
            StringWriter writer = new StringWriter();
            try {
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                xml = writer.getBuffer().toString();
            } catch (TransformerException te) {
                Log.e(LOGBIT, "couldn't transform Document into XML", te);
                return new Definition(R.string.error_unknown);
            }
            return new Definition(xml);
        }
    }

    /**
     * This is just exposed for unit testing.
     */
    public PartialDefinition defineSynchronously(String word) {
        if (reEntries == null) {
            PatternFileParser pfp = new PatternFileParser();
            pfp.loadEntries(res.openRawResource(R.raw.patterns));
            reEntries = pfp.getReEntries();
            madLibEntries = pfp.getMadLibEntries();
        }

        ReEntry re = null;
        Matcher matcher = null;
        for (ReEntry re2 : reEntries) {
            if ((matcher = re2.matcher(word)) != null) {
                re = re2;
                break;
            }
        }
        if (matcher == null) {
            throw new RuntimeException("somehow wound up without a matching pattern!?");
        }

        //  Use word.hashCode() as the seed so that we get the same definition
        //  every time for a given word.
        Random rand = new Random(word.hashCode());

        HashMap<String, String> vars = new HashMap<>();
        vars.put("WORD", word);
        vars.put("WORD$1", matcher.group(1));
        PartialDefinition pd = performSubstitutions(re, rand, vars);
        if (pd.fl == null) pd.fl = "noun";

        return pd;
    }

    /**
     * This is what takes a made-up definition and generates a Document whose
     * structure should match the XML we get from M-W.  There are some examples
     * in app/src/test/resources.
     */
    private Document pdToDocument(String word, PartialDefinition pd) {
        if (docBuilder == null) {
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException pce) {
                Log.e(LOGBIT, "couldn't build DocumentBuilder", pce);
                return null;
            }
        }

        Log.d(LOGBIT, "building Document");
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);

        Element entryList = doc.createElement("entry_list");
        doc.appendChild(entryList);

        Element entry = addElement(doc, entryList, "entry");
        entry.setAttribute("id", word.toLowerCase());
        addTextNode(doc, entry, "ew", word.toLowerCase());
        addTextNode(doc, entry, "fl", pd.fl);
        //  Here's where you would add logic for splitting the word up into
        //  syllables: "snorkel" -> snor*kel
        addTextNode(doc, entry, "hw", word.toLowerCase());
        //  Here's where you would add logic for generating pronunciation:
        //  "snorkel" -> "!snor-kul"
        addTextNode(doc, entry, "pr", word.toLowerCase());
//        int defs = (re != null) ? 1 : rand.nextInt(2) + 1;
        int defs = 1;
        for (int ii = 0; ii < defs; ++ii) {
            Element def = addElement(doc, entry, "def");
            addTextNode(doc, def, "sn", "1");
            addTextNode(doc, def, "dt", pd.def);
        }

        return doc;
    }

    /**
     * Ugh, the only reason PartialDefinition exists is so that it can be
     * returned by this (instead of String) so that we can also get an
     * entry's "fl" value.
     *
     * This chooses a definition template from the Entry, and looks for
     * @AWORD$1@ etc. in the template, performing substitutions.  (See the top
     * of patterns.txt for the list of what it's looking for.)  If it hits a
     * mad-lib key, it recurses.
     *
     * @param entry
     * @param rand will be used for
     * @param vars must contain "WORD" and "WORD$1" values; "AWORD" and
     *             "AWORD$1" may be added to it if they're used by the template
     *             or any of its sub-templates.
     * @return PartialDefinition containing the fully substituted String of the
     *         definition, and maybe an "fl" value.
     */
    private PartialDefinition performSubstitutions(Entry entry, Random rand,
                                        Map<String, String> vars) {
        String fl = entry.getFl();  //  may be null
        String src = null;
        if (entry.templateCount() == 1) {
            src = entry.getTemplate(0);
        } else {
            src = entry.getTemplate(rand.nextInt(entry.templateCount()));
        }
        StringBuilder buf = new StringBuilder();
        int srcpos = 0;
        int atpos;
        while ((atpos = src.indexOf('@', srcpos)) != -1) {
            buf.append(src, srcpos, atpos);
            int at2pos = src.indexOf('@', atpos + 1);
            if (at2pos == -1) {
                throw new RuntimeException("gurkk, expected pair of @");
            }
            String key = src.substring(atpos + 1, at2pos);
            if (vars.containsKey(key)) {
                buf.append(vars.get(key));
            } else if (key.equals("AWORD$1") || key.equals("AWORD")) {
                //  We calculate this stuff once & stuff the result back into
                //  vars, so that if we recurse, it's already there.
                String word = vars.get(key.substring(1));  //  skip "A"
                String aWord = "a " + word;
                if (word.matches("^[aeiou].+")) {
                    aWord = "an " + word;
                }
                vars.put(key, aWord);
                buf.append(aWord);
            } else {
                Entry subEntry = madLibEntries.get(key);
                if (subEntry == null) {
                    throw new RuntimeException("gurkk, failed to find substitution key \"@" + key + "@\"");
                }
                PartialDefinition pd = performSubstitutions(subEntry, rand, vars);
                buf.append(pd.def);
                if (fl == null) fl = pd.fl;
            }
            srcpos = at2pos + 1;
        }
        buf.append(src, srcpos, src.length());
        return new PartialDefinition(buf.toString(), fl);
    }

    @Override
    public void define(String word, Iterator<Dictionary> nextDictionary,
                       DefinitionListener listener) {
        word = Util.normalizeWord(word);
        new GenerateWordTask(word, nextDictionary, listener).execute();
    }
}
