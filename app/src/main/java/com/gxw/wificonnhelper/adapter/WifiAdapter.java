package com.gxw.wificonnhelper.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gxw.wificonnhelper.MyItemClickListener;
import com.gxw.wificonnhelper.R;
import com.gxw.wificonnhelperlib.utils.bean.WifiBean;

import java.util.ArrayList;


/**
 * Created by guoxw on 2017/8/4 0004.
 *
 * @auther guoxw
 * @createTime 2017/8/4 0004 15:11
 * @packageName com.gxw.wificonnectdemo.adapter
 */

public class WifiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<WifiBean> wifiBeens;
    private MyItemClickListener myItemClickListener;

    public WifiAdapter(Context context, ArrayList<WifiBean> wifiBeens) {
        this.context = context;
        this.wifiBeens = wifiBeens;
    }

    public void setMyItemClickListener(MyItemClickListener myItemClickListener) {
        this.myItemClickListener = myItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wifi, null);
        return new MyHolder(view, myItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder) {
            WifiBean wifiBean = wifiBeens.get(position);
            ScanResult result = wifiBean.getScanResult();
            ((MyHolder) holder).tv_item_name.setText(result.SSID);
            ((MyHolder) holder).tv_item_bssid.setText(result.BSSID);
            ((MyHolder) holder).tv_item_type.setText(result.capabilities);
            int frequency = result.frequency;

            int level = WifiManager.calculateSignalLevel(result.level, 100);
            ((MyHolder) holder).tv_item_level.setText("" + level);

            if (frequency < 2500 && frequency > 2400) {
                ((MyHolder) holder).tv_item_frequency.setText("2.4G");
            } else if (frequency < 6000 && frequency > 5000) {
                ((MyHolder) holder).tv_item_frequency.setText("5G");
            } else {
                ((MyHolder) holder).tv_item_frequency.setText("" + frequency);
            }

            if (wifiBean.isConnect()) {
                ((MyHolder) holder).tv_item_name.setTextColor(Color.RED);
            } else {
                ((MyHolder) holder).tv_item_name.setTextColor(Color.BLACK);
            }

        }

    }

    @Override
    public int getItemCount() {
        return wifiBeens.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private MyItemClickListener myItemClickListener;

        private TextView tv_item_name, tv_item_frequency, tv_item_level, tv_item_type, tv_item_bssid;

        public MyHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            tv_item_name = itemView.findViewById(R.id.tv_item_name);
            tv_item_frequency = itemView.findViewById(R.id.tv_item_frequency);
            tv_item_level = itemView.findViewById(R.id.tv_item_level);
            tv_item_type = itemView.findViewById(R.id.tv_item_type);
            tv_item_bssid = itemView.findViewById(R.id.tv_item_bssid);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            myItemClickListener.onItemClickListener(view, getAdapterPosition());
        }

    }

}
