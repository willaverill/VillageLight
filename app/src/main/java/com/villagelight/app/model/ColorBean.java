package com.villagelight.app.model;

import java.io.Serializable;

public class ColorBean implements Serializable {

    private String name;
    private int displayColor;
    private int sendColor;
    private int colorNo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayColor() {
        return displayColor;
    }

    public void setDisplayColor(int color) {
        this.displayColor = color;
    }

    public int getSendColor() {
        return sendColor;
    }

    public void setSendColor(int sendColor) {
        this.sendColor = sendColor;
    }

    public int getColorNo() {
        return colorNo;
    }

    public void setColorNo(int colorNo) {
        this.colorNo = colorNo;
    }
}
