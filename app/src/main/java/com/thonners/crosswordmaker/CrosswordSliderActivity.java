package com.thonners.crosswordmaker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;


public class CrosswordSliderActivity extends AppCompatActivity implements CrosswordPageFragment.OnFragmentInteractionListener, CluePageFragment.OnFragmentInteractionListener, DictionaryPageFragment.OnFragmentInteractionListener, AnagramPageFragment.OnAnagramFragmentListener, WikiPageFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = "CrosswordSliderActivity";

    private static final int NUM_PAGES      = 6 ;    // Number of slidable view/pages. Crossword, Clues, Manual anagram, Anagram, Dictionary, Doodle.
    private static final int CROSSWORD_TAB  = 0 ;
    private static final int CLUE_TAB       = 1 ;
    private static final int MANUAL_ANAGRAM_TAB = 2 ;
    private static final int DICTIONARY_TAB = 3 ;
    private static final int ANAGRAM_TAB    = 4 ;
    private static final int WIKI_TAB = 5 ;

    private ViewPager pager ;               // This handles the animation/transition between pages
    private PagerAdapter pagerAdapter ;     // This provides the pages for the PagerAdapter.
    private CrosswordPageFragment.OnFragmentInteractionListener onFragmentInteractionListener ;

    private boolean dontShowKeyboard = false ;

    private CharSequence[] tabTitles ;

    // Fragments
    private CrosswordPageFragment crosswordPageFragment ;
    private CluePageFragment cluePageFragment ;
    private ManualAnagramPageFragment manualAnagramPageFragment ;
    private DictionaryPageFragment dictionaryPageFragment;
    private AnagramPageFragment anagramPageFragment;
    private WikiPageFragment wikiPageFragment;

    private boolean firstVisitManualAnagram = true ;

    private String[] crosswordStringArray;
