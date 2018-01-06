package com.kuhrusty.scrapple.dict;

import android.util.Log;

import com.kuhrusty.scrapple.Dictionary;
import com.kuhrusty.scrapple.Util;

import java.util.Iterator;

/**
 * This waits a bit (as if it's doing a lookup), and then returns null.
 */
public class NoGoodDictionary implements Dictionary {
    private static final String LOGBIT = "NoGoodDictionary";

    private class WasteTimeTask extends DictionaryAsyncTask {
        public WasteTimeTask(String word, Iterator<Dictionary> next,
                             DefinitionListener listener) {
            super(word, next, listener);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (isCancelled()) {
                Log.i(LOGBIT, "doInBackground bailing, isCancelled() " + isCancelled());
                return null;
            }
            try {
                Thread.currentThread().sleep(1000l);
            } catch(InterruptedException ex) {
                //  don't care.
            }
            return null;
        }
    }

    @Override
    public void define(String word, Iterator<Dictionary> next, DefinitionListener listener) {
        word = Util.normalizeWord(word);
        new WasteTimeTask(word, next, listener).execute();
    }
}
