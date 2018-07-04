package com.kuhrusty.scrapple;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A few dumb utility methods which should probably be somewhere else.
 */
public class Util {
    /**
     * Removes all non-letters and converts to lower-case.
     *
     * @param word if null, null will be returned.
     */
    public static String normalizeWord(String word) {
        if (word == null) return null;
        //  well... this is for Scrabble, right?  So let's not worry that we're
        //  stripping out everything except A-Z.
        word = word.toLowerCase();
        //word = word.replaceAll("[^\\p{IsAlphabetic}]+", "");
        word = word.replaceAll("[^a-z]+", "");
        return word;
    }

    /**
     * Reads a file from resources and returns its entire contents as a String.
     *
     * @param res must not be null.
     * @param resID
     * @return
     */
    public static String snortFileFromRes(Resources res, int resID) {
        InputStream is = res.openRawResource(resID);
        //if (is == null) {
        //    throw new FileNotFoundException(filename + " in CLASSPATH");
        //}
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder buf = new StringBuilder();
        try {
            while ((line = in.readLine()) != null) {
                buf.append(line).append("\n");
            }
            in.close();
        } catch (IOException ioe) {
//TODO do something smart here
        }
        return buf.toString();
    }

    /**
     * Takes XML, runs it through a Transformer, and theoretically returns HTML.
     */
    public static String xmlToHTML(String xml, Transformer transformer) throws TransformerException {
        Source xmlSource = new StreamSource(new ByteArrayInputStream(xml.getBytes()));
        StringWriter rv = new StringWriter();
        Result result = new StreamResult(rv);
        transformer.transform(xmlSource, result);
        return rv.toString();
    }
}
