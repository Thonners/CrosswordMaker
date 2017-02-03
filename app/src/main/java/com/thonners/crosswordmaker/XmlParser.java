package com.thonners.crosswordmaker;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * XMLParser
 * Parse the XML returned by the Merriam Webster dictionary
 * Format it into word, word-type and definition
 *
 * Created by Thonners on 26/07/15.
 */

public class XmlParser {

    private static final String LOG_TAG = "xmlParser";

    private static final String CHARSET = "UTF-8" ; // Change this string if necessary. Used to be CHARSET = StandardCharsets.UTF_8 ;

    private static final String XML_TAG_ENTRY_LIST = "entry_list" ; //Hopefully it will ignore the 'version="1.0"' also in the entry tag. Contains the entire feed
    private static final String XML_TAG_ENTRY = "entry" ;           //Hopefully it will ignore the ' id=X' also in the entry tag
    private static final String XML_TAG_WORD = "ew" ;               // Word to be defined
    private static final String XML_TAG_DEFINITION_ZONE = "def" ;   // Tag containing the definition
    private static final String XML_TAG_DEFINITION_NUMBER = "sn" ;  // Tag which precedes the definition in the XML.
    private static final String XML_TAG_DEFINITION = "dt" ;         // Tag containing the definition
    private static final String XML_TAG_WORD_TYPE = "fl" ;          // Tag denoting verb, noun, etc.

    // We don't use namespaces
    private static final String ns = null;

    /**
     * Empty Constructor
     */
    public XmlParser() {
    }

    /**
     * Converts the input String to an InputStream, then calls {@link #parse(InputStream)}
     * @param input The raw XML to be parsed
     */
    public ArrayList<Entry> parse(String input) throws XmlPullParserException, IOException {
        InputStream stream = new ByteArrayInputStream(input.getBytes(CHARSET));
        return parse(stream);
    }

