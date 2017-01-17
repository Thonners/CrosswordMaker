package com.thonners.crosswordmaker;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class AnagramPageFragment extends Fragment {

    private static final String LOG_TAG = "AnagramPageFragment" ;

    private int hashMapSize = 315000 ;   // Size for the hashMap to allow loadFactor < 0.75 whilst fitting all the words in the dictionary
    private HashMap<String, ArrayList<String>> dictionaryHM = new HashMap<>(hashMapSize);    // Hashmap to store the options in

    private ArrayList<String> dictionary = new ArrayList<>() ;
    private ArrayList<String[]> dictionaryByLetter= new ArrayList<>() ;
    private ArrayList<ArrayList<String>> dictionaryByLength = new ArrayList<>() ;

    private boolean serverAvailable = false ;
    private boolean dictionaryLoaded = false ;
    private boolean buttonIsClear  = false ;    // Variable to store whether the button next to the search bar should be 'search' or 'clear'

    private OnAnagramFragmentListener mListener;

    private Button searchButton ;
    private EditText inputBox ;
    private LinearLayout resultsLinearLayout ;
    private LinearLayout progressSpinnerLinLayout ;
    private int resultCount = 0 ;

    public AnagramPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_anagram, container, false);
        searchButton = (Button) view.findViewById(R.id.anagram_search);
        inputBox = (EditText) view.findViewById(R.id.anagram_search_input);
        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.d(LOG_TAG,"IME_ACTION_SEARCH has matched.");
                    searchClicked();
                    return true;
                }
                return false;
            }
        });
        resultsLinearLayout = (LinearLayout) view.findViewById(R.id.anagram_results_layout);
        progressSpinnerLinLayout = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress) ;
        return view ;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnAnagramFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAnagramFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAnagramFragmentListener {
        void searchDictionary(String searchTerm);
    }

    /**
     * Method to check whether the sever is accessible, and if not, to load the dictionary locally
     */
    private void checkServer() {
        ServerConnection.ServerConnectionListener listener = new ServerConnection.ServerConnectionListener() {
            @Override
            public void serverConnectionResponse(ArrayList<String> answers) {

            }

            @Override
            public void setServerAvailable(boolean isAvailable) {
                serverAvailable = isAvailable ;
                if (serverAvailable) {
                    setSearchButtonClickable();
                } else {
                    loadDictionaryLocally();
                }
            }
        } ;
        ServerConnection serverConnection = new ServerConnection(listener) ;
        serverConnection.testServerConnection();
    }

    /**
     * Method to load the local copy of the sopwads dictionary into memory and prepare it for anagram/wordfit lookup
     */
    private void loadDictionaryLocally() {
        // Log how long it takes to load the dictionary
        long startTime = System.currentTimeMillis() ;
        Log.d(LOG_TAG,"AnagramPageFragment onCreate called @ millis: " + startTime);

        // Check whether Low RAM mode enabled on settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity()) ;
        boolean lowRAM = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_LOW_RAM, false) ;

        // Load the dictionaries in from the resources, provided low RAM mode not enabled
        if (!lowRAM){
            LoadDictionaryTask loadDictionaryTask = new LoadDictionaryTask();
            loadDictionaryTask.execute();
        } else {
            Log.d(LOG_TAG, "Low RAM mode enabled, so not reading the dictionary yet.");
            // Show a toast to explain why the solver isn't available
            Toast.makeText(getContext(),"Cannot connect to server, so anagram/wordfit solver unavailable when 'Low-RAM' mode enabled",Toast.LENGTH_LONG).show(); ;
        }

        long stopTime = System.currentTimeMillis();
        Log.d(LOG_TAG,"Dictionary loaded after " + (stopTime - startTime) + " millis");

    }

    /**
     * Called when the search button is clicked. Clears results view and hides the keyboard.
     */
    private void searchClicked() {
        // Clear view ready for results, then search
        Log.d(LOG_TAG, "Search button clicked. ");
        hideKeyboard();
        clearResults(true); // Move the call to search() into the clearResults method, so it can be executed after any exit animations if required
    }

    /**
     * Method to animate the clearing of the results views. The call to search() is at the end of this
     * method to
     * @param search If true, and there are no results in the results view, calls {@link AnagramPageFragment#search()}
     */
    private void clearResults(boolean search) {
        // Return to 0
        resultCount = 0 ;

        // Create the listener reference that will eventually call search(), but for all but the last animation will be null
        Animator.AnimatorListener animatorListener = null ;

        // Clear the results view
        if (resultsLinearLayout.getChildCount() > 0 ) {
            for (int i = resultsLinearLayout.getChildCount() - 1 ; i >= 0 ; i--) {
                if (i == 0 && search) {
                    // Create the listener that will call search()
                    animatorListener = new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            // Actually remove the views
                            resultsLinearLayout.removeAllViews();
                            // Search
                            search();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    };
                }

                View view = resultsLinearLayout.getChildAt(i) ;
                if (view != null) {
                    Log.d(LOG_TAG, "Animating removal. i = " + i) ;
                    view.animate()
                            .alpha(0.0f)
                            .translationY(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_Y_TRANSLATE)
                            .setStartDelay(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_STAGGER * resultCount)
                            .setDuration(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_DURATION)
                            .setListener(animatorListener);
                    resultCount++ ;
                }
            }

            // Return to 0
            resultCount = 0 ;
        } else {
            if (search) search() ;
        }

    }

    /**
     * Execute the search for the solution to either the anagram or word-fit. (Called by clearResults(true))
     *
     * Anagram or word-fit is decided based on whether the search term contains '.' (unknown letters) or not.
     */
    private void search() {
        // Search for either anagrams or word fits depending on whether the input text contains a '.'

        Log.d(LOG_TAG, "Checking input... ");
        // First check input and ignore any non letters or '.'s
        String searchStringOriginal = inputBox.getText().toString().replaceAll(" ",""); // Remove all spaces
        String searchString = "";
        boolean containsIllegalCharacters = false ;

        // Check all characters in the search are legal for either anagram or word-fit (i.e. letters or '.')
        for (int i = 0 ; i < searchStringOriginal.length() ; i++ ) {
            if (Character.isLetter(searchStringOriginal.toLowerCase().charAt(i)) || searchStringOriginal.charAt(i) == '.') {
                Log.d(LOG_TAG, "Adding '" + searchStringOriginal.substring(i,i+1) + "' to search string (currently = " + searchString + " as this is a valid character for input.");
                // Acceptable character, so add to array
                searchString = searchString + searchStringOriginal.substring(i,i+1);
            } else {
                Log.d(LOG_TAG, "Illegal character found in input: " + searchStringOriginal.substring(i,i+1));
                containsIllegalCharacters = true ; // Doesn't matter if this is overwritten multiple times
            }
        }


        Log.d(LOG_TAG, "Original input string: " + searchStringOriginal + " || Tidied string  = " + searchString);

        if(!containsIllegalCharacters) {
            if (searchString.contains(".")) {
                // Search word fit if input contains '.'
                Log.d(LOG_TAG, "Searching wordFit...");
                searchWordFit(searchString.toLowerCase());
            } else {
                Log.d(LOG_TAG, "Searching anagram...");
                searchAnagram(searchString.toLowerCase());
            }
        } else {
            showIllegalCharactersToast();
        }
    }

    /**
     * Method to initialise the ASyncTask to solve word-fit.
     * @param input
     */
    private void searchWordFit(String input) {
        // Load Async task to search the word-fit to stop it haning the UI Thread
        GetWordSolverAnswers getWordSolverAnswers = new GetWordSolverAnswers() ;
        getWordSolverAnswers.execute(input);
    }

    /**
     * Method to solve the word-fit.
     * If the first letter is known, only searches words starting with that letter.
     * Otherwise searches the whole dictionary.
     *
     * @param input The unknown word, with '.' in place of unknown letters
     * @return An ArrayList with all the words which fir the input string
     */
    private ArrayList<String> getWordFitAnswers(String input) {
        // Cycle through dictionary to find possible matches
        ArrayList<String> answers = new ArrayList<>() ;
        boolean resultFound = false ;

        Log.d(LOG_TAG, "input = " + input);
        List<String> dictionaryToSearch ; //= new ArrayList<>();
        if (input.charAt(0) == '.') {
            // Search whole dictionary
            Log.d(LOG_TAG, "searching entire dictionary. Will be slow...");
            dictionaryToSearch = dictionary ;
        } else {
            Log.d(LOG_TAG, "searching words beginning with " + input.charAt(0) + " ...");
            String[] dic = dictionaryByLetter.get(getLetterIndex(input.charAt(0)));

            dictionaryToSearch = Arrays.asList(dic);
        }
        for (String word : dictionaryToSearch) {
            if (word != null && word.toLowerCase().matches(input)) {
                Log.d(LOG_TAG, "matched " + word + " to the input: " + input);
                resultFound = true ;
                answers.add(word);
            }
        }

        if (!resultFound) {
            Log.d(LOG_TAG, "No match found for: " +  input);
            return null ;
        }

        return answers ;
    }

    /**
     * Method to solve the anagram.
     *
     * Sorts the letters of the search term into alphabetical order, then checks whether the dictionary
     * HashSet contains a key with the same string. If so, the values are all anagrams of the search string.
     * @param input
     */
    private void searchAnagram(String input) {
        // Order letters, then check if hashset contains a match. If so, print the output options
        // Sort letters
        String inputSorted = sortWord(input);
        // Check if in HashMap
        if (dictionaryHM.containsKey(inputSorted)) {
            Log.d(LOG_TAG, "Match found for: " + inputSorted);
            ArrayList<String> answers = dictionaryHM.get(inputSorted);
            // Add results to resultsLinearLayout
            resultCount = 0 ;   // Set to 0 to allow for correct timing delays to entry animation
            for (String answer : answers) {
                Log.d(LOG_TAG, "An answer for " + input + " is: " + answer);
                addToResults(answer);
            }
        } else {
            Log.d(LOG_TAG, "No match found for: " + inputSorted + ", which originally came from " + input);
            addToResults(getString(R.string.no_match_found), false);
        }

    }

    /**
     * {@code searchDictioary} defaults to true
     *
     * {@see AnagramPageFragment#addToResults(String, boolean)}
     */
    private void addToResults(String result) {
        // Default to search dictionary if result returned
        addToResults(result,true);
    }

    /**
     * Method to add a result to the view displaying all the results.
     * @param result The result to be added to the list of results.
     * @param searchDictionary Whether to search the dictionary when the results card is clicked.
     */
    private void addToResults(String result, boolean searchDictionary) {
        Log.d(LOG_TAG, "Adding results TextView for " + result);
        CardView cardView = new CardView(getActivity());
        TextView tv = new TextView(getActivity());

        if (searchDictionary) {
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v;
                    searchDictionary(tv.getText().toString());
                }
            });
        }

        tv.setText(result);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.dictionary_word));
        tv.setPadding(getResources().getDimensionPixelOffset(R.dimen.home_card_padding), getResources().getDimensionPixelOffset(R.dimen.home_card_padding), getResources().getDimensionPixelOffset(R.dimen.home_card_padding), getResources().getDimensionPixelOffset(R.dimen.home_card_padding));
        cardView.addView(tv);
        cardView.setUseCompatPadding(true);
        TypedValue outValue  = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,outValue,true);
        tv.setBackgroundResource(outValue.resourceId);

        // Prep for Animation
        cardView.setAlpha(0.0f);
        cardView.setTranslationY(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_Y_TRANSLATE);

        resultsLinearLayout.addView(cardView);

        // Animate
        cardView.animate()
                .alpha(1.0f)
                .translationY(0)
                .setStartDelay(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_STAGGER * resultCount)
                .setDuration(DictionaryPageFragment.ENTRY_EXIT_ANIMATION_DURATION)
                .setListener(null);

        // Increment the result count so subsequent cards will be delayed on their entry animation
        resultCount++ ;
    }

    /**
     * Shows a toast explaining that only letters and '.' are legal for searching with.
     */
    private void showIllegalCharactersToast() {
        Toast illegalCharactersToast = Toast.makeText(getActivity(), getString(R.string.illegalCharacterToast), Toast.LENGTH_SHORT);
        illegalCharactersToast.show();
    }

    /**
     * Method to load all words in the dictionary starting with the given letter
     * @param letter The first letter of the words to load to the dictionary
     */
    private void loadLetterToDictionary(String letter) {
        loadLetterToDictionary(getLetterIndex(letter.toCharArray()[0]));
    }

    /**
     * Method to load all words in the dictionary starting with the letter given by the index
     * @param index The index of the letter
     */
    private void loadLetterToDictionary(int index) {
        switch (index) {
            case 0:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsA));
                break ;
            case 1:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsB));
                break ;
            case 2:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsC));
                break ;
            case 3:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsD));
                break ;
            case 4:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsE));
                break ;
            case 5:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsF));
                break ;
            case 6:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsG));
                break ;
            case 7:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsH));
                break ;
            case 8:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsI));
                break ;
            case 9:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsJ));
                break ;
            case 10:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsK));
                break ;
            case 11:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsL));
                break ;
            case 12:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsM));
                break ;
            case 13:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsN));
                break ;
            case 14:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsO));
                break ;
            case 15:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsP));
                break ;
            case 16:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsQ));
                break ;
            case 17:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsR));
                break ;
            case 18:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsS));
                break ;
            case 19:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsT));
                break ;
            case 20:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsU));
                break ;
            case 21:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsV));
                break ;
            case 22:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsW));
                break ;
            case 23:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsX));
                break ;
            case 24:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsY));
                break ;
            case 25:
                dictionaryByLetter.add(getResources().getStringArray(R.array.wordsZ));
                break ;
        }
    }

    /**
     * Wrapper method to load and process the full dictionary. This will be called to prepare the
     * dictionaries for the word-fit and anagram tools unless low-RAM mode is enabled.
     */
    private void loadFullDictionary() {
        Log.d(LOG_TAG, " Low RAM mode NOT enabled, so reading the dictionary...");

        // Loop through all letters and add them to the dictionary
        for (int i = 0 ; i < 26 ; i++) {
            loadLetterToDictionary(i);
        }
        Log.d(LOG_TAG, "DictionaryByLetter Loaded...");

        Log.d(LOG_TAG, "Processing words for anagram purposes...");
        processLoadedDictionaryWords();

        Log.d(LOG_TAG, "Dictionary & HashMap loaded...");
        dictionaryLoaded = true ;
    }

    /**
     * Method to process all Strings in the String arrays in the dictionaryByLetter ArrayList, and
     * to add them to the final dictionary and HashMap.
     *
     * The HashMap contains the word, with its letters sorted alphabetically as the key, ready for
     * use in anagram solving. If the HashMap already has the sorted word as a key, the word is
     * added to the list of words which are anagrams of one-another.
     */
    private void processLoadedDictionaryWords(){
        // Add all the words to the dictionary
        for (int i = 0; i < dictionaryByLetter.size() ; i++) {
            Log.d(LOG_TAG, "i=" + i);
            for (int j = 0; j < dictionaryByLetter.get(i).length; j++) {
                String word = dictionaryByLetter.get(i)[j];
                if (word != null) {
                    dictionary.add(word);
                    // Sort the string and add it to the ordered dictionary
                    String sortedWord = sortWord(word);
                    // Add to the HashMap
                    if (dictionaryHM.containsKey(sortedWord)) {
                        ArrayList<String> possibleWords = dictionaryHM.get(sortedWord);
                        possibleWords.add(word);
                        dictionaryHM.put(sortedWord, possibleWords);
                    } else {
                        ArrayList<String> possibleWords = new ArrayList<>();
                        possibleWords.add(word);
                        dictionaryHM.put(sortedWord, possibleWords);
                    }
                }
            }
        }
    }

    /**
     * Method to make the search button clickable, changing the text and setting the onClickListener.
     */
    private void setSearchButtonClickable() {
        // Allow the search button to be pressed once dictionary is loaded
        Log.d(LOG_TAG,"Search button now clickable");
        searchButton.setClickable(true);
        searchButton.setText(getResources().getString(R.string.search));
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchClicked();
            }
        });
    }

    /**
     * Stop the search button being clicked again once it has been clicked and a word-fit search is on-going.
     */
    private void setSearchButtonNotClickable() {
        // Prevent the search button being pressed while looking up word-solver answers
        Log.d(LOG_TAG,"Search button not clickable");
        searchButton.setClickable(false);
        searchButton.setText(R.string.searching);
        searchButton.setPressed(true);
    }

    /**
     * Method to sort the letters of a word into alphabetical order.
     * @param input The word whose letters are to be sorted
     * @return The sorted letters
     */
    private String sortWord(String input) {
        // Sort the letters into alphabetical order
        char[] wordChars = input.toLowerCase().toCharArray();
        Arrays.sort(wordChars);
        return new String(wordChars);
    }

    /**
     * Simple method to look up the index of a letter in the alphabet (a = 0)
     * @param letter The letter to be looked up
     * @return The index of the letter
     */
    private int getLetterIndex(char letter) {
       String alphabet = "abcdefghijklmnopqrstuvwxyz";
        return alphabet.indexOf(letter);
    }

    /**
     * Method to hide the keyboard
     */
    private void hideKeyboard() {
    // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Method to search the MW dictionary on the DictionaryPageFragment
     * @param searchTerm The word to search
     */
    private void searchDictionary(String searchTerm) {
        // Search dictionary in DictionaryFragment
        Log.d(LOG_TAG,"Try to search for: " + searchTerm);
        mListener.searchDictionary(searchTerm);
    }

    /**
     * Method to make the input box request focus.
     */
    public void inputBoxRequestFocus() {
        inputBox.requestFocus();
        inputBox.selectAll();
    }

    /**
     * @return The EditText instance of the input/search box.
     */
    public EditText getInputBox() {
        return inputBox;
    }


    /**
     * ASyncTask to read the dictionary resource and load it into memory.
     * Sets the search button clickable once complete.
     */
    private class LoadDictionaryTask extends AsyncTask<Void,Void,String> {
        // Load the dictionary in the background to prevent hanging the main thread
        @Override
        protected String doInBackground(Void... params) {
            Log.d(LOG_TAG," Reading the dictionary in background...");

            loadFullDictionary();
            Log.d(LOG_TAG, " Dictionary loaded in background!");

            return null ;
        }

        protected void onPostExecute(String result) {
            setSearchButtonClickable();
        }

    }

    /**
     * Method to run a search for word-fit solutions on a background thread.
     * Displays a 'loading' spinner and stops the search button being clickable before executing,
     * removes the spinner and returns the search button to normal on completion.
     */
    private class GetWordSolverAnswers extends AsyncTask<String,Void,String> {
        /**
         *  Async task to compute the word-solver answers. Allows it to be moved off the main UI Thread.
         *
         * Created by mat on 04/10/15.
         */

        ArrayList<String> answers = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            // Show the progress spinner
            progressSpinnerLinLayout.setVisibility(View.VISIBLE);
            // Stop the search button from being pressed
            setSearchButtonNotClickable();
        }
        @Override
        protected String doInBackground(String... input) {
            Log.d(LOG_TAG,"Getting Word-solver answers...");
            answers = getWordFitAnswers(input[0]);
            Log.d(LOG_TAG,"Done!");
            return null ;
        }

        @Override
        protected void onPostExecute(String result) {
            // Hide the progress spinner
            progressSpinnerLinLayout.setVisibility(View.GONE);
            // Set search clickable again
            setSearchButtonClickable();

            // Add answers to view
            if (answers != null) {
                resultCount = 0 ;
                for (String word : answers) {
                    addToResults(word);
                }
            } else {
                addToResults(getString(R.string.no_match_found), false);
            }
        }
    }

}
