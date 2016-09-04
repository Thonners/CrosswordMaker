package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Class to be used in the known letters layout at the bottom of the manual anagram page.
 *
 * Cardview to display hang-man like spaces, which the user can fill if letters are known.
 *
 * @author M Thomas
 * @since 23/08/16
 */
public class ManualAnagramKnownLetterCardView extends CardView {

    private final String LOG_TAG = "MAKnownCard" ;

    private LinearLayout parentView ;
    private View mainView ;
    private TextView tv ;
    private String letter  = " ";
    private int index ;
    private boolean isActive  = false ;

    private ManualAnagramTextView associatedTV = null;

    /**
     * Constructor.
     * @param context   Application context
     * @param index     Index number of this view in the string of the answer.
     */
    public ManualAnagramKnownLetterCardView(Context context, int index) {
        super(context);
        Log.d(LOG_TAG,"KnownCard created.");
        this.index = index ;
        initialise(context);
    }

    public ManualAnagramKnownLetterCardView(Context context) {
        super(context);
        initialise(context);
    }
    public ManualAnagramKnownLetterCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ManualAnagramKnownLetterCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Method to initialise the view.
     * TODO: Move the textView params to the xml.
     * @param context   Application context
     */
    public void initialise(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f) ;
        params.leftMargin = 2 ;
        params.rightMargin = 2 ;
        params.bottomMargin = 3;
        this.setLayoutParams(params);
        tv = (TextView) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.manual_anagram_known_letters_card_view, null);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) ;
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        this.addView(tv,tvParams) ;
        clearLetter();
    }
    public void setParentView(LinearLayout parentView) {
        this.parentView = parentView ;
    }

    public void setLetter(String letter) {
        this.letter = letter ;
        tv.setText(letter);
    }
    public void setLetter(ManualAnagramTextView textView) {
        associatedTV = textView ;
        associatedTV.setLetterKnown();
        letter = associatedTV.getLetter();
        tv.setText(letter);
    }

    /**
     * Short-cut method to set this view as active.
     */
    public void setIsActive() {
        setIsActive(true);
    }

    /**
     * Method to provide visual feedback to the user that this view is active - i.e. clicking a
     * letter's textView will result in that letter being assigned to this view.
     *
     * Show active by raising elevation of the card, or changing background colour, depending on
     * android version.
     *
     * If no longer active, clear feedback and reset to normal.
     */
    public void setIsActive(boolean isActive) {
        if (isActive) {
            // View is active, so provide visual feedback to user
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.setElevation(getResources().getDimension(R.dimen.manual_anagram_card_elevation_active));
            } else {
                this.setBackgroundColor(getResources().getColor(R.color.cell_highlighted));
            }
        } else {
            // Not active, so clear highlighting
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.setElevation(getResources().getDimension(R.dimen.manual_anagram_card_elevation_resting));
            } else {
                this.setBackgroundColor(getResources().getColor(R.color.card_background));
            }
        }
    }

    public void clearLetter() {
        if (associatedTV != null) {
            associatedTV.setLetterKnown(false);
        }
        associatedTV = null ;
        this.letter = " " ;
        tv.setText(" ");
    }

    public int getIndex() {
        return index ;
    }

    public boolean isEmpty() {
        return this.letter.trim().matches("");
    }

}
