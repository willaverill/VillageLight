package com.villagelight.app.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mylhyl.circledialog.BaseCircleDialog;
import com.villagelight.app.R;
import com.weigan.loopview.LoopView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectTimeDialog extends BaseCircleDialog implements View.OnClickListener {

    private TimeSelectListener mListener;
    private LoopView loopView_hour;
    private LoopView loopView_minute;
    private LoopView loopView_apm;
    private static List<String> hours = new ArrayList<>();
    private static List<String> minutes = new ArrayList<>();
    private static List<String> apms = new ArrayList<>();
    private SimpleDateFormat tsdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
    private int hIndex;
    private int mIndex;
    private int apIndex;

    public static SelectTimeDialog getInstance() {
        SelectTimeDialog dialogFragment = new SelectTimeDialog();
        dialogFragment.setCanceledBack(false);
        dialogFragment.setCanceledOnTouchOutside(false);
        dialogFragment.setGravity(Gravity.CENTER);
        dialogFragment.setBackgroundColor(Color.parseColor("#FFf8f8f8"));

        hours.clear();
        for (int i = 1; i < 13; i++) {
            hours.add(String.valueOf(i));
        }

        minutes.clear();
        for (int i = 0; i < 60; i++) {

            if (i < 10) {
                minutes.add("0" + i);
            } else {
                minutes.add(String.valueOf(i));
            }

        }

        apms.clear();
        apms.add("AM");
        apms.add("PM");

        return dialogFragment;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.dialog_select_time, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_select).setOnClickListener(this);
        loopView_hour = (LoopView) view.findViewById(R.id.loopView_hour);
        loopView_minute = (LoopView) view.findViewById(R.id.loopView_minute);
        loopView_apm = (LoopView) view.findViewById(R.id.loopView_apm);


        loopView_hour.setItems(hours);
        loopView_hour.setInitPosition(hIndex);
        loopView_minute.setItems(minutes);
        loopView_minute.setInitPosition(mIndex);
        loopView_apm.setItems(apms);
        loopView_apm.setInitPosition(apIndex);
        loopView_apm.setNotLoop();
    }


    public SelectTimeDialog setListener(TimeSelectListener listener) {
        mListener = listener;
        return this;
    }

    public SelectTimeDialog setSelectTime(String timeStr) {

        timeStr = timeStr.toUpperCase();
        if (timeStr.contains("PM")) {
            apIndex = 1;
        } else {
            apIndex = 0;
        }

        timeStr = timeStr.replace("AM", "");
        timeStr = timeStr.replace("PM", "");
        String[] hms = timeStr.split(":");
        for (int i = 0; i < hours.size(); i++) {

            if (hms[0].equals(hours.get(i))) {
                hIndex = i;
                break;
            }
        }

        for (int i = 0; i < minutes.size(); i++) {

            if (hms[1].equals(minutes.get(i))) {
                mIndex = i;
                break;
            }
        }

        return this;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_select && mListener != null) {

            StringBuffer sb = new StringBuffer();
            sb.append(hours.get(loopView_hour.getSelectedItem()));
            sb.append(":");
            sb.append(minutes.get(loopView_minute.getSelectedItem()));
            sb.append(apms.get(loopView_apm.getSelectedItem()).toLowerCase());

            mListener.onTimeSelect(sb.toString());
        }

        dismiss();
    }


    public interface TimeSelectListener {

        void onTimeSelect(String timeString);
    }

}
