package com.thonners.crosswordmaker.ui.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import com.thonners.crosswordmaker.data.crossword.CrosswordTwo;
import com.thonners.crosswordmaker.R;
import com.thonners.crosswordmaker.ui.components.TouchImageView;
import com.thonners.crosswordmaker.ui.activities.HomeActivity;


/**
 * A {@link Fragment} subclass to display the clues.
 */
public class CluePageFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "CluePageFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;    // Int used in camera intent
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static int MIN_CLUE_IMAGE_RES = 100;  // Minimum number of pixels for clue image
    // width to use for resampling
    private static final String ARG_FILE_PATH = "filePath";

    private TouchImageView clueImageViewTouch;
    private View getCluesLayout;
    private View takeCluePhotoButton;
    private View fromGalleryButton;
    private Uri clueImageUri = Uri.EMPTY;

    private CrosswordTwo.CrosswordClueImageInterface clueImageInterface;
    private CrosswordTwo.CrosswordTitleInterface titleInterface;

    // Create an ActivityLauncher to select an image from the gallery.
    private final ActivityResultLauncher<String[]> getImageFromGallery =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
        if (uri != null) {
            // Persistable URI permission granted (GetContent() only gives us one-time-use
            // permissions, OpenDocument lets us keep the access permission for next time.
            Log.d(LOG_TAG, "Received callback from activity result with URI: " + uri);
            clueImageInterface.setClueImage(uri);
            Log.d(LOG_TAG, "Image URI: " + getImageUri().toString());
            setClueImageInView();
            // Take persistable permission so that we can open this image next time the user
            // opens the app:
            final int takeFlags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            requireActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } else {
            // User canceled the selection
            Log.d(LOG_TAG, "User cancelled the selection - received null URI from picker.");
        }
    });
    private final ActivityResultLauncher<Uri> getImageFromCamera =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            Log.d(LOG_TAG,
                    "Camera callback from activity result with boolean: " + result.toString());
            if (result) {
                clueImageInterface.setClueImage(clueImageUri);
                Log.d(LOG_TAG, "Image URI: " + getImageUri().toString());
                setClueImageInView();
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
        getCluesLayout = view.findViewById(R.id.get_clues_layout);
        takeCluePhotoButton = view.findViewById(R.id.take_picture_clues_button);
        fromGalleryButton = view.findViewById(R.id.get_library_clues_button);
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

        if (HomeActivity.deviceHasCameraCapability(requireActivity())) {
            takeCluePhotoButton.setOnClickListener(v -> {
                Log.d(LOG_TAG, "Take picture button pressed");
                dispatchTakePictureIntent();
            });
            fromGalleryButton.setOnClickListener(v -> {
                Log.d(LOG_TAG, "From gallery button pressed");
                getImageFromGallery.launch(new String[]{"image/*"});
            });

        } else {
            // If device doesn't have camera availability, display
            // error message
            TextView textView = view.findViewById(R.id.take_picture_clues_text_view);
            textView.setText(getResources().getString(R.string.take_clue_picture_error));
        }

        if (clueImageFileExists()) {
            setClueImageInView();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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
        Log.d(LOG_TAG, "Removing 'new image' buttons/view'.");
        hideGetCluesLayout();
    }

    private void hideGetCluesLayout() {
        // Hide layout from view
        if (getCluesLayout != null) {
            Log.d(LOG_TAG, "Removing getCluesLayout");
            // Just hide it so we can bring it back if the user wants to take another picture...
            getCluesLayout.setVisibility(View.GONE);
        }
    }

    public void removeImage() {
        Log.d(LOG_TAG, "Removing image");
        clueImageInterface.setClueImage(Uri.EMPTY);
        getCluesLayout.setVisibility(View.VISIBLE);
    }

    private boolean clueImageFileExists() {
        Uri uri = getImageUri();
        return (uri != null && !uri.toString().matches(Uri.EMPTY.toString()));
    }

    public void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchPictureIntent method started");
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Camera permissions granted. Starting the takePictureIntent");
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, titleInterface.getTitle());
                values.put(MediaStore.Images.Media.DESCRIPTION, "Clue image");
                clueImageUri =
                        requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Log.d(LOG_TAG, " Trying to save URI as: " + clueImageUri);
                getImageFromCamera.launch(clueImageUri);
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
        builder.setTitle(getResources().getString(R.string.dialog_remove_clue_image_message));
        builder.setPositiveButton(getResources().getString(R.string.dialog_remove),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeImage();
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
