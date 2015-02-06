package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mat on 02/02/15.
 */
public class DictionaryMWDownloadDefinition extends AsyncTask<Void,Void,String> {

    private static final String LOG_TAG = "DictionaryMWDownloadDefinition" ;
    private static final String suggestionIdentifier = "<suggestion>";
    private static final String successfulSearchIdentifier = "<entry id=";

    public static final int PROMPT_TV_ID = 2357911 ;

    public static final int SEARCH_NOT_COMPLETED = -1 ; // Initialise to this value, so if cancelled before proper value can be set, it will be known.
    public static final int SEARCH_SUCCESSFUL = 0 ; // For a search which completed successfully
    public static final int SEARCH_SUGGESTIONS = 1; // For an unsuccessful search, but one with suggestions
    public static final int SEARCH_NO_SUGGESTIONS = 2 ; //For an unsuccessful search that's so bad there are no suggestions

    public static interface DictionaryMWDownloadDefinitionListener {
        public abstract void completionCallBack(ViewGroup theFinalView, int searchSucessState);
    }
    public DictionaryMWDownloadDefinitionListener listener;
    private Context context ;
    public String searchTerm;
    private int searchSuccess ;
    public String definition ;

    private String urlPrefix = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/";
    private String urlSuffix = "?key=";
    private String url ;
//    private View finalView ;    // Final view to be passed back to the DictionaryPageFragment to be put in the answers

    public DictionaryMWDownloadDefinition (Context appContext, String aSearchTerm, DictionaryMWDownloadDefinitionListener aListener) {
        context = appContext ;
        listener = aListener;
        searchTerm = aSearchTerm;
        searchSuccess = SEARCH_NOT_COMPLETED;

        url = urlPrefix + searchTerm + urlSuffix + context.getString(R.string.dictionary_mw_api_key);
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        String xmlRaw = "";
        try {
            HttpResponse response = client.execute(request);
            InputStream in;
            in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str.append(line);
            }
            in.close();
            xmlRaw = str.toString();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Log.e(LOG_TAG, e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(LOG_TAG, e.getMessage());
        }
        return xmlRaw;
    }

    @Override
    protected void onPostExecute(String rawXML) {
        // TODO Auto-generated method stub
        super.onPostExecute(rawXML);
        if (!isCancelled()) {
            ViewGroup finalView = decodeXML(rawXML);


            listener.completionCallBack(finalView, searchSuccess);
        }
    }

    private ViewGroup decodeXML(String rawXML) {
        // Turn the raw XML into a view
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        if (rawXML.contains(suggestionIdentifier)) {
            // Then the word was not found. Create a linear layout with multiple TextViews, one on top of the other. Hide all text views of suggested words to stop accidental cheating.
            searchSuccess = SEARCH_SUGGESTIONS ;
            // Make the prompt.
            String wordNotFoundPrompt = context.getResources().getString(R.string.dictionary_word_not_found) + " " + context.getResources().getString(R.string.dictionary_suggestions_prompt);
            TextView promptTV = createTextView(wordNotFoundPrompt);
            promptTV.setId(PROMPT_TV_ID);
            view.addView(promptTV,0);

            // Get rid of all the closing tags
            rawXML = rawXML.replaceAll("</suggestion>", "") ;
            rawXML = rawXML.replaceAll("</entry_list>","");

            // Split the results by the <suggestion> tag. First value (at index 0) will not be of interest.
            String[] suggestions = rawXML.split("<suggestion>");
            for (int i = 1 ; i < suggestions.length ; i++ ) {   // Start at 1 as first value is rubbish (see above)
                TextView newTextView = createHiddenTextView(suggestions[i]);
                view.addView(newTextView,i);
            }
        } else if (rawXML.contains(successfulSearchIdentifier)) {
            searchSuccess = SEARCH_SUCCESSFUL;
            // TODO: Properly parse the successful XML
            //TextView newTextView = createTextView(rawXML);
            view.addView(parseSuccessfulXML(rawXML));
        } else {
            searchSuccess = SEARCH_NO_SUGGESTIONS;
        }

        return view ;
    }

    private ViewGroup parseSuccessfulXML(String xmlRaw) {
        // Method to turn the raw XML into something useful
        LinearLayout viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);
            Log.d(LOG_TAG, "Adding results to the results view");

        XmlParser parser = new XmlParser();
        try {
            ArrayList<XmlParser.Entry> entries = parser.parse(xmlRaw);
            Log.d(LOG_TAG, "Cycling through definitions");

            for (XmlParser.Entry entry : (ArrayList<XmlParser.Entry>) entries) {
            Log.d(LOG_TAG, "ABABABA");
                viewGroup.addView(createTextView(entry.getWord()));
                viewGroup.addView(createTextView(entry.getWordType()));
                viewGroup.addView(createTextView(entry.getDefinitions().get(0)));
                viewGroup.addView(createTextView(""));
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Was unable to parse the xmlRaw :(");
            Log.d(LOG_TAG, e.getMessage());
        }

        return viewGroup;
    }

        // Default methods for creating hidden and visible TextViews
        private TextView createHiddenTextView(String textToDisplay) {
            return createTextView(textToDisplay, false);
        }
        private TextView createTextView(String textToDisplay) {
        return createTextView(textToDisplay, true);
    }
    // Proper method that creates the TextView and sets its visibility
    private TextView createTextView(String textToDisplay, boolean createVisible) {
        TextView tv = new TextView(context);
        tv.setText(textToDisplay);
        // Put other standard settings in here, such as padding, or an xml file to use

        if (createVisible) {
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }

        return tv ;
    }
}