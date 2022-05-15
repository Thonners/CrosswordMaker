package com.thonners.crosswordmaker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Simple Activity to display the SettingsFragment.
 *
 * All admin is done by the Fragment, and this is just a wrapper.
 *
 * @author M Thomas
 * @since 07/12/16
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make the fragment the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit() ;
    }
}
