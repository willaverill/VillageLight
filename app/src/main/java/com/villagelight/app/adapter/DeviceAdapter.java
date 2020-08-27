package com.villagelight.app.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.villagelight.app.R;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {

	private List<BluetoothDevice> devices;
	private Context context;

	public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
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
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_device, parent, false);
			holder = new ViewHolder();
			holder.item_tv_name = (TextView) convertView
					.findViewById(R.id.item_tv_name);
			holder.item_tv_addr = (TextView) convertView
					.findViewById(R.id.item_tv_addr);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		BluetoothDevice device = devices.get(position);
		holder.item_tv_name.setText(device.getName());
		holder.item_tv_addr.setText(device.getAddress());

		return convertView;
	}

	class ViewHolder {

		TextView item_tv_name;
		TextView item_tv_addr;

	}

}
