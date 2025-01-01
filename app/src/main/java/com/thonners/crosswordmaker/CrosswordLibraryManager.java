package com.thonners.crosswordmaker;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.thonners.crosswordmaker.ui.activities.CrosswordSliderActivity;
import com.thonners.crosswordmaker.ui.activities.HomeActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * CrosswordLibraryManager class
 * Class to control adding/deleting crosswords, and managing the list of recent crosswords.
 * Also controls opening crosswords from the Home or CrosswordLibraryActivity activities
 * <p>
 * Created by Thonners on 23/04/15.
 */

public class CrosswordLibraryManager {

    private static final String LOG_TAG = "CrosswordLibraryManager";
    private static final String RECENT_CROSSWORD_LOG_FILE_NAME = "recent.log";

    private Context context;
    private File[] foundCrosswordFiles;    // Note that this is the directory in which the
    // crossword and images (if they exist) will be saved
    private ArrayList<String> foundCrosswordFilePathsList = new ArrayList<>();
    private File rootDir;

    private File recentCrosswordsFile;

    private int noRecentCrosswords = 3;    // Number of recent crosswords to track

    private ArrayList<CrosswordTwo> savedCrosswords = new ArrayList<>();
    private ArrayList<File> savedCrosswordFiles = new ArrayList<>();
    private ArrayList<CrosswordTwo> recentCrosswords = new ArrayList<>();

    public CrosswordLibraryManager(Context context) {
        this.context = context;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootDir = context.getFilesDir();
        } else {
            rootDir = new File(Environment.getExternalStorageDirectory() + "/.CrosswordToolkit");
        }

