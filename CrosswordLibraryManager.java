package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mat on 23/04/15.
 */
public class CrosswordLibraryManager {

    private static final String LOG_TAG = "CrosswordLibraryManager" ;

    Context context ;
    File[] foundCrosswordFiles;    // Note that this is the directory in which the crossword and images (if they exist) will be saved
    File rootDir;

    ArrayList<SavedCrossword> savedCrosswords = new ArrayList<SavedCrossword>() ;
    ArrayList<File> savedCrosswordFiles =new ArrayList<File>();

    public CrosswordLibraryManager(Context context) {
        this.context = context;

        getSavedFiles();
        processSavedFiles();
    }

    public ArrayList<SavedCrossword> getSavedCrosswords() {
        return savedCrosswords;
    }

    private void getSavedFiles() {
        // Get list of saved files and add to foundCrosswordFiles
        try {
            Log.d(LOG_TAG, "Getting list of crossword files.");
            rootDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            foundCrosswordFiles = rootDir.listFiles();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error getting file list: " + e.getMessage());
        }

        // Output what files were found
        Log.d(LOG_TAG,"Directory searched: " + rootDir);
        for (int i =0 ; i < foundCrosswordFiles.length ; i++) {
            Log.d(LOG_TAG,"File at index " + i + " is " + foundCrosswordFiles[i].getName());
        }
    }
    private void processSavedFiles() {
        for (int i = 0 ; i < foundCrosswordFiles.length ; i++ ) {
            if (foundCrosswordFiles[i].isDirectory() && foundCrosswordFiles[i].getName().contains("-")) {
                addCrosswordToLibrary(new SavedCrossword(context, foundCrosswordFiles[i]));
            }
        }

    }

    private void addCrosswordToLibrary(SavedCrossword savedCrossword) {
        savedCrosswords.add(savedCrossword);
        savedCrosswordFiles.add(savedCrossword.getCrosswordFile());
    }

    static class SavedCrossword {
        Context context ;
        File crosswordFile;
        String title ;
        String date ;
        String percentageComplete ;

        public SavedCrossword(Context context, File crosswordDir) {
            this.context = context ;
            this.crosswordFile = crosswordDir ;
            String[] crosswordDetails = crosswordDir.getName().split("-");
            this.date = Crossword.getDisplayDate(context, crosswordDetails[0]) ; // Convert date from saved YYYYMMDD format into locale display date
            this.title = crosswordDetails[1].replaceAll("__", "-").replaceAll("_", " ");   // Replace all used to restore any hyphens/spaces that were taken out during the fileName assignment in Crossword.initialiseSaveFiles
            this.percentageComplete = calculatePercentageComplete(crosswordDir);
        }

        private String calculatePercentageComplete(File crosswordDir) {
            // Return the percentage of the crossword that's complete (based on number of blanks in the file)
            File crosswordFile = new File(crosswordDir, Crossword.SAVE_CROSSWORD_FILE_NAME);
            if (crosswordFile == null) {
                Log.d(LOG_TAG, "Error finding crossword file in  " + crosswordDir.getName());
                percentageComplete = "Error finding file" ;
            }
            Log.d(LOG_TAG, "Calculating percentage completion for " + crosswordDir.getName());

            String[] crosswordArray = Crossword.getSaveArray(crosswordFile);
            int cells = 0, nonBlanks = 0 ; // Integers to count with
            for (int i = Crossword.SAVE_ARRAY_START_INDEX ; i < crosswordArray.length ; i++) {
                // If a black cell, don't include it in the counting
                if (!crosswordArray[i].matches("-")) {
                    cells++ ;
                    // Check to see whether the cell is empty or filled
                    if (!crosswordArray[i].matches("")) {
                        nonBlanks++;
                    }
                }
            }

            Log.d(LOG_TAG, "Number of cells: " + cells + " & number of nonBlanks = " + nonBlanks);

            int percentage = (int) (nonBlanks * 100) / cells ;

            return percentage + "";
        }
        public String getTitle() {
            return title;
        }
        public String getDate() {
            return date;
        }
        public String getPercentageComplete() {
            return percentageComplete;
        }
        public String getDisplayPercentageComplete() {
            return context.getString(R.string.completion) + " " + percentageComplete + "%" ;
        }
        public File getCrosswordFile() {
            return crosswordFile;
        }
    }


}
