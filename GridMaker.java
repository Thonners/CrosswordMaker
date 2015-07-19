package com.thonners.crosswordmaker;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * GridMaker Activity
 * Shows a blank grid with the number of rows/columns selected by the user in the NewCrossword activity.
 * The user should touch squares to turn them into black squares, until the grid is properly reflected.
 * Contains automatic toggling of the rotationally symmetrical square to make grid creation faster.
 *
 *  Created by Thonners on 06/05/15
 */

public class GridMaker extends ActionBarActivity {

    private static final String LOG_TAG = "GridMaker";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    EditText publicationNameInput ;
    EditText publicationDateInput ;

    String publicationSelected ;
    CharSequence[] publications ;

    String crosswordTitle;
    String crosswordDate;
    int crosswordRowCount;

    int gridTLX=0 , gridTLY=0 ;     // X/Y coords of top left of grid
    int gridBRX=0 , gridBRY=0 ;     // X/Y coords of bottom right of grid
    int gridCornerTouched = 0 ; // Counter to keep track of the number of times the grid was touched.

    int n ;                     //  Number of probes to create to sample image to find the grid. (calculated in initialise()).
    int nProbesPerRow = 9 ;     // This is the number of probes per row
    double gridCoverage = 0.5 ; // % of image (width) covered by the grid
    int blackColorThreshold = 100 ; // Value below which colours will be deemed to mean a black cell, above which implies a white cell (for greyscale 256 bit)

    private int screenWidth ;
    private int screenHeight ;
    GridLayout grid ;
    LinearLayout linearLayout;
    Crossword crossword ;

