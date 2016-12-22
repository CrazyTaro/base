package com.taro.base.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.elephant.guitar.utils.ResourceUtil;

/**
 * Created by taro on 16/10/17.
 */

public class DividerColorItemDecoration extends RecyclerView.ItemDecoration {
    private Rect mOutRect = null;
    private Rect mDrawRect = null;
    private Paint mPaint = null;

    private boolean mIsSetRect = false;
    private int[] mLineHeights = null;
    private byte[] mDrawSize = null;

    public DividerColorItemDecoration(@ColorInt int color, int dividerWidth, boolean isDp) {
        this();
        this.setColor(color);
        this.setDrawDividerAroundItem(dividerWidth, isDp);
    }

    public DividerColorItemDecoration() {
        mOutRect = new Rect();
        mDrawRect = new Rect();
        mPaint = new Paint();
        mDrawSize = new byte[4];
        mLineHeights = new int[4];
    }

    public DividerColorItemDecoration setColor(@ColorInt int color) {
        mPaint.setColor(color);
        return this;
    }

    public DividerColorItemDecoration setIsDrawDividerForSize(boolean isDrawLeft, boolean isDrawTop, boolean isDrawRight, boolean isDrawBottom) {
        mDrawSize[0] = (byte) (isDrawLeft ? 0 : 1);
        mDrawSize[1] = (byte) (isDrawTop ? 0 : 1);
        mDrawSize[2] = (byte) (isDrawRight ? 0 : 1);
        mDrawSize[3] = (byte) (isDrawBottom ? 0 : 1);

        mIsSetRect = false;
        return this;
    }

    public DividerColorItemDecoration setIsDrawDividerForSize(boolean isDraw) {
        setIsDrawDividerForSize(isDraw, isDraw, isDraw, isDraw);
        return this;
    }

    public DividerColorItemDecoration setDrawDividerAroundItem(int dividerWidth, boolean isDp) {
        return setDrawDividerAroundItem(dividerWidth, dividerWidth, dividerWidth, dividerWidth, isDp);
    }

    public DividerColorItemDecoration setDrawDividerAroundItem(int left, int top, int right, int bottom, boolean isDp) {
        if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0) {
            if (isDp) {
                left = (int) ResourceUtil.convertDpi2Px(left);
                top = (int) ResourceUtil.convertDpi2Px(top);
                right = (int) ResourceUtil.convertDpi2Px(right);
                bottom = (int) ResourceUtil.convertDpi2Px(bottom);
            }
            mLineHeights[0] = left;
            mLineHeights[1] = top;
            mLineHeights[2] = right;
            mLineHeights[3] = bottom;
        }

        mIsSetRect = false;
        return this;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        for (int i = 0; i < state.getItemCount(); i++) {
            if (i + 1 != state.getItemCount()) {
                View child = parent.getChildAt(i);
                if (child != null) {
                    getItemOffsets(mOutRect, child, parent, state);
                    drawDivider(c, parent, child, mOutRect.left, 0);
                    drawDivider(c, parent, child, mOutRect.top, 1);
                    drawDivider(c, parent, child, mOutRect.right, 2);
                    drawDivider(c, parent, child, mOutRect.bottom, 3);
                }
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (!mIsSetRect) {
            for (int i = 0; i < mDrawSize.length; i++) {
                if (mDrawSize[i] == 0) {
                    setRect(mOutRect, i, mLineHeights[i]);
                }
            }
            mIsSetRect = true;
        }
        //复用,每次都一样
        outRect.set(mOutRect);
    }

    private void setRect(Rect outRect, int index, int value) {
        switch (index) {
            case 0:
                outRect.left = value;
                break;
            case 1:
                outRect.top = value;
                break;
            case 2:
                outRect.right = value;
                break;
            case 3:
                outRect.bottom = value;
                break;
        }
    }

    private void drawDivider(Canvas canvas, ViewGroup parent, View childView, int dividerWidth, int drawIndex) {
        if (dividerWidth > 0) {
            mDrawRect.set(0, 0, 0, 0);
            int coordinate = 0;
            switch (drawIndex) {
                case 0:
                    coordinate = childView.getLeft();
                    mDrawRect.set(coordinate - dividerWidth, 0, coordinate, parent.getHeight());
                    break;
                case 1:
                    coordinate = childView.getTop();
                    mDrawRect.set(0, coordinate - dividerWidth, parent.getWidth(), coordinate);
                    break;
                case 2:
                    coordinate = childView.getRight();
                    mDrawRect.set(coordinate, 0, coordinate + dividerWidth, parent.getWidth());
                    break;
                case 3:
                    coordinate = childView.getBottom();
                    mDrawRect.set(0, coordinate, parent.getWidth(), coordinate + dividerWidth);
                    break;
            }
            canvas.drawRect(mDrawRect, mPaint);
        }
    }
}
