package com.taro.base.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by taro on 2017/2/20.
 */

public class FileUtils {
    /**
     * 获取需要加载的url路径,该url来自网络请求里,可能需要进行某些处理
     *
     * @param url
     * @return
     */
    public static String getLoadUrl(String url) {
        if (url != null && url.startsWith("http")) {
            return url;
        }
        return url;
    }

    public static boolean isNetworkUrl(String url) {
        return url != null && url.startsWith("http");
    }

    public static String getPhotoCacheDir(String cacheTag) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + ((cacheTag == null || cacheTag.length() == 0) ? "cache" : cacheTag);
    }

    public static String getDownloadCacheDir(String cacheTag) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + ((cacheTag == null || cacheTag.length() == 0) ? "cache" : cacheTag);
    }

    public static String getFileCacheDir(String cacheTag) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + ((cacheTag == null || cacheTag.length() == 0) ? "cache" : cacheTag) + "/file";
    }


    /**
     * 获取文件的后缀及其文件名,后缀不包括.,文件名不包括/,str[0]=文件名;str[1]=后缀名;<br>
     * 出错或者路径不对时返回null,否则返回的string[]两个值不存在时某个为null
     *
     * @param filePath 文件路径
     * @return 默认或者不存在时返回null,
     */
    public static String[] getLastNameWithSuffix(String filePath) {
        if (filePath == null || filePath.length() <= 0) {
            return null;
        } else {
            String suffix = null, lastName = null;
            int beginIndex = filePath.lastIndexOf('/');
            int endIndex = filePath.lastIndexOf('.');
            //存在/的符号
            if (beginIndex != -1) {
                //存在.符号,说明可能存在后缀名
                if (endIndex != -1) {
                    //.在/后面,并且都存在此符号
                    if (beginIndex < endIndex) {
                        //.符号不是路径的最后一个符号(也就是说路径不是以.为结束的),肯定有后缀的,是不是合法就不确定的
                        if (endIndex + 1 < filePath.length()) {
                            suffix = filePath.substring(endIndex + 1);
                            lastName = filePath.substring(beginIndex + 1, endIndex);
                        } else {
                            //以.为路径结尾,无法确定后缀是什么
                            lastName = filePath.substring(beginIndex + 1);
                        }
                    }
                } else {
                    //否则说明只有/符号没有后缀
                    lastName = filePath.substring(beginIndex + 1);
                }
            } else {
                //两个符号都没有
                return null;
            }
            if (lastName == null && suffix == null) {
                return null;
            } else {
                return new String[]{lastName, suffix};
            }
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


    /**
     * 复制文件到其它地方
     *
     * @param oldPath     文件旧地址
     * @param path        新的文件地址
     * @param newFileName 重命名的文件名
     * @return
     */
    public static String copyFile(@NonNull String oldPath, @NonNull String path, @Nullable String newFileName) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            return null;
        }

        //创建文件夹
        File director = new File(path);
        if (!director.exists()) {
            director.mkdirs();
        }
        if (newFileName == null || newFileName.length() <= 0) {
            newFileName = oldFile.getName();
        }

        File newFile = new File(director, newFileName);
        if (newFile.exists()) {
            newFile.delete();
        }

        FileInputStream in = null;
        FileOutputStream out = null;

        //将文件转存到缓存文件夹下
        byte[] buffer = new byte[1024];
        try {
            in = new FileInputStream(oldFile);
            out = new FileOutputStream(newFile);

            BufferedInputStream bufferIn = new BufferedInputStream(in);
            BufferedOutputStream bufferOut = new BufferedOutputStream(out);

            int read = 0;
            while (bufferIn.available() > 0) {
                read = bufferIn.read(buffer);
                if (read != -1) {
                    bufferOut.write(buffer, 0, read);
                }
            }

            return newFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 通知系统数据库更新扫描文件
     *
     * @param context
     * @param filePath
     */
    public static void notifySystemScanFile(@NonNull Context context, String filePath) {
        //方法是有效的,但是可能路径会导致扫描失败.
        //系统扫描的前提是文件的路径为 /mnt/sdcard/ 开头的路径,实际的路径可能只是 /sdcard/xxx
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));

        MediaScannerConnection.scanFile(context,
                new String[]{filePath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("MediaScanWork", "file " + path
                                + " was scanned seccessfully: " + uri);
                    }
                });
    }

    public static void downloadBmp(final String url, final String outPath, final String name, final OnDownloadFinishedListener listener) {
        String[] result = getLastNameWithSuffix(url);
        String fileName, suffix;
        if (result != null && result[0] != null && result[1] != null) {
            fileName = result[0];
            suffix = result[1];
        } else {
            fileName = String.valueOf(System.currentTimeMillis());
            //默认使用JPG
            suffix = "jpg";
        }
        if (name != null) {
            fileName = name;
        }

        final String finalFileName = fileName;
        final String finalSuffix = suffix;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL httpUrl = new URL(url);
                    connection = (HttpURLConnection) httpUrl.openConnection();
                    connection.setDoInput(true);
                    //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.connect();

                    if (connection.getResponseCode() != 200) {
                        return;
                    }
                    InputStream in = null;

                    //创建文件夹
                    File director = new File(outPath);
                    if (!director.exists()) {
                        director.mkdirs();
                    }

                    String fileName = finalFileName.concat(".").concat(finalSuffix);
                    File newFile = new File(director, fileName);
                    if (newFile.exists()) {
                        newFile.delete();
                    }

                    FileOutputStream out = null;
                    //将文件转存到缓存文件夹下
                    try {
                        in = connection.getInputStream();
                        out = new FileOutputStream(newFile);

                        Bitmap bmp = BitmapFactory.decodeStream(in);
                        if (bmp != null) {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            bmp.recycle();
                        }
                        if (listener != null) {
                            listener.onDownloadFinished(newFile, url, finalFileName, finalSuffix);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onDownloadFail(url);
                        }
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onDownloadFail(url);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

//    public static void downloadFileFromGlide(Context context, final String url, final OnDownloadFinishedListener listener) {
//        if (context != null && listener != null) {
//            final Context baseContext = context.getApplicationContext();
//            String[] result = getLastNameWithSuffix(url);
//            final String fileName, suffix;
//            if (result != null) {
//                fileName = result[0];
//                suffix = result[1];
//            } else {
//                fileName = null;
//                suffix = null;
//            }
//
//            AsyncTask<String, Void, File> task = new AsyncTask<String, Void, File>() {
//                protected File doInBackground(String... params) {
//                    try {
//                        return Glide
//                                .with(baseContext)
//                                .load(url)
//                                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                                .get() // needs to be called on background thread
//                                ;
//                    } catch (Exception ex) {
//                        return null;
//                    }
//                }
//
//                @Override
//                protected void onPostExecute(File result) {
//                    if (result == null) {
//                        listener.onDownloadFail(url);
//                    } else {
//                        listener.onDownloadFinished(result, url, fileName, suffix);
//                    }
//                }
//            };
//            task.execute(url);
//        }
//    }

    public interface OnDownloadFinishedListener {
        public void onDownloadFinished(File file, String url, String fileName, String suffix);

        public void onDownloadFail(String url);
    }

    /**
     * 获取系统的所有图片
     *
     * @param context
     * @return
     */
    public static List<SysPhoto> getSystemPhotos(@NonNull Context context) {
        List<SysPhoto> photoList = null;
        String[] pro = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            photoList = new ArrayList<>(cursor.getCount());
            int i = 0;
            while (cursor.moveToNext()) {
                //获取图片的名称
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                //获取图片的生成日期
                byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                //获取图片的详细信息
                String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                SysPhoto photo = new SysPhoto();
                photo.mPosition = i++;
                photo.mPath = path;
                photo.mDesc = desc;
                photo.mName = name;
                photoList.add(photo);
            }
            cursor.close();
        }
        return photoList;
    }

    public static class SysPhoto {
        public String mPath;
        public int mPosition;
        public String mDesc;
        public String mName;
    }

}
