package com.kuhrusty.scrapple;

/**
 * Originally I was using SAX to parse the M-W dictionary definition XML into a
 * Java object with the complete structure of the definition, and that's what
 * this class was; however, once I switched to XSLT, I quit caring about the
 * structure of the XML (outside of my XSL template, and the MWXMLHandler which
 * looks at the XML to see whether it's a valid definition).  So, now this class
 * is just a wrapper around the raw XML string, so it's pretty silly.
 *
 * <p>Also, I later added error stuff, so that errors encountered down in the
 * chain of Dictionaries can be propagated back up to the caller.
 */
public class Definition {
    private final String rawXML;

    //  well, as long as we're here, let's also use this for bringing back
    //  error messages.  If we encountered an error, one of these two members
    //  will be set.
    private final String error;
    private final int errorResID;

    /**
     * @param rawXML may be null.
     */
    public Definition(String rawXML) {
        this.rawXML = rawXML;
        error = null;
        errorResID = 0;
    }

    /**
     * @param isError true if xmlOrError is an error message, false if it's
     *                the raw XML of the definition.
     * @param xmlOrError an error message or a definition, depending on the
     *                   value of isError.
     */
    public Definition(boolean isError, String xmlOrError) {
        if (isError) {
            rawXML = null;
            error = xmlOrError;
        } else {
            rawXML = xmlOrError;
            error = null;
        }
        errorResID = 0;
    }

    /**
     * @param errorResID may be 0.
     */
    public Definition(int errorResID) {
        rawXML = null;
        error = null;
        this.errorResID = errorResID;
    }

    /**
     * May return null, even if isError() is false.
     */
    public String getRawXML() {
        return rawXML;
    }

    /**
     * Returns true if there was an error retrieving the definition.  Note that
     * not <i>finding</i> a definition is not an error, but having no network or
     * no API key <i>is.</i>
     */
    public boolean isError() {
        return (error != null) || (errorResID != 0);
    }
    /**
     * Returns the error message, or null.  If isError() is true and this
     * returns null, check getErrorResID().
     */
    public String getError() {
        return error;
    }
    /**
     * Returns the error string resource ID, or 0.
     */
    public int getErrorResID() {
        return errorResID;
    }
}
