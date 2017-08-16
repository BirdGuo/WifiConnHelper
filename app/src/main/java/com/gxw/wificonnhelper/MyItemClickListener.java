package com.gxw.wificonnhelper;

import android.view.View;

/**
 * Created by guoxw on 2017/8/4 0004.
 *
 * @auther guoxw
 * @createTime 2017 /8/4 0004 15:15
 * @packageName com.gxw.wificonnectdemo.bean
 */
public interface MyItemClickListener {

    /**
     * On item click listener.
     *
     * @param view    the view
     * @param postion the postion
     */
    void onItemClickListener(View view, int postion);

    /**
     * On item long click listener.
     *
     * @param view    the view
     * @param postion the postion
     */
    void onItemLongClickListener(View view, int postion);

}
