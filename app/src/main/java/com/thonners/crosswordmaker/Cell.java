package com.thonners.crosswordmaker;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by mat on 30/11/14.
 */
public class Cell extends EditText implements View.OnClickListener, View.OnLongClickListener, View.OnFocusChangeListener , TextWatcher {

    private static final String LOG_TAG = "Cell" ;

    private Crossword crossword ;

    private int row ;
    private int column ;
    private String cellName ; // String to make it quicker to include in debugging. Format (X,Y)
    private CellDecoration decoration ;

    private class CellDecoration {
//        NONE (0),
//        HYPEN_START (1),
//        HYPHEN_END (2),
//        WORD_END (3),
//        WORD_START (4);

        private int decorationIndex;

        CellDecoration(int decorationIndex) {
            this.decorationIndex = decorationIndex;
        }

        public void incrementIndex() {
            this.decorationIndex =  (decorationIndex + 1) % 5;
        }

        public int getWhiteCellBG() {
            switch (decorationIndex) {
                case 1:
                    return R.drawable.cell_hyphen_start;
                case 2:
                    return R.drawable.cell_hyphen_end;
                case 3:
                    return R.drawable.cell_word_end;
                case 4:
                    return R.drawable.cell_word_start;
                default:
                    return R.drawable.cell_white;
            }
        }
        public int getFocusMainBG() {
            switch (decorationIndex) {
//                case 1:
//                    return R.drawable.cell_focus_main_hyphen_start;
//                case 2:
//                    return R.drawable.cell_focus_main_hyphen_end;
//                case 3:
//                    return R.drawable.cell_focus_main_word_end;
//                case 4:
//                    return R.drawable.cell_focus_main_word_start;
                default:
                    return R.drawable.cell_focus_main;
            }
        }
        public int getFocusMinorBG() {
            switch (decorationIndex) {
//                case 1:
//                    return R.drawable.cell_focus_minor_hyphen_start;
//                case 2:
//                    return R.drawable.cell_focus_minor_hyphen_end;
//                case 3:
//                    return R.drawable.cell_focus_minor_word_end;
//                case 4:
//                    return R.drawable.cell_focus_minor_word_start;
                default:
                    return R.drawable.cell_focus_minor;
            }
        }
    }

    private Clue hClue = null ;  // Horizontal clue to which cell belongs - initialise as null, and set later if required.
    private Clue vClue = null  ; // Vertical clue to which cell belongs
    private Clue activeClue = null  ; // Set the active clue so that cell focus can move as input is done

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

    private boolean blackCell ;
    public Character value ;
    public boolean gridMakingPhase = true ;    // If gridMakingPhase, clicking on a cell changes it from black to white

    // Constructor:
    // For creating the initial grid, all cells start life as white cells
    public Cell(Context context, Crossword crossword, int r, int c) {
        super(context);
        setCrossword(crossword);

        setRow(r) ;
        setColumn(c);
        setCellName(row,column);
        setBlackCellStatus(false) ; // Force all cells to start life white
        decoration = new CellDecoration(0) ; // Start life without decoration

//        this.setBackground(getResources().getDrawable(R.drawable.cell_white));
        this.setBackground(getResources().getDrawable(decoration.getWhiteCellBG()));
        this.setTextColor(context.getResources().getColor(R.color.black));
        this.setClickable(true);
        this.setFocusable(false);   // Initialise as false for gridMaker
        this.setPadding(0,0,0,0);
        this.setGravity(Gravity.CENTER);
        this.setSelectAllOnFocus(true);         // Will select text on focus so user doesn't need to delete previous text if incorrect
        this.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        setOnClickListener(this);
        setOnFocusChangeListener(this);
//        setOnLongClickListener(this);
      //  setOnKeyListener(this);
    }

    // Maybe for creating the grid after user has made choices (/when camera has worked it out)
    public Cell(Context context, Crossword crossword, int r, int c, boolean blackCellIn) {
        super(context);

        row = r ;
        column = c ;
        blackCell = blackCellIn;

        this.setBackground(getResources().getDrawable(R.drawable.cell_white));
    }

    private void setCrossword(Crossword cwd) {
        this.crossword = cwd ;
    }

    private void setBlackCell() {
        this.setBackground(getResources().getDrawable(R.drawable.cell_black));
    }
    private void setWhiteCell() {
        this.setBackground(getResources().getDrawable(decoration.getWhiteCellBG()));
//        this.setBackground(getResources().getDrawable(R.drawable.cell_white));
    }
    public void setFocusedMajor() {
        if (! this.hasFocus()) {
            requestFocus();
        }
//        this.setBackground(getResources().getDrawable(R.drawable.cell_focus_main));
        this.setBackground(getResources().getDrawable(decoration.getFocusMainBG()));
    }
    public void setFocusedMinor() {
//        this.setBackground(getResources().getDrawable(R.drawable.cell_focus_minor));
        this.setBackground(getResources().getDrawable(decoration.getFocusMinorBG()));
    }

