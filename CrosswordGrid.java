package com.thonners.crosswordmaker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 *  Relative layout to house crossword
 *  Use this instead of a GridView, as GridView is intended for other stuff
 *
 * Created by Thonners on 09/10/15.
 */
public class CrosswordGrid extends RelativeLayout {

    private int mGridSize ;
    private Crossword mCrossword ;

    public CrosswordGrid(Context context, int gridSize) {
        super(context);
        mGridSize = gridSize ;
    }

    // Default Constructors
    public CrosswordGrid(Context context){
        super(context);
    }
    public CrosswordGrid(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public CrosswordGrid(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs,defStyle);
    }

    public void setCrossword(Crossword crossword){
        mCrossword = crossword;
    }
    public void setGridSize(int gridSize){
        mGridSize = gridSize ;
    }
    public void addCellView(int row, int col, CellView cellView){
        // Assign ID for use in relative layout
        //cellView.setId(getCellId(row,col));
        cellView.setId(View.generateViewId());
        // Create layout params
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(row == 0) {
            lp.addRule(ALIGN_PARENT_TOP);
        } else {
            //lp.addRule(RelativeLayout.BELOW,getCellId(row-1,col));
            lp.addRule(RelativeLayout.BELOW,mCrossword.getCellView(row-1,col).getId());
        }
        if (col == 0) {
            lp.addRule(ALIGN_PARENT_LEFT);
        } else {
            lp.addRule(RelativeLayout.RIGHT_OF,mCrossword.getCellView(row,col -1).getId());
            //lp.addRule(RelativeLayout.RIGHT_OF,getCellId(row,col - 1));
        }
        //
        this.addView(cellView,lp);
    }
    public void addCellView(CellView cellView){
        addCellView(cellView.getCell().getRow(), cellView.getCell().getColumn(), cellView);
    }

    private int getCellId(int row, int col) {
        // Return cell ID from row and column
        return col + (row * mGridSize);
    }

}
