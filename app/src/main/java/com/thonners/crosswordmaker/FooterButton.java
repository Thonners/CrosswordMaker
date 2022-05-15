package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import androidx.cardview.widget.CardView;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 *  Footer button, to display some text at the bottom of the layout to which it is added.
 *  Create an instance of the button with the text you'd like to display, then add it to the relevant RelativeLayout.
 *
 * Created by Thonners on 04/07/15.
 */

public class FooterButton extends CardView{
    Context context ;
    TextView tv ;
    RelativeLayout buttonRL;

    public FooterButton(Context context, String textToDisplay) {
        super(context);
        this.context = context ;

        initialise();
        setText(textToDisplay);
    }

    private void initialise() {
        this.setBackgroundColor(getResources().getColor(R.color.light_grey));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        this.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        this.setLayoutParams(layoutParams);

        // Relative Layout for card
        buttonRL = new RelativeLayout(context);
        this.addView(buttonRL);
        buttonRL.setPadding(getResources().getDimensionPixelOffset(R.dimen.edit_button_padding), getResources().getDimensionPixelOffset(R.dimen.edit_button_padding), getResources().getDimensionPixelOffset(R.dimen.edit_button_padding), getResources().getDimensionPixelOffset(R.dimen.edit_button_padding));
        // Text view
        tv = new TextView(context);
        RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT) ;
        tvlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tv.setLayoutParams(tvlp);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.home_card_text_size_main));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextColor(getResources().getColor(R.color.white));
        buttonRL.addView(tv);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setCardElevation(getResources().getDimension(R.dimen.z_card_raised));
        }
        this.setVisibility(View.INVISIBLE); // Start invisible
    }

    public void setText(String textToDisplay) {
        tv.setText(textToDisplay);
    }

}
