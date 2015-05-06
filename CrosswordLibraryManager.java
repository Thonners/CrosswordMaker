package com.thonners.crosswordmaker;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by mat on 23/04/15.
 */
public class CrosswordLibraryManager {

    private static final String LOG_TAG = "CrosswordLibraryManager" ;
    private static final String RECENT_CROSSWORD_LOG_FILE_NAME = "recent.log";

    Context context ;
    File[] foundCrosswordFiles;    // Note that this is the directory in which the crossword and images (if they exist) will be saved
    File rootDir;

    File recentCrosswordsFile ;
    String recentCrosswordFileName ;

    int noRecentCrosswords = 3 ;    // Number of recent crosswords to track

    ArrayList<SavedCrossword> savedCrosswords = new ArrayList<SavedCrossword>() ;
    ArrayList<File> savedCrosswordFiles =new ArrayList<File>();
    ArrayList<SavedCrossword> recentCrosswords = new ArrayList<SavedCrossword>();

    public CrosswordLibraryManager(Context context) {
        this.context = context;
    }

    public ArrayList<SavedCrossword> getSavedCrosswords() {
        getSavedFiles();
        processSavedFiles();
        return savedCrosswords;
    }
    public ArrayList<SavedCrossword> getRecentCrosswords() {
        getRecentFile();
        processRecentFile();
    return recentCrosswords ;
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

    private void getRecentFile() {
        // Extract recent file
        recentCrosswordsFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),RECENT_CROSSWORD_LOG_FILE_NAME);
    }
    private void processRecentFile() {
        // Divide recent file into last three crosswords and make each into a SavedCrossword object.
        if (recentCrosswordsFile.exists()) {
            Log.d(LOG_TAG,"Trying to open and then read " + recentCrosswordsFile.getAbsolutePath());

            try {
                // Read file
                BufferedReader br = new BufferedReader(new FileReader(recentCrosswordsFile));
                String line ;
                while ((line = br.readLine()) != null) {
                    File newRecentCrosswordFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),line);
                    SavedCrossword newRecentSavedCrossword = new SavedCrossword(context, newRecentCrosswordFile);
                    recentCrosswords.add(newRecentSavedCrossword);
                    Log.d(LOG_TAG, "Added a new entry to the recent crossword list: " + newRecentSavedCrossword.getTitle() + " " + newRecentSavedCrossword.getDate());
                }
                br.close() ;

            }catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            Log.d(LOG_TAG, "No recent crossword file exists...");
        }
    }
    private void addCrosswordToRecents(SavedCrossword newRecentCrossword) {

        ArrayList<SavedCrossword> oldRecentCrosswords = getRecentCrosswords();

        /* Debugging*/
        for (SavedCrossword s : oldRecentCrosswords) {
            Log.d(LOG_TAG,"oldRecentCrosswords contains: " + s.getSaveString());
        }

        if (oldRecentCrosswords.contains(newRecentCrossword)) {
            Log.d(LOG_TAG,"Removing " + newRecentCrossword + "from oldRecentCrosswords because it was already in there..." );
            oldRecentCrosswords.remove(newRecentCrossword);
        }
            Log.d(LOG_TAG,"RecentCrosswords.size() = " + recentCrosswords.size() );
//*/

        // Reset recent crosswords list
        recentCrosswords = new ArrayList<SavedCrossword>() ;
        // Add newest crossword to top of the list
        recentCrosswords.add(newRecentCrossword);
        for (SavedCrossword s : oldRecentCrosswords) {
            if (!s.getSaveString().matches(newRecentCrossword.getSaveString())) {
                recentCrosswords.add(s);
            } else {
                Log.d(LOG_TAG, "ignoring " + s.getSaveString() + "in old crosswords, as it is the latest");
            }
        }
        //recentCrosswords.addAll(oldRecentCrosswords);

         /* Debugging */
        for (SavedCrossword s : recentCrosswords) {
            Log.d(LOG_TAG,"recentCrosswords now contains: " + s.getSaveString());
        }


        saveRecentFile();
    }
    private void saveRecentFile() {
        getRecentFile();

        try {
            Log.d(LOG_TAG, "Writing recent crosswords file...");
            FileWriter fileWriter = new FileWriter(recentCrosswordsFile);

            for (int i = 0 ; i < Math.min(recentCrosswords.size(),noRecentCrosswords) ; i++) {
                fileWriter.write(recentCrosswords.get(i).getSaveString() + "\n");
            }
            fileWriter.close();


        }catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't open fileWriter to save the recent crosswords file.");
        }
    }

    public void openCrossword(File savedCrosswordDir) {

        addCrosswordToRecents(new SavedCrossword(context, savedCrosswordDir));

        String[] savedCrosswordArray = null ;

        try {
            File crossword = new File(savedCrosswordDir, Crossword.SAVE_CROSSWORD_FILE_NAME);

            if(crossword.exists()) {
                savedCrosswordArray = Crossword.getSaveArray(crossword);
            } else {
                Log.e(LOG_TAG,"Selected 'saved crossword' doesn't seem to exist");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,"Exception thrown when trying to get file: " + e.getMessage());
        }

        if (savedCrosswordArray != null) {
            Log.d(LOG_TAG, "Starting CrosswordActivity with saved crossword: " + savedCrosswordArray[Crossword.SAVED_ARRAY_INDEX_TITLE]);
            openCrossword(savedCrosswordArray);
        }
    }
    private void openCrossword(String[] savedCrossword) {

        showLoadingToast();
            Log.d(LOG_TAG,"Opening crossword " + savedCrossword[Crossword.SAVED_ARRAY_INDEX_TITLE] + " " + savedCrossword[Crossword.SAVED_ARRAY_INDEX_DATE]);
        // Start new crossword activity
        Intent crosswordActivity = new Intent(context, CrosswordSliderActivity.class);
        crosswordActivity.putExtra(Crossword.CROSSWORD_EXTRA, savedCrossword);
        context.startActivity(crosswordActivity);
    }
    private void showLoadingToast() {
        // Display loading toast as load can take a while
        Toast loadingToast = Toast.makeText(context, context.getString(R.string.loading), Toast.LENGTH_LONG);
        loadingToast.show();
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
        public String getSaveString() {
            return Crossword.getSaveDate(context, date) + "-" + title.replaceAll(" ","_").replaceAll("-","__");
        }
        public File getCrosswordFile() {
            return crosswordFile;
        }
    }


}
