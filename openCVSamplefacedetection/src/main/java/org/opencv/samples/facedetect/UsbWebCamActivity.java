package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.BitmapCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.encoder.MediaAudioEncoder;
import com.serenegiant.encoder.MediaEncoder;
import com.serenegiant.encoder.MediaMuxerWrapper;
import com.serenegiant.encoder.MediaSurfaceEncoder;
import com.serenegiant.encoder.MediaVideoBufferEncoder;
import com.serenegiant.encoder.MediaVideoEncoder;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.v7idea.Data.DetectionValue;
import com.v7idea.Data.SaveRecord;
import com.v7idea.DataBase.DataBase;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.template.View.AutoReleaseImageView;
import com.v7idea.template.View.V7TitleView;
import com.v7idea.tool.CVCameraWrapper;
import com.v7idea.tool.Commissioner;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.ParamsFilter;
import com.v7idea.tool.UsbCameraFrame;
import com.v7idea.tool.ViewScaling;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.facedetect.View.DetectionValueGroup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 參考範例1
 */
public class UsbWebCamActivity extends BaseActivity implements CameraDialog.CameraDialogParent
        , V7RCLiteController.BluetoothCallBack {

    private final static String TAG = "UsbWebCamActivity";

    private final Object mSync = new Object();

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor = null;
    private MediaMuxerWrapper mMuxer;
    private MediaVideoBufferEncoder mVideoEncoder;
    private static boolean mIsRecording = false;

    private static int SAVE_RECORD = 1;

//    private UVCCamera mUVCCamera = null;

    private Surface mPreviewSurface;

    private CVCameraWrapper mOpenCvCameraView = null;

    private ImageView showUsbImage = null;
    private Button leftButton = null;
    private Button rightButton = null;
    private ImageView startSendCommand = null;
    private ImageView Record = null;

    private Activity currentActivity = null;
    private boolean isStartDetection = false;
    private Thread detectionThread = null;

    private Air thisApp = null;
    private V7RCLiteController bleController = null;
    private ParamsFilter paramsFilter = null;
    private SimpleDatabase simpleDatabase = null;
    private Commissioner commissioner = null;

    private AutoReleaseImageView BtStatusIcon = null;
    private AutoReleaseImageView BtAliceIcon = null;

    private AutoReleaseImageView indicatorImage = null;
    private DetectionValueGroup showLeftValueGroup = null;
    private DetectionValueGroup showRightValueGroup = null;

    private V7TitleView ShowChannel1Value = null;
    private V7TitleView ShowChannel2Value = null;
    private V7TitleView ShowFPS = null;

    private MediaRecorder mediaRecorder = null;
    private Surface mediaRecorderSurface = null;

    private CameraBridgeViewBase.DrawValue cameraWrapperDrawValue = null;

    private static DataBase dataBase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_usb_web_cam);

        thisApp = (Air) getApplication();
        bleController = thisApp.getBleController();

        saveRecords = new ArrayList<SaveRecord>();

        dataBase = new DataBase(this);
        dataBase.Open();

        currentActivity = this;

        ViewScaling.setScaleValue(this);

        showUsbImage = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_ImageView_FrameImage);
        showUsbImage.setOnClickListener(onClickListener);

        LinearLayout Panel = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_LinearLayout_Panel);
        LinearLayout ChannelArea = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_LinearLayout_bleStatusArea);
        ShowChannel1Value = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_V7TitleView_ShowChannel1Value);
        ShowChannel2Value = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_V7TitleView_ShowChannel2Value);
        ShowFPS = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_V7TitleView_ShowFPS);
        LinearLayout bleStatusArea = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_LinearLayout_ChannelArea);

        BtStatusIcon = (AutoReleaseImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_AutoReleaseImageView_BtStatusIcon);
        BtAliceIcon = (AutoReleaseImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_AutoReleaseImageView_BtAliceIcon);

        leftButton = (Button) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_button_leftReferenceLine);
        leftButton.setOnClickListener(onPressLineButton);

        rightButton = (Button) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_button_rightReferenceLine);
        rightButton.setOnClickListener(onPressLineButton);

        ImageView setting = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_AutoReleaseImageView_SettingIcon);
        setting.setOnClickListener(onPressLineButton);

        ImageView EngineeringMode = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_AutoReleaseImageView_EngineeringMode);
        EngineeringMode.setOnClickListener(onPressLineButton);

        LinearLayout ButtonGroup = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_LinearLayout_ButtonGroup);

        startSendCommand = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_ImageView_startSendCommand);
        startSendCommand.setSelected(false);
        startSendCommand.setOnClickListener(onPressLineButton);

        Record = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_ImageView_Record);
        Record.setSelected(false);
        Record.setOnClickListener(onPressLineButton);

        indicatorImage = (AutoReleaseImageView) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_AutoReleaseImageView_Indicator);

        showLeftValueGroup = (DetectionValueGroup) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_DetectionValueGroup_showLeftValueGroup);
        showLeftValueGroup.isShowAccelerator(false);

        showRightValueGroup = (DetectionValueGroup) ViewScaling.findViewByIdAndScale(this, R.id.UsbWebCam_DetectionValueGroup_showRightValueGroup);
        showRightValueGroup.isShowAccelerator(true);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.register();

        mOpenCvCameraView = new CVCameraWrapper(this, 0);
        mOpenCvCameraView.setTextParams();
        mOpenCvCameraView.setCvCameraViewListener(mOpenCvCameraView);
        mOpenCvCameraView.showValueOnLayout = showValueOnLayout;

        cameraWrapperDrawValue = mOpenCvCameraView.getWrapperDrawValue();

        paramsFilter = new ParamsFilter();
        simpleDatabase = new SimpleDatabase();
        commissioner = new Commissioner();
    }

    private void initMediaRecord()
    {
        if(mediaRecorder == null)
        {
            mediaRecorder = new MediaRecorder();
        }

        mediaRecorder.reset();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mediaRecorder.setVideoFrameRate(30);

        mediaRecorder.setVideoSize(ViewScaling.getScreenWidth(), ViewScaling.getScreenHeight());
//        mediaRecorder.setPreviewDisplay(showUsbImageSurface);
    }

    public void startRecord()
    {
        if(isRecordStart == false){
            initMediaRecord();

            try {
                String strFilePath = getVideoFilePath();

                File file = new File(strFilePath);

                mediaRecorder.setOutputFile(file.getAbsolutePath());
                mediaRecorder.prepare();
                mediaRecorderSurface = mediaRecorder.getSurface();
                isRecordStart = true;
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e(TAG, "started recording!!");
        }
    }

    private boolean isRecordStart = false;

    public void endRecord()
    {
        if(isRecordStart) {
            isRecordStart = false;
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder = null;
            mediaRecorderSurface.release();
            mediaRecorderSurface = null;
        } else {
            Log.e(TAG, "is end recording!!");
        }
    }


    private String currentFileName = "";


    private final String FolderName = "SelfDriveVideo";

    private String getVideoFilePath()
    {
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

        currentFileName = "" + System.currentTimeMillis();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FolderName + "/";// + Environment.DIRECTORY_DCIM + "/";

        File file = new File(path);

        if(file.exists() == false){
            file.mkdirs();
        }

        Log.e(TAG, "path: "+path);

        return path + currentFileName + ".mp4";
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            synchronized (mSync) {
                if (thisApp.mUVCCamera == null) {
                    CameraDialog.showDialog(currentActivity);
                } else {
                    releaseCamera();
                }
            }
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
                                    startRecord();
                                }
                                else
                                {
                                    Record.setSelected(false);
                                    endRecord();
                                }
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
            else if(msg.what == SAVE_RECORD){
                Bundle recordData = msg.getData();

                if(recordData != null){
                    byte[] bitmapByteArray = recordData.getByteArray("picture");

                    if(bitmapByteArray != null){
                        SaveRecord saveRecord = new SaveRecord();
                        saveRecord.fileName = recordData.getString("fileName");
                        saveRecord.channel1Value = recordData.getInt("channel1Value");
                        saveRecord.channel2Value = recordData.getInt("channel2Value");
                        saveRecord.saveTime = recordData.getLong("saveTime");
                        saveRecord.picture = bitmapByteArray;
                        saveRecord.grayPixels = recordData.getIntArray("grayPixels");

                        saveRecords.add(saveRecord);

                        if(saveRecords.size() >= 10){
                            RecordDetectionRunnable recordDetectionRunnable = new RecordDetectionRunnable(saveRecords);

                            saveRecords.clear();

                            Thread insertInDBThread = new Thread(recordDetectionRunnable);
                            insertInDBThread.start();
                        }
                    }
                }
            }
        }
    };

    private ArrayList<SaveRecord> saveRecords = null;

    @Override
    public void onResume()
    {
        super.onResume();

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
//                mOpenCvCameraView.isConnectedDevice = true;
                break;

            case BluetoothProfile.STATE_DISCONNECTED:

                Log.e(TAG, "STATE_DISCONNECTED");

                //發送藍牙已斷線指令
                commissioner.setConnectedDevice(false);

                //改變在Camera上的圖示為紅球
//                mOpenCvCameraView.isConnectedDevice = false;
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

    @Override
    protected void onPause() {
        super.onPause();

        bleController.closeCommand();
        commissioner.stopSendMessage();
        commissioner.stopReceiveClientMessage();
        commissioner.release();

        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }

        handler.removeCallbacks(getCameraFrameRunnable);

        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                mUVCCamera.stopPreview();
//            }

            if(detectionThread != null){
                isStartDetection = false;
                detectionThread.interrupt();
                detectionThread = null;
            }
        }
    }


    @Override
    protected void onDestroy() {

        if(isFinishing()){
            synchronized (mSync) {

                releaseCamera();

                mUSBMonitor.unregister();

                if (mUSBMonitor != null) {
                    mUSBMonitor.destroy();
                    mUSBMonitor = null;
                }
            }
//        mUVCCameraView = null;

            bleController.closeCommand();
            bleController.closeConnection();
            commissioner.stopSendMessage();
            commissioner.stopReceiveClientMessage();
            commissioner.release();
        }

        super.onDestroy();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {

    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(currentActivity, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            releaseCamera();
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    final UVCCamera camera = new UVCCamera();
                    camera.open(ctrlBlock);

//					camera.setPreviewTexture(camera.getSurfaceTexture());
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }

                    List<Size> sizeList = camera.getSupportedSizeList();

                    if(sizeList != null && sizeList.size() > 0){
                        for(int i = 0 ; i < sizeList.size() ; i++){
                            Size size = sizeList.get(i);
                            Log.e(TAG, "size.width: " + size.width + " size.height: "+size.height);
                        }
                    }

                    try {
                        camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT
                                , 1, 31, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH);
                    } catch (final IllegalArgumentException e) {
                        // fallback to YUV mode
                        try {
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, 1, 31
                                    , UVCCamera.DEFAULT_PREVIEW_MODE, UVCCamera.DEFAULT_BANDWIDTH);
                        } catch (final IllegalArgumentException e1) {
                            camera.destroy();
                            return;
                        }
                    }

