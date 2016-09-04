package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
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

    /**
     * Main constructor.
     * @param context       Application context
     * @param letterChar    Character, i.e. letter, to assign to this view.
     * @param letterNo      The index number of this letter in relation to the other letters in the anagram. Used to position it in the layout.
     * @param letterCount   The total number of letters in the anagram. Used to determine spacing of letters in results.
     * @param parentView    The view this textView is to be added to.
     * @param anchorViewID  The resource ID of the anchor view - as this will be positioned relative to that.
     */
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
        // Letter spacing radius determines the size of the circle in which the letters will be displayed. Circle size grows as letter count grows, to keep sensible spacing between letters.
        letterSpacingRadius = (int) (Math.max(((double) letterCount / MAX_LETTERS_PER_CIRCLE),1.0) * this.getTextSize()) ; // Ensure that the radius multiplier will be at least 1 if there are fewer than MAX_LETTERS_PER_CIRCLE letters
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

    /**
     * Creates the layout parameters and assigns them to the view. Uses the margins calculated in
     * the calculateMargins() method.
     */
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

    /**
     * Method to calculate the margins to be applied to this text view, when positioned in the results layout.
     * The margins determine this view's position.
     *
     * The calculation derives the margins by working out the x/y coordinates of points around a circle.
     * The circle's radius is calculated in the constructor to ensure sensible spacing of the letters.
     * This view's letterNumber is used to determine how far around the circle this particular view
     * will be positioned; letterNo * 360 / letterCount, to give angle between vertical and position
     * vector of this view.
     */
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

    /**
     * Shortcut method to set letterKnown to true. Calls setLetterKnown(true).
     */
    public void setLetterKnown() {
        setLetterKnown(true);
    }

    /**
     * Method to set whether this textView's letter is known.
     * If letter is known, make text faded to remove it from user's focus in results view.
     * If letter is not known, return it to normal appearance.
     * @param isKnown Whether the position in the answer of the letter this text view is for is known by the user.
     */
    public void setLetterKnown(boolean isKnown) {
        letterKnown = isKnown ;
        if (letterKnown) {
            this.setAlpha(0.3f);
            //this.setTextColor(getResources().getColor(R.color.light_grey));
        } else {
            this.setAlpha(1.0f);
            //this.setTextColor(getResources().getColor(R.color.dark_grey));
        }
    }

    public void setIsActive() {
        setIsActive(true) ;
    }

    /**
     * Method to provide visual feedback to user that the letter is active - i.e. that pressing a
     * known letter card next will assign this letter to that card. If no longer active, reset the
     * appearance of the card.
     * @param isActive Whether this card is active or not.
     */
    public void setIsActive(boolean isActive) {
        if (isActive) {
            this.setTypeface(null, Typeface.BOLD);
        } else {
            this.setTypeface(null, Typeface.NORMAL);
        }
    }

    public String getLetter() {
        return letter ;
    }

    public int getLetterNo() {
        return letterNo;
    }

    public boolean isKnown() {
        return letterKnown ;
    }
}
