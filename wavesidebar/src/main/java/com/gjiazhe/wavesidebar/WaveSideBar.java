package com.gjiazhe.wavesidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by gjz on 8/23/16.
 */
public class WaveSideBar extends View {
    private final int DEFAULT_TEXT_SIZE = 14; // sp
    private final int DEFAULT_MAX_OFFSET = 100; //dp

    private final String[] DEFAULT_INDEX_ITEMS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private String[] mIndexItems;

    /**
     * the index in {@link #mIndexItems} of the current selected index item,
     * it's reset to -1 when the finger up
     */
    private int mCurrentIndex = -1;

    /**
     * Y coordinate of the point where finger is touching,
     * the baseline is top of {@link #mBarArea}
     * it's reset to -1 when the finger up
     */
    private float mCurrentY = -1;

    private Paint mPaint;
    private int mTextColor;
    private float mTextSize;

    /**
     * the height of each index item
     */
    private float mIndexItemHeight;

    /**
     * offset of the current selected index item
     */
    private float mMaxOffset;

    /**
     * area where the bar is draw
     */
    private RectF mBarArea = new RectF();

    /**
     * height and width of {@link #mBarArea}
     */
    private float mBarHeight;
    private float mBarWidth;

    /**
     * flag that the finger is starting touching
     */
    private boolean mStartTouching = false;

    /**
     * if true, the {@link OnSelectIndexItemListener#onSelectIndexItem(String)}
     * will not be called until the finger up.
     * if false, it will be called when the finger down, up and move.
     */
    private boolean mLazyRespond = false;

    /**
     * observe the current selected index item
     */
    private OnSelectIndexItemListener onSelectIndexItemListener;

    /**
     * the baseline of the first index item text to draw
     */
    private float mFirstItemBaseLineY;

    /**
     * for {@link #dp2px(int)} and {@link #sp2px(int)}
     */
    private DisplayMetrics mDisplayMetrics;


    public WaveSideBar(Context context) {
        this(context, null);
    }

    public WaveSideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.WaveSideBar);
        mLazyRespond = typedArray.getBoolean(R.styleable.WaveSideBar_lazy_respond, false);
        mTextColor = typedArray.getColor(R.styleable.WaveSideBar_text_color, Color.GRAY);
        typedArray.recycle();

        mDisplayMetrics = getContext().getResources().getDisplayMetrics();

        mTextSize = sp2px(DEFAULT_TEXT_SIZE);
        mMaxOffset = dp2px(DEFAULT_MAX_OFFSET);

        mIndexItems = DEFAULT_INDEX_ITEMS;

        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw each item
        for (int i = 0, mIndexItemsLength = mIndexItems.length; i < mIndexItemsLength; i++) {
            float baseLineY = mFirstItemBaseLineY + mIndexItemHeight*i;

            // calculate the scale factor of the item to draw
            float scale = 0;
            if (mCurrentIndex != -1) {
                scale = 1 - Math.abs(mCurrentY - (mIndexItemHeight*i+mIndexItemHeight/2)) / mIndexItemHeight / 4;
                scale = Math.max(scale, 0);
                Log.i("scale", mIndexItems[i] + ": " + scale);
            }

            int alphaScale = (i == mCurrentIndex) ? (255) : (int) (255 * (1-scale));
            mPaint.setAlpha(alphaScale);

            mPaint.setTextSize(mTextSize + mTextSize*scale);

            // draw
            canvas.drawText(
                    mIndexItems[i], //item text to draw
                    getWidth() - mBarWidth /2 - mMaxOffset*scale, //center text X
                    baseLineY, // baseLineY
                    mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mIndexItemHeight = fontMetrics.bottom - fontMetrics.top;

        for (String indexItem : mIndexItems) {
            mBarWidth = Math.max(mBarWidth, mPaint.measureText(indexItem));
        }
        mBarHeight = mIndexItems.length * mIndexItemHeight;

        float startDrawX = width - mBarWidth;
        float startDrawY = height/2 - mBarHeight /2;

        mBarArea.set(startDrawX, startDrawY, startDrawX + mBarWidth, startDrawY + mBarHeight);

        mFirstItemBaseLineY = (height/2 - mIndexItems.length*mIndexItemHeight/2)
                + (mIndexItemHeight/2 - (fontMetrics.descent-fontMetrics.ascent)/2)
                - fontMetrics.ascent;
    }

    public void setIndexItems(String[] indexItems) {
        mIndexItems = indexItems;
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIndexItems.length == 0) {
            return super.onTouchEvent(event);
        }

        float eventY = event.getY();
        float eventX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mBarArea.contains(eventX, eventY)) {
                    mStartTouching = true;
                    if (!mLazyRespond && onSelectIndexItemListener != null) {
                        mCurrentIndex = getSelectedIndex(eventY);
                        onSelectIndexItemListener.onSelectIndexItem(mIndexItems[mCurrentIndex]);
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mStartTouching && !mLazyRespond && onSelectIndexItemListener != null) {
                    mCurrentIndex = getSelectedIndex(eventY);
                    onSelectIndexItemListener.onSelectIndexItem(mIndexItems[mCurrentIndex]);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mLazyRespond && onSelectIndexItemListener != null) {
                    mCurrentIndex = getSelectedIndex(eventY);
                    onSelectIndexItemListener.onSelectIndexItem(mIndexItems[mCurrentIndex]);
                }
                mCurrentIndex = -1;
                mStartTouching = false;
                invalidate();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private int getSelectedIndex(float eventY) {
        mCurrentY = eventY - (getHeight()/2 - mBarHeight /2);
        if (mCurrentY <= 0) {
            return 0;
        }

        int index = (int) (mCurrentY / this.mIndexItemHeight);
        if (index >= this.mIndexItems.length) {
            index = this.mIndexItems.length - 1;
        }
        return index;
    }

    private float dp2px(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.mDisplayMetrics);
    }

    private float sp2px(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this.mDisplayMetrics);
    }

    public void setTextColor(int color) {
        mTextColor = color;
        mPaint.setColor(color);
        invalidate();
    }

    public void setOnSelectIndexItemListener(OnSelectIndexItemListener onSelectIndexItemListener) {
        this.onSelectIndexItemListener = onSelectIndexItemListener;
    }

    public interface OnSelectIndexItemListener {
        void onSelectIndexItem(String item);
    }
}
