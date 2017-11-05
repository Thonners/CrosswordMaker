package com.thonners.crosswordmaker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import static android.R.attr.bitmap;

/**
 * Class to generate a crossword grid from an input image file.
 *
 * @author M Thomas
 * @since 30/07/17
 */

public class AutoGridGenerator {

    private final static int X = 0, Y = 1; // Coord indices

    public AutoGridGenerator() {
    }

    /**
     * Creates a boolean array to represent the crossword grid
     * @param image The image of the grid
     * @param gridSize The number of cells per row / column of the grid
     * @return The crossword grid, where false indicates black cell and true indicates white
     */
    public boolean[] getGridFromImage(Bitmap image, int gridSize) {
        // Image resolution
        int imageRes = image.getWidth() ;
        // Approx no. pixels per cell
        int cellRes = imageRes / gridSize ;
        // Int array into which the average
        int[][] gridAve = new int[gridSize][gridSize] ;
        // Boolean array into which to save the grid
        boolean[] grid = new boolean[gridSize*gridSize] ;
        for (int col = 0 ; col < gridSize ; col++) {
            for (int row = 0 ; row < gridSize ; row++){
                int total = 0 ;
                int x = col * cellRes ;
                int y = row * cellRes ;
                for (int i = 0 ; i < cellRes ; i++) {
                    for (int j = 0 ; j < cellRes ; j++) {
                        total += (Color.red(image.getPixel(x + i, y+j)) + Color.blue(image.getPixel(x + i, y+j)) + Color.green(image.getPixel(x + i, y+j))) / 3 ;
                    }
                }
                int average = total / (cellRes * cellRes) ;
                gridAve[col][row] = average ;
                Log.d("AGG", "col = " + col + ", row = " + row + ", total = " + total + ", ave = " + average) ;
            }
        }

        for (int col = 0 ; col < gridSize ; col++) {
            for (int row = 0 ; row < gridSize ; row++) {
                int netTotal = gridAve[Math.max(0,col-1)][Math.max(0,row-1)] + gridAve[col][Math.max(0,row-1)] + gridAve[Math.min(gridSize-1,col+1)][Math.max(0,row-1)] ;
                netTotal += gridAve[Math.max(0,col-1)][row] + gridAve[col][row] + gridAve[Math.min(gridSize-1,col+1)][row] ;
                netTotal += gridAve[Math.max(0,col-1)][Math.min(gridSize-1,row+1)] + gridAve[col][Math.min(gridSize-1,row+1)] + gridAve[Math.min(gridSize-1,col+1)][Math.min(gridSize-1,row+1)] ;
                int netAve = netTotal / 9 ;
                int index = col + row*gridSize;
                if (gridAve[col][row] < netAve) {
                    grid[index] = false ;
                } else {
                    grid[index] = true ;
                }
                Log.d("AGG", "col = " + col + ", row = " + row + ", netTotal = " + netTotal + ", netAve = " + netAve + " & therefore: white cell = " + grid[index]) ;
            }
        }

        for (int col = 0 ; col <= gridSize/2 ; col++) {
            for (int row = 0 ; row <= gridSize/2 ; row++) {
                int oppRow = gridSize - row - 1;
                int oppCol = gridSize - col - 1;
                int netTotal = gridAve[Math.max(0,col-1)][Math.max(0,row-1)] + gridAve[col][Math.max(0,row-1)] + gridAve[Math.min(gridSize-1,col+1)][Math.max(0,row-1)] ;
                netTotal += gridAve[Math.max(0,col-1)][row] + gridAve[col][row] + gridAve[Math.min(gridSize-1,col+1)][row] ;
                netTotal += gridAve[Math.max(0,col-1)][Math.min(gridSize-1,row+1)] + gridAve[col][Math.min(gridSize-1,row+1)] + gridAve[Math.min(gridSize-1,col+1)][Math.min(gridSize-1,row+1)] ;

                int netTotalOpp = gridAve[Math.max(0,oppCol-1)][Math.max(0,oppRow-1)] + gridAve[oppCol][Math.max(0,oppRow-1)] + gridAve[Math.min(gridSize-1,oppCol+1)][Math.max(0,oppRow-1)] ;
                netTotalOpp += gridAve[Math.max(0,oppCol-1)][oppRow] + gridAve[oppCol][oppRow] + gridAve[Math.min(gridSize-1,oppCol+1)][oppRow] ;
                netTotalOpp += gridAve[Math.max(0,oppCol-1)][Math.min(gridSize-1,oppRow+1)] + gridAve[col][Math.min(gridSize-1,oppRow+1)] + gridAve[Math.min(gridSize-1,oppCol+1)][Math.min(gridSize-1,oppRow+1)] ;

                double netAve = netTotal / 9.0 ;
                double netAveOpp = netTotalOpp / 9.0 ;

                int index = col + row*gridSize;
                int indexOpp = oppCol + oppRow*gridSize;

                // Doesn't matter if they're the same or not, always take the one with the bigger delta from the mean. ( if they're the same then it doesn't matter which one to use)
//                 //Check whether the results from the comparison is the same
//                if ((gridAve[col][row] > netAve) ^ (gridAve[oppCol][oppRow] > netAveOpp)) {
                    // If they're different, pick the main one
                double diff = netAve - gridAve[col][row] ;
                double oppDiff = netAveOpp - gridAve[oppCol][oppRow] ;
                boolean whiteCell ;
                if (Math.abs(diff) >= Math.abs(oppDiff)) {
                    // If the difference between mean and individual value is greater for the original cell, assume that is the correct answer
                    whiteCell = gridAve[col][row] > netAve;
                } else {
                    whiteCell = gridAve[oppCol][oppRow] > netAveOpp;
                }
                grid[index] = whiteCell;
                grid[indexOpp] = whiteCell;
//                } else {
//                        grid[index] = gridAve[col][row] > netAve;
//                        grid[indexOpp] = gridAve[col][row] > netAve;
//
//                }

//                int index = col + row*gridSize;
//                if (gridAve[col][row] < netAve) {
//                    grid[index] = false ;
//                } else {
//                    grid[index] = true ;
//                }
                Log.d("AGG", "col = " + col + ", row = " + row + ", netTotal = " + netTotal + ", netAve = " + netAve + " & therefore: white cell = " + grid[index]) ;
            }
        }
         return grid ;
    }

