package com.kuhrusty.scrapple;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ScrappleTest {

    @Test
    public void testNormalize() throws Exception {
        assertEquals("flerb", Util.normalizeWord("flerb"));
        assertEquals("flerb", Util.normalizeWord("FLERB"));
        assertEquals("flerb", Util.normalizeWord("F LE  rb"));
        assertEquals("flerb", Util.normalizeWord(" F\tLE  rb        "));
        assertEquals("flerb", Util.normalizeWord(" F\tLE;r\0b        "));
        assertEquals("flerb", Util.normalizeWord(" F\tL123E;r\0b        "));
    }

    @Test
    public void testTransform() throws Exception {
        String styleSheet = getTestFile(this, "raw/definition.xsl");
        Transformer transformer = initTransformer(styleSheet);

        checkTransform(transformer, "snorkel.xml", "snorkel.html");
        checkTransform(transformer, "fish.xml", "fish.html");
    }

    private void checkTransform(Transformer transformer, String xmlFileName,
                                String htmlFileName) throws Exception {
        String xml = getTestFile(this, xmlFileName);
        String html = Util.xmlToHTML(xml, transformer);
        String expect = null;
        try {
            expect = getTestFile(this, htmlFileName);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Expected to find this HTML in " + htmlFileName +
                    ":\n" + html);
            throw fnfe;
        }
        assertEquals(expect, html);
    }

    public static String getTestFile(Object testClass, String filename) throws Exception {
        InputStream is = testClass.getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new FileNotFoundException(filename + " in CLASSPATH (which " +
                    "might be: " + System.getProperty("java.class.path") + ")");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder buf = new StringBuilder();
        while ((line = in.readLine()) != null) {
//            if (!line.startsWith("//")) buf.append(line).append("\n");
            buf.append(line).append("\n");
        }
        in.close();
        return buf.toString();
    }

    /**
     * @param xsl the complete XSLT XML; must not be null.
     */
    public static Transformer initTransformer(String xsl) throws TransformerConfigurationException {
        Source xsltSource = new StreamSource(new ByteArrayInputStream(xsl.getBytes()));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        return transformerFactory.newTransformer(xsltSource);
    }
}