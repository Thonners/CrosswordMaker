package com.thonners.crosswordmaker;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class AnagramPageFragment extends Fragment {

    private static final String LOG_TAG = "AnagramPageFragment" ;

    private int hashMapSize = 315000 ;   // Size for the hashMap to allow loadFactor < 0.75 whilst fitting all the words in the dictionary
    private HashMap<String, ArrayList<String>> dictionaryHM = new HashMap<String, ArrayList<String>>(hashMapSize);    // Hashmap to store the options in

    private ArrayList<String> dictionary = new ArrayList<String>() ;
    private ArrayList<String[]> dictionaryByLetter= new ArrayList<String[]>() ;
    private ArrayList<ArrayList<String>> dictionaryByLength = new ArrayList<ArrayList<String>>() ;

    private boolean dictionaryLoaded = false ;
    private boolean buttonIsClear  = false ;    // Variable to store whether the button next to the search bar should be 'search' or 'clear'

    private OnAnagramFragmentListener mListener;

    Button searchButton ;
    EditText inputBox ;
    LinearLayout resultsLinearLayout ;
    LinearLayout progressSpinnerLinLayout ;

    public AnagramPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        // Log how long it takes to load the dictionary
        long startTime = System.currentTimeMillis() ;
        Log.d(LOG_TAG,"AnagramPageFragment onCreate called @ millis: " + startTime);

        LoadDictionaryTask loadDictionaryTask = new LoadDictionaryTask();
        loadDictionaryTask.execute();

        long stopTime = System.currentTimeMillis();
        Log.d(LOG_TAG,"Dictionary loaded after " + (stopTime - startTime) + " millis");
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
        public void searchDictionary(String searchTerm);
    }

    private void searchClicked() {
        // Clear view ready for results, then search
        Log.d(LOG_TAG, "Search button clicked. ");
        hideKeyboard();
        clearResults();
        search();
    }

    private void clearResults() {
        // Clear the results view
        if (resultsLinearLayout.getChildCount() > 0 ) {
            resultsLinearLayout.removeAllViews();
        }

    }

    private void search() {
        // Search for either anagrams or word fits depending on whether the input text contains a '.'

        Log.d(LOG_TAG, "Checking input... ");
        // First check input and ignore any non letters or '.'s
        String searchStringOriginal = inputBox.getText().toString().replaceAll(" ",""); // Remove all spaces
        String searchString = "";
        boolean containsIllegalCharacters = false ;

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

    private void searchWordFit(String input) {
        // Load Async task to search the word-fit to stop it haning the UI Thread
        GetWordSolverAnswers getWordSolverAnswers = new GetWordSolverAnswers() ;
        getWordSolverAnswers.execute(input);
    }

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

    private void searchAnagram(String input) {
        // Order letters, then check if hashset contains a match. If so, print the output options
        // Sort letters
        String inputSorted = sortWord(input);
        // Check if in HashMap
        if (dictionaryHM.containsKey(inputSorted)) {
            Log.d(LOG_TAG, "Match found for: " + inputSorted);
            ArrayList<String> answers = dictionaryHM.get(inputSorted);
            // Add results to resultsLinearLayout
            for (String answer : answers) {
                Log.d(LOG_TAG, "An answer for " + input + " is: " + answer);
                addToResults(answer);
            }
        } else {
            Log.d(LOG_TAG, "No match found for: " + inputSorted + ", which originally came from " + input);
            addToResults(getString(R.string.no_match_found), false);
        }

    }

    private void addToResults(String result) {
        // Default to search dictionary if result returned
        addToResults(result,true);
    }
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

        resultsLinearLayout.addView(cardView);
    }

    private void showIllegalCharactersToast() {
        Toast illegalCharactersToast = Toast.makeText(getActivity(), getString(R.string.illegalCharacterToast), Toast.LENGTH_SHORT);
        illegalCharactersToast.show();
    }

    private void loadDictionary() {
        // Load the dictionaries in from the resouces

        Log.d(LOG_TAG," Reading the dictionary...");

        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsA));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsB));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsC));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsD));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsE));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsF));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsG));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsH));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsI));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsJ));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsK));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsL));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsM));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsN));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsO));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsP));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsQ));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsR));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsS));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsT));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsU));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsV));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsW));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsX));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsY));
        dictionaryByLetter.add(getResources().getStringArray(R.array.wordsZ));
        Log.d(LOG_TAG,"DictionaryByLetter Loaded...");
        // Add all the words to the dictionary
        for (int i = 0 ; i < 26 ; i++) {
        Log.d(LOG_TAG,"i=" + i);
            for (int j =0 ; j < dictionaryByLetter.get(i).length ; j ++) {
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
                        ArrayList<String> possibleWords = new ArrayList<String>();
                        possibleWords.add(word);
                        dictionaryHM.put(sortedWord, possibleWords);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "Dictionary & HashMap loaded...");
    }
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
    private void setSearchButtonNotClickable() {
        // Prevent the search button being pressed while looking up word-solver answers
        Log.d(LOG_TAG,"Search button not clickable");
        searchButton.setClickable(false);
        searchButton.setText(R.string.searching);
        searchButton.setPressed(true);
    }
    private String sortWord(String input) {
        // Sort the letters into alphabetical order
        char[] wordChars = input.toLowerCase().toCharArray();
        Arrays.sort(wordChars);
        return new String(wordChars);
    }

    private int getLetterIndex(char letter) {
       String alphabet = "abcdefghijklmnopqrstuvwxyz";
        return alphabet.indexOf(letter);
    }

    private void hideKeyboard() {
    // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void searchDictionary(String searchTerm) {
        // Search dictionary in DictionaryFragment
        Log.d(LOG_TAG,"Try to search for: " + searchTerm);
        mListener.searchDictionary(searchTerm);
    }

    public void inputBoxRequestFocus() {
        inputBox.requestFocus();
        inputBox.selectAll();
    }
    public EditText getInputBox() {
        return inputBox;
    }


    private class LoadDictionaryTask extends AsyncTask<Void,Void,String> {
        // Load the dictionary in the background to prevent hanging the main thread
        @Override
        protected String doInBackground(Void... params) {
            Log.d(LOG_TAG," Reading the dictionary in background...");

            loadDictionary();
            Log.d(LOG_TAG, " Dictionary loaded in background!");

            return null ;
        }

        protected void onPostExecute(String result) {
            setSearchButtonClickable();
        }

    }

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
                for (String word : answers) {
                    addToResults(word);
                }
            } else {
                addToResults(getString(R.string.no_match_found), false);
            }
        }
    }

}