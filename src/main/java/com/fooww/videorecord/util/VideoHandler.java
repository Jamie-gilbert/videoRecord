package com.fooww.videorecord.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import com.lansosdk.videoeditor.VideoEditor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ggg
 * @version 1.0
 * @date 2018/9/27 17:53
 * @description 视频处理类
 */
public class VideoHandler {

    public static final String MUSIC_PATH = Environment.getExternalStorageDirectory().getPath() + File
            .separator + "FoowwVideo" + File.separator + "bgm";
    /**
     * 视频处理
     */
    public static final int TYPE_CONVERT = 1;
    /**
     * 视频加bgm
     */
    public static final int TYPE_BGM = 2;
    /**
     * 视频加水印
     */
    public static final int TYPE_LOGO = 3;
    /**
     * 父路径
     */
    private String parentPath;
    /**
     * 水印路径
     */
    private String logoPath;
    /**
     * 加完bgm路径
     */
    private String mixMusicPath;

    /**
     * 加完水印路径
     */
    private String mixLogoPath;
    /**
     * 压缩路径
     */
    private String compressPath;
    /**
     * 视频处理路径
     */
    private String convertPath;
    /**
     * 移动之后的路径
     */
    private String moveTempPath;
    /**
     * 处理视频回调
     */
    private OnHandleVideoListener listener;

    public String getLogoPath() {
        return logoPath;
    }

    public String getMixMusicPath() {
        return mixMusicPath;
    }

    public String getMixLogoPath() {
        return mixLogoPath;
    }

    public String getCompressPath() {
        return compressPath;
    }

    public String getConvertPath() {
        return convertPath;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getMoveTempPath() {
        return moveTempPath;
    }

    /**
     * 视频处理底层类
     */
    private VideoEditor videoEditor;

    public VideoHandler(OnHandleVideoListener listener) {
        this.listener = listener;
        videoEditor = new VideoEditor();
        parentPath = Environment.getExternalStorageDirectory().getPath() + File
                .separator + "FoowwVideo" + File.separator + String.valueOf(System.currentTimeMillis());
        File file = new File(parentPath);
        if (!file.exists()) file.mkdirs();
        initPath(parentPath);
    }

    /**
     * 初始化路径
     *
     * @param parentPath
     */
    private void initPath(String parentPath) {
        logoPath = parentPath + File.separator + "logo.png";
        mixMusicPath = parentPath + File.separator + "mixMusic.mp4";
        mixLogoPath = parentPath + File.separator + "mixLogo.mp4";
        compressPath = parentPath + File.separator + "compress.mp4";
        convertPath = parentPath + File.separator + "convert.mp4";
        moveTempPath = parentPath + File.separator + "moveTemp.mp4";

    }

    /**
     * 移动并重命名
     *
     * @param src  原文件路径
     * @param dest 目标文件路径
     * @return 是否成功
     */
    public static boolean moveFile(String src, String dest) {
        boolean result = false;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(src);
            String folderPath = dest.substring(0, dest.lastIndexOf('/') + 1);
            File folder = new File(folderPath);
            if (!folder.exists() && !folder.mkdirs()) {
                return false;
            }
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(src);
                FileOutputStream fs = new FileOutputStream(dest);
                byte[] buffer = new byte[inStream.available()];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                result = true;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();

        }
        return result;
    }

    /**
     * 获取旋转值
     *
     * @param angle       旋转角度
     * @param cameraState 前后摄像头
     * @return 旋转值
     */
    private String getTranspose(String angle, int cameraState) {
        switch (angle) {
            case "0":
                switch (cameraState) {
                    case 1:
                        return "transpose=1";
                    case 2:
                        return "transpose=2";
                }
                return "transpose=1";
            case "90":
                return "vflip,hflip";
            case "270":
                return "";
        }
        return null;
    }

    /**
     * 编译视频
     *
     * @return
     */
    @SuppressLint("CheckResult")
    public void parseVideo(String path, String angle, int cameraState,String bitrate) {
        Single.create((SingleOnSubscribe<String>) e -> {
            convertPath = videoEditor.executeConcatMP4(new String[]{path}, convertPath);
            compressPath = videoEditor.executeRotateAngle(convertPath, getTranspose(angle, cameraState), compressPath,bitrate);
            e.onSuccess(compressPath);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (TextUtils.isEmpty(s)) {
                        listener.onError();
                    } else {
                        listener.videoComposed(s, TYPE_CONVERT);
                    }
                }, throwable -> listener.onError());
    }

    /**
     * 合成图片到视频里
     */
    @SuppressLint("CheckResult")
    public void mergeImage(String path, RelativeLayout rl_tuya, int videoWidth, int videoHeight) {
        //得到涂鸦view的bitmap图片
        Bitmap bitmap = Bitmap.createBitmap(rl_tuya.getWidth(), rl_tuya.getHeight(), Bitmap.Config.ARGB_8888);
        rl_tuya.draw(new Canvas(bitmap));
        //这步是根据视频尺寸来调整图片宽高,和视频保持一致
        Matrix matrix = new Matrix();
        matrix.postScale(videoWidth * 1f / bitmap.getWidth(), videoHeight * 1f / bitmap.getHeight());
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Bitmap finalBitmap = bitmap;
        Single.create((SingleOnSubscribe<String>) e -> {
            File file = new File(logoPath);//将要保存图片的路径
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
            mixLogoPath = videoEditor.executeCropOverlay(path, file.getAbsolutePath(), mixLogoPath);
            e.onSuccess(mixLogoPath);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (TextUtils.isEmpty(s)) {
                        listener.onError();
                    } else {
                        listener.videoComposed(s, TYPE_LOGO);
                    }
                }, throwable -> listener.onError());
    }

    /**
     * 加bgm
     *
     * @param video
     * @param musicUrl
     */
    @SuppressLint("CheckResult")
    public void mixAudioWithVideo(String video, final String musicUrl) {
        Single.create((SingleOnSubscribe<String>) e -> {
            mixMusicPath = videoEditor.executeVideoMergeAudio(video, musicUrl, 0.5f, 0.5f, mixMusicPath);
            e.onSuccess(mixMusicPath);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (TextUtils.isEmpty(s)) {
                        listener.onError();
                    } else {
                        listener.videoComposed(s, TYPE_BGM);
                    }
                }, throwable -> listener.onError());

    }


    /**
     * 删除文件夹下所有文件, 只保留一个
     *
     * @param fileNames 保留的文件名称
     */
    public void deleteDirRoom(File dir, List<String> fileNames) {

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                deleteDirRoom(f, fileNames);
            }
        } else if (dir.exists()) {
            if (fileNames == null) {
                dir.delete();
            } else {
                if (!fileNames.contains(dir.getAbsolutePath())) {
                    dir.delete();
                }
            }
        }
    }

    public interface OnHandleVideoListener {


        /**
         * 视频合成完成
         *
         * @param outUrl
         */
        void videoComposed(String outUrl, int type);

        /**
         * 处理失败
         */
        void onError();
    }

}
