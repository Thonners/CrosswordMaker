package com.thonners.crosswordmaker;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrosswordPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrosswordPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrosswordPageFragment extends Fragment implements  View.OnClickListener, View.OnLongClickListener{

    private static final String ARG_TAB_POSITION = "tabPosition" ;
    private static final String ARG_STRING_ARRAY = "crosswordStringArray" ;
    private static final String LOG_TAG = "CrosswordPageFragment";

    private final int MAX_COL_COUNT = 10 ;

    private CrosswordGrid crosswordGrid;
    private HorizontalScrollViewNoFocus horizontalScrollViewNoFocus ;
    private ScrollView verticalScrollView ;
    private GridLayout acrossCluesChecklist ;
    private GridLayout downCluesChecklist ;

    private Crossword crossword ;
    private String[] crosswordStringArray;

    private int tabPosition ;

    private OnFragmentInteractionListener mListener;

    public static CrosswordPageFragment newInstance(int position, String[] crosswordArray) {
        CrosswordPageFragment fragment = new CrosswordPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, position);
        args.putStringArray(ARG_STRING_ARRAY, crosswordArray);
        fragment.setArguments(args);
        return fragment ;
    }
    public CrosswordPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt(ARG_TAB_POSITION);
            crosswordStringArray = getArguments().getStringArray(ARG_STRING_ARRAY);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume called. Initialising save Files");
        crossword.initialiseSaveFiles();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crossword_page, container, false);
        // Get instances of the views
        crosswordGrid = (CrosswordGrid) view.findViewById(R.id.crossword_grid);
        horizontalScrollViewNoFocus = (HorizontalScrollViewNoFocus) view.findViewById(R.id.horizontal_scroll_view_crossword);
        verticalScrollView = (ScrollView) view.findViewById(R.id.vertical_scroll_view_crossword);
        acrossCluesChecklist = (GridLayout) view.findViewById(R.id.clues_checklist_across_layout);
        downCluesChecklist = (GridLayout) view.findViewById(R.id.clues_checklist_down_layout);

        // Pass scroll view instances to the crosswordGrid
        crosswordGrid.setHorizontalScrollView(horizontalScrollViewNoFocus);
        crosswordGrid.setVerticalScrollView(verticalScrollView);

        createCrossword();

        getActivity().setTitle(crossword.getActivityTitle());

        // Populate the horizontal clues checklist:
        populateCluesChecklists(acrossCluesChecklist, crossword.getHClues());
        // Populate the vertical clues checklist
        populateCluesChecklists(downCluesChecklist, crossword.getVClues());

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

    /**
     * Currently unused interface
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private void createCrossword() {
        // Create the crossword
        crossword = new Crossword(getActivity(), crosswordGrid,crosswordStringArray,false);
    }

    public void zoomCrossword(){
        if(crossword.getIsZoomed()){
            Log.d(LOG_TAG, "Zoom out FAB pressed");
        } else {
            Log.d(LOG_TAG, "Zoom in FAB pressed");
        }
        crossword.toggleZoom();
    }

    public Crossword getCrossword() {
        return crossword;
    }
    public File getCrosswordSaveDir() {
        return crossword.getSaveDir() ;
    }

    /**
     * Method to create ClueChecklistEntries for the given collection of clues, and add them to the
     * appropriate clues checklist layout (either across or down).
     * @param gridLayout The GridLayout to populate with <ClueChecklistEntryTextView>s
     * @param clues An ArrayList of clues, for which to populate the GridLayout
     */
    private void populateCluesChecklists(GridLayout gridLayout, ArrayList<Clue> clues) {
        Log.d(LOG_TAG,"Populating the clues checklist...");
        // Get column count, etc.
        int clueCount = clues.size() ;
        int colMax = Math.max(Math.min(MAX_COL_COUNT, clueCount),1) ; // Force the colMax to be between 1 and the MAX_COL_COUNT value
        int rowCount = clueCount / colMax ;
        gridLayout.setColumnCount(colMax);
        gridLayout.setRowCount(rowCount + 1);
        // Initialise the counters
        int row = 0, col = 0 ;
        // Cycle through the horizontal clues
        for (Clue clue : clues) {
            // Check column isn't at colMax
            if (col == colMax) {
                row++;
                col = 0 ;
            }

            ClueChecklistEntryTextView c = clue.getChecklistEntryTextView(getActivity()) ;
            c.setOnClickListener(this);
            c.setOnLongClickListener(this);

            // Set the layout params
            GridLayout.LayoutParams param =new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.rightMargin = 5;
            param.topMargin = 5;
            param.setGravity(Gravity.CENTER);
            // Set the column position, and weight if the framework supports it
            if (Build.VERSION.SDK_INT >= 21) {
                param.columnSpec = GridLayout.spec(col, 1.0f);
            } else {
                param.columnSpec = GridLayout.spec(col);
            }
            param.rowSpec = GridLayout.spec(row);
            c.setLayoutParams(param);

            // Add to the parent view
            gridLayout.addView(c) ;

            // Check whether text should have strikethrough or not
            c.getClue().checkClueComplete();

            // Increment column
            col++ ;
        }
        Log.d(LOG_TAG,"Done.");
    }

    /**
     * Method to create the TextView of the clue number to add to the checklist
     * @param checklistLayout   The layout to which the TextView will be added
     * @param clueID    The uniqueID of the clue
     * @param displayNumer  The number to be displayed for the clue
     */
    private void makeClueChecklistEntry(LinearLayout checklistLayout, int clueID, int displayNumer) {

    }

    /**
     * Method to highlight the clue if a user clicks its {@link ClueChecklistEntryTextView} in the
     * checklist GridLayout
     * @param view The {@link ClueChecklistEntryTextView} that has been clicked.
     */
    @Override
    public void onClick(View view) {
        // Check it's a clueChecklistTV that's been clicked
        if( view instanceof ClueChecklistEntryTextView) {
            Clue clue = ((ClueChecklistEntryTextView) view).getClue();
            clue.highlightClue(clue.getStartCell());
        }
    }
    /**
     * Method to manage the user long-clicking a clue checklist entry to override the automatic management
     * and uncross/cross it off
     * @param view The {@link ClueChecklistEntryTextView} that has been clicked
     * @return Whether this method has managed/consumed the long click.
     */
    @Override
    public boolean onLongClick(View view) {
        // Check it's a clueChecklistTV that's been clicked
        if( view instanceof ClueChecklistEntryTextView) {
            ((ClueChecklistEntryTextView) view).toggleChecked();
            return true ;
        } else {
            return false ;
        }
    }
}
