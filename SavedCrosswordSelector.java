package com.thonners.crosswordmaker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SavedCrosswordSelector extends ActionBarActivity  {

    private static final String LOG_TAG = "Saved Crossword Selector" ;

    File[] savedCrosswords ;    // Note that this is the directory in which the crossword and images (if they exist) will be saved
    File rootDir;

    int viewIndexCounter;
    int sidePadding ;
    int verticalPadding ;

    Drawable defaultBackground ; //= getResources().getDrawable(R.drawable.cell_white) ;    // TODO: change this to specific drawable when created

    LinearLayout layout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_crossword_selector);

        layout = (LinearLayout) findViewById(R.id.saved_selector_layout);

        sidePadding = (int) getResources().getDimension(R.dimen.saved_crossword_layout_side_padding);
        verticalPadding = (int) getResources().getDimension(R.dimen.saved_crossword_layout_vertical_padding);

        defaultBackground = getResources().getDrawable(R.drawable.cell_white) ;    // TODO: change this to specific drawable when created

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
            if (savedCrosswords[i].isDirectory()) {
                String[] crosswordDetails = savedCrosswords[i].getName().split("-");
                String crosswordDateSaveFormat = crosswordDetails[0];
                String crosswordName = crosswordDetails[1].replaceAll("__", "-").replaceAll("_", " ");   // Replace all used to restore any hyphens/spaces that were taken out during the fileName assignment in Crossword.initialiseSaveFiles

                addCrosswordToLayout(i, "Name: " + crosswordName + "; Date: " + getNiceDate(crosswordDateSaveFormat));
            }
        }

    }

    public void savedCrosswordSelected(View view) {
        // TODO: load CrosswordActivity with the save file selected

        // Get

        // Start activity with Intent
        Intent crosswordActivity = new Intent(this, CrosswordActivity.class);

    }

    private String getNiceDate(String dateIn) {
        // Turn date from save file into easier to read date
        SimpleDateFormat sdf = new SimpleDateFormat(Crossword.SAVE_DATE_FORMAT);    // Format of how date is input
        Date date ;

        try {
            date = sdf.parse(dateIn);
        } catch (ParseException e) {
            // handle exception here !
            Log.e(LOG_TAG, "Couldn't parse date into something useful, so returning it as it came in");
            return dateIn ;
        }

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        return dateFormat.format(date);

    }

    private void addCrosswordToLayout(int index, String text) {

        TextView tv = new TextView(this);

        tv.setText(text) ;
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(sidePadding, verticalPadding, sidePadding, verticalPadding);
        tv.setBackground(defaultBackground);
        tv.setId(index); // Not sure if this is required or not yet
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crosswordSelected(v);
            }
        });
        layout.addView(tv);

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
        // Start new crossword activity
        Intent crosswordActivity = new Intent(this, CrosswordActivity.class);
        crosswordActivity.putExtra(Crossword.CROSSWORD_EXTRA, savedCrossword);
        startActivity(crosswordActivity);
    }
}
