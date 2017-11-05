package com.thonners.crosswordmaker;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author M Thomas
 * @since 31/07/17
 */

public class Crossword2 implements Cell2.CellInteractionListener {

    private int rowCount ;
    private Cell2[][] cells;
    private Collection<Clue2> hClues = new ArrayList<>() ;
    private Collection<Clue2> vClues = new ArrayList<>() ;

    private Clue2.Orientation activeClueOrientation = Clue2.Orientation.HORIZONTAL;

    private CrosswordInteractionListener listener = null ;

    public Crossword2(CrosswordInteractionListener listener, int rowCount) {
        this.listener = listener;
        this.rowCount = rowCount;
    }

    /**
     * Gets the next cell in the clue to request focus. If the current cell is the last cell in
     * a clue, collapses the keyboard. Always checks whether the clue is complete.
     * @param currentRow The current cell's row index
     * @param currentCol The current cell's column index
     */
    @Override
    public void selectNextCell(int currentRow, int currentCol) {
        // Initialise the indices
        int nextCellRow = currentRow;
        int nextCellCol = currentCol;

        // Increment the next cell based on which clue is active
        if (activeClueOrientation == Clue2.Orientation.HORIZONTAL) {
            nextCellRow++;
        } else {
            nextCellCol++;
        }
        // Assuming that the next cell is still within the confines of the grid, and not a black cell
        if (nextCellRow < rowCount && nextCellCol < rowCount && !cells[nextCellRow][nextCellCol].isBlackCell()) {
            // Set it to focused
            cells[nextCellRow][nextCellCol].takeFocus() ;
        } else {
            listener.collapseKeyboard();
        }
        // Check whether the clue's complete
        checkClueComplete() ;
    }

    /**
     * Gets the previous cell in the clue to request focus. If the current cell is the first cell in
     * a clue, collapses the keyboard. Always checks whether the clue is complete.
     * @param currentRow The current cell's row index
     * @param currentCol The current cell's column index
     */
    @Override
    public void selectPreviousCell(int currentRow, int currentCol) {
        // Initialise the indices
        int nextCellRow = currentRow;
        int nextCellCol = currentCol;

        // Increment the next cell based on which clue is active
        if (activeClueOrientation == Clue2.Orientation.HORIZONTAL) {
            nextCellRow--;
        } else {
            nextCellCol--;
        }
        // Assuming that the next cell is still within the confines of the grid, and not a black cell
        if (nextCellRow >= 0 && nextCellCol >= 0 && !cells[nextCellRow][nextCellCol].isBlackCell()) {
            // Set it to focused
            cells[nextCellRow][nextCellCol].takeFocus() ;
        } else {
            listener.collapseKeyboard();
        }
        // Check whether the clue's complete
        checkClueComplete() ;
    }

    /**
     * Sets the colour of the rotationally opposite cell, for during grid creation
     * @param currentRow The current cell's row index
     * @param currentCol The current cell's column index
     * @param setBlack Set the colour of the cell black of white
     */
    @Override
    public void setOppositeCellColour(int currentRow, int currentCol, boolean setBlack) {

        int oppositeRow = (rowCount - 1) - currentRow;
        int oppositeCol = (rowCount - 1) - currentCol;

        if (oppositeCol == currentCol && oppositeRow == currentRow) {
            // Don't toggle if central cell
        } else {
            cells[oppositeRow][oppositeCol].toggleBlackCell();
        }
    }

    private void checkClueComplete() {
        // TODO: Check the clue's complete

        // TODO: Check whether the crossword is complete
    }

    public interface CrosswordInteractionListener {
        void collapseKeyboard() ;
    }

}
