package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.v7idea.DataBase.DataBase;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.template.View.RemoteControlPanel;
import com.v7idea.template.View.SpecialImageView;
import com.v7idea.tool.Channel;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.DebugLog;
import com.v7idea.tool.ViewScaling;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

public class RemoteControlPage extends Activity {

    private static final String TAG = RemoteControlPage.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private int engineering = 0;

    private static Channel[] channels = null;
    private static V7RCLiteController bleController = null;
    private DataBase dataBase = null;
    private CustomerSetting settingData = null;
    private BluetoothDevice getDevice = null;

    private static RemoteControlPanel leftPanel = null;
    private static RemoteControlPanel rightPanel = null;
    private static TextView showValue = null;
    private TextView showConnectStatus = null;
    private Toast toast = null;

    private Air thisApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 鎖定螢幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //隱藏底下有Back鍵的那條黑黑的Bar
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_remote_control_page);

        dataBase = new DataBase(this);
        dataBase.Open();

        thisApp = (Air) getApplication();

        settingData = thisApp.getCurrentSetting(dataBase);
        channels = settingData.getTotalChannels();

        ViewScaling.setScaleValue(this);
        bleController = thisApp.getBleController();
        bleController.DEBUG = true;
        bleController.setCommandPeriod(Air.SEND_DATA_DELAY_TIME);
        bleController.closeCommand();
        bleController.startCommand();

        SpecialImageView background = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlPage_SpecialImageView_backgroundImage);
        SpecialImageView toNextPage = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlPage_SpecialImageView_ToNextPage);
        toNextPage.setOnClickListener(onPressNextPageButton);

        leftPanel = (RemoteControlPanel) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlPage_RemoteControlPanel_leftHandPanel);
        leftPanel.setType(RemoteControlPanel.TYPE_CAR_ACCELERATOR);
        leftPanel.setOnTouchListener(onTouchPanelListener);

        rightPanel = (RemoteControlPanel) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlPage_RemoteControlPanel_rightHandPanel);
        rightPanel.setType(RemoteControlPanel.TYPE_CAR_DIRECTION);
        rightPanel.setOnTouchListener(onTouchPanelListener);

        showConnectStatus = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlPage_TextView_ShowConnectStatus);
        showValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlPage_TextView_ShowValue);

        showConnectStatus.setText("status: connecting");
        showConnectStatus.setOnClickListener(engineeringMode);
        showValue.setOnClickListener(engineeringMode2);

        setValue();

    }


    @Override
    protected void onResume() {
        super.onResume();

        DebugLog.d(TAG, "onResume");

        bleController.setCallBack(bluetoothBleController);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter.isEnabled() == false) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            boolean isNeedGetGatt = false;

            //從 Application 中取出之前已連線裝置的MacAddress
            String connectDeviceMac = thisApp.getTargetDeviceMacAddress();

            //MacAddress 不存在，重新連線
            if(connectDeviceMac == null)
            {
                isNeedGetGatt = true;
            }
            else
            {
                getDevice = bleController.getConnectDevice();

                //存在，確認是否仍然連線
                if(getDevice != null)
                {
                    //若與該MacAddress保持連線，顯示狀態
                    String strErrorMessage = bleController.checkWriteDataChannelIsReady() ? " prepare to write data" : " not find WRITE Characteristic";
                    String strDeviceName = getDevice != null ? getDevice.getName() : "";
                    String strShowString = "deviceName: " + strDeviceName + " status: connect" + strErrorMessage;

                    showConnectStatus.setText(strShowString);
                }
                else
                {
                    //若與該MacAddress沒有連線，斷線並重新連線
                    DebugLog.i(TAG, "斷掉已存在著為斷掉的連線");
                    bleController.closeConnection();

                    isNeedGetGatt = true;
                }
            }

            //是否需要重新連線
            if(isNeedGetGatt && TextUtils.isEmpty(connectDeviceMac) == false)
            {
                //用 Application 中取出之前已連線裝置的MacAddress取得裝置
                boolean isConnectSuccess = bleController.connect(connectDeviceMac);

                if(isConnectSuccess)
                {
                    getDevice = bleController.getConnectDevice();
                }

                if(getDevice == null)
                {
                    DebugLog.i(TAG, "等待連線 ！！");
                    showConnectStatus.setText("waiting connect !!");
                }
            }

            bleController.setFailSafeChannel1(1000);
            bleController.setFailSafeChannel2(1000);

            boolean isSendSuccess = bleController.sendFailSafeCommand();

            Log.e("MainActivity", "isSendSuccess: "+isSendSuccess);

            if(isSendSuccess)
            {
                bleController.closeCommand();
                bleController.startCommand();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DebugLog.d(TAG, "onDestroy");

        if(isFinishing())
        {
//            handler.removeCallbacks(sendDataRunnable);
            bleController.closeCommand();
            DebugLog.d(TAG, "removeRunnable !!");

            dataBase.Close();
        }
    }

    private V7RCLiteController.BluetoothCallBack bluetoothBleController = new V7RCLiteController.BluetoothCallBack(){

        @Override
        public void onConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String strDeviceName = getDevice != null ? getDevice.getName() : "";
                    String strShowString = "deviceName: " + strDeviceName + " status: connect";
                    showConnectStatus.setText(strShowString);
                }
            });
        }

        @Override
        public void onDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String strDeviceName = getDevice != null ? getDevice.getName() : "";
                    String strShowString = "deviceName: " + strDeviceName + " status: disconnect";
                    showConnectStatus.setText(strShowString);
                }
            });
        }

        @Override
        public void onDiscoverCharacteristics()
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String strErrorMessage = bleController.checkWriteDataChannelIsReady() ? " prepare to write data" : " not find WRITE Characteristic";
                    String strShowString = showConnectStatus.getText().toString() + strErrorMessage;
                    showConnectStatus.setText(strShowString);
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        }

        @Override
        public void onScanResult(final BluetoothDevice device, int rssi, byte[] scanRecord) {}

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

