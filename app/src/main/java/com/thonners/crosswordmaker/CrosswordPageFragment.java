package com.thonners.crosswordmaker;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.util.Log;
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
public class CrosswordPageFragment extends Fragment implements  View.OnLongClickListener{

    private static final String ARG_TAB_POSITION = "tabPosition" ;
    private static final String ARG_STRING_ARRAY = "crosswordStringArray" ;
    private static final String LOG_TAG = "CrosswordPageFragment";

    private CrosswordGrid crosswordGrid;
    private HorizontalScrollViewNoFocus horizontalScrollViewNoFocus ;
    private ScrollView verticalScrollView ;
    private LinearLayout acrossCluesChecklist, downCluesChecklist ;

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
        acrossCluesChecklist = (LinearLayout) view.findViewById(R.id.clues_checklist_across_layout);
        downCluesChecklist = (LinearLayout) view.findViewById(R.id.clues_checklist_down_layout);

        // Pass scroll view instances to the crosswordGrid
        crosswordGrid.setHorizontalScrollView(horizontalScrollViewNoFocus);
        crosswordGrid.setVerticalScrollView(verticalScrollView);

        createCrossword();

        getActivity().setTitle(crossword.getActivityTitle());

        populateCluesChecklists(acrossCluesChecklist, downCluesChecklist);

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
        crossword = new Crossword(getActivity().getApplicationContext(), crosswordGrid,crosswordStringArray,false);
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
     * Method to generate textviews and place them inside the appropriate clues checklist layout.
     * @param acrossLayout The linear layout to contain the checklist views for across clues
     * @param downLayout The linear layout to contain thd checklist views for the down clues
     */
    private void populateCluesChecklists(LinearLayout acrossLayout, LinearLayout downLayout) {
        Log.d(LOG_TAG,"Populating the clues checklist...");
        // Cycle through the horizontal clues
        for (Clue clue : crossword.getHClues()) {
            ClueChecklistEntryTextView c = clue.getChecklistEntryTextView(getActivity()) ;
            c.setOnLongClickListener(this);
            acrossLayout.addView(c) ;
        }
        // Cycle through the down clues
        for (Clue clue : crossword.getVClues()) {
            ClueChecklistEntryTextView c = clue.getChecklistEntryTextView(getActivity()) ;
            c.setOnLongClickListener(this);
            downLayout.addView(c) ;
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
