package com.kuhrusty.scrapple.dict.bogo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReEntry extends Entry {
    private ArrayList<Pattern> res = new ArrayList<>(4);

    public void addPattern(Pattern pat) {
        res.add(pat);
    }

    /**
     * Returns a Matcher for which find() returned true on the given word, or
     * null if none of this entry's patterns match.
     *
     * @param word must not be null
     * @return a Matcher which matches the word, or null.
     */
    public Matcher matcher(String word) {
        for (Pattern tp : res) {
            Matcher tm = tp.matcher(word);
            if (tm.find()) {
                return tm;
            }
        }
        return null;
    }

    public boolean isComplete() {
        return (res.size() > 0) && (templateCount() > 0);
    }
}
