package com.thonners.crosswordmaker.ui.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Point;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.thonners.crosswordmaker.ui.fragments.AnagramPageFragment;
import com.thonners.crosswordmaker.ui.fragments.DictionaryPageFragment;
import com.thonners.crosswordmaker.InstructionsDialog;
import com.thonners.crosswordmaker.ui.fragments.ManualAnagramPageFragment;
import com.thonners.crosswordmaker.R;
import com.thonners.crosswordmaker.ui.fragments.WikiPageFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


/**
 * Created by Thonners on 07/03/15.
 */
public class ToolkitSliderActivity extends AppCompatActivity implements DictionaryPageFragment.OnFragmentInteractionListener, AnagramPageFragment.OnAnagramFragmentListener, WikiPageFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = "ToolkitSliderActivity";

    private static final int NUM_PAGES = 4;     // Number of pages in the slider
    private static final int MANUAL_ANAGRAM_TAB = 0;
    private static final int DICTIONARY_TAB = 1;
    private static final int ANAGRAM_TAB = 2;
    private static final int WIKI_TAB = 3;

    private ViewPager2 pager;               // This handles the animation/transition between pages
    private FragmentStateAdapter pagerAdapter;     // This provides the pages for the PagerAdapter.

    private boolean dontShowKeyboard = false;

    private CharSequence[] tabTitles;

    // Fragments
    private ManualAnagramPageFragment manualAnagramPageFragment;
    private DictionaryPageFragment dictionaryPageFragment;
    private AnagramPageFragment anagramPageFragment;
    private WikiPageFragment wikiPageFragment;

    private boolean firstVisitManualAnagram = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crosword_slider);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialise();

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

        switch (item.getItemId()) {
            case R.id.action_feedback:
                // Send an email
                HomeActivity.emailDeveloperFeedback(this);
                break;
            case R.id.action_instructions:
                // Show instructions dialog/activity
                showInstructions();
                break;
            case R.id.action_about:
                // Show 'About' Dialog
                HomeActivity.showAboutDialog(this);
                break;
            case R.id.action_settings:
                // Open some settings menu
                HomeActivity.openSettings(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void onFragmentInteraction(Uri uri) {
        Log.d(LOG_TAG, "onFragmentInteraction called");
    }

    private void initialise() {
        // Instantiate a ViewPager and a PagerAdapter.
        pager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(NUM_PAGES);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    switch (pager.getCurrentItem()) {
                        case MANUAL_ANAGRAM_TAB:
                            // Show the snackbar if it's the first time the fragment has been seen
                            if (firstVisitManualAnagram) {
                                showInstructionsSnackbar();
                            } else {
                                manualAnagramPageFragment.inputBoxRequestFocus();
                                showKeyboard(manualAnagramPageFragment.getInputBox());
                            }
                            break;
                        case DICTIONARY_TAB:
                            dictionaryPageFragment.inputBoxRequestFocus();
                            if (dontShowKeyboard) {
                                dontShowKeyboard = false;  // Reset for next time
                            } else {
                                showKeyboard(dictionaryPageFragment.getInputBox());
                            }
                            break;
                        case ANAGRAM_TAB:
                            anagramPageFragment.inputBoxRequestFocus();
                            showKeyboard(anagramPageFragment.getInputBox());
                            break;
                        case WIKI_TAB:
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
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        int screenWidth = size.x;
        int tabPadding = (int) (0.25 * screenWidth);
        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            View child = tabLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                child.setPadding(tabPadding, 0, tabPadding, 0);
            }
        }
    }

    private void hideKeyboard() {
        Log.d(LOG_TAG, "Hide keyboard called");
        View view = pager.getRootView();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void showKeyboard(View view) {
        Log.d(LOG_TAG, "Show keyboard called");
        // Method to hide the keyboard
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            switch (position) {
                case MANUAL_ANAGRAM_TAB:
                    manualAnagramPageFragment = new ManualAnagramPageFragment();
                    return manualAnagramPageFragment;
                case DICTIONARY_TAB:
                    dictionaryPageFragment = new DictionaryPageFragment();
                    return dictionaryPageFragment;
                case ANAGRAM_TAB:
                    anagramPageFragment = new AnagramPageFragment();
                    return anagramPageFragment;
                case WIKI_TAB:
                    wikiPageFragment = new WikiPageFragment();
                    return wikiPageFragment;
            }

            // Safety net - in case position is out of range shown above. Should never be needed
            wikiPageFragment = new WikiPageFragment();
            return wikiPageFragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
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
        dontShowKeyboard = true;
        // Change to dictionary tab
        pager.setCurrentItem(DICTIONARY_TAB, true);
    }

    private void showInstructions() {
        InstructionsDialog dialog = new InstructionsDialog();
        dialog.show(getSupportFragmentManager(), getResources().getString(R.string.instructions));
    }

    private void showInstructionsSnackbar() {
        // Set to false to prevent snackbar being shown again
        firstVisitManualAnagram = false;
        // Create & show the snackbar
        Snackbar.make(pager, R.string.snackbar_instructions, Snackbar.LENGTH_LONG).setAction(R.string.show, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInstructions();
            }
        }).show();
    }


}
