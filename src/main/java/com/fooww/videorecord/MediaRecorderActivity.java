package com.fooww.videorecord;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.fooww.videorecord.util.BGMHandler;
import com.fooww.videorecord.util.ToastUtils;
import com.fooww.videorecord.util.VideoHandler;
import com.fooww.videorecord.widgets.MyVideoView;
import com.fooww.videorecord.widgets.RecordedButton;
import com.fooww.weixinrecorded.R;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 视频录制
 */
public class MediaRecorderActivity extends BaseActivity implements SurfaceHolder.Callback {
    //录制视频
    private static final int HANDLER_RECORD = 200;
    private SurfaceView svFfmpeg;
    private RecordedButton rbStart;
    private RelativeLayout rlBottom;
    private MyVideoView vvPlay;
    private ImageView ivPhoto;
    private RelativeLayout rlTop;
    private ImageView ivNext;
    private ImageView ivClose;
    private ImageView ivMusic;
    private RelativeLayout rlPlayer;
    private ImageView ivChangeCamera;
    //最大录制时间
    private int maxDuration = 10000;
    //本次段落是否录制完成
    private boolean isRecordedOver;
    private ImageView ivBack;

    //录制视频
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mSurfaceHolder;
    //是否放大
    private boolean isZoomIn = false;
    private Camera mCamera;
    //判断是否正在录制
    private boolean isRecording;
    /**
     * 重力感应回调
     */
    private OrientationEventListener orientationEventListener;
    /**
     * bgm处理类
     */
    private BGMHandler bgmHandler;

    /**
     * 视频处理类
     */
    private VideoHandler videoHandler;
    /**
     * 视频输出路径
     */
    private String output;

    /**
     * 原视频输出路径
     */
    private String orinalPut;
    /**
     * 视频旋转角度
     */
    private String angle = "0";
    private int cameraState = 1;

    //是否是横屏
    private boolean isLandScape;

    private File mTargetFile;
    private GestureDetector mDetector;
    //屏幕分辨率
    private int videoWidth, videoHeight;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean canRecord = true;
    /**
     * 摄像头的bitrate
     */
    private int bitrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        canRecord = true;
        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        orientationEventListener = new OrientationEventListener(this, Sensor.TYPE_GRAVITY) {
            @Override
            public void onOrientationChanged(int orientation) {
                if ((orientation > 45 && orientation < 135)) {
                    isLandScape = true;
                    angle = "90";

                } else if ((orientation > 225 && orientation < 315)) {
                    isLandScape = true;
                    angle = "270";
                } else if ((orientation == 180)) {
                    isLandScape = false;
                    angle = "180";
                } else {
                    angle = "0";
                    isLandScape = false;

                }
            }
        };
        orientationEventListener.enable();

