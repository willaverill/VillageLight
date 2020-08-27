package com.villagelight.app.activity;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigItems;
import com.mylhyl.circledialog.callback.ConfigSubTitle;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.callback.ConfigTitle;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.ItemsParams;
import com.mylhyl.circledialog.params.SubTitleParams;
import com.mylhyl.circledialog.params.TextParams;
import com.mylhyl.circledialog.params.TitleParams;
import com.mylhyl.circledialog.view.listener.OnCreateBodyViewListener;
import com.villagelight.app.BuildConfig;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.adapter.ControllerMainAdapter;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.DataEvent;
import com.villagelight.app.event.DisconnectEvent;
import com.villagelight.app.fingerprint.FingerFragment;
import com.villagelight.app.fingerprint.PrefUtils;
import com.villagelight.app.model.ControllerBean;
import com.villagelight.app.model.UpdateBean;
import com.villagelight.app.util.DensityUtils;
import com.villagelight.app.util.GsonUtil;
import com.villagelight.app.util.LogUtils;
import com.villagelight.app.util.ToastUtils;
import com.villagelight.app.util.Utils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.RFStarBLEService;
import cn.fly2think.blelib.TransUtils;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {

    public static final String SP_NAME = "villagelight";
    public static final String Key_IsFirstLaunch = "isFirstLaunch";
    public static final String Key_App_Get_Started = "getStarted";
    public static final String Key_Add_Phone = "AddPhone";
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.btn_title_right)
    ImageButton mBtnTitleRight;
    @BindView(R.id.btn_view_tutorial)
    Button mBtnViewTutorial;
    @BindView(R.id.btn_add_controller)
    Button mBtnAddController;
    @BindView(R.id.lv)
    ListView mLv;
    @BindView(R.id.layout_button)
    LinearLayout mLayoutButton;
    @BindView(R.id.separator)
    View mSeparator;
    private List<ControllerBean> mControllers = new ArrayList<>();
    private ControllerMainAdapter mAdapter;
    private boolean isReconnect = false;
    private ControllerBean mCurrentController;
    private Handler mHandler = new Handler();
    private List<Byte> mRecvs = new ArrayList<>();
    private int mLen;
    private HttpUtils mHttpUtils = new HttpUtils();
    private HttpHandler mHttpHandler;
    private short onlineVersion;
    private int localVersion;
    int deviceCount = -1;
    private String fileName;
    private String url;
    private boolean isMainChecked;
    private DialogFragment disconnectDialogFragment;
    SharedPreferences sharedPreferences;
    private String autoConnectControllerName = null;
    private Runnable upgradeRunnable = new Runnable() {
        @Override
        public void run() {

            if (!TextUtils.isEmpty(url)) {
                FirmwareError();
            } else {
                goControlMenu();
            }
        }
    };

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {

            dismissDialog();
            new CircleDialog.Builder()
                    .setCanceledOnTouchOutside(false)
                    .setTitle("Please contract VLC customer support")
                    .setText("")
                    .setPositive("OK", null)
                    .show(getSupportFragmentManager());

        }
    };

    private Runnable checkFirmware = new Runnable() {
        @Override
        public void run() {

            mHttpUtils.configCurrentHttpCacheExpiry(0);
            mHttpUtils.send(HttpRequest.HttpMethod.GET,
                    mCurrentController.getYear() == 2019 ? BuildConfig.URL_CONTROLLER_LS3 : BuildConfig.URL_CONTROLLER_LS2,
                    new RequestCallBack<String>() {
                        @Override
                        public void onLoading(long total, long current, boolean isUploading) {

                        }

                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {

                            UpdateBean updateBean = GsonUtil.fromJson(responseInfo.result, UpdateBean.class);

                            if (updateBean != null && updateBean.getFirmwares() != null && !updateBean.getFirmwares().isEmpty()) {

                                final String version = updateBean.getFirmwares().get(0).getVersion()
                                        .replace("V", "").replace("v", "");

                                onlineVersion = Short.parseShort(version);

                                url = updateBean.getFirmwares().get(0).getUrl();
                                fileName = url.substring(url.lastIndexOf("/"));
                            }

                            //1.查询属性
                            byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                    (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                            sendPackets(Utils.getSendData(query));
                            isMainChecked = true;
                            //3秒内没有固件回复，提示更新
                            mHandler.removeCallbacks(upgradeRunnable);
                            mHandler.postDelayed(upgradeRunnable, Constants.READ_VERSION_TIMEOUT);

                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {

                            //1.查询属性
                            byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                    (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                            sendPackets(Utils.getSendData(query));
                            isMainChecked = true;
                            //3秒内没有固件回复，提示更新
                            mHandler.removeCallbacks(upgradeRunnable);
                            mHandler.postDelayed(upgradeRunnable, Constants.READ_VERSION_TIMEOUT);

                        }
                    });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.CAMERA,
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(permissions -> {
                    // Storage permission are allowed.
                })
                .onDenied(permissions -> {
                    // Storage permission are not allowed.
                })
                .start();

        mBtnTitleRight.setVisibility(View.VISIBLE);
        mTvTitle.setText("Paired Controllers");

        if (app.manager.isSupport() && !app.manager.isEdnabled()) {

            app.manager.enable(true);
        }

        mAdapter = new ControllerMainAdapter(mContext, mControllers);
        mLv.setAdapter(mAdapter);

        mAdapter.setListener(mOnClickListener);

        ProjectApp.getInstance().startJob();


        if (PrefUtils.isProtect(mContext)) {

            FingerFragment fingerFragment = new FingerFragment();
            fingerFragment.show(getFragmentManager(), "fingerFragment");
            fingerFragment.setmFragmentCallBack(new FingerFragment.Callback() {
                @Override
                public void onSuccess() {
//                    checkFirmwareVersion();
//                    showIsFirstLaunch();
                }

                @Override
                public void onError(String msg) {

                    ToastUtils.showToast(mContext, msg);

                }

                @Override
                public void onCancel() {

                    finish();

                }
            });

        } else {
//            checkFirmwareVersion();
//            showIsFirstLaunch();
        }

    }

    public void showIsFirstLaunch(){
        sharedPreferences = this.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        if (mControllers != null && mControllers.size() == 0){

            sharedPreferences.edit().putBoolean(Key_IsFirstLaunch,false).commit();
            List<String> items = new ArrayList<>();
            items.add("NO, teach me how this works.");
            items.add("YES, I’m ready to get started.");
            items.add("My system is already setup,\n" +
                    "I’m adding another phone\n" +
                    "to my controller.");
            String title = "Thank you for purchasing\n" +
                    "Light Stream.\n" +
                    "Are your lights assembled\n" +
                    "and ready to be setup?\n";
            new CircleDialog.Builder()
                    .setCancelable(true)
                    .setTitle(title)
                    .configTitle(new ConfigTitle() {
                        @Override
                        public void onConfig(TitleParams params) {
                            params.height = 350;
                        }
                    })
                    .configItems(new ConfigItems() {
                        @Override
                        public void onConfig(ItemsParams params) {
                            params.itemHeight = 220;
                        }
                    })
                    .setItems(items, new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0){
                                sharedPreferences.edit()
                                        .putBoolean(Key_IsFirstLaunch,false)
                                        .commit();
                                showHelp();
                            }else if (position == 1){
                                sharedPreferences.edit()
                                        .putBoolean(Key_IsFirstLaunch,false)
                                        .putBoolean(Key_Add_Phone,false)
                                        .putBoolean(Key_App_Get_Started,true)
                                        .commit();
                                ProjectApp.getInstance().setDuplicateController(null);
                                startActivityForResult(new Intent(mContext, PairControllerQrScanActivity.class),1024);
                            }else {
                                sharedPreferences.edit()
                                        .putBoolean(Key_IsFirstLaunch,false)
                                        .putBoolean(Key_Add_Phone,true)
                                        .putBoolean(Key_App_Get_Started,false)
                                        .commit();
                                ProjectApp.getInstance().setDuplicateController(null);
                                startActivityForResult(new Intent(mContext, PairControllerQrScanActivity.class),1024);
                            }
                        }
                    })
                    .setGravity(Gravity.CENTER)
                    .show(getSupportFragmentManager());
        }else {
//            checkFirmwareVersion();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mControllers.clear();
        List<ControllerBean> list = null;
        try {
            list = ProjectApp.getInstance().getDb().findAll(Selector.from(ControllerBean.class));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (list != null && !list.isEmpty()) {
            //根据Controler Name字母排序
            Collections.sort(list,(left,right) ->{
                String leftName = left.getControllerName();
                String rightName = right.getControllerName();
                //避免空指针情况
                if (leftName == null){
                    leftName = "";
                }

                if (rightName == null){
                    rightName = "";
                }
                return leftName.compareTo(rightName);
            });
            mControllers.addAll(list);
        }
        mAdapter.notifyDataSetChanged();
        showIsFirstLaunch();
        showView();
        Log.e("onActivityResult","onResume" + autoConnectControllerName);
        if (autoConnectControllerName != null){
            //Find Pos
            int pos = -1;
            int deviceListSize = mAdapter.getCount();
            for (int i = 0 ; i < deviceListSize; i ++) {
                String controllerName = list.get(i).getControllerName();
                if (autoConnectControllerName.equals(controllerName)){
                    pos = i;
                    break;
                }
            }

            if (pos != -1 && deviceListSize > pos && deviceListSize >= 0){
                //perferclick
                View view = mAdapter.getView(pos, null, null);
                Log.e("onActivityResult","onResume performClick " + autoConnectControllerName +" " + view);
                view.findViewById(R.id.item_tv_name).performClick();
                autoConnectControllerName = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(upgradeRunnable);
        mHandler.removeCallbacks(checkFirmware);
        mHandler.removeCallbacks(resetRunnable);
        disconnect();
        ProjectApp.getInstance().stopJob();
        if (mHttpHandler != null) {
            //调用cancel()方法停止下载
            mHttpHandler.cancel();
        }
    }

    @OnClick({R.id.btn_title_right, R.id.btn_view_tutorial, R.id.btn_add_controller, R.id.tv_help})
    public void onViewClicked(View view) {

        isReconnect = false;

        switch (view.getId()) {
            case R.id.btn_title_right:
                if (mControllers.isEmpty()) {
                    ProjectApp.getInstance().setDuplicateController(null);
                    startActivityForResult(new Intent(mContext, PairControllerQrScanActivity.class),1024);
                } else {

                    String[] items = {"New", "Duplicate", "Cancel"};
                    new CircleDialog.Builder()
                            .configDialog(new ConfigDialog() {
                                @Override
                                public void onConfig(DialogParams params) {
//                                params.backgroundColorPress = Color.CYAN;
                                    //增加弹出动画
//                                params.animStyle = R.style.dialogWindowAnim;
                                }
                            })
                            .setTitle("New or Duplicate")
//                        .setTitleColor(Color.BLUE)
                            .configTitle(new ConfigTitle() {
                                @Override
                                public void onConfig(TitleParams params) {
//                                params.backgroundColor = Color.RED;
                                }
                            })
                            .setSubTitle("Would you like to build a new controller or duplicate settings?")
                            .configSubTitle(new ConfigSubTitle() {
                                @Override
                                public void onConfig(SubTitleParams params) {
//                                params.backgroundColor = Color.YELLOW;
                                    params.textSize = DensityUtils.sp2px(mContext, 14);
                                }
                            })
                            .setItems(items, new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int
                                        position, long id) {

                                    if (position == 0) {
                                        ProjectApp.getInstance().setDuplicateController(null);
                                        startActivityForResult(new Intent(mContext, PairControllerQrScanActivity.class),1024);
                                    } else if (position == 1) {
                                        startActivityForResult(new Intent(mContext, DuplicateControllerActivity.class),1024);
                                    }
                                }
                            })
                            .configItems(new ConfigItems() {
                                @Override
                                public void onConfig(ItemsParams params) {
                                    params.textColor = Color.parseColor("#4e73a4");
                                }
                            })
                            .setGravity(Gravity.CENTER)
                            .show(getSupportFragmentManager());
                }
                break;
            case R.id.btn_add_controller:
                ProjectApp.getInstance().setDuplicateController(null);
                startActivityForResult(new Intent(mContext, PairControllerQrScanActivity.class),1024);
                break;
            case R.id.btn_view_tutorial:
                startActivity(new Intent(mContext, TutorialActivity.class));
                break;
            case R.id.tv_help:
                showHelp();
                break;
        }
    }

    private void showHelp() {
        Uri uri = Uri.parse(Constants.URL_HELP);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    /****
     * onActivityResult >>>> onResume
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data.hasExtra(PairSucceedActivity.KeyControlerName)){
                autoConnectControllerName = data.getStringExtra(PairSucceedActivity.KeyControlerName);
            }else {
                autoConnectControllerName = null;
            }
            Log.e("onActivityResult","onActivityResult:"+data.getStringExtra(PairSucceedActivity.KeyControlerName));
        }else {
            autoConnectControllerName = null;
        }
    }

    private void showView() {

        if (!mControllers.isEmpty()) {
            mLayoutButton.setVisibility(View.GONE);
            mLv.setVisibility(View.VISIBLE);
            mSeparator.setVisibility(View.VISIBLE);
        } else {
            mLayoutButton.setVisibility(View.VISIBLE);
            mLv.setVisibility(View.INVISIBLE);
            mSeparator.setVisibility(View.INVISIBLE);
        }
    }

    // ble状态以及数据回调接口
    public void onEventMainThread(BroadcastEvent event) {
        Intent intent = event.getIntent();
        String action = intent.getAction();

        if (RFStarBLEService.ACTION_GATT_FAILED.equals(action)) {

            dismissDialog();
            mAdapter.notifyDataSetChanged();

            if (isReconnect) {
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
            }


        } else if (RFStarBLEService.ACTION_GATT_CONNECTED.equals(action)) {

            localVersion = 0;
            mRecvs.clear();

        } else if (RFStarBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {

            dismissDialog();
            mAdapter.notifyDataSetChanged();
            ProjectApp.getInstance().stopJob();
            EventBus.getDefault().post(new DisconnectEvent());

        } else if (RFStarBLEService.ACTION_GATT_SERVICES_DISCOVERED
                .equals(action)) {

            ProjectApp.getInstance().startJob();

            if (app.manager.cubicBLEDevice != null) {
                app.manager.cubicBLEDevice.setNotification(true);
            }


            mAdapter.notifyDataSetChanged();
            if (isReconnect) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        app.manager.setWriteUUID("00001005-0000-1000-8000-00805f9b34fb");
                        app.manager.cubicBLEDevice.writeValue(new byte[]{(byte) 0x83});
                        app.manager.setWriteUUID("00001003-0000-1000-8000-00805f9b34fb");
                        if (!TextUtils.isEmpty(mCurrentController.getPassword())) {
                            app.manager.cubicBLEDevice.writeValue(mCurrentController.getPassword().getBytes());
                        }
                        if (app.isDebug()) {
                            app.manager.setWriteUUID("0000fff1-0000-1000-8000-00805f9b34fb");
                        } else {
                            app.manager.setWriteUUID("00001001-0000-1000-8000-00805f9b34fb");
                        }


                    }
                }, 100);

                mHandler.postDelayed(checkFirmware, 5000);//1、连接成功，超时5秒没有收到授时指令，发送查询固件指令

            }

        } else if (RFStarBLEService.ACTION_DATA_AVAILABLE.equals(action)) {

            byte[] datas = intent
                    .getByteArrayExtra(RFStarBLEService.EXTRA_DATA);

//            LogUtils.d("recv:" + TransUtils.appendSpace(TransUtils.bytes2hex(datas)));

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

    private void goControlMenu() {

        dismissDialog();

        if (mCurrentController == null) {
            return;
        }

        if (deviceCount> 0){
            mCurrentController.setYear(2018);
        }else {
            mCurrentController.setYear(2019);
        }

        try {
            ProjectApp.getInstance().getDb().update(mCurrentController);
        } catch (DbException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(mContext, ControlMenuActivity.class);
        ProjectApp.getInstance().setCurrentControl(mCurrentController);
        startActivity(intent);

    }


    private void forwardData(byte[] datas) {

        EventBus.getDefault().post(new DataEvent(datas));

        LogUtils.d("pack:" + TransUtils.appendSpace(TransUtils.bytes2hex(datas)));

        if (datas[1] == (byte) 0x20) {//设备向 APP 获取当前时间

            syncTime();//下位机说收不到？？？
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    syncTime();//多发一次

                    if (isReconnect) {
                        /**
                         * 收到授时指令，发送查询固件
                         */
                        mHandler.removeCallbacks(checkFirmware);
                        mHandler.postDelayed(checkFirmware, 100);
                    }
                }
            }, 100);

        } else if (datas[1] == (byte) 0x81) {

            //标志位判断是否是主界面发送的查询固件指令，避免反复弹窗
            if (!isMainChecked) {
                return;
            }
            isMainChecked = false;

            mHandler.removeCallbacks(upgradeRunnable);

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
            localVersion = TransUtils.bytes2short(versions);
            Constants.FirmwareVersion = localVersion;
            LogUtils.d("localVersion:" + localVersion + ",onlineVersion:" + onlineVersion);

            byte[] devicesCount = {datas[35], datas[36]};
            deviceCount = TransUtils.bytes2short(devicesCount);


            if (onlineVersion > localVersion) {
                showUpgradeDialog();
            } else {
                goControlMenu();
            }


        } else if (datas[1] == (byte) 0x83) {

            mHandler.removeCallbacks(resetRunnable);
            if (isForeground(mContext, MainActivity.class.getName())) {
                goControlMenu();
            }

        }
    }


    private byte getWeek(Calendar calendar) {

        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        StringBuffer sb = new StringBuffer();
        sb.append(0);
        sb.append(w == 6 ? 1 : 0);
        sb.append(w == 5 ? 1 : 0);
        sb.append(w == 4 ? 1 : 0);
        sb.append(w == 3 ? 1 : 0);
        sb.append(w == 2 ? 1 : 0);
        sb.append(w == 1 ? 1 : 0);
        sb.append(w == 0 ? 1 : 0);


        return (byte) TransUtils.binaryToAlgorism(sb.toString());
    }

    private void checkFirmwareVersion() {
        ToastUtils.showToast(mContext, "Scanning for Controllers");
        mHttpUtils.configCurrentHttpCacheExpiry(0);
        mHttpUtils.configTimeout(8000);
        mHttpUtils.configSoTimeout(8000);
        mHttpUtils.send(HttpRequest.HttpMethod.GET,
                BuildConfig.URL_CONTROLLER_LS3,
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {

                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        dismissDialog();

                        UpdateBean updateBean = GsonUtil.fromJson(responseInfo.result, UpdateBean.class);

                        if (updateBean == null) {
                            return;
                        }

                        if (updateBean.getFirmwares().isEmpty()) {


                            return;
                        }

                        final String version = updateBean.getFirmwares().get(0).getVersion()
                                .replace("V", "").replace("v", "");

                        onlineVersion = Short.parseShort(version);

                        url = updateBean.getFirmwares().get(0).getUrl();
                        fileName = url.substring(url.lastIndexOf("/"));

                    }

                    @Override
                    public void onStart() {

                        showDialog("", "Checking...");
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                        LogUtils.d("server error:" + msg);
                        dismissDialog();
                    }
                });

    }


    private void syncTime() {

        Calendar calendar = Calendar.getInstance();
        byte[] years = TransUtils.short2bytes((short) calendar.get(Calendar.YEAR));
        byte month = (byte) (calendar.get(Calendar.MONTH) + 1);
        byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byte minute = (byte) calendar.get(Calendar.MINUTE);
        byte second = (byte) calendar.get(Calendar.SECOND);

        byte[] cmds = {(byte) 0xAA, (byte) 0xA0, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, years[0], years[1], month, day, hour, minute, second, getWeek(calendar),
                0x00, 0x55};

        sendPackets(Utils.getSendData(cmds));
    }

    private void FirmwareError() {

        dismissDialog();

        String tips = "There seems to be a problem with your controller’s firmware. Here’s some troubleshooting tips:\n\n" +
                "1. Close and relaunch your app and reconnect to the controller\n\n" +
                "2. Unplug and plug in your controller, then connect to your device\n\n" +
                "3. Reinstall Firmware\n\n" +
                "4. Reset the controller\n\n" +
                "5. Contact VLC customer support";

        new CircleDialog.Builder()
                .setCancelable(false)
                .setTitle("Controller Firmware Error")
                .setText(tips)
                .setNegative("Cancel", null)
                .setNeutral("Reset Controller", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        new CircleDialog.Builder()
                                .setCancelable(false)
                                .setTitle("RESET CONTROLLER")
                                .setText("All current themes, schedules and pairings will be erased.")
                                .setNegative("Cancel", null)
                                .setPositive("Continue", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        showDialog("", "Resetting...");

                                        //重置Controller
                                        byte[] query = {(byte) 0xAA, 0x65, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                                (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x55};
                                        sendPackets(Utils.getSendData(query));
                                        //20秒内没有回复，提示对话框
                                        mHandler.removeCallbacks(resetRunnable);
                                        mHandler.postDelayed(resetRunnable, Constants.RESET_CONTROLLER_TIMEOUT);

                                    }
                                })
                                .show(getSupportFragmentManager());

                    }
                })
                .setPositive("Firmware Update", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dismissDialog();

                        File folder = new File(Environment.getExternalStorageDirectory(), "VillageLight");
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }

                        File file = new File(folder, fileName);

                        mHttpHandler = mHttpUtils.download(url,
                                file.getPath(),
                                false, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                                true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                                new RequestCallBack<File>() {

                                    @Override
                                    public void onStart() {

                                        showDialog("", "Downloading...");
                                    }

                                    @Override
                                    public void onLoading(long total, long current, boolean isUploading) {

                                    }

                                    @Override
                                    public void onSuccess(ResponseInfo<File> responseInfo) {

                                        dismissDialog();
                                        File bin = new File(responseInfo.result.getPath());

                                        Intent intent = new Intent(mContext, FirmwareControllerUpdateActivity.class);
                                        intent.putExtra("bin", bin);
                                        intent.putExtra("version", onlineVersion);
                                        intent.putExtra("local", localVersion);
                                        startActivity(intent);

                                        /**
                                         * 避免传递后onlineVersion变成０
                                         */
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                onlineVersion = 0;
                                            }
                                        }, 500);

                                    }


                                    @Override
                                    public void onFailure(HttpException error, String msg) {

                                        onlineVersion = 0;

                                        dismissDialog();
                                        if (error.getExceptionCode() == 404) {

                                            new CircleDialog.Builder()
                                                    .setCanceledOnTouchOutside(false)
                                                    .setTitle("Firmware Not Found")
                                                    .setText("Firmware updates are currently not available at this time.")
                                                    .setPositive("OK", null)
                                                    .show(getSupportFragmentManager());

                                        } else {

                                            new CircleDialog.Builder()
                                                    .setCanceledOnTouchOutside(false)
                                                    .setTitle("Internet Unavailable")
                                                    .setText("Connect to a wifi network or enable \ncellular data to update firmware.")
                                                    .setNegative("Cancel", null)
                                                    .setPositive("OK", null)
                                                    .show(getSupportFragmentManager());
                                        }
                                    }
                                });

                    }
                })
                .configNegative(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {

                        params.textSize = DensityUtils.sp2px(mContext, 13);
                    }
                })
                .configNeutral(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {

                        params.textSize = DensityUtils.sp2px(mContext, 13);
                    }
                })
                .configPositive(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {

                        params.textSize = DensityUtils.sp2px(mContext, 13);
                    }
                })
                .show(getSupportFragmentManager());

    }

    private void showUpgradeDialog() {

        dismissDialog();

        new CircleDialog.Builder()
                .setCancelable(false)
                .setTitle("Firmware Update Available")
                .setBodyView(R.layout.dialog_upgrade, new OnCreateBodyViewListener() {
                    @Override
                    public void onCreateBodyView(View view) {


                        TextView tv_version = view.findViewById(R.id.tv_version);
                        tv_version.setText("New Firmware Version:V" + onlineVersion + "\n" + "Your Version:V" + localVersion);

                    }
                })
                .setPositive("Update Now", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        goControlMenu();
                        updateNow();

                    }
                })
                .setNegative("Update Later",v->{

                })
                .show(getSupportFragmentManager());

    }

    private void updateNow() {
        File folder = new File(Environment.getExternalStorageDirectory(), "VillageLight");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName);

        mHttpHandler = mHttpUtils.download(url,
                file.getPath(),
                false, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {

                        showDialog("", "Downloading...");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {

                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {

                        dismissDialog();
                        File bin = new File(responseInfo.result.getPath());

                        Intent intent = new Intent(mContext, FirmwareControllerUpdateActivity.class);
                        intent.putExtra("bin", bin);
                        intent.putExtra("version", onlineVersion);
                        intent.putExtra("local", localVersion);
                        startActivity(intent);

                        /**
                         * 避免传递后onlineVersion变成０
                         */
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onlineVersion = 0;
                            }
                        }, 500);

                    }


                    @Override
                    public void onFailure(HttpException error, String msg) {

                        onlineVersion = 0;

                        dismissDialog();
                        if (error.getExceptionCode() == 404) {

                            new CircleDialog.Builder()
                                    .setCanceledOnTouchOutside(false)
                                    .setTitle("Firmware Not Found")
                                    .setText("Firmware updates are currently not available at this time.")
                                    .setPositive("OK", null)
                                    .show(getSupportFragmentManager());

                        } else {

                            new CircleDialog.Builder()
                                    .setCanceledOnTouchOutside(false)
                                    .setTitle("Internet Unavailable")
                                    .setText("Connect to a wifi network or enable \ncellular data to update firmware.")
                                    .setNegative("Cancel", null)
                                    .setPositive("OK", null)
                                    .show(getSupportFragmentManager());
                        }
                    }
                });
    }

    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void showDisconnectDialog() {

        dismissDialog();

        if (disconnectDialogFragment != null) {
            disconnectDialogFragment.dismiss();
        }

        disconnectDialogFragment = new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .setTitle("Bluetooth Connection Problem")
                .setText("Your phone’s connection to the control- ler has been lost. Please connect again.")
                .setPositive("OK", null)
                .show(getSupportFragmentManager());
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final int position = (int) view.getTag();

            mCurrentController = mControllers.get(position);
            isReconnect = false;

            switch (view.getId()) {
                case R.id.item_tv_name:

                    /**
                     * 弹窗选择一次Controller的年份
                     */
//                    if (mCurrentController.getYear() == 0) {
//
//                        new CircleDialog.Builder()
//                                .setCanceledOnTouchOutside(false)
//                                .setTitle("Confirm Bulb Version")
//                                .setText("Mixing 2018 and 2019 bulbs on the same system is no longer supported. Please Choose the version of bulbs you'll be pairing to this controller:\n\n" +
//                                        "If you need to change this option in the future, delete this controller and pair again.")
//                                .setPositive("2019(LS3)", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//
//                                        mCurrentController.setYear(2019);
//                                        try {
//                                            ProjectApp.getInstance().getDb().update(mCurrentController);
//                                        } catch (DbException e) {
//                                            e.printStackTrace();
//                                        }
//                                        mOnClickListener.onClick(view);
//
//                                    }
//                                })
//                                .configNegative(new ConfigButton() {
//                                    @Override
//                                    public void onConfig(ButtonParams params) {
//                                        params.textColor = Color.parseColor("#FF007AFF");
//                                    }
//                                })
//                                .setNegative("2018(LS2)", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        mCurrentController.setYear(2018);
//                                        try {
//                                            ProjectApp.getInstance().getDb().update(mCurrentController);
//                                        } catch (DbException e) {
//                                            e.printStackTrace();
//                                        }
//                                        mOnClickListener.onClick(view);
//                                    }
//                                })
//                                .show(getSupportFragmentManager());
//
//                        return;
//                    }

                    if (isConnected() && mCurrentController.getDeviceMac().equals(app.manager.cubicBLEDevice.deviceMac)) {

                        goControlMenu();

                    } else {

                        showDialog("", "Connecting...");
                        disconnect();
                        isReconnect = true;

                        // 连接设备
                        if (app.manager.isEdnabled()) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                            connect(bluetoothManager.getAdapter().getRemoteDevice(mCurrentController.getDeviceMac()));
                        } else {

                            app.manager.enable(true);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                                    connect(bluetoothManager.getAdapter().getRemoteDevice(mCurrentController.getDeviceMac()));
                                }
                            }, 500);
                        }

                    }
                    break;
                case R.id.btn_delete:

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
                            .setText("Are you sure to delete this controller?")
                            .configText(new ConfigText() {
                                @Override
                                public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                                }
                            })
                            .setNegative("Cancel", null)
                            .setPositive("Confirm", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    try {
                                        ProjectApp.getInstance().getDb().delete(mCurrentController);
                                        mControllers.remove(position);
                                        //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                                        //且如果想让侧滑菜单同时关闭，需要同时调用 ((SwipeMenuLayout) holder.itemView).quickClose();
                                        mAdapter.notifyDataSetChanged();
                                        showView();
                                        if (isConnected() && mCurrentController.getDeviceMac().equals(app.manager.cubicBLEDevice.deviceMac)) {

                                            disconnect();
                                        }
                                    } catch (DbException e) {
                                        e.printStackTrace();
                                        ToastUtils.showToast(mContext, e.getMessage());
                                    }

                                }
                            })
                            .configPositive(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                                }
                            })
                            .show(getSupportFragmentManager());

                    break;
                case R.id.btn_rename:

                    Intent intent = new Intent(mContext, PairSucceedActivity.class);
                    intent.putExtra("controller", mCurrentController);
                    startActivity(intent);

                    break;
            }

        }
    };

}