    /**
     * Converts rawXML, as downloaded/received from the MerriamWebster dictionary and turns it into
     * a list of {@link Entry}s.
     *
     * This method just sets up the {@link XmlPullParser}, then calls {@link #readFeed(XmlPullParser)}.
     * @param in The InputStream of raw XML received from the MW dictionary.
     * @return An ArrayList of {@link Entry}s, extracted from the input rawXML.
     * @throws XmlPullParserException
     * @throws IOException
     */
    public ArrayList<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Method to read the XML feed, and to turn it into a list of {@link Entry}s.
     *
     * Loops through all objects in the parser, ignoring the {@link XmlPullParser#START_TAG}, and
     * finishing at the {@link XmlPullParser#END_TAG}.
     *
     * The useful results are read by {@link #readEntry(XmlPullParser)}. Other entries are {@link #skip(XmlPullParser)}'ed
     *
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @return An ArrayList of {@link Entry}s, extracted from the input rawXML.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private ArrayList<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Entry> entries = new ArrayList<>();

        Log.d(LOG_TAG, "Reading Feed...");

        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_ENTRY_LIST);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(XML_TAG_ENTRY)) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    /**
     * Wrapper method to read an 'entry', where the entry is the complete definition as received.
     *
     * For each type of XML tag read, this method then calls the appropriate method, from
     * {@link #readWord(XmlPullParser)}, {@link #readWordType(XmlPullParser)}, or {@link #readDefinitions(XmlPullParser)},
     * and adds the result to the appropriate part of the entry.
     *
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @return An {@link Entry} containing the word, word type, definition, etc. as extracted from the XML.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Entry readEntry(XmlPullParser parser)  throws XmlPullParserException, IOException {
        Log.d(LOG_TAG,"Entry found, reading entry...");

        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_ENTRY);
        String word = null;
        String wordType = null;
        ArrayList<String> definitions = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals(XML_TAG_WORD)) {
                word = readWord(parser);
            } else if (name.equals(XML_TAG_WORD_TYPE)) {
                wordType = readWordType(parser);
            } else if (name.equals(XML_TAG_DEFINITION_ZONE)) {
                definitions = readDefinitions(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(word, wordType, definitions);
    }

    /**
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @return The String representation of the word being defined.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readWord(XmlPullParser parser) throws IOException, XmlPullParserException {
        // Read the word in the word tag
        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_WORD);
        String word = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, XML_TAG_WORD);
        Log.d(LOG_TAG, "Word for entry found: " + word);
        return word;
    }

    /**
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @return The type of word being defined, e.g. verb, adjective, noun, etc.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readWordType(XmlPullParser parser) throws IOException, XmlPullParserException {
        // Read the word type from the tag
        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_WORD_TYPE);
        String wordType = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, XML_TAG_WORD_TYPE);
        Log.d(LOG_TAG,"WordType for entry found: " + wordType);
        return wordType ;
    }

    /**
     * Method to extract the definition(s) from the raw XML.
     *
     * For each separate definition, a new entry in the ArrayList is created.
     *
     * Complexity is added by the nested tags around certain words in a definition received from MW.
     * e.g. Links to other words, etc. which are used on the website but not by this app.
     * The method deals with these extra tags by keeping a count of how 'deep' the current XML object
     * is, and then continuing to read data and add it to the current definition until the depth == 0.
     *
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @return An ArrayList of String definitions for the word being defined.
     * @throws IOException
     * @throws XmlPullParserException
     */
    private ArrayList<String> readDefinitions(XmlPullParser parser)  throws IOException, XmlPullParserException {
        // Extract all the definitions from the various numbered definitions
        ArrayList<String> definitions = new ArrayList<String>();
        String definitionNumber = "";

        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_DEFINITION_ZONE);

        // Track how 'deep' into the definition zone's nested tags we are.
        int depth = 1 ;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    Log.d(LOG_TAG,"End tag found for: " + parser.getName() + ". Depth now = " + depth);
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    Log.d(LOG_TAG,"Start tag found for: " + parser.getName() + ". Depth now = " + depth);
                    if (parser.getName().matches(XML_TAG_DEFINITION_NUMBER)) {
                        definitionNumber = "" ;
                        int defNoDepth = 1;
                        while (defNoDepth != 0) {
                            switch (parser.next()) {
                                case XmlPullParser.END_TAG:
                                    defNoDepth--;
                                    Log.d(LOG_TAG, "End tag found for: " + parser.getName() + ". defNoDepth now = " + depth);
                                    if (parser.getName().matches(XML_TAG_DEFINITION_NUMBER)) {
                                        depth--;
                                    }
                                    break;
                                case XmlPullParser.START_TAG:
                                    defNoDepth++;
                                    Log.d(LOG_TAG, "Start tag found for: " + parser.getName() + ". defNoDepth now = " + depth);
                                    break;
                                case XmlPullParser.TEXT:
                                    definitionNumber = definitionNumber + parser.getText();
                                    Log.d(LOG_TAG, "Some defNo text found: " + parser.getText());
                                    break;
                            }
                        }
                            Log.d(LOG_TAG,"Definition number tag found: " + definitionNumber);
                    } else if (parser.getName().matches(XML_TAG_DEFINITION)) {
                        parser.next();
                        String definition = parser.getText() ;
                        int defDepth = 1 ;
                        while (defDepth != 0) {
                            switch (parser.next()) {
                                case XmlPullParser.END_TAG:
                                    defDepth--;
                                    Log.d(LOG_TAG, "Within definition tags, end tag found for: " + parser.getName() + ". defDepth now = " + defDepth);
                                    if (parser.getName().matches(XML_TAG_DEFINITION)) {
                                        // Reduce depth by 1 here as the closing <dt> tag will not be picked up by the main loop as when this inner while loop ends, parser.next() will be called.
                                        depth-- ;
                                    }
                                    break;
                                case XmlPullParser.START_TAG:
                                    defDepth++;
                                    Log.d(LOG_TAG, "Within definition tags, start tag found for: " + parser.getName() + ". defDepth now = " + defDepth);
                                    break ;
                                case XmlPullParser.TEXT:
                                    Log.d(LOG_TAG,"Some definition text: " + parser.getText());
                                    definition = definition + parser.getText();
                                    break;
                            }
                        }
                        if (definition.startsWith(":")) {
                            definition = definition.substring(1);   // Remove the ':' from the front of the definition string if it exists, so a space can be put in
                        }
                        if (!definitionNumber.matches("") && !definitionNumber.matches("null")) {
                            definition = definitionNumber + ": " + definition ;
                        }
                        Log.d(LOG_TAG,"Definition found: " + definition);
                        definitions.add(definition);
                    }
                    break;
            }
        }

        return definitions ;
    }

    /**
     * Method to actually pull the text out from between a set of XML tags.
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @return The String from between the tags
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Method to skip to the next entry in the parser.
     * @param parser The parser instance containing the rawXML downloaded from MW.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Class of dictionary entries, containing variables for the word being defined, the type of word,
     * and the definition.
     */
    public static class Entry {
        public String word ;
        public String wordType ;   // Noun, verb, etc.
        public ArrayList<String> definitions = new ArrayList<>();

        /**
         * Constructor
         * @param word The word defined by the definition
         * @param wordType The type of word being defined.
         * @param definitions An ArrayList of all the definitions for this word.
         */
        public Entry(String word, String wordType, ArrayList<String> definitions){
            this.word = word ;
            this.wordType = wordType ;
            this.definitions = definitions;
        }

        /**
         * @return The word to which this entry refers.
         */
        public String getWord() {
            return word ;
        }

        /**
         * @return The type of word being defined by this entry. E.g. verb, noun, adjective, etc.
         */
        public String getWordType() {
            return wordType ;
        }

        /**
         * @return An ArrayList of all the definitions for this word.
         */
        public ArrayList<String> getDefinitions() {
            return definitions ;
        }
    }
}