        loadViews();
        initEvent();
    }

    /**
     * 合成视频成功
     *
     * @param result 视频路径
     */
    private void composeFinish(String result) {
        rlPlayer.setVisibility(View.VISIBLE);
        rlBottom.setVisibility(View.VISIBLE);
        vvPlay.setVisibility(View.VISIBLE);
        rlTop.setVisibility(View.GONE);
        output = result;
        vvPlay.setVideoPath(result);
        if (vvPlay.isPrepared()) {
            vvPlay.start();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_RECORD: {
                    //拍摄视频的handler
                    if (!isRecordedOver) {
                        changeButton(false);
                        rbStart.setProgress(rbStart.getCurrentPro() + 50);
                        myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, 50);
                    }
                    orientationEventListener.disable();
                }
                break;
            }
        }
    };

    /**
     * 切换底部按钮
     *
     * @param flag 是否是拍摄完成
     */
    private void changeButton(boolean flag) {

        if (flag) {
            rbStart.setVisibility(View.GONE);
        } else {
            rbStart.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化视频拍摄状态
     */
    private void initMediaRecorderState() {
        vvPlay.setVisibility(View.GONE);
        vvPlay.pause();
        ivPhoto.setVisibility(View.GONE);
        rlTop.setVisibility(View.VISIBLE);
        rbStart.setVisibility(View.VISIBLE);
        rlBottom.setVisibility(View.GONE);
        rlPlayer.setVisibility(View.GONE);
        changeButton(false);
        rbStart.setProgress(0);
        rbStart.cleanSplit();
        bgmHandler.clear();
        ivMusic.setSelected(false);

    }

    /**
     * 开启预览
     *
     * @param holder
     */
    private void startPreView(SurfaceHolder holder) {

        if (mCamera == null) {
            mCamera = Camera.open(mCameraId);
        }

        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(holder);
                Camera.Parameters parameters = mCamera.getParameters();
                //实现Camera自动对焦
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    for (String mode : focusModes) {
                        if (mode.contains("continuous-video")) {
                            parameters.setFocusMode("continuous-video");
                        } else if (mode.contains("auto")) {
                            parameters.setFocusMode("auto");
                        } else if (mode.contains("continuous-picture")) {
                            parameters.setFocusMode("continuous-picture");
                        }
                    }
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void initEvent() {
        rbStart.setOnGestureListener(new RecordedButton.OnGestureListener() {
            @Override
            public void onLongClick() {
                //长按录像
                if (canRecord) {
                    isRecordedOver = false;
                    startRecord();
                    rbStart.setSplit();
                    myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, 50);
                    rlTop.setVisibility(View.GONE);
                    canRecord = false;
                }
            }

            @Override
            public void onClick() {
                canRecord = true;
                ToastUtils.showWarns(MediaRecorderActivity.this, "录制视频的时间不能少于3秒");
            }

            @Override
            public void onLift() {
                isRecordedOver = true;
                if (rbStart.getCurrentPro() < 3000) {
                    orientationEventListener.enable();
                    stopRecordUnSave();
                    ToastUtils.showWarns(MediaRecorderActivity.this, "录制视频的时间不能少于3秒");
                    initMediaRecorderState();
                } else {
                    showProgressDialog();
                    orientationEventListener.disable();
                    videoFinish();
                    canRecord = true;
                }

            }

            @Override
            public void onOver() {
                showProgressDialog();
                orientationEventListener.disable();
                isRecordedOver = true;
                rbStart.closeButton();
                videoFinish();
                canRecord = true;
            }
        });
        ivNext.setOnClickListener(v -> {
            rbStart.setDeleteMode(false);
            showProgressDialog();
            finishRecord();
        });
        ivClose.setOnClickListener(v -> {
            orientationEventListener.enable();
            initMediaRecorderState();
            isLandScape = false;
            angle = "0";
            vvPlay.reset();

        });
        vvPlay.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            vvPlay.start();
        });
        ivChangeCamera.setOnClickListener(v -> switchCamera());
        ivBack.setOnClickListener(v -> onBackPressed());
        ivMusic.setOnClickListener(v -> bgmHandler.show(ivMusic));
        bgmHandler = new BGMHandler(this, vvPlay).initData();
        bgmHandler.setOnBGMSelectedListener(new BGMHandler.OnBGMSelectedListener() {
            @Override
            public void onBGMSelected(String url) {
                if (TextUtils.isEmpty(url)) {
                    ivMusic.setSelected(false);
                } else {
                    ivMusic.setSelected(true);
                }
                showProgressDialog();
                videoHandler.mixAudioWithVideo(orinalPut, url);
            }

            @Override
            public void onClosed() {
                if (vvPlay != null) {
                    vvPlay.setVideoPath(output);
                    if (vvPlay.isPrepared()) {
                        vvPlay.start();
                    }
                }
            }
        });
        videoHandler = new VideoHandler(new VideoHandler.OnHandleVideoListener() {
            @Override
            public void videoComposed(String outUrl, int type) {
                closeProgressDialog();
                if (TextUtils.isEmpty(outUrl)) {
                    ToastUtils.showWarns(MediaRecorderActivity.this, "视频合成失败");
                    ivClose.callOnClick();
                } else {
                    output = outUrl;
                    if (type == VideoHandler.TYPE_CONVERT) {
                        orinalPut = outUrl;
                        composeFinish(output);
                        vvPlay.reset(isLandScape);

                    } else {
                        runOnUiThread(() -> {
                            composeFinish(output);
                            vvPlay.reset(isLandScape);
                        });

                    }

                }
            }

            @Override
            public void onError() {
                closeProgressDialog();
                ToastUtils.showWarns(MediaRecorderActivity.this, getString(R.string.txt_warn_add_music));
            }
        });
    }

    private void finishRecord() {
        closeProgressDialog();
        String thumbPath = vvPlay.obtainVideoInfo(output).getThumbPath();
        List<String> saveFiles = new ArrayList<>();
        saveFiles.add(output);
        saveFiles.add(thumbPath);
        videoHandler.deleteDirRoom(new File(videoHandler.getParentPath()), saveFiles);
        Intent intent = new Intent();
        intent.putExtra("path", output);
        intent.putExtra("thumb", thumbPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 加载视图
     */
    private void loadViews() {
        setContentView(R.layout.activity_media_recorder);
        svFfmpeg = findViewById(R.id.sv_ffmpeg);
        rbStart = findViewById(R.id.rb_start);
        rlPlayer = findViewById(R.id.rl_player);
        vvPlay = findViewById(R.id.vv_play);
        rlBottom = findViewById(R.id.rl_bottom2);
        ivNext = findViewById(R.id.iv_next);
        ivClose = findViewById(R.id.iv_close);
        ivBack = findViewById(R.id.iv_back);
        ivChangeCamera = findViewById(R.id.iv_change_camera);
        ivPhoto = findViewById(R.id.iv_photo);
        rlTop = findViewById(R.id.rl_top);
        ivMusic = findViewById(R.id.iv_music);
        ivNext.setSelected(true);
        rbStart.setMax(maxDuration);
        mDetector = new GestureDetector(this, new ZoomGestureListener());
        /**
         * 单独处理mSurfaceView的双击事件
         */
        svFfmpeg.setOnTouchListener((v, event) -> {
            mDetector.onTouchEvent(event);
            return true;
        });
        videoWidth = 640;
        videoHeight = 480;
        mSurfaceHolder = svFfmpeg.getHolder();
        //设置屏幕分辨率
        mSurfaceHolder.setFixedSize(videoWidth, videoHeight);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);

    }

    /**
     * 切换前置/后置摄像头
     */
    public void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = (Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraState = 2;
        } else {
            mCameraId = (Camera.CameraInfo.CAMERA_FACING_BACK);
            cameraState = 1;
        }
        stopPreview();
        startPreView(mSurfaceHolder);
    }

    private void videoFinish() {
        stopRecord();
        changeButton(true);
        rbStart.setVisibility(View.GONE);
        videoHandler.parseVideo(orinalPut, angle, cameraState, String.valueOf((bitrate/2)));

    }

    @Override
    public void onBackPressed() {
        if (!(bgmHandler != null && bgmHandler.dismiss(false))) {
            videoHandler.deleteDirRoom(new File(videoHandler.getParentPath()), null);
            super.onBackPressed();
        } else {
            if (vvPlay != null) {
                vvPlay.setVideoPath(output);
                if (vvPlay.isPrepared()) {
                    vvPlay.start();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 开始录制
     */
    private void startRecord() {

        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.reset();
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
                //从相机采集视频
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                // 从麦克采集音频信息
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                mMediaRecorder.setProfile(camcorderProfile);
                bitrate = camcorderProfile.videoBitRate;
                File targetDir = new File(videoHandler.getParentPath());
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                mTargetFile = new File(targetDir,
                        SystemClock.currentThreadTimeMillis() + ".mp4");
                if (!mTargetFile.exists()) {
                    mTargetFile.createNewFile();
                }
                mMediaRecorder.setOutputFile(mTargetFile.getAbsolutePath());
                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                //解决录制视频, 播放器横向问题
//                mMediaRecorder.setOrientationHint(90);

                mMediaRecorder.prepare();
                //正式录制
                mMediaRecorder.start();
                isRecording = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreview();
        bgmHandler.release();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        if (isRecording) {
            if (mCamera != null) {
                mCamera.lock();
            }
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            isRecording = false;
            orinalPut = mTargetFile.getAbsolutePath();

        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.lock();
                mCamera.release();
            } catch (Exception e) {

            }
            mCamera = null;
            releaseRecord();
        }
    }

    /**
     * 停止录制, 不保存
     */
    private void stopRecordUnSave() {
        try {
            if (isRecording) {
                if (mCamera != null) {
                    mCamera.lock();
                }
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                isRecording = false;
                if (mTargetFile.exists()) {
                    //不保存直接删掉
                    mTargetFile.delete();
                }
            }
            canRecord = true;
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

    }

    /**
     * 相机变焦
     *
     * @param zoomValue
     */
    public void setZoom(int zoomValue) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {//判断是否支持
                int maxZoom = parameters.getMaxZoom();
                if (maxZoom == 0) {
                    return;
                }
                if (zoomValue > maxZoom) {
                    zoomValue = maxZoom;
                }
                parameters.setZoom(zoomValue);
                mCamera.setParameters(parameters);
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startPreView(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            //停止预览并释放摄像头资源
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        releaseRecord();
    }

    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    class ZoomGestureListener extends GestureDetector.SimpleOnGestureListener {
        //双击手势事件
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            super.onDoubleTap(e);
            if (mMediaRecorder != null) {
                if (!isZoomIn) {
                    setZoom(20);
                    isZoomIn = true;
                } else {
                    setZoom(0);
                    isZoomIn = false;
                }
            }
            return true;
        }
    }
}
