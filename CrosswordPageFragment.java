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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrosswordPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrosswordPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrosswordPageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String ARG_TAB_POSITION = "tabPosition" ;
    private static final String LOG_TAG = "CrosswordPageFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int tabPosition ;

    private OnFragmentInteractionListener mListener;

    public static CrosswordPageFragment newInstance(int position) {
        CrosswordPageFragment fragment = new CrosswordPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, position);
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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            switch (tabPosition) {
                case 0 :
                    return inflater.inflate(R.layout.fragment_crossword_page, container, false);
                case 1 :
                    return inflater.inflate(R.layout.fragment_clues, container, false);
                case 2 :
                    return inflater.inflate(R.layout.fragment_dictionary, container, false);
                case 3 :
                    return inflater.inflate(R.layout.fragment_anagram, container, false);
                case 4 :
                    return inflater.inflate(R.layout.fragment_doodle, container,false);

            }

        // Safety net in case tab position is outside of range specified in switch statement
                    return inflater.inflate(R.layout.fragment_doodle, container, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public void searchClicked(View view) {
        // Search button clicked
        Log.d(LOG_TAG, "Search button clicked");
    }


}
