# Scrapple

This is a dictionary which makes up definitions for bogus words.  Uhh, you know,
for cheating at Scrabble.  "Well, let's just check the official dictionary...
see, "reunghit" *is* a word!"

(Look, I'm a sad old man; I've accepted that I can't beat my daughter in a
*fair* contest, so I'm forced to resort to... "unorthodox" methods.)

Relevant link: https://www.boardgamegeek.com/article/26688666#26688666

### How does it work?

When someone looks up a word, the first thing we do is look it up through the
Merriam-Webster Dictionary API; if it's a real word, we display the real
definition.  If not, we make something up.  That second step can be quietly
turned off, so that when your *opponent* uses a bogus word, it correctly says
that it's not real.  (You can also disable the lookup entirely, so that it says
even *valid* words are bad, in case your opponent is using words I hate, like
"qi.")

The way we're doing our lookups is indicated by one square in the logo image.
Here's the normal logo (minus the red arrow):

![Default behavior](doc/help_logo_default.png)

Tapping in the upper-right area of the logo image turns off the bogo-dictionary,
and that dark blue square turns slightly lighter blue:

![No bogo-dictionary](doc/help_logo_nobogo.png)

Tapping in the upper-left area of the logo image turns off the *good* dictionary,
so that *all* words are considered bad, and that square turns pink:

![No words are good](doc/help_logo_nogood.png)

### Fun with definitions

The bogus definitions are generated by reading a text file,
`app/src/main/res/raw/patterns.txt`.  Comments in that file describe the
details, but basically, we use patterns like "starts with 'un'" or "ends with
'ed'" as starting points for choosing random definitions.  There are also "mad
lib" style bits (probably easier to fiddle with if you don't know regular
expressions) where you can define your own lists of strings which will be
substituted at random:

    @CATNAME@
        Nibbles
        Chi-Chi
        Steve

and then you can use them in definitions:

    A subcutaneous fat deposit on @CATNAME@.

If you make changes to the file, it will probably blow up unit tests in
`BogoDictionaryTest.java`, but as they say, if you want to make an omelette, you
have to punch a few chickens in the face.

### API Key

The Merriam-Webster Dictionary API requires an API key, which is free for
non-commercial use as long as its usage remains below a certain limit, which is
why I'm not putting my own API key on the Internet.  In order to build & run
Scrapple, you need to get your own API key from M-W; see
https://www.dictionaryapi.com/.

Once you've got your key, create a file, `app/src/main/res/values/keys.xml`,
with this content:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <!--  your API key goes here  -->
        <string name="m_w_api_key">01234567-89ab-cdef-0123-456789abcdef</string>
    </resources>
