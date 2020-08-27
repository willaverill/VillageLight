package com.villagelight.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigSubTitle;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.callback.ConfigTitle;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.ItemsParams;
import com.mylhyl.circledialog.params.SubTitleParams;
import com.mylhyl.circledialog.params.TextParams;
import com.mylhyl.circledialog.params.TitleParams;
import com.orhanobut.logger.Logger;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.adapter.ChannelAdapter;
import com.villagelight.app.event.JobDoneEvent;
import com.villagelight.app.model.ChannelBean;
import com.villagelight.app.model.ColorBean;
import com.villagelight.app.model.SendBean;
import com.villagelight.app.model.ThemeColor;
import com.villagelight.app.util.ToastUtils;
import com.villagelight.app.util.Utils;
import com.villagelight.app.view.SelectDialog;
import com.weigan.loopview.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.BroadcastEvent;
import cn.fly2think.blelib.RFStarBLEService;
import cn.fly2think.blelib.TransUtils;

public class EditThemeActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.et_name)
    EditText mEtName;
    @BindView(R.id.lv)
    ListView mLv;
    @BindView(R.id.tv_fade)
    TextView mTvFade;
    @BindView(R.id.btn_save)
    Button btnSave;
    private SaveState saveState = SaveState.unknow;
    private TextView btnLink;
    private TextView btnAdd;
    private ThemeColor mThemeColor;
    private List<String> fades;
    private int fadeSelectedIndex = 1;
    private List<ChannelBean> channels;
    private ChannelAdapter mAdapter;
    private ChannelBean mChannel;
    private int colorIndex;
    private List<SendBean> mSends = new ArrayList<>();
    private List<Byte> mRecvs = new ArrayList<>();
    private int mLen;
    private boolean isEdit;
    private int themeColorIndex;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            saveState = SaveState.saveFilad;
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
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_theme_color);
        ButterKnife.bind(this);

        sharedPreferences = getSharedPreferences("EditThemeActivity",Context.MODE_PRIVATE);
        mThemeColor = (ThemeColor) getIntent().getSerializableExtra("theme");

//        Log.e("EditThemeActivity", "Id " + mThemeColor.getId() + " cid " + mThemeColor.getCid() + " Fade " + mThemeColor.getFade() + " size " + mThemeColor.getChannels().size());
        mTvTitle.setText("Color Theme");
        mBtnTitleLeft.setVisibility(View.VISIBLE);

        if (mThemeColor != null) {
            mEtName.setText(mThemeColor.getName());
            mEtName.setSelection(mEtName.getText().length());
            mTvFade.setText("Seconds " + mThemeColor.getFade());

            if (mThemeColor.getFade() == 5) {
                fadeSelectedIndex = 0;
            } else if (mThemeColor.getFade() == 10) {
                fadeSelectedIndex = 1;
            } else if (mThemeColor.getFade() == 20) {
                fadeSelectedIndex = 2;
            } else if (mThemeColor.getFade() == 30) {
                fadeSelectedIndex = 3;
            }

            channels = mThemeColor.getChannels();
        }

        fades = new ArrayList<>();
        fades.add("Seconds 5");
        fades.add("Seconds 10");
        fades.add("Seconds 20");
        fades.add("Seconds 30");

        int maxSize = 6;
        if (mThemeColor != null){
            maxSize = sharedPreferences.getInt("ThemeColor_"+mThemeColor.getName(),6);
        }
//        try {
//
//            if (mThemeColor != null) {
//                channels = ProjectApp.getInstance().getDb()
//                        .findAll(Selector.from(ChannelBean.class).where("tid", "=", mThemeColor.getId()));
//            }
//        } catch (DbException e) {
//            e.printStackTrace();
//        }

        if (channels == null) {
            channels = new ArrayList<>();
            for (int i = 1; i < 7; i++) {

                ChannelBean channel = new ChannelBean();
                channel.setName("Channel " + i);
                if (mThemeColor != null) {
                    channel.setTid(mThemeColor.getId());
                }

                channels.add(channel);
            }
        }

        Log.e("printChannel", "maxSize"+maxSize);

        while (maxSize<channels.size()){
            channels.remove(channels.size()-1);
        }