        Log.d(LOG_TAG, "Root directory being used = " + rootDir.getPath());

    }

    public ArrayList<CrosswordTwo> getSavedCrosswords() {
        getSavedFiles();
        processSavedFiles();
        return savedCrosswords;
    }

    public ArrayList<CrosswordTwo> getRecentCrosswords() {
        getRecentFile();
        processRecentFile();
        return recentCrosswords;
    }

    private void getSavedFiles() {
        // Get list of saved files and add to foundCrosswordFiles
        try {
            Log.d(LOG_TAG, "Getting list of crossword files.");
            foundCrosswordFiles = rootDir.listFiles();
            //Arrays.sort(foundCrosswordFiles);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error getting file list: " + e.getMessage());
        }

        if (foundCrosswordFiles != null) {
            // Get File paths to allow sorting - file path should lead with date in YYYYMMDD, so
            // alphabetical sorting will result in them ordered by date
            String[] foundCrosswordFilePaths = new String[foundCrosswordFiles.length];
            for (int i = 0; i < foundCrosswordFilePaths.length; i++) {
                foundCrosswordFilePaths[i] = foundCrosswordFiles[i].getAbsolutePath();
            }
            Arrays.sort(foundCrosswordFilePaths);
            // Re-assign files in alphabetical/date order
            for (int j = 0; j < foundCrosswordFilePaths.length; j++) {
                foundCrosswordFiles[j] = new File(foundCrosswordFilePaths[j]);
            }


            // Output what files were found
            Log.d(LOG_TAG, "Directory searched: " + rootDir);
            for (int i = 0; i < foundCrosswordFiles.length; i++) {
                foundCrosswordFilePathsList.add(foundCrosswordFiles[i].getName());
                Log.d(LOG_TAG, "File at index " + i + " is " + foundCrosswordFiles[i].getName());
            }
        }
    }

    private void processSavedFiles() {
        if (foundCrosswordFiles != null) {
            for (int i = 0; i < foundCrosswordFiles.length; i++) {
                if (foundCrosswordFiles[i].isFile() && foundCrosswordFiles[i].getName().endsWith(("json"))) {
                    try {
                        addCrosswordToLibrary(CrosswordTwo.fromJsonFile(context,
                                foundCrosswordFiles[i].getCanonicalPath()));
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error processing saved file: " + e.getMessage());
                    }
                }
            }
        }

    }

    private void addCrosswordToLibrary(CrosswordTwo savedCrossword) {
        savedCrosswords.add(savedCrossword);
        savedCrosswordFiles.add(savedCrossword.getCrosswordFile());
    }

    public boolean crosswordAlreadyExists(String crosswordName, String crosswordDate) {
        String directoryName =
                crosswordDate + "-" + crosswordName.replaceAll(" ", "_").replaceAll("-", "__");
        getSavedFiles();
        Log.d(LOG_TAG, "Checking crossword file doesn't already exist for: " + directoryName);

        if (foundCrosswordFilePathsList.contains(directoryName)) {
            Log.d(LOG_TAG, "Crossword file found for: " + directoryName);
            return true;
        } else {
            Log.d(LOG_TAG, "No crossword file found for: " + directoryName);
            return false;
        }

    }

    private void getRecentFile() {
        // Extract recent file
        recentCrosswordsFile = new File(rootDir, RECENT_CROSSWORD_LOG_FILE_NAME);
    }

    private void processRecentFile() {
        // Divide recent file into last three crosswords and make each into a SavedCrossword object.
        if (recentCrosswordsFile.exists()) {
            Log.d(LOG_TAG,
                    "Trying to open and then read " + recentCrosswordsFile.getAbsolutePath());

            try {
                // Read file
                BufferedReader br = new BufferedReader(new FileReader(recentCrosswordsFile));
                String line;
                while ((line = br.readLine()) != null) {
                    //                    File newRecentCrosswordFile = new File(rootDir, line);
                    File newRecentCrosswordFile = new File(line);
                    CrosswordTwo crossword = CrosswordTwo.fromJsonFile(context,
                            newRecentCrosswordFile.getAbsolutePath());
                    recentCrosswords.add(crossword);
                    Log.d(LOG_TAG,
                            "Added a new entry to the recent crossword list: " + crossword.getActivityTitle() + " " + crossword.getDisplayDate());

                }
                br.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            Log.d(LOG_TAG, "No recent crossword file exists...");
        }
    }

    //    private void addCrosswordToRecents(SavedCrossword newRecentCrossword) {
    private void addCrosswordToRecents(CrosswordTwo newRecentCrossword) {

        //        ArrayList<SavedCrossword> oldRecentCrosswords = getRecentCrosswords();
        ArrayList<CrosswordTwo> oldRecentCrosswords = getRecentCrosswords();

        /* Debugging */
        for (CrosswordTwo s : oldRecentCrosswords) {
            Log.d(LOG_TAG, "oldRecentCrosswords contains: " + s.getCrosswordFile().getName());
        }

        if (oldRecentCrosswords.contains(newRecentCrossword)) {
            Log.d(LOG_TAG,
                    "Removing " + newRecentCrossword + "from oldRecentCrosswords because " + "it "
                            + "was already in there...");
            oldRecentCrosswords.remove(newRecentCrossword);
        }
        Log.d(LOG_TAG, "RecentCrosswords.size() = " + recentCrosswords.size());

        // Reset recent crosswords list
        recentCrosswords = new ArrayList<>();
        // Add newest crossword to top of the list
        recentCrosswords.add(newRecentCrossword);
        for (CrosswordTwo c : oldRecentCrosswords) {
            if (!c.getCrosswordFile().getName().matches(newRecentCrossword.getCrosswordFile().getName())) {
                recentCrosswords.add(c);
            } else {
                Log.d(LOG_TAG, "ignoring " + c.getCrosswordFile().getName() + "in old crosswords,"
                        + " as it is the latest");
            }
        }

        /* Debugging */
        for (CrosswordTwo c : recentCrosswords) {
            Log.d(LOG_TAG, "recentCrosswords now contains: " + c.getCrosswordFile().getName());
        } //*/


        saveRecentFile();
    }

    private void saveRecentFile() {
        getRecentFile();

        try {
            Log.d(LOG_TAG, "Writing recent crosswords file...");
            FileWriter fileWriter = new FileWriter(recentCrosswordsFile);

            for (int i = 0; i < Math.min(recentCrosswords.size(), noRecentCrosswords); i++) {
                fileWriter.write(recentCrosswords.get(i).getCrosswordFile().getCanonicalPath() +
                        "\n");
            }
            fileWriter.close();


        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't open fileWriter to save the recent crosswords file.");
        }
    }

    public void openCrossword(File crosswordFile) {

        String[] savedCrosswordArray = null;

        try {
            if (crosswordFile.exists()) {
                addCrosswordToRecents(CrosswordTwo.fromJsonFile(context,
                        crosswordFile.getAbsolutePath()));
                Log.d(LOG_TAG,
                        "Starting CrosswordSliderActivity with saved crossword: " + crosswordFile.getName());
                openCrossword(crosswordFile.getAbsolutePath());
            } else {
                Log.e(LOG_TAG, "Selected 'saved crossword' doesn't seem to exist");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception thrown when trying to get file: " + e.getMessage());
        }
    }

    private void openCrossword(String savedCrosswordFilePath) {

        showLoadingToast();
        Log.d(LOG_TAG, "Opening crossword " + savedCrosswordFilePath);
        // Start new crossword activity
        Intent crosswordActivity = new Intent(context, CrosswordSliderActivity.class);
        crosswordActivity.putExtra(CrosswordTwo.CROSSWORD_EXTRA, savedCrosswordFilePath);
        // TODO: Remove the GridMaker activity from the back button history so if the main page
        //  crashes it goes back to Home
        context.startActivity(crosswordActivity);
    }

    private void openCrossword(String[] savedCrossword) { // Deprecated

        showLoadingToast();
        Log.d(LOG_TAG,
                "Opening crossword " + savedCrossword[Crossword.SAVED_ARRAY_INDEX_TITLE] + " " + savedCrossword[Crossword.SAVED_ARRAY_INDEX_DATE]);
        // Start new crossword activity
        Intent crosswordActivity = new Intent(context, CrosswordSliderActivity.class);
        crosswordActivity.putExtra(Crossword.CROSSWORD_EXTRA, savedCrossword);
        context.startActivity(crosswordActivity);
    }

    private void showLoadingToast() {
        // Display loading toast as load can take a while
        Toast loadingToast = Toast.makeText(context, context.getString(R.string.loading),
                Toast.LENGTH_LONG);
        loadingToast.show();
    }

    public void openEditCrossword(File savedCrosswordDir) {
        Log.d(LOG_TAG, "Opening crossword for editing from file: " + savedCrosswordDir.getPath());
        try {
            File crossword = new File(savedCrosswordDir, Crossword.SAVE_CROSSWORD_FILE_NAME);
            openEditCrossword(Crossword.getSaveArray(crossword));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception thrown when trying to get file: " + e.getMessage());
        }
    }

    private void openEditCrossword(String[] savedCrossword) {
        Log.d(LOG_TAG,
                "Opening crossword for editing" + savedCrossword[Crossword.SAVED_ARRAY_INDEX_TITLE] + " " + savedCrossword[Crossword.SAVED_ARRAY_INDEX_DATE]);
        // Start new crossword activity
        Intent editCrosswordActivity = new Intent(context, CrosswordGridEditor.class);
        editCrosswordActivity.putExtra(Crossword.CROSSWORD_EXTRA, savedCrossword);
        context.startActivity(editCrosswordActivity);
    }

    private void removeFromRecents(CrosswordTwo crosswordToBeRemoved) {
        ArrayList<CrosswordTwo> oldRecentCrosswords = getRecentCrosswords();
        recentCrosswords = new ArrayList<>();
        for (CrosswordTwo saved : oldRecentCrosswords) {
            if (!saved.getCrosswordFile().getName().matches(crosswordToBeRemoved.getCrosswordFile().getName())) {
                // save new array without the crossword that's being removed
                recentCrosswords.add(saved);
            }
        }
        saveRecentFile();
    }

    public void deleteSavedCrossword(File savedCrosswordFile) {
        try {
            CrosswordTwo savedCrossword = CrosswordTwo.fromJsonFile(context,
                    savedCrosswordFile.getAbsolutePath());

            Log.d(LOG_TAG, "Call to delete crossword received. Removing from recent crosswords.");
            removeFromRecents(savedCrossword);

            savedCrossword.deleteCrosswordFile();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Exception raised whilst deleting crossword: " + e.getMessage());
        }

        Log.d(LOG_TAG, "Reopening home activity.");
        returnHome();
    }

    private void returnHome() {
        // Reopen home activity after deleting crossword
        Intent openHome = new Intent(context, HomeActivity.class);
        openHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(openHome);
    }

    public static class SavedCrossword {
        Context context;
        File crosswordFile;
        String title;
        String saveDate;
        String displayDate;
        String percentageComplete;

        public SavedCrossword(Context context, File crosswordFile) {
            this.context = context;
            this.crosswordFile = crosswordFile;
            String[] crosswordDetails = crosswordFile.getName().split("-");
            this.saveDate = crosswordDetails[0];   // Date extracted from saved file name
            this.displayDate = Crossword.getDisplayDate(context, crosswordDetails[0]); // Convert
            // displayDate from saved YYYYMMDD format into locale display displayDate
            this.title = crosswordDetails[1].replaceAll("__", "-").replaceAll("_", " ");   //
            // Replace all used to restore any hyphens/spaces that were taken out during the
            // fileName assignment in Crossword.initialiseSaveFiles
            this.percentageComplete = calculatePercentageComplete(crosswordFile);
        }

        private String calculatePercentageComplete(File crosswordDir) {
            // Return the percentage of the crossword that's complete (based on number of blanks
            // in the file)
            File crosswordFile = new File(crosswordDir, Crossword.SAVE_CROSSWORD_FILE_NAME);
            if (crosswordFile == null) {
                Log.d(LOG_TAG, "Error finding crossword file in  " + crosswordDir.getName());
                percentageComplete = "Error finding file";
            }
            Log.d(LOG_TAG, "Calculating percentage completion for " + crosswordDir.getName());

            String[] crosswordArray = Crossword.getSaveArray(crosswordFile);
            int cells = 0, nonBlanks = 0; // Integers to count with
            for (int i = Crossword.SAVE_ARRAY_START_INDEX; i < crosswordArray.length; i++) {
                // If a black cell, don't include it in the counting
                if (!crosswordArray[i].matches("-")) {
                    cells++;
                    // Check to see whether the cell is empty or filled
                    if (!crosswordArray[i].matches("")) {
                        nonBlanks++;
                    }
                }
            }

            Log.d(LOG_TAG, "Number of cells: " + cells + " & number of nonBlanks = " + nonBlanks);

            int percentage;
            if (cells > 0) {
                percentage = (nonBlanks * 100) / cells;
            } else {
                percentage = 0;
            }

            return percentage + "";
        }

        public String getTitle() {
            return title;
        }

        public String getDisplayDate() {
            return displayDate;
        }

        public String getDisplayPercentageComplete() {
            return context.getString(R.string.completion) + " " + percentageComplete + "%";
        }

        public String getSaveString() {
            return saveDate + "-" + title.replaceAll(" ", "_").replaceAll("-", "__");
            // return Crossword.getSaveDate(context, displayDate) + "-" + title.replaceAll(" ",
            // "_").replaceAll("-","__");
        }

        public void deleteCrossword() {
            Log.d(LOG_TAG, "Delete called on " + getSaveString());
            Log.d(LOG_TAG, "Looping through & deleting files.");
            // Delete crossword save directory and all files contained within
            File[] files = crosswordFile.listFiles();
            for (File f : files) {
                if (f != null) {
                    Log.d(LOG_TAG, "Deleting file at: " + f.getPath());
                    f.delete();
                }
            }
            Log.d(LOG_TAG, "Deleting parent directory: " + crosswordFile.getPath());
            crosswordFile.delete();
        }

        public File getCrosswordFile() {
            return crosswordFile;
        }
    }


}
