package com.villagelight.app.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.villagelight.app.fragment.ColorFragment;

import java.util.List;

/**
 * 适配器
 */
public class ColorPagerAdapter extends FragmentPagerAdapter {

    private List<ColorFragment> mlist;

    public ColorPagerAdapter(FragmentManager fm, List<ColorFragment> list) {
        super(fm);
        this.mlist = list;
    }

    @Override
    public ColorFragment getItem(int arg0) {
        return mlist.get(arg0);//显示第几个页面
    }

    @Override
    public int getCount() {
        return mlist.size();//有几个页面
    }
}