    File tempImageFile = null;  // Temporary file in which to store the image of the crossword grid if auto-grid generation is selected
    Bitmap gridImageBW;
    Bitmap gridImageOriginal;
    Bitmap scaledBitmap ;
    ImageView iv ;          // ImageView in which to display picture of grid
    boolean[][] autoGeneratedGrid ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_maker);

        getScreenDetails();
        initialise();


    }

    private void initialise() {
        // Get instances of the views in the layout.xml
        grid = (GridLayout) findViewById(R.id.main_grid);
        linearLayout = (LinearLayout) findViewById(R.id.grid_maker_linear_layout);
        // Get intents
        crosswordTitle = getIntent().getStringExtra(Crossword.CROSSWORD_EXTRA_TITLE);
        crosswordDate = getIntent().getStringExtra(Crossword.CROSSWORD_EXTRA_DATE);
        crosswordRowCount = getIntent().getIntExtra(Crossword.CROSSWORD_EXTRA_NO_ROWS, 0);

        // Amend number of probes to scale with number of rows
        n = nProbesPerRow * crosswordRowCount ;
        autoGeneratedGrid = new boolean[crosswordRowCount][crosswordRowCount];

        if(getIntent().getBooleanExtra(NewCrossword.AUTO_GRID_GENERATION,false)) {
            // Get a photo to do the auto grid generation
            grid.setVisibility(View.INVISIBLE);

        // Get photo to generate the grid automatically
        dispatchTakePictureIntent();
        } else {
            crossword = new Crossword(getApplicationContext(), crosswordRowCount, grid, screenWidth, screenHeight, crosswordTitle, crosswordDate);
            tutorialToast();
        }

        // Set Activity Title in the action bar
        setTitle(crossword.getActivityTitle());
    }

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
        Log.d(LOG_TAG,"Setting crossword displayDate to: " + crosswordDate );

        crossword.setTitle(crosswordTitle);
        crossword.setDate(crosswordDate);

        crossword.saveCrossword();


        Log.d(LOG_TAG,"Crossword dir =  " + crossword.getSaveDir().getAbsolutePath() );

        startCrosswordActivity();

    }

    private void startCrosswordActivity(){
        // Start the com.thonners.CrosswordMaker.CrosswordActivity using the CrosswordLibraryManager
        CrosswordLibraryManager clm = new CrosswordLibraryManager(this);
        clm.openCrossword(crossword.getSaveDir());
    }

    private void tutorialToast() {
        // Toast to give hint as to what to do here
        Toast tutorial = Toast.makeText(this,getResources().getString(R.string.tutorial_toast_grid_maker), Toast.LENGTH_LONG);
        toastShowCentred(tutorial);

    }

    private void toastShowCentred(Toast toast) {
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        if (tv != null) { tv.setGravity(Gravity.CENTER); }      // Centres text within toast
        toast.setGravity(Gravity.CENTER,0,0);                // Centres toast in middle (vertically & horizontally) of screen
        toast.show();
    }



    /****************************************** Auto Grid Generation *************************************/
    @Override
         protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Automatically called when activity is resumed post photo taking.
        autoGenerateGrid();
    }

    private void autoGenerateGrid() {
        // Put image in Linear Layout
        // Display image + probe array

        gridImageOriginal = BitmapFactory.decodeFile(tempImageFile.getAbsolutePath());
        gridImageBW = convertToBlackAndWhite(gridImageOriginal);
        // Clear original image from memory
        gridImageOriginal.recycle();
        gridImageOriginal = null ;

        int originalHeight = gridImageBW.getHeight();
        int originalWidth = gridImageBW.getWidth();
        double scaleRatio = (double) screenWidth / (double) originalWidth ;
        Log.d(LOG_TAG,"oH = " + originalHeight + " oW = " + originalWidth + " scaleRatio = " + scaleRatio);
        scaledBitmap = Bitmap.createScaledBitmap(gridImageBW, (int) (originalWidth * scaleRatio), (int) (originalHeight * scaleRatio),false);
        // Clear full scale BW image from memory
        gridImageBW.recycle();
        gridImageBW = null ;

        // ImageView to show image
        iv = new ImageView(this) ;
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setImageBitmap(scaledBitmap);
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (gridCornerTouched > 1) {
                    return true ;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setGridCorner((int) event.getX(), (int) event.getY()) ;
                        return true ;
                }

                return false;
            }
        });
        linearLayout.addView(iv,0);

        // Toast to prompt user to touch top left corner of grid
        Toast tutorialToastTL = Toast.makeText(this, getResources().getString(R.string.tutorial_toast_auto_grid_top_left), Toast.LENGTH_SHORT);
        toastShowCentred(tutorialToastTL);


    }
    private void setGridCorner(int x, int y) {
        // set the grid corner depending on where was touched
        Log.d(LOG_TAG,"Corner of grid touched @ (" + x + "," + y + ").");
        if (gridCornerTouched == 0) {
            // Set Top Left
            gridTLX = x ;
            gridTLY = y ;

            // Set corner to red to show where was touched
            scaledBitmap.setPixel(x,y,Color.RED);
            iv.setImageBitmap(scaledBitmap);

            // Increment counter
            gridCornerTouched++;

            // Toast to prompt user to touch bottom right corner of grid
            Toast tutorialToastBR = Toast.makeText(this, getResources().getString(R.string.tutorial_toast_auto_grid_bottom_right), Toast.LENGTH_SHORT);
            toastShowCentred(tutorialToastBR);

        } else {
            // Set bottom right
            gridBRX = x ;
            gridBRY = y ;

            // Increment counter to let the onTouchListener know that it doesn't need to handle the touch events any more. Probably redundant in proper implementation as the grid won't be shown to the user.
            gridCornerTouched++;

            // Called image touched for second time. Now try to generate the grid automatically
            generateGrid(tempImageFile);
        }
    }
    private void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {

            tempImageFile = null;

            try {
                tempImageFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"temp.jpg");
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
        int width = scaledBitmap.getWidth();
        int height = scaledBitmap.getHeight() ;
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

        Log.d(LOG_TAG,"Storing pixel values in int array...");

        int[] pixels = new int[width * height];
        scaledBitmap.getPixels(pixels,0,width,0,0,width,height);

        // Create n x n grid of probeFullColor to check colours of pixels.
        // Assume grid (for now) sits in the middle 50% of the image?
            int[] probeX ;  // probeX and probeY to be used later
            int[] probeY ;
        int[] probeMin = new int[n] ;   //Max and Min here refer to probes along the larger or smaller axis of the image
        int[] probeMax = new int[n] ;
        int probeSpacingX, probeSpacingY ;
        if ( gridTLX != 0 && gridTLY != 0 && gridBRX != 0 && gridBRY != 0) {
            // Calculate probe spacing in x and y dimensions then average
            probeSpacingX = (Math.abs(gridBRX - gridTLX) / (n - 1)) ;
            Log.d(LOG_TAG,"ProbeSpacing calulated using input from user. probeSpacingX = " + probeSpacingX) ;
        } else {
            probeSpacingX = (int) ((minDim * gridCoverage) / (n - 1));     // Spacing between probeFullColor is the number of pixels (minDim * %coverage) divided by (n-1) (There are n-1 spaces between n probeFullColor)
        }
        if ( gridTLX != 0 && gridTLY != 0 && gridBRX != 0 && gridBRY != 0) {
            // Calculate probe spacing in x and y dimensions then average
            probeSpacingY = ( Math.abs(gridBRY - gridTLY) / (n - 1)) ;
            Log.d(LOG_TAG,"ProbeSpacing calulated using input from user. probeSpacingY = " + probeSpacingY) ;
        } else {
            probeSpacingY = (int) ((minDim * gridCoverage) / (n - 1));     // Spacing between probeFullColor is the number of pixels (minDim * %coverage) divided by (n-1) (There are n-1 spaces between n probeFullColor)
        }

        // Should end up with square grid in the middle of the image
        // Calculate probe coords
        if ( gridTLX != 0 && gridTLY != 0 && gridBRX != 0 && gridBRY != 0) {
            // Calculate probe spacing in x and y dimensions then average
            probeMin[0] = gridTLX ;
        } else {
            probeMin[0] = (int) (minDim * ((1 - gridCoverage) / 2)); // First probe row is half of what's left of the image once the grid coverage has been removed
        }

        for (int i = 1 ; i < n ; i++) {
            // Add spacing on to last probe's location to get new probe co-ord.
            probeMin[i] = probeMin[i-1] + probeSpacingX ;
            Log.d(LOG_TAG,"probeMin[" + i + "] = " + probeMin[i]);
        }
        if ( gridTLX != 0 && gridTLY != 0 && gridBRX != 0 && gridBRY != 0) {
            // Calculate probe spacing in x and y dimensions then average
            probeMax[0] = gridTLY ;
        } else {
            probeMax[0] = (int) (minDim * ((1 - gridCoverage) / 2)); // First probe row is half of what's left of the image once the grid coverage has been removed
        }

        for (int i = 1 ; i < n ; i++ ) {
            // Add spacing on to last probe's location to get new probe co-ord.
            probeMax[i] = probeMax[i-1] + probeSpacingY ;
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
//                Log.d(LOG_TAG, "Interogating probe(" + i + "," + j + "), with pixel coords (" + probeX[i] + "," + probeY[j] + ").");
                probeFullColor[i][j] = scaledBitmap.getPixel(probeX[j],probeY[i]) ;      // Note switch of i & j, to make [1][2] refer to 3rd point across 2nd row
//                Log.d(LOG_TAG,"probeFullColor = " + probeFullColor[i][j]) ;
//                Log.d(LOG_TAG,"probeFullColor.red = " + Color.red(probeFullColor[i][j])) ;  // Filter turns all Color.alpha to 255, and Color.red=Color.blue=Color.green, so only interrogate one.

                // Set pixel (& surrounging pixels) to red to aid debugging
                for (int a = -3 ; a <= 3 ; a++) {
                    for (int b = -3; b <= 3; b++) {
                        scaledBitmap.setPixel(probeX[j] + a, probeY[i] + b, Color.RED);
                    }
                }

                // Assume black if red/blue/gree < 128:
                if (Color.red(probeFullColor[i][j]) < blackColorThreshold) {
//                    Log.d(LOG_TAG, "Setting probe(" + i + "," + j + ") to black.");
                    probeBlack[i][j] = true ;
                } else  {
//                    Log.d(LOG_TAG, "Setting probe(" + i + "," + j + ") to white.");
                    probeBlack[i][j] = false ;
                }


            }
        }

        ArrayList<ArrayList<Boolean>> gridCellsRows = new ArrayList<ArrayList<Boolean>>();
        ArrayList<ArrayList<Boolean>> gridCellsColumns = new ArrayList<ArrayList<Boolean>>();

        // Find changed between black and white
        for (int i = 0 ; i < n ; i++) { /* i is the row */
            gridCellsRows.add(new ArrayList<Boolean>());
            for (int j=2 ; j<n ; j++) { /* j is the column */   // Start at j=1 to avoid arrayIndexOutOfBounds Exception
                //if ((probeBlack[i][j] && !probeBlack[i][j-1]) || (!probeBlack[i][j] && probeBlack[i][j-1])) { // replaced by hopefully more robust checks
                if ((probeBlack[i][j] && !probeBlack[i][j-1] && !probeBlack[i][j-2]) || (!probeBlack[i][j] && probeBlack[i][j-1] && probeBlack[i][j-2])) {
                    // If black/white across two probes, implies change of cell. Keep track of cells
                    // Keep track of cells for each row of probes.
                    // if previous cell is black, set to true, else set false
                    if (probeBlack[i][j - 1]) {
                        gridCellsRows.get(i).add(true);
                    } else {
                        gridCellsRows.get(i).add(false);
                    }
                }
                // If just one probe is black, assume it's found a grid line between two white cells
                if (!probeBlack[i][j] && probeBlack[i][j-1] && !probeBlack[i][j-2]) {
                    gridCellsRows.get(i).add(false);
                }
            }

            // When the end of the row is reached, set final cell equal to final colour
            if (probeBlack[i][n-1]) {
                gridCellsRows.get(i).add(true);
            } else {
                gridCellsRows.get(i).add(false);
            }

            Log.d(LOG_TAG, " Row of probes: " + i + " thinks the grid for this row is: " + gridCellsRows.get(i).toString());
        }
        // Find changed between black and white
        for (int i = 0 ; i < n ; i++) { /* i is the column */
            gridCellsColumns.add(new ArrayList<Boolean>());
            for (int j=2 ; j<n ; j++) { /* j is the row */   // Start at j=1 to avoid arrayIndexOutOfBounds Exception
                if ((probeBlack[i][j] && !probeBlack[i][j-1] && !probeBlack[i][j-2]) || (!probeBlack[i][j] && probeBlack[i][j-1] && probeBlack[i][j-2])) {
                    // If black/white across two probes, implies change of cell. Keep track of cells
                    // Keep track of cells for each row of probes.
                    // if previous cell is black, set to true, else set false
                    if (probeBlack[i][j - 1]) {
                        gridCellsColumns.get(i).add(true);
                    } else {
                        gridCellsColumns.get(i).add(false);
                    }
                }
                // If just one probe is black, assume it's found a grid line between two white cells
                if (!probeBlack[i][j] && probeBlack[i][j-1] && !probeBlack[i][j-2]) {
                    gridCellsColumns.get(i).add(false);
                }
            }

            // When the end of the row is reached, set final cell equal to final colour
            if (probeBlack[i][n-1]) {
                gridCellsColumns.get(i).add(true);
            } else {
                gridCellsColumns.get(i).add(false);
            }

            Log.d(LOG_TAG, " Column of probes: " + i + " thinks the grid for this column is: " + gridCellsColumns.get(i).toString());
        }

        iv.setImageBitmap(scaledBitmap);

        crossword = new Crossword(this, crosswordRowCount, grid, screenWidth, screenHeight, crosswordTitle, crosswordDate);

        String[] saveArrayTemp = new String[Crossword.SAVE_ARRAY_START_INDEX + (crosswordRowCount * crosswordRowCount)];
        int index = Crossword.SAVE_ARRAY_START_INDEX;   // Index at which to start counting for generating a saveArray
        saveArrayTemp[Crossword.SAVED_ARRAY_INDEX_TITLE] = crosswordTitle;
        saveArrayTemp[Crossword.SAVED_ARRAY_INDEX_DATE] = crosswordDate;

        // Parse probes for grid. This isn't making any use of the cell finding above.Merely hoping that the middle probe per row/column will be the correct colour.
        for (int a=0 ; a < crosswordRowCount ; a++) {
            for (int b=0 ; b < crosswordRowCount ; b++) {


                autoGeneratedGrid[a][b] = probeBlack[a*nProbesPerRow + ((int) nProbesPerRow / 2)][b*nProbesPerRow + ((int) nProbesPerRow / 2)];
                if (autoGeneratedGrid[a][b]) {
                    Log.d(LOG_TAG,"Cell at index: (" + a + "," + b + ") is black.");
                    crossword.getCell(a,b).toggleBlackCell();

                    saveArrayTemp[index] = "-"  ;
                } else {
                    Log.d(LOG_TAG,"Cell at index: (" + a + "," + b + ") is white.");

                    saveArrayTemp[index] = ""   ;
                }


            }
        }

        // When testing is complete, uncomment line to hide imageView
        //iv.setVisibility(View.INVISIBLE);
        grid.setVisibility(View.VISIBLE);

        Toast tutorialCheckGrid = Toast.makeText(this, getResources().getString(R.string.tutorial_toast_auto_grid_check), Toast.LENGTH_SHORT);
        toastShowCentred(tutorialCheckGrid);


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
