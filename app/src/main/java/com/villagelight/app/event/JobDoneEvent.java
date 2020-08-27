package com.villagelight.app.event;

public class JobDoneEvent {

    private byte cmd;

    public JobDoneEvent(byte cmd) {
        this.cmd = cmd;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }
}
