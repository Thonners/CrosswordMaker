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

    private static final String LOG_TAG = "Crossword" ;

    public Context context;

    public String cwdTitle = "Default title";   // Add the date to the default title?

    public int rowCount;
    public int totalCells;

    private int padding = 2 ;

    private int screenWidth ;
    private int screenHeight ;
    private int cellWidth ;
    private int borderWidth = 1 ;   // This is the width of the cell_white around the cells. (Needs to be accounted for in cell width calculation else final column is too thin
    private int gridPadding ;

    private float fontSize ;

    private ArrayList<Clue> hClues = new ArrayList<Clue>() ;     // Storage for all the horizontal clues
    private ArrayList<Clue> vClues = new ArrayList<Clue>() ;     // Storage for all the vertical clues
    private int totalClueCount = 0;
    private int hClueCount = 0 ;
    private int vClueCount = 0 ;
    public int horizontalClueIndex = -1;    // Start at -1, first clue found will bump this up to 0
    public int verticalClueIndex = -1;

    private Cell focusedCell ;
    private Clue highlightedClue ;

    public boolean[][] blackCells;

    public Cell[][] cells;
    public CellView[][] cellViews;

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
        cellViews = new CellView[rowCount][rowCount];

        // TODO: Make each row in a separate parallel thread

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {

                // Create the cell
//                cells[i][j] = new Cell(context, this, i, j);
                cellViews[i][j] = new CellView(context, this, i, j);
                cells[i][j] = cellViews[i][j].getCell();
                cells[i][j].setId(cells[i][j].getCellId(rowCount));
                cells[i][j].setWidth(cellWidth);
                cells[i][j].setHeight(cellWidth);
                cells[i][j].setAllCaps(true);
                cells[i][j].setTextColor(context.getResources().getColor(R.color.black));
                cells[i][j].setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);

                GridLayout.Spec row = GridLayout.spec(i);
                GridLayout.Spec col = GridLayout.spec(j);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(row, col);
                grid.addView(cellViews[i][j], params);

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
        for (int col = 0 ; col < rowCount ; col++) {
            findVerticalClues(col);
        }
    }

    private void findHorizontalClues(int row) {
        // Method to find all clues in row

        boolean nextWhiteCellNewClue = true ;
        for (int col = 0 ; col < rowCount ; col++) {  // only go as far as rowCount-1 because there cannot be a horizontal clue in the final column.

            // Test to see if next cell is a new clue - if next cell is
            if (col < rowCount -1 && nextWhiteCellNewClue && !cells[row][col].isBlackCell() && !cells[row][col + 1].isBlackCell() ){
                hClueCount++;
                totalClueCount++;
                horizontalClueIndex++ ;
                nextWhiteCellNewClue = false ;
                Clue newClue = new Clue(Clue.HORIZONTAL_CLUE, cells[row][col], hClueCount);
                hClues.add(newClue);                                            // Add clue to collection of clues
                hClues.get(horizontalClueIndex).addCellToClue(cells[row][col]);     // Add cell to list of cells in clue
                cells[row][col].setHClue(hClues.get(horizontalClueIndex));          // Tell the cell which clue it belongs to

                Log.d("Clues", "New clue found at: r = " + row + " & c = " + col) ;
                Log.d("Clues", "horizontalClueIndex = " + horizontalClueIndex + " & hClues.length = " + hClues.size()) ;

            } else if (! nextWhiteCellNewClue && ! cells[row][col].isBlackCell()) {
            // If white cell, augment clue count. If not, reset nextWhiteCellNewClue
                int clueLength = col - hClues.get(horizontalClueIndex).getStartCell().getColumn() + 1 ;
                hClues.get(horizontalClueIndex).setLength(clueLength);
                hClues.get(horizontalClueIndex).addCellToClue(cells[row][col]);     // Add cell to list of cells in clue
                cells[row][col].setHClue(hClues.get(horizontalClueIndex));          // Tell the cell which clue it belongs to

                Log.d("Clues", "increasing clue length to " + clueLength) ;
                Log.d("Clues", "horizontalClueIndex = " + horizontalClueIndex + " & hClues.length = " + hClues.size()) ;
            }

            // If a black cell is found, reset nextWhiteCellNewClue flag
            if (cells[row][col].isBlackCell()) {
                nextWhiteCellNewClue = true ;
            }


        }

        // Print cells in first clue to check for duplicates
        Log.d(LOG_TAG, "First Horizontal Clue Contains cells " + hClues.get(0).getCells());

    }

    private void findVerticalClues(int col) {
        // Method to find all clues in col

        boolean nextWhiteCellNewClue = true ;
        for (int row = 0 ; row < rowCount ; row++) {

            // only go as far as rowCount-1 because there cannot be a horizontal clue starting in the final column.
            // Test to see if next cell is a new clue - if next cell is, add to clue collections
            if (row < rowCount -1 && nextWhiteCellNewClue && !cells[row][col].isBlackCell() && !cells[row + 1][col].isBlackCell() ){
                vClueCount++;
                totalClueCount++;
                verticalClueIndex++ ;
                nextWhiteCellNewClue = false ;
                Clue newClue = new Clue(Clue.VERTICAL_CLUE, cells[row][col], vClueCount);
                vClues.add(newClue);                                            // Add clue to collection of clues
                vClues.get(verticalClueIndex).addCellToClue(cells[row][col]);     // Add cell to list of cells in clue
                cells[row][col].setVClue(vClues.get(verticalClueIndex));          // Tell the cell which clue it belongs to

                Log.d("Clues", "New clue found at: r = " + row + " & c = " + col) ;
                Log.d("Clues", "verticalClueIndex = " + verticalClueIndex + " & vClues.length = " + vClues.size()) ;

            } else if (! nextWhiteCellNewClue && ! cells[row][col].isBlackCell()) {
            // If white cell, augment clue count. If not, reset nextWhiteCellNewClue
                int clueLength = col - vClues.get(verticalClueIndex).getStartCell().getColumn() + 1 ;
                vClues.get(verticalClueIndex).setLength(clueLength);
                vClues.get(verticalClueIndex).addCellToClue(cells[row][col]);     // Add cell to list of cells in clue
                cells[row][col].setVClue(vClues.get(verticalClueIndex));          // Tell the cell which clue it belongs to

                Log.d("Clues", "increasing clue length to " + clueLength) ;
                Log.d("Clues", "verticalClueIndex = " + verticalClueIndex + " & vClues.length = " + vClues.size()) ;
            }

            // If a black cell is found, reset nextWhiteCellNewClue flag
            if (cells[row][col].isBlackCell()) {
                nextWhiteCellNewClue = true ;
            }
        }

        // Print cells in first clue to check for duplicates
        Log.d(LOG_TAG, "First Vertical Clue Contains cells " + vClues.get(0).getCells());
    }

    public void clearCellHighlights() {
        // Set all (non black) cells back to white backgrounds
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                if (!cells[i][j].isBlackCell()) {
                    cells[i][j].clearHighlighting();
                }
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