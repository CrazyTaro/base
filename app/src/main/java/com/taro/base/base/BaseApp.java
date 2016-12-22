package com.taro.base.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.widget.Toast;

import com.taro.base.utils.ResourceUtil;

/**
 * Created by taro on 16/10/13.
 */

public class BaseApp extends Application {
    private static Context mInstance;
    private static Toast mToast;
    private static Handler mUIHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mUIHandler = new Handler(Looper.getMainLooper());

        ExceptionCrashUnhandler.getInstance().init(this);
        ResourceUtil.init(this);

        //TODO:glide图片等其它框架统一配置和初始化
//        Glide.with(mInstance)
//                .setDefaultOptions(new RequestManager.DefaultOptions() {
//                    @Override
//                    public <T> void apply(GenericRequestBuilder<T, ?, ?, ?> requestBuilder) {
//                        //默认缓存全尺寸的图片
//                        requestBuilder.diskCacheStrategy(DiskCacheStrategy.ALL);
//                    }
//                });
    }

    /**
     * TODO:可有可无
     * 获取当前的进程名称,可用于杀死进程
     */
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static Context getContext() {
        return mInstance;
    }

    /**
     * 提交到UI线程更新
     *
     * @param runnable
     */
    public static void postUIThread(Runnable runnable) {
        postUIThreadDelay(runnable, 0);
    }

    /**
     * 提交到UI线程更新
     *
     * @param runnable
     * @param delayMills
     */
    public static void postUIThreadDelay(Runnable runnable, long delayMills) {
        if (mUIHandler != null && runnable != null) {
            //无延迟且当前处于UI线程,直接调用方法操作.
            if (isInUIThread() && delayMills == 0) {
                runnable.run();
            } else {
                mUIHandler.postDelayed(runnable, delayMills);
            }
        }
    }

    public static void showShortToast(String text) {
        if (!TextUtils.isEmpty(text) && mToast != null) {
            runOnUiToast(text, Toast.LENGTH_SHORT);
        }
    }

    public static void showLongToast(String text) {
        if (!TextUtils.isEmpty(text) && mToast != null) {
            runOnUiToast(text, Toast.LENGTH_LONG);
        }
    }

    public static void showShortToast(@StringRes int id) {
        String text = ResourceUtil.getString(id);
        showShortToast(text);
    }

    public static void showLongToast(@StringRes int id) {
        String text = ResourceUtil.getString(id);
        showLongToast(text);
    }

    private static void runOnUiToast(@NonNull final String text, final int toastDuration) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (toastDuration == Toast.LENGTH_LONG) {
                showLongToastInUIThread(text);
            } else {
                showShortToastInUIThread(text);
            }
        } else if (Looper.myLooper() != Looper.getMainLooper() && mUIHandler != null) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (toastDuration == Toast.LENGTH_LONG) {
                        showLongToastInUIThread(text);
                    } else {
                        showShortToastInUIThread(text);
                    }
                }
            });
        }
    }

    private static void showShortToastInUIThread(String text) {
        if (!TextUtils.isEmpty(text) && mToast != null) {
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setText(text);
            mToast.show();
        }
    }

    private static void showLongToastInUIThread(String text) {
        if (!TextUtils.isEmpty(text) && mToast != null) {
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setText(text);
            mToast.show();
        }
    }

    private static boolean isInUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
