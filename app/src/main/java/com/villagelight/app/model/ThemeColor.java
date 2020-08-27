package com.villagelight.app.model;

import com.lidroid.xutils.db.annotation.NoAutoIncrement;
import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "ThemeColor")
public class ThemeColor implements Serializable {

    @NoAutoIncrement
    private int id;
    private int cid;
    private String name;
    private int fade;
    private List<ChannelBean> channels = new ArrayList<>();

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFade() {
        return fade;
    }

    public void setFade(int fade) {
        this.fade = fade;
    }

    public List<ChannelBean> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelBean> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeColor that = (ThemeColor) o;
        return id == that.id &&
                fade == that.fade &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, fade);
    }
}