//        LogUtils.d("1 edit theme - Channel size: " + channels.size());
//        if (channels.size() > 10) {
//            for (int i = 0; i < channels.size() - 10; i++) {
//
//                channels.remove(channels.size() - 1);
//            }
//        }
//        LogUtils.d("2 edit theme - Channel size: " + channels.size());

        mAdapter = new ChannelAdapter(mContext, channels);
        mLv.setAdapter(mAdapter);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View footerView = layoutInflater.inflate(R.layout.item_lv_footer, null, false);

        btnLink = footerView.findViewById(R.id.btnLink);
        btnAdd = footerView.findViewById(R.id.btnAdd);
        mLv.addFooterView(footerView);

        btnAdd.setOnClickListener(v -> {

            if (channels.size() < 10){
                ChannelBean channel = new ChannelBean();
                channel.setName("Channel " + (channels.size() + 1));
                if (mThemeColor != null) {
                    channel.setTid(mThemeColor.getId());
                }

                channels.add(channel);
                mAdapter.notifyDataSetChanged();
            }else {
                ToastUtils.showToast(EditThemeActivity.this,"You can not add more channel.");
            }
        });
        btnLink.setOnClickListener(v -> {
            //https://villagelighting.com/blogs/light-stream
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://villagelighting.com/blogs/light-stream"));
            startActivity(browserIntent);
        });
        mAdapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mChannel = channels.get((Integer) v.getTag());

                switch (v.getId()) {

                    case R.id.item_layout_twinkle:
                        mChannel.setTwinkleOn(!mChannel.isTwinkleOn());
                        mAdapter.notifyDataSetChanged();

                        if (mChannel.isTwinkleOn()) {
                            new CircleDialog.Builder()
                                    .setCanceledOnTouchOutside(false)
                                    .configDialog(new ConfigDialog() {
                                        @Override
                                        public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                                        }
                                    })
                                    .setTitle("Activate Twinkle")
                                    .setText("Select a color to activate the twinkle effect on this bulbs channel.")
                                    .configText(new ConfigText() {
                                        @Override
                                        public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                                        }
                                    })
                                    .setPositive("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            colorIndex = 1;
                                            Intent colorIntent = new Intent(mContext, ColorMenuActivity.class);
                                            colorIntent.putExtra("isTwinkleOn", mChannel.isTwinkleOn());
                                            startActivityForResult(colorIntent, 1024);
                                        }
                                    })
                                    .setNegative("Cancel", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            mChannel.setTwinkleOn(false);
                                            mAdapter.notifyDataSetChanged();
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

                        break;
                    case R.id.item_layout_color1:
                        colorIndex = 1;
                        Intent colorIntent = new Intent(mContext, ColorMenuActivity.class);
                        colorIntent.putExtra("isTwinkleOn", mChannel.isTwinkleOn());
                        startActivityForResult(colorIntent, 1024);
                        break;
                    case R.id.item_layout_color2:
                        colorIndex = 2;
                        startActivityForResult(new Intent(mContext, ColorMenuActivity.class), 1024);
                        break;
                    case R.id.item_layout_color3:
                        colorIndex = 3;
                        startActivityForResult(new Intent(mContext, ColorMenuActivity.class), 1024);
                        break;
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    @OnClick({R.id.btn_title_left, R.id.btn_cancel, R.id.layout_select, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                if (saveState == SaveState.saveSucc) {
                    finish();
                } else {
                    showNotSaveDialog();
                }
                break;
            case R.id.btn_cancel:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
                break;
            case R.id.layout_select:
                SelectDialog.getInstance()
                        .setItems(fades)
                        .setInitPosition(fadeSelectedIndex)
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                fadeSelectedIndex = index;
                                mTvFade.setText(fades.get(index));
                            }
                        })
                        .show(getSupportFragmentManager(), "SelectDialog");
                break;
            case R.id.btn_save:

                saveState = SaveState.saveing;
                mSends.clear();

                String name = mEtName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {

                    ToastUtils.showToast(mContext, "The name of theme cannot be empty.");
                    return;
                }
                sharedPreferences.edit().putInt("ThemeColor_"+name,channels.size()).apply();
                themeColorIndex = 0;

                if (mThemeColor != null) {

                    themeColorIndex = getThemeColorIndex(mThemeColor);

                    if (!name.equals(mThemeColor.getName())) {
                        if (isThemeNameExisted(name)) {
                            ToastUtils.showToast(mContext, "The name has already existed. ");
                            return;
                        }
                    }
                } else {
                    if (isThemeNameExisted(name)) {
                        ToastUtils.showToast(mContext, "The name has already existed. ");
                        return;
                    }
                }


                if (mThemeColor == null) {
                    mThemeColor = new ThemeColor();
                } else {
                    isEdit = true;
                }

                ThemeColor lastThemeColor = null;
                try {

//                    Cursor cursor = app.getDb().execQuery("SELECT max(id) as id FROM ThemeColor");
//                    if (cursor != null && cursor.moveToNext()) {
//                        int id = cursor.getInt(cursor.getColumnIndex("id"));
//                        lastThemeColor = app.getDb().findFirst(Selector.from(ThemeColor.class).where("id", "=", id));
//                    }

                    List<ThemeColor> themeColors = ProjectApp.getInstance().getSyncThemeColors();
                    if (!themeColors.isEmpty()) {

                        lastThemeColor = themeColors.get(themeColors.size() - 1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isEdit) {
                    if (lastThemeColor == null) {
                        mThemeColor.setId(app.getDefaultThemeColors().get(app.getDefaultThemeColors().size() - 1).getId() + 1);
                    } else {
                        mThemeColor.setId(lastThemeColor.getId() + 1);
                    }
                }

                int fade = Integer.parseInt(mTvFade.getText().toString().replace("Seconds", "")
                        .replace(" ", ""));

                if (ProjectApp.getInstance().getCurrentControl() == null) {

                    ToastUtils.showToast(mContext, "Please go back and try again.");
                    return;
                }

                mThemeColor.setName(name);
                mThemeColor.setFade(fade);
                mThemeColor.setCid(ProjectApp.getInstance().getCurrentControl().getId());

                Logger.d("tid:" + mThemeColor.getId());

                try {

//                    app.getDb().saveOrUpdate(mThemeColor);


                    showDialogCanCancel("", "Updating Controller...");
                    cmdCount = 0;
                    cmdTotal = channels.size();

                    Log.e("channelsSave", "channels " + channels.size());
                    for (int i = 0; i < channels.size(); i++) {

                        ChannelBean channel = channels.get(i);
                        channel.setTid(mThemeColor.getId());


                        String fadeBin = TransUtils.Bytes2Bin(new byte[]{(byte) (channel.isTwinkleOn() ? 0x05 : fade)});
                        String twinkleBin = channel.isTwinkleOn() ? "1" : "0";
                        String xsBin = twinkleBin + fadeBin.substring(1);

                        byte xs = (byte) Integer.parseInt(xsBin.replace(" ", ""), 2);

                        byte[] cmdsCT;

//                        byte[] colors1 = getARGB(channel.getSendColor1());
//                        byte[] colors2 = getARGB(channel.getSendColor2());
//                        byte[] colors3 = getARGB(channel.getSendColor3());
//
//                        if (isEdit) {
//                            cmdsCT = new byte[]{
//                                    (byte) 0xAA, 0x12, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
//                                    (byte) 0x00, (byte) 0x00, (byte) themeColor.getId(), 0x0f, (byte) (i + 1),
//                                    colors1[0], colors1[1], colors1[2], colors1[3],
//                                    colors2[0], colors2[1], colors2[2], colors2[3],
//                                    colors3[0], colors3[1], colors3[2], colors3[3],
//                                    (byte) (channel.isTwinkleOn() ? 0x01 : 0x00), (byte) fade,
//                                    0x00, 0x55
//                            };
//                        } else {
//                            cmdsCT = new byte[]{
//                                    (byte) 0xAA, 0x11, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
//                                    (byte) 0x00, (byte) 0x00, (byte) themeColor.getId(), 0x03, 0x0F, (byte) (i + 1),
//                                    colors1[0], colors1[1], colors1[2], colors1[3],
//                                    colors2[0], colors2[1], colors2[2], colors2[3],
//                                    colors3[0], colors3[1], colors3[2], colors3[3],
//                                    (byte) (channel.isTwinkleOn() ? 0x01 : 0x00), (byte) fade,
//                                    0x00, 0x55
//                            };
//                        }

                        if (isEdit) {
                            cmdsCT = new byte[]{
                                    (byte) 0xAA, 0x12, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                    (byte) 0x00, (byte) 0x00, (byte) mThemeColor.getId(), 0x03, 0x05, (byte) (i + 1),
                                    (byte) channel.getColorNo1(), (byte) channel.getColorNo2(), (byte) channel.getColorNo3(),
                                    xs, 0x00, 0x55
                            };
                        } else {
                            cmdsCT = new byte[]{
                                    (byte) 0xAA, 0x11, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                    (byte) 0x00, (byte) 0x00, (byte) mThemeColor.getId(), 0x03, 0x05, (byte) (i + 1),
                                    (byte) channel.getColorNo1(), (byte) channel.getColorNo2(), (byte) channel.getColorNo3(),
                                    xs, 0x00, 0x55
                            };
                        }

                        mSends.add(new SendBean(Utils.getSendData(cmdsCT)));

                        cmd = cmdsCT[1];
                    }

                    byte[] names = name.getBytes();

                    byte[] firsts = {(byte) 0xAA, (byte) (isEdit ? 0x12 : 0x11), 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                            (byte) 0x00, (byte) 0x00, (byte) mThemeColor.getId(), 0x04, (byte) names.length};

                    byte[] lasts = {0x00, 0x55};

                    byte[] cmdsTN = new byte[firsts.length + names.length + lasts.length];

                    System.arraycopy(firsts, 0, cmdsTN, 0, firsts.length);
                    System.arraycopy(names, 0, cmdsTN, firsts.length, names.length);
                    System.arraycopy(lasts, 0, cmdsTN, firsts.length + names.length, lasts.length);

                    mSends.add(new SendBean(Utils.getSendData(cmdsTN)));

                    //发送第1包
                    if (sendData(mSends.get(0).getDatas())) {
                        mSends.remove(0);
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.postDelayed(mRunnable, TIMEOUT);
                    } else {
                        dismissDialog();
                    }

                    mThemeColor.setChannels(channels);

//                    ProjectApp.getInstance().getDb().saveOrUpdateAll(channels);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
        }
    }

    private byte[] getARGB(int color) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = (color & 0x000000ff);
        return new byte[]{(byte) red, (byte) green, (byte) blue, (byte) alpha};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && resultCode == RESULT_OK) {
            int displayColor = data.getIntExtra("displayColor", 0);
            int sendColor = data.getIntExtra("sendColor", 0);
            int colorNo = data.getIntExtra("colorNo", 0);
            if (colorIndex == 1) {
                mChannel.setSendColor1(sendColor);
                mChannel.setDisplayColor1(displayColor);
                mChannel.setColorNo1(colorNo);
            } else if (colorIndex == 2) {
                mChannel.setSendColor2(sendColor);
                mChannel.setDisplayColor2(displayColor);
                mChannel.setColorNo2(colorNo);
            } else if (colorIndex == 3) {
                mChannel.setSendColor3(sendColor);
                mChannel.setDisplayColor3(displayColor);
                mChannel.setColorNo3(colorNo);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean isThemeNameExisted(String name) {

        return getThemeColor(name) != null;

    }

    private ThemeColor getThemeColor(String name) {

        ThemeColor themeColor = null;
//        try {
//            themeColor = ProjectApp.getInstance().getDb().findFirst(Selector.from(ThemeColor.class).where("name", "=", name)
//                    .and("cid", "=", ProjectApp.getInstance().getCurrentControl().getId()));
//        } catch (DbException e) {
//            e.printStackTrace();
//        }

        List<ThemeColor> themeColors = ProjectApp.getInstance().getSyncThemeColors();
        for (ThemeColor theme : themeColors) {

            if (theme.getName().equals(name)) {

                themeColor = theme;
                break;
            }
        }

        return themeColor;

    }

    private int getThemeColorIndex(ThemeColor tc) {

        List<ThemeColor> themeColors = ProjectApp.getInstance().getSyncThemeColors();
        for (int i = 0; i < themeColors.size(); i++) {

            ThemeColor theme = themeColors.get(i);
            if (theme.getName().equals(tc.getName())) {
                return i;
            }
        }

        return -1;

    }

    public void onEventMainThread(JobDoneEvent event) {

        if (event.getCmd() == cmd) {
            cmdCount++;
            if (cmdCount == cmdTotal) {
//                dismissDialog();
//                finish();
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

        byte cmd = (byte) (isEdit ? 0x92 : 0x91);

        if (datas[1] == cmd) {//串口模式发送数据-升级

            mHandler.removeCallbacks(mRunnable);

            if (datas[6] == 0x00) {//成功

                saveState = SaveState.saveSucc;
                if (!mSends.isEmpty()) {

                    sendData(mSends.get(0).getDatas());
                    mSends.remove(0);

                } else {
                    if (isEdit) {
                        Log.e("channelsSave", "set channels " + channels.size());
                        ProjectApp.getInstance().getSyncThemeColors().set(themeColorIndex, mThemeColor);

                    } else {
                        Log.e("channelsSave", "add channels " + channels.size());
                        ProjectApp.getInstance().getSyncThemeColors().add(mThemeColor);
                    }

                    dismissDialog();

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .configDialog(new ConfigDialog() {
                                @Override
                                public void onConfig(DialogParams params) {
                                    params.mPadding = new int[] {16, 16, 16, 16};
                                }
                            })
                            .setTitle("Your Theme is Saved")
                            .setText("Your Theme is saved. To see it live, select your new theme on the home\nscreen\n\n")
//                            .setText("To see it live, select the paint bucket and choose your theme.")
                            .configText(new ConfigText() {
                                @Override
                                public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                                }
                            })
                            .setPositive("Continue", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.putExtra("THEME", mThemeColor);
                                    setResult(RESULT_OK, intent);
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

            } else if (datas[6] == 0x01) {//isEdit?失败:已经满

                dismissDialog();
                ToastUtils.showToast(mContext, isEdit ? "Failed" : "Full");

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

    @Override
    public void onBackPressed() {
        if (saveState == SaveState.saveSucc) {
            finish();
        } else {
            showNotSaveDialog();
        }
    }

    private void showNotSaveDialog() {
        ArrayList<String> menuList = new ArrayList<String>();
        menuList.add("Save now");
        menuList.add("Exit without saving");
        new CircleDialog.Builder()
                .setCancelable(true)
                .setTitle("Save Changes")
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.height = 350;
                        params.styleText = Typeface.BOLD;
                    }
                })
                .setSubTitle("Would you like to save your changes before you go?\n\n")
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

                            btnSave.performClick();
                        } else if (position == 1) {

                            finish();
                        }
                    }
                })
                .configItems((ItemsParams params) -> {
                    params.textColor = Color.parseColor("#0076FF");
                })
                .setGravity(Gravity.CENTER)
                .show(getSupportFragmentManager());
    }

    enum SaveState {
        unknow,
        saveing,
        saveFilad,
        saveSucc
    }
}
