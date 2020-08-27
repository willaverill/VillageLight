package com.villagelight.app.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.adapter.ColorPagerAdapter;
import com.villagelight.app.fragment.ColorFragment;
import com.villagelight.app.model.ColorBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ColorMenuActivity extends BaseActivity {

    @BindView(R.id.btn_title_left)
    ImageButton mBtnTitleLeft;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.btn_title_right)
    ImageButton mBtnTitleRight;
    @BindView(R.id.vp)
    ViewPager mVp;
    @BindView(R.id.main_linear)
    LinearLayout mMainLinear;
    @BindView(R.id.tv_subtitle)
    TextView mTvSubtitle;
    private int last;
    private List<ColorFragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_menu);
        ButterKnife.bind(this);

        mTvTitle.setText("Color Menu");
        mBtnTitleLeft.setVisibility(View.VISIBLE);

        boolean isTwinkleOn = getIntent().getBooleanExtra("isTwinkleOn", false);

        ColorBean colorBean;

        ArrayList<ColorBean> colors1 = new ArrayList<>();

        if (isTwinkleOn) {

            mTvSubtitle.setText("Select A color to Twinkle");

            colorBean = new ColorBean();
            colorBean.setName("Warm Clear");
            colorBean.setDisplayColor(0xFFfefe9c);
            colorBean.setSendColor(0xFF000000);
            colorBean.setColorNo(2);
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Red");
            colorBean.setDisplayColor(Color.argb(0xFF, 238, 36, 36));
            colorBean.setSendColor(Color.argb(0x00, 238, 36, 36));
            colorBean.setColorNo(5);
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Green");
            colorBean.setDisplayColor(Color.argb(0xff, 83, 189, 117));
            colorBean.setSendColor(Color.argb(0x00, 83, 189, 117));
            colorBean.setColorNo(12);
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Blue");
            colorBean.setDisplayColor(Color.argb(0xFF, 72, 123, 189));
            colorBean.setSendColor(Color.argb(0x00, 72, 123, 189));
            colorBean.setColorNo(17);
            colors1.add(colorBean);

        } else {

            colorBean = new ColorBean();
            colorBean.setName("No Color");
            colorBean.setDisplayColor(0x00000000);
            colorBean.setSendColor(0x00000000);
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Champagne");
            colorBean.setDisplayColor(Color.argb(0xFF, 229, 204, 137));
            colorBean.setSendColor(Color.argb(0x00, 234, 150, 15));
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Warm Clear");
            colorBean.setDisplayColor(0xFFfefe9c);
            colorBean.setSendColor(0xFF000000);
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Winter White");
            colorBean.setDisplayColor(Color.argb(0xFF, 235, 244, 251));
            colorBean.setSendColor(Color.argb(0x00, 210, 175, 45));
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Coral");
            colorBean.setDisplayColor(0xFFdb725d);
            colorBean.setSendColor(0x00db725d);
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Red");
            colorBean.setDisplayColor(Color.argb(0xFF, 238, 36, 36));
            colorBean.setSendColor(Color.argb(0x00, 238, 36, 36));
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Amber");
            colorBean.setDisplayColor(Color.argb(0xFF, 208, 121, 42));
            colorBean.setSendColor(Color.argb(0x00, 255, 72, 0));
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Orange");
            colorBean.setDisplayColor(Color.argb(0xFF, 247, 169, 58));
            colorBean.setSendColor(Color.argb(0x00, 255, 30, 0));
            colors1.add(colorBean);

            colorBean = new ColorBean();
            colorBean.setName("Gold");
            colorBean.setDisplayColor(Color.argb(0xFF, 244, 212, 75));
            colorBean.setSendColor(Color.argb(0x00, 255, 92, 0));
            colors1.add(colorBean);

            for (int i = 0; i < colors1.size(); i++) {
                colors1.get(i).setColorNo(i);
            }

        }


        ArrayList<ColorBean> colors2 = new ArrayList<>();

        colorBean = new ColorBean();
        colorBean.setName("Yellow");
        colorBean.setDisplayColor(Color.argb(0xFF, 236, 235, 91));
        colorBean.setSendColor(Color.argb(0x00, 255, 150, 0));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Lime");
        colorBean.setDisplayColor(0xFF93cd40);
        colorBean.setSendColor(0x0093cd40);
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Mint");
        colorBean.setDisplayColor(Color.argb(0xFF, 72, 140, 93));
        colorBean.setSendColor(Color.argb(0x00, 46, 139, 10));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Green");
        colorBean.setDisplayColor(Color.argb(0xff, 83, 189, 117));
        colorBean.setSendColor(Color.argb(0x00, 83, 189, 117));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Mountain");
        colorBean.setDisplayColor(0xFF4aa587);
        colorBean.setSendColor(0x004aa587);
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Sea Breeze");
        colorBean.setDisplayColor(0xFF61d3b4);
        colorBean.setSendColor(0x0061d3b4);
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Turquoise");
        colorBean.setDisplayColor(Color.argb(0xFF, 125, 204, 198));
        colorBean.setSendColor(Color.argb(0x00, 64, 224, 35));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Aqua");
        colorBean.setDisplayColor(Color.argb(0xff, 146, 214, 227));
        colorBean.setSendColor(Color.argb(0x00, 0, 255, 171));
        colors2.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Blue");
        colorBean.setDisplayColor(Color.argb(0xFF, 72, 123, 189));
        colorBean.setSendColor(Color.argb(0x00, 72, 123, 189));
        colors2.add(colorBean);

        for (int i = 0; i < colors2.size(); i++) {
            colors2.get(i).setColorNo(i + colors1.size());
        }

        ArrayList<ColorBean> colors3 = new ArrayList<>();

        colorBean = new ColorBean();
        colorBean.setName("Royal");
        colorBean.setDisplayColor(Color.argb(0xFF, 59, 87, 150));
        colorBean.setSendColor(Color.argb(0x00, 24, 24, 255));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Purple");
        colorBean.setDisplayColor(Color.argb(0xFF, 93, 88, 168));
        colorBean.setSendColor(Color.argb(0x00, 85, 0, 144));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Violet");
        colorBean.setDisplayColor(Color.argb(0xFF, 201, 141, 192));
        colorBean.setSendColor(Color.argb(0x00, 255, 21, 82));
        colors3.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Pink");
        colorBean.setDisplayColor(Color.argb(0xff, 196, 107, 171));
        colorBean.setSendColor(Color.argb(0x00, 255, 21, 28));
        colors3.add(colorBean);

