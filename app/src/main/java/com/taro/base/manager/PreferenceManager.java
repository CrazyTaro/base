package com.taro.base.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.taro.base.base.BaseApp;
import com.taro.base.constant.ConstantPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by taro on 16/9/9.
 */
public class PreferenceManager {

    public static int getInt(@NonNull String key, int defaultValue) {
        SharedPreferences sp = BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }

    public static float getFloat(@NonNull String key, float defaultValue) {
        SharedPreferences sp = BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
        return sp.getFloat(key, defaultValue);
    }

    public static long getLong(@NonNull String key, long defaultValue) {
        SharedPreferences sp = BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
        return sp.getLong(key, defaultValue);
    }

    public static boolean getBoolean(@NonNull String key, boolean defaultValue) {
        SharedPreferences sp = BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    public static String getString(@NonNull String key, String defaultValue) {
        SharedPreferences sp = BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }


    /**
     * 保存一个sharePreference值
     *
     * @param key
     * @param value
     */
    public static boolean saveValue(String key, Object value) {
        if (TextUtils.isEmpty(key)) {
            return false;
        } else {
            SharedPreferences sp = openSharePreference();
            SharedPreferences.Editor editor = sp.edit();
            if (value == null) {
                editor.putString(key, null);
            } else if (value.getClass() == Integer.class || value.getClass() == int.class) {
                editor.putInt(key, (int) value);
            } else if (value.getClass() == Float.class || value.getClass() == float.class) {
                editor.putFloat(key, (float) value);
            } else if (value.getClass() == Long.class || value.getClass() == long.class) {
                editor.putLong(key, (long) value);
            } else if (value.getClass() == String.class) {
                editor.putString(key, (String) value);
            } else if (value.getClass() == Boolean.class || value.getClass() == boolean.class) {
                editor.putBoolean(key, (boolean) value);
            }
            editor.apply();
            return true;
        }
    }

    //打开sharePreference
    private static SharedPreferences openSharePreference() {
        return BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
    }


    /**
     * 将对象写入为指定文件名文件
     *
     * @param obj      对象
     * @param fileName 文件名
     */
    public static boolean saveObject(Serializable obj, @NonNull String fileName) {
        ObjectOutputStream output = null;
        try {
            if (obj != null) {
                File pathFile = BaseApp.getContext().getFilesDir();
                if (pathFile != null) {
                    pathFile.mkdirs();
                    File studentFile = new File(pathFile, fileName);
                    if (studentFile.exists()) {
                        studentFile.delete();
                    }
                    studentFile.createNewFile();
                    output = new ObjectOutputStream(new FileOutputStream(studentFile));
                    output.writeObject(obj);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取保存的对象
     *
     * @param fileName 保存的文件名
     * @return
     */
    public static <T> T readObject(@NonNull String fileName) {
        ObjectInputStream input = null;
        try {
            File pathFile = BaseApp.getContext().getFilesDir();
            if (pathFile == null) {
                return null;
            } else {
                File studentFile = new File(pathFile, fileName);
                if (studentFile.exists()) {
                    input = new ObjectInputStream(new FileInputStream(studentFile));
                    return (T) input.readObject();
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除数据对象
     *
     * @param fileName
     */
    public static void deleteObject(@NonNull String fileName) {
        File pathFile = BaseApp.getContext().getFilesDir();
        File file = new File(pathFile, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static final class Editor_ {
        SharedPreferences sp = BaseApp.getContext().getSharedPreferences(ConstantPreference.PREFRENCE_PRIVATE_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();

        public Editor_ putInt(@NonNull String key, int value) {
            ed.putInt(key, value);
            return this;
        }

        public Editor_ putFloat(@NonNull String key, float value) {
            ed.putFloat(key, value);
            return this;
        }

        public Editor_ putLong(@NonNull String key, long value) {
            ed.putLong(key, value);
            return this;
        }

        public Editor_ putBoolean(@NonNull String key, boolean value) {
            ed.putBoolean(key, value);
            return this;
        }

        public Editor_ putString(@NonNull String key, String value) {
            ed.putString(key, value);
            return this;
        }

        public int getInt(@NonNull String key, int value) {
            return sp.getInt(key, value);
        }

        public float getFloat(@NonNull String key, float value) {
            return sp.getFloat(key, value);
        }

        public long getLong(@NonNull String key, long value) {
            return sp.getLong(key, value);
        }

        public boolean getBoolean(@NonNull String key, boolean value) {
            return sp.getBoolean(key, value);
        }

        public String getString(@NonNull String key, String value) {
            return sp.getString(key, value);
        }

        public Editor_ removeKey(@NonNull String key) {
            ed.remove(key);
            return this;
        }

        public Editor_ clear() {
            ed.clear();
            return this;
        }

        public Editor_ commit() {
            ed.apply();
            return this;
        }
    }
}
