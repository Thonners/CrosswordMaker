package com.thonners.crosswordmaker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by mat on 07/03/15.
 */
public class ToolkitSliderActivity extends ActionBarActivity implements DictionaryPageFragment.OnFragmentInteractionListener, AnagramPageFragment.OnAnagramFragmentListener, DoodlePageFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = "ToolkitSliderActivity";

    private static final int NUM_PAGES      = 3;     // Number of pages in the slider
    private static final int DICTIONARY_TAB = 0 ;
    private static final int ANAGRAM_TAB    = 1 ;
    private static final int DOODLE_TAB     = 2 ;

    private ViewPager pager ;               // Handles the transition between fragments
    private PagerAdapter pagerAdapter ;     // Provides the pages for the pager

    private boolean dontShowKeyboard = false ;

    private CharSequence[] tabTitles ;

    // Fragments
    DictionaryPageFragment dictionaryPageFragment ;
    AnagramPageFragment anagramPageFragment ;
    DoodlePageFragment doodlePageFragment ;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crosword_slider);

        initialise() ;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolkit, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
    public void onFragmentInteraction(Uri uri) {
        Log.d(LOG_TAG, "onFragmentInteraction called");
    }

    private void initialise() {
        // Initialise pager
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(NUM_PAGES);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state)
            {
                if (state == ViewPager.SCROLL_STATE_IDLE)
                {
                    switch (pager.getCurrentItem()) {
                        case DICTIONARY_TAB:
                            dictionaryPageFragment.inputBoxRequestFocus();
                            if (dontShowKeyboard) {
                                dontShowKeyboard = false ;  // Reset for next time
                            } else {
                                showKeyboard(dictionaryPageFragment.getInputBox());
                            }
                            break;
                        case ANAGRAM_TAB:
                            anagramPageFragment.inputBoxRequestFocus();
                            showKeyboard(anagramPageFragment.getInputBox());
                            break;
                        case DOODLE_TAB:
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

        tabTitles = getResources().getTextArray(R.array.tab_titles_toolkit);

    }
    private void hideKeyboard() {
        // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showKeyboard(View view) {
        Log.d(LOG_TAG, "Show keyboard called");
        // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, inputManager.SHOW_IMPLICIT);
    }
    private void openSettings() {
        // TODO: come up with some settings / an activity for settings
        Toast t = Toast.makeText(this, "Will create a settings option soon", Toast.LENGTH_SHORT);
        t.show();
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

}