//        colorBean = new ColorBean();
//        colorBean.setName("Midnight");
//        colorBean.setDisplayColor(Color.argb(0xff, 42, 42, 104));
//        colorBean.setSendColor(Color.argb(0x00, 42, 42, 104));
//        colors3.add(colorBean);
//
//        colorBean = new ColorBean();
//        colorBean.setName("Crimson");
//        colorBean.setDisplayColor(Color.argb(0xff, 207, 50, 69));
//        colorBean.setSendColor(Color.argb(0x00, 207, 50, 69));
//        colors3.add(colorBean);

        for (int i = 0; i < colors3.size(); i++) {
            colors3.get(i).setColorNo(i + colors1.size() + colors2.size());
        }

        ArrayList<ColorBean> transFormColorPosList = transFormColorPosList(colors1,colors2,colors3);

        mFragments = new ArrayList<>();
        mFragments.add(ColorFragment.newInstance(transFormColorPosList));
//        if (!isTwinkleOn) {
//            mFragments.add(ColorFragment.newInstance(colors2));
//            mFragments.add(ColorFragment.newInstance(colors3));
//        }

        mVp.setAdapter(new ColorPagerAdapter(getSupportFragmentManager(), mFragments));

        initDots();

        mVp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mMainLinear.getChildAt(last).setEnabled(false);
                mMainLinear.getChildAt(position).setEnabled(true);
                last = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private ArrayList<ColorBean> transFormColorPosList(ArrayList<ColorBean> colors1,
                                                       ArrayList<ColorBean> colors2,
                                                       ArrayList<ColorBean> colors3) {
        ArrayList<ColorBean> resultList = new ArrayList<>();
        ArrayList<ColorBean> transFormColorPosList = new ArrayList<>();

        transFormColorPosList.addAll(colors1);
        transFormColorPosList.addAll(colors2);
        transFormColorPosList.addAll(colors3);

        ColorBean colorBean = findColorByName(transFormColorPosList,"No Color");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Warm Clear");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Winter White");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Champagne");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Red");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Lime");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Coral");
        resultList.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Empty");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Green");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Mountain");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Sea Breeze");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Mint");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Blue");
        resultList.add(colorBean);
        colorBean = findColorByName(transFormColorPosList,"Royal");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Aqua");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Turquoise");
        resultList.add(colorBean);


        colorBean = findColorByName(transFormColorPosList,"Purple");
        resultList.add(colorBean);


        colorBean = findColorByName(transFormColorPosList,"Violet");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Pink");
        resultList.add(colorBean);

        colorBean = new ColorBean();
        colorBean.setName("Empty");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Amber");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Orange");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Gold");
        resultList.add(colorBean);

        colorBean = findColorByName(transFormColorPosList,"Yellow");
        resultList.add(colorBean);

        return resultList;
    }

    private ColorBean findColorByName(ArrayList<ColorBean> transFormColorPosList ,String name){
        ColorBean target = null;
        for (ColorBean one :
                transFormColorPosList) {
            if (one.getName().equals(name)) {
                target = one;
            }
        }
        return target;
    }


    private void initDots() {
        //设置图片
//        for (int i = 0; i < mFragments.size(); i++) {
//
//            //创建底部指示器(小圆点)
//            View view = new View(mContext);
//            view.setBackgroundResource(R.drawable.dot);
//            view.setEnabled(false);
//            //设置宽高
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, 30);
//            //设置间隔
//            if (i != 0) {
//                layoutParams.leftMargin = 32;
//            }
//            //添加到LinearLayout
//            mMainLinear.addView(view, layoutParams);
//        }
//
//        //第一次显示小白点
//        mMainLinear.getChildAt(0).setEnabled(true);
    }


    @OnClick(R.id.btn_title_left)
    public void onViewClicked() {

        finish();
    }
}
