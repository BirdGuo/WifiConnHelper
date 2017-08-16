package com.gxw.wificonnhelperlib.utils.bean;

import android.net.wifi.ScanResult;

/**
 * Created by guoxw on 2017/8/14 0014.
 *
 * @auther guoxw
 * @createTime 2017/8/14 0014 10:16
 * @packageName com.gxw.wificonnectdemo.bean
 */

public class WifiBean {

    private  boolean isConnect;
    private ScanResult scanResult;

    public WifiBean() {
    }

    public WifiBean(boolean isConnect, ScanResult scanResult) {
        this.isConnect = isConnect;
        this.scanResult = scanResult;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    @Override
    public String toString() {
        return "WifiBean{" +
                "isConnect=" + isConnect +
                ", scanResult=" + scanResult +
                '}';
    }
}
