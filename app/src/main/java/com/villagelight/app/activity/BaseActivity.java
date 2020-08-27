package com.villagelight.app.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.ProgressParams;
import com.mylhyl.circledialog.params.TextParams;
import com.mylhyl.circledialog.res.drawable.CircleDrawable;
import com.mylhyl.circledialog.res.values.CircleColor;
import com.mylhyl.circledialog.res.values.CircleDimen;
import com.mylhyl.circledialog.scale.ScaleUtils;
import com.mylhyl.circledialog.view.listener.OnCreateBodyViewListener;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.DisconnectEvent;
import com.villagelight.app.event.UpdateFailedEvent;
import com.villagelight.app.job.Priority;
import com.villagelight.app.job.SendCommandJob;
import com.villagelight.app.model.ChannelBean;
import com.villagelight.app.model.CustomSchedule;
import com.villagelight.app.model.SendBean;
import com.villagelight.app.model.SimpleSchedule;
import com.villagelight.app.model.ThemeColor;
import com.villagelight.app.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.fly2think.blelib.CubicBLEDevice;
import cn.fly2think.blelib.TransUtils;
import de.greenrobot.event.EventBus;


/**
 * Created by Charles Lui
 */
public class BaseActivity extends AppCompatActivity {

    protected final static int TIMEOUT = Constants.BASE_TIMEOUT;

