package com.thonners.crosswordmaker;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by mat on 28/12/14.
 */
public class CellView extends RelativeLayout implements Serializable {

    private static final String LOG_TAG = "CellView";

    private Cell cell ;
    private int clueNumber ;
    private TextView clueNumberDisplay = null;
    private HashMap<Cell.CellSide, View> wordSplitViews = new HashMap<>();
    private RelativeLayout.LayoutParams cellLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) ;
    private RelativeLayout.LayoutParams cellNoLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) ;

    public CellView(Context context, Crossword crossword, int r, int c) {
        super(context);
        cell = new Cell(getContext(), crossword, r, c, this) ;
        
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

    public void addHyphen(Cell.CellSide side) {
        Log.d(LOG_TAG,"Adding hyphen to " + side + " of cellView: " + cell.getCellName());
        removeWordSplitOrHyphen(side);
        int hyphenLength = getResources().getDimensionPixelOffset(R.dimen.cell_hyphen_length) ; //cellWidth / (int) getResources().getDimension(R.dimen.cell_hyphen_length_fraction);
        int hyphenWidth = getResources().getDimensionPixelOffset(R.dimen.cell_hyphen_thickness) ;
        View trailingHyphen = new View(getContext()) ;
        trailingHyphen.setBackgroundColor(getResources().getColor(R.color.black));
        RelativeLayout.LayoutParams trailingHyphenLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switch (side) {
            case TOP:
                trailingHyphenLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
                trailingHyphenLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                trailingHyphenLP.height = hyphenLength ;
                trailingHyphenLP.width = hyphenWidth ;
                break;
            case BOTTOM:
                trailingHyphenLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
                trailingHyphenLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                trailingHyphenLP.height = hyphenLength ;
                trailingHyphenLP.width = hyphenWidth ;
                break;
            case LEFT:
                trailingHyphenLP.addRule(RelativeLayout.CENTER_VERTICAL);
                trailingHyphenLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                trailingHyphenLP.height = hyphenWidth ;
                trailingHyphenLP.width = hyphenLength ;
                break;
            case RIGHT:
                trailingHyphenLP.addRule(RelativeLayout.CENTER_VERTICAL);
                trailingHyphenLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                trailingHyphenLP.height = hyphenWidth ;
                trailingHyphenLP.width = hyphenLength ;
                break;
        }
        this.addView(trailingHyphen, trailingHyphenLP);
        wordSplitViews.put(side,trailingHyphen);
        Log.d(LOG_TAG,"Added hyphen to " + side + " of cellView: " + cell.getCellName());
    }

    public void addWordSplit(Cell.CellSide side, int cellWidth) {
        Log.d(LOG_TAG,"Adding word split to " + side + " of cellView: " + cell.getCellName());
        removeWordSplitOrHyphen(side);
        int dividerThickness = getResources().getDimensionPixelOffset(R.dimen.cell_word_split_thickness) ;
        int cellSize = cellWidth;
        View trailingWordSplit = new View(getContext()) ;
        trailingWordSplit.setBackgroundColor(getResources().getColor(R.color.black));
        RelativeLayout.LayoutParams trailingWordSplitLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        switch (side) {
            case TOP:
                trailingWordSplitLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
                trailingWordSplitLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                trailingWordSplitLP.height = dividerThickness ;
                trailingWordSplitLP.width = cellSize ;
                break;
            case BOTTOM:
                trailingWordSplitLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
                trailingWordSplitLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                trailingWordSplitLP.height = dividerThickness ;
                trailingWordSplitLP.width = cellSize ;
                break;
            case LEFT:
                trailingWordSplitLP.addRule(RelativeLayout.CENTER_VERTICAL);
                trailingWordSplitLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                trailingWordSplitLP.height = cellSize ;
                trailingWordSplitLP.width = dividerThickness ;
                break;
            case RIGHT:
                trailingWordSplitLP.addRule(RelativeLayout.CENTER_VERTICAL);
                trailingWordSplitLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                trailingWordSplitLP.height = cellSize ;
                trailingWordSplitLP.width = dividerThickness ;
                break;
        }
        this.addView(trailingWordSplit, trailingWordSplitLP);
        trailingWordSplit.bringToFront();
        wordSplitViews.put(side,trailingWordSplit);
        Log.d(LOG_TAG,"Added Word|Split to " + side + " of cellView: " + cell.getCellName() + ", Width = " + trailingWordSplitLP.width + ", Height = " + trailingWordSplitLP.height);
    }

    public void removeWordSplitOrHyphen(Cell.CellSide side) {
        if (wordSplitViews.containsKey(side)) {
            Log.d(LOG_TAG,"Removing previous word separator from " + side + " side of cell");
            this.removeView(wordSplitViews.get(side));
        }
    }
}
