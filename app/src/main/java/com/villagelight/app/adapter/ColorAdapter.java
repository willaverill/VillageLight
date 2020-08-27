package com.villagelight.app.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.villagelight.app.R;
import com.villagelight.app.model.ColorBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ColorAdapter extends BaseAdapter {

    private List<ColorBean> devices;
    private Context context;
    private View.OnClickListener mListener;

    public ColorAdapter(Context context, List<ColorBean> devices) {
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
                    R.layout.item_gv_color, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ColorBean item = devices.get(position);
        holder.mItemName.setText(item.getName());
        if(item.getName().equals("No Color")){
            holder.mItemColor.setImageResource(R.mipmap.ic_no_color);
        }else {
            holder.mItemColor.setImageDrawable(new ColorDrawable(item.getDisplayColor()));
        }

        if (item.getName().equalsIgnoreCase("empty")){
            holder.mItemColor.setVisibility(View.INVISIBLE);
            holder.mItemName.setVisibility(View.INVISIBLE);
            holder.frame.setVisibility(View.INVISIBLE);
        }else {
            holder.mItemColor.setVisibility(View.VISIBLE);
            holder.mItemName.setVisibility(View.VISIBLE);
            holder.frame.setVisibility(View.VISIBLE);
        }

        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.item_color)
        CircleImageView mItemColor;
        @BindView(R.id.item_name)
        TextView mItemName;
        @BindView(R.id.frame)
        ImageView frame;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
