package com.villagelight.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
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
import com.mylhyl.circledialog.view.listener.OnInputClickListener;
import com.villagelight.app.BuildConfig;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.ChannelEvent;
import com.villagelight.app.event.DataEvent;
import com.villagelight.app.event.DisconnectEvent;
import com.villagelight.app.event.PairEvent;
import com.villagelight.app.event.ScheduleEvent;
import com.villagelight.app.event.UnpairEvent;
import com.villagelight.app.model.ChannelBean;
import com.villagelight.app.model.ColorBean;
import com.villagelight.app.model.ControllerBean;
import com.villagelight.app.model.CustomSchedule;
import com.villagelight.app.model.SimpleSchedule;
import com.villagelight.app.model.ThemeColor;
import com.villagelight.app.model.UpdateBean;
import com.villagelight.app.util.DensityUtils;
import com.villagelight.app.util.GsonUtil;
import com.villagelight.app.util.LogUtils;
import com.villagelight.app.util.ToastUtils;
import com.villagelight.app.util.Utils;
import com.villagelight.app.view.SelectDialog;
import com.weigan.loopview.OnItemSelectedListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.RFStarBLEService;
import cn.fly2think.blelib.TransUtils;
import de.greenrobot.event.EventBus;

public class ControlMenuActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.btn_color_selector)
    TextView mBtnColorSelector;
    @BindView(R.id.tv_active_theme)
    TextView mTvActiveTheme;
    @BindView(R.id.tv_simple_schedule)
    TextView mTvSimpleSchedule;
    @BindView(R.id.tv_monday)
    TextView mTvMonday;
    @BindView(R.id.tv_tuesday)
    TextView mTvTuesday;
    @BindView(R.id.layout_simple_schedule)
    LinearLayout mLayoutSimpleSchedule;
    @BindView(R.id.tv_bulbs)
    TextView mTvBulbs;
    @BindView(R.id.tv_switches)
    TextView mTvSwitches;
    @BindView(R.id.tv_wednesday)
    TextView mTvWednesday;
    @BindView(R.id.tv_thursday)
    TextView mTvThursday;
    @BindView(R.id.tv_friday)
    TextView mTvFriday;
    @BindView(R.id.tv_saturday)
    TextView mTvSaturday;
    @BindView(R.id.tv_sunday)
    TextView mTvSunday;
    @BindView(R.id.tv_custom_schedule)
    TextView mTvCustomSchedule;
    @BindView(R.id.tv_upcoming_theme)
    TextView mTvUpcomingTheme;
    @BindView(R.id.layout_customer_schedule)
    LinearLayout mLayoutCustomerSchedule;
    @BindView(R.id.iv_sync)
    ImageView mIvSync;
    @BindView(R.id.tv_sync)
    TextView mTvSync;
    @BindView(R.id.lv)
    ListView mLv;
    @BindView(R.id.rbtn_power_on)
    RadioButton mRbtnPowerOn;
    @BindView(R.id.rbtn_power_off)
    RadioButton mRbtnPowerOff;
    @BindView(R.id.rbtn_schedule_on)
    RadioButton mRbtnScheduleOn;
    @BindView(R.id.rgroup)
    RadioGroup mRgroup;
    private ArrayList<ColorBean> colors1 = new ArrayList<>();
    private ArrayList<ColorBean> colors2 = new ArrayList<>();
    private ArrayList<ColorBean> colors3 = new ArrayList<>();
    private List<String> colors = new ArrayList<>();
    private List<ThemeColor> mThemeColors = new ArrayList<>();
    private int selected;
    private boolean isPowerOn;
    private HttpUtils mHttpUtils = new HttpUtils();
    private short onlineVersion;
    private int localVersion;
    private String fileName;
    private String url;
    private boolean isReconnect = false;
    private List<Byte> mRecvs = new ArrayList<>();
    private int mLen;
    private ControllerBean mCurrentController;
    private boolean isMainChecked;
    private HttpHandler mHttpHandler;
    private boolean isPairSwitches;
    private boolean isPairBulbs;
    private boolean isNowCheckLastPair = false;
    private int offReply = -1;
    private boolean isUpgradeController;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            dismissDialog();
        }
    };
    private int mSwitchDelay = 2000;

    Runnable mRunnablePairSwitches = new Runnable() {
        @Override
        public void run() {
            Log.e("dismissDialog","dismissDialog mRunnablePairSwitches");
            stopPair();
        }
    };

    private Runnable mRunnablePairBulbs = new Runnable() {
        @Override
        public void run() {

            Log.e("dismissDialog","dismissDialog mRunnablePairBulbs");
            stopPair();
        }
    };

    private Runnable upgradeRunnable = new Runnable() {
        @Override
        public void run() {

            updateErrorCount ++;

            //重试5次
            if (updateErrorCount <= 5){
                queryVersion();
            }else {
                if (!TextUtils.isEmpty(url)) {
                    FirmwareError();
                }
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

    private Runnable checkFirmware = new Runnable() {
        @Override
        public void run() {

            Log.e("bulbs","checkFirmware");
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
    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {

            dismissDialog();
            new CircleDialog.Builder()
                    .setCanceledOnTouchOutside(false)
                    .setTitle("Controller Firmware Issue")
                    .setText("There seems to be a problem with your controller. Please connect again and select YES to install firmware when prompted.")
                    .setPositive("Continue", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            disconnect();
                            finish();
                        }
                    })
                    .show(getSupportFragmentManager());
//            new CircleDialog.Builder()
//                    .setCanceledOnTouchOutside(false)
//                    .configDialog(new ConfigDialog() {
//                        @Override
//                        public void onConfig(DialogParams params) {
////                                params.backgroundColor = Color.DKGRAY;
////                                params.backgroundColorPress = Color.BLUE;
//                        }
//                    })
//                    .setTitle("Sync failed")
//                    .setText("Timeout")
//                    .configText(new ConfigText() {
//                        @Override
//                        public void onConfig(TextParams params) {
////                                    params.padding = new int[]{150, 10, 50, 10};
//                        }
//                    })
//                    .setPositive("Try again", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                            recreate();
//                        }
//                    })
//                    .setNegative("Cancel", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            finish();
//                        }
//                    })
//                    .configPositive(new ConfigButton() {
//                        @Override
//                        public void onConfig(ButtonParams params) {
////                                    params.backgroundColorPress = Color.RED;
//                        }
//                    })
//                    .show(getSupportFragmentManager());
        }
    };

    private int themeAmount;
    private int count;
    private ThemeColor themeColor;
    private boolean canNewTheme = true;
    private int activityTheme = -1;
    private SimpleDateFormat sdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
    private boolean isSync = false;
    private boolean isReadThemeOnly = false;
    private String theme_power_on;
    private String theme_schedule_on;

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_menu);
        ButterKnife.bind(this);

        if (ProjectApp.getInstance().getCurrentControl() != null) {
            mTvTitle.setText(ProjectApp.getInstance().getCurrentControl().getControllerName());
            mCurrentController = ProjectApp.getInstance().getCurrentControl();
        }

        ProjectApp.getInstance().getSyncThemeColors().clear();
        ProjectApp.getInstance().setSyncBulbs(0);
        ProjectApp.getInstance().setSyncSwitches(0);
        ProjectApp.getInstance().setCustomSchedule(false);
        ProjectApp.getInstance().getSyncSimpleSchedule().clear();
        ProjectApp.getInstance().getSyncCustomSchedule().clear();

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mBtnColorSelector.setEnabled(false);

        initMenu();
        initColors();

        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("setOnItemClickListener","position:"+position);
                if (position == 3){
                    //Faq
                    //https://villagelighting.com/blogs/light-stream#faq
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://villagelighting.com/blogs/light-stream#faq"));
                    startActivity(browserIntent);
                }
                if (!isPowerOn) {
                    return;
                }

                if (position == 0) {

                    startActivityForResult(new Intent(mContext, ThemeColorActivity.class), 1024);

                } else if (position == 1) {

                    startActivityForResult(new Intent(mContext, ScheduleActivity.class), 2048);

                } else if (position == 2) {

                    if (mRbtnScheduleOn.isChecked()) {
                        return;
                    }

                    startActivityForResult(new Intent(mContext, ManageDeviceActivity.class),4096);
                }
            }
        });

        mRgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {

                    case R.id.rbtn_power_on:

                        isPowerOn = true;
                        initMenu();
                        mBtnColorSelector.setEnabled(true);
                        if (ProjectApp.getInstance().isCustomSchedule()) {
                            mLayoutSimpleSchedule.setVisibility(View.GONE);
                            mLayoutCustomerSchedule.setVisibility(View.INVISIBLE);
                        } else {
                            mLayoutSimpleSchedule.setVisibility(View.INVISIBLE);
                            mLayoutCustomerSchedule.setVisibility(View.GONE);
                        }

                        if (mRbtnPowerOn.isPressed()) {
                            showDialog("", "Updating Controller...");
                            mHandler.removeCallbacks(mRunnable);
                            mHandler.postDelayed(mRunnable, TIMEOUT);
                            byte[] cmdsOn = {(byte) 0xAA, 0x03, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(), (byte) 0x00, (byte) 0x00, 0x00, 0x01, 0x01, 0x00, 0x55};
                            sendPackets(Utils.getSendData(cmdsOn));
                            mTvActiveTheme.setText("  " + theme_power_on + "  ");
                        }
                        mTvActiveTheme.setTextColor(getResources().getColor(R.color.text_color_main));
                        mTvActiveTheme.setBackgroundResource(R.drawable.white_circle_drawable);
                        break;
                    case R.id.rbtn_power_off:

                        isPowerOn = false;
                        initMenu();
                        mBtnColorSelector.setEnabled(false);
                        if (ProjectApp.getInstance().isCustomSchedule()) {
                            mLayoutSimpleSchedule.setVisibility(View.GONE);
                            mLayoutCustomerSchedule.setVisibility(View.INVISIBLE);
                        } else {
                            mLayoutSimpleSchedule.setVisibility(View.INVISIBLE);
                            mLayoutCustomerSchedule.setVisibility(View.GONE);
                        }

                        if (mRbtnPowerOff.isPressed()) {
                            showDialog("", "Updating Controller...");
                            mHandler.removeCallbacks(mRunnable);
                            mHandler.postDelayed(mRunnable, TIMEOUT);
                            byte[] cmdsOff = {(byte) 0xAA, 0x03, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(), (byte) 0x00, (byte) 0x00, 0x00, 0x01, 0x00, 0x00, 0x55};
                            sendPackets(Utils.getSendData(cmdsOff));

                            mTvActiveTheme.setText("  OFF  ");
                        }
                        mTvActiveTheme.setTextColor(getResources().getColor(R.color.text_color_third));
                        mTvActiveTheme.setBackgroundResource(R.drawable.gray_cycle_drawable);
                        break;
                    case R.id.rbtn_schedule_on:

                        isPowerOn = true;
                        initMenu();
                        mBtnColorSelector.setEnabled(false);
                        if (ProjectApp.getInstance().isCustomSchedule()) {
                            mLayoutSimpleSchedule.setVisibility(View.GONE);
                            mLayoutCustomerSchedule.setVisibility(View.VISIBLE);
                        } else {
                            mLayoutSimpleSchedule.setVisibility(View.VISIBLE);
                            mLayoutCustomerSchedule.setVisibility(View.GONE);
                        }

                        if (mRbtnScheduleOn.isPressed()) {
                            showDialog("", "Updating Controller...");
                            mHandler.removeCallbacks(mRunnable);
                            mHandler.postDelayed(mRunnable, Constants.EXEC_POWER_TIMEOUT);
                            byte[] cmdsSc = {(byte) 0xAA, 0x03, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(), (byte) 0x00, (byte) 0x00, 0x00, 0x01, 0x02, 0x00, 0x55};
                            sendPackets(Utils.getSendData(cmdsSc));
                            mTvActiveTheme.setText("  " + theme_schedule_on + "  ");
                        }
                        mTvActiveTheme.setTextColor(getResources().getColor(R.color.text_color_second));
                        mTvActiveTheme.setBackgroundResource(R.drawable.white_circle_drawable);
                        break;

                }
            }
        });


