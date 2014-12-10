package com.thonners.crosswordmaker;

import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

/**
 * Created by mat on 30/11/14.
 */
public class Cell extends EditText implements View.OnClickListener {

    public TextView cell1;

    public int row ;
    public int column ;

    private int maxLength = 1 ; // Max number of letters in the editText

    public boolean blackCell ;
    public Character value ;
    public boolean gridMakingPhase = true ;    // If gridMakingPhase, clicking on a cell changes it from black to white

    // Initialisers
    // For creating the initial grid, all cells start life as white cells
    public Cell(Context context, int r, int c) {
        super(context);

        cell1 = new TextView(context);

        row = r ;
        column = c ;
        blackCell = false;

        this.setBackground(getResources().getDrawable(R.drawable.border));
        this.setAllCaps(true);
        this.setTextColor(context.getResources().getColor(R.color.black));
        this.setClickable(true);
        this.setFocusable(false);

        this.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        setOnClickListener(this);
    }

    // Maybe for creating the grid after user has made choices (/when camera has worked it out)
    public Cell(Context context, int r, int c, boolean blackCellIn) {
        super(context);

        cell1 = new TextView(context);

        row = r ;
        column = c ;
        blackCell = blackCellIn;

        this.setBackground(getResources().getDrawable(R.drawable.border));
    }

    private void setBlackCell() {
        this.setBackgroundColor(getResources().getColor(R.color.black));
 /*       this.setClickable(false);               // Not sure whether clickable or selectable
        this.setTextIsSelectable(false);
        this.setFocusable(false);*/
    }
    private void setWhiteCell() {
        this.setBackground(getResources().getDrawable(R.drawable.border));
 /*       this.setClickable(false);               // Not sure whether clickable or selectable
        this.setTextIsSelectable(false);
        this.setFocusable(false); */
    }

    public void toggleBlackCell() {
        blackCell = ! blackCell ;

        if (blackCell) {
            setBlackCell();
        } else {
            setWhiteCell();
        }
    }

    @Override
    public void onClick(View view) {
        if(gridMakingPhase) {
            Log.d("GridMakingPhase","Trying to toggleBlackCell()");
            toggleBlackCell();
            return ;
        }

    }

    public void setGridMakingPhase(Boolean b){
        gridMakingPhase = b ;
    }

    public void setWhiteCellsEditable() {
        if (! blackCell) {
            // Set focusable if white cell so that text can be edited
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);
        } else {
            // Set not clickable if black cell
            this.setClickable(false);
        }
    }


}
