package com.villagelight.app.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.model.ChannelBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChannelAdapter extends BaseAdapter {
    
    private List<ChannelBean> devices;
    private Context context;
    private View.OnClickListener mListener;

    public ChannelAdapter(Context context, List<ChannelBean> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return devices == null ? 0 : devices.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return devices.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_lv_channel, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ChannelBean item = devices.get(position);
        holder.mItemTvName.setText(item.getName());
        holder.mItemColor1.setImageDrawable(new ColorDrawable(item.getDisplayColor1()));

        if (item.isTwinkleOn()) {
            holder.mItemColor2.setImageResource(R.mipmap.color_disable);
            holder.mItemColor3.setImageResource(R.mipmap.color_disable);
        } else {
            holder.mItemColor2.setImageDrawable(new ColorDrawable(item.getDisplayColor2()));
            holder.mItemColor3.setImageDrawable(new ColorDrawable(item.getDisplayColor3()));
        }

        holder.mItemTwinkle.setImageResource(item.isTwinkleOn() ? R.mipmap.ic_twinkle_on : R.mipmap.ic_twinkle_off);


        holder.mItemLayoutColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });
        holder.mItemLayoutColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {

//                    if (item.isTwinkleOn()) {
//                        return;
//                    }

                    if (item.getDisplayColor1() == 0) {//不设置第一个颜色，后面两个颜色不允许设置
                        return;
                    }

                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });
        holder.mItemLayoutColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
//                    if (item.isTwinkleOn()) {
//                        return;
//                    }

                    if (item.getDisplayColor1() == 0) {//不设置第一个颜色，后面两个颜色不允许设置
                        return;
                    }

                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });

        holder.mItemLayoutTwinkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });

        return convertView;
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }

    static class ViewHolder {
        @BindView(R.id.item_tv_name)
        TextView mItemTvName;
        @BindView(R.id.item_color1)
        CircleImageView mItemColor1;
        @BindView(R.id.item_layout_color1)
        FrameLayout mItemLayoutColor1;
        @BindView(R.id.item_color2)
        CircleImageView mItemColor2;
        @BindView(R.id.item_layout_color2)
        FrameLayout mItemLayoutColor2;
        @BindView(R.id.item_color3)
        CircleImageView mItemColor3;
        @BindView(R.id.item_layout_color3)
        FrameLayout mItemLayoutColor3;
        @BindView(R.id.item_twinkle)
        ImageView mItemTwinkle;
        @BindView(R.id.item_layout_twinkle)
        FrameLayout mItemLayoutTwinkle;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
