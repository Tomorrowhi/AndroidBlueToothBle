package com.ble.BleDemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class MyActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TGA";

    private static final long SCAN_PERIOD = 20000;  // 20秒后停止查找搜索.
    private static final int REQUEST_ENABLE_BT = 889;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BleDeviceAdapter mUnConnectBleDeviceAdapter, mConnectBleDeviceAdapter;
    private boolean mScanning = false;
    private Context mContext;
    private BluetoothLeService mBluetoothLeService;

    private TextView msg, deviceMsg;
    private Button btOne, btTwo, btStartScan, btStopScan;
    private ListView unConnectDevicesList, connectDevicesList;

    private String mDeviceAddress;  //需要连接的设备地址

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mHandler = new Handler();
        mContext = this;
        initView();
        initEvent();
        initData();
    }

    private void initData() {
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "抱歉，此设备不支持蓝牙功能", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        openBle();
        startServer();

    }

    private void startServer() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "设备无法初始化");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            // mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void initEvent() {
        clickEvent();
        bleCheck();

        mUnConnectBleDeviceAdapter = new BleDeviceAdapter(mContext);
        mConnectBleDeviceAdapter = new BleDeviceAdapter(mContext);
        unConnectDevicesList.setAdapter(mUnConnectBleDeviceAdapter);
        connectDevicesList.setAdapter(mConnectBleDeviceAdapter);
        //scanLeDevice(true);
    }

    private void openBle() {
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * 检查当前手机是否支持ble 蓝牙
     * 如果不支持退出程序
     */
    private void bleCheck() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "此手机不支持Ble", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 点击事件初始化
     */
    private void clickEvent() {
        btOne.setOnClickListener(this);
        btTwo.setOnClickListener(this);
        btStartScan.setOnClickListener(this);
        btStopScan.setOnClickListener(this);
    }

    private void initView() {
        msg = (TextView) findViewById(R.id.msg);
        deviceMsg = (TextView) findViewById(R.id.device_msg);
        btOne = (Button) findViewById(R.id.bt_one);
        btTwo = (Button) findViewById(R.id.bt_two);
        btStartScan = (Button) findViewById(R.id.bt_start_scan);
        btStopScan = (Button) findViewById(R.id.bt_stop_scan);
        unConnectDevicesList = (ListView) findViewById(R.id.unconnect_devices);
        connectDevicesList = (ListView) findViewById(R.id.connect_devices);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_start_scan:
                mUnConnectBleDeviceAdapter.clear();
                mConnectBleDeviceAdapter.clear();
                scanLeDevice(true);
                showDevices();
                break;
            case R.id.bt_stop_scan:
                scanLeDevice(false);
                break;
            case R.id.bt_one:

                break;

            case R.id.bt_two:

                break;
        }
    }

    /**
     * 已配对设备
     */
    private void showDevices() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            Iterator<BluetoothDevice> it = bondedDevices.iterator();
            BluetoothDevice bluetoothDevice = null;
            while (it.hasNext()) {
                bluetoothDevice = it.next();
                Log.d(TAG, "bondedDevices:" + bluetoothDevice.getName()
                        + ":" + bluetoothDevice.getAddress()
                        + ":" + bluetoothDevice.getBondState()
                        + ":" + Arrays.toString(bluetoothDevice.getUuids()));
                //启动该项连接
                mBluetoothLeService.connect(bluetoothDevice.getAddress());
                mConnectBleDeviceAdapter.addDevice(bluetoothDevice);
                mConnectBleDeviceAdapter.notifyDataSetChanged();
            }
        }


    }

    /**
     * 扫描蓝牙设备
     *
     * @param enable 是否执行扫描
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * Device scan callback.
     * 当扫描到设备时，会调用此回掉
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    final BleAdvertisedData badata = BleUtil.parseAdertisedData(scanRecord);
//                    String deviceName = device.getName();
//                    if (deviceName == null) {
//                        deviceName = badata.getName();
//
//                    }
                    Log.d(TAG, "device:" + device.getName() + ":" + device.getAddress());
                    mUnConnectBleDeviceAdapter.addDevice(device);
                    mUnConnectBleDeviceAdapter.notifyDataSetChanged();
                }
            });
        }
    };

}