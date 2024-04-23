package com.thonners.crosswordmaker;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ClueTwo {

    private final ArrayList<CellTwo> cells = new ArrayList<>();

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private final Orientation orientation;

    private final int clueNumber;


    public ClueTwo(Orientation orientation, int number) {
        this.orientation = orientation;
        this.clueNumber = number;
    }

    public void addCell(CellTwo cell) {
        cells.add(cell);
    }

    public ArrayList<CellTwo> getCells() {
        return cells;
    }

    public CellTwo getFirstCell() {
        if (!cells.isEmpty()) {
            return cells.get(0);
        }
        return null;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getClueNumber() {
        return clueNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return "ClueTwo{" +
                "orientation=" + orientation +
                ", clueNumber=" + clueNumber +
                ", cells=" + cells +
                '}';
    }
}
