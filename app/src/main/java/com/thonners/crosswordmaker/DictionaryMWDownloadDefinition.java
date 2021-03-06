package com.thonners.crosswordmaker;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * DictionaryMWDownloadDefinition
 * Async task to download definition from Merriam-Webster.
 *
 * @author M Thomas
 * @since 02/02/15
 */
public class DictionaryMWDownloadDefinition extends AsyncTask<Void,Void,String> {

    private static final String LOG_TAG = "DictionaryMWDownloadDef" ;
    private static final String suggestionIdentifier = "<suggestion>";
    private static final String successfulSearchIdentifier = "<entry id=";

    public static final int PROMPT_TV_ID = 2357911 ;

    public static final int SEARCH_NOT_COMPLETED = -1 ; // Initialise to this value, so if cancelled before proper value can be set, it will be known. Also use if search failed for some other reason.
    public static final int SEARCH_SUCCESSFUL = 0 ; // For a search which completed successfully
    public static final int SEARCH_SUGGESTIONS = 1; // For an unsuccessful search, but one with suggestions
    public static final int SEARCH_NO_SUGGESTIONS = 2 ; //For an unsuccessful search that's so bad there are no suggestions

    private static final int WORD_NOT_FOUND = -1 ;
    private static final int WORD = 0 ;
    private static final int WORD_TYPE = 1 ;
    private static final int DEFINITION = 2 ;
    private static final int SUGGESTIONS = 3 ;

    public interface DictionaryMWDownloadDefinitionListener {
        void completionCallBack(ViewGroup theFinalView, int searchSuccessState);
    }
    public DictionaryMWDownloadDefinitionListener listener;
    private Context context ;
    public String searchTerm;
    private int searchSuccess ;

    private LinearLayout progressLinearLayout ;
    private Button searchButton ;

    private final String urlPrefix = "https://www.dictionaryapi.com/api/v1/references/collegiate/xml/";
    private final String urlSuffix = "?key=";
    private String url ;

    /**
     * Constructor.
     * @param appContext Application context
     * @param aSearchTerm The search term entered by the user, to be looked up in the MW dictionary.
     * @param progressLinLayout The LinearLayout containing the progress/loading spinner.
     * @param aSearchButton The search button
     * @param aListener The listener instance of the main activity, to pass the results to at the end.
     */
    public DictionaryMWDownloadDefinition (Context appContext, String aSearchTerm, LinearLayout progressLinLayout, Button aSearchButton, DictionaryMWDownloadDefinitionListener aListener) {
        context = appContext ;
        listener = aListener;
        progressLinearLayout = progressLinLayout ;
        searchButton = aSearchButton ;
        searchTerm = aSearchTerm.replaceAll(" ","%20");     // Replace all is to properly handle spaces for the website url
        searchSuccess = SEARCH_NOT_COMPLETED;

        url = urlPrefix + searchTerm + urlSuffix + context.getString(R.string.dictionary_mw_api_key);
    }

    /**
     * Method called before the {@link #doInBackground(Void...)} method, to show the loading spinner
     * and change the search button to be non-clickable and display 'searching...'.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Show progress bar - animatedly
        progressLinearLayout.setAlpha(0.0f);
        progressLinearLayout.setVisibility(View.VISIBLE);
        progressLinearLayout.animate()
                .alpha(1.0f)
                .setListener(null)
                .setDuration(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_DURATION);

        // Set search button not clickable
        searchButton.setClickable(false);
        searchButton.setText(R.string.searching);
    }

    /**
     * Method to run 'in background', i.e. not on UI thread. Creates the connection to the MW
     * website and downloads the returned results.
     * @param params No params are passed in
     * @return A String representation of the raw XML downloaded from the MW website.
     */
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
            Log.e(LOG_TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return xmlRaw;
    }

