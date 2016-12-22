package com.taro.base.api.imp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Base64;

import com.taro.base.api.base.ApiException;
import com.taro.base.base.BaseApp;
import com.taro.base.utils.EncryptUtil;
import com.taro.base.utils.GalleryAndPhotoUtils;
import com.taro.base.utils.StringUtil;
import com.taro.base.utils.UUIDGenerator;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by taro on 16/12/23.
 */

public class ImageQiNiuImp {
    //TODO:需要根据七牛的设置进行修改
    public static final String QINIU_ACCESS_KEY = "GKjmVobHqSs3IWASB12fUpxcmcEp1Lw5Cv2Uu82h";
    public static final String QINIU_SECRET_KEY = "-L4Ssulonj_gltRUThDuA6yiPr9fZKUFnLFpIvLi";
    public static final String QINIU_SCOPE = "daxiang";

    public static final String QINIU_UPLOAD_URL = "http://up-z2.qiniu.com/";
    public static final String QINIU_DOWNLOAD_URL = "http://ohjdda8lm.bkt.clouddn.com/";

    /**
     * 批量一次性上传图片,返回上传后的图片路径数组
     *
     * @param context
     * @param quality 图片质量
     * @param maxSize 图片最大的长宽尺寸
     * @param bmpFile 图片完整路径
     * @return
     */
    public static Observable<String[]> compreWithUploadMultiImages(@NonNull Context context, final int quality, final int maxSize, @NonNull final String prefix, String... bmpFile) {
        if (bmpFile == null || bmpFile.length <= 0) {
            return null;
        } else {
            final Context appContext = context.getApplicationContext();
            Observable<Object> obser = Observable.from(bmpFile)
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            return s != null && s.length() > 0;
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String filePath) {
                            return compressImage2JPEG(appContext, filePath, quality, maxSize);
                        }
                    })
                    .flatMap(new Func1<String, Observable<Object>>() {
                        @Override
                        public Observable<Object> call(String compressPath) {
                            return doImageUpload(prefix, compressPath);
                        }
                    });
            return wrapUploadResultObservable(obser)
                    .reduce(new ArrayList<String>(), new Func2<ArrayList<String>, String, ArrayList<String>>() {
                        @Override
                        public ArrayList<String> call(ArrayList<String> strings, String s) {
                            strings.add(s);
                            return strings;
                        }
                    })
                    .map(new Func1<ArrayList<String>, String[]>() {
                        @Override
                        public String[] call(ArrayList<String> strings) {
                            String[] strArr = new String[strings.size()];
                            for (int i = 0; i < strings.size(); i++) {
                                strArr[i] = strings.get(i);
                            }
                            return strArr;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    /**
     * 压缩并上传图片,请使用此方法进行上传
     *
     * @param quality 图片质量
     * @param maxSize 图片最大的长宽尺寸
     * @param bmpFile 图片完整路径
     * @return
     */
    public static final Observable<String> compressWithUploadImage(@NonNull Context context, int quality, int maxSize, @NonNull String bmpFile, @NonNull final String prefix) {
        BaseApp.showLongToast("图片正在上传");
        Observable<Object> obser = compressImage2JPEG(context, bmpFile, quality, maxSize)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s != null && s.length() >= 0;
                    }
                })
                .flatMap(new Func1<String, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(String s) {
                        return doImageUpload(prefix, s);
                    }
                });
        return wrapUploadResultObservable(obser)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 压缩图片并返回图片压缩后缓存的地址
     *
     * @param bmpFile 图片压缩地址
     * @param quality 图片质量,仅0-100
     * @param maxSize 图片最大边的值,当图片本身小于此值时,压缩将使用原图的大小
     * @return
     */
    public static final Observable<String> compressImage2JPEG(@NonNull Context context, @NonNull final String bmpFile, final int quality, final int maxSize) {
        String cachePath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cachePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        } else {
            cachePath = context.getFilesDir().getAbsolutePath();
        }
        final String outPath = cachePath;
        return Observable.just(bmpFile)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        String cachePath = outPath + "/cache/";
                        String[] suffix = GalleryAndPhotoUtils.getLastNameWithSuffix(bmpFile);
                        if (suffix == null || suffix[0] == null) {
                            return "";
                        } else {
                            File file = new File(cachePath);
                            file.mkdirs();
                            cachePath += suffix[0];
                            Bitmap bmp = GalleryAndPhotoUtils.decodeBitmapWithScaleIfNeed(bmpFile, maxSize, 100 * 1024);
                            if (!GalleryAndPhotoUtils.compressImage2JPEG(bmp, quality, cachePath, true)) {
                                cachePath = "";
                            } else {
                                cachePath = cachePath.concat(".jpg");
                            }
                            return cachePath;
                        }
                    }
                });
    }

    public static Observable<String> wrapUploadResultObservable(Observable<Object> obser) {
        if (obser != null) {
            return obser.map(new Func1<Object, String>() {
                @Override
                public String call(Object t) {
                    if (t != null) {
                        try {
                            String json = t.toString();
                            String keyStr = json.substring(json.indexOf("key") + 4);
                            String filePath = keyStr.substring(0, keyStr.indexOf('}'));
                            if (filePath.length() > 0) {
                                return filePath;
                            } else {
                                throw new ApiException("上传失败!上传参数不正确,未返回上传结果");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new ApiException("上传失败!未获取到返回的信息");
                        }
                    } else {
                        throw new ApiException("上传失败!未获取到返回的信息");
                    }
                }
            });
        } else {
            return null;
        }
    }

    public static Observable<Object> doImageUpload(@NonNull String prefix, String path) {
        String resolution = "";
        BitmapFactory.Options options = GalleryAndPhotoUtils.getOriginalBitmapOptions(path);
        if (options != null) {
            //加上图片的分辨率 |600_300(宽高)|(2)宽高比
            resolution = StringUtil.generateBmpOptionDesc(options.outWidth, options.outHeight);
        }
        String suffix = StringUtil.getBmpSuffix(path);
        String filePath = prefix.concat(UUIDGenerator.makeType1UUID().toString()).concat(resolution).concat(suffix);
        String token = createFileUploadToken(filePath);
        Map<String, Object> paramsMap = new ArrayMap<>(5);
        paramsMap.put("token", token);
        paramsMap.put("key", filePath);
        return new FileUploadImp().uploadSingleFile(QINIU_UPLOAD_URL, paramsMap, path);
    }


    public static String createFileUploadToken(String fileName) {
        try {
            JSONObject json = new JSONObject();
            if (!TextUtils.isEmpty(fileName)) {
                json.put("scope", TextUtils.join(":", new Object[]{QINIU_SCOPE, fileName}));
            } else {
                json.put("scope", QINIU_SCOPE);
            }
            json.put("deadline", System.currentTimeMillis() / 1000 + 3600);
            String result = json.toString();
            System.out.println("json = " + result);
            String encode = Base64.encodeToString(result.getBytes(), Base64.URL_SAFE).trim();
            System.out.println("encode = " + encode);
            String encodeSigned = Base64.encodeToString(EncryptUtil.encryptInHMACSha1(encode, QINIU_SECRET_KEY), Base64.URL_SAFE).trim();
            System.out.println("encodeSigned = " + encodeSigned);
            String token = TextUtils.join(":", new Object[]{QINIU_ACCESS_KEY, encodeSigned, encode});
            System.out.println("token = " + token);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
