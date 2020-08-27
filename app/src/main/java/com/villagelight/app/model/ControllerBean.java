package com.villagelight.app.model;

import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "Controller")
public class ControllerBean implements Serializable {

    private int id;
    private String controllerName;
    private String deviceName;
    private String deviceMac;
    private String password;
    private int year;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}

