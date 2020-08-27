package com.villagelight.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.model.SimpleSchedule;
import com.villagelight.app.model.ThemeColor;
import com.villagelight.app.util.TransUtils;
import com.villagelight.app.util.Utils;
import com.villagelight.app.view.SelectDialog;
import com.villagelight.app.view.SelectTimeDialog;
import com.weigan.loopview.OnItemSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ch.ielse.view.SwitchView;

public class ScheduleSimpleFragment extends BaseFragment {

    public static final long ValidTime = 1000L * 60 * 5;
    @BindView(R.id.tv_monday)
    CheckBox mTvMonday;
    @BindView(R.id.tv_tuesday)
    CheckBox mTvTuesday;
    @BindView(R.id.tv_wednesday)
    CheckBox mTvWednesday;
    @BindView(R.id.tv_thursday)
    CheckBox mTvThursday;
    @BindView(R.id.tv_friday)
    CheckBox mTvFriday;
    @BindView(R.id.tv_saturday)
    CheckBox mTvSaturday;
    @BindView(R.id.tv_sunday)
    CheckBox mTvSunday;
    @BindView(R.id.sw_schedule_time1)
    SwitchView mSwScheduleTime1;
    @BindView(R.id.tv_time_on1)
    TextView mTvTimeOn1;
    @BindView(R.id.layout_time_on)
    LinearLayout mLayoutTimeOn;
    @BindView(R.id.tv_time_off1)
    TextView mTvTimeOff1;
    @BindView(R.id.layout_time_off1)
    LinearLayout mLayoutTimeOff1;
    @BindView(R.id.sw_photocell1)
    SwitchView mSwPhotocell1;
    @BindView(R.id.tv_color_theme1)
    TextView mTvColorTheme1;
    @BindView(R.id.layout_color_theme1)
    LinearLayout mLayoutColorTheme1;
    @BindView(R.id.layout_schedule1)
    LinearLayout mLayoutSchedule1;
    @BindView(R.id.sw_schedule_time2)
    SwitchView mSwScheduleTime2;
    @BindView(R.id.tv_time_on2)
    TextView mTvTimeOn2;
    @BindView(R.id.layout_time_on2)
    LinearLayout mLayoutTimeOn2;
    @BindView(R.id.tv_time_off2)
    TextView mTvTimeOff2;
    @BindView(R.id.layout_time_off2)
    LinearLayout mLayoutTimeOff2;
    @BindView(R.id.sw_photocell2)
    SwitchView mSwPhotocell2;
    @BindView(R.id.tv_color_theme2)
    TextView mTvColorTheme2;
    @BindView(R.id.layout_color_theme2)
    LinearLayout mLayoutColorTheme2;
    @BindView(R.id.layout_schedule2)
    LinearLayout mLayoutSchedule2;
    @BindView(R.id.sw_schedule_time3)
    SwitchView mSwScheduleTime3;
    @BindView(R.id.tv_time_on3)
    TextView mTvTimeOn3;
    @BindView(R.id.layout_time_on3)
    LinearLayout mLayoutTimeOn3;
    @BindView(R.id.tv_time_off3)
    TextView mTvTimeOff3;
    @BindView(R.id.layout_time_off3)
    LinearLayout mLayoutTimeOff3;
    @BindView(R.id.sw_photocell3)
    SwitchView mSwPhotocell3;
    @BindView(R.id.tv_color_theme3)
    TextView mTvColorTheme3;
    @BindView(R.id.layout_color_theme3)
    LinearLayout mLayoutColorTheme3;
    @BindView(R.id.layout_schedule3)
    LinearLayout mLayoutSchedule3;
    Unbinder unbinder;
    @BindView(R.id.tv_st1)
    TextView mTvSt1;
    @BindView(R.id.tv_st2)
    TextView mTvSt2;
    @BindView(R.id.tv_st3)
    TextView mTvSt3;
    private View rootView;
    private List<String> defaultColors = new ArrayList<>();
    private List<ThemeColor> mThemeColors = new ArrayList<>();
    private int selected1, selected2, selected3;
    private SimpleDateFormat sdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);

    public ScheduleSimpleFragment() {
        // Required empty public constructor
    }

    public static ScheduleSimpleFragment newInstance() {
        ScheduleSimpleFragment fragment = new ScheduleSimpleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_schedule_simple, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        mSwScheduleTime1.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                view.toggleSwitch(true);
                mLayoutSchedule1.setVisibility(View.VISIBLE);
                //fix switch bug
                mSwScheduleTime2.setOpened(true);
                mSwScheduleTime2.setOpened(false);
                mTvSt1.setEnabled(true);

            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mSwScheduleTime2.toggleSwitch(false);
                mSwScheduleTime3.toggleSwitch(false);
                mLayoutSchedule1.setVisibility(View.GONE);
                mLayoutSchedule2.setVisibility(View.GONE);
                mLayoutSchedule3.setVisibility(View.GONE);
                mTvSt1.setEnabled(false);
                mTvSt2.setEnabled(false);
                mTvSt3.setEnabled(false);
            }
        });

        mSwScheduleTime2.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                if (mSwScheduleTime1.isOpened()) {
                    view.toggleSwitch(true);
                    mLayoutSchedule2.setVisibility(View.VISIBLE);
                    //fix switch bug
                    mSwScheduleTime3.setOpened(true);
                    mSwScheduleTime3.setOpened(false);
                    mTvSt2.setEnabled(true);
                }

            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mSwScheduleTime3.toggleSwitch(false);
                mLayoutSchedule2.setVisibility(View.GONE);
                mLayoutSchedule3.setVisibility(View.GONE);
                mTvSt2.setEnabled(false);
                mTvSt3.setEnabled(false);
            }
        });

        mSwScheduleTime3.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                if (mSwScheduleTime1.isOpened() && mSwScheduleTime2.isOpened()) {
                    view.toggleSwitch(true);
                    mLayoutSchedule3.setVisibility(View.VISIBLE);
                    mTvSt3.setEnabled(true);
                }


            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mLayoutSchedule3.setVisibility(View.GONE);
                mTvSt3.setEnabled(false);
            }
        });

        mSwPhotocell1.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                view.toggleSwitch(true);
                mTvTimeOn1.setTextColor(Color.GRAY);

            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mTvTimeOn1.setTextColor(Color.parseColor("#36445b"));

            }
        });

        mSwPhotocell2.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                view.toggleSwitch(true);
                mTvTimeOn2.setTextColor(Color.GRAY);

            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mTvTimeOn2.setTextColor(Color.parseColor("#36445b"));

            }
        });

        mSwPhotocell3.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                view.toggleSwitch(true);
                mTvTimeOn3.setTextColor(Color.GRAY);

            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mTvTimeOn3.setTextColor(Color.parseColor("#36445b"));

            }
        });


        mThemeColors.addAll(ProjectApp.getInstance().getDefaultThemeColors());
        mThemeColors.addAll(ProjectApp.getInstance().getSyncThemeColors());

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

        for (ThemeColor themeColor : mThemeColors) {
            defaultColors.add(themeColor.getName());
        }

        for (SimpleSchedule sc : ProjectApp.getInstance().getSyncSimpleSchedule()) {

            if (sc.getId() == 1) {
                mTvTimeOn1.setText(sc.getTime());
                if (sc.isPhotocell()) {
                    mSwPhotocell1.post(new Runnable() {
                        @Override
                        public void run() {
                            simulateTouch(mSwPhotocell1);
                        }
                    });
                }

                selected1 = findThemeIndex(sc.getTheme());
                mTvColorTheme1.setText(defaultColors.get(selected1));


                String bin = TransUtils.hexStringToBinary(TransUtils.byte2hex(sc.getWeek()));

                mTvSaturday.setChecked(bin.charAt(1) == '1');
                mTvFriday.setChecked(bin.charAt(2) == '1');
                mTvThursday.setChecked(bin.charAt(3) == '1');
                mTvWednesday.setChecked(bin.charAt(4) == '1');
                mTvTuesday.setChecked(bin.charAt(5) == '1');
                mTvMonday.setChecked(bin.charAt(6) == '1');
                mTvSunday.setChecked(bin.charAt(7) == '1');

            }
            if (sc.getId() == 2) {
                mTvTimeOff1.setText(sc.getTime());
            }
            if (sc.getId() == 3) {

                mTvTimeOn2.setText(sc.getTime());
                selected2 = findThemeIndex(sc.getTheme());
                mTvColorTheme2.setText(defaultColors.get(selected2));
                if (sc.isOn()) {
                    mSwScheduleTime2.post(new Runnable() {
                        @Override
                        public void run() {
                            simulateTouch(mSwScheduleTime2);
                        }
                    });
                }

            }
            if (sc.getId() == 4) {

                mTvTimeOff2.setText(sc.getTime());
            }
            if (sc.getId() == 5) {

                mTvTimeOn3.setText(sc.getTime());
                selected3 = findThemeIndex(sc.getTheme());
                mTvColorTheme3.setText(defaultColors.get(selected3));
                if (sc.isOn()) {
                    mSwScheduleTime3.post(new Runnable() {
                        @Override
                        public void run() {
                            simulateTouch(mSwScheduleTime3);
                        }
                    });
                }

            }
            if (sc.getId() == 6) {
                mTvTimeOff3.setText(sc.getTime());
            }

        }


        makeTimeCorrect();

        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //Fragment隐藏时调用
        } else {
            //Fragment显示时调用
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.layout_time_on, R.id.layout_time_off1, R.id.layout_color_theme1, R.id.layout_time_on2, R.id.layout_time_off2, R.id.layout_color_theme2, R.id.layout_time_on3, R.id.layout_time_off3, R.id.layout_color_theme3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_time_on:

                if (mSwPhotocell1.isOpened()) {
                    return;
                }

                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOn1.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {
                                mTvTimeOn1.setText(timeString);
                                try {
                                    Date date = sdf.parse(timeString);
                                    mTvTimeOff1.setText(sdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                makeTimeCorrect();
                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog");
                break;
            case R.id.layout_time_off1:
                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOff1.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {

                                if (mSwPhotocell1.isOpened()) {

                                    mTvTimeOff1.setText(timeString);

                                    makeTimeCorrect();

                                } else {
                                    if (Utils.isTimeOK(mTvTimeOn1.getText().toString(), timeString)) {

                                        mTvTimeOff1.setText(timeString);

                                        makeTimeCorrect();

                                    } else {

                                        showLimitDialog();
                                    }
                                }
                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog");
                break;
            case R.id.layout_color_theme1:
                SelectDialog.getInstance()
                        .setItems(defaultColors)
                        .setInitPosition(selected1)
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                selected1 = index;
                                mTvColorTheme1.setText(defaultColors.get(index));

                            }
                        })
                        .show(getFragmentManager(), "SelectDialog");
                break;
            case R.id.layout_time_on2:
                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOn2.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {

                                if (Utils.isTimeOK(mTvTimeOff1.getText().toString(), timeString)) {

                                    mTvTimeOn2.setText(timeString);
                                    try {
                                        Date date = sdf.parse(timeString);
                                        mTvTimeOff2.setText(sdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    makeTimeCorrect();

                                } else {

                                    showLimitDialog();
                                }
                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog");
                break;
            case R.id.layout_time_off2:
                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOff2.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {

                                if (Utils.isTimeOK(mTvTimeOn2.getText().toString(), timeString)) {

                                    mTvTimeOff2.setText(timeString);

                                    makeTimeCorrect();

                                } else {

                                    showLimitDialog();
                                }
                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog");
                break;
            case R.id.layout_color_theme2:
                SelectDialog.getInstance()
                        .setItems(defaultColors)
                        .setInitPosition(selected2)
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                selected2 = index;
                                mTvColorTheme2.setText(defaultColors.get(index));

                            }
                        })
                        .show(getFragmentManager(), "SelectDialog");
                break;
            case R.id.layout_time_on3:
                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOn3.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {

                                if (Utils.isTimeOK(mTvTimeOff2.getText().toString(), timeString)) {

                                    mTvTimeOn3.setText(timeString);
                                    try {
                                        Date date = sdf.parse(timeString);
                                        mTvTimeOff3.setText(sdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    makeTimeCorrect();

                                } else {

                                    showLimitDialog();
                                }


                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog");
                break;
            case R.id.layout_time_off3:
                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOff3.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {

                                if (Utils.isTimeOK(mTvTimeOn3.getText().toString(), timeString)) {

                                    mTvTimeOff3.setText(timeString);

                                } else {

                                    showLimitDialog();
                                }
                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog");
                break;
            case R.id.layout_color_theme3:
                SelectDialog.getInstance()
                        .setItems(defaultColors)
                        .setInitPosition(selected3)
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                selected3 = index;
                                mTvColorTheme3.setText(defaultColors.get(index));

                            }
                        })
                        .show(getFragmentManager(), "SelectDialog");
                break;
        }
    }

    public List<byte[]> getDatas() {


        List<byte[]> list = new ArrayList<>();

        byte[] hms = getHMs(mTvTimeOn1);

        byte[] cmdson1 = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x01, getID(selected1), 0x00, 0x01, (byte) (mSwPhotocell1.isOpened() ? 0x01 : 0x00),
                0x03, 0x03, getWeek(), hms[0], hms[1],
                0x00, 0x55};
        if (mSwScheduleTime1.isOpened()) {
            list.add(Utils.getSendData(cmdson1));
        }

        hms = getHMs(mTvTimeOff1);
        byte[] cmdsoff1 = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x02, (byte) 0xff, 0x00, 0x01, 0x00,
                0x03, 0x03, getWeek(), hms[0], hms[1],
                0x00, 0x55};
        if (mSwScheduleTime1.isOpened()) {
            list.add(Utils.getSendData(cmdsoff1));
        }

        hms = getHMs(mTvTimeOn2);

        byte[] cmdson2 = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x03, getID(selected2), 0x00, 0x01, (byte) (mSwPhotocell2.isOpened() ? 0x01 : 0x00),
                0x03, 0x03, getWeek(), hms[0], hms[1],
                0x00, 0x55};
        if (mSwScheduleTime2.isOpened()) {
            list.add(Utils.getSendData(cmdson2));
        }


        hms = getHMs(mTvTimeOff2);
        byte[] cmdsoff2 = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x04, (byte) 0xff, 0x00, 0x01, (byte) (mSwPhotocell2.isOpened() ? 0x01 : 0x00),
                0x03, 0x03, getWeek(), hms[0], hms[1],
                0x00, 0x55};
        if (mSwScheduleTime2.isOpened()) {
            list.add(Utils.getSendData(cmdsoff2));
        }

        hms = getHMs(mTvTimeOn3);
        byte[] cmdson3 = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x05, getID(selected3), 0x00, 0x01, (byte) (mSwPhotocell3.isOpened() ? 0x01 : 0x00),
                0x03, 0x03, getWeek(), hms[0], hms[1],
                0x00, 0x55};
        if (mSwScheduleTime3.isOpened()) {
            list.add(Utils.getSendData(cmdson3));
        }


        hms = getHMs(mTvTimeOff3);
        byte[] cmdsoff3 = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x06, (byte) 0xff, 0x00, 0x01, (byte) (mSwPhotocell3.isOpened() ? 0x01 : 0x00),
                0x03, 0x03, getWeek(), hms[0], hms[1],
                0x00, 0x55};
        if (mSwScheduleTime3.isOpened()) {
            list.add(Utils.getSendData(cmdsoff3));
        }

        return list;
    }


    private byte getWeek() {

        StringBuffer sb = new StringBuffer();
        sb.append(0);
        sb.append(mTvSaturday.isChecked() ? 1 : 0);
        sb.append(mTvFriday.isChecked() ? 1 : 0);
        sb.append(mTvThursday.isChecked() ? 1 : 0);
        sb.append(mTvWednesday.isChecked() ? 1 : 0);
        sb.append(mTvTuesday.isChecked() ? 1 : 0);
        sb.append(mTvMonday.isChecked() ? 1 : 0);
        sb.append(mTvSunday.isChecked() ? 1 : 0);

        return (byte) TransUtils.binaryToAlgorism(sb.toString());
    }

    private byte getID(int selected) {

        ThemeColor themeColor = mThemeColors.get(selected);

        return (byte) themeColor.getId();
    }

    private byte[] getHMs(TextView textView) {

        Calendar calendar = Calendar.getInstance();

        byte hour;
        byte minute;

        try {
            calendar.setTime(sdf.parse(textView.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        minute = (byte) calendar.get(Calendar.MINUTE);

        return new byte[]{hour, minute};
    }

    private void simulateTouch(View v) {

        long downTime = SystemClock.uptimeMillis();
        MotionEvent downEvent = MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_DOWN, v.getX(), v.getY(), 0);
        MotionEvent upEvent = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, v.getX(), v.getY(), 0);

        v.onTouchEvent(downEvent);
        v.onTouchEvent(upEvent);

        downEvent.recycle();
        upEvent.recycle();
    }

    private int findThemeIndex(int themeId) {

        for (int i = 0; i < mThemeColors.size(); i++) {
            ThemeColor tc = mThemeColors.get(i);
            if (tc.getId() == themeId) {
                return i;
            }
        }

        return 0;
    }


    private void makeTimeCorrect() {

        if (!Utils.isTimeOK(mTvTimeOn1.getText().toString(), mTvTimeOff1.getText().toString())) {
            try {
                Date date = sdf.parse(mTvTimeOn1.getText().toString());
                mTvTimeOff1.setText(sdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!Utils.isTimeOK(mTvTimeOff1.getText().toString(), mTvTimeOn2.getText().toString())) {
            try {
                Date date = sdf.parse(mTvTimeOff1.getText().toString());
                mTvTimeOn2.setText(sdf.format(new Date(date.getTime() + 60 * 60 * 1000)).toLowerCase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!Utils.isTimeOK(mTvTimeOn2.getText().toString(), mTvTimeOff2.getText().toString())) {
            try {
                Date date = sdf.parse(mTvTimeOn2.getText().toString());
                mTvTimeOff2.setText(sdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!Utils.isTimeOK(mTvTimeOff2.getText().toString(), mTvTimeOn3.getText().toString())) {
            try {
                Date date = sdf.parse(mTvTimeOff2.getText().toString());
                mTvTimeOn3.setText(sdf.format(new Date(date.getTime() + 60 * 60 * 1000)).toLowerCase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!Utils.isTimeOK(mTvTimeOn3.getText().toString(), mTvTimeOff3.getText().toString())) {
            try {
                Date date = sdf.parse(mTvTimeOn3.getText().toString());
                mTvTimeOff3.setText(sdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
