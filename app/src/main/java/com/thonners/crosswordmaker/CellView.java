package com.thonners.crosswordmaker;

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by mat on 28/12/14.
 */
public class CellView extends RelativeLayout implements Serializable {

    private Cell cell ;
    private int clueNumber ;
    private TextView clueNumberDisplay = null;
    private RelativeLayout.LayoutParams cellLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) ;
    private RelativeLayout.LayoutParams cellNoLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) ;

    public CellView(Context context, Crossword crossword, int r, int c) {
        super(context);
        cell = new Cell(getContext(), crossword, r, c) ;
        
        initialiseLayoutParams();
        
        this.addView(cell, 0, cellLP);
    }
    
    private void initialiseLayoutParams() {
        // Make both align top left to have it overlay the number
        cellLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        cellLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cellNoLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        cellNoLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    }

    public void setClueNumber(int clueNo) {
        this.clueNumber = clueNo ;
        createClueNumber();
    }

    private void createClueNumber() {
        clueNumberDisplay = new TextView(getContext()) ;
        clueNumberDisplay.setText("" + clueNumber);
        clueNumberDisplay.setPadding(5,0,0,0);
        clueNumberDisplay.setTextColor(getContext().getResources().getColor(R.color.dark_grey));
        float clueNumberTextSize = (float) (cell.getTextSize() * 0.5) ;
        clueNumberDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, clueNumberTextSize);

        this.addView(clueNumberDisplay, 1, cellNoLP);
        clueNumberDisplay.bringToFront();
    }

    public void setSize(int cellSize) {
        // Set the size of the cell view, and the text sizes of the textview and clue number
        // Calculate font sizes
        float mainTextSize = (float) cellSize / 2 ;
        float clueTextSize = mainTextSize / 2 ;

        // Set parameters
        cell.setWidth(cellSize);
        cell.setHeight(cellSize);
        cell.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainTextSize);
        if (clueNumberDisplay != null) {
            // Check not null, as will be the case the first time the CellView is created.
            clueNumberDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, clueTextSize);
        }
    }

    public Cell getCell() {
        return this.cell ;
    }
}
