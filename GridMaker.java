package com.thonners.crosswordmaker;

import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;


public class GridMaker extends ActionBarActivity {

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
        Log.d("CWM","Enter clicked (GridMaker activity)");
        Log.d("CWM","Freezing grid" +
                "");
        crossword.freezeGrid();

    }


}
