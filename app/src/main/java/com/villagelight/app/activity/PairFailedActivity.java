package com.villagelight.app.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.util.LogUtils;
import com.villagelight.app.util.ToastUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairFailedActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_tips)
    TextView mTvTips;
    @BindView(R.id.iv_error)
    ImageView mIvError;
    @BindView(R.id.btn_try_again)
    Button mBtnTryAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_failed);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText(getIntent().getStringExtra("title"));
        mTvTips.setText(getIntent().getStringExtra("tips"));
        mIvError.setImageResource(getIntent().getIntExtra("error", 0));

        mBtnTryAgain.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                File file = new File("/mnt/sdcard/VillageLight/debug.log");
                if (file.exists()) {

                    ToastUtils.showToast(mContext, file.getPath());
                    LogUtils.shareFile(mContext, file);

                }
                return false;
            }
        });

    }

    @OnClick({R.id.btn_title_left, R.id.btn_cancel, R.id.btn_try_again})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
            case R.id.btn_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_try_again:
                setResult(RESULT_FIRST_USER);
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
