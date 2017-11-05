package com.thonners.crosswordmaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

/**
 * NewCrosswordActivity Activity
 * Shows a NumberPicker to select the number of rows/columns in crossword, and an enter FAB.
 * When implemented, will also show camera button to auto-generate grid.
 *
 * @author M Thomas
 * @since 01/07/15
 */


public class NewCrosswordActivity extends AppCompatActivity {

    private static final String LOG_TAG = "NewCrosswordActivity";

    public static final String AUTO_GRID_GENERATION = "com.thonners.crosswordmaker.autoGeneration" ;
    private String crosswordTitle;
    private String crosswordDate;
    private boolean autoEnabled = true ;

    private NumberPicker numberPicker ;

    private int min = 3 ;
    private int max = 35 ;
    private int defaultCols = 13 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_crossword);

        getIntents();
        setupNumberPicker();

        // Grey out the auto-create button if it's not set in the settings
        FloatingActionButton fabAuto = (FloatingActionButton) findViewById(R.id.fab_auto) ;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        if (!prefs.getBoolean(SettingsFragment.KEY_PREF_AUTOGRID, false)) {
            fabAuto.setBackgroundColor(ContextCompat.getColor(this,R.color.light_grey));
            fabAuto.setCompatElevation(getResources().getDimension(R.dimen.cardview_default_elevation));
            autoEnabled = false ;
        }
    }

    private void getIntents() {
        // Reclaim extras passed with the intent
        crosswordTitle = getIntent().getStringExtra(Crossword.CROSSWORD_EXTRA_TITLE);
        crosswordDate = getIntent().getStringExtra(Crossword.CROSSWORD_EXTRA_DATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_crossword, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupNumberPicker() {
        // Get number picker from resource ID, and set min/max values
        numberPicker = (NumberPicker) findViewById(R.id.no_columns);
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);
        numberPicker.setValue(getDefaultColumns());
    }


    public void manualEnterClicked(View view) {
        // Pass grid size to GenerateGrid activity

        // Get number entered
        int rows = numberPicker.getValue();

        // Pass to new activity
        Intent intent = new Intent(this, GridMaker.class);
        intent.putExtra(Crossword.CROSSWORD_EXTRA_TITLE,crosswordTitle);
        intent.putExtra(Crossword.CROSSWORD_EXTRA_DATE, crosswordDate);
        intent.putExtra(Crossword.CROSSWORD_EXTRA_NO_ROWS, rows);
        intent.putExtra(AUTO_GRID_GENERATION,true);
        //intent.putExtra(AUTO_GRID_GENERATION,false);
        startActivity(intent);

    }

    public void autoEnterClicked(View view) {
        // Check that auto is enabled first, otherwise prompt to enable it in settings
        if (autoEnabled) {
            // Work out the grid from a photo. Put the boolean value into the gridmaker intent and have that deal with getting the image
            // Get number entered
            int rows = numberPicker.getValue();

            // Pass to new activity
            Intent intent = new Intent(this, GridMaker.class);
            intent.putExtra(Crossword.CROSSWORD_EXTRA_TITLE, crosswordTitle);
            intent.putExtra(Crossword.CROSSWORD_EXTRA_DATE, crosswordDate);
            intent.putExtra(Crossword.CROSSWORD_EXTRA_NO_ROWS, rows);
            intent.putExtra(AUTO_GRID_GENERATION, true);
            startActivity(intent);
        } else {
            Toast.makeText(this,"To enable auto grid generation from an image, please enable it in the settings. Note that this feature is currently in beta!",Toast.LENGTH_SHORT);
        }
    }

    /**
     * Method to read the value of the shared preferences for the default number of columns
     * @return The default number of columns for a new crossword
     */
    private int getDefaultColumns() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this) ;
        String defaultColumnsString = sharedPref.getString(SettingsFragment.KEY_PREF_DEFAULT_COLUMNS,"" + defaultCols) ;
        // Parse the string value to an int
        try {
            defaultCols = Integer.parseInt(defaultColumnsString) ;
        } catch (Exception e) {
            Log.d(LOG_TAG,"Error parsing default columns String to int: " + defaultColumnsString) ;
            Log.d(LOG_TAG,"Using default value in NewCrosswordActivity: " + defaultCols) ;
        }
        // Check that it's within the limits
        defaultCols = Math.max(min, defaultCols);
        defaultCols = Math.min(max, defaultCols);

        return defaultCols ;
    }

}
