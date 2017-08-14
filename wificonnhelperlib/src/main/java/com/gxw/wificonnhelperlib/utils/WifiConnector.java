package com.gxw.wificonnhelperlib.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type Wifi connector.
 */
public class WifiConnector {
    private static final int WIFI_CONNECT_TIMEOUT = 15; //连接WIFI的超时时间

    private Context mContext;
    private WifiManager mWifiManager;
    private Lock mLock;
    private Condition mCondition;
    private WiFiConnectReceiver mWifiConnectReceiver;
    private WifiConnectListener mWifiConnectListener;
    private boolean mIsConnnected = false;
    private int mNetworkID = -1;
    private boolean ishow = false;
    private int reason = -1;

    /**
     * The enum Security mode.
     */
//网络加密模式
    public enum SecurityMode {
        /**
         * Open security mode.
         */
        OPEN, /**
         * Wep security mode.
         */
        WEP, /**
         * Wpa security mode.
         */
        WPA, /**
         * Wpa 2 security mode.
         */
        WPA2
    }

    /**
     * The interface Wifi connect listener.
     */
//通知连接结果的监听接口
    public interface WifiConnectListener {

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

        /**
         * On wifi connect completed.
         *
         * @param isConnected
         *         the is connected
         * @param ssid
         *         the ssid
         * @param bssid
         *         the bssid
         * @param password
         *         the password
         * @param reason
         *         the reason
         */
        void OnWifiConnectCompleted(boolean isConnected, String ssid, String bssid, String password, int reason);
    }

    /**
     * Instantiates a new Wifi connector.
     *
     * @param context
     *         the context
     * @param listener
     *         the listener
     */
    public WifiConnector(Context context, WifiConnectListener listener) {

        mContext = context;

        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mWifiConnectReceiver = new WiFiConnectReceiver();

        mWifiConnectListener = listener;

    }

    /**
     * Ishow boolean.
     *
     * @return the boolean
     */
    public boolean ishow() {
        return ishow;
    }

    /**
     * Sets ishow.
     *
     * @param ishow
     *         the ishow
     */
    public void setIshow(boolean ishow) {
        this.ishow = ishow;
    }

