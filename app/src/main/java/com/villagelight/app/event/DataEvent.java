package com.villagelight.app.event;

public class DataEvent {

    private byte[] datas;

    public DataEvent(byte[] datas) {
        this.datas = datas;
    }

    public byte[] getDatas() {
        return datas;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }
}
