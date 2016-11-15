package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Class to manage the entries to the clue checklist, under the crossword on the main crossword fragment.
 *
 * @author M Thomas
 * @since 15/11/16
 */

public class ClueChecklistEntryTextView extends TextView {

    private Context context ;
    private Clue clue ;
    private boolean isChecked = false ;   // Variable to track whether this text should be displayed with a strikethrough or not

    /**
     * Default Constructors, for use in xml
     * @param context
     */
    public ClueChecklistEntryTextView(Context context) {
        super(context) ;
    }
    public ClueChecklistEntryTextView(Context context, AttributeSet attrs) {
        super(context, attrs) ;
    }
    public ClueChecklistEntryTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle) ;
    }

    /**
     * Constructor - for use when creating instances programatically.
     * This should be the only constructor used
     * @param context Application context
     * @param clue The clue associated with this view
     */
    public ClueChecklistEntryTextView(Context context, Clue clue) {
        super(context) ;
        this.context = context ;
        this.clue = clue ;
        initialise();
    }

    /**
     * Simple method to initialise the variables / appearance of the TextView
     */
    private void initialise() {
        // Set the number
        this.setText(clue.getClueDisplayNumber() + "). ");
        // Set the style
        this.setTextAppearance(context, R.style.clues_checklist);
    }


    /**
     * Method to toggle the strike-through of this view
     */
    public void toggleChecked() {
        setChecked(!isChecked) ;
    }

    /**
     * Method to set whether this view should be checked, and to set the text appearance appropriately
     * @param checked Whether the text in this view should appear with a strikethrough
     */
    public void setChecked(boolean checked) {
        isChecked = checked ;

        // Set the appearance
        if (isChecked) {
            // Set strikethrough
            this.setPaintFlags(this.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // Set back to normal
            this.setPaintFlags(this.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    /**
     * @param clue The clue associated to this checklist entry
     */
    public void setClue(Clue clue) {
        this.clue = clue ;
    }

}
