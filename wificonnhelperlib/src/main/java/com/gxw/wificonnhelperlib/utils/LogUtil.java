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
