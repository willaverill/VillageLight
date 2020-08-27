package com.villagelight.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.villagelight.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.fly2think.blelib.TransUtils;

public class PairSwitchActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_switch);
        ButterKnife.bind(this);

        mTvTitle.setText("Switch Setup");
        mBtnTitleLeft.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.btn_title_left, R.id.btn_pair_switch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_pair_switch:

                showDialog("","Pairing...");

                sendPackets(TransUtils.hex2bytes("AA 05 06 02 00 00 01 00 55".replace(" ","")));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        dismissDialog();

                        Intent intent = new Intent(mContext, PairFailedActivity.class);
                        intent.putExtra("title", "Switch Setup");
                        intent.putExtra("tips", "Something went wrong, try pairing your switch again.");
                        intent.putExtra("error", R.mipmap.pair_failed_switch);
                        startActivityForResult(intent, 1024);
                    }
                },4000);



                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024) {
            if (resultCode == RESULT_FIRST_USER) {

            } else if (resultCode == RESULT_CANCELED) {

                finish();

            } else if (resultCode == RESULT_OK) {


            }
        }
    }
}
