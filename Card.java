package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mat on 20/04/15.
 */
public class Card {

    private Context context ;
    private CardView cardView ;
    private RelativeLayout layout ;
    private int padding ;

    // Constructors
    public Card(Context context, XmlParser.Entry dictionaryEntry) {
        initialise(context);

        createMainTextView(dictionaryEntry.getWord());
    }

    private void initialise(Context context) {
        // Initialise all the views, etc.
        this.context = context;
        padding = context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding);

        // CardView
        cardView = new CardView(context);
        cardView.setUseCompatPadding(true);
        // Layout
        layout = new RelativeLayout(context);
        layout.setPadding(padding,padding,padding,padding);

        cardView.addView(layout);
    }

    private void createMainTextView(String mainText){
        // Create main text view
        TextView mainTV = new TextView(context);
        mainTV.setText(mainText);
        mainTV.setTextSize(context.getResources().getDimension(R.dimen.main_text_view));
        mainTV.setTypeface(null, Typeface.BOLD);
    }

    public CardView getCardView() {
        return cardView;
    }
    public RelativeLayout getRelativeLayout() {
        return layout;
    }
}
