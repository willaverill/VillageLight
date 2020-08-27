package com.villagelight.app.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.TextParams;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.adapter.ManagerChannelAdapter;
import com.villagelight.app.config.Constants;
import com.villagelight.app.event.ChannelEvent;
import com.villagelight.app.util.Utils;
import com.villagelight.app.view.MyListView;
import com.villagelight.app.view.SelectDialog;
import com.weigan.loopview.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ManageChannelActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.lv)
    MyListView mLv;
    @BindView(R.id.tv_channel)
    TextView mTvChannel;
    @BindView(R.id.iv_check_mark)
    ImageView mIvCheckMark;
    private List<String> items;
    private int selectedFrom;
    private int selectedTo;
    private ManagerChannelAdapter mAdapter;
    private int selectBulbsIndex = -1;
    private Handler mHandler = new Handler();
    private DialogFragment df;
    private boolean isRestore = false;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            dismissDialog();
            done();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_channel);
        ButterKnife.bind(this);

        items = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            items.add("Channel " + i);
        }

        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mTvTitle.setText("Manage Channels");

        mAdapter = new ManagerChannelAdapter(mContext);
        mLv.setAdapter(mAdapter);
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (position == 2) {
                    SelectDialog.getInstance()
                            .setItems(items)
                            .setInitPosition(selectedFrom)
                            .setListener(new OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(int index) {
                                    mAdapter.checked(position);
                                    selectedFrom = index;
                                    mTvChannel.setText(items.get(index));
                                    mAdapter.setChannel(items.get(index));
                                    selectBulbsIndex = position;
                                }
                            })
                            .show(getSupportFragmentManager(), "SelectDialog");
                } else {
                    mAdapter.checked(position);
                    selectBulbsIndex = position;
                    mAdapter.setChannel("");
                }
            }
        });

        new CircleDialog.Builder()
                .setCancelable(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle("Are you sure?")
                .setText("Managing bulb channels is recommended for experienced Light Stream users only.\n\n" +
                        "Proceed with caution.\n")
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setNegative("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .setPositive("Continue", null)
                .configPositive(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                    }
                })
                .show(getSupportFragmentManager());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    @OnClick({R.id.btn_title_left, R.id.layout_select, R.id.layout_select2, R.id.btn_continue})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.layout_select:
                SelectDialog.getInstance()
                        .setItems(items)
                        .setInitPosition(selectedTo)
                        .setListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int index) {

                                selectedTo = index;
                                mTvChannel.setText(items.get(index));
                                isRestore = false;
                                mIvCheckMark.setVisibility(View.INVISIBLE);
                            }
                        })
                        .show(getSupportFragmentManager(), "SelectDialog");
                break;
            case R.id.layout_select2:

                if (Constants.FirmwareVersion <= 29) {
                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Firmware Compatibility Error")
                            .setText("This function is not compatible with your current firmware. To proceed, update you’re firmware")
                            .setPositive("OK", null)
                            .show(getSupportFragmentManager());
                    return;
                }

                new CircleDialog.Builder()
                        .setCanceledOnTouchOutside(false)
                        .setTitle("Bulb Channel Restore")
                        .setText("This feature is for Light Stream 3 Bulbs only. Are you sure you want to proceed?")
                        .setPositive("Continue", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                isRestore = true;
                                mIvCheckMark.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegative("Cancel", null)
                        .show(getSupportFragmentManager());

                break;
            case R.id.btn_continue:

                if (selectBulbsIndex == -1) {

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .setTitle("Bulb Selection Required")
                            .setText("Please select which bulbs to update.")
                            .setPositive("Continue", null)
                            .show(getSupportFragmentManager());

                    return;
                }


                if (isRestore) {

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .configDialog(new ConfigDialog() {
                                @Override
                                public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                                }
                            })
                            .setTitle("Restore Default Bulb Channels")
                            .setText("This will restore bulbs to their factory assigned channel. This cannot be un-done.\n\n" +
                                    "DO NOT USE THIS FUNCTION FOR LIGHT STREAM 2 BULBS. THIS WILL DAMAGE THE BULBS.")
                            .configText(new ConfigText() {
                                @Override
                                public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                                }
                            })
                            .setNegative("Cancel", null)
                            .setPositive("Continue", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    byte Go = 0x00;

                                    switch (selectBulbsIndex) {

                                        case 0:
                                            Go = (byte) 0x00;
                                            break;
                                        case 1:
                                            Go = (byte) 0xff;
                                            break;
                                        case 2:
                                            Go = (byte) (selectedFrom + 1);
                                            break;
                                        default:
                                            break;

                                    }

                                    //新增一个Change to Default Bulb Channel
                                    byte[] cmdsCH = {(byte) 0xAA, 0x06, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                            (byte) 0x00, (byte) 0x00, Go, (byte) 0xFF, 0x00, 0x55};
                                    sendPackets(Utils.getSendData(cmdsCH));

                                    showDialogCanCancel("", "Updating Controller...");
                                    mHandler.postDelayed(mRunnable, Constants.MANAGE_CHANNEL_TIMEOUT);


                                }
                            })
                            .configPositive(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                                }
                            })
                            .show(getSupportFragmentManager());
                } else {

                    new CircleDialog.Builder()
                            .setCanceledOnTouchOutside(false)
                            .configDialog(new ConfigDialog() {
                                @Override
                                public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                                }
                            })
                            .setTitle("WARNING")
                            .setText("Are you sure you want to continue?")
                            .configText(new ConfigText() {
                                @Override
                                public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                                }
                            })
                            .setNegative("Cancel", null)
                            .setPositive("Continue", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    byte Go = 0x00;

                                    switch (selectBulbsIndex) {

                                        case 0:
                                            Go = (byte) 0x00;
                                            break;
                                        case 1:
                                            Go = (byte) 0xff;
                                            break;
                                        case 2:
                                            Go = (byte) (selectedFrom + 1);
                                            break;
                                        default:
                                            break;

                                    }

                                    byte[] cmdsCH = {(byte) 0xAA, 0x06, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                            (byte) 0x00, (byte) 0x00, Go, (byte) (selectedTo + 1), 0x00, 0x55};
                                    sendPackets(Utils.getSendData(cmdsCH));

                                    showDialogCanCancel("", "Updating Controller...");
                                    mHandler.postDelayed(mRunnable, Constants.MANAGE_CHANNEL_TIMEOUT);


                                }
                            })
                            .configPositive(new ConfigButton() {
                                @Override
                                public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                                }
                            })
                            .show(getSupportFragmentManager());

                }


                break;
        }
    }

    public void onEventMainThread(ChannelEvent event) {

        mHandler.post(mRunnable);
    }


    private void done() {

//        if (df != null) {
//            df.dismiss();
//        }

        if (df != null && df.getDialog() != null
                && df.getDialog().isShowing()) {
            return;
        }

        df = new CircleDialog.Builder()
                .setCanceledOnTouchOutside(false)
                .configDialog(new ConfigDialog() {
                    @Override
                    public void onConfig(DialogParams params) {
//                                params.backgroundColor = Color.DKGRAY;
//                                params.backgroundColorPress = Color.BLUE;
                    }
                })
                .setTitle("Manage Channels Complete")
                .setText("Successfully updated bulbs should now be blue.")
                .configText(new ConfigText() {
                    @Override
                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                    }
                })
                .setPositive("Finished", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .setNegative("Try Again", null)
                .configPositive(new ConfigButton() {
                    @Override
                    public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                    }
                })
                .show(getSupportFragmentManager());

    }
}