    /**
     * Creates a new, smaller image from the supplied image, based on the input corner coordinates.
     * Reduces the resolution and makes the grid square, removing any keystoning / rotation
     * @param rawImage The original image
     * @param tl The top left coordinate in the original image for the subsampled image
     * @param tr The top right coordinate in the original image for the subsampled image
     * @param bl The bottom left coordinate in the original image for the subsampled image
     * @param br The bottom right coordinate in the original image for the subsampled image
     * @return The subsampled image
     */
    public Bitmap createSubsampledImage(Bitmap rawImage, int[] tl, int[] tr, int[] bl, int[] br) {
        int outputRes = 15*20;    // No of pixels per side for output grid square
        int width = rawImage.getWidth();
        int height = rawImage.getHeight();
        int length = width*height;
        int[] source = new int[length];
        rawImage.getPixels(source,0,width,0,0,width,height);

        // Turn into array with cartesian coords (to make it easier to understand what's going on
        int[][] array = new int[width][height] ;
        for (int i = 0 ; i < source.length ; i++) {
            int x = i % width ;
            int y = i / width ;
            array[x][y] = source[i] ;
        }

        // Sub sample
        int[][] output = new int[outputRes][outputRes] ;
        // X --> U / Horizontals
        // X/Y vector components for top horizontal (th) line
        int thDiffX = tr[X] - tl[X] ;
        int thDiffY = tr[Y] - tl[Y] ;
        int[] h0 = {thDiffX, thDiffY} ;
        // X/Y vector components for bottom horizontal (bh) line
        int bhDiffX = br[X] - bl[X] ;
        int bhDiffY = br[Y] - bl[Y] ;
        int[] hN = {bhDiffX, bhDiffY} ;
        // X/Y vector components for first vertical (v0) line
        int v0DiffX = bl[X] - tl[X] ;
        int v0DiffY = bl[Y] - tl[Y] ;
        int[] v0 = {v0DiffX, v0DiffY} ;

        // Loop through each pixel in subsampled image
        for (int u = 0 ; u < outputRes ; u++) {
            for (int v = 0 ; v < outputRes ; v++) {
                int[] xy = uvTransformFromXY(u,v,outputRes,tl, h0, hN, v0) ;
//                Log.d("AGG", "u = " + u + ", v = " + v + ", length(xy)  " + xy.length) ;
                // Get the value of the point at the returned xy values, and store it in uv space
                output[u][v] = array[xy[X]][xy[Y]] ;
            }
        }
        // Calculate no of pixels in output image
        int outputRes2 = outputRes*outputRes ;
        int[] outputPixels = new int[outputRes2] ;
        // Turn back into single dimension array
        for (int i = 0 ; i < outputRes2 ; i++) {
            int x = i % outputRes ;
            int y = i / outputRes ;
            outputPixels[i] = output[x][y] ;
        }

        Bitmap returnImage = Bitmap.createBitmap(outputPixels,0,outputRes,outputRes,outputRes, Bitmap.Config.ARGB_8888);
//        rawImage.setPixels(outputPixels,0,outputRes,0,0,outputRes,outputRes);
        rawImage.recycle();
        return returnImage ;
    }

