package com.gxw.wificonnhelperlib.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.gxw.wificonnhelperlib.utils.bean.WifiBeanConn;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by guoxw on 2017/8/16 0016.
 *
 * @auther guoxw
 * @createTime 2017/8/16 0016 17:03
 * @packageName com.gxw.wificonnhelperlib.utils
 */
//通知连接结果的监听接口
public class WifiConnectorNew {

    private static WifiConnectorNew wifiConnectorNew;
    private Context mContext;

    private WifiManager mWifiManager;
    private Lock mLock;
    private Condition mCondition;

    private WifiBeanConn wifiBeanConn;
    private WifiConfiguration wifiConfiguration;

    private boolean mIsConnnected = false;
    private int mNetworkID = -1;

    private WifiConnectorNew(Context context) {
        this.mContext = context;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public WifiConnectorNew addWifiBeanConn(WifiBeanConn wifiBeanConn) {
        this.wifiBeanConn = wifiBeanConn;
        return this;
    }

    public WifiConnectorNew addWifiConfig(WifiConfiguration wifiConfiguration) {
        this.wifiConfiguration = wifiConfiguration;
        return this;
    }

    public void connectWifiHasPwd(WifiConnListener wifiConnListener) {
        dissconnectAll();
        wifiConnListener.onWifiConnectStart(wifiBeanConn.getScanResult().SSID, wifiBeanConn.getScanResult().BSSID);

    }

    public void connectWifiNoPwd() {
    }

    public void connectWifiConnected() {
    }

    private boolean onConnect() {

        //移除原有保存
        removeExitConfig(wifiBeanConn.getScanResult().SSID);

        //添加新的网络配置
        WifiConfiguration cfg = new WifiConfiguration();
        cfg.SSID = "\"" + wifiBeanConn.getScanResult().SSID + "\"";
        WifiConnector.SecurityMode securityMode = secretMode(wifiBeanConn.getScanResult());
        if (securityMode == WifiConnector.SecurityMode.WEP) {
            cfg.wepKeys[0] = "\"" + wifiBeanConn.getPassword() + "\"";
            cfg.wepTxKeyIndex = 0;
            cfg.status = WifiConfiguration.Status.ENABLED;
        } else if (securityMode == WifiConnector.SecurityMode.OPEN) {
            cfg.allowedAuthAlgorithms.clear();
            cfg.allowedGroupCiphers.clear();
            cfg.allowedKeyManagement.clear();
            cfg.allowedPairwiseCiphers.clear();
            cfg.allowedProtocols.clear();
            // config.wepKeys[0] = password;
            cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            cfg.wepTxKeyIndex = 0;
        } else {
            cfg.preSharedKey = "\"" + wifiBeanConn.getPassword() + "\"";
            cfg.status = WifiConfiguration.Status.ENABLED;
        }
        //添加网络配置
        mNetworkID = mWifiManager.addNetwork(cfg);
        mLock.lock();
        cfg.networkId = mNetworkID;
        int newPri = getMaxPriority() + 1;
        cfg.priority = newPri;//提高优先级

        mWifiManager.updateNetwork(cfg);//更新
        mWifiManager.saveConfiguration();//保存
        mIsConnnected = false;
        //连接该网络
        if (!mWifiManager.enableNetwork(mNetworkID, true)) {//enableNetwork(id,true)断开其他，false不断开
            mLock.unlock();
            return false;
        }

        boolean reassociate = mWifiManager.reassociate();

        if (!reassociate) {
            mLock.unlock();
            return false;
        }

        try {
            //等待连接结果
            mCondition.await(Constants.WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();


        return true;
    }

    /**
     * Is exsits wifi configuration.
     *
     * @param SSID
     *         the ssid
     *
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
     *
     * @param ssid
     *         the ssid
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

    private int getMaxPriority() {
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
