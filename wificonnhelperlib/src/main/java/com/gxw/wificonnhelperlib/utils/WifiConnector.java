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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.gxw.wificonnhelperlib.utils.bean.WifiBeanConn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by guoxw on 2017/8/16 0016.
 *
 * @auther guoxw
 * @createTime 2017 /8/16 0016 17:03
 * @packageName com.gxw.wificonnhelperlib.utils
 */

/**
 * Wifi连接操作类
 */
public class WifiConnector {

    /**
     * 单例
     */
    private static WifiConnector wifiConnectorNew;
    /**
     * 上下文
     */
    private Context mContext;

    /**
     * wifi控制器
     */
    private WifiManager mWifiManager;
    /**
     * wifi工具类
     */
    private WifiAdmin wifiAdmin;

    /**
     * 锁
     */
    private Lock mLock;
    private Condition mCondition;
    /**
     * 自定义wifi类
     */
    private WifiBeanConn wifiBeanConn;

    private WifiConfiguration wifiConfiguration;
    /**
     * 是否已连接
     */
    private boolean mIsConnnected = false;
    /**
     * 连接失败编号
     */
    private int reason = -1;
    /**
     * 连接wifi的NetworkID
     */
    private int mNetworkID = -1;

    /**
     * Wifi连接广播接收者
     */
    private WiFiConnectReceiver mWifiConnectReceiver;

    /**
     * 是否正在连接
     */
    private static boolean isConnecting = false;

    /**
     * 构造器
     *
     * @param context
     */
    public WifiConnector(Context context) {
        this.mContext = context;
        /**
         * 声明可重入锁 同一进程可重复进入，不同进程依次排队无法抢占
         * http://blog.csdn.net/yanyan19880509/article/details/52345422
         */
        mLock = new ReentrantLock();
        //获得Condition
        mCondition = mLock.newCondition();
        //声明wifi操作类
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //声明wifi工具类
        wifiAdmin = new WifiAdmin(context);
    }

    /**
     * 单例
     *
     * @param context
     * @return
     */
    public static WifiConnector build(Context context) {

        wifiConnectorNew = new WifiConnector(context);
        return wifiConnectorNew;
    }

    /**
     * 添加需要连接的wifi信息
     *
     * @param wifiBeanConn the wifi bean conn
     * @return the wifi connector new
     * @see com.gxw.wificonnhelperlib.utils.bean.WifiBeanConn
     */
    public WifiConnector addWifiBeanConn(WifiBeanConn wifiBeanConn) {
        this.wifiBeanConn = wifiBeanConn;
        return this;
    }

    /**
     * 增加一个 WifiConfiguration
     *
     * @param wifiConfiguration the wifi configuration
     * @return the wifi connector new
     * @see android.net.wifi.WifiConfiguration
     */
    public WifiConnector addWifiConfig(WifiConfiguration wifiConfiguration) {
        this.wifiConfiguration = wifiConfiguration;
        return this;
    }

