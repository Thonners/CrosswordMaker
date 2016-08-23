package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.Space;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Page Fragment for manual anagram solving.
 *
 * After the user enters the letters they wish to anagram, shuffles the letters and displays them in
 * a circle
 *
 * @author M Thomas
 * @since 19/08/16
 */
public class ManualAnagramPageFragment extends Fragment {

    private static final String LOG_TAG = "ManualAnagram" ;
    private OnManualAnagramFragmentListener mListener;

    private Button shuffleButton ;
    private FloatingActionButton shuffleFAB ;
    private EditText inputBox ;
    private RelativeLayout outputParentLayout ;
    private LinearLayout knownLettersLayout ;
    private int knownLetterCount = 0 ;

    /**
     * Required empty constructor
     */
    public ManualAnagramPageFragment() {
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
        View view = inflater.inflate(R.layout.fragment_manual_anagram, container, false);
        shuffleButton = (Button) view.findViewById(R.id.manual_anagram_search);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleClicked();
            }
        });
        inputBox = (EditText) view.findViewById(R.id.manual_anagram_search_input);
        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.d(LOG_TAG, "'Shuffle' clicked.");
                    shuffleClicked();
                    return true;
                }
                return false;
            }
        });
        outputParentLayout = (RelativeLayout) view.findViewById(R.id.manual_anagram_results);
        shuffleFAB = (FloatingActionButton) view.findViewById(R.id.manual_anagram_fab) ;
        shuffleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG,"Shuffle FAB clicked.");
                shuffleClicked();
            }
        });
        knownLettersLayout = (LinearLayout) view.findViewById(R.id.manual_anagram_known_letters_layout) ;
        return view ;
    }

    private void shuffleClicked() {
        // TODO: Restructure:
        // If new letters, get the letters
        // Create the views of letters in order
        // Reshuffle subfunction:
            // Shuffle order of views
            // Populate the views
        populateKnownLettersLayout(inputBox.getText().toString().toUpperCase().replaceAll("\\s", "").length());
        shuffleLetters();
    }

    private void shuffleLetters() {
        // Make sure the keyboard is hidden
        hideKeyboard();
        // Clear the view
        clearDisplayedLetters();

        // Get the text (force it to upper case for when it gets displayed, and remove all spaces)
        String input = inputBox.getText().toString().toUpperCase().replaceAll("\\s", "") ;
        int lettersLength = input.length() ;
        Log.d(LOG_TAG,"Letters to be shuffled: " + input + ", there are " + lettersLength + " letters.");

        // Split string into letters
        char[] letters = input.toCharArray() ;
        shuffleArray(letters);
        // Shuffle
        Log.d(LOG_TAG,"Shuffled letters: " + new String(letters));

        // Create the text views & add to the main view
        // Initialise the first letter - this will be the anchor
        int anchorID = getAnchorID() ;
        for (int i = 0 ; i < lettersLength ; i++) {
            ManualAnagramTextView letterTV = new ManualAnagramTextView(getActivity(), letters[i], i, lettersLength, outputParentLayout, anchorID) ;
        }
    }

    private void shuffleArray(char[] array)
    {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }
    public interface OnManualAnagramFragmentListener {
    }

    private void clearDisplayedLetters() {
        // Clear the results view
        if (outputParentLayout.getChildCount() > 0) {
            outputParentLayout.removeAllViews();
        }
    }
    private void hideKeyboard() {
    // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void inputBoxRequestFocus() {
        inputBox.requestFocus();
        inputBox.selectAll();
    }
    public EditText getInputBox() {
        return inputBox;
    }

    private int getAnchorID() {
        Space anchor = new Space(getActivity()) ;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        outputParentLayout.addView(anchor,params);
        anchor.setId(100);
        return anchor.getId() ;
    }

    private void populateKnownLettersLayout(int letterCount) {
        // Clear known letters from old instances
        knownLettersLayout.removeAllViews();
        // Create an array in which to hold the Cards
        CardView[] knownLetterCards = new CardView[letterCount] ;
        // Cycle through the number of letters and create a card for each one. Add this to the layout, giving it an index appropriately.
        for (int i = 0 ; i < letterCount ; i++) {
            knownLetterCards[i] = (CardView) LayoutInflater.from(getActivity()).inflate(R.layout.manual_anagram_known_letters_card_view, null) ;
            knownLettersLayout.addView(knownLetterCards[i],i);
        }
    }
}