    public void toggleBlackCell() {
        blackCell = ! blackCell ;

        if (blackCell) {
            setBlackCell();
        } else {
            setWhiteCell();
        }

    }

    // During grid creation phase toggle black cell. During normal operation, swap clue orientation
    @Override
    public void onClick(View view) {
        if(gridMakingPhase) {
            Log.d("GridMakingPhase","Trying to toggleBlackCell()");
            toggleBlackCell();
            crossword.toggleOppositeBlackCell(this);    // Get crossword to toggleBlackCell of the cell rotationally opposite this one
            return ;
        }

        // If in crossword filling mode, and cell clicked, change clue orientation
        Log.d(LOG_TAG, " Cell click repeated, swapping clue highlight orientation");
        swapClueHighlightOrientation();

    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        this.setOnLongClickListener(listener);
    }

    public void incrementDecorationIndex() {
        decoration.incrementIndex();
    }

    public int getDecorationBG() {
        return decoration.getWhiteCellBG() ;
    }

    @Override
    public boolean onLongClick(View view) {

        Log.d(LOG_TAG, "Cell long clicked: (" + row + "," + column + ")");

        decoration.incrementIndex();

        this.setBackground(getResources().getDrawable(decoration.getWhiteCellBG()));

        return true;
    }

    // For highlighting active cell & clue during normal operation
    @Override
    public void onFocusChange(View view, boolean hasFocus){
        // To change cell highlight if cell has focus
        if (hasFocus) {
            Log.d(LOG_TAG, "Cell " + cellName + " has focus");

            crossword.clearCellHighlights();

            if (activeClue != null) {
                activeClue.highlightClue(this);
            } else {
                // Default to selecting horizontal clue if cell belongs to both a horizontal and vertical clue but isn't active
                if (hClue != null) {
                    hClue.highlightClue(this);
                } else if (vClue != null) {
                    vClue.highlightClue(this);
                } else {
                    Log.d(LOG_TAG, "Cell seems not to belong to a clue. Is the grid valid?");
                }
            }

            crossword.scrollToCell(this);

        } else {
            Log.d(LOG_TAG, "Cell " + cellName + " has lost focus");
        }
    }

    // For moving on to the next cell once a character has been entered
    @Override
    public void onTextChanged(CharSequence s, int start,int before, int count){
        if(activeClue != null) {
            if (this.getText().toString().length() == maxLength) {
                activeClue.highlightNextCell(this);
            }
        }
        if (hClue != null) hClue.checkClueComplete();
        if (vClue != null) vClue.checkClueComplete();

        if (activeClue != null && activeClue.isCompleted()) {
            // Hide the keyboard if the last cell in the clue is completed
            HomeActivity.hideKeyboard(getContext(), this);
            // Check whether this is the last clue to be completed
            crossword.checkIsComplete() ;
        }

    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

    }
    public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
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


    private void swapClueHighlightOrientation() {
        // If clue belongs to both horizontal and vertical clues, swap orientation
        if (this.hasHorizontalClue() && this.hasVerticalClue()) {
            if (activeClue.getClueOrientation() == Clue.HORIZONTAL_CLUE) {
                crossword.clearCellHighlights();
                vClue.highlightClue(this);
            } else if (activeClue.getClueOrientation() == Clue.VERTICAL_CLUE) {
                crossword.clearCellHighlights();
                hClue.highlightClue(this);
            }
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

    public String getCellName() {
        return cellName ;
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
    public void setHClue(Clue clue) {
        hClue = clue ;
    }
    public void setVClue(Clue clue) {
        vClue = clue ;
    }
    public boolean hasHorizontalClue() {
        if (hClue == null) {
            return false ;
        } else {
            return true ;
        }
    }
    public boolean hasVerticalClue() {
        if (vClue == null) {
            return false ;
        } else {
            return true ;
        }
    }
    public void clearHighlighting() {
        setWhiteCell();
    }
    public void setActiveClue(Clue clue) {
        activeClue = clue ;
    }

    /**
     * Method to check whether a cell is empty.
     *
     * @return Boolean of whether this cell is empty
     */
    public boolean isEmpty() {
        return this.getText().toString().isEmpty() ;
    }
}
