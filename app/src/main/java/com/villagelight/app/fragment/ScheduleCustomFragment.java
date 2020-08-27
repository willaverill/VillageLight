package com.villagelight.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.adapter.ScheduleAdapter;
import com.villagelight.app.model.ColorBean;
import com.villagelight.app.model.CustomSchedule;
import com.villagelight.app.model.ScheduleBean;
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

public class ScheduleCustomFragment extends BaseFragment {

    @BindView(R.id.tv_time_on)
    TextView mTvTimeOn;
    @BindView(R.id.layout_time_on)
    LinearLayout mLayoutTimeOn;
    @BindView(R.id.tv_time_off)
    TextView mTvTimeOff;
    @BindView(R.id.layout_time_off)
    LinearLayout mLayoutTimeOff;
    @BindView(R.id.sw_photocell)
    SwitchView mSwPhotocell;
    @BindView(R.id.layout_select)
    LinearLayout mLayoutSelect;
    @BindView(R.id.lv)
    ListView mLv;
    Unbinder unbinder;
    private View rootView;
    private SimpleDateFormat dsdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
    private SimpleDateFormat tsdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
    private List<ScheduleBean> mScheduleBeanList = new ArrayList<>();
    private ScheduleAdapter mAdapter;
    private List<String> defaultColors = new ArrayList<>();
    private List<ThemeColor> mThemeColors = new ArrayList<>();

    public ScheduleCustomFragment() {
        // Required empty public constructor
    }

    public static ScheduleCustomFragment newInstance() {
        ScheduleCustomFragment fragment = new ScheduleCustomFragment();
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
        rootView = inflater.inflate(R.layout.fragment_schedule_custom, container, false);
        unbinder = ButterKnife.bind(this, rootView);


        mAdapter = new ScheduleAdapter(getActivity(), mScheduleBeanList);
        mLv.setAdapter(mAdapter);

        mSwPhotocell.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                view.toggleSwitch(true);
                mTvTimeOn.setTextColor(Color.GRAY);

            }

            @Override
            public void toggleToOff(SwitchView view) {
                view.toggleSwitch(false);
                mTvTimeOn.setTextColor(Color.parseColor("#36445b"));

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
//                defaultColors.add(themeColor.getName());
//                mThemeColors.add(themeColor);
//            }
//        }

        for (ThemeColor themeColor : mThemeColors) {
            defaultColors.add(themeColor.getName());
        }


        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final ScheduleBean bean = mScheduleBeanList.get(position);

                SelectDialog.getInstance()
                        .setItems(defaultColors)
                        .setInitPosition(bean.getColorIndex())
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                bean.getColorBean().setName(defaultColors.get(index));
                                bean.setColorIndex(index);
                                mAdapter.notifyDataSetChanged();

                            }
                        })
                        .show(getFragmentManager(), "SelectDialog");

            }
        });


        Calendar calendar = Calendar.getInstance();

        long todaySchedule = 0;
        for (CustomSchedule cs : ProjectApp.getInstance().getSyncCustomSchedule()) {

            if (cs.getId() == 7) {
                mTvTimeOn.setText(tsdf.format(new Date(cs.getDatetime())).toLowerCase());
                if (cs.isPhotocell()) {
                    mSwPhotocell.post(new Runnable() {
                        @Override
                        public void run() {
                            simulateTouch(mSwPhotocell);
                        }
                    });
                }
            } else if (cs.getId() == 8) {
                mTvTimeOff.setText(tsdf.format(new Date(cs.getDatetime())).toLowerCase());
            }

            if (cs.getId() % 2 == 1) {

                if (Utils.judgeDay(cs.getDatetime()) == 0) {//today
                    todaySchedule = cs.getDatetime();
                    calendar.setTimeInMillis(cs.getDatetime());
                    ScheduleBean item = new ScheduleBean();
                    item.setDate(dsdf.format(calendar.getTime()));
                    ColorBean defaultColor = new ColorBean();
                    int index = findThemeIndex(cs.getTheme());
                    defaultColor.setName(mThemeColors.get(index).getName());
                    item.setColorBean(defaultColor);
                    item.setColorIndex(index);
                    mScheduleBeanList.add(item);
                } else {

                    if (todaySchedule > 0 && cs.getDatetime() > todaySchedule) {
                        calendar.setTimeInMillis(cs.getDatetime());
                        ScheduleBean item = new ScheduleBean();
                        item.setDate(dsdf.format(calendar.getTime()));
                        ColorBean defaultColor = new ColorBean();
                        int index = findThemeIndex(cs.getTheme());
                        defaultColor.setName(mThemeColors.get(index).getName());
                        item.setColorBean(defaultColor);
                        item.setColorIndex(index);
                        mScheduleBeanList.add(item);
                    }

                }
            }
        }

        int size = 60 - mScheduleBeanList.size();
