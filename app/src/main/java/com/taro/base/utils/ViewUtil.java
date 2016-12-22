package com.taro.base.utils;

import android.app.Service;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
}
