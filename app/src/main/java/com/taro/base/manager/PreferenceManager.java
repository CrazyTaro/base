package com.taro.base.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.taro.base.base.BaseApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * preference操作管理工具
 * Created by taro on 16/9/9.
 */
public class PreferenceManager {
    public static final String PREFERENCE_PRIVATE_SP = "base_private_sp";


    public static int getInt(@NonNull String key, int defaultValue) {
        SharedPreferences sp = openSharePreference();
        return sp.getInt(key, defaultValue);
    }

    public static float getFloat(@NonNull String key, float defaultValue) {
        SharedPreferences sp = openSharePreference();
        return sp.getFloat(key, defaultValue);
    }

    public static long getLong(@NonNull String key, long defaultValue) {
        SharedPreferences sp = openSharePreference();
        return sp.getLong(key, defaultValue);
    }

    public static boolean getBoolean(@NonNull String key, boolean defaultValue) {
        SharedPreferences sp = openSharePreference();
        return sp.getBoolean(key, defaultValue);
    }

    public static String getString(@NonNull String key, String defaultValue) {
        SharedPreferences sp = openSharePreference();
        return sp.getString(key, defaultValue);
    }


    /**
     * 读取保存到SP中的某个对象的JSON字符串并加载成对象返回
     *
     * @param key
     * @param clazz 需要加载的对象类型
     * @param <T>
     * @return
     */
    public static <T> T getObject(@NonNull String key, Class<T> clazz) {
        SharedPreferences sp = openSharePreference();
        String json = sp.getString(key, null);
        if (json != null) {
            try {
                return new Gson().fromJson(json, clazz);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 读取保存到SP中的某个对象的JSON字符串并加载成对象返回<br>
     * 此方法适用于泛型列表等
     *
     * @param key
     * @param type 需要加载的对象类型
     * @param <T>
     * @return
     */
    public static <T> T getObject(@NonNull String key, Type type) {
        SharedPreferences sp = openSharePreference();
        String json = sp.getString(key, null);
        if (json != null) {
            try {
                return new Gson().fromJson(json, type);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 保存一个sharePreference值,根据Value的类型自动确定需要保存的方式,取出时只需要使用对应的{@code get}方式即可;<br>
     * 仅支持基础的数据类型<br>
     * 当保存的为一个对象而非SP自身支持的类型时,会将其转成JSON并保存为字符串,可通过{@link #getObject(String, Class)}方式获取
     *
     * @param key   key为空或者null时不进行任何保存工作
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
            } else {
                String json = new Gson().toJson(value, value.getClass());
                editor.putString(key, json);
            }
            editor.apply();
            return true;
        }
    }

    //打开sharePreference
    private static SharedPreferences openSharePreference() {
        return getContext().getSharedPreferences(PREFERENCE_PRIVATE_SP, Context.MODE_PRIVATE);
    }

    //需要修改返回正确的application context
    private static Context getContext() {
        return BaseApp.getContext();
    }


    /**
     * 将对象写入为指定文件名文件
     *
     * @param obj      对象
     * @param fileName 文件名
     */
    public static boolean saveObject(@NonNull Context context, Serializable obj, @NonNull String fileName) {
        ObjectOutputStream output = null;
        try {
            if (obj != null) {
                File pathFile = context.getFilesDir();
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
    public static <T> T readObject(@NonNull Context context, @NonNull String fileName) {
        ObjectInputStream input = null;
        try {
            File pathFile = context.getFilesDir();
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
        File pathFile = getContext().getFilesDir();
        File file = new File(pathFile, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static final class Editor_ {
        private SharedPreferences sp = null;
        private SharedPreferences.Editor ed = null;

        /**
         * 指定某个preference进行修改
         *
         * @param preferenceName
         */
        public Editor_(@NonNull String preferenceName) {
            sp = getContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
            ed = sp.edit();
        }

        public Editor_() {
            this(PREFERENCE_PRIVATE_SP);
        }

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
