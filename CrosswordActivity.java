package com.thonners.crosswordmaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;


public class CrosswordActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1 ;

    private static final String LOG_TAG = "CrosswordActivity" ;

    Crossword crossword ;
    GridLayout grid ;
    ImageView clueImageView ;
    File clueImageFile;
    Bitmap clueImageBitmap ;
    View takeCluePhotoButton ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossword);

        getViewsByID();

        String[] crosswordSavedArray = getIntent().getStringArrayExtra(Crossword.CROSSWORD_EXTRA);

        crossword = new Crossword(this, grid, crosswordSavedArray);
    }

    private void getViewsByID() {
        grid = (GridLayout) findViewById(R.id.crossword_grid);
        clueImageView = (ImageView) findViewById(R.id.image_view_clues);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crossword, menu);
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

    private void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Check file exists
            if (clueImageFile != null) {
                Log.d(LOG_TAG, "clueImage != null");
                //Give intent the save path
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(clueImageFile));

                Log.d(LOG_TAG, "taking the picture");
                //Take the picture
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Called when camera intent returns. Now load image just taken

        Log.d(LOG_TAG, "trying to get the photo from: " + clueImageFile.getAbsolutePath());
        //Show image in clueImageView
        clueImageBitmap = getImage(clueImageFile);


        Log.d(LOG_TAG, "Setting image");
        clueImageView.setImageBitmap(clueImageBitmap);

        //clueImageView.setMaxHeight(clueImageBitmap.getHeight());

        //Remove button from view
        //Check image exists
        Log.d(LOG_TAG, "Removing photo button");
        ((ViewGroup) takeCluePhotoButton.getParent()).removeView(takeCluePhotoButton);

    }

    public void takePictureClues(View clueImagePromptButton) {
        Log.d(LOG_TAG, "takePictureClues clicked");
        // Start intent to get image of the clues
        // Get file to save to from Crossword
        clueImageFile = crossword.getCluePictureFile();
        takeCluePhotoButton = clueImagePromptButton ;
        //TODO: check whether file exists and if so prompt user (maybe not required as if image exists it should be shown?
        // Call intent to get image
        dispatchTakePictureIntent();



    }

    private Bitmap getImage(File bitmapFile) {
        // Decode bitmapFile and return the bitmap for use in an ImageView
        Log.d(LOG_TAG, "trying to get the photo from: " + bitmapFile.getAbsolutePath());

        // Decode bounds to get size image size. For use in loading a smaller scaled image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Log.d(LOG_TAG,"Decoding bounds...");
        BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(),options) ;
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        Log.d(LOG_TAG,"Bounds: imageHeight = " + imageHeight + ", imageWidth = " + imageWidth + ", imageType = " + imageType);

        Log.d(LOG_TAG,"clueImageView.getWidth() = " + clueImageView.getWidth());
        options.inSampleSize = calculateInSampleSize(options,clueImageView.getWidth());

        // Turn off justDecodeBounds so that the file is properly decoded
        options.inJustDecodeBounds = false ;

        return BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        Log.d(LOG_TAG,"Calculating bitmap sample size...");
        // Raw height and width of image
//        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

 //       if (height > reqHeight || width > reqWidth) {

        if (width > reqWidth) {
            //final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
      //      while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {

            Log.d(LOG_TAG,"halfWidth / inSampleSize =  " + (halfWidth / inSampleSize));
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(LOG_TAG,"Final sample size = " + inSampleSize);
        return inSampleSize;
    }
    public void saveGrid(View view) {
        // Save the grid
        crossword.saveCrossword();

        Toast toast = Toast.makeText(this,"Crossword progress saved.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
