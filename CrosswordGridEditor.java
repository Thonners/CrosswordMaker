package com.thonners.crosswordmaker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;


public class CrosswordGridEditor extends ActionBarActivity {

    private static final String LOG_TAG = "CrosswordEditorActivity";

    private RelativeLayout mainLayout ;
    private FooterButton deleteButton ;

    private GridLayout crosswordGrid ;
    private Crossword crossword ;
    private String[] crosswordStringArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossword_grid_editor);
        crosswordStringArray = getIntent().getStringArrayExtra(Crossword.CROSSWORD_EXTRA);
        initialise();

        createCrossword();

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
        crossword = new Crossword(this, crosswordGrid,crosswordStringArray);
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

    private void deleteCrossword() {
        crossword.deleteCrossword();
    }
}
