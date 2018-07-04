package com.kuhrusty.scrapple.dict;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Not thread-safe; create a new one for each XML document.
 *
 * <P>This one just looks for &lt;entry&gt; elements in the returned XML, so
 * that we can figure out whether or not the word is valid.
 */
public class MWXMLHandler extends DefaultHandler {

    private static final String TAG_ENTRY = "entry";

    private boolean isValidWord = false;

    public boolean isValidWord() {
        return isValidWord;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if (qName.equals(TAG_ENTRY)) {
            isValidWord = true;
        }
    }
}
