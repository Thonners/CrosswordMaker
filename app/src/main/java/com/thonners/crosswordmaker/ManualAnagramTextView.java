package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log ;


/**
 * Class to deal with locating the text views for each letter
 * @author M Thomas
 * @since 18/08/16
 */
public class ManualAnagramTextView extends TextView {

    private static final String LOG_TAG = "ManualAnagramTextView" ;

    // Constant dimensions (probably should move to dimens?)
    private final int DEFAULT_WIDTH = 15 ;
    private final int MAX_LETTERS_PER_CIRCLE = 6 ;
    private int letterSpacingRadius = 0 ;

    private int letterNo ;      // Letter number, base 0 (i.e. final letter will be letterCount - 1)
    private int letterCount ;   // Net letter number
    private int parentViewID;
    private RelativeLayout parentView ;
    private int anchorViewID ;
    private int leftMargin ;
    private int bottomMargin ;
    private boolean letterKnown = false;   // Whether the position of the letter is already known
    private String letter ;

    public ManualAnagramTextView(Context context) {
        super(context);
    }

    public ManualAnagramTextView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public ManualAnagramTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ManualAnagramTextView(Context context, char letterChar, int letterNo, int letterCount, RelativeLayout parentView, int anchorViewID) {
        super(context) ;
        this.letterNo = letterNo ;
        this.letterCount = letterCount ;
        this.parentViewID = parentView.getId() ;
        this.parentView = parentView ;
        this.anchorViewID = anchorViewID ;

        // Set the letter
        this.letter = Character.toString(letterChar) ;
        this.setText(letter);

        // Set the font size
        this.setTextSize(getResources().getDimensionPixelSize(R.dimen.manual_anagram_text_size));
        letterSpacingRadius = (int) (Math.max(((double) letterCount / MAX_LETTERS_PER_CIRCLE),1.0) * this.getTextSize()) ; // Ensure that the radius multiplier will be at least 1 if there are fewer than MAX_LETTERS_PER_CIRCLE letters
        // Initialise
        calculateMargins() ;

        // Set position
        setPosition();

        // Set the onClickListener in case the letter is known
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Letter clicked: " + letter) ;
                toggleLetterKnown();
            }
        });

    }

    public void setLetterNo(int letterNo) {
        this.letterNo = letterNo ;
    }

    public void setLetterCount(int letterCount) {
        this.letterCount = letterCount;
    }

    public void setPosition() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (letterNo == -1) { // Never happens, just keep here in case want to put a letter in the middle of the circle again.
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); // Not sure the parent view is required here
        } else {
            params.leftMargin = leftMargin;
            params.bottomMargin = bottomMargin;

            params.addRule(RelativeLayout.RIGHT_OF, anchorViewID);
            params.addRule(RelativeLayout.ABOVE, anchorViewID);

            // Set params (this causes the view to update)
            this.setLayoutParams(params);
        }

        parentView.addView(this, params);

    }

    private void calculateMargins() {
        // Don't think these are needed, since it's always the bottom left corner being reference (both anchor and this view)
        double midpointOffsetX = this.getTextSize() / 2 ; // To be added to the offset to move the spacing from the bottom corner to the mid point of the view (Assuming it is the same size)
        double midpointOffsetY = this.getTextSize() / 2 ;

        // Calculate angle between each letter. LetterCount must be greater than 2 else a /0 error will occur. TODO: Put this check in the calling function which creates this instance.
        double dTheta = 2 * Math.PI / (letterCount) ;
        // Calculate theta (angle from dead above anchorView
        double theta = (letterNo - 1) * dTheta ;
        // Get margins from angle and radius
        leftMargin = (int) ((Math.sin(theta) * letterSpacingRadius) - midpointOffsetX);
        bottomMargin = (int) ((Math.cos(theta) * letterSpacingRadius) - midpointOffsetY);
    }

    public void toggleLetterKnown() {
        // Toggle value of letterKnown
        letterKnown = !letterKnown ;
        // Strikethough letter
        if (letterKnown) {
            this.setPaintFlags(this.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            this.setPaintFlags(this.getPaintFlags() & ~ Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    public String getLetter() {
        return letter ;
    }
}