package com.thonners.crosswordmaker;

public class CellTwo {

    private boolean isBlackCell;
    private Character character;
    private boolean hasTrailingWordSeparator;
    private boolean hasTrailingHyphen;

    /**
     * Constructor for the first time a crossword grid is created
     */
    public CellTwo() {
        this.isBlackCell = false;
        this.character = null;
        this.hasTrailingWordSeparator = false;
        this.hasTrailingHyphen = false;

    }

    /**
     * Constructor for when a grid is being restored
     *
     * @param isBlackCell
     * @param character
     * @param hasTrailingWordSeparator
     * @param hasTrailingHyphen
     */
    public CellTwo(boolean isBlackCell, char character, boolean hasTrailingWordSeparator, boolean hasTrailingHyphen) {
        this.isBlackCell = isBlackCell;
        this.character = character;
        this.hasTrailingWordSeparator = hasTrailingWordSeparator;
        this.hasTrailingHyphen = hasTrailingHyphen;
    }

    public void setCharacter(String s) {
        if (s.length() > 0) {
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
}