//        syncContriller();//debug 1363hang/开始uodate firemware
        syncContriller();//debug 1363hang/开始uodate firemware
    }

    private void showTutorialDialog() {
        sharedPreferences = getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        boolean isGetStarted = sharedPreferences.getBoolean(MainActivity.Key_App_Get_Started,false);
        boolean isAddPhone = sharedPreferences.getBoolean(MainActivity.Key_Add_Phone,false);

        if (isGetStarted == false && isAddPhone == false){
        }else if (isGetStarted){

            String number1 = "";
            String number2 = "";
            if (mCurrentController.getYear() == 2019){
                number1 = "3";
                number2 = "2";
            }else {
                number1 = "2";
                number2 = "3";
            }

            String message = "Your device has been connected to this controller and has been set up to be used with Light Stream "+number1
                    +" Bulbs. If you're using legacy Light Stream "
                    +number2+" Bulbs, you may change this setting in Manage Devices.";
            new CircleDialog.Builder()
                    .setCanceledOnTouchOutside(false)
                    .setTitle("Device Connection Successful")
                    .setText(message)
                    .setGravity(Gravity.CENTER)
                    .setPositive("Continue with Setup", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            show12B4();
                        }
                    })
                    .show(getSupportFragmentManager());
        }else if (isAddPhone){
            showAllSet();
        }

        sharedPreferences.edit()
                .putBoolean(MainActivity.Key_App_Get_Started,false)
                .putBoolean(MainActivity.Key_Add_Phone,false)
                .commit();
    }

    void showAllSet(){
        sharedPreferences.edit()
                .putBoolean(MainActivity.Key_App_Get_Started,false)
                .putBoolean(MainActivity.Key_Add_Phone,false)
                .commit();

        List<String> menuList = new ArrayList<>();
        menuList.add("Want more tips and tricks?\n" +
                "View our videos HERE");
        menuList.add("Done");
        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .setTitle("You’re all set!")
                .setSubTitle("Try creating a theme and\n" +
                        "setting a schedule.")
                .setItems(menuList, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0){
                            Uri uri = Uri.parse(Constants.URL_HELP);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }else {

                        }
                    }
                })
                .configItems(new ConfigItems() {
                    @Override
                    public void onConfig(ItemsParams params) {
                        params.textColor = Color.BLACK;
                    }
                })
                .setGravity(Gravity.CENTER)
                .show(getSupportFragmentManager());
    }

    private void showPairSwitches() {
        showPairSwitchesAct();
//        Intent intent = new Intent();
//        intent.putExtra(ManageDeviceActivity.Key_PairSwitch,true);
//        intent.setClass(ControlMenuActivity.this,ManageDeviceActivity.class);
//        startActivityForResult(intent,4096);
    }
    private void showPairSwitchesAct() {
        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
                        params.mPadding = new int[] {16, 16, 16, 16};
                    }
                })
                .setTitle("Pair Switches")
                .setText("All switches powered on, in range, and currently not paired to another device will be paired at this time.")
                .setPositive("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startPairSwitches();

                    }
                })
                .setNegative("Cancel", null)
                .configNegative(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
                        params.textColor = Color.parseColor("#0076FF");
                    }
                })
                .show(getSupportFragmentManager());
    }

    private void startPairSwitches() {
        dismissDialog();
        isPairSwitches = true;
        mHandler.removeCallbacks(mRunnablePairSwitches);
        mHandler.postDelayed(mRunnablePairSwitches, mSwitchDelay);
        showPairDialog(getString(R.string.pair_switches_title), getString(R.string.pair_switches_tips),
                ProjectApp.getInstance().getSyncSwitches(), getString(R.string.pair_device_end_button), new View.OnClickListener() {
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
        mHandler.removeCallbacks(mRunnablePairBulbs);
        mHandler.postDelayed(mRunnablePairBulbs, Constants.PAIR_TIMEOUT);
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
        mHandler.removeCallbacks(mRunnable);
        disMissPairDialog();
        LogUtils.d("stopPair...");

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

    private void pairBulbsComplete() {
        offReply = 0;

        if (isUpgradeController) {//要求Controller Firmware update不出现此对话框
            return;
        }
        isPairBulbs = false;

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
        mRgroup.check(mRbtnPowerOn.getId());
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
        mRgroup.check(mRbtnPowerOff.getId());
//        finish();
    }

    private void showPairBulbs() {

        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .setTitle("Next Step: Pair Bulbs")
                .setText("Bulbs plugged into this\n" +
                        "controller and any paired\n" +
                        "switches will be paired now.")
                .setPositive("Ok", v->{
                      startPairBulbs();
//                    Intent intent = new Intent();
//                    intent.putExtra(ManageDeviceActivity.Key_PairBulbs,true);
//                    intent.setClass(ControlMenuActivity.this,ManageDeviceActivity.class);
//                    startActivityForResult(intent,4096);
                })
                .setNegative("Cancel",v->{
                    sharedPreferences.edit()
                            .putBoolean(MainActivity.Key_App_Get_Started,false)
                            .commit();
                })
                .configNegative(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
                        params.textColor = Color.parseColor("#0076FF");
                    }
                })
//                .setNeutral("Cancel",null)
                .setGravity(Gravity.CENTER)
                .show(getSupportFragmentManager());
    }

    private void syncContriller() {
        showDialog("", "Synchronizing controller and App.\nThis may take up to one minute");

        //1.查询Currently paired device
        byte[] query1 = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x00, 0x55};
        sendPackets(Utils.getSendData(query1));

        mHandler.removeCallbacks(mTimeoutRunnable);
        mHandler.postDelayed(mTimeoutRunnable, Constants.SYNC_TIMEOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mTimeoutRunnable);
        mHandler.removeCallbacks(mRunnable);
    }

    @OnClick({R.id.btn_title_left, R.id.btn_color_selector, R.id.iv_sync, R.id.tv_sync})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                disconnect();
                finish();
                break;
            case R.id.btn_title_right:
                String[] items = {"Turn Lights On", "Turn Lights Off", "Activate Schedule", "Cancel"};
                new CircleDialog.Builder()
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
//                                params.backgroundColorPress = Color.CYAN;
                                //增加弹出动画
