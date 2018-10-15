package com.thonners.crosswordmaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kobakei.ratethisapp.RateThisApp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class HomeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String LOG_TAG = "HomeActivity";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 123;

    private final int RATE_DAYS = 3 ;
    private final int RATE_LAUNCHES = 5 ;

    private final String firstRunFileName = "firstRun" ;
    private final String PREFS_NEVER_RATE = "firstRun" ;

    private ServerConnection serverConnection ;
    private boolean serverAvailable = false ;

    private String publication;
    private String date ;
    private CharSequence[] publications ;
    private boolean safeToOverwrite = false ;
    private LinearLayout mainLayout ;

    private CrosswordLibraryManager libraryManager ;
    private ArrayList<CrosswordLibraryManager.SavedCrossword> recentCrosswords ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise SharedPreferences (iff they haven't been set before - the false means don't rewrite the defaults if they already exist)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Create / get the views
        setContentView(R.layout.activity_home_material);
        mainLayout = (LinearLayout) findViewById(R.id.home_main_layout) ;

        checkFirstRun();
        showRateDialog() ;


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sdcardIsAvailable(this)) {
            updateRecentCrosswords();
        } else {
            showToast(getResources().getString(R.string.sdcard_error_1));
            openToolkitActivity();
        }
        checkServerConnection() ;
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

    /**
     * Method to populate the recent crosswords list. After getting the list of recent crosswords
     * from the library manager, this method populates the
     */
    private void updateRecentCrosswords() {
        // Update recent crosswords list
        Log.d(LOG_TAG, "Searching for recent crossword file... ");
        libraryManager = new CrosswordLibraryManager(this);
        recentCrosswords = libraryManager.getRecentCrosswords();
        switch (recentCrosswords.size()) {
            case 3:
                TextView recentTV3Title = (TextView) findViewById(R.id.home_card_recent_3_title);
                TextView recentTV3Date = (TextView) findViewById(R.id.home_card_recent_3_date);
                CardView recentCard3 = (CardView) findViewById(R.id.home_card_recent_3);
                recentTV3Title.setText(recentCrosswords.get(2).getTitle());
                recentTV3Date.setText(recentCrosswords.get(2).getDisplayDate());
                recentCard3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        libraryManager.openCrossword(recentCrosswords.get(2).crosswordDir);
                    }
                });
            case 2:
                TextView recentTV2Title = (TextView) findViewById(R.id.home_card_recent_2_title);
                TextView recentTV2Date = (TextView) findViewById(R.id.home_card_recent_2_date);
                CardView recentCard2 = (CardView) findViewById(R.id.home_card_recent_2);
                recentTV2Title.setText(recentCrosswords.get(1).getTitle());
                recentTV2Date.setText(recentCrosswords.get(1).getDisplayDate());
                recentCard2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        libraryManager.openCrossword(recentCrosswords.get(1).crosswordDir);
                    }
                });
            case 1:
                TextView recentTV1Title = (TextView) findViewById(R.id.home_card_recent_1_title);
                TextView recentTV1Date = (TextView) findViewById(R.id.home_card_recent_1_date);
                recentTV1Title.setText(recentCrosswords.get(0).getTitle());
                recentTV1Date.setText(recentCrosswords.get(0).getDisplayDate());
                CardView recentCard1 = (CardView) findViewById(R.id.home_card_recent_1);
                recentCard1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        libraryManager.openCrossword(recentCrosswords.get(0).crosswordDir);
                    }
                });
                break;
        }

        RelativeLayout recentLayout = (RelativeLayout) findViewById(R.id.home_card_recent_layout);
        switch (recentCrosswords.size()) {
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
        openToolkitActivity();
    }
    private void openToolkitActivity() {
        // Open just a toolkit activity
        Intent intent = new Intent(this, ToolkitSliderActivity.class);
        startActivity(intent);
    }

    public void savedClicked(View view) {
        if (sdcardIsAvailable(this)) {
            if (new CrosswordLibraryManager(this).getSavedCrosswords().size() > 0 ) {
                // Open CrosswordLibraryActivity
                Intent intent = new Intent(this, CrosswordLibraryActivity.class);
                startActivity(intent);
            } else {
                showToast(getResources().getString(R.string.home_recent_none1) + "\n" + getResources().getString(R.string.home_recent_none2));
            }
        } else {
            showToast(getResources().getString(R.string.sdcard_error_1));
        }
    }

    public void newClicked(View view) {
        // Check SD card is available to save to, otherwise show toast and ignore press
        if (sdcardIsAvailable(this)) {
            // Open something to let the person enter the details
            publications = getResources().getTextArray(R.array.publications);
            popupPublicationDialogOptions();
        } else {
            showToast(getResources().getString(R.string.sdcard_error_1));
        }
    }

    public void settingsClicked(View view) {
        // Show some settings options?
        openSettings(this);
    }

    public void startNewCrossword() {
        CrosswordLibraryManager clm = new CrosswordLibraryManager(this) ;
        boolean safeToWrite = false ;

        if (clm.crosswordAlreadyExists(publication, date)) {
            Log.d(LOG_TAG, "Crossword file already exists for: " + publication + " - " + date + ". Checking if it's safe to overwrite...");
            safeToWrite = confirmOverwrite() ;
        } else {
            safeToWrite = true;
        }

        if (safeToWrite) {
            Intent intent = new Intent(this, NewCrosswordActivity.class);
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
                    public void onClick(DialogInterface dialog, int id) {
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
                popupOtherPublicationDialog();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
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
    private void showToast(String string) {
        Toast.makeText(this,string,Toast.LENGTH_LONG).show();
    }
    private void checkFirstRun() {
        File firstRunFile = new File(getFilesDir(),firstRunFileName) ;
        // If first run file doesn't exist, show the snackbar
        if (firstRunFile.exists()) {
            // This is likely to be true, so will be a faster test than the negative
            Log.d(LOG_TAG, "firstRunFile found, so not showing snackbar.");
        } else {
            Log.d(LOG_TAG,"First run file not found, so showing snackbar") ;
            Snackbar.make(mainLayout,R.string.low_ram_snackbar,Snackbar.LENGTH_LONG).show();
            // Now create the file for next time
            try {
                firstRunFile.createNewFile() ;
            } catch (IOException exception) {
                Log.d(LOG_TAG,"Error creating first run file: " + exception.getLocalizedMessage()) ;
            }
        }
    }

    /**
     * @return Whether or not offline mode is enabled in the app's settings.
     */
    private boolean getOfflineMode() {
        // Get the SharedPrefs to check that offline mode isn't enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this) ;
        return prefs.getBoolean(SettingsFragment.KEY_PREF_OFFLINE_MODE, false) ;
    }

    /**
     * Method to check whether the CrosswordToolkit Server is available to connect to, and if so,
     * sets the serverAvailable boolean to true.
     */
    private void checkServerConnection() {
        if (!getOfflineMode() && isNetworkAvailable()) {
            ServerConnection.ServerConnectionListener serverConnectionListener = new ServerConnection.ServerConnectionListener() {
                @Override
                public void serverConnectionResponse(ServerConnection.SocketIdentifier requestSuccess, ArrayList<String> answers) {

                }
                @Override
                public void setServerAvailable(boolean isAvailable) {
                    serverAvailable = isAvailable ;
                    if (serverAvailable) {
                        Log.d(LOG_TAG, "Network is available, and server connection test was successful.");
                        //Toast.makeText(getApplicationContext(),getString(R.string.server_available_toast),Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(LOG_TAG, "Network is available, and server connection test was UNsuccessful.");
                    }
                }

                @Override
                public void callShowLoadingSpinner() {
                }
                @Override
                public void callHideLoadingSpinner() {
                }
            } ;
            serverConnection = new ServerConnection(serverConnectionListener) ;
            serverConnection.testServerConnection() ;
        } else {
            // Force to false is no network available
            Log.d(LOG_TAG,"No network detected, so no server available");
            serverAvailable = false ;
        }
    }

    /**
     * @return Whether the device has a network connection.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    /**
     * Method to provide easy interface to call stopRateDialog when never is clicked.
      */
    private void neverRate() {
        RateThisApp.stopRateDialog(this);
    }

    /**
     * Method to show the rate this app dialog when appropriate
     */
    private void showRateDialog() {

        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // Set the desired frequency
        RateThisApp.Config  config = new RateThisApp.Config(RATE_DAYS, RATE_LAUNCHES);
        config.setTitle(R.string.rate_title);
        config.setMessage(R.string.rate_message);
        config.setYesButtonText(R.string.rate_yes);
        config.setNoButtonText(R.string.rate_never);
        config.setCancelButtonText(R.string.rate_later);
        // Callback from clicks
        RateThisApp.init(config);
        RateThisApp.setCallback(new RateThisApp.Callback() {
            @Override
            public void onYesClicked() {
                //Toast.makeText(HomeActivity.this, "Yes event", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoClicked() {
                //Toast.makeText(HomeActivity.this, "No event", Toast.LENGTH_SHORT).show();
                neverRate();
            }

            @Override
            public void onCancelClicked() {
                //Toast.makeText(HomeActivity.this, "Cancel event", Toast.LENGTH_SHORT).show();
            }
        });
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

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

    public static class StartDatePicker extends DialogFragment {
        // public static class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        // For displayDate picker
        Calendar c = Calendar.getInstance();
        int startYear = c.get(Calendar.YEAR);
        int startMonth = c.get(Calendar.MONTH);
        int startDay = c.get(Calendar.DAY_OF_MONTH);

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current displayDate as the default displayDate in the picker
            return new DatePickerDialog(getActivity(), (HomeActivity) getActivity(), startYear, startMonth, startDay);
        }
    }
    // Public static methods
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
        aboutTV.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
        Intent settingsIntent = new Intent(context, SettingsActivity.class) ;
        context.startActivity(settingsIntent);
        // Toast.makeText(context, "Will create a settings option soon", Toast.LENGTH_SHORT).show();
    }
    public static boolean deviceHasCameraCapability(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ;
    }
    public static boolean sdcardIsAvailable(Activity activity) {
        // Check to see if SD Card is available - This is required to save crosswords
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSDPresent)
        {
            // Check whether this app has write external storage permission or not.
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // If do not grant write external storage permission.
            if(writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                // Request user to grant write external storage permission.
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
                Log.d(LOG_TAG,"No permission for writing to external storage");
            }
            // yes SD-card is present
            Log.d("SD-Card", "SD Card is Present");
            return true ;
        }
        else
        {
            // Sorry
            Log.d("SD-Card", "SD Card not Present - cannot save crosswords");
            return false ;
        }
    }
    public static void hideKeyboard(Context context, View view) {
        // Method to hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Thanks. Saving progress is now possible", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "You denied permission to write to storage. This is required to save files.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
