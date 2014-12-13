package com.thonners.crosswordmaker;

/** Class to hold all the details of different clues
 * Created by mat on 13/12/14.
 */
public class Clue {

    public final static String HORIZONTAL_CLUE = "horizontal" ;
    public final static String VERTICAL_CLUE = "vertical" ;

    public String type ;
    public Cell startCell ;
    public int length ;

    public Clue (String clueOrientation , Cell startCell) {
        this.type = clueOrientation;
        this.startCell = startCell ;
    }

    public void setLength(int l) {
        this.length = l ;
    }

}
