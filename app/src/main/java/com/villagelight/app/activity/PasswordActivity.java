package com.villagelight.app.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.TextParams;
import com.villagelight.app.R;
import com.villagelight.app.fingerprint.FingerFragment;
import com.villagelight.app.fingerprint.JsFingerUtils;
import com.villagelight.app.fingerprint.PrefUtils;
import com.villagelight.app.util.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.ielse.view.SwitchView;

public class PasswordActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.btn_title_right)
    ImageButton mBtnTitleRight;
    @BindView(R.id.sw)
    SwitchView mSw;
    private boolean flag;
    private JsFingerUtils mFingerUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        ButterKnife.bind(this);

        mFingerUtils = new JsFingerUtils(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Password Protect");

        if (PrefUtils.isProtect(mContext)) {

            mSw.setOpened(true);
        }

        mSw.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {

                if (checkFingerprint()) {

                    FingerFragment fingerFragment = new FingerFragment();
                    fingerFragment.show(getFragmentManager(), "fingerFragment");
                    fingerFragment.setmFragmentCallBack(new FingerFragment.Callback() {
                        @Override
                        public void onSuccess() {

                            flag = true;
                            mSw.toggleSwitch(flag);

                        }

                        @Override
                        public void onError(String msg) {

                            ToastUtils.showToast(mContext, msg);
                            mSw.toggleSwitch(false);

                        }

                        @Override
                        public void onCancel() {
                            mSw.toggleSwitch(false);
                        }

                    });

                } else {

                    mSw.toggleSwitch(false);
                }

            }

            @Override
            public void toggleToOff(SwitchView view) {

                if (checkFingerprint()) {

                    FingerFragment fingerFragment = new FingerFragment();
                    fingerFragment.show(getFragmentManager(), "fingerFragment");
                    fingerFragment.setmFragmentCallBack(new FingerFragment.Callback() {
                        @Override
                        public void onSuccess() {

                            flag = false;
                            mSw.toggleSwitch(flag);

                        }

                        @Override
                        public void onError(String msg) {

                            ToastUtils.showToast(mContext, msg);
                            mSw.toggleSwitch(true);

                        }

                        @Override
                        public void onCancel() {
                            mSw.toggleSwitch(true);
                        }

                    });

                } else {

                    mSw.toggleSwitch(true);
                }

            }
        });
    }

    @OnClick({R.id.btn_title_left, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_save:

                PrefUtils.setProtect(mContext, flag);

                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                            }
                        })
                        .setTitle("SAVE SUCCESSFUL")
                        .setText("Setting has been updated.\n")
                        .configText(new ConfigText() {
                            @Override
                            public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                            }
                        })
                        .setPositive("OK", null)
                        .configPositive(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                            }
                        })
                        .show(getSupportFragmentManager());

                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkFingerprint() {

        if (mFingerUtils.checkSDKVersion()) {

            String result = mFingerUtils.isFinger();

            if (TextUtils.isEmpty(result)) {

                return true;
            } else {
                showErrorDialog(result);
                return false;
            }

        } else {

            showErrorDialog("This system version does not support");
            return false;
        }

    }

    private void showErrorDialog(String text) {

        new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle("SORRY")
                .setText(text + "\n")
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setPositive("OK", null)
                .configPositive(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                    }
                })
                .show(getSupportFragmentManager());
    }
}
