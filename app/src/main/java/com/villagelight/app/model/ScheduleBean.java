package com.villagelight.app.model;

public class ScheduleBean {

    private String mDate;
    private ColorBean mColorBean;
    private int colorIndex;

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public ColorBean getColorBean() {
        return mColorBean;
    }

    public void setColorBean(ColorBean colorBean) {
        mColorBean = colorBean;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }
}
