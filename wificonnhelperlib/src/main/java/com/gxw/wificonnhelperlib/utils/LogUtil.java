package com.gxw.wificonnhelperlib.utils;

import android.util.Log;

import com.gxw.wificonnhelperlib.BuildConfig;


/**
 * Created by GXW on 2016/12/1 0001.
 * email:603004002@qq.com
 */
public class LogUtil {

    private static boolean isShow = BuildConfig.LOGISSHOW;

    /**
     * Show info.
     *
     * @param tag
     *         the tag
     * @param msg
     *         the msg
     */
    public static void showInfo(String tag, String msg) {
        if (isShow) {
            Log.i(tag, msg);
        }
    }

    /**
     * Show debug.
     *
     * @param tag
     *         the tag
     * @param msg
     *         the msg
     */
    public static void showDebug(String tag, String msg) {
        if (isShow) {
            Log.d(tag, msg);
        }
    }

    /**
     * Show error.
     *
     * @param tag
     *         the tag
     * @param msg
     *         the msg
     */
    public static void showError(String tag, String msg) {
        if (isShow) {
            Log.e(tag, msg);
        }
    }

    /**
     * Show warn.
     *
     * @param tag
     *         the tag
     * @param msg
     *         the msg
     */
    public static void showWarn(String tag, String msg) {
        if (isShow) {
            Log.w(tag, msg);
        }
    }

}
