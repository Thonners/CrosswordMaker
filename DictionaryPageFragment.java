package com.thonners.crosswordmaker;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    LinearLayout resultsLinearLayout ;
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
        resultsLinearLayout = (LinearLayout) view.findViewById(R.id.dictionary_results_layout);
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
        // Search button clicked. Check internet connected.
        if (!networkIsAvailable()) {
            // Toast to say connect to internet
                Log.d(LOG_TAG, "Search button clicked. Internet connection not detected. Showing toast and doing nothing else...");
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.dictionary_internet_connection_required), Toast.LENGTH_SHORT);
                toast.show();
        } else {
            // Hide keyboard. & clear any previous results from the results view.
            hideKeyboard();
            clearResultsView();
            // check that the EditText isn't blank
            searchTerm = inputBox.getText().toString();
            if (searchTerm.length() > 0) {

                Log.d(LOG_TAG, "Search button clicked. Trying MW dictionary");
                searchMWDictionary(searchTerm);

            } else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.tutorial_toast_dictionary), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public void searchGoogle(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void searchMWDictionary(final String query) {
        // Search the MerriamWebster dictionary
        DictionaryMWDownloadDefinition.DictionaryMWDownloadDefinitionListener listener = new DictionaryMWDownloadDefinition.DictionaryMWDownloadDefinitionListener() {
            @Override
            public void completionCallBack(final ViewGroup theFinalView, final int searchSuccessState) {
                // Handle what happens to the output from the dictionary here
                switch (searchSuccessState) {
                    case DictionaryMWDownloadDefinition.SEARCH_NOT_COMPLETED:
                        Log.d(LOG_TAG, "Something went wrong with the search. Status returned from DictionaryMWDownloadDefinition as -1");
                        TextView tv = new TextView(getActivity());
                        tv.setText(getString(R.string.dictionary_error));
                        resultsLinearLayout.addView(tv);
                        break;
                    case DictionaryMWDownloadDefinition.SEARCH_SUCCESSFUL:
                        Log.d(LOG_TAG, "Search returned successfully. Showing results...");
                        resultsLinearLayout.addView(theFinalView);
                        break;
                    case DictionaryMWDownloadDefinition.SEARCH_SUGGESTIONS:
                        Log.d(LOG_TAG, "Search returned with suggestions. Showing prompt...");
                        // Add the returned view to the answers
                        resultsLinearLayout.addView(theFinalView);
                        final TextView promptTV = (TextView) getActivity().findViewById(DictionaryMWDownloadDefinition.PROMPT_TV_ID);
                        promptTV.setClickable(true);
                        promptTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Turn all the hidden TextViews to VISIBLE
                                Log.d(LOG_TAG, "Prompt clicked. Changing promprt text...");
                                promptTV.setText(getString(R.string.dictionary_suggestions));

                                Log.d(LOG_TAG, "Setting options to visible");
                                for (int i = 0; i < theFinalView.getChildCount(); i++) {
                                    theFinalView.getChildAt(i).setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        break;
                    case DictionaryMWDownloadDefinition.SEARCH_NO_SUGGESTIONS:
                        Log.d(LOG_TAG, "Search returned unsuccessfully, without suggestions. Showing word not found message");
                        TextView tv2 = new TextView(getActivity());
                        tv2.setText(getString(R.string.dictionary_word_not_found) + getString(R.string.dictionary_search_google));
                        tv2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                searchGoogle(query);
                            }
                        });
                        resultsLinearLayout.addView(tv2);
                        break;
                }
            }
        };
        DictionaryMWDownloadDefinition definition = new DictionaryMWDownloadDefinition(getActivity(),query,listener);
        definition.execute();
    }

    private void hideKeyboard() {
    // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void clearResultsView() {
        // Clear any results from the view
        if (resultsLinearLayout.getChildCount() > 0 ) {
            resultsLinearLayout.removeAllViews();
        }
    }
    private boolean networkIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
