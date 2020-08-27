package com.villagelight.app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigItems;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.ItemsParams;
import com.mylhyl.circledialog.params.TextParams;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.ScheduleEvent;
import com.villagelight.app.fragment.ScheduleCustomFragment;
import com.villagelight.app.fragment.ScheduleSimpleFragment;
import com.villagelight.app.event.JobDoneEvent;
import com.villagelight.app.model.SendBean;
import com.villagelight.app.util.ToastUtils;
import com.villagelight.app.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.ielse.view.SwitchView;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.RFStarBLEService;
import cn.fly2think.blelib.TransUtils;
import de.greenrobot.event.EventBus;

public class ScheduleActivity extends BaseActivity {

    enum SaveState{
        unknow,
        saveing,
        saveFilad,
        saveSucc
    }

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.sw_schedule)
    SwitchView mSwSchedule;
    @BindView(R.id.btn_save)
    Button btnSave;
    private ScheduleSimpleFragment mSimpleFragment;
    private ScheduleCustomFragment mCustomFragment;
    private Fragment currentFragment;
    private List<SendBean> mSends = new ArrayList<>();
    private List<Byte> mRecvs = new ArrayList<>();
    private int mLen;
    private SaveState saveState = SaveState.unknow;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            dismissDialog();
            saveState = SaveState.saveFilad;
            new CircleDialog.Builder()
                    .setCanceledOnTouchOutside(false)
                    .configDialog(new ConfigDialog() {
                        @Override
                        public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                        }
                    })
                    .setTitle("Save failed")
                    .setText("Timeout")
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Schedule");


        mCustomFragment = ScheduleCustomFragment.newInstance();
        mSimpleFragment = ScheduleSimpleFragment.newInstance();
        //默认
//        switchFragment(mSimpleFragment);
//        mSwSchedule.setOpened(false);

        if (ProjectApp.getInstance().isCustomSchedule()) {
            switchFragment(mCustomFragment);
            mSwSchedule.setOpened(true);
        } else {
            switchFragment(mSimpleFragment);
            mSwSchedule.setOpened(false);
        }

        mSwSchedule.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                view.toggleSwitch(true);
                switchFragment(mCustomFragment);
            }

            @Override
            public void toggleToOff(final SwitchView view) {
                view.toggleSwitch(false);
                switchFragment(mSimpleFragment);
//                new CircleDialog.Builder()
//                        .setCanceledOnTouchOutside(false)
//                        .configDialog(new ConfigDialog() {
//                            @Override
//                            public void onConfig(DialogParams params) {
////                                params.backgroundColor = Color.DKGRAY;
////                                params.backgroundColorPress = Color.BLUE;
//                            }
//                        })
//                        .setTitle("WARNING")
//                        .setText("Are you sure you want to switch to Simple Schedule? Changing to simple schedule will delete current custom schedule settings")
//                        .configText(new ConfigText() {
//                            @Override
//                            public void onConfig(TextParams params) {
////                                    params.padding = new int[]{150, 10, 50, 10};
//                            }
//                        })
//                        .setNegative("Cancel", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                view.toggleSwitch(true);
//                            }
//                        })
//                        .setPositive("Continue", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                view.toggleSwitch(false);
//                                switchFragment(mSimpleFragment);
//                            }
//                        })
//                        .configPositive(new ConfigButton() {
//                            @Override
//                            public void onConfig(ButtonParams params) {
////                                    params.backgroundColorPress = Color.RED;
//                            }
//                        })
//                        .show(getSupportFragmentManager());

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (saveState == SaveState.saveSucc){
            finish();
        }else {
            showNotSaveDialog();
        }
    }

    @OnClick({R.id.btn_title_left, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                if (saveState == SaveState.saveSucc){
                    finish();
                }else {
                    showNotSaveDialog();
                }
                break;
            case R.id.btn_save:

                saveState = SaveState.saveing;
                mSends.clear();

                showDialog("", "Updating Controller...");

                List<byte[]> datsList;

                if (mSwSchedule.isOpened()) {

                    datsList = mCustomFragment.getDatas();
                } else {
                    datsList = mSimpleFragment.getDatas();
                }
                cmdCount = 0;
                cmdTotal = datsList.size();
                for (int i = 0; i < datsList.size(); i++) {

                    byte[] datas = datsList.get(i);
                    cmd = datas[1];
                    mSends.add(new SendBean(datas));

                }

                //发送第1包
                if (sendData(mSends.get(0).getDatas())) {
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, Constants.SAVE_SCHEDULE_TIMEOUT);
                } else {
                    dismissDialog();
                }


                break;
        }
    }

    private void showNotSaveDialog() {
        String[] items = {"Save Now", "Discard Changes"};
        new CircleDialog.Builder()
                .setCancelable(true)
                .setTitle("You didn't save your changes!")
                .setItems(items, (AdapterView<?> parent, View view, int position, long id)->{
                    if (position == 0){

                        btnSave.performClick();
                    }else if (position == 1){

                        finish();
                    }
                })
                .configItems((ItemsParams params)->{
                        params.textColor = Color.parseColor("#000000");
                })
                .setGravity(Gravity.CENTER)
                .show(getSupportFragmentManager());
    }

    private void switchFragment(Fragment targetFragment) {

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!targetFragment.isAdded()) {
            //第一次使用switchFragment()时currentFragment为null，所以要判断一下
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.fragment_content, targetFragment, targetFragment.getClass().getName());

        } else {
            transaction
                    .hide(currentFragment)
                    .show(targetFragment);
        }
        currentFragment = targetFragment;
        transaction.commit();
    }


    private void showFinishDialog() {

        saveState = SaveState.saveSucc;
        new CircleDialog.Builder()
                .setCancelable(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle("SAVE SUCCESSFUL")
                .setText("Controller has been updated.\n")
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setPositive("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        if (mSwSchedule.isOpened()) {
                        setResult(RESULT_OK);
//                        }
                        //发送完Schedule数据。重新同步一次
                        EventBus.getDefault().post(new ScheduleEvent(true));
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


    public void onEventMainThread(JobDoneEvent event) {

        if (event.getCmd() == cmd) {
            cmdCount++;
            if (cmdCount == cmdTotal) {
//                dismissDialog();
//                showFinishDialog();
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

    private void forwardData(byte[] datas) {

        if (datas[1] == (byte) 0xB1) {//串口模式发送数据-升级

            mHandler.removeCallbacks(mRunnable);

            if (datas[6] == 0x00) {//成功

                if (!mSends.isEmpty() && datas[3] == mSends.get(0).getDatas()[3]) {
                    mSends.remove(0);
                }

                if (mSends.isEmpty()) {
                    dismissDialog();
                    showFinishDialog();
                } else {

                    sendData(mSends.get(0).getDatas());

                    mHandler.postDelayed(mRunnable, Constants.SAVE_SCHEDULE_TIMEOUT);
                }

            } else if (datas[6] == 0x01) {//已经满

                dismissDialog();
                ToastUtils.showToast(mContext, "Full");

            } else if (datas[6] == 0x02) {//已经存在

                dismissDialog();
                ToastUtils.showToast(mContext, "Exist");

            } else if (datas[6] == 0x03) {//其他失败

                dismissDialog();
                ToastUtils.showToast(mContext, "Failed");

            } else {//保留

                dismissDialog();
                ToastUtils.showToast(mContext, "Error");

            }
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
}
