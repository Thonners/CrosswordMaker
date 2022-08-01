package com.thonners.crosswordmaker;

import android.animation.Animator;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
//import android.app.Fragment;
import androidx.cardview.widget.CardView;
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
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DictionaryPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DictionaryPageFragment#/*newInstance} factory method to
 * create an instance of this fragment.
 */
public class DictionaryPageFragment extends Fragment {
    private static final String LOG_TAG = "DictionaryFragment";

    public static final int ENTRY_EXIT_ANIMATION_DURATION = 250 ;       // Duration of an entry/exit animation for the results cards
    public static final int ENTRY_EXIT_ANIMATION_Y_TRANSLATE = 100 ;    // Y translation distance for an entry/exit animation for the results cards
    public static final int ENTRY_EXIT_ANIMATION_STAGGER = 50 ;        // Delay duration between subsequent views for an entry/exit animation for the results cards

    private Button searchButton ;
    private EditText inputBox ;
    private LinearLayout resultsLinearLayout ;
    private LinearLayout progressLinearLayout ;
    private String searchTerm;
    private String searchPrefix = "define:";
    private boolean searchMWUnderway = false ;
    private String lastSearch = null;

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
        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchClicked();
                    return true;
                }
                return false;
            }
        });
        resultsLinearLayout = (LinearLayout) view.findViewById(R.id.dictionary_results_layout);
        progressLinearLayout = (LinearLayout) view.findViewById(R.id.dictionary_header_progress);
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
        // TODO: Use whatever replaced onActivityResult now that it's been deprecated
        // When search returns to activity, clear search box?
        inputBox.selectAll();
    }

    public interface OnFragmentInteractionListener {
        public void searchDictionary(String searchTerm);
    }

    public void searchClicked() {
        // Search button clicked. Check internet connected.
        if (!networkIsAvailable()) {
            // Toast to say connect to internet
            Log.d(LOG_TAG, "Search button clicked. Internet connection not detected. Showing toast and doing nothing else...");
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.dictionary_internet_connection_required), Toast.LENGTH_SHORT);
            toast.show();
        } else if (searchMWUnderway) {
            // Show searching toast
            Log.d(LOG_TAG, "SearchMWUnderway = true, implying search hasn't yet returned, so not submitting new search yet.");
            //showAlreadySearchingToast();
        } else {
            // Hide keyboard. & clear any previous results from the results view.
            hideKeyboard();
            clearResultsView(true);
        }
    }

    private void search() {
        // check that the EditText isn't blank
        searchTerm = inputBox.getText().toString().trim();
        if (searchTerm.length() > 0) {
            Log.d(LOG_TAG, "Search button clicked. Trying MW dictionary");
            searchMWDictionary(searchTerm);
        } else {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.tutorial_toast_dictionary), Toast.LENGTH_SHORT);
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

    public void searchMWDictionary(final String query) {
        Log.d(LOG_TAG, "SearchMW called");
        // Set to prevent multiple searches running at once
        searchMWUnderway = true;
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
                        // Get the individual cards, and animate them into view
                        if (theFinalView instanceof LinearLayout) {
                            Log.d(LOG_TAG,"Animating results in...");
                            for (int i = 0 ; i < theFinalView.getChildCount() ; i++) {
                                View view = theFinalView.getChildAt(i) ;
                                view.animate()
                                        .translationY(0)
                                        .setDuration(ENTRY_EXIT_ANIMATION_DURATION)
                                        .alpha(1.0f)
                                        .setStartDelay(ENTRY_EXIT_ANIMATION_STAGGER * i)
                                        .setListener(null) ;
                            }
                        }
                        break;
                    case DictionaryMWDownloadDefinition.SEARCH_SUGGESTIONS:
                        Log.d(LOG_TAG, "Search returned with suggestions. Showing prompt...");
                        // Add the returned view to the answers
                        resultsLinearLayout.addView(theFinalView);
                        final TextView promptTV = (TextView) getActivity().findViewById(DictionaryMWDownloadDefinition.PROMPT_TV_ID);
                        promptTV.setClickable(true);
                        promptTV.animate()
                                .setDuration(ENTRY_EXIT_ANIMATION_DURATION)
                                .alpha(1.0f)
                                .setListener(null) ;
                        promptTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Turn all the hidden TextViews to VISIBLE
                                Log.d(LOG_TAG, "Prompt clicked. Changing promprt text...");
                                promptTV.animate()
                                        .alpha(0.0f)
                                        .setDuration(ENTRY_EXIT_ANIMATION_DURATION)
                                        .translationY(ENTRY_EXIT_ANIMATION_Y_TRANSLATE)
                                        .setListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animator) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animator) {
                                                promptTV.setText(getString(R.string.dictionary_suggestions));

                                                Card searchGoogleCard = new Card(getActivity(), getString(R.string.dictionary_search_google));
                                                searchGoogleCard.setTranslationY(ENTRY_EXIT_ANIMATION_Y_TRANSLATE);
                                                searchGoogleCard.setAlpha(0.0f);
                                                searchGoogleCard.setVisibility(View.VISIBLE);
                                                searchGoogleCard.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Log.d(LOG_TAG, "Search Google Clicked - Searching for: " + searchTerm);
                                                        searchGoogle(searchTerm);
                                                    }
                                                });
                                                resultsLinearLayout.addView(searchGoogleCard,0);
                                                searchGoogleCard.animate()
                                                        .alpha(1.0f)
                                                        .setDuration(ENTRY_EXIT_ANIMATION_DURATION)
                                                        .translationY(0)
                                                        .setListener(null) ;

                                                Log.d(LOG_TAG, "Setting options to visible");
                                                for (int i = 0; i < theFinalView.getChildCount(); i++) {
                                                    View view = theFinalView.getChildAt(i) ;
                                                    view.setAlpha(0.0f);
                                                    view.setTranslationY(ENTRY_EXIT_ANIMATION_Y_TRANSLATE);
                                                    if (view instanceof Card) {
                                                        final Card card = (Card) theFinalView.getChildAt(i);
                                                        card.setVisibility(View.VISIBLE);
                                                        card.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Log.d(LOG_TAG, "Suggestion clicked. Searching Dictionary for: " + card.getSuggestionText());
                                                                mListener.searchDictionary(card.getSuggestionText());
                                                            }
                                                        });

                                                    } else if (view instanceof TextView) {
                                                    } else {
                                                        Log.d(LOG_TAG, "Child is a " + theFinalView.getChildAt(i).getClass().getName());
                                                        Log.d(LOG_TAG, "Something's gone wrong!");
                                                    }
                                                    // Animate the entry
                                                    view.animate()
                                                            .alpha(1.0f)
                                                            .setDuration(ENTRY_EXIT_ANIMATION_DURATION)
                                                            .translationY(0)
                                                            .setStartDelay(ENTRY_EXIT_ANIMATION_STAGGER * i)
                                                            .setListener(null) ;
                                                }
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animator) {

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animator) {

                                            }
                                        });

                            }
                        });
                        break;
                    case DictionaryMWDownloadDefinition.SEARCH_NO_SUGGESTIONS:
                        Log.d(LOG_TAG, "Search returned unsuccessfully, without suggestions. Showing word not found message");
                        CardView cardView = new CardView(getActivity());
                        cardView.setUseCompatPadding(true);
                        TextView tv2 = new TextView(getActivity());
                        tv2.setText(getString(R.string.dictionary_word_not_found) + " " + getString(R.string.dictionary_search_google));
                        tv2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                searchGoogle(query);
                            }
                        });
                        cardView.addView(tv2);
                        tv2.setPadding(getResources().getDimensionPixelOffset(R.dimen.home_card_padding), getResources().getDimensionPixelOffset(R.dimen.home_card_padding), getResources().getDimensionPixelOffset(R.dimen.home_card_padding), getResources().getDimensionPixelOffset(R.dimen.home_card_padding));
                        resultsLinearLayout.addView(cardView);
                        break;
                }
                searchMWUnderway = false ;  // Reset to allow future searches
            }
        };
        DictionaryMWDownloadDefinition definition = new DictionaryMWDownloadDefinition(getActivity(), query, progressLinearLayout, searchButton, listener);
        definition.execute();
    }

    private void hideKeyboard() {
        // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void clearResultsView(boolean search) {
        // Get number of children to the view - use this to determine how deep need to go to get to the cards
        if (resultsLinearLayout.getChildCount() > 0 ) {
            if (resultsLinearLayout.getChildAt(0) instanceof LinearLayout) {
                LinearLayout resultsInnerLinearLayout = (LinearLayout) resultsLinearLayout.getChildAt(0) ;
                animateViewsOutOfLayout(resultsInnerLinearLayout,search);
            } else {
                animateViewsOutOfLayout(resultsLinearLayout,search);
            }
        } else {
            search();
        }
    }

    private void animateViewsOutOfLayout(LinearLayout layout, boolean search) {
        // Prep the animator listener
        Animator.AnimatorListener animatorListener = null ;
        // Counter for use in staggering the animation
        int j = 0 ;
        // Animate the views out
        for (int i = layout.getChildCount() - 1 ; i >= 0 ; i--) {
            // Create the listener if it's the final (ie. top) view
            if (i == 0 && search) {
                animatorListener = new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        // Actually remove the views
                        resultsLinearLayout.removeAllViews();
                        // Search
                        search();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                };
            }
            View view = resultsLinearLayout.getChildAt(i) ;
            if (view != null) {
                Log.d(LOG_TAG,"Animating view removal. childcount = " + layout.getChildCount());
                view.animate()
                        .setDuration(ENTRY_EXIT_ANIMATION_DURATION)
                        .alpha(0.0f)
                        .translationY(ENTRY_EXIT_ANIMATION_Y_TRANSLATE)
                        .setStartDelay(ENTRY_EXIT_ANIMATION_STAGGER * j)
                        .setListener(animatorListener);
            }
            j++ ;
        }

    }

    private boolean networkIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setSearchTerm(String searchTerm) {
        inputBox.setText(searchTerm);
    }
    public EditText getInputBox() {
        return inputBox;
    }
    public void inputBoxRequestFocus() {
        inputBox.requestFocus();
        if(!searchMWUnderway) {
            inputBox.selectAll();
        }
    }

}
