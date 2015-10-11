package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.HorizontalScrollView;

/**
 *  Horizontal Scroll View, but with override to ignore child focus changes,
 *  which cause unwanted scrolling behaviour.
 *
 * Created by Thonners on 11/10/15.
 */
public class HorizontalScrollViewNoFocus extends HorizontalScrollView {

    private static String LOG_TAG = "HorizontalScrollViewNoFocus";

    public HorizontalScrollViewNoFocus(Context context){
        super(context);
    }
    public HorizontalScrollViewNoFocus(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    public HorizontalScrollViewNoFocus(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs,defStyle);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect){
        Log.d(LOG_TAG,"direction = " + direction);
        Log.d(LOG_TAG,"previouslyFocusedRect = " + previouslyFocusedRect.toString());
        return true ;
    }

}
