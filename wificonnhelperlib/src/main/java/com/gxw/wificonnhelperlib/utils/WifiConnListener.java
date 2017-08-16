package com.gxw.wificonnhelperlib.utils;

/**
 * Created by guoxw on 2017/8/16 0016.
 *
 * @auther guoxw
 * @createTime 2017/8/16 0016 17:04
 * @packageName com.gxw.wificonnhelperlib.utils
 */

public interface WifiConnListener {

    /**
     * On wifi connect start.
     *
     * @param ssid
     *         the ssid
     * @param bssid
     *         the bssid
     */
    void onWifiConnectStart(String ssid, String bssid);

    /**
     * On wifi connect success.
     *
     * @param ssid
     *         the ssid
     * @param bssid
     *         the bssid
     */
    void onWifiConnectSuccess(String ssid, String bssid);

    /**
     * On wifi connnect fail.
     *
     * @param ssid
     *         the ssid
     * @param bssid
     *         the bssid
     * @param reason
     *         the reason
     */
    void onWifiConnnectFail(String ssid, String bssid, int reason);

}
