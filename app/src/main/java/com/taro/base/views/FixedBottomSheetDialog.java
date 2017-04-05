package com.taro.base.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.taro.base.R;


/**
 * Created by taro on 2017/2/15.
 */

public class FixedBottomSheetDialog extends AppCompatDialog {
    private View mContentView;
    private int mBackgroundColor = Integer.MIN_VALUE;

    public FixedBottomSheetDialog(@NonNull Context context) {
        this(context, 0);
    }

    public FixedBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, getThemeResId(context, theme));
        // We hide the title bar for any style configuration. Otherwise, there will be a gap
        // above the bottom sheet when it is expanded.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    protected FixedBottomSheetDialog(@NonNull Context context, boolean cancelable,
                                     OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public View getContentView() {
        return mContentView;
    }

    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundColor = color;
        if (Color.alpha(color) == 0) {
            getWindow().setBackgroundDrawable(new ColorDrawable());
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    public void setBackgroundColorRes(@ColorRes int id) {
        mBackgroundColor = getContext().getResources().getColor(id);
        setBackgroundColor(mBackgroundColor);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(wrapInBottomSheet(layoutResId, null, null));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (mBackgroundColor != Integer.MIN_VALUE) {
            setBackgroundColor(mBackgroundColor);
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66000000")));
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(wrapInBottomSheet(0, view, null));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(wrapInBottomSheet(0, view, params));
    }

    private View wrapInBottomSheet(int layoutResId, View view, ViewGroup.LayoutParams params) {
        ViewGroup.LayoutParams frameParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View outSideView = new View(getContext());
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(frameParams);
        outSideView.setLayoutParams(frameParams);
        frameLayout.addView(outSideView);


        if (layoutResId != 0 && view == null) {
            view = getLayoutInflater().inflate(layoutResId, frameLayout, false);
        }
        FrameLayout.LayoutParams childParams = null;
        if (params == null) {
            params = view.getLayoutParams();
        }

        if (params != null) {
            childParams = new FrameLayout.LayoutParams(params);
        } else {
            childParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        childParams.gravity = Gravity.BOTTOM;
        frameLayout.addView(view, childParams);


        // We treat the CoordinatorLayout as outside the dialog though it is technically inside
        if (shouldWindowCloseOnTouchOutside()) {
            outSideView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isShowing()) {
                                cancel();
                            }
                        }
                    });
        }
        mContentView = view;
        return frameLayout;
    }

    private boolean shouldWindowCloseOnTouchOutside() {
        if (Build.VERSION.SDK_INT < 11) {
            return true;
        }
        TypedValue value = new TypedValue();
        //noinspection SimplifiableIfStatement
        if (getContext().getTheme()
                .resolveAttribute(android.R.attr.windowCloseOnTouchOutside, value, true)) {
            return value.data != 0;
        }
        return false;
    }

    private static int getThemeResId(Context context, int themeId) {
        if (themeId == 0) {
            // If the provided theme is 0, then retrieve the dialogTheme from our theme
            TypedValue outValue = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    android.support.design.R.attr.bottomSheetDialogTheme, outValue, true)) {
                themeId = outValue.resourceId;
            } else {
                themeId = 0;
            }
        }
        return themeId;
    }

}
