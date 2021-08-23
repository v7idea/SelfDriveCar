package org.opencv.samples.facedetect;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.v7idea.template.View.Banner;
import com.v7idea.tool.Constants;
import com.v7idea.tool.DebugLog;
import com.v7idea.tool.ViewScaling;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

import java.util.ArrayList;

public class ScanBluetoothDevicePage extends Activity implements View.OnClickListener{

    private static final String TAG = ScanBluetoothDevicePage.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private V7RCLiteController bleController = null;

    private ListView showBluetoothDeviceList = null;
    private Banner header = null;

    private LinkToDeviceByBluetoothAdapter linkToDeviceByBluetoothAdapter = null;
    private Handler handler = null;

    private Air thisApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisApp = (Air) getApplication();

        ViewScaling.setScaleValue(this);
        bleController = thisApp.getBleController();

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 鎖定螢幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_scan_bluetooth_device_page);

        header = (Banner) ViewScaling.findViewByIdAndScale(this, R.id.ScanBluetoothDevicePage_Banner_Header);
        header.initV7rcStyle();
        header.addButton("", R.mipmap.button_menu_back, this);

        showBluetoothDeviceList = (ListView) ViewScaling.findViewByIdAndScale(this, R.id.ScanBluetoothDevicePage_ListView_ShowDeviceList);
        showBluetoothDeviceList.setOnItemClickListener(onItemClickListener);

        linkToDeviceByBluetoothAdapter = new LinkToDeviceByBluetoothAdapter();
        showBluetoothDeviceList.setAdapter(linkToDeviceByBluetoothAdapter);

        if(handler == null){
            handler = new Handler();
        }

        if(checkPermission())
        {
            handler.post(scanRunnable);
        }
    }

    private boolean checkPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int permissionCOARSE_LOCATION = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            int permissionFINE_LOCATION = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            DebugLog.e(TAG, "permissionCOARSE_LOCATION: "+permissionCOARSE_LOCATION);
            DebugLog.e(TAG, "permissionFINE_LOCATION: "+permissionFINE_LOCATION);

            ArrayList<String> permissionList = new ArrayList<String>();

//            if(permissionCOARSE_LOCATION != PackageManager.PERMISSION_GRANTED)
//            {
//                permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//            }

            if(permissionFINE_LOCATION != PackageManager.PERMISSION_GRANTED)
            {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if(permissionList.size() > 0)
            {
                String[] permissionArray = new String[permissionList.size()];

                for(int i = 0 ; i < permissionList.size() ; i++)
                {
                    permissionArray[i] = permissionList.get(i);
                }

                requestPermissions(permissionArray, Constants.REQUEST_CODE);

                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter.isEnabled() == false) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            if(bleController == null)
            {
                bleController = new V7RCLiteController(this);
            }

            bleController.setCallBack(bluetoothBleController);
            bleController.scanDevice();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DebugLog.e(TAG, "onDestroy");

        if(isFinishing())
        {
            DebugLog.e(TAG, "onDestroy finishing");
            handler.removeCallbacks(scanRunnable);
            bleController.stopScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        shouldShowRequestPermissionRationale();

        if (requestCode == Constants.REQUEST_CODE)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                int permissionCOARSE_LOCATION = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                int permissionFINE_LOCATION = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                if(permissionCOARSE_LOCATION == PackageManager.PERMISSION_GRANTED && permissionFINE_LOCATION == PackageManager.PERMISSION_GRANTED)
                {
                    DebugLog.d(TAG, "have ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION");

                    handler.post(scanRunnable);
                }
                else
                {
                    if(permissionCOARSE_LOCATION != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this, getResources().getString(R.string.cancel_ACCESS_COARSE_LOCATION_permission), Toast.LENGTH_SHORT).show();
                    }
                    else if(permissionFINE_LOCATION != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this, getResources().getString(R.string.cancel_ACCESS_FINE_LOCATION_permission), Toast.LENGTH_SHORT).show();
                    }
                }
            }

//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Now user should be able to use camera
//            }
//            else
//            {
//                // Your app will not have this permission. Turn off all functions
//                // that require this permission or it will force close like your
//                // original question
//            }
        }
    }



    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            header.setTitleString("掃瞄中");
            bleController.stopScan();
            bleController.scanDevice();

            if(handler == null){
                handler = new Handler();
            }

            handler.postDelayed(this, 10000);
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(linkToDeviceByBluetoothAdapter != null)
            {
                final BluetoothDevice targetDevice = linkToDeviceByBluetoothAdapter.getDevice(position);

                if(targetDevice != null)
                {
                    bleController.stopScan();

                    String strDeviceMac = targetDevice.getAddress();
                    thisApp.setTargetDeviceMacAddress(strDeviceMac);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            bleController.connect(targetDevice);
                            thisApp.setBleController(bleController);
                            finish();
                        }
                    });

                    thread.start();
                }
            }
        }
    };



    private V7RCLiteController.BluetoothCallBack bluetoothBleController = new V7RCLiteController.BluetoothCallBack(){


        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onDiscoverCharacteristics() {

        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        }

        @Override
        public void onScanResult(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            DebugLog.d(TAG, "onScanResult: " + device.toString());

            linkToDeviceByBluetoothAdapter.addDevice(device);
        }

        @Override
        public void enable() {

        }

        @Override
        public void disable() {

        }

        @Override
        public void notSupport() {

        }

        @Override
        public void onGattError(int i) {

        }

        @Override
        public void onReadRssi(int i, int i1) {

        }
    };

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacks(scanRunnable);
        thisApp.setBleController(bleController);
        finish();
    }

    private class LinkToDeviceByBluetoothAdapter extends BaseAdapter
    {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LinkToDeviceByBluetoothAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                notifyDataSetChanged();
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
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_ble_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_gattService_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_gattService_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }

        private class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
    }
}
