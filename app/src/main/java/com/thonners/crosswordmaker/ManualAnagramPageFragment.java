package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.Space;
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
    private RelativeLayout fragmentParentLayout ;
    private RelativeLayout outputParentLayout ;
    private LinearLayout knownLettersLayout ;
    private int knownLetterCount = 0 ;

    private ManualAnagramKnownLetterCardView activeKnownLetterCard = null ;
    private ManualAnagramTextView activeLetterTV = null ;

    /**
     * Required empty constructor
     */
    public ManualAnagramPageFragment() {
    }


    /**
     * Standard Method. Over-ridden just because.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    /**
     * Standard Method. Used to get the instances of the various views, buttons, etc.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
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

        fragmentParentLayout = (RelativeLayout) view.findViewById(R.id.fragment_parent_layout);

        return view ;
    }

    private void showInstructionsSnackbar() {
        // Show a snackbar about the instructions
        Snackbar snackbar = Snackbar.make(fragmentParentLayout,getResources().getString(R.string.tutorial_snackbar_message),Snackbar.LENGTH_LONG) ;
        snackbar.setAction(getResources().getText(R.string.show), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInstructions() ;
            }
        }) ;
        snackbar.show();
    }
    /**
     * Method to be called once the shuffle button is clicked. Restructuring required.
     */
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

    /**
     * Method to shuffle the order of the letters and display them in the main results view.
     *
     * Hides the keyboard, clears the results so it's ready for the newly created letters.
     */
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
            letterTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    letterTVClicked((ManualAnagramTextView) view);
                }
            });
        }
    }

    /**
     * Method to randomly shuffle the character array.
     * @param array The array to be shuffled.
     */
    private void shuffleArray(char[] array) {
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

    /**
     * Clears the letters from the results view.
     */
    private void clearDisplayedLetters() {
        // Clear the results view
        if (outputParentLayout.getChildCount() > 0) {
            outputParentLayout.removeAllViews();
        }
    }

    /**
     * Method to hide the keyboard.
     */
    private void hideKeyboard() {
    // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Method to request the focus for the input text box. This is called by the main activity to
     * select the text that was in the textview from before, to allow typing to replace the old text.
     */
    public void inputBoxRequestFocus() {
        inputBox.requestFocus();
        inputBox.selectAll();
    }

    /**
     * Returns the instance of the EditText inputBox
     * @return the EditText instance of the search box.
     */
    public EditText getInputBox() {
        return inputBox;
    }

    /**
     * Method to return the ID of the anchor view - a blank space which will position itself in the
     * centre of the main results view.
     * @return The int ID of the anchor view
     */
    private int getAnchorID() {
        Space anchor = new Space(getActivity()) ;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        outputParentLayout.addView(anchor,params);
        anchor.setId(100);
        return anchor.getId() ;
    }

    /**
     * Method to create the blank KnownLetterCardViews at the bottom of the view.
     *
     * Adds onClickListeners to the views to let the user click them, then set the known letter.
     * @param letterCount The number of letters in the anagram.
     */
    private void populateKnownLettersLayout(int letterCount) {
        Log.d(LOG_TAG,"Adding known letter empty cards. Lettercount = " + letterCount);
        // Clear known letters from old instances
        knownLettersLayout.removeAllViews();
        // Create an array in which to hold the Cards
        ManualAnagramKnownLetterCardView[] knownLetterCards = new ManualAnagramKnownLetterCardView[letterCount] ;
        // Cycle through the number of letters and create a card for each one. Add this to the layout, giving it an index appropriately.
        for (int i = 0 ; i < letterCount ; i++) {
            Log.d(LOG_TAG,"1. i = " + i);
            knownLetterCards[i] = new ManualAnagramKnownLetterCardView(getActivity(), i) ;
            knownLetterCards[i].initialise(getActivity());
            Log.d(LOG_TAG,"2. i = " + i);
            knownLetterCards[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    knownLetterCardClicked((ManualAnagramKnownLetterCardView) view) ;
                }
            });
            knownLetterCards[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    knownLetterCardLongClicked((ManualAnagramKnownLetterCardView) view) ;
                    return true ;
                }
            });

            knownLettersLayout.addView(knownLetterCards[i],i);

        }
    }

    /**
     * Method to make a note of which ManualAnagramKnownLetterCardView has been touched, so it can be
     * set to the correct letter when selected by the user.
     * @param newKnownLetterCard The ManualAnagramKnownLetterCardView touched by the user.
     */
    private void knownLetterCardClicked(ManualAnagramKnownLetterCardView newKnownLetterCard) {
        Log.d(LOG_TAG,"ActiveKnownLetterCard clicked") ;
        if (activeKnownLetterCard != null && activeKnownLetterCard.getIndex() == newKnownLetterCard.getIndex()) {
            // If the card clicked is already high-lighted, clear the highlight
            clearActiveKnownLetterCard();
        } else {
            // Check that the card is empty. If not, prompt user to clear the card before re-filling it
            if (newKnownLetterCard.isEmpty()) {
                // Clear any previously set card before setting this one, to avoid two cards looking active to the user
                clearActiveKnownLetterCard();
                activeKnownLetterCard = newKnownLetterCard;
                activeKnownLetterCard.setIsActive();
            } else {
                // Not sure what to do, if anything, if user clicks a card that's already filled.
            }
        }
    }

    /**
     * Method to handle a user long-clicking a known letter card.
     *
     * If the card is empty, offer option to add hyphen/slash to the right - for compound words.
     * If the card already has a letter, clear it and reset the view to its original state.
     * @param knownLetterCardView The knownLetterCard that has been long clicked.
     */
    private void knownLetterCardLongClicked(ManualAnagramKnownLetterCardView knownLetterCardView) {
        // Clear any active card to avoid confusion
        clearActiveKnownLetterCard();
        // Handle the long-click:
        if (knownLetterCardView.isEmpty()) {
            // Show snackbar / popup to add divider to right of knownLetterCardView
        } else {
            knownLetterCardView.clearLetter();
        }
    }
    /**
     * Clears the active known letter card, so that touching a results letter will not result in any action.
     */
    private void clearActiveKnownLetterCard() {
        // Clear any set features, such as card elevation from a previously active card
        if (activeKnownLetterCard != null) {
            activeKnownLetterCard.setIsActive(false);
        }
        // Set the active card to null
        activeKnownLetterCard = null ;
    }

    /**
     * Method to set the letter of a known letter card manualAnagramTextView.
     * @param manualAnagramTextView The letter's TextView in the main results layout.
     */
    private void letterTVClicked(ManualAnagramTextView manualAnagramTextView) {
        // Check whether a knownLetterCard is active
        if (activeKnownLetterCard != null) {
            // Check that this letter isn't already assigned to another known letter card
            if (!manualAnagramTextView.isKnown()) {
                // If so, set it to the touched letter. This will also set the textView to known, i.e. greyed out.
                activeKnownLetterCard.setLetter(manualAnagramTextView);
                //activeKnownLetterCard.setLetter(letter);
                // Clear the active card / letter TV so as not to contaminate future touches
                clearActiveKnownLetterCard();
                clearActiveLetterTV();
            }
        } else {
            // Set or clear the activeLetterTV depending on whether it's already set to the same letter
            if (activeLetterTV != null && activeLetterTV.getLetterNo() == manualAnagramTextView.getLetterNo()) {
                clearActiveLetterTV();
            } else {
                // Clear any previously active TVs
                clearActiveLetterTV();
                // Set the active known letter, so the user can touch a card
                activeLetterTV = manualAnagramTextView;
                activeLetterTV.setIsActive() ;
            }
        }

    }

    private void clearActiveLetterTV() {
        // Clear any highlighting if required
        if (activeLetterTV != null) {
            activeLetterTV.setTypeface(null, Typeface.NORMAL);
        }
        // Clear the active TV
        activeLetterTV = null ;
    }

    private void showInstructions() {
        Log.d(LOG_TAG, "Would show manual anagram instructions now") ;
    }
}
