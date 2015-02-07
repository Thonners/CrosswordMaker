package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.widget.GridLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mat on 28/11/14.
 */
public class Crossword {

    public static final String CROSSWORD_EXTRA = "com.thonners.crosswordmaker.crossword_extra";
    public static final String CROSSWORD_EXTRA_TITLE = "com.thonners.crosswordmaker.crossword_title";
    public static final String CROSSWORD_EXTRA_DATE = "com.thonners.crosswordmaker.crossword_date";
    public static final String CROSSWORD_EXTRA_NO_ROWS = "com.thonners.crosswordmaker.crossword_no_rows";

    private static final String LOG_TAG = "Crossword" ;

    public static final String SAVE_DATE_FORMAT = "yyyyMMdd";
    public static final String SAVE_CROSSWORD_FILE_NAME = "crossword";
    public static final String SAVE_CLUE_IMAGE_FILE_NAME = "clue.jpg";
    public static final String SAVE_CROSSWORD_IMAGE_FILE_NAME = "image_crossword.jpg";

    public static final int SAVED_ARRAY_INDEX_TITLE = 0 ;
    public static final int SAVED_ARRAY_INDEX_DATE = 1 ;
    public static final int SAVED_ARRAY_INDEX_ROW_COUNT = 2 ;
    public static final int SAVED_ARRAY_INDEX_CELL_WIDTH = 3 ;
    public static final int SAVED_ARRAY_INDEX_CROSSWORD_IMAGE = 4 ; // Not required
    public static final int SAVED_ARRAY_INDEX_CLUE_IMAGE = 5 ;  // Not required
    public static final int SAVE_ARRAY_START_INDEX = 6 ;       // Update this if format of saveArray changes, i.e. if more fields are added before the grid is saved.

    private Context context;

    public String title = "Default title";   // Add the date to the default title?
    public String date ;    // Save date format (yyyyMMdd)          //= getFormattedDate() ;       // Default date

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
    private int totalClueCount = 0;
    private int hClueCount = 0 ;
    private int vClueCount = 0 ;
    public int horizontalClueIndex = -1;    // Start at -1, first clue found will bump this up to 0
    public int verticalClueIndex = -1;

    private Cell focusedCell ;
    private Clue highlightedClue ;

    public boolean[][] blackCells;

    private Cell[][] cells;
    private CellView[][] cellViews;
    private String [] crosswordStringArray ;    // Place to save progress
    private String crosswordImagePath ;
    private String clueImagePath ;

    FileOutputStream fileOutputStream ;
    File saveDir ;
    File crosswordFile ;
    File clueImageFile ;
    File crosswordImageFile ;
    String fileName ;
    String savePath ;

    GridLayout grid;

    public Crossword(Context context, int rows, GridLayout gridLayout, int screenWidth, int screenHeight) {
        this.context = context;
        rowCount = rows;
        grid = gridLayout;
        this.screenWidth = screenWidth ;
        this.screenHeight = screenHeight ;

        calculateCellWidth() ;
        calculateFontSize();
        createGrid();
        createCells();

        initialiseSaveFiles();
    }

    public Crossword(Context context, GridLayout gridLayout, String[] savedCrossword) {
        // Constructor for a saved crossword
        this.context = context ;
        this.grid = gridLayout;
        this.crosswordStringArray = savedCrossword ;
        this.title = crosswordStringArray[SAVED_ARRAY_INDEX_TITLE];
        this.date = crosswordStringArray[SAVED_ARRAY_INDEX_DATE];
        this.rowCount = Integer.parseInt(crosswordStringArray[SAVED_ARRAY_INDEX_ROW_COUNT]);
        this.cellWidth = Integer.parseInt(crosswordStringArray[SAVED_ARRAY_INDEX_CELL_WIDTH]);
        this.crosswordImagePath = crosswordStringArray[SAVED_ARRAY_INDEX_CROSSWORD_IMAGE];
        this.clueImagePath = crosswordStringArray[SAVED_ARRAY_INDEX_CLUE_IMAGE];

        createGrid();
        calculateFontSize();
        createCells();
        fillCells();
        freezeGrid();
        findClues();

        initialiseSaveFiles();
    }