//        calendar.setTimeInMillis(System.currentTimeMillis());
        for (int i = 0; i < size; i++) {

            if (size != 60) {
                calendar.add(Calendar.DATE, 1);
            }
            ScheduleBean item = new ScheduleBean();
            item.setDate(dsdf.format(calendar.getTime()));
            ColorBean defaultColor = new ColorBean();
            defaultColor.setName("");
            item.setColorBean(defaultColor);
            item.setColorIndex(0);
            mScheduleBeanList.add(item);
            if (size == 60) {
                calendar.add(Calendar.DATE, 1);
            }

        }

        mAdapter.notifyDataSetChanged();

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick({R.id.layout_time_on, R.id.layout_time_off})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_time_on:

                if (mSwPhotocell.isOpened()) {
                    return;
                }

                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOn.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {
                                mTvTimeOn.setText(timeString);
                                try {
                                    Date date = tsdf.parse(timeString);
                                    mTvTimeOff.setText(tsdf.format(new Date(date.getTime() + 5 * 60 * 1000)).toLowerCase());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog1");

                break;
            case R.id.layout_time_off:

                SelectTimeDialog.getInstance()
                        .setSelectTime(mTvTimeOff.getText().toString())
                        .setListener(new SelectTimeDialog.TimeSelectListener() {
                            @Override
                            public void onTimeSelect(String timeString) {

                                if (mSwPhotocell.isOpened()) {
                                    mTvTimeOff.setText(timeString);
                                } else {
                                    if (Utils.isTimeOK(mTvTimeOn.getText().toString(), timeString)) {

                                        mTvTimeOff.setText(timeString);

                                    } else {

                                        showLimitDialog();
                                    }

                                }

                            }
                        })
                        .show(getFragmentManager(), "SelectTimeDialog2");

                break;
            case R.id.btn_create:

                ScheduleBean item = new ScheduleBean();
                item.setDate(dsdf.format(new Date()));
                ColorBean defaultColor = new ColorBean();
                defaultColor.setName("Solid Green");
                item.setColorBean(defaultColor);
                item.setColorIndex(3);
                mScheduleBeanList.add(item);
                mAdapter.notifyDataSetChanged();

                break;
        }
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

    public List<byte[]> getDatas() {

        List<byte[]> list = new ArrayList<>();

        for (int i = 0; i < mScheduleBeanList.size(); i++) {

            ScheduleBean schedule = mScheduleBeanList.get(i);

            byte[] datetimes = getDateTimes(schedule.getDate(), mTvTimeOn.getText().toString());

            byte[] cmdson = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                    (byte) 0x00, (byte) 0x00, (byte) (i * 2 + 7), getID(schedule.getColorIndex()), 0x00, 0x01, (byte) (mSwPhotocell.isOpened() ? 0x01 : 0x00),
                    0x02, 0x06, datetimes[0], datetimes[1], datetimes[2], datetimes[3], datetimes[4], datetimes[5],
                    0x00, 0x55};
            list.add(Utils.getSendData(cmdson));

            datetimes = getDateTimes(schedule.getDate(), mTvTimeOff.getText().toString());

            byte[] cmdsoff = {(byte) 0xAA, 0x31, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                    (byte) 0x00, (byte) 0x00, (byte) (i * 2 + 8), (byte) 0xff, 0x00, 0x01, 0x00,
                    0x02, 0x06, datetimes[0], datetimes[1], datetimes[2], datetimes[3], datetimes[4], datetimes[5],
                    0x00, 0x55};
            list.add(Utils.getSendData(cmdsoff));
        }

        return list;
    }

    private byte[] getDateTimes(String date, String time) {

        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(dsdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        byte[] years = TransUtils.short2bytes((short) calendar.get(Calendar.YEAR));
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

    private byte getID(int selected) {

        ThemeColor themeColor = mThemeColors.get(selected);

        return (byte) themeColor.getId();
    }

    private int findThemeIndex(int themeId) {

        for (int i = 0; i < mThemeColors.size(); i++) {
            ThemeColor themeColor = mThemeColors.get(i);
            if (themeId == themeColor.getId()) {
                return i;
            }
        }

        return 0;
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
}
