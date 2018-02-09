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

package com.gxw.wificonnhelperlib.utils;

/**
 * Created by guoxw on 2017/8/16 0016.
 *
 * @auther guoxw
 * @createTime 2017/8/16 0016 17:04
 * @packageName com.gxw.wificonnhelperlib.utils
 */

/**
 * Wifi连接接口回调
 */
public interface WifiConnListener {

    /**
     * Wifi开始连接接口回调
     *
     * @param ssid  wifi名称
     * @param bssid ip
     */
    void onWifiConnectStart(String ssid, String bssid);

    /**
     * Wifi连接成功接口回调
     *
     * @param ssid  wifi名称
     * @param bssid ip
     */
    void onWifiConnectSuccess(String ssid, String bssid);

    /**
     * Wifi连接失败接口回调
     *
     * @param ssid  wifi名称
     * @param bssid ip
     * @param reason 失败原因
     */
    void onWifiConnnectFail(String ssid, String bssid, int reason);

}
