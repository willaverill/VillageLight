package com.villagelight.app.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.DataEvent;
import com.villagelight.app.util.LogUtils;
import com.villagelight.app.util.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.AppManager;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.CubicBLEDevice;
import cn.fly2think.blelib.RFStarBLEService;

public class PairControllerManualActivity extends BaseActivity implements AppManager.RFStarManageListener {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.et_id)
    EditText mEtId;
    @BindView(R.id.btn_continue)
    Button mBtnContinue;
    private String mDevName;
    private String mPassword;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            LogUtils.d("10s timeout");
            cancelDiscovery();
            pairFailed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pair);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Controller Setup");

        app.manager.setRFstarBLEManagerListener(this);

//        mEtId.setText("Lewen03_ble#000000");
    }

    @OnClick({R.id.btn_title_left, R.id.btn_cancel, R.id.btn_continue})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_cancel:
                try {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_continue:

                String id = mEtId.getText().toString().trim();

                String[] ids = id.split("#");

                if (ids.length != 2) {
                    ToastUtils.showToast(mContext, "Controller ID format is not correct.");
                    return;
                }

                mDevName = ids[0];
                mPassword = ids[1];

                mBtnContinue.removeCallbacks(mRunnable);
                mBtnContinue.postDelayed(mRunnable, Constants.SCAN_DEVICE_TIMEOUT);
                doDiscovery();

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBtnContinue.removeCallbacks(mRunnable);
        cancelDiscovery();
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        disconnect();
        app.manager.startScanBluetoothDevice();
        LogUtils.d("doDiscovery");
    }

    private void cancelDiscovery() {
        app.manager.stopScanBluetoothDevice();
        LogUtils.d("cancelDiscovery");
    }

    /*
     * ble扫描回调
     */
    @Override
    public void RFstarBLEManageListener(BluetoothDevice device, int rssi,
                                        byte[] scanRecord) {

        String devName = device.getName();
        LogUtils.d("found device:" + devName + ",input device:" + mDevName);
        if (!TextUtils.isEmpty(devName) && devName.trim().equals(mDevName)) {

            cancelDiscovery();

            if (isConnected()) {
                disconnect();
            }

            // 连接设备
            app.manager.bluetoothDevice = device;
            app.manager.cubicBLEDevice = new CubicBLEDevice(
                    app.getApplicationContext(),
                    app.manager.bluetoothDevice);

        }

    }

    /*
     * ble开始扫描
     */
    @Override
    public void RFstarBLEManageStartScan() {
        showDialog("", "Pairing...");
    }

    /*
     * ble结束扫描
     */
    @Override
    public void RFstarBLEManageStopScan() {

        dismissDialog();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024) {
            Logger.d("resultCode:" + resultCode);
            if (resultCode == RESULT_FIRST_USER) {

            } else if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                finish();

            } else if (resultCode == RESULT_OK) {

                setResult(RESULT_OK,data);
                finish();

            }
        }
    }

    private void pairSucceed(BluetoothDevice device) {

        Intent intent = new Intent(mContext, PairSucceedActivity.class);
        intent.putExtra("device", device);
        intent.putExtra("password", mPassword);
        startActivityForResult(intent, 1024);

    }

    private void pairFailed() {

        Intent intent = new Intent(mContext, PairFailedActivity.class);
        intent.putExtra("title", "Controller Setup");
        intent.putExtra("tips", "Something went wrong, try pairing your controller again.");
        intent.putExtra("error", R.mipmap.pair_failed_controller);
        startActivityForResult(intent, 1024);
    }

    // ble状态以及数据回调接口
    public void onEventMainThread(BroadcastEvent event) {
        Intent intent = event.getIntent();
        String action = intent.getAction();
        if (RFStarBLEService.ACTION_GATT_SERVICES_DISCOVERED
                .equals(action)) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (isConnected()) {
                        app.manager.setWriteUUID("00001005-0000-1000-8000-00805f9b34fb");
                        app.manager.cubicBLEDevice.writeValue(new byte[]{(byte) 0x83});
                        app.manager.setWriteUUID("00001003-0000-1000-8000-00805f9b34fb");
                        if (!TextUtils.isEmpty(mPassword)) {
                            app.manager.cubicBLEDevice.writeValue(mPassword.getBytes());
                        }
                        app.manager.setWriteUUID("00001001-0000-1000-8000-00805f9b34fb");
                    }


                }
            }, 100);

            mBtnContinue.removeCallbacks(mRunnable);
            if (ProjectApp.getInstance().getDuplicateController() == null) {

                pairSucceed(app.manager.bluetoothDevice);

            } else {

                showDialog("", "Duplicating data...");
                getSyncDatas();

                if (syncDatas.isEmpty()) {

                    pairSucceed(app.manager.bluetoothDevice);

                } else {
                    sHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            sendPackets(syncDatas.get(0).getDatas(), 100);
                            sHandler.removeCallbacks(sTimeoutRunnable);
                            sHandler.postDelayed(sTimeoutRunnable, TIMEOUT);
                        }
                    }, 200);
                }

            }

        } else if (RFStarBLEService.ACTION_GATT_FAILED.equals(action)) {
            doDiscovery();
        }
    }

    public void onEventMainThread(DataEvent event) {

        byte[] datas = event.getDatas();

        if (datas[1] == (byte) 0x91 || datas[1] == (byte) 0xB1) {

            sHandler.removeCallbacks(sTimeoutRunnable);

            if (datas[6] == 0x00) {//成功

                if (!syncDatas.isEmpty() && datas[3] == syncDatas.get(0).getDatas()[3]) {
                    syncDatas.remove(0);
                }

                if (syncDatas.isEmpty()) {

                    dismissDialog();
                    pairSucceed(app.manager.bluetoothDevice);

                } else {

                    sendPackets(syncDatas.get(0).getDatas(), 100);

                    sHandler.postDelayed(sTimeoutRunnable, TIMEOUT);
                }

            } else {

                showNormalDialog("Duplicate failed", "An unknown error occurred.");
            }
        }
    }
}
