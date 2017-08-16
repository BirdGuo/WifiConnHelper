package com.gxw.wificonnhelperlib.utils.bean;

import android.net.wifi.ScanResult;

/**
 * Created by guoxw on 2017/8/16 0016.
 *
 * @auther guoxw
 * @createTime 2017/8/16 0016 17:22
 * @packageName com.gxw.wificonnhelperlib.utils.bean
 */

public class WifiBeanConn {

    private String password = "";
    private ScanResult scanResult;

    public WifiBeanConn() {
    }

    public WifiBeanConn(String password, ScanResult scanResult) {
        this.password = password;
        this.scanResult = scanResult;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    @Override
    public String toString() {
        return "WifiBeanConn{" +
                "password='" + password + '\'' +
                ", scanResult=" + scanResult +
                '}';
    }
}
