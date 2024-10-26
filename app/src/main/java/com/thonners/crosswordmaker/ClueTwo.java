package com.thonners.crosswordmaker;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ClueTwo {

    private final ArrayList<CellTwo> cells = new ArrayList<>();

    public enum Orientation {
        HORIZONTAL, VERTICAL
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

    public CellTwo getNextCell(CellTwo previousCell) throws NoMoreCellsException {
        boolean thisCellIsNextCell = false;
        for (CellTwo cell : this.cells) {
            if (thisCellIsNextCell) return cell;
            thisCellIsNextCell = (previousCell == cell);
        }
        throw new NoMoreCellsException();
        //        return null;
    }

    public CellTwo getPreviousCell(CellTwo subsequentCell) throws NoMoreCellsException {
        CellTwo previousCell = null;
        for (CellTwo cell : this.cells) {
            if (previousCell == null) {
                // If it's null and we match, then we're already at the first cell
                if (subsequentCell == cell) throw new NoMoreCellsException();
            } else {
                if (subsequentCell == cell) {
                    return previousCell;
                }
            }
            previousCell = cell;
        }
        throw new NoMoreCellsException();
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
        return "ClueTwo{" + "orientation=" + orientation + ", clueNumber=" + clueNumber + ", " +
                "cells=" + cells + '}';
    }
}

class NoMoreCellsException extends Exception {

}