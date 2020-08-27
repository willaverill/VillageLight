package com.villagelight.app.model;

import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "Channel")
public class ChannelBean implements Serializable {

    private int id;
    private int tid;
    private String name;
    private int sendColor1 = 0x00000000;
    private int sendColor2 = 0x00000000;
    private int sendColor3 = 0x00000000;
    private int displayColor1 = 0x00000000;
    private int displayColor2 = 0x00000000;
    private int displayColor3 = 0x00000000;
    private boolean isTwinkleOn;
    private int colorNo1;
    private int colorNo2;
    private int colorNo3;

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSendColor1() {
        return sendColor1;
    }

    public void setSendColor1(int color1) {
        this.sendColor1 = color1;
    }

    public int getSendColor2() {
        return sendColor2;
    }

    public void setSendColor2(int color2) {
        this.sendColor2 = color2;
    }

    public int getSendColor3() {
        return sendColor3;
    }

    public void setSendColor3(int color3) {
        this.sendColor3 = color3;
    }

    public int getDisplayColor1() {
        return displayColor1;
    }

    public void setDisplayColor1(int displayColor1) {
        this.displayColor1 = displayColor1;
    }

    public int getDisplayColor2() {
        return displayColor2;
    }

    public void setDisplayColor2(int displayColor2) {
        this.displayColor2 = displayColor2;
    }

    public int getDisplayColor3() {
        return displayColor3;
    }

    public void setDisplayColor3(int displayColor3) {
        this.displayColor3 = displayColor3;
    }

    public boolean isTwinkleOn() {
        return isTwinkleOn;
    }

    public void setTwinkleOn(boolean twinkleOn) {
        isTwinkleOn = twinkleOn;
    }

    public int getColorNo1() {
        return colorNo1;
    }

    public void setColorNo1(int colorNo1) {
        this.colorNo1 = colorNo1;
    }

    public int getColorNo2() {
        return colorNo2;
    }

    public void setColorNo2(int colorNo2) {
        this.colorNo2 = colorNo2;
    }

    public int getColorNo3() {
        return colorNo3;
    }

    public void setColorNo3(int colorNo3) {
        this.colorNo3 = colorNo3;
    }


}
