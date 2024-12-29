package com.thonners.crosswordmaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;

import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CluePageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CluePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CluePageFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "CluePageFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;    // Int used in camera intent
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static int MIN_CLUE_IMAGE_RES = 100;  // Minimum number of pixels for clue image
    // width to use for resampling
    private static final String ARG_FILE_PATH = "filePath";

    private TouchImageView clueImageViewTouch;
    private View takeCluePhotoButton;
    private Uri clueImageUri = Uri.EMPTY;


    // GetContent creates an ActivityResultLauncher<String> to let you pass in the mime type you
    // want to let the user select
    private final ActivityResultLauncher<String> mGetContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            // Handle the returned Uri
            Log.d(LOG_TAG, "Received callback from activity result with URI: " + uri.toString());
        }
    });
    private final ActivityResultLauncher<Uri> mGetImageFromCamera =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            Log.d(LOG_TAG,
                    "Camera callback from activity result with boolean: " + result.toString());
            if (result) {
                clueImageInterface.setClueImage(clueImageUri);
                Log.d(LOG_TAG, "Image URI: " + getImageUri().toString());
                clueImageViewTouch.setImageURI(getImageUri());
                clueImageViewTouch.invalidate();
                removePhotoButton();
                Log.d(LOG_TAG, "Image set and button removed.");
            } else {
                Log.w(LOG_TAG, "Camera app was unable to save image to URI: " + getImageUri());
            }
        }
    });
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
        if (isGranted) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(getActivity(), "Camera permissions are required to take a picture " +
                    "of the clues.", Toast.LENGTH_LONG).show();
        }
    });
    private CrosswordTwo.CrosswordClueImageInterface clueImageInterface;
    private CrosswordTwo.CrosswordTitleInterface titleInterface;

    public static CluePageFragment newInstance(String crosswordFilePath,
                                               CrosswordTwo.CrosswordClueImageInterface clueImageInterface, CrosswordTwo.CrosswordTitleInterface titleInterface) {
        Log.d(LOG_TAG, "CrosswordFilePath for clue image: " + crosswordFilePath);
        CluePageFragment fragment = new CluePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, crosswordFilePath);
        fragment.setArguments(args);
        fragment.setClueImageInterface(clueImageInterface);
        fragment.setTitleInterface(titleInterface);
        return fragment;
    }

    private void setTitleInterface(CrosswordTwo.CrosswordTitleInterface titleInterface) {
        this.titleInterface = titleInterface;
    }

    private void setClueImageInterface(CrosswordTwo.CrosswordClueImageInterface clueImageInterface) {
        this.clueImageInterface = clueImageInterface;
    }

    public CluePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate called");
        Log.d(LOG_TAG, "clueImageInterface = " + clueImageInterface.getClueImageUri().toString());
        // TODO: Remove the crossword file path string from the bundle as we don't need it
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_clues, container, false);

        initialise(view);

        return view;
    }

    private void initialise(View view) {

        Log.d(LOG_TAG, "Initialising...");
        takeCluePhotoButton = view.findViewById(R.id.take_picture_clues_button);
        clueImageViewTouch = view.findViewById(R.id.image_view_clues);
        clueImageViewTouch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Offer option to retake clues picture if user long-clicks
                Log.d(LOG_TAG, "Long-click on clue image detected");
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
                // If no picture file found and device doesn't have camera availability, display
                // error message
                TextView textView = view.findViewById(R.id.take_picture_clues_text_view);
                textView.setText(getResources().getString(R.string.take_clue_picture_error));
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    private Uri getImageUri() {
        return clueImageInterface.getClueImageUri();
    }

    private void setClueImageInView() {
        Log.d(LOG_TAG, "Setting image");
        Log.d(LOG_TAG, "Uri: " + getImageUri().toString());
        clueImageViewTouch.setImageURI(getImageUri());
        clueImageViewTouch.invalidate();
        Log.d(LOG_TAG, "Image should be set.");
        clueImageViewTouch.setZoom(2.0f);
        clueImageViewTouch.setZoom(1.0f);
    }

    private void removePhotoButton() {
        //Remove button from view
        if (takeCluePhotoButton != null) {
            Log.d(LOG_TAG, "Removing photo button");
            ((ViewGroup) takeCluePhotoButton.getParent()).removeView(takeCluePhotoButton);
            takeCluePhotoButton = null;    // Force to null. Not sure what it would be without this.
        }
    }

    private boolean clueImageFileExists() {
        Uri uri = getImageUri();
        return (getImageUri() != null && !getImageUri().toString().matches(Uri.EMPTY.toString()));
    }


    public void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Camera permissions granted. Starting the takePictureIntent");
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) == null)
                Log.d(LOG_TAG, " resolveImageIntent == null");
            if (getActivity().getPackageManager() == null)
                Log.d(LOG_TAG, " getPackageManager == null");
            if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
                Log.d(LOG_TAG, " camera feature == null");

            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, titleInterface.getTitle());
                values.put(MediaStore.Images.Media.DESCRIPTION, "Clue image");
                clueImageUri =
                        requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Log.d(LOG_TAG, " Trying to save URI as: " + clueImageUri.toString());
                mGetImageFromCamera.launch(clueImageUri);
            } catch (ActivityNotFoundException e) {
                // display error state to the user
                Log.d(LOG_TAG, " Caught activity not found exception");
            } catch (Exception ex) {
                Log.e(LOG_TAG,
                        "Caught exception when trying to make camera intent: " + ex.getMessage());
            }
        } else {
            Log.d(LOG_TAG, "Requesting Camera permission.");
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showOverwriteClueImageFileDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_overwrite_clue_image_message));
        builder.setPositiveButton(getResources().getString(R.string.dialog_overwrite),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dispatchTakePictureIntent();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {
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
