package com.taro.base.base;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by taro on 16/10/13.
 */

public abstract class BaseFragment extends Fragment {
    private boolean mIsFinishedFirstVisible = false;
    private boolean mIsViewInit = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsViewInit = true;
        if (getUserVisibleHint() && !mIsFinishedFirstVisible) {
            onFirstFragmentVisible();
            mIsFinishedFirstVisible = true;
        } else {
            onFragmentStatusChanged(true, getUserVisibleHint());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        boolean oldVisible = this.getUserVisibleHint();
        super.setUserVisibleHint(isVisibleToUser);
        if (oldVisible != isVisibleToUser) {
            if (mIsViewInit && !mIsFinishedFirstVisible) {
                onFirstFragmentVisible();
                mIsFinishedFirstVisible = true;
            } else {
                onFragmentStatusChanged(mIsViewInit, isVisibleToUser);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View contentView = inflater.inflate(getLayoutId(), container, false);
            //TODO:绑定界面控件
            //ButterKnife.bind(this, contentView);
            initViewAfterInflate(contentView, contentView != null);
            return contentView;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取此fragment的布局ID
     *
     * @return
     */
    public abstract int getLayoutId();

    /**
     * 加载布局后调用的初始化布局界面,不建议在这里初始化数据;<br>
     * 已经bind过view了
     *
     * @param contentView  加载后的布局界面
     * @param isLayoutFail 是否加载布局失败,当layoutId不正确时加载失败
     */
    public abstract void initViewAfterInflate(View contentView, boolean isLayoutFail);

    /**
     * fragment第一次可见,一般可用于初始化数据
     */
    public abstract void onFirstFragmentVisible();

    /**
     * 当fragment的状态切换时回调,包括fragment对用户是否可见状态切换时及view初始完毕时;(第一次可见时不会回调)
     *
     * @param isViewInit    当前view是否初始完毕
     * @param isUserVisible 当前fragment对用户是否可见
     */
    public abstract void onFragmentStatusChanged(boolean isViewInit, boolean isUserVisible);


    /**
     * TODO:这个可有可无吧...
     * 获取fragment的icon图标
     *
     * @return
     */
    @DrawableRes
    public abstract int getIconRes();

    /**
     * 获取fragment的title,默认返回fragnt的类名
     *
     * @return
     */
    @NonNull
    public String getFragmentTitle() {
        return this.getClass().getSimpleName();
    }

    /**
     * fragment是否已经完成第一次可见初始化了.
     *
     * @return
     */
    public boolean isFinishedFirstFragmentVisible() {
        return mIsFinishedFirstVisible;
    }
}
