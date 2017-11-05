package com.thonners.crosswordmaker;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class to hold all the details of a clue
 *
 * Refactored version to clean up original code
 *
 * @author M Thomas
 * @since 01/08/17
 */

public class Clue2 {

    private Orientation orientation ;
    private Collection<Cell2> clueCells = new ArrayList<>();

    public Clue2(Orientation orientation) {
        this.orientation = orientation;
    }

    public enum Orientation{
        HORIZONTAL, VERTICAL
    }

}
