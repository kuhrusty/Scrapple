package com.kuhrusty.scrapple.dict;

import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.kuhrusty.scrapple.Definition;
import com.kuhrusty.scrapple.Dictionary;
import com.kuhrusty.scrapple.R;
import com.kuhrusty.scrapple.Util;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This is the Dictionary implementation which attempts to retrieve a definition
 * from m-w.com.
 */
public class MWDictionary implements Dictionary {
    private static final String LOGBIT = "MWDictionary";
    private String url;
    private String apiKey;
    private SAXParser parser;
    ConnectivityManager connectivityManager;

    private class NetworkTask extends DictionaryAsyncTask {
        final String normalizedWord;
        final DefinitionListener listener;
        public NetworkTask(String word, Iterator<Dictionary> nextDictionary,
                           DefinitionListener listener) {
            super(word, nextDictionary, listener);
            this.normalizedWord = Util.normalizeWord(word);
            this.listener = listener;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            Log.d(LOGBIT, "MWDictionary onPreExecute(): networkInfo " + networkInfo);
            if ((networkInfo == null) || (!networkInfo.isConnected()) ||
                ((networkInfo.getType() != ConnectivityManager.TYPE_WIFI) &&
                 (networkInfo.getType() != ConnectivityManager.TYPE_MOBILE))) {
                cancel(true);
                listener.definitionComplete(normalizedWord,
                        new Definition(R.string.error_no_network));
                return;
            }
//here is also where we would start some UI indicator that we're waiting.
        }

        @Override
        protected Definition doInBackground(String... strings) {
            if (isCancelled()) {
                Log.i(LOGBIT, "doInBackground bailing, isCancelled()");
                return null;
            }
            if ((strings == null) || (strings.length == 0) || (strings[0] == null)) {
                Log.i(LOGBIT, "doInBackground bailing, strings " + strings);
                return new Definition(R.string.error_no_api_key);
            }
            Log.d(LOGBIT, "doInBackground, url == \"" + strings[0] + "\"");
            String xml = null;
            try {
                URL url = new URL(strings[0]);
                xml = downloadURL(this, url);
                if (xml == null) return null;
                //  parse the XML to see whether it's a valid word
                MWXMLHandler handler = new MWXMLHandler();
                parser.parse(new InputSource(new StringReader(xml)), handler);
                return handler.isValidWord() ? new Definition(xml) : null;
            } catch(Exception ex) {
                Log.w(LOGBIT, "doInBackground() croaked", ex);
                return new Definition(true, ex.getLocalizedMessage());
            }
        }

        /**
         * Given a URL, sets up a connection and gets the HTTP response body
         * from the server.  If the network request is successful, it returns
         * the response body in String form. Otherwise, it will throw an
         * IOException.
         */
        private String downloadURL(NetworkTask task, URL url) throws IOException {
            InputStream stream = null;
            HttpURLConnection connection = null;
            String result = null;
            try {
                //Log.d(LOGBIT, "url.openConnection() for " + url);
                connection = (HttpURLConnection)(url.openConnection());
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();
                //publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                // Retrieve the response body as an InputStream.
                stream = connection.getInputStream();
                //publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
                if (stream != null) {
                    Reader reader = new InputStreamReader(stream);
                    char[] chunk = new char[1024];
                    int len;
                    StringBuilder buf = new StringBuilder();
                    Log.d(LOGBIT, "reading " + chunk.length + " chars...");
                    while ((len = reader.read(chunk)) != -1) {
                        Log.d(LOGBIT, "  got " + len);
                        buf.append(chunk, 0, len);
                        //publishProgress();
                        Log.d(LOGBIT, "reading " + chunk.length + " chars...");
                    }
                    reader.close();
                    result = buf.toString();
                    //  ugh, you probably want to remove this.
                    Log.d(LOGBIT, "DONE, got XML:\n" + result);
                }
            } finally {
                if (stream != null) {
                    stream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }

    public MWDictionary(Resources res, ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
        apiKey = res.getString(R.string.m_w_api_key);
        if ((apiKey == null) || (apiKey.trim().length() == 0)) {
            //  life is bad; no point continuing.  This leaves url == null,
            //  which means the strings[0] passed to doInBackground() will be
            //  null, which will cause us to give a "no API key" error message.
            return;
        }

        String ts = res.getString(R.string.m_w_api_url);
        url = ts.replace("@KEY@", apiKey);
        //Log.d(LOGBIT, "got URL \"" + url + "\"");

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException pce) {
            Log.w(LOGBIT, "failed to create SAX parser", pce);
        } catch (SAXException saxe) {
            Log.w(LOGBIT, "failed to create SAX parser", saxe);
        }
    }

    @Override
    public void define(String word, Iterator<Dictionary> nextDictionary,
                       DefinitionListener listener) {
        word = Util.normalizeWord(word);
        String turl = (url != null) ? url.replace("@WORD@", word) : null;
        new NetworkTask(word, nextDictionary, listener).execute(turl);
    }
}