    protected Context mContext;
    protected ProjectApp app;
    private AlertDialog dialog;
    private DialogFragment dialogFragment;
    private DialogFragment dialogFragmentPair;
    private CircleDialog.Builder builder;
    protected List<SendBean> syncDatas = new ArrayList<>();
    private SimpleDateFormat dsdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
    private SimpleDateFormat tsdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
    protected byte cmd;
    protected int cmdTotal;
    protected int cmdCount;
    protected Handler sHandler = new Handler();
    protected Runnable sTimeoutRunnable = new Runnable() {
        @Override
        public void run() {

            showNormalDialog("Duplicate failed", "Timeout");

        }
    };

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_OFF) {

                    EventBus.getDefault().post(new DisconnectEvent());
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        app = ProjectApp.getInstance();

        // 注册ble回调监听
        EventBus.getDefault().register(this);

        IntentFilter disConnectedFilter = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(stateChangeReceiver, disConnectedFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
        sHandler.removeCallbacks(sTimeoutRunnable);
        EventBus.getDefault().unregister(this);
        unregisterReceiver(stateChangeReceiver);
    }

    protected void showDialog(String title, String message) {

        dismissDialog();

        builder = new CircleDialog.Builder();

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        dialogFragment = builder
                .setProgressText(message)
                .setCancelable(false)
                .setProgressStyle(ProgressParams.STYLE_SPINNER)
//                        .setProgressDrawable(R.drawable.bg_progress_s)
                .show(getSupportFragmentManager());


    }

    protected void showDialogCanCancel(String title, String message) {

        dismissDialog();

        builder = new CircleDialog.Builder();

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        dialogFragment = builder
                .setProgressText(message)
                .setCanceledOnTouchOutside(false)
                .setProgressStyle(ProgressParams.STYLE_SPINNER)
//                        .setProgressDrawable(R.drawable.bg_progress_s)
                .show(getSupportFragmentManager());


    }

    protected void showDialog(String title, String message, String button, View.OnClickListener listener) {

        dismissDialog();

        builder = new CircleDialog.Builder();

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        dialogFragment = builder
                .setProgressText(message)
                .setCancelable(false)
                .setPositive(button, listener)
                .setProgressStyle(ProgressParams.STYLE_SPINNER)
//                        .setProgressDrawable(R.drawable.bg_progress_s)
                .show(getSupportFragmentManager());


    }

    protected void updateDialog(String text) {

        if (builder != null) {
            builder.setProgressText(text).create();
        }

    }

    protected void dismissDialog() {
        if (dialogFragment != null) {
            dialogFragment.dismissAllowingStateLoss();//fixed IllegalStateException
            dialogFragment = null;
        }
    }

    protected void disMissPairDialog(){
        if (dialogFragmentPair != null){
            dialogFragmentPair.dismissAllowingStateLoss();//fixed IllegalStateException
            dialogFragmentPair = null;
        }
    }

    protected boolean isConnected() {
        return app.manager.cubicBLEDevice != null && app.manager.cubicBLEDevice.isConnected();
    }

    protected void disconnect() {
        if (app.manager.cubicBLEDevice != null) {
            app.manager.cubicBLEDevice.disconnectedDevice();
            app.manager.cubicBLEDevice = null;
        }
    }

    protected void connect(BluetoothDevice bluetoothDevice) {

        // 连接设备
        app.manager.bluetoothDevice = bluetoothDevice;
        app.manager.cubicBLEDevice = new CubicBLEDevice(
                app.getApplicationContext(),
                app.manager.bluetoothDevice);

    }

    protected boolean sendDatas2(byte[] datas) {
        if (isConnected()) {
            if (datas == null) {
                return false;
            }
            app.manager.cubicBLEDevice.writeValue(datas);
            return true;
        } else {

            if (dialog == null) {
                dialog = new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("尚未连接蓝牙设备，是否连接？")
                        .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(mContext, DeviceListActivity.class));
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
            }

            if (!dialog.isShowing()) {
                dialog.show();
            }

            return false;

        }
    }

    protected void sendPackets(byte[] packets, long delay) {

        SendCommandJob sendCommandJob = new SendCommandJob(packets, Priority.MID);
        sendCommandJob.setDelayTimeInMs(delay);
        app.getJobManager().addJobInBackground(sendCommandJob);
    }


    protected void sendPackets(byte[] packets) {

        app.getJobManager().addJobInBackground(new SendCommandJob(packets, Priority.MID));

    }

    public void onEventMainThread(DisconnectEvent event) {

        if (this instanceof MainActivity) {

            disconnect();

            ((MainActivity) this).notifyDataSetChanged();

            ((MainActivity) this).showDisconnectDialog();

        } else {

            finish();
        }

    }

    public void onEventMainThread(UpdateFailedEvent event) {

        if (this instanceof MainActivity) {

        } else {

            finish();
        }

    }


    protected void getSyncDatas() {

        syncDatas.clear();

        List<ThemeColor> themeColors = ProjectApp.getInstance().getDuplThemeColors();
        for (ThemeColor tc : themeColors) {

            for (int i = 0; i < tc.getChannels().size(); i++) {

                ChannelBean channel = tc.getChannels().get(i);
                channel.setTid(tc.getId());


                String fadeBin = TransUtils.Bytes2Bin(new byte[]{(byte) tc.getFade()});
                String twinkleBin = channel.isTwinkleOn() ? "1" : "0";
                String xsBin = twinkleBin + fadeBin.substring(1, fadeBin.length());

                byte xs = (byte) Integer.parseInt(xsBin.replace(" ", ""), 2);

                byte[] cmdsCT = new byte[]{
                        (byte) 0xAA, 0x11, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                        (byte) 0x00, (byte) 0x00, (byte) tc.getId(), 0x03, 0x05, (byte) (i + 1),
                        (byte) channel.getColorNo1(), (byte) channel.getColorNo2(), (byte) channel.getColorNo3(),
                        xs, 0x00, 0x55
                };

                syncDatas.add(new SendBean(Utils.getSendData(cmdsCT)));

            }

            byte[] names = tc.getName().getBytes();

            byte[] firsts = {(byte) 0xAA, 0x11, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                    (byte) 0x00, (byte) 0x00, (byte) tc.getId(), 0x04, (byte) names.length};

            byte[] lasts = {0x00, 0x55};

            byte[] cmdsTN = new byte[firsts.length + names.length + lasts.length];

            System.arraycopy(firsts, 0, cmdsTN, 0, firsts.length);
            System.arraycopy(names, 0, cmdsTN, firsts.length, names.length);
            System.arraycopy(lasts, 0, cmdsTN, firsts.length + names.length, lasts.length);

            syncDatas.add(new SendBean(Utils.getSendData(cmdsTN)));

        }

        List<SimpleSchedule> simpleSchedules = ProjectApp.getInstance().getDuplSimpleSchedule();
        for (SimpleSchedule ss : simpleSchedules) {


            byte[] hms = getHMs(ss.getTime());

            byte[] cmds = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                    (byte) 0x00, (byte) 0x00, (byte) ss.getId(), (byte) ss.getTheme(), 0x00, 0x01, (byte) (ss.isPhotocell() ? 0x01 : 0x00),
                    0x03, 0x03, ss.getWeek(), hms[0], hms[1],
                    0x00, 0x55};


            syncDatas.add(new SendBean(Utils.getSendData(cmds)));
        }


        Calendar calendar = Calendar.getInstance();
        List<CustomSchedule> customSchedules = ProjectApp.getInstance().getDuplCustomSchedule();
        for (CustomSchedule cs : customSchedules) {

            calendar.setTimeInMillis(cs.getDatetime());

            byte[] datetimes = getDateTimes(dsdf.format(calendar.getTime()),
                    tsdf.format(new Date(cs.getDatetime())).toLowerCase());

            byte[] cmds = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                    (byte) 0x00, (byte) 0x00, (byte) cs.getId(), (byte) cs.getTheme(), 0x00, 0x01, (byte) (cs.isPhotocell() ? 0x01 : 0x00),
                    0x02, 0x06, datetimes[0], datetimes[1], datetimes[2], datetimes[3], datetimes[4], datetimes[5],
                    0x00, 0x55};

            syncDatas.add(new SendBean(Utils.getSendData(cmds)));
        }

    }

    private byte[] getHMs(String timeStr) {

        Calendar calendar = Calendar.getInstance();

        byte hour;
        byte minute;

        try {
            calendar.setTime(tsdf.parse(timeStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        minute = (byte) calendar.get(Calendar.MINUTE);

        return new byte[]{hour, minute};
    }

    protected byte[] getDateTimes(String date, String time) {

        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(dsdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        byte[] years = com.villagelight.app.util.TransUtils.short2bytes((short) calendar.get(Calendar.YEAR));
        byte month = (byte) (calendar.get(Calendar.MONTH) + 1);
        byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);

        byte hour;
        byte minute;

        try {
            calendar.setTime(tsdf.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        minute = (byte) calendar.get(Calendar.MINUTE);

        return new byte[]{years[0], years[1], month, day, hour, minute};
    }

    protected void showNormalDialog(String title, String content) {

        dismissDialog();

        builder = new CircleDialog.Builder();

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle(title)
                .setText(content)
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setPositive("Try again", null)
                .setNegative("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

    protected void showDownloadDialog(String title, final String message, String version) {

        dismissDialog();

        builder = new CircleDialog.Builder();

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        dialogFragment = builder
                .setCancelable(false)
                //不影响顶部标题和底部按钮部份
                .setBodyView(R.layout.dialog_progress, new OnCreateBodyViewListener() {
                    @Override
                    public void onCreateBodyView(View view) {

                        CircleDrawable bgCircleDrawable = new CircleDrawable(CircleColor.DIALOG_BACKGROUND
                                , 0, 0, CircleDimen.DIALOG_RADIUS, CircleDimen.DIALOG_RADIUS);
                        view.setBackgroundDrawable(bgCircleDrawable);

                        TextView textView = view.findViewById(R.id.tv_content);
                        textView.setText(message);

                        int dimenTextSize = ScaleUtils.scaleValue(CircleDimen.CONTENT_TEXT_SIZE);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimenTextSize);

                        TextView tv_version = view.findViewById(R.id.tv_switches);
                        tv_version.setVisibility(View.VISIBLE);
                        tv_version.setText(version);

                    }
                })
                .show(getSupportFragmentManager());

    }

    protected void showPairDialog(String title, final String message, final int switches, String button, View.OnClickListener listener) {

        dismissDialog();

        builder = new CircleDialog.Builder();

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        dialogFragmentPair = builder
                .setCancelable(false)
                //不影响顶部标题和底部按钮部份
                .setBodyView(R.layout.dialog_progress, new OnCreateBodyViewListener() {
                    @Override
                    public void onCreateBodyView(View view) {

                        TextView textView = view.findViewById(R.id.tv_content);
                        textView.setText(message);

                        TextView tv_switches = view.findViewById(R.id.tv_switches);
                        tv_switches.setVisibility(View.VISIBLE);
                        tv_switches.setText(switches + " Switch(es) Found");

                        int dimenTextSize = ScaleUtils.scaleValue(CircleDimen.CONTENT_TEXT_SIZE);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimenTextSize);

                    }
                })
                .setPositive(button, listener)
                .show(getSupportFragmentManager());

    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    protected boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        for (ActivityManager.RunningTaskInfo taskInfo : list) {
            if (taskInfo.topActivity.getClassName().contains(className)) { // 说明它已经启动了
                return true;
            }
        }
        return false;

    }

    protected void requestMtu() {
        if (app.manager.cubicBLEDevice != null && isConnected()) {
            app.manager.cubicBLEDevice.requestMaxMtu();
        }
    }

}
