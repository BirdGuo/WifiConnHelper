package com.gxw.wificonnhelperlib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

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
     * @param context
     *         the context
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
     */
// 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * Close wifi.
     */
// 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * Check state int.
     *
     * @return the int
     */
// 检查当前WIFI状态
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * Acquire wifi lock.
     */
// 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * Release wifi lock.
     */
// 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * Creat wifi lock.
     */
// 创建一个WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    /**
     * Gets configuration.
     *
     * @return the configuration
     */
// 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    /**
     * Connect configuration.
     *
     * @param index
     *         the index
     */
// 指定配置好的网络进行连接
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }


    /**
     * Start scan.
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
     *
     * @return the wifi list 24 g
     */
// 得到网络列表
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
     *
     * @return the wifi list all
     */
// 得到网络列表
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
     * @param sr
     *         扫描结果
     * @param scanResult
     *         要查询的名称
     *
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
     *
     * @return the string builder
     */
// 查看扫描结果
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
     *
     * @return the mac address
     */
// 得到MAC地址
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    /**
     * Gets bssid.
     *
     * @return the bssid
     */
// 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    /**
     * Gets ip address.
     *
     * @return the ip address
     */
// 得到IP地址
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /**
     * Gets network id.
     *
     * @return the network id
     */
// 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * Gets wifi info.
     *
     * @return the wifi info
     */
// 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    /**
     * Add network int.
     *
     * @param wcg
     *         the wcg
     *
     * @return the int
     */
// 添加一个网络并连接
    public int addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        return wcgID;
    }

    /**
     * Disconnect wifi.
     *
     * @param netId
     *         the net id
     */
// 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * Disconnect wifi no id.
     */
    public void disconnectWifiNoId() {
        mWifiManager.disconnect();
    }

    //然后是一个实际应用方法，只验证过没有密码的情况：

    /**
     * 添加wifi
     *
     * @param SSID
     *         wifi名
     * @param Password
     *         密码
     * @param Type
     *         1：未知；2：无密码 3：有密码
     *
     * @return wifi configuration
     */
    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 是否连接WIFI
     *
     * @param context
     *         the context
     *
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
     * @param context
     *         the context
     *
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
     * @param context
     *         the context
     *
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
     * @param context
     *         the context
     *
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
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }


    /**
     * 获取连接id
     *
     * @param context
     *         the context
     *
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
     * @param context
     *         the context
     * @param ssid
     *         唯一识别号
     *
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
     * @param scanResult
     *         the scan result
     *
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
     * @param scanResult
     *         the scan result
     *
     * @return wifi connector . security mode
     */
    public WifiConnector.SecurityMode secretMode(ScanResult scanResult) {
        WifiConnector.SecurityMode sm = null;
        String capabilities = scanResult.capabilities;
        if (capabilities.contains("WPA") || capabilities.contains("WPS")) {
            sm = WifiConnector.SecurityMode.WPA2;
        } else if (capabilities.contains("WEP")) {
            sm = WifiConnector.SecurityMode.WEP;
        } else {
            sm = WifiConnector.SecurityMode.OPEN;
        }
        return sm;
    }

}
