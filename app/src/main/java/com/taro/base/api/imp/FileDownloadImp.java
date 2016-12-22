package com.taro.base.api.imp;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.taro.base.api.base.ApiManager;
import com.taro.base.base.BaseApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by taro on 16/11/14.
 */
public class FileDownloadImp {
    private String DOWNLOAD_PATH = null;

    /**
     * 获取默认的文件存放路径,当存在SD卡时存放到SD卡中的 cache 文件夹下;不存在SD卡时放到程序的目录下.
     *
     * @return
     */
    public String getDefaultDownloadPath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //SD卡已经挂载
            DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/cache/" + BaseApp.getContext().getPackageName();
        } else {
            //未挂载,下载到包目录下的文件夹
            DOWNLOAD_PATH = BaseApp.getContext().getFilesDir()
                    + "/cache/" + BaseApp.getContext().getPackageName();
        }
        Log.e("file-download", DOWNLOAD_PATH);
        return DOWNLOAD_PATH;
    }

    private IFileDownloadApi mApi = null;

    public FileDownloadImp() {
        mApi = ApiManager.getInstance().createService(IFileDownloadApi.class);
    }

    /**
     * 下载小型文件
     *
     * @param fileUrl        文件下载URL
     * @param downloadPath   文件下载存放的路径
     * @param renameFileName 重命名的文件名
     * @return
     */
    public Observable<String> downloadSmallFile(@NonNull String fileUrl, @NonNull final String downloadPath, @Nullable final String renameFileName) {
        final String fileName = getFileName(fileUrl, renameFileName);
        if (fileName == null) {
            return Observable.just("文件路径未存在文件名")
                    .doOnNext(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            throw new RuntimeException(s);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            Observable<String> obser = mApi.downloadSmallFile(fileUrl)
                    .map(new Func1<ResponseBody, String>() {
                        @Override
                        public String call(ResponseBody responseBody) {
                            if (responseBody != null) {
                                boolean isSave = writeResponseBodyToDisk(downloadPath, fileName, responseBody);
                                if (isSave) {
                                    return downloadPath + "/" + fileName;
                                }
                            }
                            throw new RuntimeException("无法下载文件");
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            return obser;
        }
    }

    /**
     * 下载小型文件,文件下载存放的位置将使用 {@link #getDefaultDownloadPath()} 默认的存放路径
     *
     * @param fileUrl        文件下载URL
     * @param renameFileName 重命名的文件名,若URL中存在后缀则可不带后缀;否则只会重命名不会添加任何后缀
     * @return
     */
    public Observable<String> downloadSmallFile(@NonNull String fileUrl, @Nullable String renameFileName) {
        return downloadSmallFile(fileUrl, getDefaultDownloadPath(), renameFileName);
    }

    /**
     * 从存在的URL中获取文件名
     *
     * @param fileUrl        下载的url文件
     * @param renameFileName 重新命名的文件名,可不带后缀;默认使用url中的后缀进行补充,url不存在后缀时仅重命名不作任何处理
     * @return
     */
    public static String getFileName(@NonNull String fileUrl, @Nullable String renameFileName) {
        if (renameFileName == null || renameFileName.length() <= 0) {
            int index = fileUrl.lastIndexOf('/');
            if (index == -1) {
                return null;
            }
            renameFileName = fileUrl.substring(index);
        } else {
            int dotIndex = fileUrl.lastIndexOf('.');
            if (dotIndex != -1) {
                renameFileName += fileUrl.substring(dotIndex);
            }
        }
        return renameFileName;
    }

    //将Response数据写到磁盘中
    private boolean writeResponseBodyToDisk(String path, String fileName, ResponseBody body) {
        if (path != null && path.length() > 0 && fileName != null && fileName.length() > 0) {
            try {
                File pathFile = new File(path);
                pathFile.mkdirs();
                File fileNameFile = new File(pathFile, fileName);
                if (fileNameFile.exists()) {
                    fileNameFile.delete();
                }
                fileNameFile.createNewFile();

                return writeResponseBodyToDisk(fileNameFile, body);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    //将response数据写到磁盘中
    private boolean writeResponseBodyToDisk(File pathWithFileName, ResponseBody body) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(pathWithFileName);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.e("download", "file path: " + pathWithFileName.getAbsolutePath());
                    Log.d("download", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 文件下载统一接口
     * Created by taro on 16/11/14.
     */
    public static interface IFileDownloadApi {
        /**
         * 下载小型文件
         *
         * @param fileUrl 文件路径
         * @return
         */
        @GET
        public Observable<ResponseBody> downloadSmallFile(@Url String fileUrl);

        @Streaming
        @GET
        Call<ResponseBody> downloadBigFile(@Url String fileUrl);
    }
}