//                    final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                    final SurfaceTexture st = new SurfaceTexture(10);

                    if (st != null) {
                        mPreviewSurface = new Surface(st);
                        camera.setPreviewDisplay(mPreviewSurface);
                        camera.startPreview();
						camera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_NV21/*UVCCamera.PIXEL_FORMAT_NV21*/);
                    }
                    synchronized (mSync) {
                        thisApp.mUVCCamera = camera;
                    }

                    if(detectionThread == null)
                    {
                        detectionThread = new Thread(TestRunnable);
                        detectionThread.start();
                    }
                }
            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the coming device equal to camera device that currently using
            releaseCamera();
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(currentActivity, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    private synchronized void releaseCamera() {
        synchronized (mSync) {
            if (thisApp.mUVCCamera != null) {
                try {
                    thisApp.mUVCCamera.setStatusCallback(null);
                    thisApp.mUVCCamera.setButtonCallback(null);
                    thisApp.mUVCCamera.close();
                    thisApp.mUVCCamera.destroy();
                } catch (final Exception e) {
                    //
                }
                thisApp.mUVCCamera = null;
            }
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
        }
    }


    private UsbCameraFrame usbCameraFrame = null;

    // if you need frame data as byte array on Java side, you can use this callback method with UVCCamera#setFrameCallback
    // if you need to create Bitmap in IFrameCallback, please refer following snippet.
	final Bitmap bitmap = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, Bitmap.Config.ARGB_8888);

	private final IFrameCallback mIFrameCallback = new IFrameCallback() {
		@Override
		public void onFrame(final ByteBuffer frame) {

			frame.clear();

            if(usbCameraFrame == null){
                usbCameraFrame = new UsbCameraFrame();
            }

			synchronized (bitmap) {
                usbCameraFrame.setByteBuffer(frame);
			}
		}
	};

    private CVCameraWrapper.ShowValueOnLayout showValueOnLayout = new CVCameraWrapper.ShowValueOnLayout() {
        @Override
        public void isSuccessValue(boolean isLeftSuccess, boolean isRightSuccess) {

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

	private final Runnable TestRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStartDetection){

                if (usbCameraFrame != null && usbCameraFrame.rgba() != null && mUSBMonitor.isRegistered()){

                    int width = usbCameraFrame.rgba().width();
                    int height = usbCameraFrame.rgba().height();

//                    Log.e(TAG, "width: "+width+"  height: "+height);

                    if(width > 0 && height > 0){

                        UsbCameraFrame detectionFrame = usbCameraFrame.copy();

                        Mat beforeReSize = detectionFrame.rgba();

                        if(beforeReSize.width() > Constants.videoWidth){
                            Mat resizeImage = new Mat();
                            org.opencv.core.Size size = new org.opencv.core.Size(Constants.videoWidth, Constants.videoHeight);
                            Imgproc.resize(beforeReSize, resizeImage, size);

                            detectionFrame.rgba().release();

                            detectionFrame.setRgbaMat(resizeImage);
                        }

                        final long beforeDetectionTime = System.currentTimeMillis();

                        Log.e(TAG, "beforeDetectionTime: "+beforeDetectionTime);

                        final Bitmap recorderBitmap = Bitmap.createBitmap(detectionFrame.rgba().width(), detectionFrame.rgba().height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(detectionFrame.rgba(), recorderBitmap);

//                        Log.e(TAG, "recorderBitmap width: "+recorderBitmap.getWidth());
//                        Log.e(TAG, "recorderBitmap height: "+recorderBitmap.getHeight());

                        Mat afterDetectionMat = mOpenCvCameraView.onCameraFrame(detectionFrame);

                        double channel1Value = mOpenCvCameraView.channel1Value;
                        double channel2Value = mOpenCvCameraView.channel2Value;

                        final int currentChannel1Value = (int)channel1Value;
                        final int currentChannel2Value = (int)channel2Value;

                        final Bitmap bitmap = Bitmap.createBitmap(afterDetectionMat.width(), afterDetectionMat.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(afterDetectionMat, bitmap);

                        showUsbImage.post(new Runnable() {
                            @Override
                            public void run() {
                                showUsbImage.setImageBitmap(bitmap);
                            }
                        });

                        if(mediaRecorderSurface != null && isRecordStart)
                        {
                            synchronized (mediaRecorderSurface){
                                Canvas canvas = mediaRecorderSurface.lockCanvas(null);

                                int screenWidth = ViewScaling.getScreenWidth();
                                int screenHeight = ViewScaling.getScreenHeight();

                                Matrix matrix = getMatrix(screenWidth, screenHeight, recorderBitmap);

                                final Bitmap resizedBitmap = Bitmap.createBitmap(recorderBitmap, 0, 0, recorderBitmap.getWidth(), recorderBitmap.getHeight(), matrix, true);

                                int marginLeft = (canvas.getWidth() - resizedBitmap.getWidth()) / 2;
                                int marginRight = (canvas.getWidth() - resizedBitmap.getWidth()) / 2 + resizedBitmap.getWidth();

                                canvas.drawBitmap(resizedBitmap, new Rect(0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight()),
                                        new Rect(
                                                marginLeft
                                                , (canvas.getHeight() - resizedBitmap.getHeight()) / 2
                                                , marginRight
                                                , (canvas.getHeight() - resizedBitmap.getHeight()) / 2 + resizedBitmap.getHeight()
                                        ), null);

//                                cameraWrapperDrawValue.drawValue(marginLeft, marginRight, resizedBitmap.getWidth(), resizedBitmap.getHeight(), canvas);

                                mediaRecorderSurface.unlockCanvasAndPost(canvas);

                                Bundle bundle = new Bundle();
                                bundle.putString("fileName", currentFileName);
                                bundle.putInt("channel1Value", currentChannel1Value);
                                bundle.putInt("channel2Value", currentChannel2Value);
                                bundle.putLong("saveTime", beforeDetectionTime);

                                long compressStartTime = System.currentTimeMillis();

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                recorderBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                byte[] bytes = byteArrayOutputStream.toByteArray();

                                long compressEndTime = System.currentTimeMillis();

                                bundle.putByteArray("picture", bytes);

                                Log.e(TAG, "compress time: " + (compressEndTime - compressStartTime));


                                //縮圖

                                long scaleBitmapStartTime = System.currentTimeMillis();

                                final Bitmap afterScaleBitmap = ThumbnailUtils.extractThumbnail(recorderBitmap, 100, 100, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

                                long scaleBitmapEndTime = System.currentTimeMillis();

                                Log.e(TAG, "scale time: " + (scaleBitmapEndTime - scaleBitmapStartTime));

                                long toGrayscaleStartTime = System.currentTimeMillis();

                                toGrayscale(afterScaleBitmap);

                                long toGrayscaleEndTime = System.currentTimeMillis();

                                Log.e(TAG, "toGrayscale time: " + (toGrayscaleEndTime - toGrayscaleStartTime));

                                long getPixelsStartTime = System.currentTimeMillis();

                                int x = afterScaleBitmap.getWidth();
                                int y = afterScaleBitmap.getHeight();
                                int[] intArray = new int[x * y];
                                afterScaleBitmap.getPixels(intArray, 0, x, 0, 0, x, y);

                                long getPixelsBitmapEndTime = System.currentTimeMillis();

                                Log.e(TAG, "getPixels time: " + (getPixelsBitmapEndTime - getPixelsStartTime));

                                bundle.putIntArray("grayPixels", intArray);

//                                showUsbImage.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showUsbImage.setImageBitmap(afterScaleBitmap);
//                                    }
//                                });

                                Message message = new Message();
                                message.what = SAVE_RECORD;
                                message.setData(bundle);

                                handler.sendMessage(message);
                            }
                        }else{
//                            showUsbImage.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    showUsbImage.setImageBitmap(bitmap);
//                                }
//                            });
                        }

//                        try {
//                            detectionThread.sleep(30);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            }
        }
    };

	private void toGrayscaleAndShow(final Bitmap bmpOriginal){
        toGrayscale(bmpOriginal);


    }

    private void toGrayscale(Bitmap bmpOriginal)
    {
        Canvas c = new Canvas(bmpOriginal);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
    }

	private class RecordDetectionRunnable implements Runnable{

	    private SaveRecord[] records = null;

	    public RecordDetectionRunnable(ArrayList<SaveRecord> records){
	        this.records = records.toArray(new SaveRecord[0]);
        }

        @Override
        public void run() {
            //db save data

            long dbInsertStartTime = System.currentTimeMillis();

            dataBase.insertDetectionData(this.records);

            long dbInsertEndTime = System.currentTimeMillis();

            Log.e(TAG, "db insert time: " + (dbInsertEndTime - dbInsertStartTime));

            this.records = null;

//            //export file
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/" + FolderName + "/" + currentFileName + "/RecordPicture/";
//
//            File file = new File(path);
//
//            if(file.exists() == false){
//                file.mkdirs();
//            }
//
//            File saveFile = new File(path + saveTime + ".png");
//
//            try {
//                long savePictureStartTime = System.currentTimeMillis();
//
//                FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
//                fileOutputStream.write(byteArray, 0, byteArray.length);
//                fileOutputStream.flush();
//                fileOutputStream.close();
//
//                long savePictureEndTime = System.currentTimeMillis();
//
//                Log.e(TAG, "save picture time: " + (savePictureEndTime - savePictureStartTime));
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


//            try {
//                stream.close();
//                stream = null;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private Matrix getMatrix(int scaleWidth, int scaleHeight, Bitmap bitmap){
        float widthMin = (float) scaleWidth / bitmap.getWidth();
        float heightMin = (float) scaleHeight / bitmap.getHeight();

        Matrix matrix = new Matrix();
        if (widthMin > heightMin) {
            matrix.postScale(heightMin, heightMin);
        } else {
            matrix.postScale(widthMin, widthMin);
        }

        return matrix;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.e(TAG, "open cv load SUCCESS");

                    mOpenCvCameraView.MODE = CVCameraWrapper.MODE_NORMAL;
                    mOpenCvCameraView.updateSetting();

                    synchronized (mSync) {
                        if (thisApp.mUVCCamera != null) {
                            thisApp. mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_NV21/*UVCCamera.PIXEL_FORMAT_NV21*/);
//                            mUVCCamera.startPreview();
                        }

                        if(detectionThread == null)
                        {
                            isStartDetection = true;
                            detectionThread = new Thread(TestRunnable);
                            detectionThread.start();
                        }
                    }
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private View.OnClickListener onPressLineButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.UsbWebCam_button_leftReferenceLine:

                    if(mOpenCvCameraView.isLeftRefernceLine)
                    {
                        mOpenCvCameraView.isLeftRefernceLine = false;
                    }
                    else
                    {
                        mOpenCvCameraView.isLeftRefernceLine = true;
                    }

                    break;

                case R.id.UsbWebCam_button_rightReferenceLine:

                    if(mOpenCvCameraView.isRightRefernceLine)
                    {
                        mOpenCvCameraView.isRightRefernceLine = false;
                    }
                    else
                    {
                        mOpenCvCameraView.isRightRefernceLine = true;
                    }

                    break;

                case R.id.UsbWebCam_AutoReleaseImageView_SettingIcon:
                    thisApp.setBleController(bleController);
                    Intent intent = new Intent(UsbWebCamActivity.this, ChangeDetectionParamsPage.class);
                    intent.putExtra(Constants.APP_MODE, Constants.WEBCAM_MODE);
                    startActivity(intent);
                    break;

                case R.id.UsbWebCam_ImageView_startSendCommand:

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

                case R.id.UsbWebCam_ImageView_Record:

                    if(v.isSelected())
                    {
                        mIsRecording = false;
                        v.setSelected(false);
                        endRecord();
                    }
                    else
                    {
                        mIsRecording = true;
                        v.setSelected(true);
                        startRecord();
                    }

                    break;
            }
        }
    };

    public void onPressChangeCommandPeriod(View view)
    {
        if(view.getId() == R.id.UsbWebCam_TextView_showIpAddress)
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

        if(mOpenCvCameraView != null)
        {
            //改變在Camera上的圖示為綠球
//            mOpenCvCameraView.isConnectedDevice = true;
        }
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
        if(mOpenCvCameraView != null){
//            mOpenCvCameraView.isConnectedDevice = false;
        }
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
}
