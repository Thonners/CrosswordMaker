package com.thonners.crosswordmaker;

import android.content.Context;

import java.util.ArrayList;

/** Class to hold all the details of a clue
 *
 * @author M Thomas
 * @since 13/12/14
 */
public class Clue {

    public final static String HORIZONTAL_CLUE = "horizontal" ;
    public final static String VERTICAL_CLUE = "vertical" ;

    private static final String LOG_TAG = "Clue" ;

    private String orientation;
    private Cell startCell ;
    private int length ;
    private ArrayList<Cell> clueCells = new ArrayList<Cell>();
    private int clueID;
    private int clueDisplayNumber ;

    private boolean isHighlighted ;
    private boolean isCompleted = false ;

    private ClueChecklistEntryTextView checklistEntryTextView = null;

    //public Clue (String clueOrientation , Cell startCell, OnClueInteractionListener crosswordPageFragment) {
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
        clueCells.add(newCell);
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
    /**
     * @return The unique ID number of the clue
     */
    public int getClueID() {
        return clueID;
    }

    /**
     * Method to highlight the cells in the clue which contains the focus cell.
     * Sets the focus of the     * non focusCell cells to focusedMinor, whilst setting the focusCell to focusedMajor.
     * @param focusCell The cell in the clue that is to have the major focus - i.e. the active cell
     */
    public void highlightClue(Cell focusCell) {
        // Highlight the cells in the clue
        setIsHighlighted();
        for (Cell cell : clueCells ) {
            cell.setActiveClue(this);
            if (cell.equals(focusCell)) {
                cell.setFocusedMajor();
            } else {
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

    /**
     * Method to highlight the next cell in a clue, or, if the current cell is the final cell in the
     * clue, set the clue as complete.
     *
     * Checks whether there is a next cell to highlight, and if so, moves the highlight to that cell.
     * If not, marks the clue as complete.
     *
     * @param currentCell The cell which is currently active, and has just been populated by the user.
     */
    public void highlightNextCell(Cell currentCell) {
        // Check that it isn't the last cell in the clue
        if (clueCells.indexOf(currentCell) < clueCells.size() - 1) {
            //highlight clue using cell from next in index. Should always be next one due to direction
            // in which cells are searched for and added to clues in findHorizontal/VerticalClues
            this.highlightClue(clueCells.get(clueCells.indexOf(currentCell) + 1));
        }
    }

    /**
     * Method to check that all cells are filled and then set the clue as complete
     */
    public void checkClueComplete() {
        // Set isCompleted to true now, and set it to false if any of the cells aren't actually complete
        isCompleted = true ;
        // Loop through and check all cells populated
        for (Cell cell : clueCells) {
            // Check if a cell is empty, and if so, set isCompleted to false
            if (cell.isEmpty()) {
                isCompleted = false ;
            }
        }

        // If still completed, cross it off the list
        if (isCompleted) {
            checklistEntryTextView.setChecked(true) ;
        } else {
            checklistEntryTextView.setChecked(false) ;
        }
    }
    public String getCells() {
        String cellList = "";
        for (Cell cell : clueCells) {
            cellList = cellList + cell.getCellName() + " ; " ;
        }
        return  cellList ;
    }

    /**
     * Method to return
     * @return The checklistEntryTextView associated with this clue
     */
    public ClueChecklistEntryTextView getChecklistEntryTextView(Context context) {
        if (checklistEntryTextView == null) {
            checklistEntryTextView = new ClueChecklistEntryTextView(context, this) ;
        }

        return checklistEntryTextView ;
    }


}
