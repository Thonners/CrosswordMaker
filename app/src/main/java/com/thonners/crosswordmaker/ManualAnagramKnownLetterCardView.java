package com.thonners.crosswordmaker;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Class to be used in the known letters layout at the bottom of the manual anagram page.
 *
 * Cardview to display hang-man like spaces, which the user can fill if letters are known.
 *
 * @author M Thomas
 * @since 23/08/16
 */
public class ManualAnagramKnownLetterCardView extends CardView {

    public ManualAnagramKnownLetterCardView(Context context) {
        super(context);
    }
    public ManualAnagramKnownLetterCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ManualAnagramKnownLetterCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
