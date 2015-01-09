package com.thonners.crosswordmaker;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;


public class GridMaker extends ActionBarActivity {

    private static final String LOG_TAG = "GridMaker";

    private int screenWidth ;
    private int screenHeight ;

    Crossword crossword ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_maker);

        getScreenDetails();

        GridLayout grid = (GridLayout) findViewById(R.id.main_grid);

        crossword = new Crossword(getApplicationContext(), getIntent().getIntExtra(NewCrossword.NO_ROWS,8),grid,screenWidth,screenHeight);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid_maker, menu);
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

    private void getScreenDetails() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        Log.d("CWM", "Screen Width = " + screenWidth);
        Log.d("CWM","Screen Height = " + screenHeight);
    }

    public void enterClicked(View view){
        // Freeze crossword grid and progress to next stage
        String crosswordTitle = ((TextView) findViewById(R.id.crossword_title_input)).getText().toString() ;
        String crosswordDate = ((TextView) findViewById(R.id.crossword_date_input)).getText().toString() ;
        Log.d("CWM","Enter clicked (GridMaker activity)");
        Log.d(LOG_TAG,"Setting crossword title to: " + crosswordTitle );
        Log.d(LOG_TAG,"Setting crossword date to: " + crosswordDate );

        crossword.setTitle(crosswordTitle);
        crossword.setDate(crosswordDate);

        startCrosswordActivity();

    }

    private void startCrosswordActivity(){
        // Start the com.thonners.CrosswordMaker.CrosswordActivity
        // Put the crossword in the Intent as an Extra using Crossword.getSaveArray()
        Intent intent = new Intent(this, CrosswordActivity.class);
        intent.putExtra(Crossword.CROSSWORD_EXTRA, crossword.getSaveArray());

        startActivity(intent);
    }

}