//    private Crossword crossword ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crosword_slider);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialise();

    }

    // onPause called when activity is being shut down. Use it to save the progress
    @Override
    protected void onPause() {
        super.onPause();    // Always call superclass first
        Log.d(LOG_TAG, "onPaused called - saving crossword if auto-save is on, otherwise showing dialog.");

        // If not zoomed in and on crossword fragment, go home.
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this) ;
//        if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_DEFAULT_AUTO_SAVE,true) && !crosswordPageFragment.getCrossword().isSaved()) {
//            // If auto-save on, save the grid without prompting
//            crosswordPageFragment.getCrossword().saveCrossword();
//        }

    }

    //onRestart called only when activity is being restarted after being stopped.Try redirecting here back to home page.
    @Override
    protected void onRestart() {
        super.onRestart();      // Always call superclass first!

        Log.d(LOG_TAG, "onRestart called");
    }


    private void initialise() {
        // Get intent Extras
        crosswordStringArray = getIntent().getStringArrayExtra(Crossword.CROSSWORD_EXTRA);

        // Instantiate a ViewPager and a PagerAdapter.
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(NUM_PAGES);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    switch (pager.getCurrentItem()) {
                        case CROSSWORD_TAB:
                            hideKeyboard();
                            showActionZoom();
                            break;
                        case CLUE_TAB:
                            hideActionZoom();
                            hideKeyboard();
                            break;
                        case MANUAL_ANAGRAM_TAB:
                            hideActionZoom();
                            // Show the snackbar if it's the first time the fragment has been seen
                            if (firstVisitManualAnagram) {
                                showInstructionsSnackbar();
                            } else {
                                manualAnagramPageFragment.inputBoxRequestFocus();
                                showKeyboard(manualAnagramPageFragment.getInputBox());
                            }
                            break ;
                        case DICTIONARY_TAB:
                            hideActionZoom();
                            dictionaryPageFragment.inputBoxRequestFocus();
                            if (dontShowKeyboard) {
                                dontShowKeyboard = false;  // Reset for next time
                            } else {
                                showKeyboard(dictionaryPageFragment.getInputBox());
                            }
                            break;
                        case ANAGRAM_TAB:
                            hideActionZoom();
                            anagramPageFragment.inputBoxRequestFocus();
                            showKeyboard(anagramPageFragment.getInputBox());
                            break;
                        case WIKI_TAB:
                            hideActionZoom();
                            hideKeyboard();
                            break;
                    }

                }
            }

            @Override
            public void onPageSelected(int position) {
                // Empty required method
            }

            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                // Empty required method
            }
        });

        // Get tab titles
        tabTitles = getResources().getTextArray(R.array.tab_titles);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crosword_slider, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true ;
            case R.id.action_zoom:
                // Toggle the zoom
                toggleZoom();
                break;
            case R.id.action_save:
                // Save the grid
                saveGrid();
                break;
            case R.id.action_edit:
                // Launch a CrosswordGridEditor activity
                launchEditorActivity();
                break;
            case R.id.action_retake_clues:
                // Retake the clues picture
                retakeCluesPicture();
                break;
            case R.id.action_feedback:
                // Send an email
                emailDeveloperFeedback();
                break;
            case R.id.action_instructions:
                // Show instructions dialog/activity
                showInstructions() ;
                break;
            case R.id.action_about:
                // Show 'About' Dialog
                showAboutDialog();
                break;
            case R.id.action_settings:
                // Open some settings menu
                HomeActivity.openSettings(this);
                break ;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, i.e. the crossword activity, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            //super.onBackPressed();
            if (crosswordPageFragment.getCrossword().getIsZoomed()) {
                // Zoom out if zoomed in and back pressed
                crosswordPageFragment.getCrossword().toggleZoom();
            } else {
                // If not zoomed in and on crossword fragment, go home.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this) ;
                if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_DEFAULT_AUTO_SAVE,true)) {
                    // If auto-save on, save the grid without prompting
                    crosswordPageFragment.getCrossword().saveCrossword();
                    Log.d(LOG_TAG,"goHome() called...");
                    goHome();
                } else if (!crosswordPageFragment.getCrossword().isSaved()){
                    // If not autosaving, and if not saved since last edit, prompt to save
                    AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
                    builder.setTitle(this.getString(R.string.dialog_save_title))
                            .setMessage(this.getString(R.string.dialog_save_message))
                            .setPositiveButton(this.getString(R.string.dialog_save_save), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    crosswordPageFragment.getCrossword().saveCrossword();
                                    goHome() ;
                                }
                            })
                            .setNegativeButton(this.getString(R.string.dialog_save_dont_save), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    goHome();
                                }
                            /*})
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }*/
                            });
                    // Build and show the dialog
                    builder.create().show();
                } else {
                    Log.d(LOG_TAG,"goHome() called...");
                    goHome();
                }
            }
        } else {
            // Otherwise, return to the crossword Activity
            pager.setCurrentItem(0);
        }
    }

    private void goHome() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }
    public void onFragmentInteraction(Uri uri) {
        Log.d(LOG_TAG, "onFragmentInteraction called");
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case CROSSWORD_TAB:
                    crosswordPageFragment = CrosswordPageFragment.newInstance(position,crosswordStringArray);
                    return crosswordPageFragment ;
                case CLUE_TAB:
                    cluePageFragment = CluePageFragment.newInstance(crosswordStringArray[Crossword.SAVED_ARRAY_INDEX_CLUE_IMAGE]);
                    return cluePageFragment ;
                case MANUAL_ANAGRAM_TAB:
                    manualAnagramPageFragment = new ManualAnagramPageFragment() ;
                    return manualAnagramPageFragment ;
                case DICTIONARY_TAB:
                    dictionaryPageFragment = new DictionaryPageFragment() ;
                    return dictionaryPageFragment;
                case ANAGRAM_TAB:
                    anagramPageFragment = new AnagramPageFragment();
                    return anagramPageFragment;
                case WIKI_TAB:
                    wikiPageFragment = new WikiPageFragment();
                    return wikiPageFragment;
            }

            // Safety net - in case position is out of range shown above. Should never be needed
            return wikiPageFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    private void hideKeyboard() {
        // Method to hide the keyboard
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException npe) {
            Log.d(LOG_TAG,"Caught null pointer exception trying to close keyboard.");
        }
    }
    private void hideActionZoom() {
        // Remove/hide the zoom icon from the menu
        ActionMenuItemView menuZoom = (ActionMenuItemView) findViewById(R.id.action_zoom);
        menuZoom.setVisibility(View.GONE);
    }
    private void showActionZoom(){
        // Replace the zoom icon in the menu
        ActionMenuItemView menuZoom = (ActionMenuItemView) findViewById(R.id.action_zoom);
        menuZoom.setVisibility(View.VISIBLE);
    }
    private void showKeyboard(View view) {
        Log.d(LOG_TAG,"Show keyboard called");
        // Method to show the keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, inputManager.SHOW_IMPLICIT);
    }

    public void searchDictionary(String searchTerm) {
        // Search the dictionary:
        Log.d(LOG_TAG, "Searching for dictionary from Slider Activity for word: " + searchTerm);

        // Set text in search box to match that of searchTerm
        dictionaryPageFragment.setSearchTerm(searchTerm);
        // Run search
        dictionaryPageFragment.searchClicked();
        // Make sure keyboard isn't shown, as search term is already in box
        dontShowKeyboard = true ;
        // Change to dictionary tab
        pager.setCurrentItem(DICTIONARY_TAB, true);
    }

    private void showInstructionsSnackbar() {
        // Set to false to prevent snackbar being shown again
        firstVisitManualAnagram = false ;
        // Create & show the snackbar
        Snackbar.make(pager,R.string.snackbar_instructions,Snackbar.LENGTH_LONG).setAction(R.string.show, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInstructions();
            }
        }).show();
    }

    // ------------------------- Menu button presses --------------------------------------

    private void toggleZoom() {
        // Toggle the zoom
        crosswordPageFragment.zoomCrossword();

        // Toggle the icon
        ActionMenuItemView menuZoom = (ActionMenuItemView) findViewById(R.id.action_zoom);

        // Check Android version sufficient
        if(Build.VERSION.SDK_INT >= 21) {
            if (crosswordPageFragment.getCrossword().getIsZoomed()) {
                menuZoom.setIcon(getDrawable(R.drawable.ic_zoom_out_white));
            } else {
                menuZoom.setIcon(getDrawable(R.drawable.ic_zoom_in_white));
            }
        }
    }
    public void saveGrid() {
        // Save the grid
        crosswordPageFragment.getCrossword().saveCrossword();

        Toast toast = Toast.makeText(this,"Crossword progress saved.", Toast.LENGTH_SHORT);
        toast.show();
    }
    private void showAboutDialog() {
        HomeActivity.showAboutDialog(this);
    }
    private void emailDeveloperFeedback() {
        HomeActivity.emailDeveloperFeedback(this);
    }
    private void launchEditorActivity() {
        // Launch activitiy to edit the grid
        CrosswordLibraryManager clm = new CrosswordLibraryManager(this);
        clm.openEditCrossword(crosswordPageFragment.getCrosswordSaveDir());
    }
    private void retakeCluesPicture() {

        pager.setCurrentItem(CLUE_TAB,true);
        cluePageFragment.dispatchTakePictureIntent();
    }
    private void showInstructions() {
        InstructionsDialog dialog = new InstructionsDialog() ;
        dialog.show(getSupportFragmentManager(), getResources().getString(R.string.instructions));
    }

}
