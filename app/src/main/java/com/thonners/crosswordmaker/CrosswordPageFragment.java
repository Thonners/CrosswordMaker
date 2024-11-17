package com.thonners.crosswordmaker;

import android.app.Activity;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrosswordPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrosswordPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrosswordPageFragment extends Fragment implements View.OnClickListener,
        View.OnLongClickListener, Crossword.WordSplitHyphenDeactivatedListener,
        Clue.ClueInteractionListener {

    private static final String ARG_TAB_POSITION = "tabPosition";
    private static final String ARG_STRING_ARRAY = "crosswordStringArray";
    private static final String ARG_STRING_FILENAME = "com.thonners.crosswordmaker" +
            ".CrosswordPageFragment.crosswordFilename";
    private static final String LOG_TAG = "CrosswordPageFragment";

    private final int MAX_COL_COUNT = 10;
    private CrosswordGrid crosswordGrid;
    private HorizontalScrollViewNoFocus horizontalScrollViewNoFocus;
    private ScrollView verticalScrollView;
    private GridLayout acrossCluesChecklist;
    private GridLayout downCluesChecklist;
    private LinearLayout hangmanLayout;
    private FloatingActionButton wordSplitFAB;
    private FloatingActionButton hyphenFAB;

    private Crossword crossword;
    private String[] crosswordStringArray;
    private CrosswordTwo crosswordTwo;
    private String crosswordFilePath;
    private CrosswordCanvas canvasEditor;

    private boolean addHyphenActive = false;
    private boolean addWordSplitActive = false;

    private int tabPosition;

    private OnFragmentInteractionListener mListener;

    public static CrosswordPageFragment newInstance(int position, String crosswordFilePath) {
        CrosswordPageFragment fragment = new CrosswordPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, position);
        args.putString(ARG_STRING_FILENAME, crosswordFilePath);
        fragment.setArguments(args);
        return fragment;
    }

    public static CrosswordPageFragment newInstance(int position, String[] crosswordArray) {
        CrosswordPageFragment fragment = new CrosswordPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, position);
        args.putStringArray(ARG_STRING_ARRAY, crosswordArray);
        fragment.setArguments(args);
        return fragment;
    }

    public CrosswordPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt(ARG_TAB_POSITION);
            //            crosswordStringArray = getArguments().getStringArray(ARG_STRING_ARRAY);

            crosswordFilePath = getArguments().getString(ARG_STRING_FILENAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume called. Initialising save Files");
        //        crossword.initialiseSaveFiles(); // TODO: Uncomment this!
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreateView called...");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crossword_page, container, false);
        // Get instances of the views
        Log.d(LOG_TAG, "Getting the canvas editor view");
        canvasEditor = view.findViewById(R.id.edit_crossword_canvas);
        //        crosswordGrid = view.findViewById(R.id.crossword_grid);
        //        horizontalScrollViewNoFocus = view.findViewById(R.id
        //        .horizontal_scroll_view_crossword);
        //        verticalScrollView = view.findViewById(R.id.vertical_scroll_view_crossword);
        //        acrossCluesChecklist = view.findViewById(R.id.clues_checklist_across_layout);
        //        downCluesChecklist = view.findViewById(R.id.clues_checklist_down_layout);
        wordSplitFAB = view.findViewById(R.id.add_word_split);
        hyphenFAB = view.findViewById(R.id.add_hyphen);
        ////        hangmanLayout = view.findViewById(R.id.hangman_letters_layout);
        //
        //        // Pass scroll view instances to the crosswordGrid
        //        crosswordGrid.setHorizontalScrollView(horizontalScrollViewNoFocus);
        //        crosswordGrid.setVerticalScrollView(verticalScrollView);
        //
        //        createCrossword();

        try {
            crosswordTwo = CrosswordTwo.fromJsonFile(getContext(), crosswordFilePath);
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Error loading the crossword!");
            Log.e(LOG_TAG, ex.getMessage());
            return view;
        }

        getActivity().setTitle(crosswordTwo.getActivityTitle());

        //        // Populate the horizontal clues checklist:
        //        populateCluesChecklists(acrossCluesChecklist, crosswordTwo.getHClues());
        //        // Populate the vertical clues checklist
        //        populateCluesChecklists(downCluesChecklist, crosswordTwo.getVClues());

        // Add click listeners for the hyphen/word split FABs
        view.findViewById(R.id.add_word_split).setOnClickListener(this);
        view.findViewById(R.id.add_hyphen).setOnClickListener(this);
        // Reduce their prominence
        //        resetFABs();

        //        // Add click listener to the cell views, in case we're in add hyphen or add
        //        word split mode...
        //        crossword.setClickListenerForAllCells(this);

        // TODO: Think about managing this better (other function somewhere, deal with
        //  portrait/landscape, etc
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int gridSize = size.x - 80;
        canvasEditor.initialise(gridSize, gridSize, crosswordTwo);
        Log.d(LOG_TAG, "Invalidating canvas");
        canvasEditor.invalidate();
        Log.d(LOG_TAG, "onCreateView returning...");
        return view;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Currently unused interface
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private void createCrossword() {
        // Create the crossword
        crossword = new Crossword(getActivity(), crosswordGrid, crosswordStringArray, false);
        crossword.setWordSplitHyphenListener(this);
        crossword.setClueHangmanListener(this);
    }

    public void zoomCrossword() {
        if (crossword.getIsZoomed()) {
            Log.d(LOG_TAG, "Zoom out FAB pressed");
        } else {
            Log.d(LOG_TAG, "Zoom in FAB pressed");
        }
        crossword.toggleZoom();
    }

    public CrosswordTwo getCrossword() {
        return crosswordTwo;
    }

    public File getCrosswordSaveDir() {
        return crossword.getSaveDir();
    }

    /**
     * Method to create ClueChecklistEntries for the given collection of clues, and add them to the
     * appropriate clues checklist layout (either across or down).
     *
     * @param gridLayout The GridLayout to populate with <ClueChecklistEntryTextView>s
     * @param clues      An ArrayList of clues, for which to populate the GridLayout
     */
    private void populateCluesChecklists(GridLayout gridLayout, ArrayList<Clue> clues) {
        Log.d(LOG_TAG, "Populating the clues checklist...");
        // Get column count, etc.
        int clueCount = clues.size();
        int colMax = Math.max(Math.min(MAX_COL_COUNT, clueCount), 1); // Force the colMax to be
        // between 1 and the MAX_COL_COUNT value
        int rowCount = clueCount / colMax;
        gridLayout.setColumnCount(colMax);
        gridLayout.setRowCount(rowCount + 1);
        // Initialise the counters
        int row = 0, col = 0;
        // Cycle through the horizontal clues
        for (Clue clue : clues) {
            // Check column isn't at colMax
            if (col == colMax) {
                row++;
                col = 0;
            }

            ClueChecklistEntryTextView c = clue.getChecklistEntryTextView(getActivity());
            c.setOnClickListener(this);
            c.setOnLongClickListener(this);

            // Set the layout params
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.rightMargin = 5;
            param.topMargin = 5;
            param.setGravity(Gravity.CENTER);
            // Set the column position, and weight if the framework supports it
            param.columnSpec = GridLayout.spec(col, 1.0f);
            param.rowSpec = GridLayout.spec(row);
            c.setLayoutParams(param);

            // Add to the parent view
            gridLayout.addView(c);

            // Check whether text should have strikethrough or not
            c.getClue().checkClueComplete();

            // Increment column
            col++;
        }
        Log.d(LOG_TAG, "Done.");
    }

    /**
     * Method to create the TextView of the clue number to add to the checklist
     *
     * @param checklistLayout The layout to which the TextView will be added
     * @param clueID          The uniqueID of the clue
     * @param displayNumer    The number to be displayed for the clue
     */
    private void makeClueChecklistEntry(LinearLayout checklistLayout, int clueID,
                                        int displayNumer) {

    }

    /**
     * Method to highlight the clue if a user clicks its {@link ClueChecklistEntryTextView} in the
     * checklist GridLayout
     *
     * @param view The {@link ClueChecklistEntryTextView} that has been clicked.
     */
    @Override
    public void onClick(View view) {
        // Check it's a clueChecklistTV that's been clicked
        if (view instanceof ClueChecklistEntryTextView) {
            Clue clue = ((ClueChecklistEntryTextView) view).getClue();
            clue.highlightClue(clue.getStartCell());
        } else if (view instanceof FloatingActionButton) {
            Toast.makeText(getContext(), R.string.word_split_instructions_1, Toast.LENGTH_LONG).show();
            if (view.equals(wordSplitFAB)) addWordSplit();
            if (view.equals(hyphenFAB)) addHyphen();
        } else if (view instanceof CellView) {
            Log.d(LOG_TAG, "Cell view clicked");
            if (this.addHyphenActive || this.addWordSplitActive) {
                Log.d(LOG_TAG, "Adding something active");
            }
        } else if (view instanceof HorizontalScrollViewNoFocus) {
            Log.d(LOG_TAG, "HorizontalScrollViewNoFocus view clicked");
            if (this.addHyphenActive || this.addWordSplitActive) {
                Log.d(LOG_TAG, "Adding something active");

            }
        }
    }

    /**
     * Method to manage the user long-clicking a clue checklist entry to override the automatic
     * management
     * and uncross/cross it off
     *
     * @param view The {@link ClueChecklistEntryTextView} that has been clicked
     * @return Whether this method has managed/consumed the long click.
     */
    @Override
    public boolean onLongClick(View view) {
        // Check it's a clueChecklistTV that's been clicked
        if (view instanceof ClueChecklistEntryTextView) {
            ((ClueChecklistEntryTextView) view).toggleChecked();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param letters Array of Strings for each letter in the clue
     */
    public void updateHangman(char[] letters) {
        Log.d(LOG_TAG, "(NOT) Updating hangman..." + String.valueOf(letters));
        //        this.hangmanLayout.removeAllViews();
        //        if (letters.length == 0) {
        //            Log.d(LOG_TAG, "letters.length = 0");
        //            return;
        //        }
        //        for (int i = 0; i < letters.length; i++) {
        //            ManualAnagramKnownLetterCardView tv = new ManualAnagramKnownLetterCardView
        //            (getContext());
        //            tv.setLetter(letters[i] + "");
        //            this.hangmanLayout.addView(tv);
        //        }
    }

    private void addWordSplit() {
        Log.d(LOG_TAG, "Add word split clicked");
        if (crossword.isAddWordSplitActive()) {
            // Then deactivate it
            crossword.setAddWordSplitActive(false);
            setFABNormal(wordSplitFAB);
            setFABNormal(hyphenFAB);
        } else {
            setFABActive(wordSplitFAB);
            setFABDeactive(hyphenFAB);
            crossword.setAddWordSplitActive(true);
        }
    }

    private void addHyphen() {
        Log.d(LOG_TAG, "Add hyphen clicked");
        if (crossword.isAddHyphenActive()) {
            // Then deactivate it
            crossword.setAddHyphenActive(false);
            setFABNormal(wordSplitFAB);
            setFABNormal(hyphenFAB);
        } else {
            setFABActive(hyphenFAB);
            setFABDeactive(wordSplitFAB);
            crossword.setAddHyphenActive(true);
        }
    }

    private void setFABActive(FloatingActionButton fabView) {
        fabView.animate().scaleX((float) 1.3).scaleY((float) 1.3).alpha(1).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator());

    }

    private void setFABDeactive(FloatingActionButton fabView) {
        fabView.animate().scaleX((float) 0.5).scaleY((float) 0.5).alpha((float) 0.25).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator());

    }

    private void setFABNormal(FloatingActionButton fabView) {
        fabView.animate().scaleX((float) 1.0).scaleY((float) 1.0).alpha((float) 0.5).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator());

    }

    @Override
    public void wordSplitHyphenDeactivated() {
        resetFABs();
    }

    private void resetFABs() {
        setFABNormal(hyphenFAB);
        setFABNormal(wordSplitFAB);
    }
}
