/*
 * Copyright 2017 Bird Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
