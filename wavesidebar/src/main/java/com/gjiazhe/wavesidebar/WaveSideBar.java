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

    private String[] mIndexItems = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private DisplayMetrics mDisplayMetrics;

    private Paint mPaint;
    private int mTextColor;
    private float mTextSize;
    private float mIndexItemHeight;


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
        float startY = (getHeight()/2 - mIndexItems.length*mIndexItemHeight/2)
                - (mIndexItemHeight/2 - mTextSize/2);

        for (String mIndexItem : mIndexItems) {
            startY += mIndexItemHeight;
            canvas.drawText(mIndexItem, getWidth() - mTextSize, startY, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIndexItems.length == 0) {
            return super.onTouchEvent(event);
        }

        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int index = getSelectedIndex(eventY);
                if (onSelectIndexItemListener != null) {
                    onSelectIndexItemListener.onSelectIndexItem(mIndexItems[index]);
                }
        }

        return super.onTouchEvent(event);
    }

    private int getSelectedIndex(float eventY) {
        float currentY = eventY - getPaddingTop()
                - (getHeight()/2 - mIndexItems.length*mIndexItemHeight/2);

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