//                                params.animStyle = R.style.dialogWindowAnim;
                            }
                        })
                        .setTitle("Power Menu")
//                        .setTitleColor(Color.BLUE)
                        .configTitle(new ConfigTitle() {
                            @Override
                            public void onConfig(TitleParams params) {
//                                params.backgroundColor = Color.RED;
                            }
                        })
                        .setSubTitle("Select a power option below to use The Light Stream™ System.")
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

                                switch (position) {
                                    case 0:

                                        sendPackets(TransUtils.hex2bytes("AA 14 06 00 00 00 01 13 55".replace(" ", "")));

//                                        mBtnControl.setImageResource(R.mipmap.ic_power_on_selected);
                                        mBtnColorSelector.setEnabled(true);
                                        mLayoutSimpleSchedule.setVisibility(View.INVISIBLE);
                                        break;
                                    case 1:

                                        sendPackets(TransUtils.hex2bytes("AA 14 06 00 00 00 01 14 52".replace(" ", "")));

//                                        mBtnControl.setImageResource(R.mipmap.ic_power_off_selected);
                                        mBtnColorSelector.setEnabled(false);
                                        mLayoutSimpleSchedule.setVisibility(View.INVISIBLE);
                                        break;
                                    case 2:

                                        sendPackets(TransUtils.hex2bytes("AA 14 08 00 10 00 01 13 51".replace(" ", "")));

//                                        mBtnControl.setImageResource(R.mipmap.ic_schedule_on_selected);
                                        mBtnColorSelector.setEnabled(false);
                                        mLayoutSimpleSchedule.setVisibility(View.VISIBLE);
                                        break;
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
                break;
            case R.id.btn_color_selector:

                SelectDialog.getInstance()
                        .setItems(colors)
                        .setInitPosition(selected)
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                theme_power_on = colors.get(index);
                                mTvActiveTheme.setText("  " + theme_power_on + "  ");
                                selected = index;

                                showDialog("", "Updating Controller...");
                                mHandler.removeCallbacks(mRunnable);
                                mHandler.postDelayed(mRunnable, Constants.EXEC_THEME_TIMEOUT);

                                ThemeColor themeColor = mThemeColors.get(index);
                                byte ID = (byte) themeColor.getId();

                                byte[] cmdsTH = {(byte) 0xAA, 0x14, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                        (byte) 0x00, (byte) 0x00, ID, 0x00, 0x55};
                                sendPackets(Utils.getSendData(cmdsTH));

                            }
                        })
                        .show(getSupportFragmentManager(), "SelectDialog");

                break;
            case R.id.iv_sync:
                break;
            case R.id.tv_sync:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        syncThemeColorMenu();

    }

    private void show12B4() {
        List<String> menuList = new ArrayList<>();
        menuList.add("NO - Just a controller");
        menuList.add("YES - I have at least 1 switch");
        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
                        params.mPadding = new int[] {16, 16, 16, 16};
                    }
                })
                .setTitle("Are you using a controller\n" +
                        "AND a switch on this system?")
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.height = 350;
                    }
                })
                .configItems(new ConfigItems() {
                    @Override
                    public void onConfig(ItemsParams params) {
                        params.textColor = Color.parseColor("#0076FF");
                    }
                })
                .setItems(menuList, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0){

                            showPairBulbs();
                        }else {

                            showPairSwitches();
                        }
                    }
                })
                .setGravity(Gravity.CENTER)
                .show(getSupportFragmentManager());
    }

    private void syncThemeColorMenu() {
        mThemeColors.clear();
        colors.clear();
        mThemeColors.addAll(app.getDefaultThemeColors());
//        List<ThemeColor> list = null;
//        try {
//            list = ProjectApp.getInstance().getDb()
//                    .findAll(Selector.from(ThemeColor.class).where("cid", "=",
//                            ProjectApp.getInstance().getCurrentControl().getId()));
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
//        if (list != null && !list.isEmpty()) {
//
//            for (ThemeColor themeColor : list) {
//                mThemeColors.add(themeColor);
//            }
//        }

        mThemeColors.addAll(app.getSyncThemeColors());

        for (ThemeColor themeColor : mThemeColors) {
            colors.add(themeColor.getName());
        }
    }

    private void syncConfirmDialog() {


        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle("Don’t Forget to Sync")
//                .setText("Leaving without syncing changes will\nrevert to current controller settings.")
                .setText("If  update the controller at once?\n")
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setNegative("Cancel", null)
                .setPositive("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        byte[] syncs = {(byte) 0xAA, 0x03, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                (byte) 0x00, (byte) 0x00, 0x00, 0x01, 0x02, 0x00, 0x55};
                        sendPackets(Utils.getSendData(syncs));

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && resultCode == RESULT_OK) {
              ThemeColor themeColor = (ThemeColor) data.getExtras().getSerializable("THEME");
              colors.add(themeColor.getName());
              mThemeColors.add(themeColor);
        }

        if (requestCode == 2048 && resultCode == RESULT_OK) {

            if (mRbtnScheduleOn.isChecked()) {
//                syncConfirmDialog();
//                showDialogCanCancel("", "Updating...");
//                mHandler.postDelayed(mRunnable, 30 * 1000);
            }

        }

        if (requestCode == 1024 && resultCode == PairDeviceSucceedActivity.RESULT_FIRST_USER){
            showPairBulbs();
        }
    }

    private void initMenu() {

        ArrayAdapter<String> adapter;
        if (isPowerOn) {
            adapter = new ArrayAdapter<>(mContext, R.layout.item_lv_menu);
        } else {
            adapter = new ArrayAdapter<>(mContext, R.layout.item_lv_menu_disable);
        }
        mLv.setAdapter(adapter);

        adapter.add("Themes & Colors");
        adapter.add("Schedule");
        adapter.add("Manage Devices");
        adapter.add("FAQ's");
    }

    public void onEventMainThread(ScheduleEvent event) {

        ProjectApp.getInstance().setCustomSchedule(false);
        ProjectApp.getInstance().getSyncSimpleSchedule().clear();
        ProjectApp.getInstance().getSyncCustomSchedule().clear();

        byte[] query4 = {(byte) 0xAA, 0x30, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, (byte) 0xff, 0x00, 0x55};
        sendPackets(Utils.getSendData(query4));

        if (event.isShowDialog()) {
            /**
             * 2019-07-25
             * 要求是schedule on情况下修改save schedule数据后才有此弹窗，
             * 如果是power ON 下修改并save schedule数据后不需要此弹窗
             */
            if (mRbtnScheduleOn.isChecked()) {
                showDialog("", "Updating Controller...");
            }

        }

        mHandler.postDelayed(mRunnable, Constants.UPDATE_SCHEDULE_TIMEOUT);

    }

    private boolean hasSetControlerType = false;
    public void onEventMainThread(DataEvent event) {

        byte[] datas = event.getDatas();
        if (datas[1] == (byte) 0x00) {//pair device 时的递增设备数量

            int bulbs = datas[datas.length - 4] & 0xFF;
            int switches = datas[datas.length - 3] & 0xFF;

            EventBus.getDefault().post(new PairEvent(bulbs, switches));

        } else if (datas[1] == (byte) 0x81) {//读取设备数量

            int bulbs = datas[datas.length - 4] & 0xFF;
            int switches = datas[datas.length - 3] & 0xFF;

            if (bulbs == 0){
                mCurrentController.setYear(2019);
            }else {
                mCurrentController.setYear(2018);
            }
            if (hasSetControlerType == false){
                hasSetControlerType = true;
                mHandler.removeCallbacks(checkFirmware);
                mHandler.postDelayed(checkFirmware, 100);
                showTutorialDialog();
            }
            Log.e("bulbs","bulbs:"+bulbs + " " + mCurrentController);
            ProjectApp.getInstance().setSyncBulbs(bulbs);
            ProjectApp.getInstance().setSyncSwitches(switches);

            mTvBulbs.setText(bulbs + " Bulbs");
            mTvSwitches.setText(switches + " Switch(s)");

            EventBus.getDefault().post(new PairEvent()); //以前为什么会用这个？为了避免和pair的时候递增的数量区分
//            EventBus.getDefault().post(new PairEvent(bulbs, switches));

            if (!isSync) {//避免重复查询
                isSync = true;
                //2.查询设备状态
                byte[] query2 = {(byte) 0xAA, 0x02, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                        (byte) 0x00, (byte) 0x00, 0x00, 0x55};
                sendPackets(Utils.getSendData(query2));

                mHandler.removeCallbacks(mTimeoutRunnable);
                mHandler.postDelayed(mTimeoutRunnable, Constants.SYNC_TIMEOUT);
            }

        } else if (datas[1] == (byte) 0x82) {//读取POWER状态

            int status = datas[8] & 0xFF;
            if (status == 0) {

                mRgroup.check(mRbtnPowerOff.getId());

            } else if (status == 1) {

                mRgroup.check(mRbtnPowerOn.getId());

            } else if (status == 2) {

                mRgroup.check(mRbtnScheduleOn.getId());

            }

            //3.查询theme，当查询 ID 号为 0xFF 返回 表示获取设备所有的 ID 编号，主要功能:告诉查询者设备当前有哪些 ID 编号。
            syncThemeColor();

            mHandler.removeCallbacks(mTimeoutRunnable);
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, Constants.SYNC_TIMEOUT);


        } else if (datas[1] == (byte) 0x83) {//执行POWER ON/OFF等的回复
            Log.e("dismissDialog","dismissDialog 0x83");
            mHandler.removeCallbacks(mRunnable);
            dismissDialog();

            if (isForeground(mContext, ControlMenuActivity.class.getName())) {

                isReadThemeOnly = true;

                //重新查询theme，为了显示正确的ActivityTheme
                syncThemeColor();
            }

            if (offReply == 0){
                isNowCheckLastPair = true;
            }
            if (isNowCheckLastPair){

                offReply++;
                LogUtils.d("off count:" + offReply);
                if (offReply == 1) {

                    disMissPairDialog();

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
                                    isNowCheckLastPair = false;
//                                    showDialog("", "Updating Controller...");
                                    powerOn();
                                    showAllSet();
                                }
                            })
                            .setNegative("NO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                    showDialog("", "Updating Controller...");
                                    isNowCheckLastPair = false;
                                    powerOff();
                                    showAllSet();
                                }
                            })
                            .configNegative(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {
                                    params.textColor = Color.parseColor("#0076FF");
                                }
                            })
                            .show(getSupportFragmentManager());
                } else if (offReply < 4) {
                    powerOff();
                } else if (offReply == 4) {

//                    dismissDialog();
//
//                    if (isUpgradeController) {//要求Controller Firmware update不出现此对话框
//                        return;
//                    }
//
//                    new CircleDialog.Builder()
//                            .setCancelable(false)
//                            .setTitle("Connection Check")
//                            .setText("Are they off now? If not, you may have a pairing issue. Force-pair the switch that’s not responding.\n\n" +
//                                    "For pairing questions or troubleshoot- ing, visit www.VLCPRO.com/LSHelp.")
//                            .setPositive("Continue", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    showDialog("", "Updating Controller...");
//                                    powerOn();
//                                }
//                            })
//                            .show(getSupportFragmentManager());
                }
            }

        } else if (datas[1] == (byte) 0x84) {//Unpair Device 回复

            EventBus.getDefault().post(new UnpairEvent());

        } else if (datas[1] == (byte) 0x86) {//Manage Channels 回复

            EventBus.getDefault().post(new ChannelEvent());

        } else if (datas[1] == (byte) 0x90) {//读取theme数据

            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, Constants.SYNC_TIMEOUT);

            if (datas[7] == 0x05) { //theme amount

                themeAmount = datas[9] & 0xFF;
                LogUtils.d("themeAmount:" + themeAmount);
                activityTheme = datas[12] & 0xFF;
                LogUtils.d("activityTheme:" + activityTheme);
                if (themeAmount == 0) {

                    syncThemeColorMenu();

                    if (activityTheme != -1) {
                        for (ThemeColor tc : mThemeColors) {
                            if (tc.getId() == activityTheme) {
                                theme_power_on = tc.getName();
                                if (mRbtnPowerOn.isChecked()) {
                                    mTvActiveTheme.setText("  " + theme_power_on + "  ");
                                }
                                break;
                            }
                        }
                    }

                    if (!isReadThemeOnly) {
                        //4、查询schedule，当查询 ID 号为 0xFF 返回 表示获取设备所有的 ID 编号，主要功能:告诉查询者设备当前有哪些 ID 编号。
                        onEventMainThread(new ScheduleEvent(false));
                    }

                    isReadThemeOnly = false;
                }

            } else if (datas[7] == 0x03) {//theme channel

                if (canNewTheme) {
                    canNewTheme = false;
                    themeColor = new ThemeColor();
                }

                themeColor.setId(datas[6] & 0xFF);

                ChannelBean channel = new ChannelBean();

                String channelName = "Channel " + (datas[9] & 0xFF);
                channel.setName(channelName);
                channel.setColorNo1(datas[10] & 0XFF);
                channel.setColorNo2(datas[11] & 0XFF);
                channel.setColorNo3(datas[12] & 0XFF);
                channel.setDisplayColor1(findColor(channel.getColorNo1()).getDisplayColor());
                channel.setDisplayColor2(findColor(channel.getColorNo2()).getDisplayColor());
                channel.setDisplayColor3(findColor(channel.getColorNo3()).getDisplayColor());
                byte XS = datas[13];
                channel.setTwinkleOn((XS & 0x80) == 128);
                themeColor.setFade(XS & 0x7F);
                List<ChannelBean> channelBeanList = themeColor.getChannels();
                boolean hasAddChannel = false;
                if (channelBeanList != null){
                    for (ChannelBean one :
                            channelBeanList) {
                        if (one.getName().equals(channelName)){
                            hasAddChannel = true;
                        }
                    }
                }
                if (!hasAddChannel){
                    themeColor.getChannels().add(channel);
                }

            } else if (datas[7] == 0x04) {//theme name

                count++;

                int themeNameLen = datas[8] & 0xFF;
                byte[] names = new byte[themeNameLen];
                System.arraycopy(datas, 9, names, 0, themeNameLen);

                themeColor.setName(new String(names));

                LogUtils.d("themeColorthemeColor:" + themeColor.getName());

                if (!app.getSyncThemeColors().contains(themeColor)) {
                    app.getSyncThemeColors().add(themeColor);
                }

                canNewTheme = true;

                if (count == themeAmount) {
                    count = 0;

                    syncThemeColorMenu();

                    if (activityTheme != -1) {
                        for (ThemeColor tc : mThemeColors) {
                            if (tc.getId() == activityTheme) {
                                theme_power_on = tc.getName();
                                if (mRbtnPowerOn.isChecked()) {
                                    mTvActiveTheme.setText("  " + theme_power_on + "  ");
                                }
                                break;
                            }
                        }
                    }

                    if (!isReadThemeOnly) {

                        //4、查询schedule，当查询 ID 号为 0xFF 返回 表示获取设备所有的 ID 编号，主要功能:告诉查询者设备当前有哪些 ID 编号。
                        onEventMainThread(new ScheduleEvent(false));
                    }

                    isReadThemeOnly = false;
                }


            }
        } else if (datas[1] == (byte) 0x94) {//执行theme回复
            Log.e("dismissDialog","dismissDialog 0x94");
            mHandler.removeCallbacks(mRunnable);
            dismissDialog();

        } else if (datas[1] == (byte) 0xB0) {//schedule数据

            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, Constants.SYNC_TIMEOUT);

            if (datas.length == 18) {//simple schedule - 18bytes

                ProjectApp.getInstance().setCustomSchedule(false);

                if (mRbtnScheduleOn.isChecked()) {
                    mLayoutCustomerSchedule.setVisibility(View.GONE);
                    mLayoutSimpleSchedule.setVisibility(View.VISIBLE);
                }

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

                ProjectApp.getInstance().getSyncSimpleSchedule().add(simpleSchedule);

                if (ProjectApp.getInstance().getSyncSimpleSchedule().size() >= 2) {

                    SimpleSchedule ss1 = ProjectApp.getInstance().getSyncSimpleSchedule().get(0);
                    SimpleSchedule ss2 = ProjectApp.getInstance().getSyncSimpleSchedule().get(1);

                    String bin = TransUtils.hexStringToBinary(TransUtils.byte2hex(ss1.getWeek()));

                    mTvSaturday.setSelected(bin.charAt(1) == '1');
                    mTvFriday.setSelected(bin.charAt(2) == '1');
                    mTvThursday.setSelected(bin.charAt(3) == '1');
                    mTvWednesday.setSelected(bin.charAt(4) == '1');
                    mTvTuesday.setSelected(bin.charAt(5) == '1');
                    mTvMonday.setSelected(bin.charAt(6) == '1');
                    mTvSunday.setSelected(bin.charAt(7) == '1');

                    mTvSimpleSchedule.setText(ss1.getTime() + " On to ");
                    mTvSimpleSchedule.append(ss2.getTime());
                    updateScheduleTheme(ss1.getTheme());

                    if (ss1.isPhotocell()) {//Photocell光敏开关打开，忽略其他时间
                        mTvSimpleSchedule.setText("Photocell On to ");
                        mTvSimpleSchedule.append(ss2.getTime());
                        updateScheduleTheme(ss1.getTheme());

                    } else {

                        String sTime1 = null;
                        String sTime2 = null;
                        int sThemeId = -1;

                        for (SimpleSchedule ss : ProjectApp.getInstance().getSyncSimpleSchedule()) {

                            if (ss.getId() % 2 == 1) {

                                sTime1 = ss.getTime();
                                sThemeId = ss.getTheme();

                            } else {

                                sTime2 = ss.getTime();

                            }

                            if (!TextUtils.isEmpty(sTime1) && !TextUtils.isEmpty(sTime2)) {

                                if (Utils.isInTimeRange(System.currentTimeMillis(), sTime1, sTime2)) {

                                    mTvSimpleSchedule.setText(sTime1 + " On to ");
                                    mTvSimpleSchedule.append(sTime2);
                                    updateScheduleTheme(sThemeId);

                                    break;

                                } else {

                                    if (Utils.judgeTime(sTime1) < 0) {

                                        mTvSimpleSchedule.setText(sTime1 + " On to ");
                                        mTvSimpleSchedule.append(sTime2);
                                        updateScheduleTheme(sThemeId);

                                        break;

                                    }

                                }

                                sTime1 = null;
                                sTime2 = null;

                            }

                        }

                    }

                } else {

                    theme_schedule_on = null;
                    if (mRbtnScheduleOn.isChecked()) {
                        mTvActiveTheme.setText("  " + theme_schedule_on + "  ");
                    }
                    mTvSimpleSchedule.setText("");

                }

            } else {//custom schedule - 21bytes

                ProjectApp.getInstance().setCustomSchedule(true);

                if (mRbtnScheduleOn.isChecked()) {
                    mLayoutCustomerSchedule.setVisibility(View.VISIBLE);
                    mLayoutSimpleSchedule.setVisibility(View.GONE);
                }

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

                if (customSchedule.getId() == 7) {

                    theme_schedule_on = null;

                    mTvUpcomingTheme.setText("");
                    if (mRbtnScheduleOn.isChecked()) {
                        mTvActiveTheme.setText("    ");
                    }

                    if (customSchedule.isPhotocell()) {
                        mTvCustomSchedule.setText("Photocell On to ");
                    } else {
                        mTvCustomSchedule.setText(sdf.format(new Date(customSchedule.getDatetime())) + " On to ");
                    }
                }
                if (customSchedule.getId() == 8) {
                    mTvCustomSchedule.append(sdf.format(new Date(customSchedule.getDatetime())));
                }

                if (datas[7] != (byte) 0xFF) {

                    if (Utils.judgeDay(customSchedule.getDatetime()) == 0) {//today

                        for (ThemeColor tc : mThemeColors) {
                            if (tc.getId() == customSchedule.getTheme()) {
                                theme_schedule_on = tc.getName();
                                if (mRbtnScheduleOn.isChecked()) {
                                    mTvActiveTheme.setText("  " + theme_schedule_on + "  ");
                                }
                                break;
                            }
                        }
                    } else if (Utils.judgeDay(customSchedule.getDatetime()) == 1) {//tomorrow

                        for (ThemeColor tc : mThemeColors) {
                            if (tc.getId() == customSchedule.getTheme()) {
                                mTvUpcomingTheme.setText(tc.getName());
                                break;
                            }
                        }
                    }

                }

                ProjectApp.getInstance().getSyncCustomSchedule().add(customSchedule);

                if (ProjectApp.getInstance().getSyncCustomSchedule().size() == 120) {
                    Log.e("dismissDialog","dismissDialog 120");
                    mHandler.removeCallbacks(mRunnable);
                    dismissDialog();
                    //
                    checkFirmwareVersion();

                }

            }

        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnect();
    }

    public void onEventMainThread(UnpairEvent event) {

//        mRgroup.check(mRbtnPowerOff.getId());

    }

    public void onEventMainThread(BroadcastEvent event) {
        Intent intent = event.getIntent();
        String action = intent.getAction();
        if (RFStarBLEService.ACTION_DATA_AVAILABLE.equals(action)) {

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

            if (onlineVersion > localVersion) {
                showUpgradeDialog();
            }else {
                syncThemeColorMenu();
            }

        } else if (datas[1] == (byte) 0x83) {

            mHandler.removeCallbacks(resetRunnable);

        }
    }

    private void showUpgradeDialog() {

        dismissDialog();

        ArrayList<String> menuList = new ArrayList<String>();
        menuList.add("Continue with Setup");
        new CircleDialog.Builder()
                .setCancelable(true)
                .setTitle("Device Connection Successful")
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.height = 350;
                        params.styleText = Typeface.BOLD;
                    }
                })
                .setSubTitle("Your device has been connected to\nthis controller and has been set up to\nbe used with Light Stream 2 Bulbs. If\nyou're using legacy Light Stream 3\nBulbs, you may change this setting in\nManage Devices.\n\n")
                .configSubTitle(new ConfigSubTitle() {
                    @Override
                    public void onConfig(SubTitleParams params) {
                    }
                })
                .configDialog(new ConfigDialog() {
                                  @Override
                                  public void onConfig(DialogParams params) {
                                      params.mPadding = new int[] {16, 16, 16, 16};
                                  }
                              }
                )
                .setItems(menuList, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                        }
                    }
                })
                .configItems((ItemsParams params) -> {
                    params.textColor = Color.parseColor("#0076FF");
                })
                .setGravity(Gravity.CENTER)
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
                        syncThemeColorMenu();
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
    private void updateScheduleTheme(int sThemeId) {

        for (ThemeColor tc : mThemeColors) {
            if (tc.getId() == sThemeId) {
                theme_schedule_on = tc.getName();
                if (mRbtnScheduleOn.isChecked()) {
                    mTvActiveTheme.setText("  " + theme_schedule_on + "  ");
                }
                break;
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

    private void syncThemeColor() {
        ProjectApp.getInstance().getSyncThemeColors().clear();
        byte[] query3 = {(byte) 0xAA, 0x10, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, (byte) 0xff, 0x00, 0x55};
        sendPackets(Utils.getSendData(query3));
    }


    private int updateErrorCount = 0;
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
                        queryVersion();
                    }

                    @Override
                    public void onStart() {

                        showDialog("", "Checking...");
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                        LogUtils.d("server error:" + msg);
                        dismissDialog();
                        queryVersion();
                    }
                });
    }

    private void queryVersion() {
        Log.e("bulbs","queryVersion");
        //1.查询属性
        byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x00, 0x55};
        sendPackets(Utils.getSendData(query));
        isMainChecked = true;
        //5秒内没有固件回复，提示更新
        mHandler.removeCallbacks(upgradeRunnable);
        mHandler.postDelayed(upgradeRunnable, Constants.READ_VERSION_TIMEOUT);
    }

}
