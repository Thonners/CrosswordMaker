package com.thonners.crosswordmaker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

/**
 *  Dialog Fragment to offer options of common crossword publications
 *
 * Created by mat on 17/01/15.
 */
public class PublicationNameDialogFragment extends DialogFragment{

    String publicationSelected ;
    CharSequence[] publications = getResources().getTextArray(R.array.publications);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the dialog title
        builder.setTitle(R.string.pub_name_dialog_title) ;
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected

        builder.setSingleChoiceItems(publications, -1,
                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pub) {

                                switch (pub) {

                                    // Set publicationSelected equal to the string displayed
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
                                    // User clicked OK, so save the publicationSelected result somewhere
                                    // or return it to the component that opened the dialog

                                }
                            }

                            ) ;
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface dialog,int id){
                                    // Do nothing
                                }
                            }

                            );

                            return builder.create();
                        }
    }
