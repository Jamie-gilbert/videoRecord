package com.fooww.videorecord.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author ggg
 * @version 1.0
 * @date 2018/10/18 11:08
 * @description 视频的基本信息
 */
public class VideoModel {
    private int width;
    private int height;
    private int roate;
    private int bitRate;
    private int duration;
    private String thumbPath;
    private String videoPath;
    private Context context;
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRoate() {
        return roate;
    }

    public void setRoate(int roate) {
        this.roate = roate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getThumbPath() {

        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
    private String saveThumb(Bitmap bitmap,String parentPath,String name){

        File file=new File(parentPath+File.separator+name);
        if(file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            FileOutputStream outputStream=null;
            try {

                 outputStream=new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG,80,outputStream);
                outputStream.flush();
                thumbPath=file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(outputStream!=null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        return thumbPath;
    }

    public VideoModel(Context context,String videoPath) {
        this.context=context;
        this.videoPath = videoPath;
        File file=new File(videoPath);
        if(file.exists()) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            String rotation = retriever.extractMetadata(retriever.METADATA_KEY_VIDEO_ROTATION);
            String width = retriever.extractMetadata(retriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(retriever.METADATA_KEY_VIDEO_HEIGHT);
            String bitRate = retriever.extractMetadata(retriever.METADATA_KEY_BITRATE);
            String duration = retriever.extractMetadata(retriever.METADATA_KEY_DURATION);
            Bitmap thumb = retriever.getFrameAtTime(1);
            int r = 0;
            int w = 0;
            int h = 0;
            int br = 350000;
            int d = 0;
            try {
                r = Integer.parseInt(rotation);

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                w = Integer.parseInt(width);

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                h = Integer.parseInt(height);

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                br = Integer.parseInt(bitRate) / 8;

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                d = Integer.parseInt(duration) ;

            } catch (Exception e) {
                e.printStackTrace();
            }
            this.width = w;
            this.height = h;
            this.roate = r;
            this.duration = d;
            this.bitRate = br;
            saveThumb(thumb,file.getParentFile().getAbsolutePath(),file.getName().replace(".mp4",".jpg") );
        }
    }

    public VideoModel() {
    }



}
