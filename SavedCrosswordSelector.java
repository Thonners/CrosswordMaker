package com.thonners.crosswordmaker;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SavedCrosswordSelector extends ActionBarActivity  {

    private static final String LOG_TAG = "SavedCrosswordSelector" ;

    File[] savedCrosswords ;    // Note that this is the directory in which the crossword and images (if they exist) will be saved
    File rootDir;

    LinearLayout layout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_crossword_selector);

        layout = (LinearLayout) findViewById(R.id.saved_selector_layout);

        getSavedFiles();
        displaySavedFiles() ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_crossword_selector, menu);
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

    private void getSavedFiles() {
        // Get list of saved files and add to savedCrosswords
        try {
            Log.d(LOG_TAG, "Getting list of crossword files.");
            rootDir = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            savedCrosswords = rootDir.listFiles();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error getting file list: " + e.getMessage());
        }

        // Output what files were found
        Log.d(LOG_TAG,"Directory searched: " + rootDir);
        for (int i =0 ; i < savedCrosswords.length ; i++) {
            Log.d(LOG_TAG,"File at index " + i + " is " + savedCrosswords[i].getName());
        }
    }

    private void displaySavedFiles() {
        //TODO: list all files
        // TODO: create specific drawable for the background
        for (int i = 0 ; i < savedCrosswords.length ; i++ ) {
            if (savedCrosswords[i].isDirectory() && savedCrosswords[i].getName().contains("-")) {
                String[] crosswordDetails = savedCrosswords[i].getName().split("-");
                String crosswordDateSaveFormat = crosswordDetails[0];
                String crosswordName = crosswordDetails[1].replaceAll("__", "-").replaceAll("_", " ");   // Replace all used to restore any hyphens/spaces that were taken out during the fileName assignment in Crossword.initialiseSaveFiles

                addCrosswordToLayout(i, crosswordName, getNiceDate(crosswordDateSaveFormat), getPercentageComplete(savedCrosswords[i]));
            }
        }

    }
    private String getNiceDate(String dateIn) {
        // Turn date from save file into easier to read date
        SimpleDateFormat sdf = new SimpleDateFormat(Crossword.SAVE_DATE_FORMAT);    // Format of how date is input
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this); // Locale date format
        Date date ;

        try {
            date = sdf.parse(dateIn);
        } catch (ParseException e) {
            // handle exception here !
            Log.e(LOG_TAG, "Couldn't parse date into something useful, so returning it as it came in");
            return this.getResources().getString(R.string.error_crossword_date); // Return the error message to be displayed.
        }

        return dateFormat.format(date);
    }
    public String getPercentageComplete(File crosswordDir) {
        // Return the percentage of the crossword that's complete (based on number of blanks in the file)
        File crosswordFile = new File(crosswordDir, Crossword.SAVE_CROSSWORD_FILE_NAME);
        if (crosswordFile == null) {
            Log.d(LOG_TAG, "Error finding crossword file in  " + crosswordDir.getName());
            return "Error finding file" ;
        }
        Log.d(LOG_TAG, "Calculating percentage completion for " + crosswordDir.getName());

        String[] crosswordArray = Crossword.getSaveArray(crosswordFile);
        int cells = 0, nonBlanks = 0 ; // Integers to count with
        for (int i = Crossword.SAVE_ARRAY_START_INDEX ; i < crosswordArray.length ; i++) {
            // If a black cell, don't include it in the counting
            if (!crosswordArray[i].matches("-")) {
                cells++ ;
                // Check to see whether the cell is empty or filled
                if (!crosswordArray[i].matches("")) {
                    nonBlanks++;
                }
            }
        }

        Log.d(LOG_TAG, "Number of cells: " + cells + " & number of nonBlanks = " + nonBlanks);

        int percentage = (int) (nonBlanks * 100) / cells ;

        return getString(R.string.completion) + " " + percentage  + "%";
    }
    private void addCrosswordToLayout(int index, String name, String date, String percentageComplete) {

        Card card = new Card(getApplicationContext(),name,date,percentageComplete) ;
        card.setId(index);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crosswordSelected(v);
            }
        });
        layout.addView(card);

    }

    private void crosswordSelected(View view) {
        int i = view.getId() ;  // get index of save file

        Log.d(LOG_TAG,"Crossword selected: " + savedCrosswords[i].getName());

        File crossword = null;
        String[] savedCrossword = null;

        // Get saved crossword
        // Pretty sure this can come out of the try-catch loop...
        try {
            crossword = new File(savedCrosswords[i], Crossword.SAVE_CROSSWORD_FILE_NAME);

            if(crossword.exists()) {
                savedCrossword = Crossword.getSaveArray(crossword);
            } else {
                Log.e(LOG_TAG,"Selected 'saved crossword' doesn't seem to exist");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,"Exception thrown when trying to get file: " + e.getMessage());
        }

        if (savedCrossword != null) {
            Log.d(LOG_TAG, "Starting CrosswordActivity with saved crossword: " + savedCrossword[Crossword.SAVED_ARRAY_INDEX_TITLE]);
            startNewCrosswordActivity(savedCrossword);
        }
    }

    private void startNewCrosswordActivity(String[] savedCrossword) {
        showLoadingToast();
        // Start new crossword activity
        Intent crosswordActivity = new Intent(this, CrosswordSliderActivity.class);
        crosswordActivity.putExtra(Crossword.CROSSWORD_EXTRA, savedCrossword);
        startActivity(crosswordActivity);
    }

    private void showLoadingToast() {
        // Display loading toast as load can take a while
        Toast loadingToast = Toast.makeText(this, getString(R.string.loading), Toast.LENGTH_LONG);
        loadingToast.show();
    }
}
