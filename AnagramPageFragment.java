package com.thonners.crosswordmaker;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class AnagramPageFragment extends Fragment {

    private static final String LOG_TAG = "AnagramPageFragment" ;

    private int hashMapSize = 315000 ;   // Size for the hashMap to allow loadFactor < 0.75 whilst fiting all the words in the dictionary
    private HashMap<String, ArrayList<String>> dictionaryHM = new HashMap<String, ArrayList<String>>(hashMapSize);    // Hashmap to store the options in

    private ArrayList<String> dictionary = new ArrayList<String>() ;
    private ArrayList<String[]> dictionaryByLetter= new ArrayList<String[]>() ;
    private ArrayList<String> sortedDictionary = new ArrayList<String>() ;


    private boolean buttonIsClear  = false ;    // Variable to store whether the button next to the search bar should be 'search' or 'clear'

    private OnFragmentInteractionListener mListener;

    Button searchButton ;
    EditText inputBox ;
    LinearLayout resultsLinearLayout ;

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
        loadDictionary();
        long stopTime = System.currentTimeMillis();
        Log.d(LOG_TAG,"Dictionary loaded after " + (stopTime - startTime) + " millis");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_anagram, container, false);
        searchButton = (Button) view.findViewById(R.id.anagram_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchClicked();
            }
        });
        inputBox = (EditText) view.findViewById(R.id.anagram_search_input);
        resultsLinearLayout = (LinearLayout) view.findViewById(R.id.anagram_results_layout);
        return view ;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void searchClicked() {
        // Do something
            Log.d(LOG_TAG, "Search button clicked. ");
        toggleSearchButton();
    }

    private void toggleSearchButton() {
        //Toggle the function of the search button
        if (buttonIsClear) {
            // If button was 'clear' when clicked, clear search/view, and return text to 'search'
            Log.d(LOG_TAG, "Clearing results and search. ");
            clearSearchAndResults();
            searchButton.setText(getString(R.string.search));
        } else {
            // If button was 'search' when clicked, do search, and change button to 'clear'.
            Log.d(LOG_TAG, "Searching... ");
            search();
            searchButton.setText(getString(R.string.clear));
        }
        buttonIsClear = ! buttonIsClear ;
    }

    private void clearSearchAndResults() {
        // If clear clicked, clear the text from the search, and clear results from the view
        // Clear text from input box
        inputBox.setText("");
        // Clear the results view
        if (resultsLinearLayout.getChildCount() > 0 ) {
            resultsLinearLayout.removeAllViews();
        }

    }

    private void search() {
        // Search for either anagrams or word fits depending on whether the input text contains a '.'

        Log.d(LOG_TAG, "Checking input... ");
        // First check input and ignore any non letters or '.'s
        String searchStringOriginal = inputBox.getText().toString();
        String searchString = "";
        boolean containsIllegalCharachters = false ;
        int j=0;
        for (int i = 0 ; i < searchStringOriginal.length() ; i++ ) {
            if (Character.isLetter(searchStringOriginal.toLowerCase().charAt(i)) || searchStringOriginal.substring(i,i+1).matches(".")) {
                Log.d(LOG_TAG, "Adding '" + searchStringOriginal.substring(i,i+1) + "' to search string (currently = " + searchString + " as this is a valid character for input.");
                // Acceptable character, so add to array
                searchString = searchString + searchStringOriginal.substring(i,i+1);
            } else {
                Log.d(LOG_TAG, "Illegal character found in input: " + searchStringOriginal.substring(i,i+1));
                containsIllegalCharachters = true ; // Doesn't matter if this is overwritten multiple times
            }
        }


        Log.d(LOG_TAG, "Original input string: " + searchStringOriginal + " || Tidied string  = " + searchString);

        if(!containsIllegalCharachters) {
            if (searchString.contains(".")) {
                // Search word fit if input contains '.'
                Log.d(LOG_TAG, "Searching wordFit...");
                searchWordFit(searchString);
            } else {
                Log.d(LOG_TAG, "Searching anagram...");
                searchAnagram(searchString);
            }
        } else {
            showIllegalCharactersToast();
        }
    }

    private void searchWordFit(String input) {
        // Cycle through dictionary to find it
        showSearchingToast();
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
            addToResults(getString(R.string.no_match_found));
        }

    }

    private void addToResults(String result) {
        Log.d(LOG_TAG, "Adding results TextView for " + result);
        TextView tv = new TextView(getActivity());
        tv.setText(result);
        resultsLinearLayout.addView(tv);
    }

    private void showSearchingToast() {
        Toast searchingToast = Toast.makeText(getActivity(),getString(R.string.searching),Toast.LENGTH_LONG);
        searchingToast.show();
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
        // Add all thr words to the dictionary
        for (int i = 0 ; i < 26 ; i++) {
        Log.d(LOG_TAG,"i=" + i);
            for (int j =0 ; j < dictionaryByLetter.get(i).length ; j ++) {
                String word = dictionaryByLetter.get(i)[j];
                if (word != null) {
                    dictionary.add(word);
                    // Sort the string and add it to the ordered dictionary
          /*          char[] wordChars = word.toLowerCase().toCharArray();
                    Arrays.sort(wordChars);
                    String sortedWord = new String(wordChars);
                    sortedDictionary.add(sortedWord);
*/

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

        Log.d(LOG_TAG,"Dictionary & HashMap loaded...");
    }

    private String sortWord(String input) {
        // Sort the letters into alphabetical order
        char[] wordChars = input.toLowerCase().toCharArray();
        Arrays.sort(wordChars);
        return new String(wordChars);
    }

}