    /**
     * Method called once the definition download has completed. Hides the loading spinner, parses
     * the downloaded XML and turns it into views, and, once the exit animation is complete, calls the
     * {@link DictionaryMWDownloadDefinitionListener#completionCallBack(ViewGroup, int)}
     * method on the listener from the main activity to pass the final results view back.
     * @param rawXML The raw XML that has been downloaded from the MW website.
     */
    @Override
    protected void onPostExecute(String rawXML) {
        super.onPostExecute(rawXML);
        // Decode the XML
        final ViewGroup finalView = decodeXML(rawXML);
        // Hide progress bar
        progressLinearLayout.animate()
                .alpha(0.0f)
                .setDuration(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    /**
                     * Called once the exit-animation for the spinner is complete, to pass the
                     * results back to the main Activity.
                     * @param animator The animator controlling the removal of the loading spinner.
                     */
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        Log.d(LOG_TAG,"ProgressLayout Exit Animation complete");
                        // Get rid of the progress view
                        progressLinearLayout.setVisibility(View.GONE);
                        // Return search button to normal
                        searchButton.setClickable(true);
                        searchButton.setText(R.string.search);

                        if (!isCancelled()) {
                            listener.completionCallBack(finalView, searchSuccess);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    /**
     * Method to decode the XML downloaded from Merriam-Webster and to process it into the views
     * which are to be displayed in the results view.
     *
     * This method checks that the results were successful using the tag-keywords denoting successs
     * or failure with regards to looking up the word.
     *
     * @param rawXML The ram XML data as received from Merriam-Webster
     * @return The ViewGroup containing cards with TextViews containing the sorted, formatted results.
     */
    private ViewGroup decodeXML(String rawXML) {
        // Turn the raw XML into a view
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        //view.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding),context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding),context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding),context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding));
        if (rawXML.contains(suggestionIdentifier)) {
            // Then the word was not found. Create a linear layout with multiple TextViews, one on top of the other. Hide all text views of suggested words to stop accidental cheating.
            searchSuccess = SEARCH_SUGGESTIONS ;
            // Make the prompt.
            String wordNotFoundPrompt = context.getResources().getString(R.string.dictionary_word_not_found) + " " + context.getResources().getString(R.string.dictionary_suggestions_prompt);
            TextView promptTV = createTextView(wordNotFoundPrompt, WORD_NOT_FOUND);
            promptTV.setId(PROMPT_TV_ID);
            promptTV.setAlpha(0.0f);
            view.addView(promptTV,0);

            // Get rid of all the closing tags
            rawXML = rawXML.replaceAll("</suggestion>", "") ;
            rawXML = rawXML.replaceAll("</entry_list>","");

            // Split the results by the <suggestion> tag. First value (at index 0) will not be of interest.
            String[] suggestions = rawXML.split("<suggestion>");
            for (int i = 1 ; i < suggestions.length ; i++ ) {   // Start at 1 as first value is rubbish (see above)
                //TextView newTextView = createHiddenTextView(suggestions[i], SUGGESTIONS);
                //view.addView(newTextView,i);

                Card card = new Card(context,suggestions[i]);
                view.addView(card, i);
                // Prep for entry animation
                card.setTranslationY(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_Y_TRANSLATE);
                card.setAlpha(0.0f);
            }
        } else if (rawXML.contains(successfulSearchIdentifier)) {
            searchSuccess = SEARCH_SUCCESSFUL;
            parseSuccessfulXML(rawXML, view);
        } else {
            searchSuccess = SEARCH_NO_SUGGESTIONS;
        }

        return view ;
    }

    /**
     * Method to parse the downloaded XML, and turn it into a card entry.
     * The card will be transparent, (alpha = 0f) and offset, ready to be animated into view.
     *
     * Actual parsing done by separate {@link XmlParser} class. This method just adds the results to
     * a new CardView, and adds that card to the parent view.
     *
     * @param xmlRaw The XML as received
     * @param viewGroup The parent view to which to add the resulting card.
     */
    private void parseSuccessfulXML(String xmlRaw, LinearLayout viewGroup) {
        Log.d(LOG_TAG, "Adding results to the results view");

        XmlParser parser = new XmlParser();
        try {
            ArrayList<XmlParser.Entry> entries = parser.parse(xmlRaw);
            Log.d(LOG_TAG, "Cycling through definitions");

            for (XmlParser.Entry entry : entries) {
                CardView card = new CardView(context);
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(createTextView(entry.getWord(), WORD));
                linearLayout.addView(createTextView(entry.getWordType(), WORD_TYPE));
                for (String def : entry.getDefinitions()) {
                    linearLayout.addView(createTextView(def, DEFINITION));
                }
                linearLayout.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding), context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding), context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding), context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding));
                card.addView(linearLayout);
                card.setUseCompatPadding(true); // Forces same padding on Lollipop as is on pre-L
                viewGroup.addView(card);
                // Position/transparent it ready for the entry animation
                card.setTranslationY(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_Y_TRANSLATE);
                card.setAlpha(0.0f);
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Was unable to parse the xmlRaw :(");
            Log.d(LOG_TAG, e.getMessage());
            searchSuccess = SEARCH_NOT_COMPLETED;
        }
    }

    /**
     * See {@link #createTextView(String, boolean, int)}. Defaults to true for createVisible.
     */
    private TextView createTextView(String textToDisplay, int displayType) {
        return createTextView(textToDisplay, true, displayType);
    }

    /**
     * @param textToDisplay The text to be shown
     * @param createVisible Whether to make the TextView visible, or create it hidden.
     * @param displayType The type of text to be displayed - from the public static options of this class.
     * @return A text view of the input text, with the correct formatting as specified by the displayType.
     */
    private TextView createTextView(String textToDisplay, boolean createVisible, int displayType) {
        TextView tv = new TextView(context);
        tv.setText(textToDisplay);
        // Put other standard settings in here, such as padding, or an xml file to use

        if (createVisible) {
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }

        float textSize = 0 ;

        // Get settings for each word type
        switch (displayType) {
            case WORD:  // Bold
                tv.setTypeface(null, Typeface.BOLD);
                textSize = context.getResources().getDimension(R.dimen.dictionary_word);
                break;
            case WORD_TYPE: // Italic
                tv.setTypeface(null, Typeface.ITALIC);
                textSize = context.getResources().getDimension(R.dimen.dictionary_word_type);
                break;
            case DEFINITION: // Normal
                textSize = context.getResources().getDimension(R.dimen.dictionary_definition);
                break;
            case WORD_NOT_FOUND: // Bold + bigger?
                tv.setTypeface(null, Typeface.BOLD);
                textSize = context.getResources().getDimension(R.dimen.dictionary_word_not_found);
                break;
            case SUGGESTIONS: // Normal?
                textSize = context.getResources().getDimension(R.dimen.dictionary_suggestion);
                break;
        }
        // Set size
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);

        return tv ;
    }
}
