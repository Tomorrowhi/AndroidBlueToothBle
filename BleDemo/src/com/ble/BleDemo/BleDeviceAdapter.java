package com.ble.BleDemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tao on 2015/10/26 0026.
 */
public class BleDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> deviceNames = new ArrayList<String>();
    private List<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();

    public BleDeviceAdapter(Context context) {
        mContext = context;
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_ble_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.name);
            viewHolder.deviceBondState = (TextView) view.findViewById(R.id.bondState);
            viewHolder.deviceType = (TextView) view.findViewById(R.id.type);
            viewHolder.deviceUUID = (TextView) view.findViewById(R.id.uuid);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        BluetoothDevice bluetoothDevice = mLeDevices.get(i);
        final String deviceName = bluetoothDevice.getName();
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
        } else {
            viewHolder.deviceName.setText("unknown_device");
        }
        viewHolder.deviceAddress.setText(bluetoothDevice.getAddress());
        viewHolder.deviceBondState.setText(bluetoothDevice.getBondState() + "");
        viewHolder.deviceType.setText(bluetoothDevice.getType() + "");
        viewHolder.deviceUUID.setText(Arrays.toString(bluetoothDevice.getUuids()));

        return view;
    }

    static class ViewHolder {
        TextView deviceName, deviceAddress, deviceType, deviceBondState, deviceUUID;
    }
}
