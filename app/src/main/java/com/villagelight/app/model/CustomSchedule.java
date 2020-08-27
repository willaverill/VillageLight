package com.villagelight.app.model;

public class CustomSchedule {

    private int id;
    private boolean isOn;
    private String time;
    private boolean photocell;
    private int theme;
    private long datetime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isPhotocell() {
        return photocell;
    }

    public void setPhotocell(boolean photocell) {
        this.photocell = photocell;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
