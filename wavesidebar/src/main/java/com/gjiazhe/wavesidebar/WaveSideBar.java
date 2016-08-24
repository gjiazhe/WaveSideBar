package com.gjiazhe.wavesidebar;

import android.content.Context;
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

    private final String[] DEFAULT_INDEX_ITEMS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private String[] mIndexItems;
    private float drawWidth;

    private DisplayMetrics mDisplayMetrics;

    private Paint mPaint;
    private int mTextColor;
    private float mTextSize;
    private float mIndexItemHeight;

    private RectF mItemDrawArea = new RectF();

    private boolean mStartTouching = false;

    private OnSelectIndexItemListener onSelectIndexItemListener;


    public WaveSideBar(Context context) {
        this(context, null);
    }

    public WaveSideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDisplayMetrics = getContext().getResources().getDisplayMetrics();

        mTextColor = Color.GRAY;
        mTextSize = sp2px(DEFAULT_TEXT_SIZE);
        mIndexItemHeight = mTextSize * 1.3f;
        initPaint();

        mIndexItems = DEFAULT_INDEX_ITEMS;
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(this.mTextColor);
        mPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // vertical center
        float textStartY = (getHeight()/2 - mIndexItems.length*mIndexItemHeight/2)
                - (mIndexItemHeight/2 - mTextSize/2);

        for (String indexItem : mIndexItems) {
            textStartY += mIndexItemHeight;
            canvas.drawText(indexItem, getWidth() - drawWidth / 2, textStartY, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float drawHeight = mIndexItems.length*mIndexItemHeight;
        float mStartDrawY = getHeight() / 2 - drawHeight / 2;

        for (String indexItem : mIndexItems) {
            float itemTextWidth = mPaint.measureText(indexItem);
            drawWidth = Math.max(drawWidth, itemTextWidth);
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
                    if (onSelectIndexItemListener != null) {
                        int index = getSelectedIndex(eventY);
                        onSelectIndexItemListener.onSelectIndexItem(mIndexItems[index]);
                    }
                    mStartTouching = true;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mStartTouching && onSelectIndexItemListener != null) {
                    int index = getSelectedIndex(eventY);
                    onSelectIndexItemListener.onSelectIndexItem(mIndexItems[index]);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mStartTouching = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private int getSelectedIndex(float eventY) {
        float currentY = eventY - (getHeight()/2 - mIndexItems.length*mIndexItemHeight/2);

        int index = (int) (currentY / this.mIndexItemHeight);
        if (index <= 0) {
            return 0;
        }
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
