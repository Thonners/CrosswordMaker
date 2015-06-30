package com.thonners.crosswordmaker;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;


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

        Card card = new Card(getApplicationContext(),savedCrossword.getTitle(),savedCrossword.getDisplayDate(),savedCrossword.getDisplayPercentageComplete()) ;
        card.setId(index);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crosswordSelected(v);
            }
        });
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    toggleChangeCardElevation(v);

                return true;
            }
        });
        layout.addView(card);

    }
    private void crosswordSelected(View view) {
        int i = view.getId() ;  // get index of save file

        Log.d(LOG_TAG, "Crossword selected: " + libraryManager.getSavedCrosswords().get(i).getTitle());

        libraryManager.openCrossword(libraryManager.getSavedCrosswords().get(i).getCrosswordFile());
    }

    private void toggleChangeCardElevation(View view) {
        // Method to toggle whether card is highlighted (i.e. raised) after long click
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (view.getElevation() == getResources().getDimension(R.dimen.z_card_default)) {
                view.setElevation(getResources().getDimension(R.dimen.z_library_card_highlighted));
            } else {
                view.setElevation(getResources().getDimension(R.dimen.z_card_default));
            }
        } else  {
            view.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }
    }

    private void toggleEditDeleteButtons(View view) {
        // Method to show or hide the edit and delete buttons

    }

}
