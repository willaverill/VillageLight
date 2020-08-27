package com.villagelight.app.util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.villagelight.app.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by CharlesLui on 03/11/2017.
 */

public class LogUtils {

    private static String TAG = "LogToFile";

    private static String logPath = "/mnt/sdcard/VillageLight";//log日志存放路径

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);//日期格式;

    private static final char VERBOSE = 'v';

    private static final char DEBUG = 'd';

    private static final char INFO = 'i';

    private static final char WARN = 'w';

    private static final char ERROR = 'e';

    public static void v(String msg) {
        writeToFile(VERBOSE, TAG, msg);
    }

    public static void d(String msg) {

        if (BuildConfig.LOG_DEBUG) {
            Logger.d(msg);
            writeToFile(DEBUG, TAG, msg);
        }

    }

    public static void i(String msg) {
        writeToFile(INFO, TAG, msg);
    }

    public static void w(String msg) {
        writeToFile(WARN, TAG, msg);
    }

    public static void e(String msg) {
        writeToFile(ERROR, TAG, msg);
    }

    /**
     * 将log信息写入文件中
     *
     * @param type
     * @param tag
     * @param msg
     */
    public static void writeToFile(char type, String tag, String msg) {

        if (null == logPath) {
            Log.e(TAG, "logPath == null ，未初始化LogToFile");
            return;
        }

        Date date = new Date();
        String fileName = logPath + "/" + "debug.log";//log日志名，使用时间命名，保证不重复
        String log = dateFormat.format(date) + " -> " + msg + "\n";//log日志内容，可以自行定制

        //如果父路径不存在
        File file = new File(logPath);
        if (!file.exists()) {
            file.mkdirs();//创建父路径
        }

        FileOutputStream fos = null;//FileOutputStream会自动调用底层的close()方法，不用关闭
        BufferedWriter bw = null;
        try {

            fos = new FileOutputStream(fileName, true);//这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(log);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();//关闭缓冲流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void deleteLog() {

        File log = new File(logPath, "debug.log");
        long size = 5 * 1024 * 1024;
        if (log.exists() && log.length() > size) {
            log.delete();
        }

    }

    // 調用系統方法分享文件
    public static void shareFile(Context context, File file) {
        if (null != file && file.exists()) {

            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".fileProvider",
                        file);
            } else {
                uri = Uri.fromFile(file);
            }

            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "发送日志"));
        } else {
            Toast.makeText(context, "日志文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    private static String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }
}
