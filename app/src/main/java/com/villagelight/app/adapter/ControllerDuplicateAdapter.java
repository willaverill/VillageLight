package com.villagelight.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.model.ControllerBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ControllerDuplicateAdapter extends BaseAdapter {

    private List<ControllerBean> devices;
    private Context context;

    public ControllerDuplicateAdapter(Context context, List<ControllerBean> devices) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_lv_controller_duplicate, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ControllerBean item = devices.get(position);
        holder.mItemTvName.setText(item.getControllerName());

        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.item_tv_name)
        TextView mItemTvName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
