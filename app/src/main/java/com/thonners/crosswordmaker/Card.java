package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Typeface;
import androidx.cardview.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mat on 20/04/15.
 */
public class Card extends CardView {

    private Context context;
    private RelativeLayout layout;
    private int padding;
    private boolean cardSelected = false ;

    private static final int DICTIONARY_WORD_NOT_FOUND  = -1 ;
    private static final int DICTIONARY_WORD            = 0;
    private static final int DICTIONARY_WORD_TYPE       = 1;
    private static final int DICTIONARY_DEFINITION      = 2;
    private static final int DICTIONARY_SUGGESTION      = 3;
    private static final int ANAGRAM_ANSWER             = 4;
    private static final int CROSSWORD_TITLE            = 5;
    private static final int CROSSWORD_DATE             = 6;
    private static final int CROSSWORD_PERCENTAGE       = 7;

    TextView suggestionTextView;

    // Constructors
    // Home page Cards

    // Saved crossword
    public Card (Context context, String crosswordTitle, String crosswordDate, String crosswordPercentage) {
        super(context);
        initialise(context);

        this.setMinimumHeight(context.getResources().getDimensionPixelOffset(R.dimen.home_card_default_height));

        // Create text views
        TextView titleTV = createTextView(CROSSWORD_TITLE, crosswordTitle);
        TextView dateTV = createTextView(CROSSWORD_DATE, crosswordDate);
        TextView percentageTV = createTextView(CROSSWORD_PERCENTAGE, crosswordPercentage);

        // Create layout parameters
        // Title
        titleTV.setId(1000);
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        // Date
        RelativeLayout.LayoutParams dateLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        dateLP.addRule(RelativeLayout.BELOW, titleTV.getId());
        // Percentage
        RelativeLayout.LayoutParams percentageLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        percentageLP.addRule(RelativeLayout.BELOW, titleTV.getId());
        percentageLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        // Add to layout
        layout.addView(titleTV, titleLP);
        layout.addView(dateTV, dateLP);
        layout.addView(percentageTV, percentageLP);
    }
    // Dictionary Results
    public Card(Context context, XmlParser.Entry dictionaryEntry) {
        super(context);
        initialise(context);

        this.addView(createTextView(DICTIONARY_WORD, dictionaryEntry.getWord()));
    }
    // Dictionary Suggestions
    public Card(Context context, String suggestion) {
        super(context);
        initialise(context);
        // Create the text view
        suggestionTextView = createTextView(DICTIONARY_SUGGESTION, suggestion);
        layout.addView(suggestionTextView);     // Add to layout to ensure proper padding, etc.
        this.setVisibility(INVISIBLE);          // Set invisible to allow user to show if desired.
    }
    // Anagram/Word-fit results

    //

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
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        layout.setBackgroundResource(outValue.resourceId);

        this.addView(layout);
    }


    private TextView createTextView(int displayType, String textToDisplay) {
        TextView tv = new TextView(context);
        tv.setTextColor(getResources().getColor(R.color.text_default));

        // Get settings for each word type
        switch(displayType){
            case DICTIONARY_WORD:  // Bold
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.dictionary_word));
                break;
            case DICTIONARY_WORD_TYPE: // Italic
                tv.setTypeface(null, Typeface.ITALIC);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.dictionary_word_type));
                break;
            case DICTIONARY_DEFINITION: // Normal
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.dictionary_definition));
                break;
            case DICTIONARY_WORD_NOT_FOUND: // Bold
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.dictionary_word_not_found));
                break;
            case DICTIONARY_SUGGESTION: // Normal?
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.dictionary_suggestion));
                break;
            case ANAGRAM_ANSWER:
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.main_text_view));
                break;
            case CROSSWORD_TITLE: // Bold
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.home_card_text_size_main));
                break;
            case CROSSWORD_DATE: // Normal
                tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.home_card_text_size_minor));
                break;
            case CROSSWORD_PERCENTAGE: //Italic
                tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tv.setTypeface(null, Typeface.ITALIC);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.home_card_text_size_minor));
                break;
        }

        // Set text
        tv.setText(textToDisplay);

        return tv ;
    }

    public String getSuggestionText(){
        if (suggestionTextView != null) {
            return suggestionTextView.getText().toString();
        } else {
            return null;
        }
    }
    public RelativeLayout getRelativeLayout() {
        return layout;
    }
    public boolean getCardSelected() {
        return cardSelected ;
    }
    public void toggleCardSelected() {
        if (cardSelected) {
            cardSelected = false ;
        } else {
            cardSelected = true ;
        }
    }
    public void setCardDeselected() {
        cardSelected = false ;
    }
}
