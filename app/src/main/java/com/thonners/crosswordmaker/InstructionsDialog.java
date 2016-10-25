package com.thonners.crosswordmaker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * DialogFragment to show instructions on how to use the app.
 *
 * At creation, only showing written instructions on how to use manual anagram.
 *
 * @author M Thomas
 * @since 25/10/16
 */

public class InstructionsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use builder to make dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()) ;
        // Add text
        builder.setTitle(R.string.instructions)
                .setMessage(R.string.manual_anagram_instructions) ;

        // Create and return the Dialog
        return builder.create() ;
    }

}
