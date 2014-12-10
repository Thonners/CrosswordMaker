package com.thonners.crosswordmaker;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;


public class NewCrossword extends ActionBarActivity {

    static final String NO_ROWS = "com.thonners.crosswordmaker.rows" ;
//    static final String SCREEN_WIDTH = "com.thonners.crosswordmaker.screenx" ;
//    static final String SCREEN_HEIGHT = "com.thonners.crosswordmaker.screeny" ;

    NumberPicker numberPicker ;

    private int min = 3 ;
    private int max = 20 ;
    private int defaultValue = 8 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_crossword);

        setupNumberPicker();

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


    public void enterClicked(View view) {
        // Pass grid size to GenerateGrid activity

        // Get number entered
        int rows = numberPicker.getValue();

        // Pass to new activity
        Intent intent = new Intent(this, GridMaker.class);
        intent.putExtra(NO_ROWS, rows);
        startActivity(intent);

    }


}
