package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mat on 20/04/15.
 */
public class Card extends CardView {

    private Context context;
    private RelativeLayout layout;
    private int padding;

    private static final int DICTIONARY_WORD_NOT_FOUND  = -1 ;
    private static final int DICTIONARY_WORD            = 0;
    private static final int DICTIONARY_WORD_TYPE       = 1;
    private static final int DICTIONARY_DEFINITION      = 2;
    private static final int DICTIONARY_SUGGESTIONS     = 3;
    private static final int ANAGRAM_ANSWER             = 4;

    // Constructors
    public Card(Context context, XmlParser.Entry dictionaryEntry) {
        super(context);
        initialise(context);

        createMainTextView(dictionaryEntry.getWord());
    }

    private void initialise(Context context) {
        // Initialise all the views, etc.
        this.context = context;
        padding = context.getResources().getDimensionPixelOffset(R.dimen.home_card_padding);

        // CardView
        this.setUseCompatPadding(true);
        // Layout
        layout = new RelativeLayout(context);
        layout.setPadding(padding, padding, padding, padding);
        // Provide feedback when button pressed. Pretty rubbish atm. Could be improved.
        TypedValue outValue  = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,outValue,true);
        layout.setBackgroundResource(outValue.resourceId);

        this.addView(layout);
    }

    private TextView createMainTextView(String mainText) {
        // Create main text view
        TextView mainTV = new TextView(context);
        mainTV.setText(mainText);
        mainTV.setTextSize(context.getResources().getDimension(R.dimen.main_text_view));
        mainTV.setTypeface(null, Typeface.BOLD);

        return mainTV;
    }

    private TextView createMinorTextView(String minorText) {
        // Create minor text view
        TextView mainTV = new TextView(context);
        mainTV.setText(minorText);
        mainTV.setTextSize(context.getResources().getDimension(R.dimen.minor_text_view));
        mainTV.setTypeface(null, Typeface.BOLD);

        return mainTV;

    }

    private TextView createTextView(int displayType) {
        TextView tv = new TextView(context);
        float textSize = 0 ;

        // Get settings for each word type
        switch(displayType){
            case DICTIONARY_WORD:  // Bold
                tv.setTypeface(null, Typeface.BOLD);
                textSize = context.getResources().getDimension(R.dimen.dictionary_word);
                break;
            case DICTIONARY_WORD_TYPE: // Italic
                tv.setTypeface(null, Typeface.ITALIC);
                textSize = context.getResources().getDimension(R.dimen.dictionary_word_type);
                break;
            case DICTIONARY_DEFINITION: // Normal
                textSize = context.getResources().getDimension(R.dimen.dictionary_definition);
                break;
            case DICTIONARY_WORD_NOT_FOUND: // Bold + bigger?
                tv.setTypeface(null, Typeface.BOLD);
                textSize = context.getResources().getDimension(R.dimen.dictionary_word_not_found);
                break;
            case DICTIONARY_SUGGESTIONS: // Normal?
                textSize = context.getResources().getDimension(R.dimen.dictionary_suggestion);
                break;
            case ANAGRAM_ANSWER:
                tv.setTypeface(null, Typeface.BOLD);
                textSize = context.getResources().getDimension(R.dimen.main_text_view);
                break;
        }
        // Set size
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);

        return tv ;
    }

    public RelativeLayout getRelativeLayout() {
        return layout;
    }
}
