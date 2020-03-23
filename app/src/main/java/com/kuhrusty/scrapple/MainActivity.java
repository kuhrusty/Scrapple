package com.kuhrusty.scrapple;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Parcelable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuhrusty.scrapple.dict.BogoDictionary;
import com.kuhrusty.scrapple.dict.CacheDictionary;
import com.kuhrusty.scrapple.dict.MWDictionary;
import com.kuhrusty.scrapple.dict.NoGoodDictionary;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * This has a text field where you enter your word; a button to trigger the
 * lookup; a text area/WebView where we display the result; a nice background &
 * "Scrapple OFFICIAL DICTIONARY" image; somewhere you can tap to get it to
 * switch between bogus and non-bogus words; and an indicator of which mode it's
 * in.
 */
public class MainActivity extends AppCompatActivity implements Dictionary.DefinitionListener {
    private static final String LOGBIT = "Scrapple";

    //  If our XSL template is Parcelable, we store it that way; otherwise, if
    //  it's Serializable, we store it that way; otherwise, we just store the
    //  raw XML string.
    private static final String KEY_TEMPLATE = "com.kuhrusty.scrapple.template";
    private static final String KEY_TEMPLATE_PARCELABLE = "com.kuhrusty.scrapple.templateP";
    private static final String KEY_TEMPLATE_SERIALIZABLE = "com.kuhrusty.scrapple.templateS";
    private static final String KEY_STYLESHEET = "com.kuhrusty.scrapple.stylesheet";

    private static final String KEY_USE_BOGO_DICT = "com.kuhrusty.scrapple.useBogoDict";
    private static final String KEY_ALL_WORDS_BAD = "com.kuhrusty.scrapple.allWordsBad";
    //private static final String KEY_WAITING_NETWORK = "com.kuhrusty.scrapple.waitingOnNetwork";

    //  These states correspond to the minLevel/maxLevel values in
    //  res/drawable/logo.xml.
    private static final int LEVEL_DEFAULT = 0;
    private static final int LEVEL_NOBOGO = 1;
    private static final int LEVEL_NOGOOD = 2;

    private TextView word;
    private WebView definition;
    private boolean useBogoDict = true;
    private boolean allWordsAreBad = false;

    private List<Dictionary> allBadChain;
    private List<Dictionary> bogoChain;
    private List<Dictionary> onlyGoodChain;

