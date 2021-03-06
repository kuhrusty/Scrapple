package com.kuhrusty.scrapple;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This doesn't contain any tests; it's just utility methods & annotations
 * needed by Dictionary subclass tests.
 * <p>
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DictionaryTest {

    private static class SynchronousDefinitionListener implements Dictionary.DefinitionListener {
        Definition def = null;

        @Override
        public void definitionComplete(String word, Definition def) {
            this.def = def;
        }
    }

    /**
     * Looks up the given word in the given Dictionary, and confirms that the
     * XML output from the Dictionary matches the contents of the given file.
     */
    protected void checkXML(Dictionary dict, String word, String xmlFileName)
            throws Exception {
        SynchronousDefinitionListener sdl = new SynchronousDefinitionListener();
        dict.define(word, null, sdl);
        //  we expect that to have called definitionComplete() before returning
        assertNotNull(sdl.def);
        assertNotNull(sdl.def.getRawXML());

        String expect = null;
        try {
            expect = ScrappleTest.getTestFile(this, xmlFileName);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Expected to find this XML in " + xmlFileName +
                    ":\n" + expect);
            throw fnfe;
        }
        //  Ugh, screw with whitespace.
        String es = expect.replaceAll("\\s+", " ");
        es = es.replaceAll("\\> \\<", "><").trim();
        String gs = sdl.def.getRawXML().replaceAll("\\s+", " ");
        gs = gs.replaceAll("\\> \\<", "><").trim();
        assertEquals(es, gs);
    }
}