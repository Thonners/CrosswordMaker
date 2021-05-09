package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collection;

public class GridMakerImageView extends android.support.v7.widget.AppCompatImageView implements View.OnTouchListener {

    private static String LOG_TAG = "GridMakerImageView";

//    private int gridTLX=0 , gridTLY=0 ;     // X/Y coords of top left of grid
//    private int gridBRX=0 , gridBRY=0 ;     // X/Y coords of bottom right of grid

    private Corner activeCorner = null;
    private float mLineThickness = 3.0f ;
    private float mCornerCircleRadius = 50.0f ;
    private Paint mLinePaint = new Paint() ;
    private Paint mCornerPaint = new Paint() ;
    private float[][] gridCorners = new float[4][2] ; // % of image width value - to deal with image vs view pixel densities
    private ArrayList<Corner> corners = new ArrayList<>();
    private int gridCornerTouched = 0 ; // Counter to keep track of the number of times the grid was touched.
    private float defaultCornerPosition = 0.2f ; // % of image assumed as border around initial grid corners

    private Bitmap gridImage ;
    private Rect mMeasuredRect = new Rect();

    public GridMakerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs, defStyle);
        init();
    }
    public GridMakerImageView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
    }
    public GridMakerImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Log.d(LOG_TAG,"init called");
        // Set touch listener
        this.setOnTouchListener(this);
        // -- Create the paints--
        // Lines
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(mLineThickness);
        // Corner circles
        mCornerPaint.setStyle(Paint.Style.FILL);
        mCornerPaint.setColor(Color.RED);
    }

    /**
     * @return 2D array, first index is corner index from TL,TR,BR,BL and second is X/Y (0/1)
     */
    public float[][] getGridCorners() {
        float[][] gridCorners = new float[4][2] ;
        for(int iCorner = 0; iCorner < 4 ; iCorner++) {
            gridCorners[iCorner][0] = corners.get(iCorner).getX();
            gridCorners[iCorner][1] = corners.get(iCorner).getY();
        }
        return gridCorners;
    }

    public String getGridCornersString() {
        float[][] gridCorners = getGridCorners();
        return "(" + gridCorners[0][0] + "," + gridCorners[0][1] + "),(" + gridCorners[1][0] + "," + gridCorners[1][1] +
                "),(" + gridCorners[2][0] + "," + gridCorners[2][1] + "),(" + gridCorners[3][0] + "," + gridCorners[3][1] + ")" ;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        Log.d(LOG_TAG,"setImageBitmap called");
        // Take a mutable copy of the image
        gridImage = bitmap.copy(Bitmap.Config.ARGB_8888, true) ;
        // Set the default corner locations
        corners.add(new Corner(defaultCornerPosition, defaultCornerPosition)) ; // TL
        corners.add(new Corner(1 - defaultCornerPosition, defaultCornerPosition)) ; // TR
        corners.add(new Corner(1 - defaultCornerPosition, 1 - defaultCornerPosition)) ; //BR
        corners.add(new Corner(defaultCornerPosition, 1 - defaultCornerPosition)) ; // BL
        // Draw the corners & update the view (invalidate calls onDraw())
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(LOG_TAG,"onDraw called");
        // Draw the background image
        canvas.drawBitmap(gridImage,null, mMeasuredRect, null);
        // Draw the corners
        for (int i = 0 ; i < 4 ; i++) {
            // Index for 'next' corner
            int j = (i + 1) % 4 ; // Limit to 4, so 4th corner uses 1st corner as the 'next'
            float x = corners.get(i).getX()*mMeasuredRect.width() ;
            float y = corners.get(i).getY()*mMeasuredRect.height() ;
            float nextX = corners.get(j).getX()*mMeasuredRect.width() ;
            float nextY = corners.get(j).getY()*mMeasuredRect.height() ;
            // Draw lines between the circles first so the circles sit on top
            canvas.drawLine(x,y,nextX, nextY,mLinePaint);
            // Draw the corner circles
            canvas.drawCircle(x,y,mCornerCircleRadius,mCornerPaint);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(LOG_TAG,"onMeasure called");
        mMeasuredRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
//        Log.d(LOG_TAG,"Something touched...");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeCorner = Corner.getActiveCorner(corners, mMeasuredRect, event.getX(), event.getY(), mCornerCircleRadius) ;
                if (activeCorner != null) {
//                    Log.d(LOG_TAG,"Not null, active X = " + activeCorner.getX() + ", active Y = " + activeCorner.getY());
                } else {
                    Log.d(LOG_TAG,"Null - no corner touched");
                }
                return true ;
            case MotionEvent.ACTION_MOVE:
                if (activeCorner != null) {
                    activeCorner.setX(event.getX() / mMeasuredRect.width());
                    activeCorner.setY(event.getY() / mMeasuredRect.height());
                    invalidate();
                }
                return true ;
            case MotionEvent.ACTION_UP:
                if (activeCorner != null) {
                    activeCorner.setX(event.getX() / mMeasuredRect.width());
                    activeCorner.setY(event.getY() / mMeasuredRect.height());
                    invalidate();
                }
                activeCorner = null ;
                return true ;
        }
        return false;
    }

    private static class Corner {

        private float x, y ;

        public Corner(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public static Corner getActiveCorner(Collection<Corner> corners, Rect imageViewRect, float touchX, float touchY, float tolerance) {
            for (Corner corner : corners) {
                if ((Math.abs(touchX - corner.getX()*imageViewRect.width()) <= tolerance) && (Math.abs(touchY - corner.getY()*imageViewRect.height()) <= tolerance)) {
                    return corner;
                }
            }
            Log.d(LOG_TAG, "no active corner found: touchX = " + touchX + ", touchY = " + touchY + ", width = " + imageViewRect.width() + ", height = " + imageViewRect.height() +
                    " product = " + touchX*imageViewRect.width() + ", " + Math.abs(touchY*imageViewRect.height()));
            return null ;
        }
    }
}
