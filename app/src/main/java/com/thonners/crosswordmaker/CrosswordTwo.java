package com.thonners.crosswordmaker;

import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class CrosswordTwo {

    private final String LOG_TAG = "CrosswordTwo";

    private String title, date;
    private final CellTwo[][] cells;

    private ClueTwo[] hClues, vClues;

    private CellTwo highlightedCell;
    private ClueTwo highlightedClue;
    public final int rowCount;
    public final int colCount;
    public boolean editGridMode;

    private final boolean isRotationallySymmetric;

    public static CrosswordTwo fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CrosswordTwo.class);
    }

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

    public void findClues() {
        ArrayList<ClueTwo> hCluesTmp = new ArrayList<>();
        ArrayList<ClueTwo> vCluesTmp = new ArrayList<>();
        ArrayList<CellTwo> hClueCells = new ArrayList<>();
        ArrayList<CellTwo> vClueCells = new ArrayList<>();
        CellTwo currentCell;
        int hClueNo = 0, vClueNo = 0;
        boolean nextCellIsHClue;
        boolean nextCellIsVClue;
        for (int row = 0; row < this.rowCount; row++) {
            nextCellIsHClue = true;
            for (int col = 0; col < this.rowCount; col++) {
                currentCell = this.getCell(row, col);
                if (currentCell.getIsBlackCell()) {
                    if (nextCellIsHClue) {
                        // Then we're in the middle of a line of black cells, so do nothing...
                    } else {
                        // First black cell since a line of cells, so save the last active clue
                        ClueTwo newHClue = new ClueTwo(hClueCells.toArray(
                                new CellTwo[hClueCells.size()]),
                                ClueTwo.Orientation.HORIZONTAL, hClueNo);
                        hCluesTmp.add(newHClue);
                        nextCellIsHClue = true;
                    }
                } else {
                    // Check it's :
                    // - the first white cell after black cells
                    // - not the final col
                    // - Next cell is also not a black cell (e.g. if this cell is only in v clue)
                    if (nextCellIsHClue) {
                        if (col < (colCount - 1) && !getCell(row, col + 1).getIsBlackCell()) {
                            hClueNo++;
                            // Then this is the first cell in the next clue!
                            nextCellIsHClue = false;
                            // Create a new array list to which we can add the cells
                            hClueCells = new ArrayList<>();
                            // Put the relevant cell in the clue!
                            hClueCells.add(currentCell);
                            Log.d(LOG_TAG, "First cell of a horz clue: (" + row + ", " + col + ")");
                        }
                    } else {
                        // Next white cell in the line of white cells, so add it to the list
                        hClueCells.add(currentCell);
                    }
                }
            }
        }
        // Add the last one in the case of no black cell at the end of the row
        // TODO: handle the case that it has already been added...
        ClueTwo newHClue = new ClueTwo(hClueCells.toArray(
                new CellTwo[hClueCells.size()]), ClueTwo.Orientation.HORIZONTAL, hClueNo);
        hCluesTmp.add(newHClue);
        for (int col = 0; col < this.rowCount; col++) {
            nextCellIsVClue = true;
            for (int row = 0; row < this.rowCount; row++) {
                currentCell = this.getCell(row, col);
                if (currentCell.getIsBlackCell()) {
                    if (nextCellIsVClue) {
                        // Then we're in the middle of a line of black cells, so do nothing...
                    } else {
                        // First black cell since a line of cells, so save the last active clue
                        ClueTwo newVClue = new ClueTwo(vClueCells.toArray(
                                new CellTwo[vClueCells.size()]),
                                ClueTwo.Orientation.VERTICAL, vClueNo);
                        vCluesTmp.add(newVClue);
                        nextCellIsVClue = true;
                    }
                } else {
                    if (nextCellIsVClue) {
                        if (row < (rowCount - 1) && !getCell(row + 1, col).getIsBlackCell()) {
                            // Then this is the first cell in the next clue!
                            vClueNo++;
                            nextCellIsVClue = false;
                            // Create a new array list to which we can add the cells
                            vClueCells = new ArrayList<>();
                            // Put the relevant cell in the clue!
                            vClueCells.add(currentCell);
                            Log.d(LOG_TAG, "First cell of a vert clue: (" + row + ", " + col + ")");
                        }
                    } else {
                        // Next white cell in the line of white cells, so add it to the list
                        vClueCells.add(currentCell);
                    }
                }
            }
        }
        ClueTwo newVClue = new ClueTwo(vClueCells.toArray(
                new CellTwo[vClueCells.size()]), ClueTwo.Orientation.VERTICAL, vClueNo);
        vCluesTmp.add(newVClue);
        this.hClues = hCluesTmp.toArray(new ClueTwo[0]);
        this.vClues = vCluesTmp.toArray(new ClueTwo[vCluesTmp.size()]);
        Log.d(LOG_TAG, "Clues found: hClues: " + hClueNo + ", vClues: " + vClueNo);
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

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void cellTouched(int row, int col) {
        if (editGridMode) {
            if (0 <= row && row < rowCount && 0 <= col && col < colCount) {
                toggleBlackCell(row, col);
            } else {
                Log.d(LOG_TAG, "Touch detected outside the active grid, so ignoring it.");
            }
        } else {
            Log.e(LOG_TAG, "Need to program what happens when a click happens but not editing");
        }
    }
}
