package com.thonners.crosswordmaker;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * A PreferenceFragment to display the app's settings/options.
 *
 * Settings will be stored as SharedPreferences, and created according to preferences.xml
 *
 * @author M Thomas
 * @since 07/12/16
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Populate according to preferences.xml
        addPreferencesFromResource(R.xml.preferences);
    }
}
