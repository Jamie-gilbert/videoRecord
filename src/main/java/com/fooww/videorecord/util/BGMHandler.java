package com.fooww.videorecord.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.fooww.videorecord.bean.BGMBean;
import com.fooww.videorecord.widgets.MyVideoView;
import com.fooww.weixinrecorded.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ggg
 * @version 1.0
 * @date 2018/9/27 13:24
 * @description bgm的处理类
 */
public class BGMHandler {
    private Context context;
    /**
     * BGM 列表
     */
    private PopupWindow musicList;
    /**
     * 数据源
     */
    private List<BGMBean> bgmBeans;
    /**
     * bgm 适配器
     */
    private BGMAdapter adapter;
    /**
     * bgm 选中事件
     */
    private OnBGMSelectedListener onBGMSelectedListener;

    private MediaPlayer mediaPlayer;
    private MyVideoView videoView;

    public BGMHandler(Context context, MyVideoView videoView) {
        this.context = context;
        this.videoView = videoView;
        bgmBeans = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        initMusicList();
    }

    /**
     * 初始化数据
     *
     * @return
     */
    @SuppressLint({"CheckResult"})
    public BGMHandler initData() {
        Single.create((SingleOnSubscribe<String>) e -> {
            BGMBean bgmBean = new BGMBean();
            bgmBean.setName("无音乐");
            bgmBean.setUrl("");
            bgmBean.setSelected(true);
            bgmBeans.add(bgmBean);

            bgmBean = new BGMBean();
            bgmBean.setName("舒缓");
            bgmBean.setUrl(writeBGM(R.raw.music1, VideoHandler.MUSIC_PATH + File.separator + "music1.mp3"));
            bgmBeans.add(bgmBean);

            bgmBean = new BGMBean();
            bgmBean.setName("浪漫");
            bgmBean.setUrl(writeBGM(R.raw.music2, VideoHandler.MUSIC_PATH + File.separator + "music2.mp3"));
            bgmBeans.add(bgmBean);

            bgmBean = new BGMBean();
            bgmBean.setName("温馨");
            bgmBean.setUrl(writeBGM(R.raw.music3, VideoHandler.MUSIC_PATH + File.separator + "music3.mp3"));
            bgmBeans.add(bgmBean);

            bgmBean = new BGMBean();
            bgmBean.setName("宁静");
            bgmBean.setUrl(writeBGM(R.raw.music4, VideoHandler.MUSIC_PATH + File.separator + "music4.mp3"));
            bgmBeans.add(bgmBean);

            bgmBean = new BGMBean();
            bgmBean.setName("欢乐");
            bgmBean.setUrl(writeBGM(R.raw.music5, VideoHandler.MUSIC_PATH + File.separator + "music5.mp3"));
            bgmBeans.add(bgmBean);
            e.onSuccess("success");

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }, throwable -> {

                });

        return this;
    }

