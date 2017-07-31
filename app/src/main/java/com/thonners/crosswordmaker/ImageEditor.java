package com.thonners.crosswordmaker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Class to handle all image editing for CrosswordToolkit
 *
 * @author M Thomas
 * @since 30/07/17
 */

public class ImageEditor {

    public ImageEditor() {
    }

    public void rotateImage(File imageFile) {
        Bitmap bm = BitmapFactory.decodeFile(imageFile.getAbsolutePath()) ;
        Matrix matrix = new Matrix() ;
        matrix.postRotate(90) ;
        Bitmap rotatedBM = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),matrix, true) ;
        // Try to save it
        try {
            FileOutputStream out = new FileOutputStream(imageFile) ;
            rotatedBM.compress(Bitmap.CompressFormat.JPEG, 90, out) ;
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
