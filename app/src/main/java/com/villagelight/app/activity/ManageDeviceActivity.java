package com.villagelight.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.TextParams;
import com.mylhyl.circledialog.view.listener.OnCreateBodyViewListener;
import com.villagelight.app.BuildConfig;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.DataEvent;
import com.villagelight.app.event.PairEvent;
import com.villagelight.app.event.UnpairEvent;
import com.villagelight.app.model.UpdateBean;
import com.villagelight.app.util.DensityUtils;
import com.villagelight.app.util.GsonUtil;
import com.villagelight.app.util.LogUtils;
import com.villagelight.app.util.Utils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.TransUtils;

public class ManageDeviceActivity extends BaseActivity {

    public static String Key_PairBulbs = "PairBulbs";
    public static String Key_PairSwitch = "PairSwitch";
    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_bulbs)
    TextView mTvBulbs;
    @BindView(R.id.tv_switches)
    TextView mTvSwitches;
    @BindView(R.id.lv)
    ListView mLv;
    EditText etPwd;
    private HttpUtils mHttpUtils = new HttpUtils();
    private HttpHandler mHttpHandler;
    private Handler mHandler = new Handler();
    private short onlineVersion;
    private int localVersion;
    private String fileName;
    private String url;
    private boolean isPairSwitches;
    private boolean isPairBulbs;
    private int offReply;
    private boolean isPowerOn;
    private boolean isUpgradeController;
    private boolean is2019Device;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            stopPair();
        }
    };

    private Runnable dismissRunanble = new Runnable() {
        @Override
        public void run() {

            dismissDialog();
        }
    };

    private Runnable upgradeRunnable = new Runnable() {
        @Override
        public void run() {

            showUpgradeDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_device);
        ButterKnife.bind(this);

//        is2019Device = ProjectApp.getInstance().getCurrentControl().getYear() == 2019;

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Manage Devices");

        mTvBulbs.setText(ProjectApp.getInstance().getSyncBulbs() + " Bulbs");
        mTvSwitches.setText(ProjectApp.getInstance().getSyncSwitches() + " Switch(s)");

        if (ProjectApp.getInstance().getSyncBulbs() == 0) {
            is2019Device = true;
        } else {
            is2019Device = false;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.item_lv_menu);
        mLv.setAdapter(adapter);

        adapter.add(getString(R.string.pair_switches));
        adapter.add(getString(R.string.pair_bulbs));
        adapter.add(getString(R.string.unpair_devices));
        adapter.add(getString(R.string.manager_bulb_channels));
        adapter.add(getString(R.string.update_control_firmware));
        adapter.add(getString(R.string.update_switch_firmware));
        adapter.add(getString(R.string.update_bulb_firmware));
        adapter.add("Revert to Legacy Bulb Firmware");
        adapter.add(getString(R.string.calibrate_photocell));
        adapter.add(getString(R.string.setup_tutorial));
        adapter.add(getString(R.string.password_protect));
        adapter.add(getString(R.string.about));

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Key_PairBulbs) && intent.getBooleanExtra(Key_PairBulbs, false)) {
                showPairBulbs();
            } else if (intent.hasExtra(Key_PairSwitch) && intent.getBooleanExtra(Key_PairSwitch, false)) {
                showPairSwitches();
            }
        }

        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onlineVersion = 0;
                isUpgradeController = false;

                if (adapter.getItem(position).equals(getString(R.string.manager_bulb_channels))) {

                    //输入密码
                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Alert")
                            .setBodyView(R.layout.pwd, new OnCreateBodyViewListener() {
                                @Override
                                public void onCreateBodyView(View view) {

                                    etPwd = view.findViewById(R.id.etPwd);
                                }
                            })
                            .setNegative("Cancel", v -> {
                                etPwd = null;
                            })
                            .setPositive("OK", v -> {
                                if (etPwd != null) {
                                    String pwd = etPwd.getText().toString();
                                    if (pwd.equalsIgnoreCase("2020")) {
                                        manageBulbChannels();
                                    }
                                }
                                etPwd = null;
                            })
                            .show(getSupportFragmentManager());

                } else if (adapter.getItem(position).equals(getString(R.string.update_control_firmware))) {

                    mHttpUtils.configCurrentHttpCacheExpiry(0);
                    mHttpUtils.send(HttpRequest.HttpMethod.GET,
                            is2019Device ? BuildConfig.URL_CONTROLLER_LS3 : BuildConfig.URL_CONTROLLER_LS2,
                            new RequestCallBack<String>() {
                                @Override
                                public void onLoading(long total, long current, boolean isUploading) {

                                }

                                @Override
                                public void onSuccess(ResponseInfo<String> responseInfo) {

                                    UpdateBean updateBean = GsonUtil.fromJson(responseInfo.result, UpdateBean.class);
                                    if (updateBean == null) {
                                        return;
                                    }

                                    if (updateBean.getFirmwares().isEmpty()) {
                                        dismissDialog();

                                        new CircleDialog.Builder()
                                                .setCanceledOnTouchOutside(false)
                                                .setTitle("Firmware is up to date")
                                                .setText("Your hardware is running the latest version of firmware.")
                                                .setPositive("OK", null)
                                                .show(getSupportFragmentManager());

                                        return;
                                    }

                                    final String version = updateBean.getFirmwares().get(0).getVersion()
                                            .replace("V", "").replace("v", "");

                                    onlineVersion = Short.parseShort(version);

                                    url = updateBean.getFirmwares().get(0).getUrl();
                                    fileName = url.substring(url.lastIndexOf("/"));

                                    byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                            (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                                    sendPackets(Utils.getSendData(query));

                                    mHandler.removeCallbacks(upgradeRunnable);
                                    mHandler.postDelayed(upgradeRunnable, Constants.READ_VERSION_TIMEOUT);

                                }

                                @Override
                                public void onStart() {

                                    showDialog("", "Checking...");
                                }

                                @Override
                                public void onFailure(HttpException error, String msg) {

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

                } else if (adapter.getItem(position).equals(getString(R.string.update_switch_firmware))) {

                    mHttpUtils.configCurrentHttpCacheExpiry(0);
                    mHttpUtils.send(HttpRequest.HttpMethod.GET,
                            is2019Device ? BuildConfig.URL_SWITCHER_LS3 : BuildConfig.URL_SWITCHER_LS2,
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
                                        dismissDialog();

                                        new CircleDialog.Builder()
                                                .setCanceledOnTouchOutside(false)
                                                .setTitle("Firmware is up to date")
                                                .setText("Your hardware is running the latest version of firmware.")
                                                .setPositive("OK", null)
                                                .show(getSupportFragmentManager());

                                        return;
                                    }

                                    final String version = updateBean.getFirmwares().get(0).getVersion()
                                            .replace("V", "").replace("v", "");

                                    String url = updateBean.getFirmwares().get(0).getUrl();
                                    String fileName = url.substring(url.lastIndexOf("/"));

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

                                                    Intent intent = new Intent(mContext, FirmwareSwitchUpdateActivity.class);
                                                    intent.putExtra("bin", bin);
                                                    intent.putExtra("version", Short.parseShort(version));
                                                    startActivity(intent);

                                                }


                                                @Override
                                                public void onFailure(HttpException error, String msg) {

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

                                @Override
                                public void onStart() {

                                    showDialog("", "Checking...");
                                }

                                @Override
                                public void onFailure(HttpException error, String msg) {

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

                } else if (adapter.getItem(position).equals(getString(R.string.update_bulb_firmware))) {


                    //输入密码
                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Alert")
                            .setBodyView(R.layout.pwd, new OnCreateBodyViewListener() {
                                @Override
                                public void onCreateBodyView(View view) {

                                    etPwd = view.findViewById(R.id.etPwd);
                                }
                            })
                            .setNegative("Cancel", v -> {
                                etPwd = null;
                            })
                            .setPositive("OK", v -> {
                                if (etPwd != null) {
                                    String pwd = etPwd.getText().toString();
                                    if (pwd.equalsIgnoreCase("2020")) {
                                        updateBulbFirmware();
                                    }
                                }
                                etPwd = null;
                            })
                            .show(getSupportFragmentManager());

                } else if (adapter.getItem(position).equals("Revert to Legacy Bulb Firmware")) {
                    new CircleDialog.Builder()
                            .setTitle("Update Legacy Bulb Firmware")
                            .setText("Please choose the type of bulbs you will be using with this controller below.\n" +
                                    "You will be prompted to install the correct controller firmware version for the bulbs you select")
                            .setNegative("2018(LS2)", v -> {

                                startQueryUpdate(2018);
                            })
                            .setPositive("2019+(LS3)", v -> {
                                startQueryUpdate(2019);
                            })
                            .show(getSupportFragmentManager());
                } else if (adapter.getItem(position).equals(getString(R.string.calibrate_photocell))) {

                    if (Constants.FirmwareVersion <= 29) {
                        new CircleDialog.Builder()
                                .setCanceledOnTouchOutside(false)
                                .setTitle("Firmware Compatibility Error")
                                .setText("This function is not compatible with your current firmware. To proceed, update you’re firmware")
                                .setPositive("OK", null)
                                .show(getSupportFragmentManager());
                        return;
                    }

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Photocell Calibration")
                            .setText("This process will update the photocell to trigger APPROXIMATELY at the current light level. Results may vary.")
                            .setNeutral("RESTORE DEFAULT", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //AA 08 06 02 00 00 01 0D 55
                                    byte[] cmds = {(byte) 0xAA, 0x08, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                            0x00, 0x00, 0x01, 0x00, 0x55};
                                    sendPackets(Utils.getSendData(cmds));

                                    showDialogCanCancel("", "Calibrating...");
                                    mHandler.postDelayed(dismissRunanble, Constants.CALIBRATE_TIMEOUT);
                                }
                            })
                            .setNegative("CANCEL", null)
                            .setPositive("CONTINUE", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //AA 08 06 03 00 00 00 0D 55
                                    byte[] cmds = {(byte) 0xAA, 0x08, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                            0x00, 0x00, 0x00, 0x00, 0x55};
                                    sendPackets(Utils.getSendData(cmds));

                                    showDialogCanCancel("", "Calibrating...");
                                    mHandler.postDelayed(dismissRunanble, Constants.READ_VERSION_TIMEOUT);
                                }
                            })
                            .configNeutral(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {

                                    params.textSize = DensityUtils.sp2px(mContext, 12);
                                }
                            })
                            .configPositive(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {

                                    params.textSize = DensityUtils.sp2px(mContext, 12);
                                }
                            })
                            .configNegative(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {

                                    params.textSize = DensityUtils.sp2px(mContext, 12);
                                }
                            })
                            .show(getSupportFragmentManager());

                } else if (adapter.getItem(position).equals(getString(R.string.setup_tutorial))) {

                    startActivity(new Intent(mContext, TutorialActivity.class));

                } else if (adapter.getItem(position).equals(getString(R.string.password_protect))) {

                    startActivity(new Intent(mContext, PasswordActivity.class));

                } else if (adapter.getItem(position).equals(getString(R.string.pair_switches))) {
                    showPairSwitches();

                } else if (adapter.getItem(position).equals(getString(R.string.pair_bulbs))) {

                    showPairBulbs();

                } else if (adapter.getItem(position).equals(getString(R.string.unpair_devices))) {

                    if (ProjectApp.getInstance().getSyncSwitches() == 0) {

                        new CircleDialog.Builder()
                                .setCanceledOnTouchOutside(false)
                                .setTitle("WARNING")
                                .setText("No switches found, cannot unpair.")
                                .setPositive("OK", null)
                                .show(getSupportFragmentManager());

                        return;

                    }

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Are you sure?")
                            .setText("ALL switches currently paired\nto this controller will now be unpaired.\n")
                            .setPositive("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    unpair();
                                    showDialog("", "Updating Controller...");
                                    mHandler.postDelayed(dismissRunanble, Constants.UNPAIR_DEVICE_TIMEOUT);
                                }
                            })
                            .setNegative("Cancel", null)
                            .show(getSupportFragmentManager());
                } else if (adapter.getItem(position).equals(getString(R.string.about))) {

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Light Stream App")
                            .setText("Version " + Utils.getLocalVersionName(mContext) + "\n")
                            .setPositive("OK", null)
                            .show(getSupportFragmentManager());
                }
            }
        });

    }

    private void startQueryUpdate(int year) {
        mHttpUtils.configCurrentHttpCacheExpiry(0);
        mHttpUtils.send(HttpRequest.HttpMethod.GET,
                year == 2019 ? BuildConfig.URL_CONTROLLER_LS3 : BuildConfig.URL_CONTROLLER_LS2,
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

                        Log.e("bulbs","queryVersion");
                        //1.查询属性
                        byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                        sendPackets(Utils.getSendData(query));
                        //5秒内没有固件回复，提示更新
                        mHandler.removeCallbacks(upgradeRunnable);
                        mHandler.postDelayed(upgradeRunnable, Constants.READ_VERSION_TIMEOUT);

                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                        sendPackets(Utils.getSendData(query));
                        //5秒内没有固件回复，提示更新
                        mHandler.removeCallbacks(upgradeRunnable);
                        mHandler.postDelayed(upgradeRunnable, Constants.READ_VERSION_TIMEOUT);

                    }
                });
    }

    private void showPairSwitches() {
        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .setTitle("Pair Switches")
                .setText("Any switches powered on, in range, and currently not paired to another device will be paired at this time.")
                .setPositive("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startPairSwitches();

                    }
                })
                .setNegative("Cancel", null)
                .show(getSupportFragmentManager());
    }

    private void showPairBulbs() {
        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .setTitle("Pair Bulbs")
                .setText("All bulbs connected directly to this controller or paired switches will be paired at this time.")
                .setPositive("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPairBulbs();
                    }
                })
                .setNegative("Cancel", null)
                .show(getSupportFragmentManager());
    }

    private void updateBulbFirmware() {

        if (Constants.FirmwareVersion <= 29) {
            new CircleDialog.Builder()
                    .setCanceledOnTouchOutside(false)
                    .setTitle("Firmware Compatibility Error")
                    .setText("This function is not compatible with your current firmware. To proceed, update you’re firmware")
                    .setPositive("OK", null)
                    .show(getSupportFragmentManager());
            return;
        }

        mHttpUtils.configCurrentHttpCacheExpiry(0);
        mHttpUtils.send(HttpRequest.HttpMethod.GET,
                is2019Device ? BuildConfig.URL_BULB_LS3 : BuildConfig.URL_BULB_LS2,
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
                                    .setTitle("Bulb Firmware Update")
                                    .setText("There are currently no firmware updates available for your bulbs.")
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

                            return;
                        }

                        final String version = updateBean.getFirmwares().get(0).getVersion()
                                .replace("V", "").replace("v", "");

                        String url = updateBean.getFirmwares().get(0).getUrl();
                        String fileName = url.substring(url.lastIndexOf("/"));

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

                                        Intent intent = new Intent(mContext, FirmwareBulbUpdateActivity.class);
                                        intent.putExtra("bin", bin);
                                        intent.putExtra("version", Short.parseShort(version));
                                        startActivity(intent);

                                    }


                                    @Override
                                    public void onFailure(HttpException error, String msg) {

                                        dismissDialog();
                                        if (error.getExceptionCode() == 404) {

                                            new CircleDialog.Builder()
                                                    .setCanceledOnTouchOutside(false)
                                                    .setTitle("Bulb Firmware Update")
                                                    .setText("There are currently no firmware updates available for your bulbs.")
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

                    @Override
                    public void onStart() {

                        showDialog("", "Checking...");
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                        dismissDialog();

                        if (error.getExceptionCode() == 404) {

                            new CircleDialog.Builder()
                                    .setCanceledOnTouchOutside(false)
                                    .setTitle("Bulb Firmware Update")
                                    .setText("There are currently no firmware updates available for your bulbs.")
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

    private void manageBulbChannels() {
        startActivity(new Intent(mContext, ManageChannelActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(upgradeRunnable);
        mHandler.removeCallbacks(mRunnable);
        if (mHttpHandler != null) {
            //调用cancel()方法停止下载
            mHttpHandler.cancel();
        }
    }

    @OnClick({R.id.btn_title_left, R.id.btn_pair_devices})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_pair_devices:
//                startActivity(new Intent(mContext, PairDevicesActivity.class));
                break;
        }
    }

    private void startPairSwitches() {

        dismissDialog();
        isPairSwitches = true;
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

    private void startPairBulbs() {

        dismissDialog();
        isPairBulbs = true;
        offReply = 0;
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, Constants.PAIR_TIMEOUT);
        showPairDialog(getString(R.string.pair_bulbs_title), getString(R.string.pair_bulbs_tips),
                ProjectApp.getInstance().getSyncSwitches(), getString(R.string.pair_device_end_button), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopPair();
                    }
                });

        byte[] cmdsPairStart = {(byte) 0xAA, 0x05, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x02, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsPairStart));
    }

    private void stopPair() {

        dismissDialog();
        LogUtils.d("stopPair...");

        mHandler.removeCallbacks(mRunnable);


        if (isPairSwitches) {

            Intent intent = new Intent(mContext, PairDeviceSucceedActivity.class);
            startActivityForResult(intent, 1024);

            isPairSwitches = false;
        }

        byte[] cmdsPairStop = {(byte) 0xAA, 0x05, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsPairStop));

        if (isPairBulbs) {
            /**
             * 点击End pairing发送AA 05停止pair，
             * 并弹窗Pairing Complete，点击OK发送AA 03 power off，
             * 收到回复AA 83弹窗Connection Check（#3），点击YES发送AA 03 power on并返回主页面，点击NO连发3次AA 03 power off
             * 收到3次AA 83转到第2个Connection Check弹窗（#4），点击Continue发送AA 03 power on并返回主页面
             */
            pairBulbsComplete();
        }
    }

    private void unpair() {
        byte[] cmdsUnpair = {(byte) 0xAA, 0x04, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x01, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsUnpair));
    }

    public void onEventMainThread(PairEvent event) {

        if (event.getBulbs() == -1 && event.getSwitches() == -1) {
            mTvBulbs.setText(ProjectApp.getInstance().getSyncBulbs() + " Bulbs");
            mTvSwitches.setText(ProjectApp.getInstance().getSyncSwitches() + " Switch(s)");
        } else {

            if (isPairSwitches) {
                showPairDialog(getString(R.string.pair_switches_title), getString(R.string.pair_switches_tips),
                        event.getSwitches(), getString(R.string.pair_device_end_button), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                stopPair();
                            }
                        });
            }

        }

    }

    public void onEventMainThread(DataEvent event) {

        byte[] datas = event.getDatas();

        if (datas[1] == (byte) 0x81) {

            if (isUpgradeController) {
                return;
            }

            isUpgradeController = true;

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

            if (onlineVersion > localVersion) {

                showUpgradeDialog();

            } else {

                dismissDialog();

                if (onlineVersion == 0) {

                    return;
                }

                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .setTitle("Your Firmware is up to date")
                        .setText("You may continue to force update your controller by pressing \"force update\"below. Otherwise press cancel to exit.\n\n" +
                                "Your current controller version: " + localVersion + "\n\n " +
                                "2018 bulbs v.40-49 / 2019 bulbs v.50+")
                        .setPositive("Cancel", null)
                        .setNegative("Force update", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                showUpgradeDialog();
                            }
                        })
                        .configNegative(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
                                params.textColor = Color.parseColor("#FF007AFF");
                            }
                        })
                        .show(getSupportFragmentManager());
            }

        } else if (datas[1] == (byte) 0x85) {

        } else if (datas[1] == (byte) 0x83) {//执行POWER ON/OFF等的回复

            if (isPowerOn) {
                isPowerOn = false;
                dismissDialog();
                finish();
                return;
            }

            offReply++;
            LogUtils.d("off count:" + offReply);
            if (offReply == 1) {

                dismissDialog();

                if (isUpgradeController) {//要求Controller Firmware update不出现此对话框
                    return;
                }

                new CircleDialog.Builder()
                        .setCancelable(false)
                        .setTitle("Connection Check")
                        .setText("Are all the switches and bulbs paired to this controller now off?")
                        .setPositive("YES", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog("", "Updating Controller...");
                                powerOn();
                            }
                        })
                        .setNegative("NO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog("", "Updating Controller...");
                                powerOff();
                            }
                        })
                        .show(getSupportFragmentManager());
            } else if (offReply < 4) {
                powerOff();
            } else if (offReply == 4) {

                dismissDialog();

                if (isUpgradeController) {//要求Controller Firmware update不出现此对话框
                    return;
                }

                new CircleDialog.Builder()
                        .setCancelable(false)
                        .setTitle("Connection Check")
                        .setText("Are they off now? If not, you may have a pairing issue. Force-pair the switch that’s not responding.\n\n" +
                                "For pairing questions or troubleshoot- ing, visit www.VLCPRO.com/LSHelp.")
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog("", "Updating Controller...");
                                powerOn();
                            }
                        })
                        .show(getSupportFragmentManager());
            }
        } else if (datas[1] == (byte) 0x88) {//Calibrate Photocell

            dismissDialog();

            byte[] values = {datas[11], datas[10], datas[9], datas[8]};
            int value = Integer.parseInt(TransUtils.bytes2hex(values), 16);

            new CircleDialog.Builder()
                    .setCanceledOnTouchOutside(false)
                    .setTitle("Photocell Calibration Successful")
                    .setText("Your photocell has been calibrated to:\n" + value)
                    .setPositive("CONTINUE", null)
                    .show(getSupportFragmentManager());

        }
    }


    private void showUpgradeDialog() {

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

                        isUpgradeController = true;

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

    public void onEventMainThread(UnpairEvent event) {

//        if (isForeground(mContext, PairDeviceSucceedActivity.class.getName())) {
//
//            return;
//        }

        mHandler.removeCallbacks(dismissRunanble);

        dismissDialog();

        startActivity(new Intent(mContext, PairDeviceFailedActivity.class));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1024) {
                startPairSwitches();
            }
        } else if (resultCode == RESULT_FIRST_USER) {
            setResult(resultCode);
            finish();
        }

    }

    private void powerOff() {

        byte[] cmdsOff = {(byte) 0xAA, 0x03, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(), (byte) 0x00, (byte) 0x00, 0x00, 0x01, 0x00, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsOff));
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(MainActivity.Key_App_Get_Started, false)) {
            sharedPreferences.edit()
                    .putBoolean(MainActivity.Key_App_Get_Started, false)
                    .putBoolean(MainActivity.Key_Add_Phone, true)
                    .commit();
        }
        finish();
    }


    private void powerOn() {
        isPowerOn = true;
        byte[] cmdsOn = {(byte) 0xAA, 0x03, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(), (byte) 0x00, (byte) 0x00, 0x00, 0x01, 0x01, 0x00, 0x55};
        sendPackets(Utils.getSendData(cmdsOn));
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(MainActivity.Key_App_Get_Started, false)) {
            sharedPreferences.edit()
                    .putBoolean(MainActivity.Key_App_Get_Started, false)
                    .putBoolean(MainActivity.Key_Add_Phone, true)
                    .commit();
        }
        finish();
    }

    private void pairBulbsComplete() {

        if (isUpgradeController) {//要求Controller Firmware update不出现此对话框
            return;
        }

        new CircleDialog.Builder()
                .setCancelable(false)
                .setTitle("Pairing Complete")
                .setText("")
                .setPositive("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showDialog("", "Updating Controller...");
                        powerOff();
                    }
                })
                .show(getSupportFragmentManager());
    }

}