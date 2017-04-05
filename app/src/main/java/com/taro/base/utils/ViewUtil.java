package com.taro.base.utils;

import android.app.Service;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.taro.base.base.BaseApp;

/**
 * Created by taro on 16/12/22.
 */

public class ViewUtil {
    public static final void setViewLayoutParamsWidthPadding(View itemView, int width, int height,
                                                             int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(0, 0);
        }
        params.width = width;
        params.height = height;
        itemView.setLayoutParams(params);
        itemView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    public static final void setViewLayoutParamsWidthMargin(View itemView, int width, int height, int marginLeft, int marginTop, int marginRight, int marginBottom) {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams = null;
        if (params == null) {
            params = new ViewGroup.LayoutParams(0, 0);
        }
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            marginParams = new ViewGroup.MarginLayoutParams(params);
        } else {
            marginParams = (ViewGroup.MarginLayoutParams) params;
        }
        marginParams.width = width;
        marginParams.height = height;
        marginParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        itemView.setLayoutParams(marginParams);
    }


    public static final boolean isValidEditText(EditText... edits) {
        if (edits != null) {
            for (EditText editText : edits) {
                if (editText != null) {
                    if (TextUtils.isEmpty(editText.getText())) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 强制隐藏输入法
     * @param view
     */
    public static void hideInputMethodPanel(@NonNull View view) {
        InputMethodManager inputMgr = (InputMethodManager) BaseApp.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
        //强制隐藏
        inputMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 强制显示输入法
     * @param view
     */
    public static void showInputMethodPanel(@NonNull View view) {
        InputMethodManager inputMgr = (InputMethodManager) BaseApp.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
        inputMgr.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }


    public interface OnViewDrawableClickListener {
        public void onClick(View view, int position);
    }

    public static class DrawableClickListener implements View.OnTouchListener {
        private OnViewDrawableClickListener mViewListener;

        public DrawableClickListener(OnViewDrawableClickListener listener) {
            mViewListener = listener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v instanceof TextView && mViewListener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                TextView tv = (TextView) v;
                Drawable[] draws = tv.getCompoundDrawables();
                if (draws != null) {
                    for (int i = 0; i < draws.length; i++) {
                        Drawable compoun = draws[i];
                        if (compoun != null) {
                            int position = isInDrawable(i, (int) event.getX(), (int) event.getY(), tv, compoun);
                            if (position != -1) {
                                mViewListener.onClick(v, position);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        private int isInDrawable(int index, int x, int y, @NonNull TextView containView, @NonNull Drawable drawable) {
            int paddingLeft = containView.getPaddingLeft();
            int paddingRight = containView.getWidth() - containView.getPaddingRight();
            int paddingTop = containView.getPaddingTop();
            int paddingBottom = containView.getHeight() - containView.getPaddingBottom();
            switch (index) {
                case 0:
                    //LEFT
                    if (x > paddingLeft && x < paddingLeft + drawable.getIntrinsicWidth()) {
                        return Gravity.LEFT;
                    }
                    break;
                case 1:
                    //TOP
                    if (y > paddingTop && y < paddingTop + drawable.getIntrinsicHeight()) {
                        return Gravity.TOP;
                    }
                    break;
                case 2:
                    //RIGHT
                    if (x < paddingRight && x > paddingRight - drawable.getIntrinsicWidth()) {
                        return Gravity.RIGHT;
                    }
                    break;
                case 3:
                    //BOTTOM
                    if (y < paddingBottom && y < paddingBottom - drawable.getIntrinsicHeight()) {
                        return Gravity.BOTTOM;
                    }
                    break;
            }
            return -1;
        }
    }
}
