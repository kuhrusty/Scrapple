package com.kuhrusty.scrapple.dict;

import android.os.AsyncTask;
import android.util.Log;

import com.kuhrusty.scrapple.Definition;
import com.kuhrusty.scrapple.Dictionary;
import com.kuhrusty.scrapple.Util;

import java.util.Iterator;

/**
 * Implements an onPostExecute() which is common to all of our dictionaries'
 * AsyncTasks.
 */
public abstract class DictionaryAsyncTask extends AsyncTask<String, Void, Definition> {
    private static final String LOGBIT = "DictionaryAsyncTask";

    private final String normalizedWord;
    private final Iterator<Dictionary> nextDictionary;
    private final Dictionary.DefinitionListener listener;

    /**
     * @param word will be passed to Util.normalizeWord().
     * @param nextDictionary optional Iterator to list of Dictionaries to use if
     *                       this dictionary doesn't contain a definition for
     *                       the word.
     * @param listener will be notified once we're done looking up the word.
     */
    public DictionaryAsyncTask(String word, Iterator<Dictionary> nextDictionary,
                               Dictionary.DefinitionListener listener) {
        this.normalizedWord = Util.normalizeWord(word);
        this.nextDictionary = nextDictionary;
        this.listener = listener;
    }

    /**
     * Overridden to either call definitionComplete() on the listener, or
     * pass the request on to the next Dictionary in the chain.
     */
    @Override
    protected void onPostExecute(Definition result) {
        if (result != null) {
            Log.d(LOGBIT, "definitionComplete(\"" + normalizedWord + "\", non-null)");
            listener.definitionComplete(normalizedWord, result);
        } else if ((nextDictionary != null) && (nextDictionary.hasNext())) {
            Log.d(LOGBIT, "passing on define(\"" + normalizedWord + "\")");
            nextDictionary.next().define(normalizedWord, nextDictionary, listener);
        } else {
            Log.d(LOGBIT, "definitionComplete(\"" + normalizedWord + "\", null)");
            listener.definitionComplete(normalizedWord, null);
        }
    }
}
