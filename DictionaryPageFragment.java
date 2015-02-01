package com.thonners.crosswordmaker;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DictionaryPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DictionaryPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DictionaryPageFragment extends Fragment {
    private static final String LOG_TAG = "DictionaryFragment";

    Button searchButton ;
    EditText inputBox ;
    String searchTerm;
    String searchPrefix = "define:";

    private OnFragmentInteractionListener mListener;

     public DictionaryPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);
        searchButton = (Button) view.findViewById(R.id.dictionary_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchClicked();
            }
        });
        inputBox = (EditText) view.findViewById(R.id.dictionary_search_input);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When search returns to activity, clear search box?
        inputBox.selectAll();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void searchClicked() {
        // Search button clicked

        // check that the EditText isn't blank
        searchTerm = inputBox.getText().toString();
        if(searchTerm.length() > 0) {
            Log.d(LOG_TAG, "Search button clicked. Starting google search intent");
            searchGoogle(searchPrefix + searchTerm);
        } else {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.tutorial_toast_dictionary),Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void searchGoogle(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