    /**
     * Gets the x,y coords for the corresponding u,v values, RELATIVE TO (U=0,V=0)
     * Descends the v0 line until 'v1' is reached, then traverses the 'horizontal' vector corresponding
     * to 'v' to get the coords at point u,v.
     * @param u Horizontal coordinate in transformed system
     * @param v Vertical coordinate in transformed system
     * @param res Resolution of UV space (i.e. max u or v value)
     * @param h0 Horizontal vector '0', i.e. top
     * @param hN Horizontal vector final, i.e. bottom
     * @param v0 Vertical vector '0'
     * @return x,y coords of u,v in original cartesian space
     */
    private int[] uvTransformFromXY(int u, int v, int res, int[] origin, int[] h0, int[] hN, int[] v0) {

        // Go down v0 until we get to v
        int[] v1 = {v0[X]*v/res, v0[Y]*v/res} ;
        // Calculate hn - the horizontal vector along v
        int[] hn = new int[2] ;
        hn[X] = ((res - v)*h0[X] + v*hN[X]) / res ;
        hn[Y] = ((res - v)*h0[Y] + v*hN[Y]) / res ;
        // Go along hn until we get to u
        int[] dHn = {hn[X]*u/res, hn[Y]*u/res} ;

        // Add dHn to v1
        int[] xy = {origin[X] + v1[X] + dHn[X], origin[Y] + v1[Y] + dHn[Y]} ;

        // Return the result
        return xy ;
    }

    /**
     * Resizes image to new width, preserving aspect ratio
     * @param rawImage The image to be scaled
     * @param width The new width of the image
     * @return The scaled image
     */
    public Bitmap resize(Bitmap rawImage, int width) {
        int originalHeight = rawImage.getHeight();
        int originalWidth = rawImage.getWidth();
        double scaleRatio = (double) width / (double) originalWidth ;
        return Bitmap.createScaledBitmap(rawImage, (int) (originalWidth * scaleRatio), (int) (originalHeight * scaleRatio),false);
    }

