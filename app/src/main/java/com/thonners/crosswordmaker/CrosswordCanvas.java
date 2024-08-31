package com.thonners.crosswordmaker;

import static androidx.core.content.ContextCompat.getSystemService;

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
import android.view.inputmethod.InputMethodManager;


public class CrosswordCanvas extends View implements View.OnTouchListener {

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

    private static final String LOG_TAG = "CrosswordCanvas";

    private boolean hasBeenInitialised = false;
    private Bitmap gridBitmap, blackCellBitmap, clueNumberBitmap;
    private Canvas backgroundGrid, blackCellMask, clueNumberCanvas;
    private Rect bounds;
    private Paint blackPaint, clueHighlightPaint, cellHighlightPaint, whitePaint;
    private int width, height, cellWidth, outerPadding, fontSize;
    private final int cellSizeOverClueNumberSize = 4, clueNumberPadding = 5;
    private final int cursorMarginBottom = 15, cursorMarginSide = 15;
    private final int cursorThickness = 5;
    private CrosswordTwo crossword;

    private int[] rowColIndex;
    private Row[] rows;
    private Col[] cols;

    public CrosswordCanvas(Context context) {
        super(context);
        setFocusableInTouchMode(true); // allows the keyboard to pop up on touch down
    }

    public CrosswordCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusableInTouchMode(true); // allows the keyboard to pop up on touch down
    }

    public CrosswordCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusableInTouchMode(true); // allows the keyboard to pop up on touch down
    }


    public void initialise(int width, int height, CrosswordTwo crossword) {
        this.setOnTouchListener(this);
        hasBeenInitialised = true;
        this.width = width;
        this.height = height;
        this.cellWidth = width / crossword.rowCount;
        this.outerPadding = (this.width - (this.cellWidth * crossword.rowCount)) / 2;
        this.fontSize = cellWidth / cellSizeOverClueNumberSize;

        this.crossword = crossword;
        Log.d(LOG_TAG, "Got crossword: " + crossword);
        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black, null));
        blackPaint.setStrokeWidth(2);
        blackPaint.setTextSize(fontSize);
        whitePaint = new Paint();
        whitePaint.setColor(getResources().getColor(R.color.white, null));
        whitePaint.setStyle(Paint.Style.FILL);
        clueHighlightPaint = new Paint();
        clueHighlightPaint.setColor(getResources().getColor(R.color.clue_highlighted, null));
        cellHighlightPaint = new Paint();
        cellHighlightPaint.setColor(getResources().getColor(R.color.cell_highlighted, null));

        gridBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        blackCellBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        clueNumberBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        backgroundGrid = new Canvas(gridBitmap);
        blackCellMask = new Canvas(blackCellBitmap);
        clueNumberCanvas = new Canvas(clueNumberBitmap);
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
        drawBlackCellMask();
        drawClueNumbers();
        prepopulatePixelIndices();
    }

    private void drawBackgroundGrid() {
        // Draw the border
        if (cols[0].getXMin() > 0) {
            backgroundGrid.drawRect(bounds, blackPaint);
            backgroundGrid.drawRect(cols[0].getXMin(), rows[0].getYMin(),
                    cols[crossword.rowCount - 1].getXMax(),
                    rows[crossword.rowCount - 1].getYMax(), whitePaint);
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

    private void drawBlackCellMask() {
        blackCellBitmap.eraseColor(Color.TRANSPARENT);
        for (int r = 0; r < crossword.rowCount; r++) {
            for (int c = 0; c < crossword.rowCount; c++) {
                if (crossword.getCell(r, c).getIsBlackCell()) {
                    blackCellMask.drawRect(cols[c].getXMin(), rows[r].getYMin(),
                            cols[c].getXMax(), rows[r].getYMax(), blackPaint);
                }
            }
        }
    }

    private void drawClueNumber(ClueTwo clue) {
        Log.d(LOG_TAG, "Drawing clue number: " + clue.getClueNumber());
        CellTwo firstCell = clue.getFirstCell();
        int xPosition = cols[firstCell.getCol()].getXMin() + clueNumberPadding;
        int yPosition = rows[firstCell.getRow()].getYMin() + fontSize; // Doesn't need vert padding
        clueNumberCanvas.drawText("" + clue.getClueNumber(), xPosition, yPosition, blackPaint);
    }

    private void drawClueNumbers() {
        if (crossword.editGridMode) {
            // No clue numbers during edit grid mode
            // (yet - could find clues live to help know when the grid is done correctly)
            return;
        }
        Log.d(LOG_TAG, "Drawing clue numbers. Crossword has: " + crossword.hClues.length +
                "hClues");

        for (ClueTwo clue : crossword.hClues) {
            drawClueNumber(clue);
        }

        for (ClueTwo clue : crossword.vClues) {
            drawClueNumber(clue);
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
        // Background grid
        canvas.drawBitmap(gridBitmap, null, bounds, null);
        // Draw the black cells
        canvas.drawBitmap(blackCellBitmap, null, bounds, null);
        // Draw the highlights
        ClueTwo highlightedClue = crossword.getHighlightedClue();
        if (highlightedClue != null) {
            for (CellTwo cell : crossword.getHighlightedClue().getCells()) {
                if (crossword.getHighlightedCell() != null && cell == crossword.getHighlightedCell()) {
                    canvas.drawRect(getCellRect(cell), cellHighlightPaint);
                    canvas.drawRect(getCellCursorRect(cell), blackPaint);
                } else {
                    canvas.drawRect(getCellRect(cell), clueHighlightPaint);
                }
            }
        }
        // Draw the clue numbers
        canvas.drawBitmap(clueNumberBitmap, null, bounds, null);

        Log.d(LOG_TAG, "Canvas.onDraw called");
    }

    private Rect getCellRect(CellTwo cell) {
        int row = cell.getRow();
        int col = cell.getCol();
        return new Rect(cols[col].getXMin(), rows[row].getYMin(), cols[col].getXMax(),
                rows[row].getYMax());
    }

    private Rect getCellCursorRect(CellTwo cell) {
        int row = cell.getRow();
        int col = cell.getCol();
        return new Rect(cols[col].getXMin() + cursorMarginSide,
                rows[row].getYMax() - cursorMarginBottom - cursorThickness,
                cols[col].getXMax() - cursorMarginSide,
                rows[row].getYMax() - cursorMarginBottom);
    }

    private void showKeyboard() {

        if (this.requestFocus()) {
            InputMethodManager imm = getSystemService(getContext(),
                    InputMethodManager.class);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    private void hideKeyboard() {
        InputMethodManager imm = getSystemService(getContext(),
                InputMethodManager.class);
        imm.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
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
                crossword.cellTouched(row, col);
                // Only if editing the grid do we need to redraw the black cell mask
                if (crossword.editGridMode) {
                    drawBlackCellMask();
                }
                // Redraw the grid!
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                int rowUp = rowColIndex[Math.max(0, Math.min((int) event.getY(), this.height - 1))];
                int colUp = rowColIndex[Math.max(0, Math.min((int) event.getX(), this.width - 1))];
                if (!crossword.editGridMode) {
                    if (crossword.getCell(rowUp, colUp).getIsBlackCell()) {
                        // Hide the keyboard if we've touched a black cell
                        hideKeyboard();
                    } else {
                        // Otherwise, show the keyboard so we can enter text
                        showKeyboard();
                    }
                }


        }
        return false;
    }
}
