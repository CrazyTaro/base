package com.taro.base.helper;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by taro on 2017/2/20.
 */

public class SingleChoiceHelper {
    private Drawable mCheckedDrawable;
    private Drawable mUncheckDrawable;
    private int mChoiceIndex;
    private Context mContext;
    private Point mRecyclePoint;

    private OnChosenChangedListener mOnChosenChangedListener;


    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int drawRes) {
        if (drawRes != 0) {
            try {
                Drawable drawble = context.getResources().getDrawable(drawRes);
                drawble.setBounds(0, 0, drawble.getIntrinsicWidth(), drawble.getIntrinsicHeight());
                return drawble;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 设置textview的drawable
     *
     * @param drawRes drawable资源
     * @param gravity 设置位置,Gravity属性中的left,top,right,bottom
     */
    public static boolean setTextDrawable(TextView tv, @DrawableRes int drawRes, int gravity) {
        if (tv != null && drawRes != 0) {
            Drawable[] drawables = tv.getCompoundDrawables();
            Drawable newDraw = tv.getContext().getResources().getDrawable(drawRes);
            newDraw.setBounds(0, 0, newDraw.getIntrinsicWidth(), newDraw.getIntrinsicHeight());
            int index = 0;
            switch (gravity) {
                case Gravity.LEFT:
                    index = 0;
                    break;
                case Gravity.TOP:
                    index = 1;
                    break;
                case Gravity.RIGHT:
                    index = 2;
                    break;
                case Gravity.BOTTOM:
                    index = 3;
                    break;
                default:
                    index = -1;
                    break;
            }
            if (index != -1) {
                drawables[index] = newDraw;
                tv.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                return true;
            }
        }
        return false;
    }

    /**
     * 设置textview的drawable
     *
     * @param drawRes drawable资源
     * @param gravity 设置位置,Gravity属性中的left,top,right,bottom
     */
    public static boolean setTextDrawable(TextView tv, Drawable drawRes, int gravity) {
        if (tv != null) {
            Drawable[] drawables = tv.getCompoundDrawables();
            int index = 0;
            switch (gravity) {
                case Gravity.LEFT:
                    index = 0;
                    break;
                case Gravity.TOP:
                    index = 1;
                    break;
                case Gravity.RIGHT:
                    index = 2;
                    break;
                case Gravity.BOTTOM:
                    index = 3;
                    break;
                default:
                    index = -1;
                    break;
            }
            if (index != -1) {
                drawables[index] = drawRes;
                tv.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                return true;
            }
        }

        return false;
    }


    public SingleChoiceHelper(Context context) {
        this(context, 0, 0);
    }


    public SingleChoiceHelper(Context context, @DrawableRes int checkedRes, @DrawableRes int uncheckedRes) {
        mContext = context;
        mUncheckDrawable = getDrawable(context, uncheckedRes);
        mCheckedDrawable = getDrawable(context, checkedRes);

        mRecyclePoint = new Point();
    }

    public boolean isPositionChosen(int position) {
        return mChoiceIndex == position;
    }

    public void setChoiceIndex(int choiceIndex) {
        if (mOnChosenChangedListener != null) {
            mChoiceIndex = mOnChosenChangedListener.onChosenIndexChanged(mChoiceIndex, choiceIndex, mCheckedDrawable, mUncheckDrawable);
        } else {
            mChoiceIndex = choiceIndex;
        }
    }

    public int getChoiceIndex() {
        return mChoiceIndex;
    }

    public void setCheckedDrawbleRes(@DrawableRes int drawRes) {
        mCheckedDrawable = getDrawable(mContext, drawRes);
    }

    public void setUnCheckedDrawable(@DrawableRes int drawRes) {
        mUncheckDrawable = getDrawable(mContext, drawRes);
    }

    public Drawable getCheckedDrawble() {
        return mCheckedDrawable;
    }

    public Drawable getUnCheckedDrawable() {
        return mUncheckDrawable;
    }

    public void setOnChosenChangedListener(OnChosenChangedListener onItemClickListener) {
        mOnChosenChangedListener = onItemClickListener;
    }

    @NonNull
    public Point onChosenChangedEvent(int position) {
        if (mChoiceIndex != position) {
            int oldIndex = mChoiceIndex;
            mChoiceIndex = position;
            if (mOnChosenChangedListener != null) {
                mChoiceIndex = mOnChosenChangedListener.onChosenIndexChanged(oldIndex, position, mCheckedDrawable, mUncheckDrawable);
            }
            mRecyclePoint.set(oldIndex, mChoiceIndex);
            return mRecyclePoint;
        } else {
            mRecyclePoint.set(position, position);
            return mRecyclePoint;
        }
    }

    /**
     * 单选选中项改变时回调
     */
    public interface OnChosenChangedListener {
        /**
         * 单选选中项选中时回调
         *
         * @param oldChosen         旧的选中项
         * @param newChosen         新的选中项
         * @param checkedDrawable   选中的图标
         * @param unCheckedDrawable 未选中的图标
         * @return 默认情况下请返回newChosen, 当需要改变选中项时, 直接返回对应的选中项即可
         */
        public int onChosenIndexChanged(int oldChosen, int newChosen, Drawable checkedDrawable, Drawable unCheckedDrawable);
    }
}

