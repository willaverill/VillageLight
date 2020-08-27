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
import com.weigan.loopview.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class SelectDialog extends BaseCircleDialog implements View.OnClickListener {

    private OnItemSelectedListener mListener;
    private List<String> items = new ArrayList<>();
    private int initPosition;
    private LoopView loopView;

    public static SelectDialog getInstance() {
        SelectDialog dialogFragment = new SelectDialog();
        dialogFragment.setCanceledBack(false);
        dialogFragment.setCanceledOnTouchOutside(false);
        dialogFragment.setGravity(Gravity.CENTER);
        dialogFragment.setBackgroundColor(Color.parseColor("#FFf8f8f8"));
        return dialogFragment;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.dialog_select_channel, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_select).setOnClickListener(this);
        loopView = (LoopView) view.findViewById(R.id.loopView);
        // 设置原始数据
        loopView.setItems(items);
        loopView.setInitPosition(initPosition);
    }

    public List<String> getItems() {
        return items;
    }

    public SelectDialog setItems(List<String> items) {
        this.items.clear();
        this.items.addAll(items);
        return this;
    }


    public SelectDialog setInitPosition(int initPosition) {
        this.initPosition = initPosition;
        return this;
    }

    public SelectDialog setListener(OnItemSelectedListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_select && mListener != null) {

            mListener.onItemSelected(loopView.getSelectedItem());
        }

        dismiss();
    }


}
