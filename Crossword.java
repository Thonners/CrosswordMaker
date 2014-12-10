package com.thonners.crosswordmaker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.text.Layout;
import android.view.Display;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.GridLayout.Spec;
import android.widget.Space;

/**
 * Created by mat on 28/11/14.
 */
public class Crossword {

    public Context context;

    public String cwdTitle = "Default title";   // Add the date to the default title?

    public int rowCount;
    public int totalCells;

    private int padding = 2 ;

    private int screenWidth ;
    private int screenHeight ;
    private int cellWidth ;
    private int gridPadding ;

    private float fontSize ;

    public int clueCount;

    public boolean[][] blackCells;

    public Cell[][] cells;

    GridLayout grid;

    public Crossword(Context context, int rows, GridLayout gridLayout, int screenWidth, int screenHeight) {
        this.context = context;
        rowCount = rows;
        grid = gridLayout;
        this.screenWidth = screenWidth ;
        this.screenHeight = screenHeight ;

        calculateCellWidth() ;
        createGrid();
        createCells();

    }

    private void createGrid() {
        grid.setRowCount(rowCount);
        grid.setColumnCount(rowCount);
    }

    private void createCells() {
        cells = new Cell[rowCount][rowCount];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {

                            // Create the cell
                cells[i][j] = new Cell(context, i, j);
                cells[i][j].setWidth(cellWidth);
                cells[i][j].setHeight(cellWidth);
                cells[i][j].setAllCaps(true);
                cells[i][j].setTextColor(context.getResources().getColor(R.color.black));
                cells[i][j].setTextSize(fontSize);

                GridLayout.Spec row = GridLayout.spec(i);
                GridLayout.Spec col = GridLayout.spec(j);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(row, col);
                grid.addView(cells[i][j], params);

            }
        }
    }

    private void setAllCellsWhite() {
        blackCells = new boolean[rowCount][rowCount];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                blackCells[i][j] = false;
            }
        }

    }

    private int getTotalCells(int rowCountIn) {
        return rowCountIn * rowCountIn;
    }

    private void calculateCellWidth() {
        // Use screen width to calculate cell size.
        // Make cell size (screenWidth / (rowCount + 1)). This allows half a cellWidth on each side to keep the borders nice
        cellWidth = (int) screenWidth / (rowCount + 1);
        gridPadding = (int) cellWidth / 2 ;
        fontSize = (float) cellWidth / 2 ;
    }

    public void freezeGrid() {
        // Locks the grid so that black cells stay black and white cells stay white
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                cells[i][j].setGridMakingPhase(false);
                cells[i][j].setWhiteCellsEditable();
            }
        }
    }
}