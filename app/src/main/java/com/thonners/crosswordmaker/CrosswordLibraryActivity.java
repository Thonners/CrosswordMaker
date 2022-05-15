package com.thonners.crosswordmaker;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * CrosswordLibraryActivity Activity
 * Shows a list of all the crosswords saved by the user on their device.
 * Shows completion % of the crossword, calculated as number of filled spaces over total number of white spaces
 *
 * Created by Thonners on 06/05/15.
 */

public class CrosswordLibraryActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CrosswordLibActivity" ;

    CrosswordLibraryManager libraryManager ;
    RelativeLayout mainLayout ;
    RelativeLayout layout ;
    CardView editButton;
    int editCrosswordIndex = -1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_crossword_selector);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainLayout = (RelativeLayout) findViewById(R.id.saved_crossword_main_layout);
        layout = (RelativeLayout) findViewById(R.id.saved_crosswords_r_layout)  ;
        createEditButton();

        libraryManager = new CrosswordLibraryManager(this);

        int i = 1;
        for (CrosswordLibraryManager.SavedCrossword savedCrossword : libraryManager.getSavedCrosswords()) {
            addCrosswordToLayout(i, savedCrossword);
            i++;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_crossword_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_edit_delete:
                // Show the tutorial toast
                showEditTutorialToast();
                break;
            case R.id.action_feedback:
                // Send an email
                HomeActivity.emailDeveloperFeedback(this);
                break;
            case R.id.action_about:
                // Show 'About' Dialog
                HomeActivity.showAboutDialog(this);
                break;
            case R.id.action_settings:
                // Open some settings menu
                HomeActivity.openSettings(this);
                break ;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addCrosswordToLayout(int index, CrosswordLibraryManager.SavedCrossword savedCrossword) {

        Card card = new Card(getApplicationContext(),savedCrossword.getTitle(),savedCrossword.getDisplayDate(),savedCrossword.getDisplayPercentageComplete()) ;
        card.setId(index);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crosswordSelected(v);
            }
        });
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleCardSelection(v);

                return true;
            }
        });
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT );
        if(index == 1) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, index - 1);
        }
        card.setLayoutParams(layoutParams);
        layout.addView(card);

    }
    private void crosswordSelected(View view) {
        int i = view.getId() - 1;  // get index of save file
        // View index starts at 1, but file index at 0, so need to -1
        Log.d(LOG_TAG, "Crossword selected: " + libraryManager.getSavedCrosswords().get(i).getTitle());
        libraryManager.openCrossword(libraryManager.getSavedCrosswords().get(i).getCrosswordDir());
    }

    private void toggleCardSelection(View view) {
        // Method to toggle whether card is highlighted (i.e. raised) after long click
        Card card = (Card) view ;
        card.toggleCardSelected();

        if (card.getCardSelected()) {
            // Get index of selected card
            editCrosswordIndex = view.getId() ;
            Log.d(LOG_TAG, "editCardIndex = " + view.getId());

            selectCard(card);
        } else {
            // Set index to <0 to imply that no card is selected
            editCrosswordIndex = -1 ;
            Log.d(LOG_TAG, "editCardIndex = -1");

            deselectCard(card);

        }

    toggleEditDeleteButtons();

    }

    private void selectCard(Card card) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Lower all views (in case another was selected)
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View v = layout.getChildAt(i);
                    if (v instanceof Card) {
                        v.setElevation(getResources().getDimension(R.dimen.z_card_default));
                        ((Card) v).setCardDeselected();
                    }
                }
                // Set selected view to raised elevation
                card.setElevation(getResources().getDimension(R.dimen.z_library_card_highlighted));
                card.toggleCardSelected();  // Put it back to selected as it's turned off by the for loop above
            } else {
                card.setBackgroundColor(getResources().getColor(R.color.light_grey));
            }
    }
    private void deselectCard(Card card) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Lower selected view as it's already raised
                card.setElevation(getResources().getDimension(R.dimen.z_card_default));
            } else {
                card.setBackgroundColor(getResources().getColor(R.color.white));
            }

    }

    private void toggleEditDeleteButtons() {
        // Method to show or hide the edit and delete buttons
        if (editCrosswordIndex < 0) {
            editButton.setVisibility(View.INVISIBLE);
        } else {
            editButton.setVisibility(View.VISIBLE);
        }
    }

    private void createEditButton() {
        editButton = new FooterButton(this, getResources().getString(R.string.edit));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCrossword();
            }
        });

        mainLayout.addView(editButton);
    }

    private void editCrossword() {
        // Launch new activity to edit the selected crossword
        if (editCrosswordIndex < 0) {
            Log.d(LOG_TAG, "editCrosswordIndex < 0 - not doing anything (why is this method being called???!?!?!");
        } else {
            // TODO: Launch edit activity
            Log.d(LOG_TAG, "Opening edit task for: " + libraryManager.getSavedCrosswords().get(editCrosswordIndex).getTitle());
            libraryManager.openEditCrossword(libraryManager.getSavedCrosswords().get(editCrosswordIndex).getCrosswordDir());

        }
    }

    private void showEditTutorialToast() {
        Toast.makeText(this,getResources().getString(R.string.edit_delete_tutorial),Toast.LENGTH_LONG).show();
    }

}
