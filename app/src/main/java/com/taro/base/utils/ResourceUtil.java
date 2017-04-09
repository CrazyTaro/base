package com.taro.base.utils;

import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by taro on 16/9/17.
 */
public class ResourceUtil {
    public static Application mApp = null;

    /**
     * inti the util,this method must be called before using any other methods.and just need to call once.
     *
     * @param app
     */
    public static final void init(Application app) {
        mApp = app;
    }

    /**
     * check if the app is exist,if application is null, a runtime exception will be thrown.
     *
     * @return always return true
     */
    public static final boolean isAppExist() {
        if (mApp == null) {
            throw new RuntimeException("application is null,have to call 'init()' before using any methods\n" +
                    "在使用任何方法之前请先调用一次init(),仅需要调用一次");
        } else {
            return true;
        }
    }

    public static final float getScreenWidth() {
        if (isAppExist()) {
            return mApp.getResources().getDisplayMetrics().widthPixels;
        } else {
            return 0;
        }
    }

    public static final float getScreenHeight() {
        if (isAppExist()) {
            return mApp.getResources().getDisplayMetrics().heightPixels;
        } else {
            return 0;
        }
    }

    @Nullable
    public static final String getString(@StringRes int resId) {
        if (isAppExist() && resId != 0) {
            return mApp.getResources().getString(resId);
        } else {
            return null;
        }
    }

    /**
     * 获取格式化的字符串结果
     *
     * @param resId 格式化字符串的ID
     * @param objs  格式化数据对象
     * @return
     */
    public static final String getFormatString(@StringRes int resId, Object... objs) {
        String format = getString(resId);
        return getFormatString(format, objs);
    }

    /**
     * 获取格式化的字符串结果
     *
     * @param format 进行格式化的字符串
     * @param objs   格式化数据对象
     * @return
     */
    public static final String getFormatString(String format, Object... objs) {
        if (format != null && objs != null) {
            return String.format(format, objs);
        } else {
            return null;
        }
    }

    /**
     * 获取格式化字符串结果
     *
     * @param resID 格式化字符串ID
     * @param ress  字符串资源ID
     * @return
     */
    public static final String getFormatStringFromRes(@StringRes int resID, @StringRes int... ress) {
        String format = getString(resID);
        return getFormatStringFromRes(format, ress);
    }

    /**
     * 获取格式化字符串结果
     *
     * @param format
     * @param ress   字符串资源ID
     * @return
     */
    public static final String getFormatStringFromRes(String format, @StringRes int... ress) {
        if (ress != null && ress.length > 0) {
            Object[] objs = new Object[ress.length];
            for (int i = 0; i < ress.length; i++) {
                objs[i] = getString(ress[i]);
            }
            return getFormatString(format, objs);
        } else {
            return format;
        }
    }

    public static final float getDimen(@DimenRes int resId) {
        if (isAppExist()) {
            return mApp.getResources().getDimension(resId);
        } else {
            return -1;
        }
    }

    public static final int getColor(@Nullable String colorStr, @ColorInt int defaultColor) {
        if (isEmpty(colorStr)) {
            return defaultColor;
        } else {
            try {
                return Color.parseColor(colorStr);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return defaultColor;
            }
        }
    }

    public static final int getColor(@ColorRes int resId) {
        if (isAppExist()) {
            return mApp.getResources().getColor(resId);
        } else {
            return -1;
        }
    }

    public static final Drawable getDrawable(@DrawableRes int resId) {
        if (isAppExist()) {
            Drawable drawable = mApp.getResources().getDrawable(resId);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            return drawable;
        } else {
            return null;
        }
    }

    public static final float convertDpi2Px(float dp) {
        if (isAppExist()) {
            return convertOther2Px(TypedValue.COMPLEX_UNIT_DIP, dp);
        } else {
            return 0;
        }
    }

    public static final float converSp2Px(float sp) {
        if (isAppExist()) {
            return convertOther2Px(TypedValue.COMPLEX_UNIT_SP, sp);
        } else {
            return 0;
        }
    }


    public static float convertDimension(@DimensionUnit int fromUnit, @DimensionUnit int toUnit, float valueForConvert) {
        float result = valueForConvert;
        if (fromUnit != TypedValue.COMPLEX_UNIT_PX) {
            result = convertOther2Px(fromUnit, result);
        }
        if (toUnit != TypedValue.COMPLEX_UNIT_PX) {
            result = convertPx2Other(toUnit, result);
        }
        return result;
    }

    public static final float convertOther2Px(@DimensionUnit int fromUnit, float otherValue) {
        if (isAppExist()) {
            float rate = computeConvertRate(fromUnit, mApp.getResources().getDisplayMetrics());
            return otherValue * rate;
        } else {
            return 0;
        }
    }

    public static final float convertPx2Other(@DimensionUnit int toUnit, float pxValue) {
        if (isAppExist()) {
            float rate = computeConvertRate(toUnit, mApp.getResources().getDisplayMetrics());
            return pxValue / rate;
        } else {
            return 0;
        }
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() <= 0;
    }

    private static final float computeConvertRate(@DimensionUnit int unit, @NonNull DisplayMetrics metrics) {
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_DIP:
                return metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return metrics.xdpi * (1.0f / 72);
            case TypedValue.COMPLEX_UNIT_IN:
                return metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return metrics.xdpi * (1.0f / 25.4f);
            default:
                return 1;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {TypedValue.COMPLEX_UNIT_PX, TypedValue.COMPLEX_UNIT_DIP, TypedValue.COMPLEX_UNIT_SP,
            TypedValue.COMPLEX_UNIT_PT, TypedValue.COMPLEX_UNIT_IN, TypedValue.COMPLEX_UNIT_MM})
    public @interface DimensionUnit {
    }
}