//    private static Runnable sendDataRunnable = new Runnable() {
//        @Override
//        public void run()
//        {
//            int rightOffset = 0;
//            int leftOffset = 0;
//
//            if(rightPanel.getStatus() == RemoteControlPanel.STATUS_TOUCH_MOVE){
//                rightOffset = rightPanel.getScaleOffsetX();
//            }
//
//            if(leftPanel.getStatus() == RemoteControlPanel.STATUS_TOUCH_MOVE){
//                leftOffset = leftPanel.getScaleOffsetY();
//            }
//
////            DebugLog.e(TAG, "channels[0].MaximunValue is： " + channels[0].getMaximunValue());
////            DebugLog.e(TAG, "channels[0].MinimunValue is： " + channels[0].getMinimunValue());
//
//            String showValueString = "channel1: "+ (int)(channels[0].getChannelValue()) + "("+rightOffset+")"
//                    + "  channel2: " + (int)(channels[1].getChannelValue()) + "("+leftOffset+")";
//
//            showValue.setText(showValueString);
//
//            bleController.setChannel1((int)(channels[0].getChannelValue()));
//            bleController.setChannel2((int)(channels[1].getChannelValue()));
//
//            handler.postDelayed(this, 50);
//        }
//    };
//
//    private static Handler handler = new Handler();

    private void setValue()
    {
        int rightOffset = 0;
        int leftOffset = 0;

        if(rightPanel.getStatus() == RemoteControlPanel.STATUS_TOUCH_MOVE){
            rightOffset = rightPanel.getScaleOffsetX();
        }

        if(leftPanel.getStatus() == RemoteControlPanel.STATUS_TOUCH_MOVE){
            leftOffset = leftPanel.getScaleOffsetY();
        }

//            DebugLog.e(TAG, "channels[0].MaximunValue is： " + channels[0].getMaximunValue());
//            DebugLog.e(TAG, "channels[0].MinimunValue is： " + channels[0].getMinimunValue());

        String showValueString = "channel1: "+ (int)(channels[0].getChannelValue()) + "("+rightOffset+")"
                + "  channel2: " + (int)(channels[1].getChannelValue()) + "("+leftOffset+")";

        showValue.setText(showValueString);

        bleController.setChannel1((int)(channels[0].getChannelValue()));
        bleController.setChannel2((int)(channels[1].getChannelValue()));
    }

    @Override
    public void onBackPressed() {
        bleController.closeCommand();
        bleController.closeConnection();
//        handler.removeCallbacks(sendDataRunnable);
        finish();
    }

    private View.OnClickListener engineeringMode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            engineering++;

            if(engineering >= 3)
            {
                engineering = 0;

                Dialog dialog = getChangeSendDataFrequencyDialog(v.getContext());
                dialog.show();
            }
        }
    };

    private View.OnClickListener engineeringMode2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String showMessage = "現在寫入方式為" + Air.SEND_DATA_DELAY_TIME ;

            int currentWriteType = bleController.getCurrentWriteCharacteristicType();

            switch (currentWriteType)
            {
                case BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT:
                    showMessage = "現在寫入方式為" + " WRITE_TYPE_DEFAULT";
                    break;

                case BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE:
                    showMessage = "現在寫入方式為" + " WRITE_TYPE_NO_RESPONSE";
                    break;

                case BluetoothGattCharacteristic.WRITE_TYPE_SIGNED:
                    showMessage = "現在寫入方式為" + " WRITE_TYPE_SIGNED";
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("調整寫入方式");
            builder.setMessage(showMessage);
            builder.setNegativeButton("DEFAULT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bleController.setCurrentWriteCharacteristicType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                }
            });
            builder.setPositiveButton("NO_RESPONSE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bleController.setCurrentWriteCharacteristicType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                }
            });
            builder.setNeutralButton("SIGNED", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bleController.setCurrentWriteCharacteristicType(BluetoothGattCharacteristic.WRITE_TYPE_SIGNED);
                }
            });

            final AlertDialog thisDialog = builder.create();
            thisDialog.setCancelable(false);
            thisDialog.show();
        }
    };

    private Dialog getChangeSendDataFrequencyDialog(Context context)
    {
        String showMessage = "每" + Air.SEND_DATA_DELAY_TIME + "毫秒發送一次資料";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("調整發送頻率");
        builder.setMessage(showMessage);
        builder.setNegativeButton("-5", null);
        builder.setPositiveButton("+5", null);
        builder.setNeutralButton("確定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SimpleDatabase simpleDatabase = new SimpleDatabase();
                simpleDatabase.setValueByKey(Constants.Setting.DELAY_TIME
                        , Air.SEND_DATA_DELAY_TIME);
            }
        });

        final AlertDialog thisDialog = builder.create();
        thisDialog.setCancelable(false);

        thisDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button addFiveMillis = thisDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                addFiveMillis.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Air.SEND_DATA_DELAY_TIME += 5;

                        String showMessage = "每" + Air.SEND_DATA_DELAY_TIME + "毫秒發送一次資料";

                        bleController.setCommandPeriod(Air.SEND_DATA_DELAY_TIME);

                        thisDialog.setMessage(showMessage);
                    }
                });

                Button plusFiveMillis = thisDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                plusFiveMillis.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Air.SEND_DATA_DELAY_TIME -= 5;

                        String showMessage = "每" + Air.SEND_DATA_DELAY_TIME + "毫秒發送一次資料";

                        bleController.setCommandPeriod(Air.SEND_DATA_DELAY_TIME);

                        thisDialog.setMessage(showMessage);
                    }
                });
            }
        });

        return thisDialog;
    }

    private View.OnClickListener onPressNextPageButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RemoteControlPage.this, RemoteControlSettingPage.class);
            startActivity(intent);
            bleController.closeCommand();
            thisApp.setBleController(bleController);
