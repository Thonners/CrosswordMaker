package com.thonners.crosswordmaker;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/** Class to hold all the details of different clues
 * Created by mat on 13/12/14.
 */
public class Clue {

    public final static String HORIZONTAL_CLUE = "horizontal" ;
    public final static String VERTICAL_CLUE = "vertical" ;

    private static final String LOG_TAG = "Clue" ;

    private String orientation;
    private Cell startCell ;
    private int length ;
    private ArrayList<Cell> clueCells = new ArrayList<Cell>();
    private int clueNo ;

    private boolean isHighlighted ;

    public Clue (String clueOrientation , Cell startCell) {
        this.orientation = clueOrientation;
        this.startCell = startCell ;
        this.isHighlighted = false ;
    }

    public void setLength(int l) {
        this.length = l ;
    }

    public int getLength() {
        return length ;
    }

    public void addCellToClue(Cell newCell) {
 //       Log.d(LOG_TAG , "Adding new cell to clue that starts in " + startCell.getRow() + ", "  + startCell.getColumn());
        clueCells.add(newCell);
    }

    public void setClueNo(int clueNumber){
        // Set the clue number
        this.clueNo = clueNumber ;
    }
    public int getClueNo(int clueNumber){
        return this.clueNo ;
    }

    public void highlightClue(Cell focusCell) {
        // Highlight the cells in the clue
        //Log.d(LOG_TAG, "Highlighting clue with start cell " + startCell.getCellName());
        setIsHighlighted();
        for (Cell cell : clueCells ) {
            cell.setActiveClue(this);
            if (cell.equals(focusCell)) {
          //      Log.d(LOG_TAG, "Setting cell " + cell.getCellName() + " to major highlight");
                cell.setFocusedMajor();
            } else {
          //      Log.d(LOG_TAG, "Setting cell " + cell.getCellName() + " to minor highlight");
                cell.setFocusedMinor();
            }
        }
    }

    private void setIsHighlighted() {
        this.isHighlighted = true ;
    }

    private boolean isHighlighted() {
        return isHighlighted ;
    }

    public Cell getStartCell() {
        return startCell ;
    }
    public String getClueOrientation() {
        return orientation;
    }
    public int getClueNo() {
        return clueNo ;
    }
    public void highlightNextCell(Cell currentCell) {
        // Method to highlight the next cell in the clue
//        Log.d(LOG_TAG, "Call to highlightNextCell made from cell: " + currentCell.getCellName());
        if (clueCells.indexOf(currentCell) < clueCells.size() - 1) {
            //highlight clue using cell from next in index. Should always be next one due to direction
            // in which cells are searched for and added to clues in findHorizontal/VerticalClues
            this.highlightClue(clueCells.get(clueCells.indexOf(currentCell) + 1));
        }
    }
    public String getCells() {
        String cellList = "";
        for (Cell cell : clueCells) {
            cellList = cellList + cell.getCellName() + " ; " ;
        }
        return  cellList ;
    }

}