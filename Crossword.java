package com.thonners.crosswordmaker;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.widget.GridLayout;

import java.util.ArrayList;

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
    private int borderWidth = 1 ;   // This is the width of the border around the cells. (Needs to be accounted for in cell width calculation else final column is too thin
    private int gridPadding ;

    private float fontSize ;

    private ArrayList<Clue> hClues = new ArrayList<Clue>() ;     // Storage for all the horizontal clues
    private ArrayList<Clue> vClues = new ArrayList<Clue>() ;     // Storage for all the vertical clues
    public int clueCount = 0;
    public int horizontalClueIndex = -1;    // Start at -1, first clue found will bump this up to 0
    public int verticalClueCount = 0;

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
        //Generate all the cells in the crossword grid

        // Initialise the cells variable to the right size
        cells = new Cell[rowCount][rowCount];

        // TODO: Make each row in a separate parallel thread

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {

                // Create the cell
                cells[i][j] = new Cell(context, i, j);
                cells[i][j].setId(cells[i][j].getCellId(rowCount));
                cells[i][j].setWidth(cellWidth);
                cells[i][j].setHeight(cellWidth);
                cells[i][j].setAllCaps(true);
                cells[i][j].setTextColor(context.getResources().getColor(R.color.black));
                cells[i][j].setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);

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

    public void findClues() {
        // Locate clues by cycling through grid and finding all clues and their numbered square
        // TODO: make horizontal and vertical clue finding happen on different threads

        for (int row = 0 ; row < rowCount ; row++) {
            findHorizontalClues(row);
        }
    }

    private void findHorizontalClues(int row) {
        // Method to find all clues in row
        boolean nextWhiteCellNewClue = true ;
        for (int col = 0 ; col < rowCount ; col++) {  // only go as far as rowCount-1 because there cannot be a horizontal clue in the final column.

            // Test to see if next cell is a new clue - if next cell is
            if (nextWhiteCellNewClue && ! cells[row][col].blackCell && !cells[row][col + 1].blackCell && col < rowCount -1){
                horizontalClueIndex++ ;
                nextWhiteCellNewClue = false ;
                Clue newClue = new Clue(Clue.HORIZONTAL_CLUE, cells[row][col]);
                hClues.add(newClue);

                Log.d("Clues", "New clue found at: r = " + row + " & c = " + col) ;
            }

            // If white cell, augment clue count. If not, reset nextWhiteCellNewClue
            if (! nextWhiteCellNewClue && ! cells[row][col].blackCell) {
                int clueLength = col - hClues.get(horizontalClueIndex).startCell.column + 1 ;
                hClues.get(horizontalClueIndex).setLength(clueLength);

                Log.d("Clues", "increasing clue length to " + clueLength) ;
            }

            // If a black cell is found, reset nextWhiteCellNewClue flag
            if (cells[row][col].blackCell) {
                nextWhiteCellNewClue = true ;
            }


        }
    }

    private void calculateCellWidth() {
        // Use screen width to calculate cell size.
        // Make cell size (screenWidth / (rowCount + 1)). This allows half a cellWidth on each side to keep the borders nice
        cellWidth = (int) screenWidth / (rowCount + 1) - (borderWidth * 2) ;
        gridPadding = (int) cellWidth / 2 ;
        fontSize = (float) cellWidth / 2 ;

        Log.d("Sizes","cellWidth = " + cellWidth);
        Log.d("Sizes","fontSize = " + fontSize);
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