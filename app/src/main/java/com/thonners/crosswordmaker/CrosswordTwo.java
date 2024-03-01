package com.thonners.crosswordmaker;

import android.util.Log;

public class CrosswordTwo {

    private static final String LOG_TAG = "CrosswordTwo";

    private CellTwo[][] cells;
    private ClueTwo[] hClues, vClues;

    private CellTwo highlightedCell;
    private ClueTwo highlightedClue;
    public final int rowCount;
    public final int colCount;
    public boolean editGridMode;

    private boolean isRotationallySymmetric;

    /**
     * Constructor for when a new square Crossword is being created
     *
     * @param rowCount
     */
    public CrosswordTwo(int rowCount) {
        this(rowCount, rowCount);
    }

    /**
     * Constructor for when a new, rotationally symmetric Crossword is being created
     *
     * @param rowCount
     * @param colCount
     */
    public CrosswordTwo(int rowCount, int colCount) {
        this(rowCount, colCount, true);
    }

    /**
     * Constructor for when a new Crossword is being created
     *
     * @param rowCount
     * @param colCount
     */
    public CrosswordTwo(int rowCount, int colCount, boolean isRotationallySymmetric) {
        editGridMode = true;
        // Assume symmetric for now, add a constructor when supporting non-symmetric grids
        this.isRotationallySymmetric = isRotationallySymmetric;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.cells = new CellTwo[rowCount][colCount];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                this.cells[r][c] = new CellTwo();
            }
        }
    }

    public CellTwo getCell(int row, int col) {
        return this.cells[row][col];
    }

    /**
     * If in edit mode, toggles whether the cell is black.
     * If the grid is rotationally symmetric, toggles the 'opposite' cell to match too.
     *
     * @param row
     * @param col
     */
    public void toggleBlackCell(int row, int col) {
        if (this.editGridMode) {
            CellTwo cellToToggle = cells[row][col];
            cellToToggle.toggleIsBlackCell();
            if (this.isRotationallySymmetric) {
                cells[rowCount - row - 1][colCount - col - 1].setIsBlackCell(cellToToggle.getIsBlackCell());
            }
        } else {
            Log.e(LOG_TAG, "ToggleBlackCell called when the Crossword isn't in edit mode?");
        }
    }
}
