package com.villagelight.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.model.ScheduleBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleAdapter extends BaseAdapter {

    private List<ScheduleBean> devices;
    private Context context;
    private View.OnClickListener mListener;

    public ScheduleAdapter(Context context, List<ScheduleBean> devices) {
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
                    R.layout.item_lv_schedule, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScheduleBean item = devices.get(position);
        holder.mItemTvDate.setText(item.getDate());
        holder.mItemTvColor.setText(item.getColorBean().getName());


        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.item_tv_date)
        TextView mItemTvDate;
        @BindView(R.id.item_tv_color)
        TextView mItemTvColor;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