//            handler.removeCallbacks(sendDataRunnable);
            finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
        {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnTouchListener onTouchPanelListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:

                    if(v.getId() == R.id.RemoteControlPage_RemoteControlPanel_leftHandPanel)
                    {
                        float thisPointDownX = event.getX();
                        float thisPointDownY = event.getY();
                        leftPanel.setTouch();
                        leftPanel.setFirstTouchPoint(thisPointDownX, thisPointDownY);
                    }
                    else  if(v.getId() == R.id.RemoteControlPage_RemoteControlPanel_rightHandPanel)
                    {
                        float thisPointDownX = event.getX();
                        float thisPointDownY = event.getY();
                        rightPanel.setTouch();
                        rightPanel.setFirstTouchPoint(thisPointDownX, thisPointDownY);
                    }

                    break;

                case MotionEvent.ACTION_MOVE:

                    if(v.getId() == R.id.RemoteControlPage_RemoteControlPanel_leftHandPanel)
                    {
                        leftPanel.setTouchMove();
                        leftPanel.calculateSendPoint(event.getX(), event.getY());

                        PointF value = leftPanel.getCurrentTouchPoint();

                        channels[1].calculateResult((int)(value.y));

//                    DebugLog.d(TAG,"leftPanel channels[1] value " + channels[1].getChannelValue());
                    }
                    else  if(v.getId() == R.id.RemoteControlPage_RemoteControlPanel_rightHandPanel)
                    {
                        rightPanel.setTouchMove();
                        rightPanel.calculateSendPoint(event.getX(), event.getY());

                        PointF value = rightPanel.getCurrentTouchPoint();

                        channels[0].calculateResult((int)(value.x));

//                    DebugLog.d(TAG,"rightPanel channels[0] value " + channels[0].getChannelValue());
                    }

                    setValue();

                    break;

                case MotionEvent.ACTION_UP:

                    if(v.getId() == R.id.RemoteControlPage_RemoteControlPanel_leftHandPanel)
                    {
                        leftPanel.setUnTouch();
                        leftPanel.reset();
                        channels[1].rest();
                    }
                    else  if(v.getId() == R.id.RemoteControlPage_RemoteControlPanel_rightHandPanel)
                    {
                        rightPanel.setUnTouch();
                        rightPanel.reset();
                        channels[0].rest();
                    }

                    setValue();

                    break;
            }

            return true;
        }
    };
}
