package com.thonners.crosswordmaker;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CrosswordTwo implements View.OnKeyListener {

    private static final String LOG_TAG = "CrosswordTwo";

    public static final String CROSSWORD_EXTRA = "com.thonners.crosswordmaker.crossword_extra";
    public static final String SAVE_DATE_FORMAT = "yyyyMMdd";
    public static final String SAVE_CROSSWORD_FILE_NAME = "crossword.json";
    public static final String SAVE_CLUE_IMAGE_FILE_NAME = "clue.jpg";
    public static final String SAVE_CROSSWORD_IMAGE_FILE_NAME = "image_crossword.jpg";

    @Expose // This decorator select these attributes to be included in the json serialisation
    private String title = "", date = "";
    @Expose
    private final CellTwo[][] cells;

    public ClueTwo[] hClues = null, vClues = null;

    private CellTwo highlightedCell;
    private ClueTwo highlightedClue = null;
    @Expose
    public final int rowCount;
    @Expose
    public final int colCount;
    public boolean editGridMode;

    @Expose
    private final boolean isRotationallySymmetric;

    private File crosswordFile = null;


    public static CrosswordTwo fromJsonFile(Context context, String jsonFilePath) throws IOException {

        try (FileInputStream fIn = new FileInputStream(jsonFilePath); BufferedReader myReader =
                new BufferedReader(new InputStreamReader(fIn)); FileReader fileReader =
                new FileReader(jsonFilePath)) {
            String jsonString = "", line;
            while ((line = myReader.readLine()) != null) {
                jsonString += line;
            }
            Log.d(LOG_TAG, "Crossword read from: " + jsonFilePath + ":");
            Log.d(LOG_TAG, jsonString);
            return fromJson(context, jsonString);
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Exception occurred during file save. Target filename: " + jsonFilePath);
            Log.e(LOG_TAG, ex.getMessage());
            throw ex;
        }
    }

    public static CrosswordTwo fromJson(Context context, String json) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        CrosswordTwo crossword = gson.fromJson(json, CrosswordTwo.class);
        crossword.findClues();
        Log.d(LOG_TAG, "Crossword instantiated from JSON: " + crossword);
        crossword.initialiseSaveFile(context);
        return crossword;
    }

    /**
     * Constructor for when a new square Crossword is being created
     *
     * @param rowCount Number of rows/cols in the crossword
     */
    public CrosswordTwo(int rowCount) {
        this(rowCount, rowCount, true);
    }

    /**
     * Constructor for when a new, non-rotationally symmetric Crossword is being created
     *
     * @param rowCount Number of rows in the crossword
     * @param colCount Number of columns in the crossword
     */
    public CrosswordTwo(int rowCount, int colCount) {
        this(rowCount, colCount, false);
    }

    /**
     * Constructor for when a new Crossword is being created
     *
     * @param rowCount                Number of rows in the crossword
     * @param colCount                Number of columns in the crossword
     * @param isRotationallySymmetric Whether the crossword grid is rotationally symmetric
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
                this.cells[r][c] = new CellTwo(r, c);
            }
        }
    }


    public void findClues() {
        int currentClueNumber = 0;
        ArrayList<ClueTwo> hCluesTmp = new ArrayList<>();
        ArrayList<ClueTwo> vCluesTmp = new ArrayList<>();
        for (int row = 0; row < this.rowCount; row++) {
            for (int col = 0; col < this.colCount; col++) {
                CellTwo cell = this.getCell(row, col);
                if (cell.getIsBlackCell()) {
                    // Not much to do...
                } else {
                    boolean previousHCellIsWhite =
                            col > 0 && !getCell(row, col - 1).getIsBlackCell();
                    boolean previousVCellIsWhite =
                            row > 0 && !getCell(row - 1, col).getIsBlackCell();
                    boolean nextHCellIsWhite =
                            col < colCount - 1 && !getCell(row, col + 1).getIsBlackCell();
                    boolean nextVCellIsWhite =
                            row < rowCount - 1 && !getCell(row + 1, col).getIsBlackCell();
                    boolean nextCellIsHClueStarter = nextHCellIsWhite && !previousHCellIsWhite;
                    boolean nextCellIsVClueStarter = nextVCellIsWhite && !previousVCellIsWhite;
                    if (nextCellIsHClueStarter || nextCellIsVClueStarter) {
                        currentClueNumber++;
                        Log.d(LOG_TAG,
                                "Found new clue (#" + currentClueNumber + "), cell: " + cell);
                    }
                    if (nextCellIsHClueStarter) {
                        ClueTwo clue = new ClueTwo(ClueTwo.Orientation.HORIZONTAL,
                                currentClueNumber);
                        hCluesTmp.add(clue);
                        Log.d(LOG_TAG, "\tNew clue was horizontal");
                        cell.setHClue(clue);
                    }
                    if (nextCellIsVClueStarter) {
                        ClueTwo clue = new ClueTwo(ClueTwo.Orientation.VERTICAL, currentClueNumber);
                        vCluesTmp.add(clue);
                        Log.d(LOG_TAG, "\tNew clue was vertical");
                        cell.setVClue(clue);
                    }
                    if (previousHCellIsWhite) {
                        // Get the clue from the previous cell
                        cell.setHClue(getCell(row, col - 1).getHClue());
                        Log.d(LOG_TAG,
                                "Adding cell: " + cell + " to H clue #" + cell.getHClue().getClueNumber());
                        Log.d(LOG_TAG, "Clue has " + cell.getHClue().getCells().size() + " cells");
                    }
                    if (previousVCellIsWhite) {
                        // Get the clue from the previous cell
                        cell.setVClue(getCell(row - 1, col).getVClue());
                        Log.d(LOG_TAG,
                                "Adding cell: " + cell + " to V clue #" + cell.getVClue().getClueNumber());
                    }
                }
            }
        }
        hClues = new ClueTwo[hCluesTmp.size()];
        vClues = new ClueTwo[vCluesTmp.size()];
        hClues = hCluesTmp.toArray(hClues);
        vClues = vCluesTmp.toArray(vClues);
        Log.d(LOG_TAG, "Clues found: #H = " + hClues.length + ", #V = " + vClues.length);
    }

    public void saveCrossword(Context context) {
        Log.d(LOG_TAG, "saveCrossword called...");
        if (crosswordFile == null) initialiseSaveFile(context);
        try (FileWriter fileWriter = new FileWriter(crosswordFile)) {
            fileWriter.write(toJson());
            Log.d(LOG_TAG, "Crossword Saved to: " + crosswordFile.getPath());
        } catch (IOException ex) {
            Log.e(LOG_TAG,
                    "Exception occurred during file save. Target filename: " + crosswordFile.getName());
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void initialiseSaveFile(Context context) {
        if (date.isEmpty() || title.isEmpty()) {
            throw new IllegalArgumentException("Date and Title must both be set. Got Date: " + date + ", Title: " + title);
        }
        String filename =
                date + "-" + title.replaceAll(" ", "_").replaceAll("-", "__") + "-" + SAVE_CROSSWORD_FILE_NAME;
        crosswordFile = new File(context.getFilesDir(), filename);
        Log.d(LOG_TAG,
                "Crossword File: " + crosswordFile.getPath() + ", exists: " + crosswordFile.exists() + ", is a file: " + crosswordFile.isFile());
    }

    public File getCrosswordFile() {
        return crosswordFile;
    }

    public CellTwo getCell(int row, int col) {
        return this.cells[row][col];
    }

    /**
     * If in edit mode, toggles whether the cell is black.
     * If the grid is rotationally symmetric, toggles the 'opposite' cell to match too.
     *
     * @param row The row index of the cell
     * @param col The column index of the cell
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
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDisplayDate() {
        return date.substring(6, 8) + "/" + date.substring(4, 6) + "/" + date.substring(0, 4);
        //        // Method to use for saving the display displayDate. Not sure this is required
        //        SimpleDateFormat sdf = new SimpleDateFormat(SAVE_DATE_FORMAT);      // Save
        //        Formatted
        //        displayDate
        //        DateFormat localeDateFormat = android.text.format.DateFormat.getDateFormat(con)
        //        ;    //
        //        Locale displayDate format
        //        Date dateProper;
        //
        //        try {
        //            dateProper = sdf.parse(date);
        //        } catch (Exception e) {
        //            Log.e(LOG_TAG, "Couldn't parse Crossword.displayDate (should be in save
        //            format)
        //            into something useful. This is coming from HomeActivity via intents so
        //            check the
        //            routing!");
        //            return context.getResources().getString(R.string.error_crossword_date); //
        //            Return
        //            the error message to be displayed.
        //        }
        //
        //        return localeDateFormat.format(dateProper);
    }

    public String getDisplayPercentageComplete() {
        // TODO: Calculate % complete and store it in the object/file somewhere
        return "??%";
    }

    public String getActivityTitle() {
        return getDisplayDate() + ": " + title;
    }

    public void cellTouched(int row, int col) {
        if (row < 0 || row >= rowCount || col < 0 || col >= colCount) {
            Log.d(LOG_TAG, "Touch detected outside the active grid, so ignoring it.");
            return;
        }
        if (editGridMode) {
            toggleBlackCell(row, col);
        } else {
            CellTwo touchedCell = getCell(row, col);
            if (touchedCell.getIsBlackCell()) {
                Log.d(LOG_TAG, "Black cell touched: " + touchedCell);
                return;
            }
            boolean flipOrientation = (highlightedCell != null) && (highlightedCell == touchedCell);
            if (flipOrientation) {
                highlightedClue = touchedCell.getOtherClue(highlightedClue);
            } else {
                highlightedCell = touchedCell;
                highlightedClue = touchedCell.getClueForHighlight(highlightedClue);
            }
            if (highlightedClue != null) {
                Log.d(LOG_TAG, "Highlighted clue: " + highlightedClue.toString());
            } else {
                Log.d(LOG_TAG, "Highlighted clue: NULL");
            }
            if (highlightedCell != null) {
                Log.d(LOG_TAG, "Highlighted cell: " + highlightedCell.toString());
            } else {
                Log.d(LOG_TAG, "Highlighted cell: NULL");
            }
        }
    }

    public CellTwo getHighlightedCell() {
        return highlightedCell;
    }

    public ClueTwo getHighlightedClue() {
        return highlightedClue;
    }

    public boolean deleteCrosswordFile() {
        // TODO: Implement deleting the crossword file
        Log.d(LOG_TAG, "deleteCrosswordFile() called on " + getActivityTitle() + ", but deleting "
                + "the crossword file not yet implemented.");
        return false;
    }

    @NonNull
    public String toString() {
        if (title.isEmpty() || date.isEmpty()) return "Uninitialised Crossword";
        return getActivityTitle();
    }


    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO: Move this key even handling into the CrosswordTwo class,
        //  make a list of legit keys that we will respond to (i.e. all the letters + backspace +
        //  enter)
        //  Set the cell value to the pressed letter and highlight next cell (/collapse keyboard
        //  if final cell in clue)
        //  Rotate cell if enter pressed (if poss)
        //  delete cell value and highlight previous cell if backspace (aka DEL) pressed
        Log.d(LOG_TAG, "onKeyListener");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DEL:
                    Log.d(LOG_TAG, "Backspace pressed: " + event.getDisplayLabel());
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                    Log.d(LOG_TAG, "Enter pressed: " + event.getDisplayLabel());
                    return true;
                case KeyEvent.KEYCODE_A:
                    highlightedCell.setCharacter("A");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_B:
                    highlightedCell.setCharacter("B");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_C:
                    highlightedCell.setCharacter("C");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_D:
                    highlightedCell.setCharacter("D");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_E:
                    highlightedCell.setCharacter("E");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_F:
                    highlightedCell.setCharacter("F");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_G:
                    highlightedCell.setCharacter("G");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_H:
                    highlightedCell.setCharacter("H");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_I:
                    highlightedCell.setCharacter("I");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_J:
                    highlightedCell.setCharacter("J");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_K:
                    highlightedCell.setCharacter("K");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_L:
                    highlightedCell.setCharacter("L");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_M:
                    highlightedCell.setCharacter("M");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_N:
                    highlightedCell.setCharacter("N");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_O:
                    highlightedCell.setCharacter("O");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_P:
                    highlightedCell.setCharacter("P");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_Q:
                    highlightedCell.setCharacter("Q");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_R:
                    highlightedCell.setCharacter("R");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_S:
                    highlightedCell.setCharacter("S");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_T:
                    highlightedCell.setCharacter("T");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_U:
                    highlightedCell.setCharacter("U");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_V:
                    highlightedCell.setCharacter("V");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_W:
                    highlightedCell.setCharacter("W");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_X:
                    highlightedCell.setCharacter("X");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_Y:
                    highlightedCell.setCharacter("Y");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
                case KeyEvent.KEYCODE_Z:
                    highlightedCell.setCharacter("Z");
                    Log.d(LOG_TAG,
                            "ACTION_DOWN: " + event.getDisplayLabel() + ", keycode: " + event);
                    return true;
            }
            Log.d(LOG_TAG,
                    "Invalid key pressed: " + event.getDisplayLabel() + ", keycode: " + event);
        }
        return false;
    }
}
