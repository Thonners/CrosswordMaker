package com.thonners.crosswordmaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;


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
    private static final int REQUEST_IMAGE_CAPTURE = 1 ;    // Int used in camera intent
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static int MIN_CLUE_IMAGE_RES = 100 ;  // Minimum number of pixels for clue image width to use for resampling
    private static final String ARG_FILE_PATH = "filePath" ;


    Crossword crossword ;
    String imageFilePath;
    GridLayout grid ;
    ImageView clueImageView ;
    TouchImageView clueImageViewTouch ;
    File clueImageFile;
    Bitmap clueImageBitmap ;
    View takeCluePhotoButton ;

    private OnFragmentInteractionListener mListener;


    public static CluePageFragment newInstance(String filePath) {
        Log.d(LOG_TAG,"Initial filePath for clue image: " + filePath);
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

        Log.d(LOG_TAG,"Initialising...");
        takeCluePhotoButton = view.findViewById(R.id.take_picture_clues_button) ;
        clueImageViewTouch = view.findViewById(R.id.image_view_clues) ;
        clueImageViewTouch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Offer option to retake clues picture if user long-clicks
                Log.d(LOG_TAG,"Long-click on clue image detected");
                retakePicture();
                return true;
            }
        });

        if (clueImageFileExists()) {
            setClueImageInView();
            removePhotoButton();
        } else {
            if (HomeActivity.deviceHasCameraCapability(getActivity())) {
                takeCluePhotoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "Take picture button pressed");
                        dispatchTakePictureIntent();
                    }
                });
            } else {
                // If no picture file found and device doesn't have camera availability, display error message
                TextView textView = view.findViewById(R.id.take_picture_clues_text_view);
                textView.setText(getResources().getString(R.string.take_clue_picture_error));
            }
        }
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
        public void onFragmentInteraction(Uri uri);
    }

    private void setClueImageInView() {
        Log.d(LOG_TAG, "trying to get the photo from: " + clueImageFile.getAbsolutePath());
        //Show image in clueImageView
        clueImageBitmap = getImage(clueImageFile);

        Log.d(LOG_TAG, "Setting image");
        //clueImageView.setImageBitmap(clueImageBitmap);
        clueImageViewTouch.setImageBitmap(clueImageBitmap);
        Log.d(LOG_TAG, "Image should be set.");
        clueImageViewTouch.setZoom(2.0f);
        clueImageViewTouch.setZoom(1.0f);
    }
    private void removePhotoButton() {
        //Remove button from view
        if (takeCluePhotoButton != null) {
            Log.d(LOG_TAG, "Removing photo button");
            ((ViewGroup) takeCluePhotoButton.getParent()).removeView(takeCluePhotoButton);
            takeCluePhotoButton = null ;    // Force to null. Not sure what it would be without this.
        }
    }

    private boolean clueImageFileExists() {        // Initialise the file required
        try {
            clueImageFile = new File(imageFilePath);
        } catch (Exception e) {
            Log.e(LOG_TAG,"Couldn't create imageFile. Exception message: " + e.getMessage());
        }
        if (clueImageFile.length() > 10) {
            return true ;
        } else {
            return false ;
        }
    }

    public void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            Log.d(LOG_TAG,"Camera permissions granted. Starting the takePictureIntent");
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) == null) Log.d(LOG_TAG," resolveImageIntent == null");
            if (getActivity().getPackageManager() == null) Log.d(LOG_TAG," getPackageManager == null");
            if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) Log.d(LOG_TAG," camera feature == null");

            try {
                // Check file exists
                if (clueImageFile != null) {
                    Log.d(LOG_TAG, "clueImage != null");
                    //Give intent the save path
                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                            getActivity().getPackageName() + ".provider" ,      // This provider name is set in the manifest & must match, else there will be permissions errors
                            clueImageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    Log.d(LOG_TAG, "taking the picture");
                    //Take the picture
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (ActivityNotFoundException e) {
                // display error state to the user
                Log.d(LOG_TAG," Caught activity not found exception");
            } catch (Exception ex) {
                Log.e(LOG_TAG,"Caught exception when trying to make camera intent: " + ex.getMessage());
            }
        } else {
            Log.d(LOG_TAG,"Requesting Camera permission.");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Called when camera intent returns. Now load image just taken
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode != Activity.RESULT_CANCELED) {
            Log.d(LOG_TAG, "Camera activity returned.");
            setClueImageInView();
            removePhotoButton();
            Log.d(LOG_TAG, "Image set and button removed.");
        } else {
            Log.d(LOG_TAG, "resultCode = RESULT_CANCELLED, (or the request code did not match REQUEST_IMAGE_CAPTURE) so not doing anything.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getActivity(), "Camera permissions are required to take a picture of the clues.", Toast.LENGTH_LONG).show();
            }
        }
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

        Log.d(LOG_TAG,"clueImageView.getWidth() = " + clueImageViewTouch.getWidth());
        options.inSampleSize = calculateInSampleSize(options,clueImageViewTouch.getWidth());
        // Turn off justDecodeBounds so that the file is properly decoded
        options.inJustDecodeBounds = false ;

        return BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        Log.d(LOG_TAG, "Calculating bitmap sample size...");
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

    private void showOverwriteClueImageFileDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_overwrite_clue_image_message));
        builder.setPositiveButton(getResources().getString(R.string.dialog_overwrite), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dispatchTakePictureIntent();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    public void retakePicture() {
        if (clueImageFileExists()) {
            showOverwriteClueImageFileDialog();
        } else {
            dispatchTakePictureIntent();
        }
    }
}
