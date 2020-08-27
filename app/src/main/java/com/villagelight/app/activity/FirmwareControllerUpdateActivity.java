package com.villagelight.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.DataEvent;
import com.villagelight.app.event.UpdateFailedEvent;
import com.villagelight.app.model.SendBean;
import com.villagelight.app.util.ToastUtils;
import com.villagelight.app.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.RFStarBLEService;
import cn.fly2think.blelib.TransUtils;
import de.greenrobot.event.EventBus;

public class FirmwareControllerUpdateActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    private File binFile;
    private short version;
    private int local;
    private List<Byte> mRecvs = new ArrayList<>();
    private int mLen;
    private List<SendBean> mSends = new ArrayList<>();
    private Handler mHandler = new Handler();
    private int upgradeCount = 0;
    private int checkCount;
    private DialogFragment disconnectDialogFragment;

    private Runnable resendRunnable = new Runnable() {
        @Override
        public void run() {
            upgradeCount++;
            if (upgradeCount < 3) {
                sendData(mSends.get(0).getDatas());
                mHandler.removeCallbacks(resendRunnable);
                mHandler.postDelayed(resendRunnable, TIMEOUT);
            } else {
                updateFailed("[Timeout]");
            }
        }
    };

    private Runnable checkFirmware = new Runnable() {
        @Override
        public void run() {

            if (checkCount < 3) {
                //1.查询属性
                byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                        (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                sendPackets(Utils.getSendData(query));
                mHandler.postDelayed(this, 5000);
            } else {
                updateFailed("[Timeout]");
            }

            checkCount++;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_firmware_update_controller);
        ButterKnife.bind(this);

        binFile = (File) getIntent().getSerializableExtra("bin");
        version = getIntent().getShortExtra("version", (short) 0);
        local = getIntent().getIntExtra("local", 0);
        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Update Controller Firmware");

        requestMtu();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(resendRunnable);
    }

    @OnClick({R.id.btn_title_left, R.id.btn_update_firmware, R.id.tv_help})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_update_firmware:

                checkCount = 0;

                mSends.clear();
                byte firstSer = (byte) ProjectApp.getInstance().getSerialNumber();

                showDownloadDialog("Updating Firmware",
                        "This update will take several minutes. Don’t close the app or disconnect power to the controller.",
                        "Installing Version:V" + version);

                FileInputStream is = null;

                try {
                    is = new FileInputStream(binFile);
                    byte[] buffer = new byte[28];
                    int len = -1;
                    int sum = 0;
                    int packet = 0;
                    while ((len = is.read(buffer)) != -1) {

                        for (int i = 0; i < len; i++) {
                            sum += buffer[i] & 0xFF;
                        }

                        packet++;
                        byte[] packets = TransUtils.short2bytes((short) packet);
                        byte ser = (byte) ProjectApp.getInstance().getSerialNumber();

                        byte[] heads = {(byte) 0xAA, 0x62, 0x00, ser, 0x00, 0x00, packets[0], packets[1]};
                        byte[] tails = {0x00, 0x55};
                        byte[] sends = new byte[heads.length + len + tails.length];
                        System.arraycopy(heads, 0, sends, 0, heads.length);
                        System.arraycopy(buffer, 0, sends, heads.length, len);
                        System.arraycopy(tails, 0, sends, heads.length + len, tails.length);

                        sends = Utils.getSendData(sends);

                        mSends.add(new SendBean(sends));


                    }

                    is.close();

                    byte[] versions = TransUtils.short2bytes(version);
                    byte[] lengths = TransUtils.int2bytes((int) binFile.length());
                    byte[] sums = TransUtils.int2bytes(sum);

                    byte[] firsts = {(byte) 0xAA, 0x62, 0x00, firstSer, 0x00, 0x00,
                            0x00, 0x00, versions[0], versions[1], lengths[0], lengths[1], lengths[2], lengths[3],
                            sums[0], sums[1], sums[2], sums[3], 0x00, 0x55};
                    firsts = Utils.getSendData(firsts);

                    mSends.add(0, new SendBean(firsts));

                    //发送固件第1包
                    if (sendData(mSends.get(0).getDatas())) {
                        upgradeCount = 0;
                        mHandler.removeCallbacks(resendRunnable);
                        mHandler.postDelayed(resendRunnable, TIMEOUT);
                    } else {
                        dismissDialog();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dismissDialog();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                break;

            case R.id.tv_help:
                Uri uri = Uri.parse(Constants.URL_HELP);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }

    private boolean sendData(byte[] command) {

        // Check that we're actually connected before trying anything
        if (!isConnected()) {
            ToastUtils.showToast(mContext, "No connection.");
            return false;
        }

        sendPackets(command, 40);

        return true;

    }


    private void forwardData(byte[] datas) {

        if (datas[1] == (byte) 0xE2) {//串口模式发送数据-升级


            if (datas[6] == 0x00) {//成功

                if (mSends.get(0).getDatas()[3] == datas[3]) {//判断回复的流水号和发送的流水号一致才继续发送下一条数据

                    mSends.remove(0);
                    mHandler.removeCallbacks(resendRunnable);

                    if (!mSends.isEmpty()) {

                        sendData(mSends.get(0).getDatas());
                        upgradeCount = 0;
                        mHandler.postDelayed(resendRunnable, TIMEOUT);

                    } else {

                        //5秒后查询下位机固件版本
                        mHandler.removeCallbacks(resendRunnable);
                        mHandler.removeCallbacks(checkFirmware);
                        mHandler.postDelayed(checkFirmware, 5000);
                    }
                }

            } else if (datas[6] == 0x01) {//失败

                updateFailed("[Failed]");

            } else if (datas[6] == 0x02) {//忙碌

                updateFailed("[Busy]");

            } else if (datas[6] == 0x03) {//下位机返回固件升级成功

//                updateSucceed();
                //5秒后查询下位机固件版本
                mHandler.removeCallbacks(resendRunnable);
                mHandler.removeCallbacks(checkFirmware);
                mHandler.postDelayed(checkFirmware, 5000);

            } else {//保留

                updateFailed("[Error]");

            }
        } else if (datas[1] == (byte) 0x81) {

            mHandler.removeCallbacks(checkFirmware);

            /**
             *读取设备属性
             AA 81 24 B9 00 00
             00 02 00 01 -- 协议版本
             02 02 00 00 -- 产品类型
             03 02 00 0A -- 固件版本
             05 04 35 EA 54 96 -- 设备地址
             06 07 52 57 5F 47 57 41 59 -- 设备名称
             08 02 06 00 -- 设备数量
             57 55
             */

            byte[] versions = {datas[16], datas[17]};
            short localVersion = TransUtils.bytes2short(versions);
            Constants.FirmwareVersion = localVersion;

            if (version == localVersion) {
                updateSucceed();
            } else {
                updateUnsuccessful();
            }

        }
    }


    // ble状态以及数据回调接口
    public void onEventMainThread(BroadcastEvent event) {
        Intent intent = event.getIntent();
        String action = intent.getAction();
        if (RFStarBLEService.ACTION_GATT_CONNECTED.equals(action)) {

            mRecvs.clear();

        } else if (RFStarBLEService.ACTION_DATA_AVAILABLE.equals(action)) {

            byte[] datas = intent
                    .getByteArrayExtra(RFStarBLEService.EXTRA_DATA);

            for (byte b : datas) {
                mRecvs.add(b);
                if (mRecvs.size() == 1) {
                    if (mRecvs.get(0) != (byte) 0xAA) {
                        mRecvs.clear();
                    }
                } else if (mRecvs.size() == 3) {

                    mLen = mRecvs.get(2) & 0xFF;

                } else if (mRecvs.size() == (mLen + 3)) {

                    if (mRecvs.get(mRecvs.size() - 1) == (byte) 0x55) {
                        byte[] results = TransUtils.toPrimitive(mRecvs);
                        if (Utils.getXor(results) == results[results.length - 2]) {

                            forwardData(results);
                        }
                    }

                    mRecvs.clear();
                }
            }

        }
    }

    private void back2list() {

        disconnect();
        dismissDialog();
        EventBus.getDefault().post(new UpdateFailedEvent());
    }

    private void updateSucceed() {

        dismissDialog();

        if (disconnectDialogFragment != null) {
            disconnectDialogFragment.dismiss();
        }

        disconnectDialogFragment =
                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .setTitle("Firmware Updated")
                        .setText("The firmware update was successfully installed.")
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                finish();

                            }
                        })
                        .show(getSupportFragmentManager());
    }

    private void updateFailed(String reason) {

        dismissDialog();

        if (disconnectDialogFragment != null) {
            disconnectDialogFragment.dismiss();
        }

        disconnectDialogFragment =
                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .setTitle("Firmware Error")
                        .setText("Sorry, there was a problem installing the firmware. Please power cycle the controller then reconnect to the app.  \n" +
                                "\n" +
                                "Follow the instructions when the app reconnects to install the firmware." + reason)
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                back2list();
                            }
                        })
                        .show(getSupportFragmentManager());
    }

    private void updateUnsuccessful() {

        dismissDialog();

        if (disconnectDialogFragment != null) {
            disconnectDialogFragment.dismiss();
        }

        disconnectDialogFragment =
                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .setTitle("Firmware Update Error")
                        .setText("The firmware update didn't complete.Please try again.")
                        .setPositive("OK", null)
                        .show(getSupportFragmentManager());
    }
}
