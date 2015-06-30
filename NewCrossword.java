package com.thonners.crosswordmaker;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;


public class NewCrossword extends ActionBarActivity {

    static final String AUTO_GRID_GENERATION = "com.thonners.crosswordmaker.autoGeneration" ;
//    static final String SCREEN_WIDTH = "com.thonners.crosswordmaker.screenx" ;
//    static final String SCREEN_HEIGHT = "com.thonners.crosswordmaker.screeny" ;

    String crosswordTitle;
    String crosswordDate;

    NumberPicker numberPicker ;

    private int min = 3 ;
    private int max = 20 ;
    private int defaultValue = 13 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_crossword);

        getIntents();

        setupNumberPicker();

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
        numberPicker.setValue(defaultValue);
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
        intent.putExtra(AUTO_GRID_GENERATION,false);
        startActivity(intent);

    }

    public void autoEnterClicked(View view) {
        // Work out the grid from a photo. Put the boolean value into the gridmaker intent and have that deal with getting the image
        // Get number entered
        int rows = numberPicker.getValue();

        // Pass to new activity
        Intent intent = new Intent(this, GridMaker.class);
        intent.putExtra(Crossword.CROSSWORD_EXTRA_TITLE,crosswordTitle);
        intent.putExtra(Crossword.CROSSWORD_EXTRA_DATE, crosswordDate);
        intent.putExtra(Crossword.CROSSWORD_EXTRA_NO_ROWS, rows);
        intent.putExtra(AUTO_GRID_GENERATION,true);
        startActivity(intent);
    }


}
