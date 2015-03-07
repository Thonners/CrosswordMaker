package com.thonners.crosswordmaker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;


public class HomeActivity extends ActionBarActivity {

    private static final String LOG_TAG = "HomeActivity";

    String publication;
    String date ;
    CharSequence[] publications ;

    // For date picker
    Calendar c = Calendar.getInstance();
    int startYear = c.get(Calendar.YEAR);
    int startMonth = c.get(Calendar.MONTH);
    int startDay = c.get(Calendar.DAY_OF_MONTH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_home);
        setContentView(R.layout.activity_home_material);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        Intent intent = new Intent(this, NewCrossword.class);
        Log.d(LOG_TAG,"Crossword publication selected: " + publication);
        Log.d(LOG_TAG,"Crossword date entered: " + date);

        intent.putExtra(Crossword.CROSSWORD_EXTRA_TITLE,publication) ;
        intent.putExtra(Crossword.CROSSWORD_EXTRA_DATE,date) ;
        startActivity(intent);
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
        // Pop up dialog pox with NumberPickers for the date
        // Auto-fill the input EditText with the selected option. Do nothing if user presses cancel
        DialogFragment dialogFragment = new StartDatePicker();
        dialogFragment.show(getFragmentManager(), "start_date_picker");
    }

    public class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        // Use the current date as the default date in the picker
        DatePickerDialog dialog = new DatePickerDialog(HomeActivity.this, this, startYear, startMonth, startDay);
        return dialog ;
    }
    public void onDateSet(DatePicker view, int year, int monthOfYear,
            int dayOfMonth) {
        // TODO Auto-generated method stub
        String yr = year + "" ;
        String month = (monthOfYear + 1) + "" ;   // Add 1 to the month so that it displays normally. Calendar returns months 0-11.
        String day = dayOfMonth + "";

                // Do something with the date chosen by the user
        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day ;
        }

        // Date in save format (yyyyMMdd)
        date = yr + month + day ;

        Log.d(LOG_TAG,"Starting date set, so starting new crossword...");
        startNewCrossword();
    }

}
}
