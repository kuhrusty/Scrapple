package com.kuhrusty.scrapple;

import android.content.res.Resources;
import android.util.Log;

import com.kuhrusty.scrapple.dict.BogoDictionary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import javax.xml.transform.Transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class BogoDictionaryTest extends DictionaryTest {

    @Test
    public void testBogoDictionary() throws Exception {
        Resources res = PowerMockito.mock(Resources.class);
        when(res.openRawResource(R.raw.patterns)).thenReturn(getClass().getClassLoader().getResourceAsStream("raw/patterns.txt"));
        BogoDictionary dict = new BogoDictionary(res);
//        checkXML(dict, "Snorpfkel", "bogo/snorpfkel.xml");
//        checkXML(dict, "snorpfkel", "bogo/snorpfkel.xml");
//        checkXML(dict, "SNORPFKEL", "bogo/snorpfkel.xml");
//
//        checkXML(dict, "reunfnurgled", "bogo/reunfnurgled.xml");
//        checkXML(dict, "reunfnurgle",  "bogo/reunfnurgle.xml");
//        checkXML(dict, "unfnurgle",    "bogo/unfnurgle.xml");
//        checkXML(dict, "fnurgle",      "bogo/fnurgle.xml");

        //  This one should be a verb.
//        checkXML(dict, "blaff",      "bogo/blaff.xml");

        checkXML(dict, "sarinade",      "bogo/sarinade.xml");

        check(dict, "nelf",    "noun", ":an olfactory disturbance of unknown origin");
        check(dict, "fleb",    "noun", ":the ejection port cutout found in Swingline staplers manufactured between 1927 and 1965");
        check(dict, "hrrp",    "noun", ":the mainspring housing retaining pin found in Swingline staplers manufactured between 1927 and 1965");
        check(dict, "blup",    "verb", ":to swing at or hit a flying insect with an improvised weapon such as a shoe, sports racket, or rolled-up newspaper");
        check(dict, "blaff",   "verb", ":to swing at or hit a flying insect with an improvised weapon such as a shoe, sports racket, or rolled-up newspaper");
        check(dict, "snibble", "noun", ":an eye injury which causes the perception of light and dark to be reversed");
        check(dict, "gyck",    "noun", ":a roughly spherical chunk of flesh removed by a melon-baller");
        check(dict, "englurg", "verb", ":to fill or swell with glurg");
        check(dict, "glurg",   "verb", ":to swing at or hit a flying insect with an improvised weapon such as a shoe, sports racket, or rolled-up newspaper");
        check(dict, "defrep",  "verb", ":to remove frep");
        check(dict, "frep",    "noun", ":a roughly spherical chunk of flesh removed by a melon-baller");
//        check(dict, "snudge",    "noun", ":a roughly spherical chunk of flesh removed by a melon-baller");
//        check(dict, "gex",    "noun", ":a roughly spherical chunk of flesh removed by a melon-baller");
        check(dict, "piff",    "noun", ":a 17th-century sailor or dock worker in Spain or Portugal");
//        check(dict, "blug",    "verb", ":to swing at or hit a flying insect with an improvised weapon such as a shoe, sports racket, or rolled-up newspaper");
        check(dict, "jecued",  "verb", ":past tense of jecue");
        check(dict, "jecue",   "noun", ":a roughly spherical chunk of flesh removed by a melon-baller");
    }

    private void check(BogoDictionary dict, String word, String fl, String dt) {
        word = Util.normalizeWord(word);
        BogoDictionary.PartialDefinition pd = dict.defineSynchronously(word);
        //  check this one first because it breaks more often
        assertEquals(word + " definition", dt, pd.def);
        assertEquals(word + " part of speech", fl, pd.fl);
    }
}