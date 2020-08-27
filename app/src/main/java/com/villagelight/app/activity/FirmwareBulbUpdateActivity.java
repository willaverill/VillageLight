package com.villagelight.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.TextParams;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.PairEvent;
import com.villagelight.app.model.SendBean;
import com.villagelight.app.util.LogUtils;
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

public class FirmwareBulbUpdateActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    private File binFile;
    private short version;
    private List<Byte> mRecvs = new ArrayList<>();
    private int mLen;
    private List<SendBean> mSends = new ArrayList<>();
    private Handler mHandler = new Handler();
    private int count = 0;

    private boolean isPair = false;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            stopPair();
        }
    };

    private Runnable resendRunnable = new Runnable() {
        @Override
        public void run() {

            count++;
            if (count < 3) {
                sendData(mSends.get(0).getDatas());
                mHandler.removeCallbacks(resendRunnable);
                mHandler.postDelayed(resendRunnable, TIMEOUT);
            } else {

                dismissDialog();

                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                            }
                        })
                        .setTitle("Firmware Error")
                        .setText("Sorry, there was a problem installing the firmware. Please power cycle the controller then reconnect to the app.  \n" +
                                "\n" +
                                "Follow the instructions when the app reconnects to install the firmware.[Timeout]")
                        .configText(new ConfigText() {
                            @Override
                            public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                            }
                        })
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                back2list();
                            }
                        })
                        .configPositive(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                            }
                        })
                        .show(getSupportFragmentManager());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_firmware_update_bulb);
        ButterKnife.bind(this);

        binFile = (File) getIntent().getSerializableExtra("bin");
        version = getIntent().getShortExtra("version", (short) 0);
        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Update Bulb Firmware");

        requestMtu();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(resendRunnable);
        mHandler.removeCallbacks(mRunnable);
    }

    @OnClick({R.id.btn_title_left, R.id.btn_update_firmware})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_update_firmware:

                mSends.clear();
                byte firstSer = (byte) ProjectApp.getInstance().getSerialNumber();

                showDownloadDialog("Downloading Bulb Firmware",
                        "Please wait while the update is down- loaded into the controller.\n\n" +
                                "Don’t close the app or disconnect power to the controller.",
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

                        byte[] heads = {(byte) 0xAA, 0x62, 0x00, ser, 0x00, 0x01, packets[0], packets[1]};
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

                    byte[] firsts = {(byte) 0xAA, 0x62, 0x00, firstSer, 0x00, 0x01,
                            0x00, 0x00, versions[0], versions[1], lengths[0], lengths[1], lengths[2], lengths[3],
                            sums[0], sums[1], sums[2], sums[3], 0x42, 0x00, 0x55};//0x42灯泡
                    firsts = Utils.getSendData(firsts);

                    mSends.add(0, new SendBean(firsts));

                    //发送固件第1包
                    if (sendData(mSends.get(0).getDatas())) {
                        count = 0;
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
        }
    }

    private boolean sendData(byte[] command) {

        // Check that we're actually connected before trying anything
        if (!isConnected()) {
            ToastUtils.showToast(mContext, "No connection.");
            return false;
        }

        sendPackets(command, 100);

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
                        count = 0;
                        mHandler.postDelayed(resendRunnable, TIMEOUT);

                    } else {
                        mHandler.removeCallbacks(resendRunnable);
                        dismissDialog();

                        showPowerCycleDialog();

                    }
                }

            } else if (datas[6] == 0x01) {//失败

                dismissDialog();
                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                            }
                        })
                        .setTitle("Firmware Error")
                        .setText("Sorry, there was a problem installing the firmware. Please power cycle the controller then reconnect to the app.  \n" +
                                "\n" +
                                "Follow the instructions when the app reconnects to install the firmware.")
                        .configText(new ConfigText() {
                            @Override
                            public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                            }
                        })
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                back2list();
                            }
                        })
                        .configPositive(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                            }
                        })
                        .show(getSupportFragmentManager());

            } else if (datas[6] == 0x02) {//忙碌

                dismissDialog();
                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                            }
                        })
                        .setTitle("Firmware Error")
                        .setText("Sorry, there was a problem installing the firmware. Please power cycle the controller then reconnect to the app.  \n" +
                                "\n" +
                                "Follow the instructions when the app reconnects to install the firmware.[Busy]")
                        .configText(new ConfigText() {
                            @Override
                            public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                            }
                        })
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                back2list();
                            }
                        })
                        .configPositive(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                            }
                        })
                        .show(getSupportFragmentManager());

            } else {//保留

                dismissDialog();
                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                            }
                        })
                        .setTitle("Firmware Error")
                        .setText("Sorry, there was a problem installing the firmware. Please power cycle the controller then reconnect to the app.  \n" +
                                "\n" +
                                "Follow the instructions when the app reconnects to install the firmware.[Error]")
                        .configText(new ConfigText() {
                            @Override
                            public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                            }
                        })
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                back2list();
                            }
                        })
                        .configPositive(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                            }
                        })
                        .show(getSupportFragmentManager());

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

    private void startPair() {

        dismissDialog();
        isPair = true;
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, Constants.PAIR_TIMEOUT);

        showPairDialog(getString(R.string.pair_switches_title), getString(R.string.pair_switches_tips),
                0, getString(R.string.pair_device_end_button), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopPair();
                    }
                });

        byte[] cmdsPairStart = {(byte) 0xAA, 0x05, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x01, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsPairStart));
    }

    private void stopPair() {

        dismissDialog();
        LogUtils.d("stopPair...");

        mHandler.removeCallbacks(mRunnable);
        isPair = false;

        byte[] cmdsPairStop = {(byte) 0xAA, 0x05, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsPairStop));

        startActivityForResult(new Intent(mContext, PairDeviceSucceedActivity.class), 1024);
    }


    public void onEventMainThread(PairEvent event) {

        if (event.getBulbs() == -1 && event.getSwitches() == -1) {

        } else {

            if (!isPair) {
                return;
            }

            showPairDialog(getString(R.string.pair_device_title), getString(R.string.pair_device_tips),
                    event.getSwitches(), getString(R.string.pair_device_end_button), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopPair();
                        }
                    });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024) {

            if (resultCode == RESULT_FIRST_USER) {
                finish();
            } else if (resultCode == RESULT_OK) {
                startPair();
            }

        }
    }

    private void back2list() {

        dismissDialog();
//        EventBus.getDefault().post(new UpdateFailedEvent());
        finish();
    }


    private void showPowerCycleDialog() {

        String tips = "End the update when all bulbs have turned light green.\n" +
                "\n" +
                "Bulbs out of range may not recieve the update. Move controller and bulbs to closer proximity and try again.\n" +
                "\n" +
                "Pair bulbs again after update.\n";


        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle("Now Broadcasting Update")
                .setText(tips)
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setPositive("End Update", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        byte[] cmds = {(byte) 0xAA, 0x07, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                (byte) 0x00, (byte) 0x01, 0x00, 0x55};
                        sendPackets(Utils.getSendData(cmds));
                        finish();

                    }
                })
                .configPositive(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                    }
                })
                .show(getSupportFragmentManager());

    }


}
