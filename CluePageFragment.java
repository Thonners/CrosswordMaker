package com.thonners.crosswordmaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CluePageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CluePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CluePageFragment extends Fragment {

    private static final String LOG_TAG = "CluePageFragment";
    static final int REQUEST_IMAGE_CAPTURE = 1 ;    // Int used in camera intent
    private static int MIN_CLUE_IMAGE_RES = 100 ;  // Minimum number of pixels for clue image width to use for resampling
    private static final String ARG_FILE_PATH = "filePath" ;


    Crossword crossword ;
    String imageFilePath;
    GridLayout grid ;
    ImageView clueImageView ;
    File clueImageFile;
    Bitmap clueImageBitmap ;
    View takeCluePhotoButton ;

    private OnFragmentInteractionListener mListener;


    public static CluePageFragment newInstance(String filePath) {
        CluePageFragment fragment = new CluePageFragment();
        Bundle args = new Bundle();
        String imageFilePath = filePath;
        args.putString(ARG_FILE_PATH, imageFilePath);
        fragment.setArguments(args);
        return fragment ;
    }

    public CluePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageFilePath = getArguments().getString(ARG_FILE_PATH);
            Log.d(LOG_TAG,"imageFilePath = " + imageFilePath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_clues, container, false);

        initialise(view);

        return view ;
    }

    private void initialise(View view) {
        takeCluePhotoButton = (View) view.findViewById(R.id.take_picture_clues_button) ;
        takeCluePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Take picture button pressed");
                dispatchTakePictureIntent();
            }
        });
        clueImageView = (ImageView) view.findViewById(R.id.image_view_clues);
        loadClueImage();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void setClueImageInView() {
        Log.d(LOG_TAG, "trying to get the photo from: " + clueImageFile.getAbsolutePath());
        //Show image in clueImageView
        clueImageBitmap = getImage(clueImageFile);

        Log.d(LOG_TAG, "Setting image");
        clueImageView.setImageBitmap(clueImageBitmap);

        //Remove button from view
        //Check image exists
        Log.d(LOG_TAG, "Removing photo button");
        ((ViewGroup) takeCluePhotoButton.getParent()).removeView(takeCluePhotoButton);

    }
    private void loadClueImage() {
        // Initialise the file required
        try {
            clueImageFile = new File(imageFilePath);
        } catch (Exception e) {
            Log.e(LOG_TAG,"Couldn't create imageFile. Exception message: " + e.getMessage());
        }
        // Load clue image if one already exists
        if (clueImageFile.length() > 10) {
            setClueImageInView();
        }
    }

    private void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Called when camera intent returns. Now load image just taken

        setClueImageInView();

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
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        Log.d(LOG_TAG,"Calculating bitmap sample size...");
        // Check that reqWidth is sensible
        if (reqWidth < MIN_CLUE_IMAGE_RES) {
            reqWidth = getScreenWidth();
            reqWidth = Math.min(reqWidth, 4096);    // 4096 is the maximum bitmap size that can be imported into a 'texture'. Limit it here to prevent it falling over on larger resolutioned screens.
        }
        // Raw width of image
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.

            Log.d(LOG_TAG,"halfWidth / inSampleSize =  " + (halfWidth / inSampleSize));
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(LOG_TAG,"Final sample size = " + inSampleSize);
        return inSampleSize;
    }
    private int getScreenWidth() {
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        return size.x;
    }
}
