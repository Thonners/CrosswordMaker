package com.thonners.crosswordmaker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class CellTwo {
    private final static String LOG_TAG = "CellTwo";

    @Expose
    private final int row, col;
    @Expose
    private boolean isBlackCell;
    @Expose
    private Character character;
    @Expose
    private boolean hasTrailingWordSeparator;
    @Expose
    private boolean hasTrailingHyphen;

    private ClueTwo hClue = null;
    private ClueTwo vClue = null;

    /**
     * Constructor for the first time a crossword grid is created
     */
    public CellTwo(int row, int col) {
        this.row = row;
        this.col = col;
        this.isBlackCell = false;
        this.character = null;
        this.hasTrailingWordSeparator = false;
        this.hasTrailingHyphen = false;

    }

    /**
     * Constructor for when a grid is being restored
     *
     * @param isBlackCell              Whether the cell is a black cell or not
     * @param character                The character to be filled into this cell
     * @param hasTrailingWordSeparator Whether the cell has a word-separator after it
     * @param hasTrailingHyphen        Whether the cell has a hyphen after it
     */
    public CellTwo(int row, int col, boolean isBlackCell, char character,
                   boolean hasTrailingWordSeparator, boolean hasTrailingHyphen) {
        this.row = row;
        this.col = col;
        this.isBlackCell = isBlackCell;
        this.character = character;
        this.hasTrailingWordSeparator = hasTrailingWordSeparator;
        this.hasTrailingHyphen = hasTrailingHyphen;
    }

    public String getCharacterAsString() {
        if (this.character == null) return "";
        return this.character.toString();
    }

    public void setCharacter(String s) {
        if (!s.isEmpty()) {
            this.character = s.charAt(s.length() - 1);
        }
    }

    public void setIsBlackCell(boolean isBlackCell) {
        this.isBlackCell = isBlackCell;
    }

    public void toggleIsBlackCell() {
        this.isBlackCell = !this.isBlackCell;
    }

    public boolean getIsBlackCell() {
        return this.isBlackCell;
    }

    public boolean hasTrailingWordSeparator() {
        return hasTrailingWordSeparator;
    }

    public void setHasTrailingWordSeparator(boolean hasTrailingWordSeparator) {
        this.hasTrailingWordSeparator = hasTrailingWordSeparator;
    }

    public boolean hasTrailingHyphen() {
        return hasTrailingHyphen;
    }

    public void setHasTrailingHyphen(boolean hasTrailingHyphen) {
        this.hasTrailingHyphen = hasTrailingHyphen;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public ClueTwo getHClue() {
        return hClue;
    }

    public void setHClue(ClueTwo horizontalClue) {
        if (horizontalClue == null) {
            Log.e(LOG_TAG, "hClue is null");
            return;
        }
        this.hClue = horizontalClue;
        horizontalClue.addCell(this);
    }

    public ClueTwo getVClue() {
        return vClue;
    }

    public void setVClue(ClueTwo verticalClue) {
        if (verticalClue == null) {
            Log.e(LOG_TAG, "vClue is null for cell: " + this);
            return;
        }
        this.vClue = verticalClue;
        verticalClue.addCell(this);
    }

    public ClueTwo getOtherClue(ClueTwo currentClue) throws NoOtherClueException {
        ClueTwo.Orientation currentClueOrientation = currentClue.getOrientation();
        switch (currentClueOrientation) {
            case VERTICAL:
                if (getHClue() != null) return getHClue();
            case HORIZONTAL:
                if (getVClue() != null) return getVClue();
        }
        throw new NoOtherClueException();
    }

    public ClueTwo getPrimaryClue() {
        if (hClue != null) {
            return hClue;
        }
        return vClue;
    }

    public ClueTwo getClueForHighlight(ClueTwo previousClue) {
        if (previousClue != null && (previousClue == this.hClue || previousClue == this.vClue)) {
            return previousClue;
        }
        return getPrimaryClue();
    }

    public String toJson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public static CellTwo fromJson(String json) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        CellTwo cell = gson.fromJson(json, CellTwo.class);
        Log.d(LOG_TAG, "Rebuilt cell from JSON: " + cell);
        return cell;
    }

    @NonNull
    public String toString() {
        return "Cell(" + row + ", " + col + ")";
    }
}

class NoOtherClueException extends Exception {

}