package com.thonners.crosswordmaker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class GridMaker extends ActionBarActivity {

    private static final String LOG_TAG = "GridMaker";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    EditText publicationNameInput ;
    EditText publicationDateInput ;

    String publicationSelected ;
    CharSequence[] publications ;

    String crosswordTitle;
    String crosswordDate;

    int n = 20 ;    // Number of sample of image to take to find grid
    double gridCoverage = 0.5 ; // % of image covered by the grid
    int blackColorThreshold = 100 ; // Value below which colours will be deemed to mean a black cell, above which implies a white cell

    private int screenWidth ;
    private int screenHeight ;
    GridLayout grid ;
    Crossword crossword ;

    File tempImageFile = null;  // Temporary file in which to store the image of the crossword grid if auto-grid generation is selected
    Bitmap gridImageBW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_maker);

        getScreenDetails();
        initialise();


        if(getIntent().getBooleanExtra(NewCrossword.AUTO_GRID_GENERATION,false)) {
            // Get a photo to do the auto grid generation
            autoGenerateGrid() ;
        } else {
            crossword = new Crossword(getApplicationContext(), getIntent().getIntExtra(NewCrossword.NO_ROWS, 8), grid, screenWidth, screenHeight);
            tutorialToast();
        }
    }

    private void initialise() {
        // Get instances of the views in the layout.xml
        grid = (GridLayout) findViewById(R.id.main_grid);
        crosswordTitle = getIntent().getStringExtra(Crossword.CROSSWORD_EXTRA_TITLE);
        crosswordDate = getIntent().getStringExtra(Crossword.CROSSWORD_EXTRA_DATE);
/*        publicationNameInput = (EditText) findViewById(R.id.crossword_publication_input);
        publicationDateInput = (EditText) findViewById(R.id.crossword_date_input);
        publications = getResources().getTextArray(R.array.publications);

        View.OnFocusChangeListener pubNameInputListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // pop up dialog box to give possible options
                    popupPublicationDialog();
                }
            }
        } ;
        publicationNameInput.setOnFocusChangeListener(pubNameInputListener);

        View.OnFocusChangeListener pubDateInputListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // pop up dialog box to give possible options
                    popupDateDialog();
            }
        } ;*/
    }
