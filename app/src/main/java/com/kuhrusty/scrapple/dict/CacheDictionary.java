package com.kuhrusty.scrapple.dict;

import android.util.Log;

import com.kuhrusty.scrapple.Definition;
import com.kuhrusty.scrapple.Dictionary;
import com.kuhrusty.scrapple.Util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This caches the last n lookups, and returns them if they're requested again.
 * This is so that, if the app's owner turns off the BogoDictionary or even the
 * MWDictionary, the victim can't prove that it's bogus by looking up a
 * previously-looked-up word.
 */
public class CacheDictionary implements Dictionary {
    private static final String LOGBIT = "CacheDictionary";

    //  Once we pass this limit, we start discarding older entries.
    private static final int MAX_ENTRIES = 100;

    //  because Definition doesn't include the word!?
    private static class CacheEntry {
        public CacheEntry(String word, Definition def) {
            this.word = word;
            this.def = def;
        }
        String word;
        Definition def;
    }

    private LinkedList<CacheEntry> defs = new LinkedList<>();

    @Override
    public void define(String word, Iterator<Dictionary> next,
                       final DefinitionListener listener) {
        word = Util.normalizeWord(word);
        for (CacheEntry te : defs) {
            if (te.word.equals(word)) {
                Log.d(LOGBIT, "definitionComplete(\"" + word + "\", " +
                        ((te.def != null) ? "non-null" : "null") + ")");
                listener.definitionComplete(word, te.def);
                return;
            }
        }

        Dictionary td = (next != null) ? next.next() : null;
        if (td != null) {
            Log.d(LOGBIT, "passing on define(\"" + word + "\")");
            td.define(word, next, new DefinitionListener() {
                @Override
                public void definitionComplete(String word, Definition def) {
                    defs.addFirst(new CacheEntry(word, def));
                    if (defs.size() > MAX_ENTRIES) defs.removeLast();
                    Log.d(LOGBIT, "definitionComplete(\"" + word + "\", " +
                            ((def != null) ? "non-null" : "null") + ")");
                    listener.definitionComplete(word, def);
                }
            });
        } else {
            //  not adding the null definition to the cache??  Well... there
            //  should always be another Dictionary in the chain after this
            //  guy, so we should never hit this block anyway.
            Log.d(LOGBIT, "definitionComplete(\"" + word + "\", null)");
            listener.definitionComplete(word, null);
        }
    }
}
