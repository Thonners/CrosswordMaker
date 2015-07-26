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

public class XmlParser2 {

    private static final String LOG_TAG = "xmlParser";

    private static final String CHARSET = "UTF-8" ; // Change this string if necessary. Used to be CHARSET = StandardCharsets.UTF_8 ;

    private static final String XML_TAG_ENTRY_LIST = "entry_list" ; //Hopefully it will ignore the 'version="1.0"' also in the entry tag. Contains the entire feed
    private static final String XML_TAG_ENTRY = "entry" ;           //Hopefully it will ignore the ' id=X' also in the entry tag
    private static final String XML_TAG_WORD = "ew" ;               // Word to be defined
    private static final String XML_TAG_DEFINITION_ZONE = "def" ;   // Tag containing the definition
    private static final String XML_TAG_DEFINITION_NUMBER = "sn" ;  // Tag which precedes the definition in the XML.
    private static final String XML_TAG_DEFINITION_NUMBER2 = "snp" ;  // Tag which precedes the definition in the XML.
    private static final String XML_TAG_DEFINITION = "dt" ;         // Tag containing the definition
    private static final String XML_TAG_WORD_TYPE = "fl" ;          // Tag denoting verb, noun, etc.
    private static final String XML_TAG_LINK_1 ="sx";               // Tag with a link to another word. Ignore this in the definition
    private static final String XML_TAG_LINK_2 ="fw";               // Tag with a link to another word. Ignore this in the definition

    // We don't use namespaces
    private static final String ns = null;

    public XmlParser2() {
        // Empty Constructor
    }

    public ArrayList<Entry> parse(String input) throws XmlPullParserException, IOException {
    //    input = cleanTags(input);
        InputStream stream = new ByteArrayInputStream(input.getBytes(CHARSET));
        return parse(stream);
    }

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


    private Entry readEntry(XmlPullParser parser)  throws XmlPullParserException, IOException {
        Log.d(LOG_TAG,"Entry found, reading entry...");

        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_ENTRY);
        String word = null;
        String wordType = null;
        ArrayList<String> definitions = new ArrayList<String>();

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

    private String readWord(XmlPullParser parser) throws IOException, XmlPullParserException {
        // Read the word in the word tag
        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_WORD);
        String word = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, XML_TAG_WORD);
        Log.d(LOG_TAG, "Word for entry found: " + word);
        return word;
    }
    private String readWordType(XmlPullParser parser) throws IOException, XmlPullParserException {
        // Read the word type from the tag
        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_WORD_TYPE);
        String wordType = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, XML_TAG_WORD_TYPE);
        Log.d(LOG_TAG,"WordType for entry found: " + wordType);
        return wordType ;
    }
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
                        /*} else if (parser.getName().matches(XML_TAG_DEFINITION_NUMBER2)) {
                        parser.next();
                        definitionNumber = definitionNumber + parser.getText() ;
                        Log.d(LOG_TAG,"Definition number2 tag found: " + definitionNumber);//*/
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
                        if (!definitionNumber.matches("")) {
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
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
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
    public static class Entry {
        public String word ;
        public String wordType ;   // Noun, verb, etc.
        public ArrayList<String> definitions = new ArrayList<String>();

        public Entry(String word, String wordType, ArrayList<String> definitions){
            this.word = word ;
            this.wordType = wordType ;
            this.definitions = definitions;
        }

        public String getWord() {
            return word ;
        }
        public String getWordType() {
            return wordType ;
        }
        public ArrayList<String> getDefinitions() {
            return definitions ;
        }
    }
}
