package com.kuhrusty.scrapple.dict.bogo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Arguably this belongs in BogoDictionary, but I moved it out just to make that
 * class cleaner.  Create this, have it loadEntries(), then getReEntries() and
 * getMadLibEntries(), and discard it.
 */
public class PatternFileParser {
    private static final String LOGBIT = "PatternFileParser";

    private ArrayList<ReEntry> reEntries;
    private HashMap<String, Entry> madLibEntries;

    /**
     * Returns the list of patterns & definitions which were loaded by
     * loadEntries(), or null if that hasn't been called yet.
     */
    public List<ReEntry> getReEntries() {
        return reEntries;
    }

    /**
     * Returns the mapping of substitution keys to values which were loaded
     * by loadEntries(), or null if that hasn't been called yet.
     */
    public Map<String, Entry> getMadLibEntries() {
        return madLibEntries;
    }

    /**
     * After calling this, getReEntries() and getMadLibEntries() should give
     * you the populated collections of entries.
     *
     * @param is must not be null.
     */
    public void loadEntries(InputStream is) {
        reEntries = new ArrayList<>();
        madLibEntries = new HashMap<>();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        Entry entry = null;
        boolean wasReadingDefinitions = true;
        int lineNumber = 0;
        try {
            while ((line = in.readLine()) != null) {
                ++lineNumber;
                if (line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                boolean isEntry = !(line.startsWith(" ") || line.startsWith("\t"));
                boolean isMadLib = isEntry && line.startsWith("@");
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (isMadLib) {
                    if ((line.length() == 1) || (!line.endsWith("@"))) {
                        throw new RuntimeException("patterns.txt line " +
                                lineNumber + ": expected ending \"@\"");
                    }
                    line = line.substring(1, line.length() - 1);
                }
                if (wasReadingDefinitions && isEntry) {
                    //  we've started a new entry.
                    if (isMadLib) {
                        entry = new Entry();
                        //  this is done below, in "if (isEntry)", in case we
                        //  have multiple @KEY@ lines for the same Entry.
                        //madLibEntries.put(line, entry);
                    } else {
                        entry = new ReEntry();
                        reEntries.add((ReEntry)entry);
                    }
                    wasReadingDefinitions = false;
                }
                if (isEntry) {
                    if (isMadLib) {
                        madLibEntries.put(line, entry);
                    } else {
                        try {
                            ((ReEntry)entry).addPattern(Pattern.compile(line));
                        } catch (PatternSyntaxException pse) {
                            Log.w(LOGBIT, "couldn't compile pattern \"" + line + "\"", pse);
                        }
                    }
                } else {
                    wasReadingDefinitions = true;
                    handleLine(entry, line);
                }
            }
            in.close();
        } catch (IOException ioe) {
            Log.w(LOGBIT, "problem reading patterns.txt", ioe);
        }
        //  Now let's remove any elements which aren't valid.  (Not sure this is
        //  necessary, but just in case my logic above isn't right.)
        for (int ii = reEntries.size() - 1; ii <= 0; --ii) {
            if (!reEntries.get(ii).isComplete()) {
                reEntries.remove(ii);
            }
        }
        //  not totally sure this won't throw an exception for removing from the
        //  set while iterating over it.
        for (String key : madLibEntries.keySet()) {
            if (madLibEntries.get(key).templateCount() == 0) {
                madLibEntries.remove(key);
            }
        }
        //  Also, this doesn't check for circular references, which will croak us.
    }

    private void handleLine(Entry entry, String line) {
        if (line.startsWith("@fl ")) {  //  LAME
            entry.setFlIfNull(line.substring(4).trim());
        } else {
            entry.addTemplate(line);
        }
    }
}
