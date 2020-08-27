package com.villagelight.app.fragment;

import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.res.drawable.CircleDrawable;
import com.mylhyl.circledialog.res.values.CircleColor;
import com.mylhyl.circledialog.res.values.CircleDimen;
import com.mylhyl.circledialog.scale.ScaleUtils;
import com.mylhyl.circledialog.view.listener.OnCreateBodyViewListener;
import com.villagelight.app.R;

public class BaseFragment extends Fragment {

    protected void showLimitDialog() {

        new CircleDialog.Builder()
                .setCancelable(false)
                //不影响顶部标题和底部按钮部份
                .setTitle("Schedule Conflict")
                .setBodyView(R.layout.dialog_limit, new OnCreateBodyViewListener() {
                    @Override
                    public void onCreateBodyView(View view) {

                        CircleDrawable bgCircleDrawable = new CircleDrawable(CircleColor.DIALOG_BACKGROUND
                                , 0, 0, 0, 0);
                        view.setBackgroundDrawable(bgCircleDrawable);

                        TextView textView = view.findViewById(R.id.tv_content);
                        textView.setText("“Off”Times must be after “On”times and must remain on the same calendar day. Additional scheduled times must be in sequential order.");

                        int dimenTextSize = ScaleUtils.scaleValue(CircleDimen.CONTENT_TEXT_SIZE);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimenTextSize);


                    }
                })
                .setNegative("Close",null)
                .show(getChildFragmentManager());

    }
}
