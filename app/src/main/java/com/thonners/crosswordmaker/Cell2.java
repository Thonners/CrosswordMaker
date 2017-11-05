package com.thonners.crosswordmaker;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Class to hold all details for a cell.
 *
 * Refactored version to clean up original code
 *
 * @author M Thomas
 * @since 31/07/17
 */
public class Cell2 extends RelativeLayout implements View.OnClickListener, View.OnFocusChangeListener , TextWatcher {

    private enum CellPhase{
        GRID_CREATE, GRID_EDIT, SOLVE
    }

    private int row ;
    private int column ;
    private boolean blackCell = false ;
    private CellPhase cellPhase = CellPhase.SOLVE; // Default to solve

    private CellInteractionListener mListener = null ;

    private TextView clueNoTV = null ;
    private EditText cellET = null ;

    public interface CellInteractionListener {
        void selectNextCell(int currentRow, int currentCol) ;
        void selectPreviousCell(int currentRow, int currentCol) ;
        void setOppositeCellColour(int currentRow, int currentCol, boolean setBlack);
    }

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


    public Cell2(Context context) {
        super(context);
    }

    public Cell2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Cell2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Cell2(Context context, CellInteractionListener listener, int row, int col) {
        super(context);
        this.mListener = listener ;
        this.row = row ;
        this.column = col ;

        this.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cell_white));
        this.setClickable(true);
        this.setFocusable(false);
        this.setFocusableInTouchMode(false);
        this.setPadding(0,0,0,0);
        this.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        // If gridEditPhase, toggle to black
        switch (cellPhase) {
            case GRID_CREATE:
                toggleBlackCell();
                mListener.setOppositeCellColour(row,column,blackCell);
                break;
            case GRID_EDIT:
                toggleBlackCell();
                break;
            case SOLVE:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    /**
     * Method to toggle status of cell from white to black and vica versa.
     */
    public void toggleBlackCell() {
        // Toggle status
        blackCell = !blackCell ;
        // Set background
        if (blackCell) {
            setCellBackground(R.drawable.cell_black);
        } else {
            setCellBackground(R.drawable.cell_white);
        }
    }

    /**
     * Simple method to set the cell's background drawable
     * @param rDrawableResource The R.drawable resource to set as the cell's background
     */
    private void setCellBackground(int rDrawableResource) {
        this.setBackground(ContextCompat.getDrawable(getContext(),rDrawableResource));
    }

    public boolean isBlackCell() {
        return blackCell;
    }

    public void takeFocus() {

    }
}
