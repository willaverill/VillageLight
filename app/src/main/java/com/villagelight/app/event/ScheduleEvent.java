package com.villagelight.app.event;

public class ScheduleEvent {

    private boolean isShowDialog = true;

    public ScheduleEvent(boolean isShowDialog) {
        this.isShowDialog = isShowDialog;
    }

    public boolean isShowDialog() {
        return isShowDialog;
    }

    public void setShowDialog(boolean showDialog) {
        isShowDialog = showDialog;
    }
}
