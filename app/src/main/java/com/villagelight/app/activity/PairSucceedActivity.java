package com.villagelight.app.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.params.ButtonParams;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.model.ControllerBean;
import com.villagelight.app.util.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairSucceedActivity extends BaseActivity {

    public static final String KeyControlerName = "ControlerName";

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.et_name)
    EditText mEtName;
    private BluetoothDevice mDevice;
    private ControllerBean mController;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_succeed);
        ButterKnife.bind(this);

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Controller Setup");

        mDevice = getIntent().getParcelableExtra("device");
        mPassword = getIntent().getStringExtra("password");
        mController = (ControllerBean) getIntent().getSerializableExtra("controller");
        if (mController != null) {
            mEtName.setText(mController.getControllerName());
            mEtName.setSelection(mEtName.getText().length());
        }
    }

    @OnClick({R.id.btn_title_left, R.id.btn_cancel, R.id.btn_continue})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_cancel:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.btn_continue:

                String name = mEtName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtils.showToast(mContext, "Controller name cannot be empty.");
                    return;
                }

                ControllerBean controller = null;
                try {
                    controller = ProjectApp.getInstance().getDb().findFirst(Selector.from(ControllerBean.class).where("controllerName", "=", name));
                } catch (DbException e) {
                    e.printStackTrace();
                }
                if (controller != null) {
                    ToastUtils.showToast(mContext, "Controller name has already existed.");
                    return;
                }

                if (mController == null) {//new

                    if (mDevice == null) {
                        ToastUtils.showToast(mContext, "Please go back and try again.");
                        return;
                    }

                    controller = new ControllerBean();
                    controller.setControllerName(name);
                    controller.setDeviceMac(mDevice.getAddress());
                    controller.setDeviceName(mDevice.getName());
                    controller.setPassword(mPassword);

                    if (ProjectApp.getInstance().getDuplicateController() != null) {

                    }

                    try {
                        ProjectApp.getInstance().getDb().save(controller);
                        Intent intent = new Intent();
                        intent.putExtra(KeyControlerName,name);
                        setResult(RESULT_OK , intent);
                        finish();
                    } catch (DbException e) {
                        e.printStackTrace();
                        ToastUtils.showToast(mContext, e.getMessage());
                    }

//                    fixControlYear(controller , view);

                } else {//rename

                    mController.setControllerName(name);

                    try {
                        ProjectApp.getInstance().getDb().update(mController);
                        Intent intent = new Intent();
                        intent.putExtra(KeyControlerName,name);
                        setResult(RESULT_OK,intent);
                        finish();
                    } catch (DbException e) {
                        e.printStackTrace();
                        ToastUtils.showToast(mContext, e.getMessage());
                    }

//                    fixControlYear(mController , view);
                }


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
