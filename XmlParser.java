package com.thonners.crosswordmaker;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mat on 04/02/15.
 */
public class XmlParser {

    private static final String LOG_TAG = "xmlParser";

    private static final String XML_TAG_ENTRY_LIST = "entry_list" ; //Hopefully it will ignore the 'version="1.0"' also in the entry tag. Contains the entire feed
    private static final String XML_TAG_ENTRY = "entry" ;           //Hopefully it will ignore the ' id=X' also in the entry tag
    private static final String XML_TAG_WORD = "ew" ;               // Word to be defined
    private static final String XML_TAG_DEFINITION_ZONE = "def" ;   // Tag containing the definition
    private static final String XML_TAG_DEFINITION_NUMBER = "sn" ;  // Tag which precedes the definition in the XML.
    private static final String XML_TAG_DEFINITION = "dt" ;         // Tag containing the definition
    private static final String XML_TAG_WORD_TYPE = "fl" ;          // Tag denoting verb, noun, etc.
    private static final String XML_TAG_LINK_1 ="sx";               // Tag with a link to another word. Ignore this in the definition
    private static final String XML_TAG_LINK_2 ="fw";               // Tag with a link to another word. Ignore this in the definition

    // We don't use namespaces
    private static final String ns = null;

    public XmlParser() {
        // Empty Constructor
    }

    public ArrayList<Entry> parse(String input) throws XmlPullParserException, IOException  {
        input = cleanTags(input);
        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
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

    // DOESN'T WORK ATM
    private String cleanTags(String stringToClean) {
        // Remove any tags that will cause trouble
        ArrayList<String> badTags = new ArrayList<>();
        badTags.add("vi");
        String cleanString = "";
        for (String badTag : badTags) {
            cleanString = stringToClean.replaceAll("<" + badTag  + ">[^</ "+ badTag + ">]*</ "+ badTag + ">","") ;
        }
        return cleanString ;
    }

    private ArrayList<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Entry> entries = new ArrayList();

        Log.d(LOG_TAG,"Reading Feed...");

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
//        String definition = null; //DELETE ME
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
        Log.d(LOG_TAG,"Word for entry found: " + word);
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

    private ArrayList<String> readDefinitions(XmlPullParser parser) throws IOException, XmlPullParserException {
        // Extract all the definitions from the various numbered definitions
        ArrayList<String> definitions = new ArrayList<String>();
        String definitionNumber = "";


        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_DEFINITION_ZONE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName() ;
            if (name.equals(XML_TAG_DEFINITION_NUMBER)) {
                definitionNumber = readDefinitionNo(parser);
            } else if (name.equals(XML_TAG_DEFINITION)) {
                String def = readDefinition(parser);
                if (!def.matches("")) {
                    definitions.add(definitionNumber + ": " + def);
                }
            } else  {
                skip(parser);
            }
        }

        return definitions ;
    }

    private String readDefinitionNo(XmlPullParser parser)throws IOException, XmlPullParserException {
    // Extract the value from within the tag
        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_DEFINITION_NUMBER);
        String definitionNo = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, XML_TAG_DEFINITION_NUMBER);
        Log.d(LOG_TAG,"DefinitionNo for entry found: " + definitionNo);
        return definitionNo ;
    }

    private String readDefinition(XmlPullParser parser)throws IOException, XmlPullParserException {
    // Extract the value from within the tag
        String definition = "";
        parser.require(XmlPullParser.START_TAG, ns, XML_TAG_DEFINITION);
        definition = readText(parser);
         // Skip first character which is always ':'. Put it in manually above to have a space between ':' and definition. If no definition found, return definition to blank so it won't be added to the definitions ArrayList in readDefinitions()
        if (definition.length() > 1) {
            definition = definition.substring(1) ; // Skip first character which is always ':'. Put it in manually above to have a space between ':' and definition
        } else {
            definition = "";
        }

        String tagName = parser.getName();
        while (!tagName.equals(XML_TAG_DEFINITION)) {

            if(tagName.equals(XML_TAG_LINK_1) || tagName.equals(XML_TAG_LINK_2)) {
                Log.d(LOG_TAG, "Ignoring link tags, but adding to definition");
                definition = definition + readText(parser);
            } else {
                Log.d(LOG_TAG, "skipping bunf");
                parser.next();
            }
            if (parser.getEventType() == XmlPullParser.END_TAG) {
                tagName = parser.getName();
            }
        }
        Log.d(LOG_TAG,"Definition found: " + definition);

        parser.require(XmlPullParser.END_TAG, ns, XML_TAG_DEFINITION);
        return definition ;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
    // Extract the value from within the tag
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
            Log.d(LOG_TAG, "Reading text: " + result);
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.d(LOG_TAG,"Skipping entry...");
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
        public ArrayList<String> definitions = new ArrayList<String>(); // Might need to make this a list

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