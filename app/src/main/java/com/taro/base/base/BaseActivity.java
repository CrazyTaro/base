package com.taro.base.base;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;


import com.taro.base.R;
import com.taro.base.helper.MenuHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by taro on 2017/2/5.
 */

public class BaseActivity extends AppCompatActivity implements MenuHelper.OnMenuClickListener {
    @Nullable
    @BindView(R.id.layout_menu)
    View mVMenu;

    private MenuHelper mMenuHelper;

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ButterKnife.bind(this, view);
        this.initMenu();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ButterKnife.bind(this, view);
        this.initMenu();
    }

    public MenuHelper getMenuHelper() {
        return mMenuHelper;
    }

    private void initMenu() {
        if (mVMenu != null) {
            mMenuHelper = new MenuHelper(mVMenu);
            mMenuHelper.setMenuClickListener(this);
        }
    }

    @Override
    public void onLeftMenuClick() {
        this.finish();
    }

    @Override
    public void onRightMenuClick() {

    }

    @Override
    public void onMultiRightMenuIconFirstClick() {

    }

    @Override
    public void onMultiRightMenuIconSecondClick() {

    }
}