    /**
     * 链接没有密码的wifi
     *
     * @param wifiConnListener
     * @see com.gxw.wificonnhelperlib.utils.WifiConnListener
     */
    public void connectWithSSID(WifiConnListener wifiConnListener) {
        if (!isConnecting) {
            wifiAdmin.dissconnectAll();
            wifiConnListener.onWifiConnectStart(wifiBeanConn.getScanResult().SSID, wifiBeanConn.getScanResult().BSSID);
            isConnecting = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    //打开wifi
                    wifiAdmin.openWifi();

//                    //注册连接结果监听对象
//                    mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

                    regReceive();

                    if (onConnectWithSSID()) {//连接成功
                        wifiConnListener.onWifiConnectSuccess(wifiBeanConn.getScanResult().SSID, wifiBeanConn.getScanResult().BSSID);
                    } else {//连接失败
                        wifiConnListener.onWifiConnnectFail(wifiBeanConn.getScanResult().SSID, wifiBeanConn.getScanResult().BSSID, reason);
                    }

                    removeReceive();

                    isConnecting = false;
                }
            }).start();
        } else {
            Toast.makeText(mContext, "正在连接请稍后", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 构造 WifiConfiguration
     *
     * @return
     */
    private WifiConnector buildWifiConfiguration() {

        //移除原有保存
        wifiAdmin.removeExitConfig(wifiBeanConn.getScanResult().SSID);

        //添加新的网络配置
        wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + wifiBeanConn.getScanResult().SSID + "\"";
        WifiSecurityMode securityMode = wifiAdmin.secretMode(wifiBeanConn.getScanResult());
        if (securityMode == WifiSecurityMode.WEP) {
            wifiConfiguration.wepKeys[0] = "\"" + wifiBeanConn.getPassword() + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        } else if (securityMode == WifiSecurityMode.OPEN) {
            wifiConfiguration.allowedAuthAlgorithms.clear();
            wifiConfiguration.allowedGroupCiphers.clear();
            wifiConfiguration.allowedKeyManagement.clear();
            wifiConfiguration.allowedPairwiseCiphers.clear();
            wifiConfiguration.allowedProtocols.clear();
            // config.wepKeys[0] = password;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.wepTxKeyIndex = 0;
        } else {
            wifiConfiguration.preSharedKey = "\"" + wifiBeanConn.getPassword() + "\"";
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        }

        //添加网络配置
        mNetworkID = mWifiManager.addNetwork(wifiConfiguration);
        wifiConfiguration.networkId = mNetworkID;
        int newPri = wifiAdmin.getMaxPriority() + 1;
        wifiConfiguration.priority = newPri;//提高优先级

        mWifiManager.updateNetwork(wifiConfiguration);//更新
        mWifiManager.saveConfiguration();//保存

        return this;

    }


    /**
     * 具体连接方法
     *
     * @return
     */
    private boolean onConnectWithSSID() {

        buildWifiConfiguration();

        mLock.lock();
        mIsConnnected = false;
        //连接该网络
        if (!mWifiManager.enableNetwork(mNetworkID, true)) {//enableNetwork(id,true)断开其他，false不断开
            mLock.unlock();
            return false;
        }

        /**
         * 即使已经存在，重新连接到当前活动的接入点连接的。 这可能导致异步传送状态改变事件。
         */
//        boolean reassociate = mWifiManager.reassociate();
//
//        if (!reassociate) {
//            mLock.unlock();
//            return false;
//        }

        try {
            //等待连接结果
            mCondition.await(Constants.WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();


        return mIsConnnected;
    }

    /**
     * 链接已连接过的WiFi
     *
     * @param wifiConnListener the wifi conn listener
     * @see com.gxw.wificonnhelperlib.utils.WifiConnListener
     */
    public void connectWithConfig(WifiConnListener wifiConnListener) {
        if (!isConnecting) {
            wifiAdmin.dissconnectAll();
            wifiConnListener.onWifiConnectStart(wifiConfiguration.SSID, wifiConfiguration.BSSID);
            isConnecting = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    wifiAdmin.openWifi();
//                    //注册连接结果监听对象
//                    mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

                    regReceive();

                    if (onConnectWithConfig()) {
                        wifiConnListener.onWifiConnectSuccess(wifiConfiguration.SSID, wifiConfiguration.BSSID);
                    } else {
                        wifiConnListener.onWifiConnnectFail(wifiConfiguration.SSID, wifiConfiguration.BSSID, reason);
                    }

                    removeReceive();

                    isConnecting = false;
                }
            }).start();
        } else {
            Toast.makeText(mContext, "正在连接请稍后", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 使用Config具体连接方法
     *
     * @return
     */
    private boolean onConnectWithConfig() {

        //添加网络配置
        int networkId1 = wifiConfiguration.networkId;
        mNetworkID = mWifiManager.addNetwork(wifiConfiguration);
        if (mNetworkID == -1) {
            mNetworkID = networkId1;
        }
        // Make it the highest priority.
        int newPri = wifiAdmin.getMaxPriority() + 1;
        wifiConfiguration.networkId = mNetworkID;
        wifiConfiguration.priority = newPri;//提高优先级
        mWifiManager.updateNetwork(wifiConfiguration);//更新
        mIsConnnected = false;
        mWifiManager.saveConfiguration();//保存
        mLock.lock();

        boolean b = mWifiManager.enableNetwork(mNetworkID, true);
        //连接该网络
        if (!b) {
            mLock.unlock();
            return false;
        }

//        boolean connect = mWifiManager.reassociate();
//        if (!connect) {
//            mLock.unlock();
//            return false;
//        }

        try {
            //等待连接结果
            mCondition.await(Constants.WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();


        return mIsConnnected;
    }

    /**
     * 监听系统的WIFI连接广播
     */
    private class WiFiConnectReceiver extends BroadcastReceiver {

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

    /**
     * 注册监听
     */
    private void regReceive() {

        if (mWifiConnectReceiver == null) {
            mWifiConnectReceiver = new WiFiConnectReceiver();
            //注册连接结果监听对象
            mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        }
    }

    /**
     * 移除广播
     */
    public void removeReceive() {
        //删除注册的监听类对象
        //http://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error
        try {
            if (mContext != null && mWifiConnectReceiver != null) {
//                mCondition.signalAll();
//                mLock.unlock();
                mContext.unregisterReceiver(mWifiConnectReceiver);
                mWifiConnectReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用内部API来连接wifi,这个就不能使用内部监听回调
     */
    public void connectByInnerMethod() {
        //构造 WifiConfiguration
        buildWifiConfiguration();
        //获得操作类的Class
        Class<? extends WifiManager> cls = mWifiManager.getClass();
        try {
            //获得方法
            Method connect = cls.getMethod("connect", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            /**
             * 执行方法，只是无法添加回调监听
             */
            connect.invoke(mWifiManager, wifiConfiguration, null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
