package com.thonners.crosswordmaker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;


public class HomeActivity extends ActionBarActivity {

    private static final String LOG_TAG = "HomeActivity";

    private String publication;
    private String date ;
    private CharSequence[] publications ;
    private boolean safeToOverwrite = false ;

    private CrosswordLibraryManager libraryManager ;
    private ArrayList<CrosswordLibraryManager.SavedCrossword> recentCrosswords ;

    // For displayDate picker
    Calendar c = Calendar.getInstance();
    int startYear = c.get(Calendar.YEAR);
    int startMonth = c.get(Calendar.MONTH);
    int startDay = c.get(Calendar.DAY_OF_MONTH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_material);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecentCrosswords();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_feedback:
                // Send an email
                emailDeveloperFeedback(this);
                break;
            case R.id.action_about:
                // Show 'About' Dialog
                showAboutDialog(this);
                break;
            case R.id.action_settings:
                // Open some settings menu
                openSettings(this);
                break ;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateRecentCrosswords() {
        // Update recent crosswords list
        Log.d(LOG_TAG,"Searching for recent crossword file... ");
        libraryManager = new CrosswordLibraryManager(this);
        recentCrosswords = libraryManager.getRecentCrosswords();
        switch (recentCrosswords.size())  {
            case 3:
                TextView recentTV3Title = (TextView) findViewById(R.id.home_card_recent_3_title);
                TextView recentTV3Date = (TextView) findViewById(R.id.home_card_recent_3_date);
                CardView recentCard3 = (CardView) findViewById(R.id.home_card_recent_3);
                recentTV3Title.setText(recentCrosswords.get(2).getTitle());
                recentTV3Date.setText(recentCrosswords.get(2).getDisplayDate());
                recentCard3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        libraryManager.openCrossword(recentCrosswords.get(2).crosswordFile);
                    }
                });
                //      recentCard3.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            case 2:
                TextView recentTV2Title = (TextView) findViewById(R.id.home_card_recent_2_title);
                TextView recentTV2Date = (TextView) findViewById(R.id.home_card_recent_2_date);
                CardView recentCard2 = (CardView) findViewById(R.id.home_card_recent_2);
                recentTV2Title.setText(recentCrosswords.get(1).getTitle());
                recentTV2Date.setText(recentCrosswords.get(1).getDisplayDate());
                recentCard2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        libraryManager.openCrossword(recentCrosswords.get(1).crosswordFile);
                    }
                });
                //        recentCard2.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            case 1:
                TextView recentTV1Title = (TextView) findViewById(R.id.home_card_recent_1_title);
                TextView recentTV1Date = (TextView) findViewById(R.id.home_card_recent_1_date);
                recentTV1Title.setText(recentCrosswords.get(0).getTitle());
                recentTV1Date.setText(recentCrosswords.get(0).getDisplayDate());
                CardView recentCard1 = (CardView) findViewById(R.id.home_card_recent_1);
                recentCard1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        libraryManager.openCrossword(recentCrosswords.get(0).crosswordFile);
                    }
                });
                break;
        }


        RelativeLayout recentLayout = (RelativeLayout) findViewById(R.id.home_card_recent_layout);
        switch (recentCrosswords.size())  {
            case 0:
                // Set no recent text
                TextView recentTV1Title = (TextView) findViewById(R.id.home_card_recent_1_title);
                TextView recentTV1Date = (TextView) findViewById(R.id.home_card_recent_1_date);
                recentTV1Title.setText(getResources().getString(R.string.home_recent_none1));
                recentTV1Date.setText(getResources().getString(R.string.home_recent_none2));
            case 1:
                // Remove 2nd box
                CardView recentCard2 = (CardView) findViewById(R.id.home_card_recent_2);
                recentLayout.removeView(recentCard2);
            case 2:
                // Remove 3rd box
                CardView recentCard3 = (CardView) findViewById(R.id.home_card_recent_3);
                recentLayout.removeView(recentCard3);
                break;
        }
    }

    public void toolkitClicked(View view) {
        // Open just a toolkit activity
        Intent intent = new Intent(this, ToolkitSliderActivity.class);
        startActivity(intent);
    }

    public void savedClicked(View view) {
        // Toast to say do new one
        Intent intent = new Intent(this, SavedCrosswordSelector.class);
        startActivity(intent);

    }

    public void newClicked(View view) {
        // Open something to let the person enter the details
        publications = getResources().getTextArray(R.array.publications);
        popupPublicationDialogOptions();

    }

    public void recentClicked(View view) {
        // Deal with user clicking on a recent crossword
    }
    public void settingsClicked(View view) {
        // Show some settings options?
    }

    private void startNewCrossword() {
        CrosswordLibraryManager clm = new CrosswordLibraryManager(this) ;
        boolean safeToWrite = false ;

        if (clm.crosswordAlreadyExists(publication, date)) {
            Log.d(LOG_TAG, "Crossword file already exists for: " + publication + " - " + date + ". Checking if it's safe to overwrite...");
            safeToWrite = confirmOverwrite() ;
        } else {
            safeToWrite = true;
        }

        if (safeToWrite) {
            Intent intent = new Intent(this, NewCrossword.class);
            Log.d(LOG_TAG, "Crossword publication selected: " + publication);
            Log.d(LOG_TAG, "Crossword displayDate entered: " + date);

            intent.putExtra(Crossword.CROSSWORD_EXTRA_TITLE, publication);
            intent.putExtra(Crossword.CROSSWORD_EXTRA_DATE, date);
            startActivity(intent);
        }
    }

    private boolean confirmOverwrite() {
        // Popup with confirmation that the previously saved crossword with this publication(i.e. name) and date will be overwritten
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(publication + " - " + Crossword.getDisplayDate(this, date)) ;
        // Set the action buttons
        builder.setPositiveButton(R.string.dialog_overwrite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so allow previous file to be overwritten
                        safeToOverwrite = true;

                    }
                }

        ) ;
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Should already be false, but just make sure
                        safeToOverwrite = false;
                        // Do nothing
                        dialog.cancel();
                    }
                }

        );
        // Warning Message:
        TextView dialogMessage = new TextView(this) ;
        dialogMessage.setText(R.string.dialog_overwrite_crossword_message);
        dialogMessage.setGravity(Gravity.CENTER);
        builder.setView(dialogMessage);

        AlertDialog overwritePopup = builder.create();
        overwritePopup.show();

        return safeToOverwrite ;
    }
    private void popupPublicationDialogOptions() {
        // Pop up dialog pox with radio buttons of common publications
        // Auto-fill the input EditText with the selected option. Do nothing if 'other' is selected

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(R.string.pub_name_dialog_title) ;

        // Specify the list array, the items to be selected by default,
        // and the listener through which to receive callbacks when items are selected
        builder.setSingleChoiceItems(publications, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int pub) {

                        publication = publications[pub].toString();

                    }
                });


        // Set the action buttons
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int id){
                        // User clicked OK, so save the publication result somewhere
                        // or return it to the component that opened the dialog

                        // Date dialog popup
                        popupDateDialog();

                    }
                }

        ) ;
        builder.setNeutralButton(R.string.pub_other, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 'Other' Selected, so popup edittext dialog box
                popupOtherPublicationDialog() ;
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int id){
                        // Do nothing
                        dialog.cancel();
                    }
                }

        );

        AlertDialog namePopup = builder.create();
        namePopup.show();

    }
    private void popupOtherPublicationDialog(){
        // Popup to allow user to input non-standard publication

        AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
        builder.setTitle(R.string.pub_name_other_dialog_title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set selectedPublication to the text entered in the box
                if (input.getText().toString().equals("")) {
                    popupOtherPublicationDialog();  // If it is blank, call this alert again. User can press cancel to get out of it if requried.
                } else {
                    publication = input.getText().toString();
                }

                // Date dialog popup
                popupDateDialog();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel clicked so do nothing
                dialog.cancel();
            }
        }) ;

        builder.show() ;
    }
    private void popupDateDialog() {
        // Pop up dialog pox with NumberPickers for the displayDate
        // Auto-fill the input EditText with the selected option. Do nothing if user presses cancel
        DialogFragment dialogFragment = new StartDatePicker();
        dialogFragment.show(getFragmentManager(), "start_date_picker");
    }

    public class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            // Use the current displayDate as the default displayDate in the picker
            DatePickerDialog dialog = new DatePickerDialog(HomeActivity.this, this, startYear, startMonth, startDay);
            return dialog ;
        }
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String yr = year + "" ;
            String month = (monthOfYear + 1) + "" ;   // Add 1 to the month so that it displays normally. Calendar returns months 0-11.
            String day = dayOfMonth + "";

            // Do something with the displayDate chosen by the user
            if (month.length() == 1) {
                month = "0" + month;
            }
            if (day.length() == 1) {
                day = "0" + day ;
            }

            // Date in save format (yyyyMMdd)
            date = yr + month + day ;

            Log.d(LOG_TAG,"Starting displayDate set, so starting new crossword...");
            startNewCrossword();
        }

    }

    public static void emailDeveloperFeedback(Context context) {
        // Create an email to me with specific subject heading. Outsource actual email sending.
        Log.d(LOG_TAG,"Trying to launch email to developer");
        Log.d(LOG_TAG,"email_type: " + context.getString(R.string.email_type));

        // Create Intent & let email client send the message
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(context.getResources().getString(R.string.email_type));
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{context.getResources().getString(R.string.email_target)});
        i.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.email_subject));
        i.putExtra(Intent.EXTRA_TEXT   , context.getResources().getString(R.string.email_body));
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, context.getResources().getString(R.string.email_error), Toast.LENGTH_SHORT).show();
        }
    }
    public static void showAboutDialog(Context context) {
        // Show a dialog containing an 'about' section
        // Text view (probably should move this to a layout resource later)
        TextView aboutTV = new TextView(context);
        aboutTV.setText(context.getString(R.string.about_text));
        aboutTV.setGravity(View.TEXT_ALIGNMENT_CENTER);
        aboutTV.setPadding(context.getResources().getDimensionPixelOffset(R.dimen.about_dialog_padding), context.getResources().getDimensionPixelOffset(R.dimen.about_dialog_padding), context.getResources().getDimensionPixelOffset(R.dimen.about_dialog_padding), context.getResources().getDimensionPixelOffset(R.dimen.about_dialog_padding));
        // Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.about_title));
        builder.setView(aboutTV);
        builder.setNegativeButton(R.string.about_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                        dialog.cancel();
                    }
                }
        );
        builder.show();
    }
    public static void openSettings(Context context) {
        Toast.makeText(context, "Will create a settings option soon", Toast.LENGTH_SHORT).show();
    }
    public static boolean deviceHasCameraCapability(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
