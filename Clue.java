package com.thonners.crosswordmaker;

import android.util.Log;

import java.util.ArrayList;

/** Class to hold all the details of different clues
 * Created by mat on 13/12/14.
 */
public class Clue {

    public final static String HORIZONTAL_CLUE = "horizontal" ;
    public final static String VERTICAL_CLUE = "vertical" ;

    private String logTag = "Clue" ;

    public String type ;
    public Cell startCell ;
    private int length ;
    private ArrayList<Cell> clueCells = new ArrayList<Cell>();

    public Clue (String clueOrientation , Cell startCell) {
        this.type = clueOrientation;
        this.startCell = startCell ;
    }

    public void setLength(int l) {
        this.length = l ;
    }

    public int getLength() {
        return length ;
    }

    public void addCellToClue(Cell newCell) {
        Log.d(logTag , "Adding new cell to clue that starts in " + startCell.row + ", "  + startCell.column);
        clueCells.add(newCell);
    }


}