/*
    private void popupPublicationDialog() {
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

                        switch (pub) {

                            // Set publication equal to the string displayed
                            case 0:
                                publicationSelected = publications[0].toString();
                                break;
                            case 1:
                                publicationSelected = publications[1].toString();
                                break;
                            case 2:
                                publicationSelected = publications[2].toString();
                                break;
                            case 3:
                                publicationSelected = publications[3].toString();
                                break;
                            case 4:
                                publicationSelected = publications[4].toString();
                                break;
                            case 5:
                                publicationSelected = publications[5].toString();
                                break;

                        }
                    }
                });


        // Set the action buttons
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int id){
                        // User clicked OK, so save the publication result somewhere
                        // or return it to the component that opened the dialog
                            publicationNameInput.setText(publicationSelected) ;

                    }
                }

        ) ;
        builder.setNeutralButton(R.string.pub_other, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int id){
                        // Do nothing
                    }
                }

        );

        AlertDialog namePopup = builder.create();
        namePopup.show();

        return ;
    }
        private void popupDateDialog() {
        // Pop up dialog pox with NumberPickers for the date
        // Auto-fill the input EditText with the selected option. Do nothing if user presses cancel
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid_maker, menu);
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

    private void getScreenDetails() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        Log.d("CWM", "Screen Width = " + screenWidth);
        Log.d("CWM","Screen Height = " + screenHeight);
    }

    public void enterClicked(View view){
        // Freeze crossword grid and progress to next stage
//        String crosswordTitle = ((TextView) findViewById(R.id.crossword_publication_input)).getText().toString() ;
//        String crosswordDate = ((TextView) findViewById(R.id.crossword_date_input)).getText().toString() ;
        Log.d("CWM","Enter clicked (GridMaker activity)");
        Log.d(LOG_TAG,"Setting crossword title to: " + crosswordTitle );
        Log.d(LOG_TAG,"Setting crossword date to: " + crosswordDate );

        crossword.setTitle(crosswordTitle);
        crossword.setDate(crosswordDate);

        startCrosswordActivity();

    }

    private void startCrosswordActivity(){
        // Start the com.thonners.CrosswordMaker.CrosswordActivity
        // Put the crossword in the Intent as an Extra using Crossword.getSaveArray()
        Intent intent = new Intent(this, CrosswordActivity.class);
        intent.putExtra(Crossword.CROSSWORD_EXTRA, crossword.getSaveArray());

        startActivity(intent);
    }

    private void tutorialToast() {
        // Toast to give hint as to what to do here
        Toast tutorial = Toast.makeText(this,getResources().getString(R.string.tutorial_toast_grid_maker), Toast.LENGTH_LONG);
        TextView tv = (TextView) tutorial.getView().findViewById(android.R.id.message);
        if (tv != null) { tv.setGravity(Gravity.CENTER); }      // Centres text within toast
        tutorial.setGravity(Gravity.CENTER,0,0);                // Centres toast in middle (vertically & horizontally) of screen
        tutorial.show();
    }

    private void autoGenerateGrid() {
        // Get photo to generate the grid automatically
        dispatchTakePictureIntent();
        // Interpret the photo to work out the grid
    // Put method this in the crossword class?
        // create the crossword
        //crossword = new Crossword(this,grid,);
    }

    @Override
         protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Called when camera intent returns. Now try to generate the grid automatically
        generateGrid(tempImageFile);

        ImageView iv = new ImageView(this) ;
        iv.setImageBitmap(Bitmap.createScaledBitmap(gridImageBW, 4000, 4000, false));
        grid.addView(iv,0);

    }

    private void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {

            tempImageFile = null;

            try {
                tempImageFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"temp.jpg");
       //         tempImageFile = File.createTempFile("tempClueImage",".jpg");
            } catch (Exception e) {
                Log.e(LOG_TAG, "There was some error creating the temporary file") ;
            }

            // Check file exists
            if (tempImageFile != null) {
                Log.d(LOG_TAG, "tempImageFile != null");
                //Give intent the save path
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempImageFile));

                Log.d(LOG_TAG, "taking the picture");
                //Take the picture
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Auto reading of grid
    private void generateGrid(File crosswordImage) {
        // Start by working out the image orientation and size
        Bitmap gridImageOriginal = BitmapFactory.decodeFile(crosswordImage.getAbsolutePath());
        int width = gridImageOriginal.getWidth();
        int height = gridImageOriginal.getHeight() ;
        int minDim ;       // Minimum dimension used in creating probe array below
        int maxDim ;       // Maximum dimension used in creating probe array below
        boolean isPortrait = true ;   // Assume portrait        // Dont think this matters. Assuming for now the image is the correct way up.

        Log.d(LOG_TAG,"Width = " + width);
        Log.d(LOG_TAG,"Height = " + height);

        // Check orientation
        if (width > height) {
            Log.d(LOG_TAG,"Orientation = Landscape");
            maxDim = width ;
            minDim = height ;
            isPortrait = false ;
        } else {
            maxDim = height ;
            minDim = width ;
            Log.d(LOG_TAG,"Orientation = Portrait");
        }

        // Convert bitmap to black and white
        gridImageBW = convertToBlackAndWhite(gridImageOriginal);

        Log.d(LOG_TAG,"Storing pixel values in int array...");

        int[] pixels = new int[width * height];
        gridImageBW.getPixels(pixels,0,width,0,0,width,height);

        // Create n x n grid of probeFullColor to check colours of pixels.
        // Assume grid (for now) sits in the middle 50% of the image?
            int[] probeX ;  // probeX and probeY to be used later
            int[] probeY ;
        int[] probeMin = new int[n] ;
        int[] probeMax = new int[n] ;
        int probeSpacing = (int) ((minDim * gridCoverage) / (n - 1)) ;     // Spacing between probeFullColor is the number of pixels (minDim * %coverage) divided by (n-1) (There are n-1 spaces between n probeFullColor)

        // Should end up with square grid in the middle of the image
        // Calculate probe coords
        probeMin[0] = (int) (minDim * ((1-gridCoverage) / 2)) ; // First probe row is half of what's left of the image once the grid coverage has been removed
        for (int i = 1 ; i < n ; i++) {
            // Add spacing on to last probe's location to get new probe co-ord.
            probeMin[i] = probeMin[i-1] + probeSpacing ;
            Log.d(LOG_TAG,"probeMin[" + i + "] = " + probeMin[i]);
        }
        probeMax[0] = (int) ((maxDim - (minDim * gridCoverage)) / 2 ) ;     // First coord for probesMax starts at [maxDim - (width of probeFullColor in minDim)] / 2
        for (int i = 1 ; i < n ; i++ ) {
            // Add spacing on to last probe's location to get new probe co-ord.
            probeMax[i] = probeMax[i-1] + probeSpacing ;
            Log.d(LOG_TAG,"probeMax[" + i + "] = " + probeMax[i]);
        }

        if (isPortrait) {
            probeX = probeMin;
            probeY = probeMax;
        } else {
            probeX = probeMax;
            probeY = probeMin;
        }

        int[][] probeFullColor = new int[n][n];     // Value of probeFullColor[m][n] = coords in x and y
        boolean[][] probeBlack = new boolean[n][n];     // Value of probeFullColor[m][n] = coords in x and y

        // Get values of pixel at these probe locations
        for (int i = 0 ; i < n ; i++) { /* i is the row */
            for (int j=0 ; j<n ; j++) { /* j is the column */
                Log.d(LOG_TAG, "Interogating probe(" + i + "," + j + "), with pixel coords (" + probeX[i] + "," + probeY[j] + ").");
                probeFullColor[i][j] = gridImageBW.getPixel(probeX[j],probeY[i]) ;      // Note switch of i & j, to make [1][2] refer to 3rd point across 2nd row
                Log.d(LOG_TAG,"probeFullColor = " + probeFullColor[i][j]) ;
                Log.d(LOG_TAG,"probeFullColor.red = " + Color.red(probeFullColor[i][j])) ;  // Filter turns all Color.alpha to 255, and Color.red=Color.blue=Color.green, so only interrogate one.

                // Assume black if red/blue/gree < 128:
                if (Color.red(probeFullColor[i][j]) < blackColorThreshold) {
                    Log.d(LOG_TAG, "Setting probe(" + i + "," + j + ") to black.");
                    probeBlack[i][j] = true ;
                } else  {
                    Log.d(LOG_TAG, "Setting probe(" + i + "," + j + ") to white.");
                    probeBlack[i][j] = false ;
                }
                gridImageBW.setPixel(probeX[j],probeY[i],Color.RED);

            }
        }



    }

    // Lifted from th'internet.
    private Bitmap convertToBlackAndWhite(Bitmap orginalBitmap) {

        Log.d(LOG_TAG,"Converting bitmap to B&W");

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);

        Bitmap blackAndWhiteBitmap = orginalBitmap.copy(
                Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);

        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);

        return blackAndWhiteBitmap;
    }


}
