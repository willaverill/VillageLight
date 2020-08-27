package com.villagelight.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.callback.ConfigText;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.ItemsParams;
import com.mylhyl.circledialog.params.TextParams;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.R;
import com.villagelight.app.adapter.ThemeAdapter;
import com.villagelight.app.model.ThemeColor;
import com.villagelight.app.util.ToastUtils;
import com.villagelight.app.util.Utils;
import com.villagelight.app.view.MyListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThemeColorActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.btn_title_right)
    ImageButton mBtnTitleRight;
    @BindView(R.id.lv_my)
    MyListView mLvMy;
    @BindView(R.id.lv_default)
    MyListView mLvDefault;
    @BindView(R.id.separator)
    View mSeparator;
    private List<ThemeColor> mList = new ArrayList<>();
    private ThemeAdapter mAdapter;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_color);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences("EditThemeActivity", Context.MODE_PRIVATE);
        mTvTitle.setText("Themes & Colors");
        mBtnTitleLeft.setVisibility(View.VISIBLE);
        mBtnTitleRight.setVisibility(View.VISIBLE);
        mBtnTitleRight.setImageResource(R.mipmap.ic_add_black);

        ArrayAdapter<ThemeColor> adapter = new ArrayAdapter<>(mContext, R.layout.item_lv_default_theme,
                app.getDefaultThemeColors());
        mLvDefault.setAdapter(adapter);


        mAdapter = new ThemeAdapter(mContext, mList);
        mLvMy.setAdapter(mAdapter);

        setListener();

//        try {
//            ProjectApp.getInstance().getDb().createTableIfNotExist(ThemeColor.class);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1024 && resultCode == RESULT_OK) {
            ThemeColor themeColor = (ThemeColor) data.getExtras().getSerializable("THEME");
            Intent intent = new Intent();
            intent.putExtra("THEME", themeColor);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mList.clear();
//        List<ThemeColor> list = null;
//        try {
//            list = ProjectApp.getInstance().getDb()
//                    .findAll(Selector.from(ThemeColor.class).where("cid", "=",
//                            ProjectApp.getInstance().getCurrentControl().getId()));
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
        if (ProjectApp.getInstance().getSyncThemeColors() != null
                && !ProjectApp.getInstance().getSyncThemeColors().isEmpty()) {
            mList.addAll(ProjectApp.getInstance().getSyncThemeColors());
        }
        mAdapter.notifyDataSetChanged();
        showView();
    }

    @OnClick({R.id.btn_title_left, R.id.btn_title_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_title_left:
                finish();
                break;
            case R.id.btn_title_right:
                Intent intent = new Intent(mContext, EditThemeActivity.class);
                startActivityForResult(intent, 1024);
                break;
        }
    }

    private void showView() {

        mSeparator.setVisibility(mList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setListener() {

        mAdapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int position = (int) v.getTag();
                final ThemeColor item = mList.get(position);

                switch (v.getId()) {
                    case R.id.item_tv_name:

//                        Intent intent1 = new Intent();
//                        intent1.putExtra("theme", item.getName());
//                        setResult(RESULT_OK,intent1);
//                        finish();
                        Intent intent1 = new Intent(mContext, EditThemeActivity.class);
                        intent1.putExtra("theme", item);
                        startActivity(intent1);

                        break;
                    case R.id.btn_delete:

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
                                .setText("Are you sure to delete this theme?")
                                .configText(new ConfigText() {
                                    @Override
                                    public void onConfig(TextParams params) {
//                                    params.padding = new int[]{150, 10, 50, 10};
                                    }
                                })
                                .setNegative("Cancel", null)
                                .setPositive("Confirm", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        try {

                                            ProjectApp.getInstance().getSyncThemeColors().remove(position);
//                                            ProjectApp.getInstance().getDb().delete(item);
                                            ThemeColor themeColor = mList.remove(position);
                                            sharedPreferences.edit().remove("ThemeColor_"+themeColor.getName()).apply();
                                            //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                                            //且如果想让侧滑菜单同时关闭，需要同时调用 ((SwipeMenuLayout) holder.itemView).quickClose();
                                            mAdapter.notifyDataSetChanged();
                                            showView();

                                            byte[] cmdsDT = {(byte) 0xAA, 0x13, 0x00, (byte) ProjectApp.getInstance().getSerialNumber(),
                                                    (byte) 0x00, (byte) 0x00, (byte) item.getId(), 0x00, 0x55};
                                            sendPackets(Utils.getSendData(cmdsDT));

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            ToastUtils.showToast(mContext, e.getMessage());
                                        }

                                    }
                                })
                                .configPositive(new ConfigButton() {
                                    @Override
                                    public void onConfig(ButtonParams params) {
//                                    params.backgroundColorPress = Color.RED;
                                    }
                                })
                                .show(getSupportFragmentManager());

                        break;
                    case R.id.btn_rename:

                        Intent intent2 = new Intent(mContext, EditThemeActivity.class);
                        intent2.putExtra("theme", item);
                        startActivity(intent2);

                        break;
                }

            }
        });

        mLvDefault.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent();
//                intent.putExtra("theme", (ThemeColor) parent.getAdapter().getItem(position));
//                setResult(RESULT_OK, intent);
//                finish();
            }
        });

    }

    private int getThemeColorIndex(ThemeColor tc) {

        List<ThemeColor> themeColors = ProjectApp.getInstance().getSyncThemeColors();
        for (int i = 0; i < themeColors.size(); i++) {

            ThemeColor theme = themeColors.get(i);
            if (theme.getName().equals(tc.getName())) {
                return i;
            }
        }

        return -1;

    }


}
