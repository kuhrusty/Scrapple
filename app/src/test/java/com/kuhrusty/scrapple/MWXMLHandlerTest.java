package com.kuhrusty.scrapple;

import com.kuhrusty.scrapple.dict.MWXMLHandler;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static org.junit.Assert.assertEquals;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MWXMLHandlerTest {
    private SAXParser parser;

    @Before
    public void initParser() throws ParserConfigurationException, SAXException {
        if (parser == null) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();
        }
    }

    @Test
    public void testIsValidWord() throws Exception {
        checkIfValid("fish.xml", true);
        checkIfValid("jecue.xml", false);
        checkIfValid("snorkel.xml", true);
        checkIfValid("snorkeler.xml", false);
        checkIfValid("snorkeling.xml", true);
        checkIfValid("snorkels.xml", true);
        checkIfValid("snorpfkel.xml", false);
        checkIfValid("unbox.xml", true);
        checkIfValid("unboxer.xml", false);
        checkIfValid("unboxers.xml", false);
    }

    private void checkIfValid(String filename, boolean expect) throws Exception {
        MWXMLHandler th = new MWXMLHandler();
        parser.parse(getTestFile(filename), th);
        assertEquals(expect, th.isValidWord());
    }

    private InputStream getTestFile(String filename) throws FileNotFoundException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new FileNotFoundException(filename + " in CLASSPATH");
        }
        return is;
    }
}