    private String styleSheet;  //  only used if we can't store the Templates
    private Templates ssTemplate;
    private Transformer transformer;
    //private boolean waitingOnNetwork = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            useBogoDict = savedInstanceState.getBoolean(KEY_USE_BOGO_DICT, useBogoDict);
            allWordsAreBad = savedInstanceState.getBoolean(KEY_ALL_WORDS_BAD, allWordsAreBad);
            //waitingOnNetwork = savedInstanceState.getBoolean(KEY_WAITING_NETWORK, waitingOnNetwork);
            if (savedInstanceState.getBoolean(KEY_TEMPLATE_PARCELABLE, false)) {
                Log.d(LOGBIT, "got Parcelable template");
                ssTemplate = savedInstanceState.getParcelable(KEY_TEMPLATE);
            } else if (savedInstanceState.getBoolean(KEY_TEMPLATE_SERIALIZABLE, false)) {
                Log.d(LOGBIT, "got Serializable template");
                ssTemplate = (Templates)(savedInstanceState.getSerializable(KEY_TEMPLATE));
            } else {
                Log.d(LOGBIT, "got no template, looking for XML");
                styleSheet = savedInstanceState.getString(KEY_STYLESHEET);
            }
        }

        if ((ssTemplate == null) && (styleSheet == null)) {
            styleSheet = Util.snortFileFromRes(getResources(), R.raw.definition);
        }
        try {
            if (ssTemplate == null) {
                Source xsltSource = new StreamSource(new ByteArrayInputStream(styleSheet.getBytes()));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                ssTemplate = transformerFactory.newTemplates(xsltSource);
                //  the API documentation for newTemplates() says it never
                //  returns null, but I am seeing cases where bad XSLT causes it
                //  to return null instead of throwing an exception.
                if (ssTemplate == null) {
                    throw new TransformerConfigurationException(
                            "transformerFactory.newTemplates() returned null");
                }
            }
            transformer = ssTemplate.newTransformer();
        } catch (TransformerConfigurationException tce) {
            Log.e(LOGBIT, "couldn't initialize transformer", tce);
        }

        Dictionary bogoDict = new BogoDictionary(getResources());
        Dictionary mwDict = new MWDictionary(getResources(),
                (ConnectivityManager)(getSystemService(Context.CONNECTIVITY_SERVICE)));
        Dictionary cache = new CacheDictionary();
        //  We keep three chains of dictionaries, and just switch between chains
        //  based on which mode we're in.
        allBadChain = new ArrayList<>(2);
        allBadChain.add(cache);
        allBadChain.add(new NoGoodDictionary());

        bogoChain = new ArrayList<>(3);
        bogoChain.add(cache);
        bogoChain.add(mwDict);
        bogoChain.add(bogoDict);

        onlyGoodChain = new ArrayList<>(2);
        onlyGoodChain.add(cache);
        onlyGoodChain.add(mwDict);

        word = (TextView)(findViewById(R.id.word));
        //  This bit is for launching the lookup when the person clicks return
        //  on the soft keyboard.
        word.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView tv, int actionID, KeyEvent event) {
                    if ((actionID == EditorInfo.IME_ACTION_DONE)) {
                        startSearch(tv);
                        //InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        //imm.hideSoftInputFromWindow(auto_data.getWindowToken(), 0);
                        return true;
                    }
                    return false;
                }
            });

        definition = (WebView)(findViewById(R.id.definition));

        ImageView iv = (ImageView)(findViewById(R.id.logo));
        //  This guy listens for touches on the logo image, and updates our
        //  mode accordingly.
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if (ev.getY() < (view.getHeight() / 2)) {
                    int x = (int)(ev.getX());
                    int wq = view.getWidth() / 3;
                    if (x < wq) {
                        allWordsAreBad = !allWordsAreBad;
                        updateStateIndicator((ImageView)view);
                    } else if (x > (wq * 2)) {
                        useBogoDict = !useBogoDict;
                        updateStateIndicator((ImageView)view);
                    }
                }
                return false;
            }
        });
        updateStateIndicator(iv);
    }

    private void updateStateIndicator(ImageView logo) {
        //  the image level is how we're toggling the state indicator.
        logo.setImageLevel(allWordsAreBad ? (LEVEL_NOGOOD) :
                (useBogoDict ? LEVEL_DEFAULT : LEVEL_NOBOGO));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //state.putBoolean(KEY_WAITING_NETWORK, waitingOnNetwork);
        state.putBoolean(KEY_USE_BOGO_DICT, useBogoDict);
        state.putBoolean(KEY_ALL_WORDS_BAD, allWordsAreBad);
        if (ssTemplate != null) {
            if (ssTemplate instanceof Parcelable) {
                state.putBoolean(KEY_TEMPLATE_PARCELABLE, true);
                state.putParcelable(KEY_TEMPLATE, (Parcelable)ssTemplate);
            } else if (ssTemplate instanceof Serializable) {
                state.putBoolean(KEY_TEMPLATE_SERIALIZABLE, true);
                state.putSerializable(KEY_TEMPLATE, (Serializable)ssTemplate);
            } else if (styleSheet != null) {
                state.putString(KEY_STYLESHEET, styleSheet);
            }
        }
        super.onSaveInstanceState(state);
    }

    /**
     * Called when someone clicks on the button to start looking up a word.
     */
    public void startSearch(View view) {
        //  Ughh... hide the keyboard.
        View tv = getCurrentFocus();
        if (tv != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        String ts = word.getText().toString();
        ts = Util.normalizeWord(ts);
        List<Dictionary> chain = onlyGoodChain;
        if (allWordsAreBad) {
            chain = allBadChain;
        } else if (useBogoDict) {
            chain = bogoChain;
        }
        Log.d(LOGBIT, "starting search for \"" + ts + "\"");
        definition.loadData(getResources().getString(R.string.definition_loading,
                    ts.toUpperCase()), "text/html", null);
        Iterator<Dictionary> iterator = chain.iterator();
        iterator.next().define(ts, iterator, this);
    }

    /**
     * Called when we've got a definition back from our Dictionaries.
     */
    @Override
    public void definitionComplete(String word, Definition def) {
        Log.d(LOGBIT, "definitionComplete() for \"" + word + "\"");
        String html = null;
        if ((def != null) && def.isError()) {
            String msg = (def.getError() != null) ? def.getError() :
                    getResources().getString(def.getErrorResID());
            html = getResources().getString(R.string.definition_error, msg);
        } else if ((def == null) || (def.getRawXML() == null)) {
            html = getResources().getString(R.string.definition_invalid, word.toUpperCase());
        } else if (transformer != null) {
            try {
                html = Util.xmlToHTML(def.getRawXML(), transformer);
            } catch (TransformerException te) {
                html = getResources().getString(R.string.definition_error, te.getMessage());
            }
        }
        if (html == null) {
            //  either the XML -> HTML transformation failed, or earlier, we
            //  were unable to create a Transformer.
            html = getResources().getString(R.string.definition_error,
                    getResources().getString(R.string.error_unknown));
        }
        definition.loadData(html, "text/html", null);
    }
}
