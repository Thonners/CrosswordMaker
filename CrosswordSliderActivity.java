package com.thonners.crosswordmaker;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


public class CrosswordSliderActivity extends ActionBarActivity implements CrosswordPageFragment.OnFragmentInteractionListener, CluePageFragment.OnFragmentInteractionListener, DictionaryPageFragment.OnFragmentInteractionListener, AnagramPageFragment.OnAnagramFragmentListener, DoodlePageFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = "CrosswordSliderActivity";

    private static final int NUM_PAGES      = 5 ;    // Number of slidable view/pages. Crossword, Clues, Anagram, Dictionary, Doodle.
    private static final int CROSSWORD_TAB  = 0 ;
    private static final int CLUE_TAB       = 1 ;
    private static final int DICTIONARY_TAB = 2 ;
    private static final int ANAGRAM_TAB    = 3 ;
    private static final int DOODLE_TAB     = 4 ;

    private ViewPager pager ;               // This handles the animation/transition between pages
    private PagerAdapter pagerAdapter ;     // This provides the pages for the PagerAdapter.
    private CrosswordPageFragment.OnFragmentInteractionListener onFragmentInteractionListener ;

    private CharSequence[] tabTitles ;

    // Fragments
    CrosswordPageFragment crosswordPageFragment ;
    CluePageFragment cluePageFragment ;
    DictionaryPageFragment dictionaryPageFragment;
    AnagramPageFragment anagramPageFragment;
    DoodlePageFragment doodlePageFragment;

    String[] crosswordStringArray;
    Crossword crossword ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crosword_slider);

        initialise();

    }

    // onPause called when activity is being shut down. Use it to save the progress
    @Override
    protected void onPause() {
        super.onPause();    // Always call superclass first
        Log.d(LOG_TAG, "onPaused called - saving crossword.");

        crosswordPageFragment.getCrossword().saveCrossword();
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
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                // Open some settings menu
                openSettings();
                break ;
            case R.id.action_save:
                // Save the grid
                saveGrid();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, i.e. the crossword activity, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, return to the crossword Activity
            pager.setCurrentItem(0);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
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
                case DICTIONARY_TAB:
                    dictionaryPageFragment = new DictionaryPageFragment() ;
                    return dictionaryPageFragment;
                case ANAGRAM_TAB:
                    anagramPageFragment = new AnagramPageFragment();
                    return anagramPageFragment;
                case DOODLE_TAB:
                    doodlePageFragment = new DoodlePageFragment();
                    return doodlePageFragment;
            }

            // Safety net - in case position is out of range shown above. Should never be needed
            return doodlePageFragment;
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
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void saveGrid() {
        // Save the grid
        crosswordPageFragment.getCrossword().saveCrossword();

        Toast toast = Toast.makeText(this,"Crossword progress saved.", Toast.LENGTH_SHORT);
        toast.show();
    }
    private void openSettings() {
        // TODO: come up with some settings / an activity for settings
        Toast t = Toast.makeText(this, "Will create a settings option soon", Toast.LENGTH_SHORT);
        t.show();
    }

    public void searchDictionary(String searchTerm) {
        // Search the dictionary:
        Log.d(LOG_TAG, "Searching for dictionary from Slider Activity for word: " + searchTerm);

        // Set text in search box to match that of searchTerm
        dictionaryPageFragment.setSearchTerm(searchTerm);
        // Run search
        dictionaryPageFragment.searchClicked();
        // Change to dictionary tab
        pager.setCurrentItem(DICTIONARY_TAB, true);
    }


}
