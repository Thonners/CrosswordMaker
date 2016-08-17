package com.thonners.crosswordmaker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class CrosswordGridEditor extends ActionBarActivity {

    private static final String LOG_TAG = "CrosswordEditorActivity";

    private RelativeLayout mainLayout ;
    private FooterButton deleteButton ;
    private FloatingActionButton saveFab ;

    private GridLayout crosswordGrid ;
    private Crossword crossword ;
    private String[] originalCrosswordStringArray;
    private String[] newCrossworyStringArray ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossword_grid_editor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        originalCrosswordStringArray = getIntent().getStringArrayExtra(Crossword.CROSSWORD_EXTRA);
        newCrossworyStringArray = new String[originalCrosswordStringArray.length];
        initialise();

        createCrossword();
        showWarningToast();
        this.setTitle(crossword.getActivityTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crossword_grid_editor, menu);
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

    private void initialise() {
        mainLayout = (RelativeLayout) findViewById(R.id.editor_main_layout);
        crosswordGrid = (GridLayout) findViewById(R.id.main_grid_editor);
        saveFab = (FloatingActionButton) findViewById(R.id.fab_edit_save);

        deleteButton = new FooterButton(this, getString(R.string.delete));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Delete Clicked.");
                showDeleteDialog();
            }
        });
        mainLayout.addView(deleteButton);
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void createCrossword() {
        // Create the crossword
        crossword = new Crossword(this, crosswordGrid, originalCrosswordStringArray,true);
    }


    private void showWarningToast() {
        Toast.makeText(this,getResources().getString(R.string.edit_warning_toast),Toast.LENGTH_LONG).show();
    }
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.delete_dialog_title) ;        // Set the action buttons
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int id){
                        // User clicked Delete, so delete crossword
                        deleteCrossword();
                    }
                }

        ) ;
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                        dialog.cancel();
                    }
                }

        );
        builder.show();
    }

    public void saveClicked(View view) {
        Log.d(LOG_TAG, "Save clicked, so saving crossword now...");
        saveCrosswordGrid();

        Log.d(LOG_TAG,"Crossword saved, opening new activity...");
        CrosswordLibraryManager crosswordLibraryManager = new CrosswordLibraryManager(this);
        crosswordLibraryManager.openCrossword(crossword.getSaveDir());
    }

    private void saveCrosswordGrid() {
        // Save the new grid to the crossword
        // Save admin details to newCrosswordStringArray
        for (int i=0 ; i < Crossword.SAVE_ARRAY_START_INDEX ; i++) {
            Log.d(LOG_TAG,"Setting value at index position " + i + " of newCrosswordSA to: " + originalCrosswordStringArray[i]);
            newCrossworyStringArray[i] = originalCrosswordStringArray[i] ;
        }

        getNewGrid();

        // Loop through original and new grids and find where they differ to the original
        for (int i=Crossword.SAVE_ARRAY_START_INDEX ; i < originalCrosswordStringArray.length ; i++) {
            if (!newCrossworyStringArray[i].matches("-")) {
                // If new array isn't a black cell, carryover value from old array, unless it used to be a black cell, in which case set it blank
                String oldValue = originalCrosswordStringArray[i] ;
                if (oldValue.matches("-")) {
                    newCrossworyStringArray[i] = "" ;
                } else {
                    newCrossworyStringArray[i] = originalCrosswordStringArray[i] ;
                }
            }
            Log.d(LOG_TAG, "Setting value at index: " + i  + " to " + newCrossworyStringArray[i]);
        }
        Log.d(LOG_TAG,"Setting new string array in crossword");
        crossword.setSaveArray(newCrossworyStringArray);
        crossword.saveCrossword();
    }
    private void getNewGrid() {
        newCrossworyStringArray = crossword.getSaveArray() ;
    }
    private void deleteCrossword() {
        new CrosswordLibraryManager(this).deleteSavedCrossword(crossword.getSaveDir());
        //crossword.deleteCrossword();
    }
}
