package com.taro.base.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by taro on 2017/2/7.
 */

public abstract class BasePageAdapter<T extends View> extends PagerAdapter {
    private View[] mViewArr;
    private int mCount;
    protected Context mContext;

    public BasePageAdapter(@NonNull Context context, int count) {
        mContext = context;
        mCount = count;
        mViewArr = new View[mCount];
        for (int i = 0; i < mCount; i++) {
            View rv = onCreateView(context, i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ViewGroup.LayoutParams newParams = onInitLayoutParams((T) rv, params, i);
            if (newParams != null) {
                rv.setLayoutParams(newParams);
            } else {
                rv.setLayoutParams(params);
            }
            mViewArr[i] = rv;
        }
    }

    public Context getContext() {
        return mContext;
    }

    @NonNull
    protected abstract T onCreateView(Context context, int index);

    protected abstract ViewGroup.LayoutParams onInitLayoutParams(T view, ViewGroup.LayoutParams params, int index);


    @Nullable
    public T getItem(int position) {
        if (position >= 0 && position < mCount) {
            return (T) mViewArr[position];
        } else {
            return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View rv = mViewArr[position];
        if (container.getChildCount() > 0 && container.getChildAt(0) != rv) {
            container.removeView(rv);
        }
        container.addView(rv);
        return rv;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mViewArr[position].setTag(null);
        mViewArr[position] = null;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