    private void createGrid() {
        grid.setRowCount(rowCount);
        grid.setColumnCount(rowCount);
        grid.setBackground(context.getResources().getDrawable(R.drawable.cell_white));
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

    private void fillCells() {
      // Fill a grid with pre-existing CellViews
        Log.d(LOG_TAG, "Rebuilding grid from saveArray...");
      // TODO: Make each row in a separate parallel thread
        int index = SAVE_ARRAY_START_INDEX;
        String tempString ;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                tempString = crosswordStringArray[index] ;
//                Log.d(LOG_TAG,"For cell " + cells[i][j].getCellName() + ", read value: " + tempString);
                if (tempString.matches("-")) {
//                    Log.d(LOG_TAG,"Value matched '-', so toggling black cell.");
                    cells[i][j].toggleBlackCell();
                } else {
//                    Log.d(LOG_TAG,"Setting value to: " + tempString);
                    cells[i][j].setText(tempString);
                }
                index++;
            }
        }


    }

    private int getTotalCells(int rowCountIn) {
        return rowCountIn * rowCountIn;
    }

    public void setTitle(String newTitle) {
        if(newTitle.length() > 0 ) {
            this.title = newTitle;
        }
        return ;
    }

    public void setDate(String inputDate) {
        // String inputDate to be provided from HomeActivity intent, which should put it into yyyyMMdd.
        this.date = inputDate ;
    }

    public String getDisplayDate() {
        // Method to use for saving the display date. Not sure this is required
        SimpleDateFormat sdf = new SimpleDateFormat(SAVE_DATE_FORMAT);      // Save Formatted date
        DateFormat localeDateFormat = android.text.format.DateFormat.getDateFormat(context);    // Locale date format
        Date dateProper ;

        try {
            dateProper = sdf.parse(date) ;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't parse Crossword.date (should be in save format) into something useful. This is coming from HomeActivity via intents so check the routing!");
            return context.getResources().getString(R.string.error_crossword_date); // Return the error message to be displayed.
        }

        return localeDateFormat.format(dateProper) ;
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
        // Print cells in first clue to check for duplicates. Check size > 0 to prevent it crashing due to IndexOutOfBounds Exception
        if (hClues.size() > 0 ){ Log.d(LOG_TAG, "First Horizontal Clue Contains cells " + hClues.get(0).getCells()); }
        if (vClues.size() > 0 ){ Log.d(LOG_TAG, "First Vertical Clue Contains cells " + vClues.get(0).getCells()); }

        Log.d(LOG_TAG, "");
        Log.d(LOG_TAG, "Getting clue numbers...");
        Log.d(LOG_TAG, "");
        getClueNumbers();
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
                Clue newClue = new Clue(Clue.HORIZONTAL_CLUE, cells[row][col]);
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

 //               Log.d("Clues", "increasing clue length to " + clueLength) ;
 //               Log.d("Clues", "horizontalClueIndex = " + horizontalClueIndex + " & hClues.length = " + hClues.size()) ;
            }

            // If a black cell is found, reset nextWhiteCellNewClue flag
            if (cells[row][col].isBlackCell()) {
                nextWhiteCellNewClue = true ;
            }


        }
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
                Clue newClue = new Clue(Clue.VERTICAL_CLUE, cells[row][col]);
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

    //            Log.d("Clues", "increasing clue length to " + clueLength) ;
    //            Log.d("Clues", "verticalClueIndex = " + verticalClueIndex + " & vClues.length = " + vClues.size()) ;
            }

            // If a black cell is found, reset nextWhiteCellNewClue flag
            if (cells[row][col].isBlackCell()) {
                nextWhiteCellNewClue = true ;
            }
        }
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

        Log.d("Sizes","cellWidth = " + cellWidth);
    }

    private void calculateFontSize() {
        fontSize = (float) cellWidth / 2 ;
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

    public void toggleOppositeBlackCell(Cell inputCell) {
        // Makes use of crossword's rotational symmetry to speed the process of creating your own grid.
        // Applys toggleBlackCell method to 'opposite' cell to that input

        // indices run 0 -> (rowCount - 1), hence use of that term below
        int oppositeRow = (rowCount - 1) - inputCell.getRow();
        int oppositeCol = (rowCount - 1) - inputCell.getColumn();

        if (oppositeCol == inputCell.getColumn() && oppositeRow == inputCell.getRow()) {
            Log.d(LOG_TAG,"Not toggling opposite cell as in this instance it is the same cell!") ;
        } else {
 //           Log.d(LOG_TAG, "Toggling blackCell opposite: " + inputCell.getCellName() + ", i.e. toggling: " + cells[oppositeRow][oppositeCol].getCellName());
            cells[oppositeRow][oppositeCol].toggleBlackCell();
        }
    }

    private void getClueNumbers() {
        ArrayList<Cell> startCells = new ArrayList<Cell>();
        ArrayList<Integer[]> startCellCoordsGeneral = new ArrayList<Integer[]>();
        ArrayList<Integer[]> startCellCoordsOrdered = new ArrayList<Integer[]>();

        Integer[] lastCellCoords = {rowCount, rowCount};          // Initialise coords as last cell so that it will definitely be corrected on the first run through
        Integer[] nextClueStartCellCoords = lastCellCoords;
        int clueNo = 1;


        for (Clue clue : hClues){
            if (! startCells.contains(clue.getStartCell())){
                startCells.add(clue.getStartCell());
                Integer[] coords = {clue.getStartCell().getRow(), clue.getStartCell().getColumn()} ;
                startCellCoordsGeneral.add(coords);

            }
        }
        for (Clue clue : vClues){
            if (! startCells.contains(clue.getStartCell())){
                startCells.add(clue.getStartCell());
                Integer[] coords = {clue.getStartCell().getRow(), clue.getStartCell().getColumn()} ;
                startCellCoordsGeneral.add(coords);
            }
        }

        // Loop through all coords and sort them into order. Once the next startCell is found, remove it from the general list.
        while (startCellCoordsGeneral.size() > 0 ) {
            for (Integer[] coords : startCellCoordsGeneral) {
                // Make nextClueStartCell equal to that under investigation if its row is less than that of another
                if (coords[0] < nextClueStartCellCoords[0]) {
                    nextClueStartCellCoords = coords;
                } else if (coords[0] == nextClueStartCellCoords[0]) {
                    if (coords[1] < nextClueStartCellCoords[1]) {
                        nextClueStartCellCoords = coords;
                    }
                }
            }

            // Check that it isn't the last time through
            if (nextClueStartCellCoords != lastCellCoords) {
 //               Log.d(LOG_TAG, "Next Clue Start Cell Coords = (" + nextClueStartCellCoords[0] + "," + nextClueStartCellCoords[1] + ").");
                startCellCoordsOrdered.add(nextClueStartCellCoords);
                startCellCoordsGeneral.remove(nextClueStartCellCoords);

                //Reset nextClueStartCellCoords:
                nextClueStartCellCoords = lastCellCoords;
            }
        }

        for (Integer[] coords : startCellCoordsOrdered ) {
            // Loop through and assign each clue its number
            // i.e. cellViews[row][col].setClueNumber(clueNo);
            cellViews[coords[0]][coords[1]].setClueNumber(clueNo);

            clueNo++;
        }
    }

    // Redundant
    private String getFormattedDate() {
        // Return current date in YYYYMMDD format
        SimpleDateFormat sdf = new SimpleDateFormat(SAVE_DATE_FORMAT);
        String formattedDate = sdf.format(new Date());
        Log.d(LOG_TAG, "Date being input to saveArray[1] = " + formattedDate);

        return formattedDate ;
    }

    public int getScreenWidth() {
        return screenWidth;
    }
    public File getCrosswordPictureFile() {
        return crosswordImageFile ;
    }
    public File getCluePictureFile() {
        return clueImageFile ;
    }
    public String getActivityTitle() {
        return getDisplayDate()+ ": " + title ;
    }

    public Cell getCell(int row, int column) {
        return cells[row][column];
    }

    public String[] getSaveArray() {
        // Create String array to save and pass around with intents
        // Format:  array[0]    = Crossword name
        //          array[1]    = crossword date
        //          array[2]    = crossword rowCount
        //          array[3]    = cell width
        //          array[4]    = crossword picture resource
        //          array[5]    = clues picture resource
        //          array[6-n]  = crossword cells. "" denotes a blank (white) cell. "-" denotes a black cell.

        Log.d(LOG_TAG, "Saving Crossword...");

        int saveArraySize = SAVE_ARRAY_START_INDEX + (rowCount * rowCount);
        String[] saveArray = new String[saveArraySize];

        // Initialise array
        saveArray[SAVED_ARRAY_INDEX_TITLE] = this.title ;
        saveArray[SAVED_ARRAY_INDEX_DATE] = date ;
        saveArray[SAVED_ARRAY_INDEX_ROW_COUNT] = "" + rowCount ;
        saveArray[SAVED_ARRAY_INDEX_CELL_WIDTH] = "" + cellWidth ;
        saveArray[SAVED_ARRAY_INDEX_CROSSWORD_IMAGE] = "" + saveDir.getAbsolutePath()  + "/" + SAVE_CROSSWORD_FILE_NAME;
        saveArray[SAVED_ARRAY_INDEX_CLUE_IMAGE] = "" + saveDir.getAbsolutePath() + "/" + SAVE_CLUE_IMAGE_FILE_NAME;

        // Loop through cells and save contents to the array.
        int index = SAVE_ARRAY_START_INDEX; // For iterating over, and saving current cell to index.
         for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                if (!cells[i][j].isBlackCell()) {
                    saveArray[index] = cells[i][j].getText().toString() ;   // Save whatever text is in the box to the saveArray. Will be empty if box is empty.
                } else {
                    saveArray[index] = "-" ;
                }

//                Log.d("SAVEARRAY:", "At index = " + index + ", saveArray[index] = " + saveArray[index]);  // Don't output this at the moment because it seems to work, but spams the log thread. If saving stops working, uncomment this line.
                index++ ;
            }
        }

        for (int k = 0 ; k < saveArray.length ; k++) {
//            Log.d(LOG_TAG, "saveArray[" + k + "] = " + saveArray[k]) ;  // Don't output this at the moment because it seems to work, but spams the log thread. If saving stops working, uncomment this line.
        }


        return saveArray ;
    }

    public static String[] getSaveArray(File savedFile) {
        // Return the save array so that the app may continue as normal from a saved file
        String[] saveArray ;
        ArrayList<String> saveArrayList = new ArrayList<String>() ;
        // Probably not required as Activity calling this method should check the file exists first
        if (!savedFile.exists()) {
            Log.e(LOG_TAG, "saved file does not exist, so cannot be loaded");
            saveArrayList.add(null);  // Return empty array
            saveArray = saveArrayList.toArray(new String[saveArrayList.size()]) ;
            return saveArray ;
        }

        try {
            // Read file
            BufferedReader br = new BufferedReader(new FileReader(savedFile));
            String line ;
            while ((line = br.readLine()) != null) {
                saveArrayList.add(line) ;
            }
            br.close() ;

        }catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }

        Log.d(LOG_TAG, "size of file read = " + saveArrayList.size());

        saveArray = saveArrayList.toArray(new String[saveArrayList.size()]) ;
        Log.d(LOG_TAG, "Length of saveArray = " + saveArray.length);

        return saveArray ;
    }

    public void saveCrossword() {
        // Save file to memory. Use files created by initialiseSaveFiles
        String[] saveArray = getSaveArray() ;

        try {
            Log.d(LOG_TAG, "Writing crossword file...");
            FileWriter fileWriter = new FileWriter(crosswordFile);

            for (int i = 0 ; i < saveArray.length ; i++) {
                fileWriter.write(saveArray[i] + "\n");
            }
            fileWriter.close();


        }catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't open fileWriter to save the crossword file.");
        }
    }

    private void initialiseSaveFiles() {
        // Format File name
        fileName = date + "-" + title.replaceAll(" ","_").replaceAll("-","__").toLowerCase() ;
        // Create the save files/directories
        // Directory
        saveDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),fileName);
        Log.d(LOG_TAG,"rootDir = " + saveDir.getPath());
        if(!saveDir.exists()) {
            Log.d(LOG_TAG,"rootDir doesn't exist, so creating it...");
            if (!saveDir.mkdirs()) {
                Log.e(LOG_TAG, "Main directory not created");
            }
        } else {
            Log.d(LOG_TAG, "rootDir already exsits.");
        }

        // Main Crossword file
        crosswordFile = new File(saveDir, SAVE_CROSSWORD_FILE_NAME);
        if(!crosswordFile.exists()) {
            try {
                if (!crosswordFile.createNewFile()) {
                    Log.e(LOG_TAG, "Crossword file not created");
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Crossword file not created. Exception message: ");
                Log.e(LOG_TAG,e.getMessage());

            }
        }

        // Clue Image path
        clueImageFile = new File(saveDir,SAVE_CLUE_IMAGE_FILE_NAME) ;
        if(!clueImageFile.exists()) {
            try {
                if (!clueImageFile.createNewFile()) {
                    Log.e(LOG_TAG, "Clue image file not created");
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Clue image file not created. Exception message: ");
                Log.e(LOG_TAG,e.getMessage());

            }
        }
        // Crossword Image path
        crosswordImageFile = new File(saveDir,SAVE_CROSSWORD_IMAGE_FILE_NAME) ;
        if(!crosswordImageFile.exists()) {
            try {
                if (!crosswordImageFile.createNewFile()) {
                    Log.e(LOG_TAG, "Crossword image file not created");
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Crossword image file not created. Exception message: ");
                Log.e(LOG_TAG,e.getMessage());

            }
        }

        // Save the grid
        saveCrossword();
    }

}