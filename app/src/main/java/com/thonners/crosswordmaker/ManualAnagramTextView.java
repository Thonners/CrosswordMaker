package com.thonners.crosswordmaker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Class to deal with locating the text views for each letter
 * @author M Thomas
 * @since 18/08/16
 */
public class ManualAnagramTextView extends TextView {

    // Constant dimensions (probably should move to dimens?)
    private final int DEFAULT_WIDTH = 8 ;
    private final int DEFAULT_HEIGHT = 8 ;
    private final int DEFAULT_RADIUS = 32 ;

    private int letterNo ;      // Letter number, base 0 (i.e. final letter will be letterCount - 1)
    private int letterCount ;   // Net letter number
    private int parentViewID;
    private int anchorViewID ;
    private int leftMargin ;
    private int bottomMargin ;

    public ManualAnagramTextView(Context context) {
        super(context);
    }

    public ManualAnagramTextView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public ManualAnagramTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ManualAnagramTextView(Context context, int letterNo, int letterCount, RelativeLayout parentViewID, ManualAnagramTextView anchorView) {
        super(context) ;
        this.letterNo = letterNo ;
        this.letterCount = letterCount ;
        this.parentViewID = parentViewID.getId() ;
        anchorViewID = anchorView.getId() ;

        // Initialise
        calculateMargins() ;

        // Set position
        setPosition();
    }

    public void setLetterNo(int letterNo) {
        this.letterNo = letterNo ;
    }

    public void setLetterCount(int letterCount) {
        this.letterCount = letterCount;
    }

    public void setPosition() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(8, 8);
        if (letterNo == 0) {
            params.addRule(RelativeLayout.CENTER_IN_PARENT, parentViewID); // Not sure the parent view is required here
        } else {
            params.leftMargin = leftMargin;
            params.bottomMargin = bottomMargin;

            params.addRule(RelativeLayout.RIGHT_OF, anchorViewID);

            // Set params (this causes the view to update)
            this.setLayoutParams(params);
        }
    }

    private void calculateMargins() {
        // Dont think these are needed, since it's always the bottom left corner being reference (both anchor and this view)
        int midpointOffsetX = DEFAULT_WIDTH / 2 ; // To be added to the offset to move the spacing from the bottom corner to the mid point of the view (Assuming it is the same size)
        int midpointOffsetY = DEFAULT_HEIGHT / 2 ;

        // Calculate angle between each letter. LetterCount must be greater than 2 else a /0 error will occur. TODO: Put this check in the calling function which creates this instance.
        double dTheta = 2 * Math.PI / (letterCount - 1) ;
        // Calculate theta (angle from dead above anchorView
        double theta = (letterNo - 1) * dTheta ;
        // Get margins from angle and radius
        leftMargin = (int) (Math.sin(theta) * DEFAULT_RADIUS);
        bottomMargin = (int) (Math.cos(theta) * DEFAULT_RADIUS);
    }
}
