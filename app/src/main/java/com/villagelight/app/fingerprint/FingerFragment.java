package com.villagelight.app.fingerprint;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.villagelight.app.R;


/**
 * Title: FingerFragment
 * Description:指纹识别弹框
 */
public class FingerFragment extends DialogFragment implements FingerListener {

    Dialog mDialog;
    LinearLayout ll_btn;
    TextView tv_msg;
    ImageView iv;

    private Callback mCallback;

    private JsFingerUtils jsFingerUtils;

    private int error_num = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mDialog == null) {
            mDialog = new Dialog(getActivity(), R.style.petgirls_dialog);
            mDialog.setContentView(R.layout.fragment_finger);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.getWindow().setGravity(Gravity.CENTER);
            View view = mDialog.getWindow().getDecorView();
            tv_msg = (TextView) view.findViewById(R.id.tv_msg);
            ll_btn = (LinearLayout) view.findViewById(R.id.ll_btn);
            iv = (ImageView) view.findViewById(R.id.iv);
            view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mCallback != null) {
                        mCallback.onCancel();
                    }
                }
            });


            jsFingerUtils = new JsFingerUtils(getActivity().getApplicationContext());
            String result = jsFingerUtils.isFinger();

            if (!TextUtils.isEmpty(result)) {
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                jsFingerUtils.startListening(this);
            }
        }

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });

        return mDialog;
    }


    public void setmFragmentCallBack(Callback mFragmentCallBack) {
        this.mCallback = mFragmentCallBack;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        jsFingerUtils.cancelListening();
    }

    private void shake(View v) {
        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        v.startAnimation(shake);
    }

    @Override
    public void onStartListening() {

    }

    @Override
    public void onStopListening() {

    }

    @Override
    public void onSuccess(FingerprintManager.AuthenticationResult result) {
        if (mCallback != null) {
            mCallback.onSuccess();
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onFail(boolean isNormal, final String info) {


        if (isNormal) {
            tv_msg.setTextColor(getResources().getColor(R.color.color_FB544B));
            if (error_num == 2) {
//                tv_msg.setText("指纹识别失败三次，请重试");
                tv_msg.setText("Three times the fingerprint identification failed, please try again");
                jsFingerUtils.cancelListening();
                if (mCallback != null) {
                    mCallback.onError(info);
                }
            } else {
                error_num++;
//                tv_msg.setText("指纹验证错误，请重试");
                tv_msg.setText("Fingerprint verification error, please try again");
            }

            shake(iv);
            shake(tv_msg);
        } else {

            tv_msg.setText(info);
        }


    }

    @Override
    public void onAuthenticationError(int errorCode, final CharSequence errString) {

        if (mDialog != null && mDialog.isShowing()) {
//            tv_msg.setText("指纹验证太过频繁，请稍后重试");
            tv_msg.setText("Fingerprint verification is too frequent, please try again later");
            tv_msg.setTextColor(getResources().getColor(R.color.color_FB544B));
        }

        if (mCallback != null) {
            mCallback.onError(errString.toString());
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

    }


    public interface Callback {

        void onSuccess();

        void onError(String msg);

        void onCancel();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        jsFingerUtils.cancelListening();
        mCallback = null;
    }
}
