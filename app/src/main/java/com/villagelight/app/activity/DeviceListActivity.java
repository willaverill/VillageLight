package com.villagelight.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.villagelight.app.R;
import com.villagelight.app.adapter.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.fly2think.blelib.AppManager.RFStarManageListener;

public class DeviceListActivity extends BaseActivity implements RFStarManageListener {
	// Debugging
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;


	// Return Intent extra
	public static String EXTRA_DEVICE = "device";

	// Member fields
	private DeviceAdapter deviceAdapter;
	private List<BluetoothDevice> devices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_device);

		app.manager.setRFstarBLEManagerListener(this);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		devices = new ArrayList<BluetoothDevice>();
		deviceAdapter = new DeviceAdapter(this, devices);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(deviceAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		if (app.manager.cubicBLEDevice != null) {
			app.manager.cubicBLEDevice.disconnectedDevice();
			app.manager.cubicBLEDevice = null;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		cancelDiscovery();
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		app.manager.startScanBluetoothDevice();
	}

	private void cancelDiscovery() {
		app.manager.stopScanBluetoothDevice();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			cancelDiscovery();
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE, devices.get(arg2));

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	/*
	 * ble扫描回调
	 */
	@Override
	public void RFstarBLEManageListener(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		// TODO Auto-generated method stub

		devices.add(device);
		deviceAdapter.notifyDataSetChanged();
	}

	/*
	 * ble开始扫描
	 */
	@Override
	public void RFstarBLEManageStartScan() {
		// TODO Auto-generated method stub
		devices.clear();
		deviceAdapter.notifyDataSetChanged();
	}

	/*
	 * ble结束扫描
	 */
	@Override
	public void RFstarBLEManageStopScan() {
		// TODO Auto-generated method stub
		setProgressBarIndeterminateVisibility(false);
		setTitle(R.string.select_device);

		findViewById(R.id.button_scan).setVisibility(View.VISIBLE);
	}
}
