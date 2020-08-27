package com.villagelight.app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.model.ChannelManagerSelect;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManagerChannelAdapter extends BaseAdapter {

    private List<ChannelManagerSelect> devices;
    private Context context;
    private String channel;

    public ManagerChannelAdapter(Context context) {
        this.context = context;
        this.devices = new ArrayList<>();
        ChannelManagerSelect channelManagerSelect;

        channelManagerSelect = new ChannelManagerSelect();
        channelManagerSelect.setName("All Unassigned Bulbs (flashing)");
        devices.add(channelManagerSelect);

        channelManagerSelect = new ChannelManagerSelect();
        channelManagerSelect.setName("All Bulbs");
        devices.add(channelManagerSelect);

        channelManagerSelect = new ChannelManagerSelect();
        channelManagerSelect.setName("Bulb Channel");
        devices.add(channelManagerSelect);
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
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_lv_manager_channel, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChannelManagerSelect item = devices.get(position);
        holder.mItemTvName.setText(item.getName());

        if (position == 2) {

            if (TextUtils.isEmpty(channel)) {
                holder.mIvChecked.setVisibility(View.INVISIBLE);
                holder.mItemTvChannel.setVisibility(View.GONE);
            } else {
                holder.mIvChecked.setVisibility(View.GONE);
                holder.mItemTvChannel.setVisibility(View.VISIBLE);
                holder.mItemTvChannel.setText(channel);
            }

        } else {
            holder.mItemTvChannel.setVisibility(View.GONE);
            holder.mIvChecked.setVisibility(item.isChecked() ? View.VISIBLE : View.INVISIBLE);
        }


        return convertView;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
        notifyDataSetChanged();
    }

    public void checked(int position) {

        for (ChannelManagerSelect channelManagerSelect : devices) {

            channelManagerSelect.setChecked(false);

        }

        devices.get(position).setChecked(true);

        notifyDataSetChanged();
    }

    static class ViewHolder {
        @BindView(R.id.item_tv_name)
        TextView mItemTvName;
        @BindView(R.id.item_tv_channel)
        TextView mItemTvChannel;
        @BindView(R.id.iv_checked)
        ImageView mIvChecked;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
