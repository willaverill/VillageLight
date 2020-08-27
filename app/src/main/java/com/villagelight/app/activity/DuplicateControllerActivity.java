package com.villagelight.app.activity;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.TextParams;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.adapter.ControllerDuplicateAdapter;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.DataEvent;
import com.villagelight.app.model.ChannelBean;
import com.villagelight.app.model.ColorBean;
import com.villagelight.app.model.ControllerBean;
import com.villagelight.app.model.CustomSchedule;
import com.villagelight.app.model.SimpleSchedule;
import com.villagelight.app.model.ThemeColor;
import com.villagelight.app.util.LogUtils;
import com.villagelight.app.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.CubicBLEDevice;
import cn.fly2think.blelib.RFStarBLEService;
import cn.fly2think.blelib.TransUtils;

public class DuplicateControllerActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.lv)
    ListView mLv;
    @BindView(R.id.separator)
    View mSeparator;
    private ControllerDuplicateAdapter mAdapter;
    private ControllerBean duplicateController;
    private Handler mHandler = new Handler();
    private ArrayList<ColorBean> colors1 = new ArrayList<>();
    private ArrayList<ColorBean> colors2 = new ArrayList<>();
    private ArrayList<ColorBean> colors3 = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
    private int themeAmount;
    private ThemeColor themeColor;
    private int count;
    private boolean canNewTheme = true;
    private boolean isDuplicateOnThisActivity;
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {

            isDuplicateOnThisActivity = false;

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
                    .setTitle("WARNING")
                    .setText("Timeout, please try again.")
                    .configText(new ConfigText() {
                        @Override
                        public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                        }
                    })
                    .setPositive("OK", null)
                    .configPositive(new ConfigButton() {
                        @Override
                        public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                        }
                    })
                    .show(getSupportFragmentManager());

        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            go();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplicate);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Controller Setup");

        initColors();

        List<ControllerBean> list = null;
        try {
            list = ProjectApp.getInstance().getDb().findAll(Selector.from(ControllerBean.class));
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (list == null || list.isEmpty()) {
            mSeparator.setVisibility(View.INVISIBLE);
        }

        mAdapter = new ControllerDuplicateAdapter(mContext, list);
        mLv.setAdapter(mAdapter);

        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                isDuplicateOnThisActivity = true;

                duplicateController = (ControllerBean) parent.getAdapter().getItem(position);

                if (isConnected() && duplicateController.getDeviceMac().equals(app.manager.cubicBLEDevice.deviceMac)) {

                    readTheme();

                } else {

                    showDialog("", "Connecting...");
                    disconnect();

                    BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

                    // 连接设备
                    app.manager.bluetoothDevice = bluetoothManager.getAdapter().getRemoteDevice(duplicateController.getDeviceMac());
                    app.manager.cubicBLEDevice = new CubicBLEDevice(
                            app.getApplicationContext(),
                            app.manager.bluetoothDevice);
                }


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(timeoutRunnable);
        mHandler.removeCallbacks(mRunnable);
        ProjectApp.getInstance().setDuplicateController(null);
        ProjectApp.getInstance().getDuplThemeColors().clear();
        ProjectApp.getInstance().getDuplSimpleSchedule().clear();
        ProjectApp.getInstance().getDuplCustomSchedule().clear();
    }


    @OnClick({R.id.btn_title_left})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024) {
            if (resultCode == RESULT_FIRST_USER) {

            } else if (resultCode == RESULT_CANCELED) {

                finish();

            } else if (resultCode == RESULT_OK) {

                finish();
            }
        }
    }

    // ble状态以及数据回调接口
    public void onEventMainThread(BroadcastEvent event) {

        if (!isDuplicateOnThisActivity) {
            return;
        }

        Intent intent = event.getIntent();
        String action = intent.getAction();

        if (RFStarBLEService.ACTION_GATT_FAILED.equals(action)) {

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
                    .setTitle("WARNING")
                    .setText("The connection to this controller failed.  Please make sure your controller is plugged in and within range and try again.")
                    .configText(new ConfigText() {
                        @Override
                        public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                        }
                    })
                    .setPositive("OK", null)
                    .configPositive(new ConfigButton() {
                        @Override
                        public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                        }
                    })
                    .show(getSupportFragmentManager());

        } else if (RFStarBLEService.ACTION_GATT_SERVICES_DISCOVERED
                .equals(action)) {

            dismissDialog();

            if (app.manager.cubicBLEDevice != null) {
                app.manager.cubicBLEDevice.setNotification(true);
            }


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    app.manager.setWriteUUID("00001005-0000-1000-8000-00805f9b34fb");
                    app.manager.cubicBLEDevice.writeValue(new byte[]{(byte) 0x83});
                    app.manager.setWriteUUID("00001003-0000-1000-8000-00805f9b34fb");
                    if (!TextUtils.isEmpty(duplicateController.getPassword())) {
                        app.manager.cubicBLEDevice.writeValue(duplicateController.getPassword().getBytes());
                    }
                    if (app.isDebug()) {
                        app.manager.setWriteUUID("0000fff1-0000-1000-8000-00805f9b34fb");
                    } else {
                        app.manager.setWriteUUID("00001001-0000-1000-8000-00805f9b34fb");
                    }


                }
            }, 100);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    readTheme();

                }
            }, 200);


        }
    }

    private void readTheme() {

        ProjectApp.getInstance().getDuplThemeColors().clear();
        ProjectApp.getInstance().getDuplSimpleSchedule().clear();
        ProjectApp.getInstance().getDuplCustomSchedule().clear();

        themeAmount = 0;
        themeColor = null;
        count = 0;
        canNewTheme = true;

        //查询theme，当查询 ID 号为 0xFF 返回 表示获取设备所有的 ID 编号，主要功能:告诉查询者设备当前有哪些 ID 编号。
        byte[] query = {(byte) 0xAA, 0x10, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, (byte) 0xff, 0x00, 0x55};
        sendPackets(Utils.getSendData(query));

        showDialog("", "Reading data from " + duplicateController.getControllerName() + "...");

        mHandler.removeCallbacks(timeoutRunnable);
        mHandler.postDelayed(timeoutRunnable, Constants.DUPLICATE_READ_TIMEOUT);
    }

    private void readSchedule() {

        //4、查询schedule，当查询 ID 号为 0xFF 返回 表示获取设备所有的 ID 编号，主要功能:告诉查询者设备当前有哪些 ID 编号。
        byte[] query = {(byte) 0xAA, 0x30, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, (byte) 0xff, 0x00, 0x55};
        sendPackets(Utils.getSendData(query));

        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, Constants.DUPLICATE_WAIT_TIMEOUT);

    }


    public void onEventMainThread(DataEvent event) {


        if (!isDuplicateOnThisActivity) {
            return;
        }

        byte[] datas = event.getDatas();

        if (datas[1] == (byte) 0x20) {//设备向 APP 获取当前时间

            return;
        }


        if (datas[1] == (byte) 0x90) {//读取theme数据

            mHandler.removeCallbacks(timeoutRunnable);
            mHandler.postDelayed(timeoutRunnable, Constants.DUPLICATE_READ_TIMEOUT);

            if (datas[7] == 0x05) { //theme amount

                themeAmount = datas[9] & 0xFF;
                if (themeAmount == 0) {
                    mHandler.removeCallbacks(timeoutRunnable);
                    readSchedule();
                }

                LogUtils.d("themeAmount:" + themeAmount);

            } else if (datas[7] == 0x03) {//theme channel

                if (canNewTheme) {
                    canNewTheme = false;
                    themeColor = new ThemeColor();
                }

                themeColor.setId(datas[6] & 0xFF);

                ChannelBean channel = new ChannelBean();

                channel.setName("Channel " + (datas[9] & 0xFF));
                channel.setColorNo1(datas[10] & 0XFF);
                channel.setColorNo2(datas[11] & 0XFF);
                channel.setColorNo3(datas[12] & 0XFF);
                channel.setDisplayColor1(findColor(channel.getColorNo1()).getDisplayColor());
                channel.setDisplayColor2(findColor(channel.getColorNo2()).getDisplayColor());
                channel.setDisplayColor3(findColor(channel.getColorNo3()).getDisplayColor());
                byte XS = datas[13];
                channel.setTwinkleOn((XS & 0x80) == 128);
                themeColor.setFade(XS & 0x7F);
                themeColor.getChannels().add(channel);

            } else if (datas[7] == 0x04) {//theme name

                count++;

                int themeNameLen = datas[8] & 0xFF;
                byte[] names = new byte[themeNameLen];
                System.arraycopy(datas, 9, names, 0, themeNameLen);

                themeColor.setName(new String(names));

                LogUtils.d("themeName:" + themeColor.getName());

                ProjectApp.getInstance().getDuplThemeColors().add(themeColor);

                canNewTheme = true;

                if (count == themeAmount) {
                    mHandler.removeCallbacks(timeoutRunnable);
                    readSchedule();
                }


            }
        } else if (datas[1] == (byte) 0xB0) {//schedule数据

            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, Constants.DUPLICATE_WAIT_TIMEOUT);

            if (datas.length == 18) {//simple schedule - 18bytes

                SimpleSchedule simpleSchedule = new SimpleSchedule();
                simpleSchedule.setId(datas[6] & 0xff);
                simpleSchedule.setTheme(datas[7] & 0xff);
                simpleSchedule.setPhotocell(datas[10] == 0x01);
                simpleSchedule.setWeek(datas[13]);

                int hour = datas[14] & 0xff;
                int minute = datas[15] & 0xff;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                simpleSchedule.setTime(sdf.format(calendar.getTime()).toLowerCase());
                simpleSchedule.setOn(true);

                ProjectApp.getInstance().getDuplSimpleSchedule().add(simpleSchedule);

            } else {//custom schedule - 21bytes

                CustomSchedule customSchedule = new CustomSchedule();
                customSchedule.setId(datas[6] & 0xFF);
                customSchedule.setTheme(datas[7] & 0xFF);
                customSchedule.setPhotocell(datas[10] == 0x01);
                byte[] years = {datas[13], datas[14]};
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, TransUtils.bytes2short(years));
                calendar.set(Calendar.MONTH, (datas[15] & 0xFF) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, datas[16] & 0xFF);
                calendar.set(Calendar.HOUR_OF_DAY, datas[17] & 0xFF);
                calendar.set(Calendar.MINUTE, datas[18] & 0xFF);
                customSchedule.setDatetime(calendar.getTimeInMillis());

                ProjectApp.getInstance().getDuplCustomSchedule().add(customSchedule);

                if (ProjectApp.getInstance().getDuplCustomSchedule().size() == 120) {
                    mHandler.removeCallbacks(mRunnable);
                    go();
                }
            }

        }

    }

    private void initColors() {

        ColorBean colorBean;

        colorBean = new ColorBean();
        colorBean.setName("No Color");
        colorBean.setDisplayColor(0x00000000);
        colorBean.setSendColor(0x00000000);
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Champagne");
        colorBean.setDisplayColor(Color.argb(0xFF, 229, 204, 137));
        colorBean.setSendColor(Color.argb(0x00, 234, 150, 15));
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Warm Clear");
        colorBean.setDisplayColor(0xFFfefe9c);
        colorBean.setSendColor(0xFF000000);
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Winter White");
        colorBean.setDisplayColor(Color.argb(0xFF, 235, 244, 251));
        colorBean.setSendColor(Color.argb(0x00, 210, 175, 45));
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Coral");
        colorBean.setDisplayColor(0xFFdb725d);
        colorBean.setSendColor(0x00db725d);
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Red");
        colorBean.setDisplayColor(Color.argb(0xFF, 238, 36, 36));
        colorBean.setSendColor(Color.argb(0x00, 238, 36, 36));
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Amber");
        colorBean.setDisplayColor(Color.argb(0xFF, 208, 121, 42));
        colorBean.setSendColor(Color.argb(0x00, 255, 72, 0));
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Orange");
        colorBean.setDisplayColor(Color.argb(0xFF, 247, 169, 58));
        colorBean.setSendColor(Color.argb(0x00, 255, 30, 0));
        colors1.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Gold");
        colorBean.setDisplayColor(Color.argb(0xFF, 244, 212, 75));
        colorBean.setSendColor(Color.argb(0x00, 255, 92, 0));
        colors1.add(colorBean);

        for (int i = 0; i < colors1.size(); i++) {
            colors1.get(i).setColorNo(i);
        }

        colorBean = new ColorBean();
        colorBean.setName("Yellow");
        colorBean.setDisplayColor(Color.argb(0xFF, 236, 235, 91));
        colorBean.setSendColor(Color.argb(0x00, 255, 150, 0));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Lime");
        colorBean.setDisplayColor(0xFF93cd40);
        colorBean.setSendColor(0x0093cd40);
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Mint");
        colorBean.setDisplayColor(Color.argb(0xFF, 72, 140, 93));
        colorBean.setSendColor(Color.argb(0x00, 46, 139, 10));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Green");
        colorBean.setDisplayColor(Color.argb(0xff, 83, 189, 117));
        colorBean.setSendColor(Color.argb(0x00, 83, 189, 117));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Mountain");
        colorBean.setDisplayColor(0xFF4aa587);
        colorBean.setSendColor(0x004aa587);
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Sea Breeze");
        colorBean.setDisplayColor(0xFF61d3b4);
        colorBean.setSendColor(0x0061d3b4);
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Turquoise");
        colorBean.setDisplayColor(Color.argb(0xFF, 125, 204, 198));
        colorBean.setSendColor(Color.argb(0x00, 64, 224, 35));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Aqua");
        colorBean.setDisplayColor(Color.argb(0xff, 146, 214, 227));
        colorBean.setSendColor(Color.argb(0x00, 0, 255, 171));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Blue");
        colorBean.setDisplayColor(Color.argb(0xFF, 72, 123, 189));
        colorBean.setSendColor(Color.argb(0x00, 72, 123, 189));
        colors2.add(colorBean);

        for (int i = 0; i < colors2.size(); i++) {
            colors2.get(i).setColorNo(i + colors1.size());
        }

        colorBean = new ColorBean();
        colorBean.setName("Royal");
        colorBean.setDisplayColor(Color.argb(0xFF, 59, 87, 150));
        colorBean.setSendColor(Color.argb(0x00, 24, 24, 255));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Purple");
        colorBean.setDisplayColor(Color.argb(0xFF, 93, 88, 168));
        colorBean.setSendColor(Color.argb(0x00, 85, 0, 144));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Violet");
        colorBean.setDisplayColor(Color.argb(0xFF, 201, 141, 192));
        colorBean.setSendColor(Color.argb(0x00, 255, 21, 82));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Pink");
        colorBean.setDisplayColor(Color.argb(0xff, 196, 107, 171));
        colorBean.setSendColor(Color.argb(0x00, 255, 21, 28));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Midnight");
        colorBean.setDisplayColor(Color.argb(0xff, 42, 42, 104));
        colorBean.setSendColor(Color.argb(0x00, 42, 42, 104));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Crimson");
        colorBean.setDisplayColor(Color.argb(0xff, 207, 50, 69));
        colorBean.setSendColor(Color.argb(0x00, 207, 50, 69));
        colors3.add(colorBean);

        for (int i = 0; i < colors3.size(); i++) {
            colors3.get(i).setColorNo(i + colors1.size() + colors2.size());
        }

    }

    private ColorBean findColor(int colorNo) {

        for (ColorBean color : colors1) {

            if (color.getColorNo() == colorNo) {
                return color;
            }
        }


        for (ColorBean color : colors2) {

            if (color.getColorNo() == colorNo) {
                return color;
            }
        }


        for (ColorBean color : colors3) {

            if (color.getColorNo() == colorNo) {
                return color;
            }
        }

        return colors1.get(0);
    }

    private void go() {

        isDuplicateOnThisActivity = false;

        mHandler.removeCallbacks(timeoutRunnable);
        dismissDialog();

        ProjectApp.getInstance().setDuplicateController(duplicateController);
        startActivityForResult(new Intent(mContext, PairControllerQrScanActivity.class), 1024);
    }

}
