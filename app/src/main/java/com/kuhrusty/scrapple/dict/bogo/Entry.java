package com.kuhrusty.scrapple.dict.bogo;

import java.util.ArrayList;

/**
 * Each regular expression or mad-lib entry may be associated with multiple
 * possible definition strings.
 */
public class Entry {
    private String fl;  //  part-of-speech or whatever, from MW's XML.
    private ArrayList<String> templates = new ArrayList<>(4);

    public void addTemplate(String template) {
        templates.add(template);
    }

    public int templateCount() {
        return templates.size();
    }

    public String getTemplate(int idx) {
        return templates.get(idx);
    }

    /**
     * The part-of-speech-or-whatever, from M-W's XML.  May return null.
     */
    public String getFl() {
        return fl;
    }
    public void setFlIfNull(String newFL) {
        if (fl == null) fl = newFL;
    }
}
