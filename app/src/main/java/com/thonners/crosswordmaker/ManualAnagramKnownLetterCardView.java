package com.thonners.crosswordmaker;

import android.content.Context;
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
    private String letter ;
    private int index ;

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

    public void initialise(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) ;
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
        tv.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        tv.setText(letter);
    }
    public void clearLetter() {
        this.letter = " " ;
        tv.setText(" ");
    }

    public int getIndex() {
        return index ;
    }

}
