package com.fooww.videorecord.bean;

/**
 * @author ggg
 * @version 1.0
 * @date 2018/9/27 13:31
 * @description
 */
public class BGMBean {
    private String id;
    private String name;
    private boolean selected;
    private boolean playing;
    private String url;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
