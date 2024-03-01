package com.thonners.crosswordmaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CrosswordCanvas extends View implements View.OnTouchListener {

    class CellCanvas {
        int row, col, cellWidth;

        public CellCanvas(int row, int col, int cellWidth) {
            this.row = row;
            this.col = col;
            this.cellWidth = cellWidth;
        }

        public int getXMin() {
            /*
            0123455678
            |_||_||_|
            */
            return col * cellWidth;
        }

        public int getXMax() {
            return (col + 1) * cellWidth - 1;
        }

        public int getYMin() {
            return row * cellWidth;
        }

        public int getYMax() {
            return (row + 1) * cellWidth - 1;
        }
    }

    class Row {
        int row, paddingOffset;

        public Row(int row, int paddingOffset) {
            this.row = row;
            this.paddingOffset = paddingOffset;
        }

        public int getYMin() {
            return row * cellWidth + paddingOffset;
        }

        public int getYMax() {
            return (row + 1) * cellWidth - 1 + paddingOffset;
        }
    }

    class Col {
        int col, paddingOffset;

        public Col(int col, int paddingOffset) {
            this.col = col;
            this.paddingOffset = paddingOffset;
        }

        public int getXMin() {
            return col * cellWidth + paddingOffset;
        }

        public int getXMax() {
            return (col + 1) * cellWidth - 1 + paddingOffset;
        }
    }

    private static String LOG_TAG = "CrosswordCanvas";

    boolean hasBeenInitialised = false;
    Bitmap frame, gridBitmap;
    Canvas frameDrawer, backgroundGrid;
    Rect bounds;
    Paint blackPaint, clueHighlightPaint, cellHighlightPaint, whitePaint;
    int width, height, cellWidth, outerPadding;
    private CrosswordTwo crossword;
    CellCanvas[] cells;

    int[] rowColIndex;
    Row[] rows;
    Col[] cols;

    public CrosswordCanvas(Context context) {
        super(context);
    }

    public CrosswordCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CrosswordCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void initialise(int width, int height, CrosswordTwo crossword) {
        this.setOnTouchListener(this);
        hasBeenInitialised = true;
        this.width = width;
        this.height = height;
        this.cellWidth = width / crossword.rowCount;
        this.outerPadding = (this.width - (this.cellWidth * crossword.rowCount)) / 2;

        this.crossword = crossword;
        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black, null));
        blackPaint.setStrokeWidth(2);
        whitePaint = new Paint();
        whitePaint.setColor(getResources().getColor(R.color.white, null));
        whitePaint.setStyle(Paint.Style.FILL);
        clueHighlightPaint = new Paint();
        clueHighlightPaint.setColor(getResources().getColor(R.color.clue_highlighted, null));
        cellHighlightPaint = new Paint();
        cellHighlightPaint.setColor(getResources().getColor(R.color.cell_highlighted, null));

        frame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        gridBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        frameDrawer = new Canvas(frame);
        backgroundGrid = new Canvas(gridBitmap);
        bounds = new Rect(0, 0, width, height);
        Log.d(LOG_TAG, "Canvas initialised to (w,h) = (" + width + ", " + height + ")");

        rows = new Row[crossword.rowCount];
        cols = new Col[crossword.colCount];

        for (int i = 0; i < crossword.rowCount; i++) {
            rows[i] = new Row(i, outerPadding);
        }
        for (int i = 0; i < crossword.colCount; i++) {
            cols[i] = new Col(i, outerPadding);
        }
        drawBackgroundGrid();
        prepopulatePixelIndices();
//        cells = new CellCanvas[crossword.rowCount * crossword.rowCount];
//        for (int r = 0; r < crossword.rowCount; r++) {
//            for (int c = 0; c < crossword.rowCount; c++) {
//                int iCell = r * crossword.rowCount + c;
//                cells[iCell] = new CellCanvas(r, c, cellWidth);
//            }
//        }

    }

    private void drawBackgroundGrid() {
        // Draw the border
        if (cols[0].getXMin() > 0) {
            backgroundGrid.drawRect(bounds, blackPaint);
            backgroundGrid.drawRect(cols[0].getXMin(), rows[0].getYMin(), cols[crossword.rowCount - 1].getXMax(), rows[crossword.rowCount - 1].getYMax(), whitePaint);
        }
        for (int i = 0; i < crossword.rowCount; i++) {
            // Draw both sides of the rows
            backgroundGrid.drawLine(0, rows[i].getYMin(), width, rows[i].getYMin(), blackPaint);
            backgroundGrid.drawLine(0, rows[i].getYMax(), width, rows[i].getYMax(), blackPaint);
            // Draw both sides of the cols
            backgroundGrid.drawLine(cols[i].getXMin(), 0, cols[i].getXMin(), width, blackPaint);
            backgroundGrid.drawLine(cols[i].getXMax(), 0, cols[i].getXMax(), width, blackPaint);
        }

    }

    /**
     * Calculates to which row/column a pixel index belongs, to facilitate immediate lookup of the
     * row/column on touch later
     */
    private void prepopulatePixelIndices() {
        rowColIndex = new int[width];
        for (int p = 0; p < width; p++) {
            if (p < rows[0].getYMin()) {
                rowColIndex[p] = -1;
            } else if (p > rows[crossword.rowCount - 1].getYMax()) {
                rowColIndex[p] = -1;
            } else {
                for (int i = 0; i < crossword.rowCount; i++) {
                    if (p >= rows[i].getYMin() && p <= rows[i].getYMax()) {
                        rowColIndex[p] = i;
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the bitmap on the view canvas
        canvas.drawBitmap(gridBitmap, null, bounds, null);
        // Draw the black cells
        // TODO: Move to a black cell mask that can be calcualted once for a pre-defined grid
        for (int r = 0; r < crossword.rowCount; r++) {
            for (int c = 0; c < crossword.rowCount; c++) {
                if (crossword.getCell(r, c).getIsBlackCell()) {
                    canvas.drawRect(cols[c].getXMin(), rows[r].getYMin(), cols[c].getXMax(),
                            rows[r].getYMax(), blackPaint);
                }
            }
        }
        Log.d(LOG_TAG, "Canvas.onDraw called");
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(LOG_TAG, "onTouch triggered. Motion event: " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int row = rowColIndex[Math.max(0, Math.min((int) event.getY(), this.height - 1))];
                int col = rowColIndex[Math.max(0, Math.min((int) event.getX(), this.width - 1))];
                Log.d(LOG_TAG, "onTouch ActionDown triggered. Motion event: (" + event.getX() +
                        "," + event.getY() + "). This corresponds to row " + row + ", col: " + col);
                if (0 <= row && row < crossword.rowCount && 0 <= col && col < crossword.colCount) {
                    crossword.toggleBlackCell(row, col);
                    invalidate();
                    return true;
                } else {
                    Log.d(LOG_TAG, "Touch detected outside the active grid, so ignoring it.");
                }
        }
        return false;
    }
}
