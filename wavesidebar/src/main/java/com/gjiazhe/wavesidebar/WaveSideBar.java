package com.gjiazhe.wavesidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    private int mCurrentIndex = -1;
    private float mCurrentY;

    private float drawWidth;

    private DisplayMetrics mDisplayMetrics;

    private Paint mPaint;
    private int mTextColor;
    private float mTextSize;
    private float mIndexItemHeight;

    private float mMaxOffset;

    private RectF mItemDrawArea = new RectF();

    private boolean mStartTouching = false;

    private boolean mLazyRespond = false;

    private OnSelectIndexItemListener onSelectIndexItemListener;


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
        typedArray.recycle();

        mDisplayMetrics = getContext().getResources().getDisplayMetrics();

        mTextColor = Color.GRAY;
        mTextSize = sp2px(DEFAULT_TEXT_SIZE);
        initPaint();

        mIndexItems = DEFAULT_INDEX_ITEMS;

        mMaxOffset = dp2px(DEFAULT_MAX_OFFSET);
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

        // vertical center
        float firstItemBaseLineY = (getHeight()/2 - mIndexItems.length*mIndexItemHeight/2)
                + (mIndexItemHeight/2 - mIndexItemHeight/2)
                - mPaint.getFontMetrics().ascent;

        // draw each item
        for (int i = 0, mIndexItemsLength = mIndexItems.length; i < mIndexItemsLength; i++) {
            float baseLineY = firstItemBaseLineY + mIndexItemHeight*i;

            // calculate the scale factor of the item to draw
            float scale = 0;
            if (mCurrentIndex != -1) {
                scale = 1 - Math.abs(mCurrentY - (mIndexItemHeight*i+mIndexItemHeight/2)) / mIndexItemHeight / 4;
                scale = Math.max(scale, 0);

//                Log.i("scale", mIndexItems[i] + ": " + scale);
                if (i == mCurrentIndex) {
                    mPaint.setAlpha(255);
                } else {
                    mPaint.setAlpha((int)(255 * (1-scale)));
                }
            }

            mPaint.setTextSize(mTextSize + mTextSize*scale);

            // draw
            canvas.drawText(
                    mIndexItems[i], //item text to draw
                    getWidth() - drawWidth/2 - mMaxOffset*scale, //center text X
                    baseLineY, // baseLineY
                    mPaint);

            // reset
            mPaint.setTextSize(mTextSize);
            mPaint.setAlpha(255);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mIndexItemHeight = fontMetrics.bottom - fontMetrics.top;

        float drawHeight = mIndexItems.length*mIndexItemHeight;
        float mStartDrawY = getHeight()/2 - drawHeight/2;

        for (String indexItem : mIndexItems) {
            drawWidth = Math.max(drawWidth, mPaint.measureText(indexItem));
        }
        float mStartDrawX = getWidth() - drawWidth;

        mItemDrawArea.set(mStartDrawX, mStartDrawY, mStartDrawX + drawWidth, mStartDrawY + drawHeight);
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
                if (mItemDrawArea.contains(eventX, eventY)) {
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
        mCurrentY = eventY - (getHeight()/2 - mIndexItems.length*mIndexItemHeight/2);
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


    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.mDisplayMetrics);
    }


    private float sp2px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this.mDisplayMetrics);
    }

    public void setOnSelectIndexItemListener(OnSelectIndexItemListener onSelectIndexItemListener) {
        this.onSelectIndexItemListener = onSelectIndexItemListener;
    }

    public interface OnSelectIndexItemListener {
        void onSelectIndexItem(String item);
    }
}
