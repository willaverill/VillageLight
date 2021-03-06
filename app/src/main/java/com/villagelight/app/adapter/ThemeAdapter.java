package com.villagelight.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.villagelight.app.R;
import com.villagelight.app.model.ThemeColor;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThemeAdapter extends BaseAdapter {

    private List<ThemeColor> devices;
    private Context context;
    private View.OnClickListener mListener;

    public ThemeAdapter(Context context, List<ThemeColor> devices) {
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
                    R.layout.item_lv_my_theme, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ThemeColor item = devices.get(position);
        holder.mItemTvName.setText(item.getName());

        //注意事项，设置item点击，不能对整个holder.itemView设置咯，只能对第一个子View，即原来的content设置，这算是局限性吧。
        holder.mItemTvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });

        holder.mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.mItemView.quickClose();
                if (mListener != null) {
                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });
        holder.mBtnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // holder.mItemView.smoothClose();
                holder.mItemView.quickClose();

                if (mListener != null) {
                    v.setTag(position);
                    mListener.onClick(v);
                }
            }
        });

        return convertView;
    }

    public View.OnClickListener getListener() {
        return mListener;
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }


    static class ViewHolder {
        @BindView(R.id.item_tv_name)
        TextView mItemTvName;
        @BindView(R.id.btn_rename)
        Button mBtnRename;
        @BindView(R.id.btn_delete)
        Button mBtnDelete;
        @BindView(R.id.itemView)
        SwipeMenuLayout mItemView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
