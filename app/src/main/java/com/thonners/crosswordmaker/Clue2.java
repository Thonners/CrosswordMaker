package com.thonners.crosswordmaker;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class to hold all the details of a clue
 *
 * Refactored version to clean up original code
 *
 * @author M Thomas
 * @since 01/08/17
 */

public class Clue2 {

    private Orientation orientation ;
    private Collection<Cell2> clueCells = new ArrayList<>();
    private int clueDisplayNumber ;
    private int clueID;

    public Clue2(Orientation orientation) {
        this.orientation = orientation;
    }

    public enum Orientation{
        HORIZONTAL, VERTICAL
    }

    public Clue2(Orientation orientation, Collection<Cell2> clueCells) {
        this.orientation = orientation;
        this.clueCells = clueCells;
    }


    /**
     * Method to set the display number, i.e. the number of the clue in its across / down capacity
     * @param clueDisplayNumber
     */
    public void setClueDisplayNumber(int clueDisplayNumber) {
        this.clueDisplayNumber = clueDisplayNumber ;
    }

    /**
     * @return The number of this clue, as defined by the crossword
     */
    public int getClueDisplayNumber() {
        return clueDisplayNumber ;
    }
    /**
     * @param clueID The unique ID number of the clue
     */
    public void setClueID(int clueID){
        // Set the clue ID
        this.clueID = clueID ;
    }
}