    /**
     * 将bgm写到sd卡
     *
     * @param rawId
     * @param path
     * @throws IOException
     */
    private String writeBGM(int rawId, String path) throws IOException {

        InputStream inStream = context.getResources().openRawResource(rawId);
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);//存入SDCard
            byte[] buffer = new byte[10];
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] bs = outStream.toByteArray();
            fileOutputStream.write(bs);
            outStream.close();
            inStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        return path;
    }

    public void setOnBGMSelectedListener(OnBGMSelectedListener onBGMSelectedListener) {
        this.onBGMSelectedListener = onBGMSelectedListener;
    }

    /**
     * 显示背景音乐列表
     */
    public void show(View parent) {
        if (musicList == null) {
            initMusicList();
        }
        int index = 0;
        for (int i = 0; i < bgmBeans.size(); i++) {
            if (bgmBeans.get(i).isSelected() == true) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            play(index);
        }

        if (!musicList.isShowing()) {
            musicList.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        }

    }

    /**
     * 清除选择中的BGM
     */
    public void clear() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        if (bgmBeans != null) {
            for (BGMBean bgmBean : bgmBeans) {
                bgmBean.setSelected(false);
                bgmBean.setPlaying(false);
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 隐藏列表
     */
    public boolean dismiss(boolean isSure) {
        boolean isShow = musicList != null && musicList.isShowing();
        if (!isSure) {
            for (int i = 0; i < bgmBeans.size(); i++) {
                bgmBeans.get(i).setSelected(false);
            }
            bgmBeans.get(0).setSelected(true);
            adapter.notifyDataSetChanged();
        }
        if (isShow) {
            musicList.dismiss();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }

        return isShow;
    }

    /**
     * 初始化bgm 选择列表
     */
    private void initMusicList() {
        musicList = new PopupWindow(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bgms, null);
        RecyclerView rvBgms = view.findViewById(R.id.rv_bgms);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        Button btnSure = view.findViewById(R.id.btn_sure);
        musicList.setWidth(DeviceUtils.getScreenWidth(context));
        musicList.setBackgroundDrawable(null);
        rvBgms.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        rvBgms.setLayoutManager(new LinearLayoutManager(context));
        adapter = new BGMAdapter(bgmBeans, index -> {
            for (int i = 0; i < bgmBeans.size(); i++) {
                bgmBeans.get(i).setSelected(false);
            }
            bgmBeans.get(index).setSelected(true);
            adapter.notifyDataSetChanged();

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                play(index);
            }
        });
        rvBgms.setAdapter(adapter);
        musicList.setContentView(view);
        btnSure.setOnClickListener(v -> {
            dismiss(true);
            if (onBGMSelectedListener != null) {
                String url = "";
                for (BGMBean bgmBean : bgmBeans) {
                    if (bgmBean.isSelected()) {
                        url = bgmBean.getUrl();
                        break;
                    }
                }
                onBGMSelectedListener.onBGMSelected(url);
            }

        });
        ivClose.setOnClickListener(v -> {

            dismiss(false);
            onBGMSelectedListener.onClosed();
        });
    }

    /**
     * 播放音乐
     *
     * @param index
     */
    private void play(int index) {
        if (videoView != null && videoView.isPlaying()) {
            videoView.stop();
        }
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            if (!TextUtils.isEmpty(bgmBeans.get(index).getUrl())) {
                try {
                    mediaPlayer.setDataSource(bgmBeans.get(index).getUrl());

                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * bgm 列表适配器
     */
    class BGMAdapter extends RecyclerView.Adapter<BGMAdapter.BGMHolder> {
        private List<BGMBean> bgmBeans;
        private OnBGMItemSelectedListener listener;

        public BGMAdapter(List<BGMBean> bgmBeans, OnBGMItemSelectedListener listener) {
            this.bgmBeans = bgmBeans;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BGMHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_bgm, parent, false);
            BGMHolder holder = new BGMHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final BGMHolder holder, final int position) {
            BGMBean bgmBean = bgmBeans.get(position);
            holder.ivSelector.setSelected(bgmBean.isSelected());
            holder.tvName.setText(bgmBean.getName());
            holder.itemView.setOnClickListener(v -> listener.onBGMItemSelected(position));

        }

        @Override
        public int getItemCount() {
            return bgmBeans != null ? bgmBeans.size() : 0;
        }

        class BGMHolder extends RecyclerView.ViewHolder {
            ImageView ivSelector;
            TextView tvName;

            public BGMHolder(View itemView) {
                super(itemView);
                ivSelector = itemView.findViewById(R.id.iv_selector);
                tvName = itemView.findViewById(R.id.tv_name);
            }
        }

    }

    /**
     * bgm音乐选择
     */
    interface OnBGMItemSelectedListener {
        /**
         * bgm音乐选择
         *
         * @param index 下标
         */
        void onBGMItemSelected(int index);


    }

    /**
     * bgm 选中回调
     */
    public interface OnBGMSelectedListener {
        /**
         * bgm 选中事件
         *
         * @param url bgm 路径
         */
        void onBGMSelected(String url);

        /**
         * 点击关闭按钮
         */
        void onClosed();

    }
}
