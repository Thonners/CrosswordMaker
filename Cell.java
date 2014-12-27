package com.thonners.crosswordmaker;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.security.Key;

/**
 * Created by mat on 30/11/14.
 */
public class Cell extends EditText implements View.OnClickListener, View.OnFocusChangeListener{

    private String logTag = "Clue" ;

    public int row ;
    public int column ;
    private String cellName ; // String to make it quicker to include in debugging. Format (X,Y)

    private int maxLength = 1 ; // Max number of letters in the editText
    private InputFilter[] whiteCellInputFilter = {new InputFilter.AllCaps(), new InputFilter.LengthFilter(maxLength), new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            // Input filter ensures that letter entered is a letter. Sets it to "" (i.e. nothing) if not.
            for (int i = start ; i < end ; i++){
                if (!Character.isLetter(source.charAt(start))){
                    return "";
                }
            }

            return null;
        }
    }} ;

    public boolean blackCell ;
    public Character value ;
    public boolean gridMakingPhase = true ;    // If gridMakingPhase, clicking on a cell changes it from black to white

    // Constructor:
    // For creating the initial grid, all cells start life as white cells
    public Cell(Context context, int r, int c) {
        super(context);

        setRow(r) ;
        setColumn(c);
        setCellName(row,column);
        setBlackCellStatus(false) ; // Force all cells to start life white

        this.setBackground(getResources().getDrawable(R.drawable.cell_white));
        this.setAllCaps(true);
        this.setTextColor(context.getResources().getColor(R.color.black));
        this.setClickable(true);
        this.setFocusable(false);
        this.setPadding(0,0,0,0);
        this.setGravity(Gravity.CENTER);
        this.setSelectAllOnFocus(true);         // Will select text on focus so user doesn't need to delete previous text if incorrect
        this.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        setOnClickListener(this);
        setOnFocusChangeListener(this);
      //  setOnKeyListener(this);
    }

    // Maybe for creating the grid after user has made choices (/when camera has worked it out)
    public Cell(Context context, int r, int c, boolean blackCellIn) {
        super(context);

        row = r ;
        column = c ;
        blackCell = blackCellIn;

        this.setBackground(getResources().getDrawable(R.drawable.cell_white));
    }

    private void setBlackCell() {
        this.setBackground(getResources().getDrawable(R.drawable.cell_black));
    }
    private void setWhiteCell() {
        this.setBackground(getResources().getDrawable(R.drawable.cell_white));
    }
    private void setFocusedMajor() {
        this.setBackground(getResources().getDrawable(R.drawable.cell_focus_main));
    }
    private void setFocusedMinor() {
        this.setBackground(getResources().getDrawable(R.drawable.cell_focus_minor));
    }

    public void toggleBlackCell() {
        blackCell = ! blackCell ;

        if (blackCell) {
            setBlackCell();
        } else {
            setWhiteCell();
        }
    }

    // During grid creation phase
    @Override
    public void onClick(View view) {
        if(gridMakingPhase) {
            Log.d("GridMakingPhase","Trying to toggleBlackCell()");
            toggleBlackCell();
            return ;
        }
    }

    // For highlighting active cell during normal operation
    @Override
    public void onFocusChange(View view, boolean hasFocus){
        // To change cell highlight if cell has focus
        if (hasFocus) {
            Log.d(logTag, "Cell " + cellName + " has focus");
            setFocusedMajor();
        } else {
            Log.d(logTag, "Cell " + cellName + " has lost focus");
            setWhiteCell();
        }
    }


    public void setGridMakingPhase(Boolean isGridMakingPhase){
        gridMakingPhase = isGridMakingPhase ;
    }

    public void setWhiteCellsEditable() {
        if (! blackCell) {
            // Set focusable if white cell so that text can be edited
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);

            // Force all input to capital letters
            this.setFilters(whiteCellInputFilter);
        } else {
            // Set not clickable if black cell
            this.setClickable(false);
        }
    }

    public void setBlackCellStatus(boolean isBlackCell){
        this.blackCell = isBlackCell ;
    }

    public void setColumn(int col) {
        this.column = col ;
    }

    public void setRow (int row) {
        this.row = row ;
    }

    public void setCellName(int row, int col) {
        this.cellName = "(" + row + "," + col + ")" ;
    }


    public int getCellId(int rowCount) {
        return (this.row * rowCount + column) ;     // Cells are numbered from 0 through to rowCount^2 -1
    }

    public int getRow() {
        return this.row ;
    }

    public int getColumn() {
        return this.column ;
    }

    public boolean isBlackCell() {
        return blackCell ;
    }

}
