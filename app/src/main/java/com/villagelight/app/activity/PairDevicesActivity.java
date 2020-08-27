package com.villagelight.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.villagelight.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairDevicesActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.lv)
    ListView mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_devices);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Pair Devices");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.item_lv_menu);
        mLv.setAdapter(adapter);

        adapter.add("Pair Bulbs");
        adapter.add("Pair Switch");

        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {

                    startActivity(new Intent(mContext, PairBulbsActivityStep1.class));

                } else if (position == 1) {

                    startActivity(new Intent(mContext, PairSwitchActivity.class));

                }
            }
        });
    }


    @OnClick({R.id.btn_title_left})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
        }
    }
}