    /**
     * Converts image to grayscale
     * @param rawImage The original colour image
     * @return The grayscale representation
     */
    public Bitmap convertToBlackAndWhite(Bitmap rawImage) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);

        Bitmap blackAndWhiteBitmap = rawImage.copy(Bitmap.Config.ARGB_8888, true);
        rawImage.recycle(); // Free up the memory

        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);

        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);

        return blackAndWhiteBitmap ;
    }

    public Bitmap invertColours(Bitmap rawImage) {
        int length = rawImage.getWidth()*rawImage.getHeight();
        int[] array = new int[length];
        rawImage.getPixels(array,0,rawImage.getWidth(),0,0,rawImage.getWidth(),rawImage.getHeight());
        for (int i=0;i<length;i++){
            // If the rawImage is in ARGB_8888 format, invert its values
            array[i] = 0xffffffff - array[i];
        }

        rawImage.setPixels(array,0,rawImage.getWidth(),0,0,rawImage.getWidth(),rawImage.getHeight());
        return rawImage ;
    }

    /**
     * @param rawImage The image to have blurring applied
     * @return The blurred image
     */
    public Bitmap addBlur(Bitmap rawImage) {
        int blurRad = 3;
        return addBlur(rawImage, blurRad) ;
    }

    public Bitmap addBlur(Bitmap rawImage, int blurRad){

        int width = rawImage.getWidth();
        int height = rawImage.getHeight();
        int length = width * height;
        int[] source = new int[length];
        int[] thresholded = new int[length];
        rawImage.getPixels(source, 0, width, 0, 0, width, height);
        int[][] original = new int[width][height] ;
        int[][] totals =  new int[width][height] ;
        // Turn single dimensional pixel array into easier to understand 2D array
        for (int i = 0 ; i < source.length ; i++) {
            int x = i % width ;
            int y = i / width ;
            original[x][y] = source[i] ;
        }
        // Prep the first set of cells
        // Get the total for the first set
        int total = 0;
        for (int x = 0 ; x < blurRad ; x++) {
            for (int y = 0 ; y < blurRad ; y++) {
                total += Color.red(original[x][y]) ;
            }
        }
        // Save the total into the first set (up to blur rad / 2)
        for (int x = 0 ; x < (blurRad +1)/2 ; x++) {
            for (int y = 0 ; y < (blurRad+1)/2 ; y++) {
                totals[x][y] = total ;
            }
        }

        // Loop through and add new cells to total, removing the old ones
        for (int x = blurRad / 2 ; x < width - (blurRad / 2); x++) {
            if (x % 100 == 0) Log.d("AGG-Blur", "x = " + x);
            for (int y = blurRad / 2; y < height - (blurRad / 2); y++) {

            }
        }


        return rawImage ;
    }

    /**
     * @param rawImage The image to which to add the thresholding
     * @return The thresholded image
     */
    public Bitmap addThreshold(Bitmap rawImage) {
        int white = 0xffffffff;
        int black = 0xff000000;
        int thresholdRad = 5 ;
        int blurRad = 3 ;

        int width = rawImage.getWidth();
        int height = rawImage.getHeight();
        int length = width * height;
        int[] source = new int[length];
        int[] thresholded = new int[length];
        rawImage.getPixels(source, 0, width, 0, 0, width, height);
        int[][] original = new int[width][height] ;
        int[][] output =  new int[width][height] ;
        // Turn single dimensional pixel array into easier to understand 2D array
        for (int i = 0 ; i < source.length ; i++) {
            int x = i % width ;
            int y = i / width ;
            original[x][y] = source[i] ;
        }
        // Loop through and do the thresholding
        for (int x = 0 ; x < width ; x++) {
            if (x%100 == 0) Log.d("AGG-Threshold", "x = " + x);
            for (int y = 0 ; y < height ; y++) {
                int xStart = Math.max(0,x-(thresholdRad/2)) ;
                int yStart = Math.max(0,y-(thresholdRad/2)) ;
                int total = 0 ;
                int count = 0 ;
                int redXY = Color.red(original[x][y]) ;
                int blurTotal = 0;
                int blurCount = 0 ;
                for (int a = xStart ; a < Math.min(width-1,xStart+thresholdRad) ; a++) {
                    for (int b = yStart ; b < Math.min(height-1, yStart+thresholdRad) ; b++) {
                        total += Color.red(original[a][b]) ;
                        count++ ;
                        if (Math.abs(a-x) <= blurRad && Math.abs(b-y) <= blurRad) {
                            blurTotal += Color.red(original[a][b]) ;
                            blurCount++ ;
                        }
                    }
                }
                double mean = (double) total / (double) count ;
                double blurredPixel = (double) blurTotal / (double) blurCount ;
                if (x%100 == 0) Log.d("AGG-Threshold", "blurredPix = " + blurredPixel + ", blurTotal = " + blurTotal + ", blurCount = " + blurCount);
                if (blurredPixel < mean) {
                    output[x][y] = black ;
                } else {
                    output[x][y] = white ;
                }
            }
        }
        Log.d("AGG-Threshold", "Turning back to 1D array");
        // Turn back into 1D array
        for (int i = 0 ; i < source.length ; i++) {
            int x = i % width ;
            int y = i / width ;
            thresholded[i] = output[x][y];
        }
        // Turn back into a bitmap
        Bitmap returnImage = Bitmap.createBitmap(thresholded,0,width,width,height, Bitmap.Config.ARGB_8888);
        rawImage.recycle();
        return returnImage ;
    }
        /**
         * @param rawImage The image to which to add the thresholding
         * @return The thresholded image
         */
    public Bitmap addThreshold(Bitmap rawImage, boolean oldMethod) {
        int white = 0xffffffff ;
        int black = 0xff000000 ;

        int width = rawImage.getWidth();
        int height = rawImage.getHeight();
        int length = width*height;
        int[] source = new int[length];
        rawImage.getPixels(source,0,width,0,0,width,height);
        int[] output = new int[source.length] ;
        for (int i = 0 ; i < source.length ; i++) {
            output[i] = source[i] ;
            if (i%1000000 == 0) {
                Log.d("AGG", "Color.red(source[" + i + "]) = " + Color.red(source[i]));
            }
        }
        int aveSpan = 20 ;

        // ignore edges for now
        for (int i=aveSpan;i<length-aveSpan;i++){
            //int localMean = source[i-aveSpan]/(2*aveSpan/5) ;
            double localMean = 0.0;
            // loop through other values and add them to the total
            // Only currently averaging 'linearly' (with weird new line wrapping). needs to be 2D
            for (int j = -aveSpan ; j<=aveSpan ; j+=aveSpan/2) {
                //localMean += source[i+j]/(2*aveSpan/5) ;
//                localMean += source[i+j];
                int pix = source[i+j];
                localMean += Color.red(pix); //+Color.blue(pix)*Color.blue(pix)+Color.green(pix)*Color.green(pix));

                if (i%1000000 == 0) {
                    Log.d("AGG", "localMean (j =  "+ j + ") = " + localMean);
                }
            }
            localMean /= 2*2 + 1;
            // Check local mean not just white or black, in which case reset it to middle (probably indicative that larger radius required
            if ((int) localMean < 254 || (int) localMean > 1) {
                // Set it in the middle
                localMean = 255/2 ;
            }
            // use local mean as threshold
            if (Color.red(source[i]) < (int) localMean) {
                // assume black
                output[i] = black ;
            } else {
                if (i%1000000 == 0) {
                    Log.d("AGG", "Color.red(source[" + i + "]) = " + Color.red(source[i]) + " & localMean = " + localMean);
                }
                output[i] = white ;
            }
        }

        rawImage.setPixels(output,0,width,0,0,width,height);
        return rawImage ;
    }
}
