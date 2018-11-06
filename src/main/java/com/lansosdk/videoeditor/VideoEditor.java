package com.lansosdk.videoeditor;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.lansosdk.videoeditor.LanSongFileUtil.fileExist;
/**
 * 最简单的调用方法:
 * VideoEditor veditor=new VideoEditor();
 * veditor.executeXXXXX();  //阻塞执行, 要放到AsyncTask或Thread中执行;
 * <p>

 */
public class VideoEditor {

    private static LanSongLogCollector lanSongLogCollector = null;

    /**
     * 使能在ffmpeg执行的时候, 收集错误信息;
     *
     * @param ctx
     */
    public static void logEnable(Context ctx) {

        if (ctx != null) {
            lanSongLogCollector = new LanSongLogCollector(ctx);
        } else {
            if (lanSongLogCollector != null && lanSongLogCollector.isRunning()) {
                lanSongLogCollector.stop();
                lanSongLogCollector = null;
            }
        }
    }


    /**
     * 构造方法.
     * 如果您想扩展ffmpeg的命令, 可以继承这个类,然后在其中像我们的各种executeXXX的举例一样来拼接ffmpeg的命令;不要直接修改我们的这个文件, 以方便以后的sdk更新升级.
     */

    public VideoEditor() {

    }

    /**
     * 异步线程执行的代码.
     */
    public int executeVideoEditor(String[] array) {
        return execute(array);
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private void postEventFromNative(int what, int arg1, int arg2) {

    }


    /**
     * 获取当前版本号
     *
     * @return
     */
    public static native String getSDKVersion();


    /**
     * 获取用户使用sdk的版本类型
     *
     * @return
     */
    public static native int getLanSongSDKType();

    /**
     * 执行成功,返回0, 失败返回错误码.
     * <p>
     * 解析参数失败 返回1
     * sdk未授权 -1；
     * 解码器错误：69
     * 收到线程的中断信号：255
     * 如硬件编码器错误，则返回：26625---26630
     *
     * @param cmdArray ffmpeg命令的字符串数组, 可参考此文件中的各种方法举例来编写.
     * @return 执行成功, 返回0, 失败返回错误码.
     */
    private native int execute(Object cmdArray);

    /**
     * 新增 在执行过程中取消的方法.
     * 如果在执行中调用了这个方法, 则会直接终止当前的操作.
     * 此方法仅仅是在ffmpeg线程中设置一个标志位,当前这一帧处理完毕后, 会检测到这个标志位,从而退出.
     * 因为execute是阻塞执行, 你可以判断execute有没有执行完,来判断是否完成.
     */
    public native void cancel();


    /**
     * 把一个音乐合并到视频中;
     *
     * @param video   原视频
     * @param audio   要合并的音频
     * @param volume1 在合并的时候, 原视频中的音频音量, 如果为0,则删除原有的视频声音;
     * @param volume2 合并时的, 音频音量;
     * @return 合并后的返回目标视频;
     */
    public String executeVideoMergeAudio(String video, String audio, float volume1, float volume2, String retPath) {

        if (volume2 <= 0) {
            return video;
        }

        MediaInfo vInfo = new MediaInfo(video);
        MediaInfo aInfo = new MediaInfo(audio);

        if (vInfo.prepare() && aInfo.prepare() && aInfo.isHaveAudio()) {
            boolean isAAC = "aac".equals(aInfo.aCodecName);

            List<String> cmdList = new ArrayList<>();
            cmdList.add("-i");
            cmdList.add(video);

            cmdList.add("-i");
            cmdList.add(audio);

            cmdList.add("-t");
            cmdList.add(String.valueOf(vInfo.vDuration));

            if (volume1 > 0 && vInfo.isHaveAudio()) {//两个声音混合;
                String filter = String.format(Locale.getDefault(), "[0:a]volume=volume=%f[a1]; " +
                        "[1:a]volume=volume=%f[a2]; " +
                        "[a1][a2]amix=inputs=2:duration=first:dropout_transition=2", volume1, volume2);

                cmdList.add("-filter_complex");
                cmdList.add(filter);

                cmdList.add("-vcodec");
                cmdList.add("copy");
                cmdList.add("-threads");
                cmdList.add("15");
                cmdList.add("-acodec");
                cmdList.add("libfaac");

                cmdList.add("-ac");
                cmdList.add("2");

                cmdList.add("-ar");
                cmdList.add("44100");

                cmdList.add("-b:a");
                cmdList.add("128000");
            } else if (isAAC && volume2 == 1.0f) {  //删去视频的原音,直接增加音频

                cmdList.add("-map");
                cmdList.add("0:v");

                cmdList.add("-map");
                cmdList.add("1:a");

                cmdList.add("-vcodec");
                cmdList.add("copy");
                cmdList.add("-threads");
                cmdList.add("15");
                cmdList.add("-acodec");
                cmdList.add("copy");

                cmdList.add("-absf");
                cmdList.add("aac_adtstoasc");

            } else { //删去视频的原音,并对音频编码
                cmdList.add("-map");
                cmdList.add("0:v");

                cmdList.add("-map");
                cmdList.add("1:a");

                String filter = String.format(Locale.getDefault(), "volume=%f", volume2);
                cmdList.add("-af");
                cmdList.add(filter);

                cmdList.add("-vcodec");
                cmdList.add("copy");
                cmdList.add("-threads");
                cmdList.add("15");
                cmdList.add("-acodec");
                cmdList.add("libfaac");

                cmdList.add("-ac");
                cmdList.add("2");

                cmdList.add("-ar");
                cmdList.add("44100");

                cmdList.add("-b:a");
                cmdList.add("128000");
            }


            cmdList.add("-y");
            cmdList.add(retPath);
            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] =  cmdList.get(i);
            }
            VideoEditor editor = new VideoEditor();
            int ret = editor.executeVideoEditor(command);
            if (ret == 0) {
                return retPath;
            } else {
                return video;
            }
        }
        return video;
    }


    protected String executeConvertMp4toTs(String mp4Path) {
        if (fileExist(mp4Path)) {

            List<String> cmdList = new ArrayList<>();

            String dstTs = LanSongFileUtil.createFileInBox("ts");
            cmdList.add("-i");
            cmdList.add(mp4Path);

            cmdList.add("-c");
            cmdList.add("copy");

            cmdList.add("-bsf:v");
            cmdList.add("h264_mp4toannexb");

            cmdList.add("-f");
            cmdList.add("mpegts");

            cmdList.add("-y");
            cmdList.add(dstTs);
            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] =  cmdList.get(i);
            }
            int ret = executeVideoEditor(command);
            if (ret == 0) {
                return dstTs;
            } else {
                return null;
            }
        }
        return null;
    }

    protected String executeConvertTsToMp4(String[] tsArray, String output) {
        if (LanSongFileUtil.filesExist(tsArray)) {

            String concat = "concat:";
            for (int i = 0; i < tsArray.length - 1; i++) {
                concat += tsArray[i];
                concat += "|";
            }
            concat += tsArray[tsArray.length - 1];

            List<String> cmdList = new ArrayList<String>();

            cmdList.add("-i");
            cmdList.add(concat);

            cmdList.add("-c");
            cmdList.add("copy");

            cmdList.add("-bsf:a");
            cmdList.add("aac_adtstoasc");

            cmdList.add("-y");

            cmdList.add(output);
            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] =  cmdList.get(i);
            }
            int ret = executeVideoEditor(command);
            if (ret == 0) {
                return output;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 把分段录制的视频, 拼接在一起;
     * <p>
     * 注意:此方法仅仅使用在分段录制的场合
     * 注意:此方法仅仅使用在分段录制的场合
     * 注意:此方法仅仅使用在分段录制的场合
     *
     * @param mp4Array
     */
    public String executeConcatMP4(String[] mp4Array, String output) {

        //第一步,先把所有的mp4转换为ts流
        ArrayList<String> tsPathArray = new ArrayList<String>();
        for (int i = 0; i < mp4Array.length; i++) {
            String segTs1 = executeConvertMp4toTs(mp4Array[i]);
            tsPathArray.add(segTs1);
        }

        //第二步: 把ts流拼接成mp4
        String[] tsPaths = new String[tsPathArray.size()];
        for (int i = 0; i < tsPathArray.size(); i++) {
            tsPaths[i] =  tsPathArray.get(i);
        }
        String dstVideo = executeConvertTsToMp4(tsPaths, output);
        return dstVideo;
    }


    /**
     * <p>
     * 添加水印
     *
     * @param videoFile 原视频
     * @param pngPath
     * @return
     */
    public String executeCropOverlay(String videoFile, String pngPath, String output) {
        if (fileExist(videoFile)) {
            List<String> cmdList = new ArrayList<>();
            cmdList.add("-i");
            cmdList.add(videoFile);
            cmdList.add("-i");
            cmdList.add(pngPath);

            cmdList.add("-filter_complex");
            cmdList.add(" overlay=20:35");
            cmdList.add("-vcodec");
            cmdList.add("libx264");
            cmdList.add("-profile:v");
            cmdList.add("baseline");
            cmdList.add("-preset");
            cmdList.add("ultrafast");
            cmdList.add("-threads");
            cmdList.add("15");
            cmdList.add("-acodec");
            cmdList.add("copy");
            cmdList.add("-strict");
            cmdList.add("-2");
            cmdList.add(output);

            if (executeVideoEditor(cmdList.toArray(new String[cmdList.size()])) == 0) {
                return output;
            }

        }
        return null;
    }

    /**
     *  给视频旋转角度
     * @param srcPath 视频路径
     * @param transpose 旋转值
     * @param ouput 输出路径
     * @param bitrate 压缩之后的比特率
     * @return 输出路径
     */
    public String executeRotateAngle(String srcPath, String transpose, String ouput,String bitrate) {
        if (TextUtils.isEmpty(srcPath)) {
            return null;
        }
        List<String> cmdList = new ArrayList<>();

        cmdList.add("-hwaccel");
        cmdList.add("auto");
        cmdList.add("-i");
        cmdList.add(srcPath);
        if (!TextUtils.isEmpty(transpose)) {
            cmdList.add("-vf");
            cmdList.add(transpose);
        }
        cmdList.add("-vcodec");
        cmdList.add("libx264");
        cmdList.add("-b");
        cmdList.add(bitrate);
//        cmdList.add("-crf");
//        cmdList.add("35");
//        cmdList.add("-s");
//        cmdList.add("960*540");
        cmdList.add("-preset");
        cmdList.add("ultrafast");
        cmdList.add("-threads");
        cmdList.add("15");
        cmdList.add("-acodec");
        cmdList.add("copy");
//        cmdList.add("-metadata:s:v:0");
//        cmdList.add("rotate=90");
        cmdList.add("-strict");
        cmdList.add("-2");
        cmdList.add(ouput);

        if (executeVideoEditor(cmdList.toArray(new String[cmdList.size()])) == 0) {
            return ouput;
        }
        return null;

    }


}
