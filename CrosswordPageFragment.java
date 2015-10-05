package com.thonners.crosswordmaker;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrosswordPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrosswordPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrosswordPageFragment extends Fragment {

    private static final String ARG_TAB_POSITION = "tabPosition" ;
    private static final String ARG_STRING_ARRAY = "crosswordStringArray" ;
    private static final String LOG_TAG = "CrosswordPageFragment";

    private GridLayout crosswordGrid ;
    Crossword crossword ;
    String[] crosswordStringArray;

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
        crosswordGrid = (GridLayout) view.findViewById(R.id.crossword_grid);

        createCrossword();

        getActivity().setTitle(crossword.getActivityTitle());

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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public GridLayout getCrosswordGrid() {
        return crosswordGrid;
    }

    private void createCrossword() {
        // Create the crossword
        crossword = new Crossword(getActivity().getApplicationContext(), crosswordGrid,crosswordStringArray);
    }

    public void zoomCrossword(){
        if(crossword.getIsZoomed()){
            Log.d(LOG_TAG, "Zoom out FAB pressed");
            // TODO: Set zoom out icon instead of zoom in icon in this set
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
}
