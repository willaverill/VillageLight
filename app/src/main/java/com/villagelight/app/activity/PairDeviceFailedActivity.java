package com.villagelight.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.event.PairEvent;
import com.villagelight.app.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairDeviceFailedActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_switches)
    TextView mTvSwitches;
    @BindView(R.id.tv_bulbs)
    TextView mTvBulbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device_failed);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Unpair Devices");

        mTvBulbs.setText(ProjectApp.getInstance().getSyncBulbs() + " Bulbs");
        mTvSwitches.setText(ProjectApp.getInstance().getSyncSwitches() + " Switch(s)");

        //查询Currently paired device
        byte[] query = {(byte) 0xAA, 0x01, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                (byte) 0x00, (byte) 0x00, 0x00, 0x55};
        sendPackets(Utils.getSendData(query));

    }


    @OnClick({R.id.btn_title_left, R.id.btn_continue})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_continue:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    public void onEventMainThread(PairEvent event) {

        if (event.getBulbs() == -1 && event.getSwitches() == -1) {
            mTvBulbs.setText(ProjectApp.getInstance().getSyncBulbs() + " Bulbs");
            mTvSwitches.setText(ProjectApp.getInstance().getSyncSwitches() + " Switch(s)");
        }

    }
}
