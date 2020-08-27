package com.villagelight.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.villagelight.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairBulbsActivityStep1 extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_bulbs_step1);
        ButterKnife.bind(this);

        mTvTitle.setText("Switch Setup");
        mBtnTitleLeft.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.btn_title_left, R.id.btn_pair_bulbs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_pair_bulbs:

                Intent intent = new Intent(mContext, PairBulbsActivityStep2.class);
                startActivityForResult(intent, 1024);

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
