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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * The type Wifi admin.
 */
public class WifiAdmin {

    private String TAG = WifiAdmin.class.getName().toString();

    // 定义WifiManager对象
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private ArrayList<ScanResult> mWifiList;

    private ArrayList<ScanResult> m24GData, m5GData, mAllData;

    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;

    /**
     * 定义一个wifiLock
     */
    WifiManager.WifiLock mWifiLock;


    /**
     * 构造器
     *
     * @param context the context
     */
    public WifiAdmin(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();

        m24GData = new ArrayList<ScanResult>();
        m5GData = new ArrayList<ScanResult>();
        mAllData = new ArrayList<ScanResult>();
    }

    /**
     * Open wifi.
     * 打开WIFI
     */
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * Close wifi.
     * 关闭WIFI
     */
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * Check state int.
     * 检查当前WIFI状态
     *
     * @return the int
     */
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * Acquire wifi lock.
     * 锁定WifiLock
     */
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * Release wifi lock.
     * 解锁WifiLock
     */
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * Creat wifi lock.
     * 创建一个WifiLock
     */
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    /**
     * Gets configuration.
     * 得到配置好的网络
     *
     * @return the configuration
     */
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    /**
     * Start scan.
     * 开始扫描
     */
    public void startScan() {
        mWifiManager.startScan();
        // 得到扫描结果
//        mWifiList = mWifiManager.getScanResults();
        m24GData.clear();
        m24GData = null;
        m24GData = new ArrayList<ScanResult>();
        m5GData.clear();
        m5GData = null;
        m5GData = new ArrayList<ScanResult>();
        mAllData.clear();
        mAllData = null;
        mAllData = new ArrayList<ScanResult>();
        try {
            List<ScanResult> list = mWifiManager.getScanResults();
            if (list != null) {
                for (ScanResult scanResult : list) {
                    int nSigLevel = WifiManager.calculateSignalLevel(
                            scanResult.level, 100);
                    int value = scanResult.frequency;
                    if (value > 2400 && value < 2500) {
                        m24GData.add(scanResult);
                    } else {
                        m5GData.add(scanResult);
                    }
                    mAllData.add(scanResult);
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    /**
     * Gets wifi list 24 g.
     * 得到网络列表
     *
     * @return the wifi list 24 g
     */
    public ArrayList<ScanResult> getWifiList24G() {
        ArrayList<ScanResult> newSr = new ArrayList<ScanResult>();
        for (ScanResult result : m24GData) {
            if (!TextUtils.isEmpty(result.SSID) && !result.capabilities.contains("[IBSS]") && !containName(newSr, result))
                newSr.add(result);
        }
        return newSr;
    }

    /**
     * Gets wifi list all.
     * 得到网络列表
     *
     * @return the wifi list all
     */
    public ArrayList<ScanResult> getWifiListAll() {
        ArrayList<ScanResult> newSr = new ArrayList<ScanResult>();
        for (ScanResult result : mAllData) {
            if (!TextUtils.isEmpty(result.SSID) && !result.capabilities.contains("[IBSS]") && !containName(newSr, result))
                newSr.add(result);
        }
        return newSr;
    }

    /**
     * 判断一个扫描结果中，是否包含了某个名称的WIFI
     *
     * @param sr         扫描结果
     * @param scanResult 要查询的名称
     * @return 返回true表示包含了该名称的WIFI ，返回false表示不包含
     */
    public boolean containName(List<ScanResult> sr, ScanResult scanResult) {
        for (ScanResult result : sr) {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(scanResult.SSID) && result.capabilities.equals(scanResult.capabilities)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Look up scan string builder.
     * 查看扫描结果
     *
     * @return the string builder
     */
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder
                    .append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    /**
     * Gets mac address.
     * 得到MAC地址
     *
     * @return the mac address
     */
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    /**
     * Gets bssid.
     * 得到接入点的BSSID
     *
     * @return the bssid
     */
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    /**
     * Gets ip address.
     * 得到IP地址
     *
     * @return the ip address
     */
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /**
     * Gets network id.
     * 得到连接的ID
     *
     * @return the network id
     */
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * Gets wifi info.
     * 得到WifiInfo的所有信息包
     *
     * @return the wifi info
     */
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    /**
     * Add network int.
     * 添加一个网络并连接
     *
     * @param wcg the wcg
     * @return the int
     */
    public int addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        return wcgID;
    }

    /**
     * Disconnect wifi.
     * 断开指定wifi
     *
     * @param netId the net id
     */
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * 是否连接WIFI
     *
     * @param context the context
     * @return boolean boolean
     */
    public boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {

            return true;
        }
        return false;
    }

    /**
     * 获取连接wifi的bssid
     *
     * @param context the context
     * @return bSSID /若无连接则返回空
     */
    public String getConnectWifiBSSID(Context context) {
        String bSSID = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            bSSID = wifiInfo.getBSSID();
        }
        return bSSID;
    }

    /**
     * 获取连接wifi的bssid
     *
     * @param context the context
     * @return bSSID /若无连接则返回空
     */
    public WifiInfo getConnectWifi(Context context) {
        WifiInfo wifiInfo = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();
        }
        return wifiInfo;
    }

    /**
     * 获取连接wifi的bssid
     *
     * @param context the context
     * @return bSSID /若无连接则返回空
     */
    public String getConnectWifiSSID(Context context) {
        String ssid = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid)) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        return ssid;
    }


    /**
     * 获取连接id
     *
     * @param context the context
     * @return connect wifi net work id
     */
    public int getConnectWifiNetWorkID(Context context) {
        int id = -1;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            id = wifiInfo.getNetworkId();
        }
        return id;
    }


    /**
     * 判断是否为当前连接wifi
     *
     * @param context the context
     * @param ssid    唯一识别号
     * @return boolean boolean
     */
    public boolean isWifiConnected(Context context, String ssid) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid1 = wifiInfo.getSSID();
            if (ssid.equals(ssid1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有密码
     *
     * @param scanResult the scan result
     * @return boolean
     */
    public boolean hasPwd(ScanResult scanResult) {
        //判断是否有密码
        if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("PSK") || scanResult.capabilities.contains("EAP")) {
            //标识有密码
            return true;
        }
        return false;
    }

    /**
     * 返回加密类型
     *
     * @param scanResult the scan result
     * @return wifi connector . security mode
     */
    public WifiSecurityMode secretMode(ScanResult scanResult) {
        WifiSecurityMode sm = null;
        String capabilities = scanResult.capabilities;
        if (capabilities.contains("WPA") || capabilities.contains("WPS")) {
            sm = WifiSecurityMode.WPA2;
        } else if (capabilities.contains("WEP")) {
            sm = WifiSecurityMode.WEP;
        } else {
            sm = WifiSecurityMode.OPEN;
        }
        return sm;
    }

    /**
     * Is exsits wifi configuration.
     * 判断是否已经连接过
     *
     * @param SSID the ssid
     * @return the wifi configuration
     */
    public WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * Remove exit config.
     * 移除已经连接过的
     *
     * @param ssid the ssid
     */
    public void removeExitConfig(String ssid) {
        WifiConfiguration exsits = isExsits(ssid);
        if (exsits != null) {//如果已经连接过则删除
            mWifiManager.removeNetwork(exsits.networkId);
        }

    }

    /**
     * 断开所有
     */
    public void dissconnectAll() {
        mWifiManager.disconnect();
    }

    /**
     * 获得最高优先级
     *
     * @return 最大优先级
     */
    public int getMaxPriority() {
        final List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        int pri = 0;
        for (WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

}
