package com.lansosdk.videoeditor;

import android.text.TextUtils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * 各种文件操作.

 *
 */
public class LanSongFileUtil {

    public static final String TAG = "LanSongFileUtil";
    public static final boolean VERBOSE = false;

    //可以修改这个路径;
    public static  String TMP_DIR = "/sdcard/lansongBox/";

    public static String getPath() {
        File file = new File(TMP_DIR);
        if (!file.exists() ) {
            file.mkdir();
        }
        return TMP_DIR;
    }


    /**
     * 在指定的文件夹里创建一个文件名字, 名字是当前时间,指定后缀.
     *
     * @return
     */
    public static String createFile(String dir, String suffix) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int second = c.get(Calendar.SECOND);
        int millisecond = c.get(Calendar.MILLISECOND);
        year = year - 2000;
        String name = dir;
        File d = new File(name);

        // 如果目录不中存在，创建这个目录
        if (!d.exists())
            d.mkdir();
        name += "/";
        name += String.valueOf(year);
        name += String.valueOf(month);
        name += String.valueOf(day);
        name += String.valueOf(hour);
        name += String.valueOf(minute);
        name += String.valueOf(second);
        name += String.valueOf(millisecond);
        if (!suffix.startsWith(".")) {
            name += ".";
        }
        name += suffix;

        try {
            Thread.sleep(1);  //保持文件名的唯一性.
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        File file = new File(name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    /**
     * 在box目录下生成一个指定后缀名的文件,并返回名字的路径.这里仅仅创建一个名字.
     *
     * @param suffix 指定的后缀名.
     * @return
     */
    public static String createFileInBox(String suffix) {
        return createFile(TMP_DIR, suffix);
    }


    public static String getParent(String path) {
        if (TextUtils.equals("/", path))
            return path;
        String parentPath = path;
        if (parentPath.endsWith("/"))
            parentPath = parentPath.substring(0, parentPath.length() - 1);
        int index = parentPath.lastIndexOf('/');
        if (index > 0) {
            parentPath = parentPath.substring(0, index);
        } else if (index == 0)
            parentPath = "/";
        return parentPath;
    }

    public static boolean fileExist(String absolutePath) {
        if (absolutePath == null)
            return false;
        else {
            File file = new File(absolutePath);
            if (file.exists())
                return true;
        }
        return false;
    }

    public static boolean filesExist(String[] fileArray) {

        for (String file : fileArray) {
            if (!fileExist(file))
                return false;
        }
        return true;
    }

    public static boolean close(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
            }
        return false;
    }


}
