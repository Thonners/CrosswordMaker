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

    // Keys are set in preferences.xml, these are just non-linked copies.
    public final static String KEY_PREF_DEFAULT_COLUMNS = "pref_no_columns" ;
    public final static String KEY_PREF_LOW_RAM = "pref_low_ram" ;
    public final static String KEY_PREF_CLOUD_SYNC = "pref_cloud_sync" ;
    public final static String KEY_PREF_AUTOGRID = "pref_autogrid" ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Populate according to preferences.xml
        addPreferencesFromResource(R.xml.preferences);
    }
}
