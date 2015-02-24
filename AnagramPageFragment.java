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
import android.widget.TextView;

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
        buttonIsClear = ! buttonIsClear ;
        if (buttonIsClear) {
            searchButton.setText(getString(R.string.clear));
        } else {
            searchButton.setText(getString(R.string.search));
        }
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
                    char[] wordChars = word.toLowerCase().toCharArray();
                    Arrays.sort(wordChars);
                    String sortedWord = new String(wordChars);
                    sortedDictionary.add(sortedWord);


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
}
