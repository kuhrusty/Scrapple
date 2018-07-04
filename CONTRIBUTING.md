# Scrapple

If you're considering building this yourself, this is for you.

### Interesting stuff in the code

- XSLT to turn the XML definition into HTML
  (`MainActivity.definitionComplete()`, `Util.xmlToHTML()`,
  `definition.xsl`)
- Asynchronously handling a web service response (although this isn't
  done particularly *well)* (`MWDictionary.java`)
- Reading regular expressions from file, recursing through mad-lib tree
  (`BogoDictionary.java` and `PatternFileParser.java`)
- Dropping in replacement `android.util.Log` etc. during unit tests

### Checking out & building

In Android Studio 3.1.3:

1. File -> New -> Project from Version Control -> GitHub

2. Clone Repository:
   - Repository URL: https://github.com/kuhrusty/Scrapple.git
   - Parent Directory: (whatever you want; probably
     /home/.../AndroidStudioProjects)
   - Directory Name: (whatever you want; probably Scrapple)

   That should check the stuff out and start a Gradle build.  (If you
   get errors about missing .iml files, ignore them--do *not* remove the
   modules it's talking about.)

3. Hit the "Sync Project with Gradle Files" button in the toolbar.  This
   should generate the .iml files it was complaining about.

4. In order to build, you'll need a Merriam-Webster Dictionary API key,
   and I don't know yet whether I should check mine in, so see below (or
   send me email & I'll send you my key).  I will probably end up
   checking it in and removing this step.

### API Key

The Merriam-Webster Dictionary API requires an API key, which is free
for non-commercial use as long as its usage remains below a certain
limit, which is why I'm not putting my own API key on the Internet.  In
order to build & run Scrapple, you need to get your own API key from
M-W; see https://www.dictionaryapi.com/.  (This is kind of a pain.)

Once you've got your key, create a file,
`app/src/main/res/values/keys.xml`, with this content:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <!--  your API key goes in here  -->
        <string name="m_w_api_key">01234567-89ab-cdef-0123-456789abcdef</string>
    </resources>

Then you should be able to compile.