package com.villagelight.app.model;

import java.util.Arrays;


public class SendBean {

    byte[] datas;

    public SendBean(byte[] datas) {
        this.datas = datas;
    }

    public byte[] getDatas() {
        return datas;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SendBean that = (SendBean) o;

        return Arrays.equals(datas, that.datas);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(datas);
    }
}
