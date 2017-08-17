package com.gxw.wificonnhelper;

import android.app.AlertDialog;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gxw.wificonnhelper.adapter.WifiAdapter;
import com.gxw.wificonnhelperlib.utils.LogUtil;
import com.gxw.wificonnhelperlib.utils.WifiAdmin;
import com.gxw.wificonnhelperlib.utils.WifiConnListener;
import com.gxw.wificonnhelperlib.utils.WifiConnectorNew;
import com.gxw.wificonnhelperlib.utils.bean.WifiBean;
import com.gxw.wificonnhelperlib.utils.bean.WifiBeanConn;

import java.util.ArrayList;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity implements MyItemClickListener, WifiConnListener {

    private String TAG = MainActivity.class.getName().toString();

    private ArrayList<WifiBean> wifiBeens, wifiBeensTemp;
    private ScanResult scanResultNeedToConn = null;

    private WifiAdapter wifiAdapter;

    private RecyclerView rcv_main;
    private View dialogView;
    private TextView tv_dialog_ssid;
    private EditText et_dialog;
    private CheckBox cb_dialog;
    private Button btn_dialog_cancel, btn_dialog_sure;
    private AlertDialog dialogPWD;

    //    private WifiConnector wifiConnector;
    private WifiAdmin wifiAdmin;
    private WifiConnectorNew build;

    private boolean runScan = true;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0x0001:
                    wifiBeens.clear();
                    wifiBeens.addAll(wifiBeensTemp);
                    wifiAdapter.notifyDataSetChanged();

                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initData();
        initView();
        initThread();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        rcv_main = (RecyclerView) findViewById(R.id.rcv_main);
        wifiAdapter = new WifiAdapter(this, wifiBeens);
        wifiAdapter.setMyItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rcv_main.setLayoutManager(linearLayoutManager);
        rcv_main.setAdapter(wifiAdapter);

        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_pwd, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        dialogPWD = builder.create();

        tv_dialog_ssid = (TextView) dialogView.findViewById(R.id.tv_dialog_ssid);
        et_dialog = (EditText) dialogView.findViewById(R.id.et_dialog);
        btn_dialog_cancel = (Button) dialogView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_sure = (Button) dialogView.findViewById(R.id.btn_dialog_sure);
        cb_dialog = (CheckBox) dialogView.findViewById(R.id.cb_dialog);

        cb_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cb_dialog.isChecked()) {
                    et_dialog.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    et_dialog.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPWD.dismiss();
                scanResultNeedToConn = null;
            }
        });

        btn_dialog_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pwd = et_dialog.getText().toString();
                if (!TextUtils.isEmpty(pwd) && pwd.length() > 7 && scanResultNeedToConn != null) {
                    dialogPWD.dismiss();
//                    wifiConnector.connect(scanResultNeedToConn.SSID, pwd, scanResultNeedToConn.BSSID, wifiAdmin.secretMode(scanResultNeedToConn));

                    build.addWifiBeanConn(new WifiBeanConn(pwd, scanResultNeedToConn)).connectWithSSID(MainActivity.this);

                    et_dialog.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "密码必须大于等于8位", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void initData() {

        wifiAdmin = new WifiAdmin(this);
        //打开WiFi
        wifiAdmin.openWifi();
//        wifiConnector = new WifiConnector(this, this);
        wifiBeens = new ArrayList<>();
        wifiBeensTemp = new ArrayList<>();
        buildWifiBean();
        wifiBeens.addAll(wifiBeensTemp);

        build = new WifiConnectorNew(this);


    }

    private void initThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runScan) {
                    try {
                        reflashWifiList();
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    /**
     * 刷新wifi列表
     */
    private void reflashWifiList() {
//        scanResultsTemp = wifiAdmin.getWifiListAll();
        buildWifiBean();
        handler.sendEmptyMessage(0x0001);
    }

    private void buildWifiBean() {
        wifiBeensTemp.clear();
        wifiAdmin.startScan();
        String connectWifiSSID = wifiAdmin.getConnectWifiSSID(this);
        ArrayList<ScanResult> wifiListAll = wifiAdmin.getWifiListAll();
        for (ScanResult scanResult : wifiListAll) {
            if (connectWifiSSID != null && connectWifiSSID.equalsIgnoreCase(scanResult.SSID)) {
                wifiBeensTemp.add(new WifiBean(true, scanResult));
            } else {
                wifiBeensTemp.add(new WifiBean(false, scanResult));
            }
        }
    }

    @Override
    public void onItemClickListener(View view, int postion) {

        Log.i(TAG, wifiBeens.size() + "postion:" + postion);

        if (postion > 0 && postion < (wifiBeens.size() - 1)) {
            ScanResult result = wifiBeens.get(postion).getScanResult();

            LogUtil.showInfo(TAG, "连接到：" + result.SSID);

            String ssid = result.SSID;
//        tv_dialog_ssid.setText("连接到"+result.SSID);
//        dialogPWD.show();

            String connectWifiSSID = wifiAdmin.getConnectWifiSSID(view.getContext());
            if (ssid.equalsIgnoreCase(connectWifiSSID)) {//已连接到当前wifi
                Toast.makeText(view.getContext(), "已连接到当前wifi", Toast.LENGTH_LONG).show();
            } else {//未连接

//            WifiConfiguration exsits = wifiConnector.isExsits(ssid);
                WifiConfiguration exsits = wifiAdmin.isExsits(ssid);
                if (exsits != null) {//曾经连接过
//                wifiConnector.connect(exsits);//连接
                    build.addWifiConfig(exsits).connectWithConfig(this);
                } else {//未连接过

                    //判断是否有密码
                    if (wifiAdmin.hasPwd(result)) {
                        //标识有密码
                        tv_dialog_ssid.setText("连接到" + result.SSID);
                        scanResultNeedToConn = result;

                        if (!MainActivity.this.isFinishing()) {
                            dialogPWD.show();
                        }
                    } else {//无密码
//                    wifiConnector.connect(result.SSID, "", result.BSSID, wifiAdmin.secretMode(result));
                        build.addWifiBeanConn(new WifiBeanConn("", result)).connectWithSSID(this);
                    }
                }

            }
        }

    }

    @Override
    public void onItemLongClickListener(View view, int postion) {

    }

    @Override
    public void onWifiConnectStart(String ssid, String bssid) {
        LogUtil.showInfo(TAG, ssid + " 正在连接 ");
//        reflashWifiList();

    }

    @Override
    public void onWifiConnectSuccess(String ssid, String bssid) {
//        Toast.makeText(this, "连接成功", Toast.LENGTH_LONG).show();
        LogUtil.showInfo(TAG, ssid + " 连接成功 ");
        reflashWifiList();

    }

    @Override
    public void onWifiConnnectFail(String ssid, String bssid, int reason) {
//        Toast.makeText(this, "连接失败", Toast.LENGTH_LONG).show();
        LogUtil.showInfo(TAG, ssid + " 连接失败 " + reason);
        reflashWifiList();
    }

//    @Override
//    public void OnWifiConnectCompleted(boolean isConnected, String ssid, String bssid, String password, int reason) {
//
//        LogUtil.showInfo(TAG, ssid + " " + isConnected + " " + reason);
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runScan = false;
        LogUtil.showInfo(TAG, "------------------onDestroy-------------");
//        WifiConnectorNew.removeReceive();
        build.removeReceive();
    }
}
