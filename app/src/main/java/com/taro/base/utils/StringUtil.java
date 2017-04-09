package com.taro.base.utils;

import java.util.List;

/**
 * Created by taro on 16/12/23.
 */

public class StringUtil {
    /**
     * 生成图片后的分辨率说明.默认格式为 _widthxheight
     *
     * @param width
     * @param height
     * @return
     */
    public static String generateBmpOptionDesc(int width, int height) {
        return String.format("_%1$dx%2$d", width, height);
    }

    /**
     * 将数组的对象以连接符合并为一个字符串
     *
     * @param arr
     * @param connectChar        连接符
     * @param isSkipNull         当元素为null时是否跳过合并
     * @param placeHolderForNull 当元素为null时使用统一的替换字符(当isSkipNull为false时有效)
     * @return
     */
    public static String joinStr(String[] arr, String connectChar, boolean isSkipNull, String placeHolderForNull) {
        if (arr != null && arr.length > 0) {
            Object[] obj = new Object[arr.length];
            System.arraycopy(arr, 0, obj, 0, arr.length);
            return joinStr(obj, connectChar, isSkipNull, placeHolderForNull);
        } else {
            return null;
        }
    }

    /**
     * 将数组的对象以连接符合并为一个字符串
     *
     * @param arr
     * @param connectChar        连接符
     * @param isSkipNull         当元素为null时是否跳过合并
     * @param placeHolderForNull 当元素为null时使用统一的替换字符(当isSkipNull为false时有效)
     * @return
     */
    public static String joinStr(Object[] arr, String connectChar, boolean isSkipNull, String placeHolderForNull) {
        if (arr != null) {
            if (connectChar == null) {
                connectChar = "";
            }
            StringBuilder builder = new StringBuilder(arr.length * 10);
            for (Object obj : arr) {
                if (obj != null) {
                    builder.append(obj.toString());
                    builder.append(connectChar);
                } else if (!isSkipNull) {
                    builder.append(placeHolderForNull);
                    builder.append(connectChar);
                }
            }
            //删除最后的连接符
            if (builder.length() > 0) {
                builder.delete(builder.length() - connectChar.length(), builder.length());
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    /**
     * 将列表的对象以连接符合并为一个字符串
     *
     * @param list
     * @param connectChar        连接符
     * @param isSkipNull         当元素为null时是否跳过合并
     * @param placeHolderForNull 当元素为null时使用统一的替换字符(当isSkipNull为false时有效)
     * @return
     */
    public static String joinStr(List<? extends Object> list, String connectChar, boolean isSkipNull, String placeHolderForNull) {
        if (list != null) {
            Object[] obj = list.toArray();
            return joinStr(obj, connectChar, isSkipNull, placeHolderForNull);
        } else {
            return null;
        }
    }

    /**
     * 返回后缀,包含.,若不存在后缀返回空字符串
     *
     * @param bmpPath
     * @return
     */
    public static String getFileSuffix(String bmpPath) {
        if (bmpPath == null) {
            return "";
        } else {
            int index = bmpPath.lastIndexOf('.');
            if (index != -1) {
                return bmpPath.substring(index);
            } else {
                return "";
            }
        }
    }
}