    /**
     * 未连接的wifi
     *
     * @param ssid
     *         the ssid
     * @param password
     *         the password
     * @param bssid
     *         the bssid
     * @param mode
     *         the mode
     */
    public void connect(final String ssid, final String password, final String bssid, final SecurityMode mode) {
        //断开WiFi 这样做会比较快一点
        dissconnectAll();
        mWifiConnectListener.onWifiConnectStart(ssid, bssid);
        new Thread(new Runnable() {

            @Override
            public void run() {
                //如果WIFI没有打开，则打开WIFI
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }

                //注册连接结果监听对象
                mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                //连接指定SSID
                if (password != null && !"".equals(password)) {
                    if (!onConnect(ssid, password, mode, true)) {//连接未连过且有密码的wifi
//                        mWifiConnectListener.OnWifiConnectCompleted(false, ssid, bssid, password, reason);//连接失败
                        mWifiConnectListener.onWifiConnnectFail(ssid, bssid, reason);
                    } else {
//                        mWifiConnectListener.OnWifiConnectCompleted(true, ssid, bssid, password, reason);//连接成功
                        mWifiConnectListener.onWifiConnectSuccess(ssid, bssid);
                    }
                } else {
                    // ESS
                    if (!onConnect(ssid, true)) {//连接未连过且无密码的wifi
//                        mWifiConnectListener.OnWifiConnectCompleted(false, ssid, bssid, password, reason);//连接失败
                        mWifiConnectListener.onWifiConnnectFail(ssid, bssid, reason);

                    } else {
//                        mWifiConnectListener.OnWifiConnectCompleted(true, ssid, bssid, password, reason);//连接成功
                        mWifiConnectListener.onWifiConnectSuccess(ssid, bssid);
                    }
                }

                //删除注册的监听类对象
                //http://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error
                try {
                    mContext.unregisterReceiver(mWifiConnectReceiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 链接有密码的WiFi
     *
     * @param ssid
     *         the ssid
     * @param password
     *         the password
     * @param mode
     *         the mode
     * @param reassociate
     *         the reassociate
     *
     * @return boolean boolean
     */
    protected boolean onConnect(String ssid, String password, SecurityMode mode, boolean reassociate) {

        removeExitConfig(ssid);

        //添加新的网络配置
        WifiConfiguration cfg = new WifiConfiguration();
        cfg.SSID = "\"" + ssid + "\"";
        if (password != null && !"".equals(password)) {
            //这里比较关键，如果是WEP加密方式的网络，密码需要放到cfg.wepKeys[0]里面
            if (mode == SecurityMode.WEP) {
                cfg.wepKeys[0] = "\"" + password + "\"";
                cfg.wepTxKeyIndex = 0;
            } else {
                cfg.preSharedKey = "\"" + password + "\"";
            }
        }
        cfg.status = WifiConfiguration.Status.ENABLED;

        //添加网络配置
        mNetworkID = mWifiManager.addNetwork(cfg);

        mLock.lock();
        cfg.networkId = mNetworkID;

        int oldPri = cfg.priority;
        // Make it the highest priority.
        int newPri = getMaxPriority(mWifiManager) + 1;
        cfg.priority = newPri;//提高优先级

        mWifiManager.updateNetwork(cfg);
        mWifiManager.saveConfiguration();//保存，这是放在lock前还是lock后
        mIsConnnected = false;
        //连接该网络
        if (!mWifiManager.enableNetwork(mNetworkID, true)) {//enableNetwork(id,true)断开其他，false不断开
            mLock.unlock();
            return false;
        }

        boolean connect = reassociate ? mWifiManager.reassociate() : mWifiManager.reconnect();//无论是否连接，都连这个
        if (!connect) {
            mLock.unlock();
            return false;
        }

        try {
            //等待连接结果
            mCondition.await(WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();

        return mIsConnnected;
    }


    /**
     * 链接没有密码的wifi
     *
     * @param SSID
     *
     * @return
     */
    private boolean onConnect(String SSID, boolean reassociate) {

        removeExitConfig(SSID);

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // config.wepKeys[0] = password;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.wepTxKeyIndex = 0;

        //添加网络配置
        mNetworkID = mWifiManager.addNetwork(config);

        mLock.lock();

        mIsConnnected = false;
        config.networkId = mNetworkID;
        int oldPri = config.priority;
        // Make it the highest priority.
        int newPri = getMaxPriority(mWifiManager) + 1;
        config.priority = newPri;//提高优先级
        mWifiManager.updateNetwork(config);
        mWifiManager.saveConfiguration();//保存
        //连接该网络
        if (!mWifiManager.enableNetwork(mNetworkID, true)) {
            mLock.unlock();
            return false;
        }

        boolean connect = reassociate ? mWifiManager.reassociate() : mWifiManager.reconnect();
        if (!connect) {
            mLock.unlock();
            return false;
        }

        try {
            //等待连接结果
            mCondition.await(WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();

        return mIsConnnected;
    }

    /**
     * 链接已连接过的WiFi
     *
     * @param configuration
     *         the configuration
     */
    public void connect(final WifiConfiguration configuration) {
        //断开WiFi 这样做会比较快一点
        dissconnectAll();
        new Thread(new Runnable() {

            @Override
            public void run() {
                //如果WIFI没有打开，则打开WIFI
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }

                //注册连接结果监听对象
                mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                if (configuration != null) {
                    if (!onConnectConfig(configuration, true)) {//连接
//                        mWifiConnectListener.OnWifiConnectCompleted(false, configuration.SSID.substring(1, configuration.SSID.length() - 1), configuration.BSSID, "", reason);
                        mWifiConnectListener.onWifiConnnectFail(configuration.SSID, configuration.BSSID, reason);
                    } else {
//                        mWifiConnectListener.OnWifiConnectCompleted(true, configuration.SSID.substring(1, configuration.SSID.length() - 1), configuration.BSSID, "", reason);
                        mWifiConnectListener.onWifiConnectSuccess(configuration.SSID, configuration.BSSID);
                    }
                }

                //删除注册的监听类对象
                //http://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error
                try {
                    mContext.unregisterReceiver(mWifiConnectReceiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean onConnectConfig(WifiConfiguration wifiConfiguration, boolean reassociate) {
        //添加新的网络配置

        //添加网络配置
        int networkId1 = wifiConfiguration.networkId;
        mNetworkID = mWifiManager.addNetwork(wifiConfiguration);
        if (mNetworkID == -1) {
            mNetworkID = networkId1;
        }
        mLock.lock();
        int oldPri = wifiConfiguration.priority;
        // Make it the highest priority.
        int newPri = getMaxPriority(mWifiManager) + 1;
        wifiConfiguration.networkId = mNetworkID;
        wifiConfiguration.priority = newPri;//提高优先级
        mWifiManager.updateNetwork(wifiConfiguration);//更新
        mIsConnnected = false;
        mWifiManager.saveConfiguration();//保存


        boolean b = mWifiManager.enableNetwork(mNetworkID, true);
        //连接该网络
        if (!b) {
            mLock.unlock();
            return false;
        }

        boolean connect = reassociate ? mWifiManager.reassociate() : mWifiManager.reconnect();
        if (!connect) {
            mLock.unlock();
            return false;
        }

        try {
            //等待连接结果
            mCondition.await(WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();

        return mIsConnnected;
    }

    /**
     * The type Wi fi connect receiver.
     */
//监听系统的WIFI连接消息
    protected class WiFiConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }

            mLock.lock();
            int authState = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if (authState == WifiManager.ERROR_AUTHENTICATING) {//身份验证错误到这儿
                //提示密码错误
                reason = 1;
            }
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                //获取当前wifi名称
                WifiInfo info2 = mWifiManager.getConnectionInfo();

                if (info2.getNetworkId() == mNetworkID && info2.getSupplicantState() == SupplicantState.COMPLETED) {//这儿可能报空指针
                    mIsConnnected = true;
                    mCondition.signalAll();
                }
            }

            mLock.unlock();
        }
    }

    private int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        for (final WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

    private static final int MAX_PRIORITY = 99999;//最大连接数

    private int shiftPriorityAndSave(final WifiManager wifiMgr) {
        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        sortByPriority(configurations);
        final int size = configurations.size();
        for (int i = 0; i < size; i++) {
            final WifiConfiguration config = configurations.get(i);
            config.priority = i;
            wifiMgr.updateNetwork(config);
        }
        wifiMgr.saveConfiguration();
        return size;
    }

    private static void sortByPriority(final List<WifiConfiguration> configurations) {
        java.util.Collections.sort(configurations, new Comparator<WifiConfiguration>() {

            @Override
            public int compare(WifiConfiguration object1,
                               WifiConfiguration object2) {
                return object1.priority - object2.priority;
            }
        });
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
        LogUtil.showError("GXW", "------------------------");
        for (WifiConfiguration existingConfig : existingConfigs) {
            LogUtil.showError("GXW", existingConfig.SSID);
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        LogUtil.showError("GXW", "------------------------");
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
     * Dissconnect all.
     */
    public void dissconnectAll() {
        mWifiManager.disconnect();
    }

}
