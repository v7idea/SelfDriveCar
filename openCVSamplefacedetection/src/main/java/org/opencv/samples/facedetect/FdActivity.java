package org.opencv.samples.facedetect;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.samples.facedetect.View.DetectionValueGroup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.v7idea.Activity.BaseActivity;
import com.v7idea.Data.DetectionValue;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.template.View.AutoReleaseImageView;
import com.v7idea.template.View.V7TitleView;
import com.v7idea.tool.CVCameraWrapper;
import com.v7idea.tool.Commissioner;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.DebugLog;
import com.v7idea.tool.ParamsFilter;
import com.v7idea.tool.ViewScaling;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

import java.util.ArrayList;
import java.util.List;


public class FdActivity extends BaseActivity implements V7RCLiteController.BluetoothCallBack
{

    private static final String TAG = "OCVSample::Activity";
    private CVCameraWrapper mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    int deviceState = bleController.getDeviceState();

                    Log.e(TAG, "deviceState: "+deviceState);

                    if(deviceState == BluetoothProfile.STATE_CONNECTED)
                    {
                        Log.e(TAG, "connected device!!");
                        mOpenCvCameraView.MODE = CVCameraWrapper.MODE_NORMAL;
                        mOpenCvCameraView.enableView();
                    }
                    else
                    {
                        Log.e(TAG, "not connected device!!");

                        mOpenCvCameraView.MODE = CVCameraWrapper.MODE_NORMAL;
                        mOpenCvCameraView.enableView();
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private V7RCLiteController bleController = null;
    private Air thisApp = null;

    private ParamsFilter paramsFilter = null;
    private SimpleDatabase simpleDatabase = null;

    private ImageView startSendCommand = null;
    private ImageView Record = null;
    private Button leftButton = null;
    private Button rightButton = null;
    private AutoReleaseImageView BtStatusIcon = null;
    private AutoReleaseImageView BtAliceIcon = null;

    private AutoReleaseImageView indicatorImage = null;
    private DetectionValueGroup showLeftValueGroup = null;
    private DetectionValueGroup showRightValueGroup = null;

    private V7TitleView ShowChannel1Value = null;
    private V7TitleView ShowChannel2Value = null;
    private V7TitleView ShowFPS = null;

    private Commissioner commissioner = null;

    private static final int REQUEST_CODE = 9997;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");

        isHideStatusBar(true);
        keepScreenOn();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.face_detect_surface_view);

        ViewScaling.setScaleValue(this);


        thisApp = (Air) getApplication();
        bleController = thisApp.getBleController();

        mOpenCvCameraView = (CVCameraWrapper) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.showValueOnLayout = showValueOnLayout;
        mOpenCvCameraView.setMaxFrameSize(Constants.videoWidth * 2, Constants.videoHeight * 2);
        mOpenCvCameraView.setTextParams();
        mOpenCvCameraView.setCvCameraViewListener(mOpenCvCameraView);

