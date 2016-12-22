package com.taro.base.utils;

import android.graphics.Point;
import android.support.annotation.NonNull;

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
     * 解析图片路径后的分辨率,默认格式为 _widthxheight
     *
     * @param outPoint 用于输出width及height的对象
     * @param baseSize
     * @param url      图片路径, ...._200x200.jpg 类型
     */
    public static void getImageWidthHeightFromResolution(@NonNull Point outPoint, int baseSize, String url) {
        if (url == null || !url.contains("x")) {
            outPoint.set(0, 0);
            return;
        }
        String resolution = url.substring(url.lastIndexOf('_') + 1, url.lastIndexOf('.'));
        String[] params = resolution.split("x");
        float width = Float.valueOf(params[0]);
        float height = Float.valueOf(params[1]);
        float value = width / height;
        if (value > 1) {
            //TODO:根据需要再去将宽高及其比例计算出相关的数据
            //宽大于高
//            width = baseSize * 2;
//            height = width / value;
        } else {
//            height = baseSize * 2;
//            width = height * value;
        }
        outPoint.set((int) width, (int) height);
    }

    /**
     * 返回后缀,包含.,若不存在后缀返回空字符串
     *
     * @param bmpPath
     * @return
     */
    public static String getBmpSuffix(String bmpPath) {
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
