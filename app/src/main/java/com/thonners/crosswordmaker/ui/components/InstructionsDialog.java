package com.thonners.crosswordmaker.ui.components;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.thonners.crosswordmaker.R;

/**
 * DialogFragment to show instructions on how to use the app.
 * <p>
 * At creation, only showing written instructions on how to use manual anagram.
 *
 * @author M Thomas
 * @since 25/10/16
 */

public class InstructionsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use builder to make dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add text
        builder.setTitle(R.string.instructions).setMessage(R.string.manual_anagram_instructions);

        // Create and return the Dialog
        return builder.create();
    }

}
