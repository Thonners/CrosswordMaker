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

    CrosswordLibraryManager libraryManager ;
    LinearLayout layout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_crossword_selector);

        layout = (LinearLayout) findViewById(R.id.saved_selector_layout);

        libraryManager = new CrosswordLibraryManager(this);

        int i = 0;
        for (CrosswordLibraryManager.SavedCrossword savedCrossword : libraryManager.getSavedCrosswords()) {
            addCrosswordToLayout(i,savedCrossword);
            i++ ;
        }

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

    private void addCrosswordToLayout(int index, CrosswordLibraryManager.SavedCrossword savedCrossword) {

        Card card = new Card(getApplicationContext(),savedCrossword.getTitle(),savedCrossword.getDate(),savedCrossword.getDisplayPercentageComplete()) ;
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

        Log.d(LOG_TAG,"Crossword selected: " + libraryManager.getSavedCrosswords().get(i).getTitle());

        File crossword = null;
        String[] savedCrosswordArray = null;

        // Get saved crossword
        // Pretty sure this can come out of the try-catch loop...
        try {
            crossword = new File(libraryManager.getSavedCrosswords().get(i).getCrosswordFile(), Crossword.SAVE_CROSSWORD_FILE_NAME);

            if(crossword.exists()) {
                savedCrosswordArray = Crossword.getSaveArray(crossword);
            } else {
                Log.e(LOG_TAG,"Selected 'saved crossword' doesn't seem to exist");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,"Exception thrown when trying to get file: " + e.getMessage());
        }

        if (savedCrosswordArray != null) {
            Log.d(LOG_TAG, "Starting CrosswordActivity with saved crossword: " + savedCrosswordArray[Crossword.SAVED_ARRAY_INDEX_TITLE]);
            startNewCrosswordActivity(savedCrosswordArray);
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
