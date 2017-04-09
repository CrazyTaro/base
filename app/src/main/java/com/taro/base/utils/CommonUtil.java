package com.taro.base.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;


import com.taro.base.base.BaseApp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Created by taro on 16/9/18.
 */
public class CommonUtil {
    public static int getActivityRequestCode(@NonNull Class clazz) {
        String name = clazz.getSimpleName();
        int code = name.hashCode();
        return (short) code;
    }

    /**
     * 复制文本到剪贴板中
     *
     * @param context 用于获取剪贴板服务对象
     * @param text    需要复制的文本
     * @return 操作成功返回true, 否则返回false
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean copyToClipBoard(@NonNull Context context, CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Service.CLIPBOARD_SERVICE);
            ClipData myClip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(myClip);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 从剪贴板中将文本取出
     *
     * @param context 用于获取剪贴板服务对象
     * @return 返回值必定不为空;若不存在数据返回空字符串,否则返回获取的字符串本身.
     */
    @NonNull
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static CharSequence pasteFromClipBoard(@NonNull Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Service.CLIPBOARD_SERVICE);
        ClipData abc = clipboard.getPrimaryClip();
        if (abc != null && abc.getItemCount() > 0) {
            ClipData.Item item = abc.getItemAt(0);
            return (item == null || TextUtils.isEmpty(item.getText())) ? "" : item.getText();
        } else {
            return "";
        }
    }

    /**
     * 打开电话拨打界面
     *
     * @param act
     * @param phoneNumber 电话号码
     */
    public static void openCall(@NonNull Activity act, @NonNull String phoneNumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        act.startActivity(intent);
    }

    /**
     * 打开短信发送界面
     *
     * @param act
     * @param phoneNumber 存放短信的集合列表
     * @param message     短信内容
     */
    public static void openSms(@NonNull Activity act, @NonNull String phoneNumber, @Nullable String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
        if (!TextUtils.isEmpty(message)) {
            intent.putExtra("sms_body", message);
        }
        act.startActivity(intent);
    }

    /**
     * 发送多条短信
     *
     * @param act
     * @param phoneNumbers 存放短信的集合列表
     * @param message      短信内容
     */
    public static void openSms(@NonNull Activity act, @NonNull Collection<String> phoneNumbers, @Nullable String message) {
        StringBuilder builder = new StringBuilder(200);
        for (String phone : phoneNumbers) {
            if (!TextUtils.isEmpty(phone)) {
                builder.append(phone);
                builder.append(";");
            }
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        //phone不存在
        if (builder.length() <= 0) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + builder.toString()));
        if (!TextUtils.isEmpty(message)) {
            intent.putExtra("sms_body", message);
        }
        act.startActivity(intent);
    }

    /**
     * 获取版本号(内部识别号)
     *
     * @param context
     * @return
     */
    public static int getVersionCode(@NonNull Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    public static String getPackageName(@NonNull Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 获取时间显示的文本
     *
     * @param millisecond
     * @return
     */
    public static String getFormatDateTimeStr(long millisecond) {
        long past = new Date().getTime() - millisecond;
        if (past < 10 * 1000) {
            return "刚刚";
        } else if (past < 60 * 1000) {
            long pastStr = past / 1000;
            return pastStr + "秒前";
        } else if (past < 3600 * 1000) {
            long pastStr = past / 1000 / 60;
            return pastStr + "分前";
        } else if (past < 24 * 3600 * 1000) {
            long pastStr = past / 1000 / 60 / 60;
            return pastStr + "小时前";
        } else {
            int nowYear = 0;
            int createYear = 0;
            String formatStr = null;
            Calendar dateTime = Calendar.getInstance();
            nowYear = dateTime.get(Calendar.YEAR);
            dateTime.setTimeInMillis(millisecond);
            createYear = dateTime.get(Calendar.YEAR);

            if (nowYear != createYear) {
                formatStr = "yyyy-MM-dd HH:mm";
            } else {
                formatStr = "MM-dd HH:mm";
            }
            DateFormat df = new SimpleDateFormat(formatStr);
            return df.format(millisecond);
        }
    }

    /**
     * 检测网络是否连接
     *
     * @return
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager conMan = (ConnectivityManager) BaseApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //mobile 3G Data Network
        //兼容到API21以下
        NetworkInfo[] infos = conMan.getAllNetworkInfo();
        if (infos != null) {
            for (NetworkInfo info : infos) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测WIFI是否开启
     *
     * @return
     */
    public static boolean isNetworkWifi(boolean checkNetworkConnFirst) {
        boolean isConn = false;
        ConnectivityManager conMan = (ConnectivityManager) BaseApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (checkNetworkConnFirst) {
            //兼容到API21以下
            NetworkInfo[] infos = conMan.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo info : infos) {
                    if (info.isConnected()) {
                        isConn = true;
                        break;
                    }
                }
            }
        } else {
            isConn = true;
        }
        if (isConn) {
            NetworkInfo info = conMan.getActiveNetworkInfo();
            return (info != null && info.getType() == ConnectivityManager.TYPE_WIFI);
        } else {
            return false;
        }
    }

    /**
     * 检测当前网络是否使用数据网络(有可能同时也开启了WIFI)
     *
     * @return
     */
    public static boolean isNetworkMobile(boolean checkNetworkConnFirst) {
        boolean isConn = false;
        ConnectivityManager conMan = (ConnectivityManager) BaseApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (checkNetworkConnFirst) {
            //兼容到API21以下
            NetworkInfo[] infos = conMan.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo info : infos) {
                    if (info.isConnected()) {
                        isConn = true;
                        break;
                    }
                }
            }
        } else {
            isConn = true;
        }
        if (isConn) {
            NetworkInfo info = conMan.getActiveNetworkInfo();
            return (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
        } else {
            return false;
        }
    }

    /**
     * 检查输入是否合法，某些地方只能输入a-z A-Z 中文字符和"_"和"-";
     *
     * @return
     */
    public static boolean inputValidation(String str) {
        String reg = "[^a-zA-Z0-9\u4E00-\u9FA5_-]";
        if (str.replaceAll(reg, "").length() != str.length()) {
            return false;
        }
        return true;
    }

}