        LinearLayout Panel = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_LinearLayout_Panel);
        LinearLayout ChannelArea = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_LinearLayout_bleStatusArea);
        ShowChannel1Value = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_V7TitleView_ShowChannel1Value);
        ShowChannel2Value = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_V7TitleView_ShowChannel2Value);
        ShowFPS = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_V7TitleView_ShowFPS);
        LinearLayout bleStatusArea = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_LinearLayout_ChannelArea);

        BtStatusIcon = (AutoReleaseImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_AutoReleaseImageView_BtStatusIcon);
        BtAliceIcon = (AutoReleaseImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_AutoReleaseImageView_BtAliceIcon);

        leftButton = (Button) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_button_leftReferenceLine);
        leftButton.setOnClickListener(onPressLineButton);

        rightButton = (Button) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_button_rightReferenceLine);
        rightButton.setOnClickListener(onPressLineButton);

        ImageView setting = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_AutoReleaseImageView_SettingIcon);
        setting.setOnClickListener(onPressLineButton);

        ImageView EngineeringMode = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_AutoReleaseImageView_EngineeringMode);
        EngineeringMode.setOnClickListener(onPressLineButton);

        LinearLayout ButtonGroup = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_LinearLayout_ButtonGroup);

        startSendCommand = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_ImageView_startSendCommand);
        startSendCommand.setSelected(false);
        startSendCommand.setOnClickListener(onPressLineButton);

        Record = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_ImageView_Record);
        Record.setSelected(false);
        Record.setOnClickListener(onPressLineButton);

        indicatorImage = (AutoReleaseImageView) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_AutoReleaseImageView_Indicator);

        showLeftValueGroup = (DetectionValueGroup) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_DetectionValueGroup_showLeftValueGroup);
        showLeftValueGroup.isShowAccelerator(false);

        showRightValueGroup = (DetectionValueGroup) ViewScaling.findViewByIdAndScale(this, R.id.fdActivity_DetectionValueGroup_showRightValueGroup);
        showRightValueGroup.isShowAccelerator(true);

        paramsFilter = new ParamsFilter();
        simpleDatabase = new SimpleDatabase();

        commissioner = new Commissioner();
    }

    private CVCameraWrapper.ShowValueOnLayout showValueOnLayout = new CVCameraWrapper.ShowValueOnLayout() {
        @Override
        public void isSuccessValue(final boolean isLeftSuccess, final boolean isRightSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isLeftSuccess){
                        showLeftValueGroup.setM1TextColor(Color.BLACK);
                    }
                    else{
                        showLeftValueGroup.setM1TextColor(Color.RED);
                    }

                    if(isRightSuccess){
                        showRightValueGroup.setM1TextColor(Color.BLACK);
                    }
                    else{
                        showRightValueGroup.setM1TextColor(Color.RED);
                    }
                }
            });
        }

        @Override
        public void leftValue(final DetectionValue detectionValue) {
            if(showLeftValueGroup.getVisibility() == View.VISIBLE){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLeftValueGroup.setDetectionValue(detectionValue);
                    }
                });
            }
        }

        @Override
        public void rightValue(final DetectionValue detectionValue) {
            if(showRightValueGroup.getVisibility() == View.VISIBLE){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRightValueGroup.setDetectionValue(detectionValue);
                    }
                });
            }
        }

        @Override
        public void fpsValue(final double fpsValue) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowFPS.setText(String.format("%.2f", fpsValue));
                }
            });
        }

        @Override
        public void channelsValue(final int channel1, final int channel2) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowChannel1Value.setText(""+channel1);
                    ShowChannel2Value.setText(""+channel2);
                }
            });
        }
    };

    private Runnable getCameraFrameRunnable = new Runnable() {
        @Override
        public void run() {
            if(mOpenCvCameraView != null)
            {
                commissioner.setCurrentCameraFrame(mOpenCvCameraView.cameraFrame);
            }

            handler.postDelayed(this, 30);
        }
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == Commissioner.GET_MESSAGE)
            {
                if(msg.obj != null)
                {
                    String message = (String)msg.obj;

                    Log.e(TAG, "message: "+message);

                    String[] commandArray = message.split(";");

                    if(commandArray != null && commandArray.length > 0)
                    {
                        for(int i = 0 ; i < commandArray.length ; i++)
                        {
                            String subMessage = commandArray[i];

                            int startedIndex = subMessage.indexOf(":");

                            String strCommand = subMessage.substring(0, startedIndex);

                            String strValue = subMessage.substring((startedIndex + 1), subMessage.length());

                            Log.e(TAG, "strCommand: "+strCommand);
                            Log.e(TAG, "strValue: "+strValue);

                            if(strCommand.contentEquals("Active"))
                            {
                                if(strValue.contentEquals("true"))
                                {
                                    startSendCommand.setSelected(true);
                                    mOpenCvCameraView.isActive = true;
                                }
                                else
                                {
                                    startSendCommand.setSelected(false);
                                    mOpenCvCameraView.isActive = false;
                                }
                            }
                            else if(strCommand.contentEquals("isRecord"))
                            {
                                if(strValue.contentEquals("true"))
                                {
                                    Record.setSelected(true);
                                    mOpenCvCameraView.startRecord();
                                }
                                else
                                {
                                    Record.setSelected(false);
                                    mOpenCvCameraView.endRecord();
                                }
                            }
                            else if(strCommand.contentEquals("stream"))
                            {
                                if(strValue.contentEquals("true"))
                                {
                                    if(commissioner.isStartSendVideo == false)
                                    {
                                        commissioner.startSendVideo();
                                        handler.post(getCameraFrameRunnable);
                                    }
                                }
                                else
                                {
                                    if(commissioner.isStartSendVideo)
                                    {
                                        commissioner.stopSendVideo();
                                        handler.removeCallbacks(getCameraFrameRunnable);
                                    }
                                }//leftReferentLine  rightReferentLine
                            }
                            else if(strCommand.contentEquals("leftReferentLine"))
                            {
                                if(strValue.contentEquals("true"))
                                {
                                    leftButton.setSelected(true);
                                    mOpenCvCameraView.isLeftRefernceLine = true;
                                }
                                else
                                {
                                    leftButton.setSelected(false);
                                    mOpenCvCameraView.isLeftRefernceLine = false;
                                }//  rightReferentLine
                            }
                            else if(strCommand.contentEquals("rightReferentLine"))
                            {
                                if(strValue.contentEquals("true"))
                                {
                                    rightButton.setSelected(true);
                                    mOpenCvCameraView.isRightRefernceLine = true;
                                }
                                else
                                {
                                    rightButton.setSelected(false);
                                    mOpenCvCameraView.isRightRefernceLine = false;
                                }
                            }
                            else if(strCommand.contentEquals("detectionParameter"))
                            {
                                if(strValue.contentEquals("true"))
                                {
                                    JSONObject detectionParameter = paramsFilter.getCurrentDetectionJSON(simpleDatabase);

                                    if(detectionParameter != null && detectionParameter.length() > 0)
                                    {
                                        commissioner.detectionParameter = detectionParameter;
                                    }
                                }
                                else
                                {
                                    commissioner.detectionParameter = null;
                                }
                            }
                            else  if(strCommand.contentEquals("parameterSetting"))
                            {
                                if(strValue != null && strValue.isEmpty() == false)
                                {
                                    try {
                                        JSONObject testObject = new JSONObject(strValue);
                                        CustomerSetting onlyDetectionParams = paramsFilter.parseJSONObjectToCustomerSetting(testObject);
                                        paramsFilter.setCustomSettingDetectionParamsToSimpleDataBase(simpleDatabase, onlyDetectionParams);

                                        mOpenCvCameraView.setDetectParams(onlyDetectionParams);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    private boolean checkPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int permissionWRITESTORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            DebugLog.e(TAG, "permissionWRITESTORAGE: "+permissionWRITESTORAGE);

            ArrayList<String> permissionList = new ArrayList<String>();

            if(permissionWRITESTORAGE != PackageManager.PERMISSION_GRANTED)
            {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                int permissionWRITESTORAGE = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(permissionWRITESTORAGE == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {
                    String strAlertString = "";

                    if(permissionWRITESTORAGE != PackageManager.PERMISSION_GRANTED)
                    {
//                        Toast.makeText(this, getResources().getString(R.string.cancel_WRITE_EXTERNAL_STORAGE_permission), Toast.LENGTH_SHORT).show();
                        strAlertString += (getResources().getString(R.string.cancel_WRITE_EXTERNAL_STORAGE_permission) + "\n");
                    }

                    if(strAlertString.isEmpty() == false){
                        AlertDialog.Builder builder = new AlertDialog.Builder(FdActivity.this);
                        builder.setMessage(strAlertString);
                        builder.setPositiveButton("確定", null);
                        final AlertDialog thisDialog = builder.create();
                        thisDialog.setCancelable(false);
                        thisDialog.show();
                    }
                }
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(accelerometerListener, mSensorAccelerometer);
        sensorManager.unregisterListener(gyroscopeListener, mSensorGyroscope);

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        bleController.closeCommand();
        commissioner.stopSendMessage();
        commissioner.stopReceiveClientMessage();
        commissioner.release();

        handler.removeCallbacks(getCameraFrameRunnable);
    }

    private Sensor mSensorAccelerometer = null;
    private Sensor mSensorGyroscope = null;

    @Override
    public void onResume()
    {
        super.onResume();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);

        if(sensorList != null && sensorList.size() > 0){
            for(int i = 0 ; i < sensorList.size() ; i++){

                Sensor sensor = sensorList.get(i);

                String strSensor  = "Name: " + sensor.getName()
                        + "\nVersion: " + String.valueOf(sensor.getVersion())
                        + "\nVendor: " + sensor.getVendor()
                        + "\nType: " + String.valueOf(sensor.getType())
                        + "\nMax: " + String.valueOf(sensor.getMaximumRange())
                        + "\nResolution: " + String.valueOf(sensor.getResolution())
                        + "\nPower: " + String.valueOf(sensor.getPower())
                        + "\nClass: " + sensor.getClass().toString();

                DebugLog.e(TAG, strSensor);
            }
        }
        else{
            DebugLog.e(TAG, "沒有陀羅儀！！");
        }

        mSensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(gyroscopeListener, mSensorGyroscope, SensorManager.SENSOR_DELAY_UI);


        bleController.setCallBack(this);

        bleController.startCommand();

        commissioner.init();
        commissioner.startReceiveClientMessage(handler);
        commissioner.startSendMessage(handler);

        switch (bleController.getDeviceState())
        {
            case BluetoothProfile.STATE_CONNECTED:
                Log.e(TAG, "STATE_CONNECTED");
                //發送藍牙已連線指令
                commissioner.setConnectedDevice(true);

                //改變在Camera上的圖示為綠球
                BtStatusIcon.setAlpha(0.5f);
                break;

            case BluetoothProfile.STATE_DISCONNECTED:

                Log.e(TAG, "STATE_DISCONNECTED");

                //發送藍牙已斷線指令
                commissioner.setConnectedDevice(false);

                //改變在Camera上的圖示為紅球
                BtStatusIcon.setAlpha(1f);
                break;
        }

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(isFinishing())
        {
            bleController.closeCommand();
            bleController.closeConnection();
            commissioner.stopSendMessage();
            commissioner.stopReceiveClientMessage();
            mOpenCvCameraView.disableView();

            commissioner.release();
        }
    }

    private View.OnClickListener onPressLineButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.fdActivity_button_leftReferenceLine:

                    if(mOpenCvCameraView.isLeftRefernceLine)
                    {
                        v.setBackgroundResource(R.drawable.reference_line_selector);
                        mOpenCvCameraView.isLeftRefernceLine = false;
                    }
                    else
                    {
                        v.setBackgroundResource(R.mipmap.buton_circle_on);
                        mOpenCvCameraView.isLeftRefernceLine = true;
                    }

                    break;

                case R.id.fdActivity_button_rightReferenceLine:

                    if(mOpenCvCameraView.isRightRefernceLine)
                    {
                        v.setBackgroundResource(R.drawable.reference_line_selector);
                        mOpenCvCameraView.isRightRefernceLine = false;
                    }
                    else
                    {
                        v.setBackgroundResource(R.mipmap.buton_circle_on);
                        mOpenCvCameraView.isRightRefernceLine = true;
                    }

                    break;

                case R.id.fdActivity_AutoReleaseImageView_SettingIcon:
                    thisApp.setBleController(bleController);
                    Intent intent = new Intent(FdActivity.this, ChangeDetectionParamsPage.class);
                    startActivity(intent);
                    break;

                case R.id.fdActivity_ImageView_startSendCommand:

                    if(v.isSelected())
                    {
                        mOpenCvCameraView.isActive = false;
                        v.setSelected(false);
                    }
                    else
                    {
                        mOpenCvCameraView.isActive = true;
                        v.setSelected(true);
                    }

                    break;

                case R.id.fdActivity_ImageView_Record:

                    if(checkPermission()){
                        if(v.isSelected())
                        {
                            v.setSelected(false);
                            mOpenCvCameraView.endRecord();
                        }
                        else
                        {
                            v.setSelected(true);
                            mOpenCvCameraView.startRecord();
                        }
                    }

                    break;

                case R.id.fdActivity_AutoReleaseImageView_EngineeringMode:{
                    if(v.isSelected()){
                        indicatorImage.setVisibility(View.VISIBLE);
                        showLeftValueGroup.setVisibility(View.VISIBLE);
                        showRightValueGroup.setVisibility(View.VISIBLE);

                        v.setSelected(false);
                    }
                    else{
                        indicatorImage.setVisibility(View.INVISIBLE);
                        showLeftValueGroup.setVisibility(View.INVISIBLE);
                        showRightValueGroup.setVisibility(View.INVISIBLE);

                        v.setSelected(true);
                    }
                }
                break;
            }
        }
    };

    public void onPressChangeCommandPeriod(View view)
    {
        if(view.getId() == R.id.fdActivity_TextView_showIpAddress)
        {
            String showMessage = commissioner.getPhoneIpAddresss();
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(showMessage);
            builder.setPositiveButton("確定", null);
            final AlertDialog thisDialog = builder.create();
            thisDialog.setCancelable(false);
            thisDialog.show();

            return;
        }

        String showMessage = "每" + Air.SEND_DATA_DELAY_TIME + "毫秒發送一次資料";

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
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

        thisDialog.setCancelable(false);
        thisDialog.show();
    }


    @Override
    public void onConnected() {
        //檢查藍牙連線
        Log.e(TAG, "onConnected");
        //發送藍牙已連線指令
        if(commissioner != null)
        {
            commissioner.setConnectedDevice(true);
        }

        BtStatusIcon.setAlpha(1f);
    }

    @Override
    public void onDisconnected() {
        //檢查藍牙連線

        Log.e(TAG, "onDisconnected");

        //發送藍牙已斷線指令
        if(commissioner != null){
            commissioner.setConnectedDevice(false);
        }

        //改變在Camera上的圖示為紅球
        BtStatusIcon.setAlpha(0.5f);
    }

    @Override
    public void onDiscoverCharacteristics() {

    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic bluetoothGattCharacteristic) {

    }

    @Override
    public void onScanResult(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

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

    private SensorEventListener accelerometerListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
//            DebugLog.e(TAG, "TYPE_ACCELEROMETER get event");

            if(event.values != null && event.values.length == 3){
                showRightValueGroup.setSensorValue(event.values);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gyroscopeListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
//            DebugLog.e(TAG, "TYPE_GYROSCOPE get event");
            if(event.values != null && event.values.length == 3){
                showLeftValueGroup.setSensorValue(event.values);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
