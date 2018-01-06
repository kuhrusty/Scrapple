package com.kuhrusty.scrapple;

import java.util.Iterator;

/**
 * An object which gives us back definitions for words.
 */
public interface Dictionary {
    /**
     * Some Dictionaries need time to look up their results; this is how you
     * know when they're done.
     */
    public interface DefinitionListener {
        /**
         * @param word the word they were asked to define
         * @param def null, if the word isn't valid.
         */
        void definitionComplete(String word, Definition def);
    }

    /**
     * Looks up a word.  Because some Dictionary implementations may take a
     * while to look up their word, this notifies the given listener when it's
     * done.
     *
     * @param word the word to look up.  This will be passed through
     *             Util.normalizeWord().
     * @param next an optional Iterator over a list of Dictionaries to pass the
     *             request on to when this one doesn't contain a definition.
     *             May be null or empty.
     * @param listener must not be null; its definitionComplete() will be called
     *                 when this Dictionary (or another in the chain) is done
     *                 looking up the word.
     */
    public void define(String word, Iterator<Dictionary> next, DefinitionListener listener);
}
