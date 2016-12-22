package com.taro.base.api.imp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.taro.base.api.base.ApiException;
import com.taro.base.api.base.ApiManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Url;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by taro on 16/9/12.
 */
public class FileUploadImp {

    public static interface IFileUploadApi {
        @POST
        @Multipart
        public Observable<Object> uploadFile(@Url String url, @PartMap Map<String, RequestBody> paramsBody);
    }

    private static void wrapMultiBodyParamsMap(@NonNull Map<String, RequestBody> bodyMap, Map<String, ? extends Object> paramsMap) {
        if (paramsMap != null && paramsMap.size() > 0) {
            for (Map.Entry<String, ? extends Object> entry : paramsMap.entrySet()) {
                RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(entry.getValue()));
                bodyMap.put(entry.getKey(), body);
            }
        }
    }

    private static Observable<Object> createDefaultErrorObservable(@NonNull final String errorMsg) {
        return Observable.just(new Object())
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        throw new ApiException(errorMsg);
                    }
                });
    }

    public static Map<String, RequestBody> createRequestBodyUploadMultiFile(List<String> fileList, Map<String, ? extends Object> paramsMap) {
        if (fileList == null || fileList.size() <= 0) {
            return null;
        } else {
            Map<String, RequestBody> bodyMap = new ArrayMap<>();

            int index = 0;
            for (int i = 0; i < fileList.size(); i++) {
                String filePath = fileList.get(i);
                File file = new File(filePath);
                if (file.exists()) {
                    RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    String key = String.format("file%d\"; filename=\"%s", index, file.getName());
                    //TODO:多文件一并上传
                    bodyMap.put(key, body);
                    index++;
                }
            }
            //没有任何文件被添加进去,说明文件都不存在
            if (bodyMap.size() <= 0) {
                return null;
            }

            wrapMultiBodyParamsMap(bodyMap, paramsMap);
            return bodyMap;
        }
    }

    public static Map<String, RequestBody> createUploadRequestBodyUploadSingleFile(String filePath, Map<String, ? extends Object> paramsMap) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {
            Map<String, RequestBody> bodyMap = new ArrayMap<>();
            //添加附带参数
            wrapMultiBodyParamsMap(bodyMap, paramsMap);

            File file = new File(filePath);
            if (file.exists()) {
                RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                bodyMap.put(String.format("file\"; filename=\"%s", file.getName()), body);
            } else {
                return null;
            }
            return bodyMap;
        }
    }

    @NonNull
    public static Observable<Object> uploadSingleFile(@NonNull String url, @Nullable Map<String, Object> paramsMap, String filePath) {
        Map<String, RequestBody> uploadBody = createUploadRequestBodyUploadSingleFile(filePath, paramsMap);
        if (uploadBody != null) {
            IFileUploadApi api = ApiManager.getInstance().createService(IFileUploadApi.class);
            return api.uploadFile(url, uploadBody);
        } else {
            return createDefaultErrorObservable("文件路径不存在");
        }
    }

    @NonNull
    public static Observable<Object> uploadMultiFile(@NonNull String url, @Nullable Map<String, Object> paramsMap, List<String> filePath) {
        Map<String, RequestBody> uploadBody = createRequestBodyUploadMultiFile(filePath, paramsMap);
        if (uploadBody != null) {
            IFileUploadApi api = ApiManager.getInstance().createService(IFileUploadApi.class);
            return api.uploadFile(url, uploadBody);
        } else {
            return createDefaultErrorObservable("文件路径不存在");
        }
    }

    @NonNull
    public static Observable<Object> uploadFile(@NonNull String url, @Nullable Map<String, Object> paramsMap, String... filePaths) {
        if (filePaths == null || filePaths.length <= 0) {
            Log.e("file upload", "文件路径不存在");
            return createDefaultErrorObservable("文件路径不存在");
        } else {
            if (filePaths.length == 1) {
                String filePath = filePaths[0];
                return uploadSingleFile(url, paramsMap, filePath);
            } else {
                List<String> fileList = Arrays.asList(filePaths);
                return uploadMultiFile(url, paramsMap, fileList);
            }
        }
    }

}
