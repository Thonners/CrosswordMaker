package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 *  Relative layout to house crossword
 *  Use this instead of a GridView, as GridView is intended for other stuff
 *
 * Created by Thonners on 09/10/15.
 */
public class CrosswordGrid extends RelativeLayout {

    private static final String LOG_TAG = "CrosswordGrid" ;

    private int mGridSize ;
    private Crossword mCrossword ;
    private HorizontalScrollViewNoFocus mHorizontalScrollView ;
    private ScrollView mVerticalScrollView ;

    public CrosswordGrid(Context context, int gridSize) {
        super(context);
        mGridSize = gridSize ;
    }

    // Constructors
    public CrosswordGrid(Context context){
        super(context);
    }
    public CrosswordGrid(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public CrosswordGrid(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs,defStyle);
    }

    // ------------------------------------- Initialisation Methods --------------------------------
    public void setCrossword(Crossword crossword){
        mCrossword = crossword;
    }
    public void setGridSize(int gridSize){
        mGridSize = gridSize ;
    }
    public void addCellView(int row, int col, CellView cellView){
        // Assign ID for use in relative layout
        // Preferred method of generating view IDs
        cellView.setId(View.generateViewId());

        // Create layout params
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(row == 0) {
            lp.addRule(ALIGN_PARENT_TOP);
        } else {
            lp.addRule(RelativeLayout.BELOW,mCrossword.getCellView(row-1,col).getId());
        }
        if (col == 0) {
            lp.addRule(ALIGN_PARENT_LEFT);
        } else {
            lp.addRule(RelativeLayout.RIGHT_OF,mCrossword.getCellView(row,col -1).getId());
        }
        // Add cell to view
        this.addView(cellView,lp);
    }
    public void setHorizontalScrollView(HorizontalScrollViewNoFocus horizontalScrollView){
        mHorizontalScrollView = horizontalScrollView ;
    }
    public void setVerticalScrollView(ScrollView scrollView){
        mVerticalScrollView = scrollView ;
    }

    // --------------------------------- Public Methods --------------------------------------------
    public void scrollToView(View view) {
        // Scroll smoothly to focused view
        if(mHorizontalScrollView != null){
            //mHorizontalScrollView.smoothScrollTo(view.getLeft(),0);
            mHorizontalScrollView.requestChildFocus(view,view);
            Log.d(LOG_TAG,"HScrolling to: " + view.getLeft());
            showKeyboard(view);
        }
        if(mVerticalScrollView != null) {
            //mVerticalScrollView.smoothScrollTo(0,view.getTop());
            mVerticalScrollView.requestChildFocus(view, view);
            Log.d(LOG_TAG, "VScrolling to: " + view.getLeft());
        }
    }


    // --------------------------------- Private Methods -------------------------------------------
    private void showKeyboard(View view) {
        Log.d(LOG_TAG,"Show keyboard called");
        // Method to show the keyboard
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, inputManager.SHOW_IMPLICIT);
    }


/*//----------------------------- DELETE THESE -----------------------------------------------------
    public void addCellView(CellView cellView){
        addCellView(cellView.getCell().getRow(), cellView.getCell().getColumn(), cellView);
    }

    private int getCellId(int row, int col) {
        // Return cell ID from row and column
        return col + (row * mGridSize);
    }
    //*/


}
