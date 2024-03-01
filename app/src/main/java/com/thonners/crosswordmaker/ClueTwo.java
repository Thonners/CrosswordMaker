package com.thonners.crosswordmaker;

public class ClueTwo {

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private Orientation orientation;

    private CellTwo[] cells;

    public ClueTwo(CellTwo[] cells, Orientation orientation) {
        this.cells = cells;
        this.orientation = orientation;
    }
}
