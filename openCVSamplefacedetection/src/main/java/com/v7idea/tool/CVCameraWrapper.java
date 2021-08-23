package com.v7idea.tool;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.PermissionChecker;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.v7idea.Data.DetectionResultJava;
import com.v7idea.Data.DetectionValue;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.FpsMeter;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.facedetect.Air;
import org.opencv.samples.facedetect.OpenCVWrapper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by mortal on 2017/7/27.
 */

public class CVCameraWrapper extends CameraBridgeViewBase implements CameraBridgeViewBase.CvCameraViewListener2
{
    private static final String TAG = "CVCameraWrapper";

    private int frameIdentify = 0;

    private Paint paint = null;

    private boolean mCameraFrameReady = false;

    private Mat[] mFrameChain;
    private int mChainIdx = 0;
    private Thread mThread;
    private boolean mStopThread;

    protected JavaCameraFrame[] mCameraFrame;

    private String mCameraId;
    private CameraManager mCameraManager;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder builder;
    private Handler mCameraHandler;
    private Handler mImageHandler;
    private android.util.Size[] mSizes;

    private ImageReader mImageReader;


    public CVCameraWrapper(Context context, int cameraId) {
        super(context, cameraId);
        checkBluetoothChannelAndDetectParams();

        paint = new Paint();
        paint.setTextSize(30f * ViewScaling.getScaleMin());
        paint.setColor(Color.BLACK);

        drawValue = cameraWrapperDrawValue;
    }

    public CVCameraWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkBluetoothChannelAndDetectParams();

        paint = new Paint();
        paint.setTextSize(30f);
        paint.setColor(Color.BLACK);

        drawValue = cameraWrapperDrawValue;
    }

    protected void initializeCamera(int width, int height) {

        Log.e(TAG, "width: "+ width + " height: "+height);

        //initCamera2 會給相機支援寬高，並塞入 mSizes

        synchronized (this) {
            initCamera2();
            //获取摄像头支持的分辨率
            android.util.Size mPreviewSize = calculateCameraFrameSize(mSizes, width, height);
            mFrameWidth = mPreviewSize.getWidth();
            mFrameHeight = mPreviewSize.getHeight();

            if ((getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) && (getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT))
                mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
            else
                mScale = 0;

//            if (width < mFrameWidth || height < mFrameHeight)
//                mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
//            else
//                mScale = 0;

            if (mFpsMeter != null) {
                mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
            }

            mFrameChain = new Mat[2];
            mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
            mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);

            AllocateCache();

            mCameraFrame = new JavaCameraFrame[2];
            mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0], mFrameWidth, mFrameHeight);
            mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1], mFrameWidth, mFrameHeight);
        }
    }

    protected void releaseCamera() {
        synchronized (this) {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.getDevice().close();
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (mFrameChain != null) {
                mFrameChain[0].release();
                mFrameChain[1].release();
            }
            if (mCameraFrame != null) {
                mCameraFrame[0].release();
                mCameraFrame[1].release();
            }
        }
    }


    @Override
    protected boolean connectCamera(int width, int height) {

        /* 1. We need to instantiate camera
         * 2. We need to start thread which will be getting frames
         */
        /* First step - initialize camera connection */
        initializeCamera(width, height);

        mCameraFrameReady = false;

        /* now we can start update thread */
        Log.d(TAG, "Starting processing thread");
        mStopThread = false;
        mThread = new Thread(new CameraWorker());
        mThread.start();

        return true;
    }

    @Override
    protected void disconnectCamera() {
        /* 1. We need to stop thread which updating the frames
         * 2. Stop camera and release it
         */
        Log.d(TAG, "Disconnecting from camera");
        try {
            mStopThread = true;
            Log.d(TAG, "Notify thread");
            synchronized (this) {
                this.notify();
            }
            Log.d(TAG, "Wating for thread");
            if (mThread != null)
                mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mThread =  null;
        }

        /* Now release camera */
        releaseCamera();

        mCameraFrameReady = false;
    }

    private class JavaCameraFrame implements CvCameraViewFrame {
        @Override
        public Mat gray() {
            return mYuvFrameData.submat(0, mHeight, 0, mWidth);
        }

        @Override
        public Mat rgba() {
            Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
            return mRgba;
        }

        public JavaCameraFrame(Mat Yuv420sp, int width, int height) {
            super();
            mWidth = width;
            mHeight = height;
            mYuvFrameData = Yuv420sp;
            mRgba = new Mat();
        }

        public void release() {
            mRgba.release();
        }

        private Mat mYuvFrameData;
        private Mat mRgba;
        private int mWidth;
        private int mHeight;
    }
    private class CameraWorker implements Runnable {

        @Override
        public void run() {
            openCamera2();
            do {
                boolean hasFrame = false;
                synchronized (CVCameraWrapper.this) {
                    try {
                        while (!mCameraFrameReady && !mStopThread) {
                            CVCameraWrapper.this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mCameraFrameReady)
                    {
                        mChainIdx = 1 - mChainIdx;
                        mCameraFrameReady = false;
                        hasFrame = true;
                    }
                }

                if (!mStopThread && hasFrame) {
                    if (!mFrameChain[1 - mChainIdx].empty())
                        deliverAndDrawFrame(mCameraFrame[1 - mChainIdx]);
                }
            } while (!mStopThread);
            Log.d(TAG, "Finish processing thread");
        }
    }

    private void initCamera2() {
        HandlerThread cameraHandlerThread = new HandlerThread("Camera2");
        cameraHandlerThread.start();
        mCameraHandler = new Handler(cameraHandlerThread.getLooper());

        HandlerThread imageHandlerThread = new HandlerThread("image");
        imageHandlerThread.start();
        mImageHandler = new Handler(imageHandlerThread.getLooper());
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] CameraIdList = mCameraManager.getCameraIdList();
            mCameraId = CameraIdList[0];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                mSizes = map.getOutputSizes(SurfaceTexture.class);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera2() {
        if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                mCameraManager.openCamera(mCameraId, stateCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }
    };

    private void takePreview() {

        try {
            //如果是用opengl渲染,ImageReader只是拿来给opencv计算用
            //宽高不能设置太高,高了会影响opengl渲染线程
            //Image的宽高和摄像头支持的宽高有关
            //举个例子，设置600x600,Image的宽高却是640x480
            //640x480这个宽高是摄像头支持宽高，基本是会小于设定值
            mImageReader = ImageReader.newInstance(mFrameWidth, mFrameHeight, ImageFormat.YUV_420_888, 1);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = imageReader.acquireNextImage();
                    if (image == null) {
                        return;
                    }

                    byte[] data = ImageUtils.YUV_420_888toNV21(image);
                    synchronized (CVCameraWrapper.this) {
                        mFrameChain[mChainIdx].put(0, 0, data);
                        mCameraFrameReady = true;
                        CVCameraWrapper.this.notify();
                    }
                    image.close();
                }
            }, mImageHandler);

            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    mCameraCaptureSession = cameraCaptureSession;
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    CaptureRequest previewRequest = builder.build();
                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }
            }, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected android.util.Size calculateCameraFrameSize(android.util.Size[] supportedSizes, int surfaceWidth, int surfaceHeight) {
        int calcWidth = 0;
        int calcHeight = 0;

        int maxAllowedWidth = (mMaxWidth != -1 && mMaxWidth < surfaceWidth)? mMaxWidth : surfaceWidth;
        int maxAllowedHeight = (mMaxHeight != -1 && mMaxHeight < surfaceHeight)? mMaxHeight : surfaceHeight;

        for (android.util.Size size : supportedSizes) {
            int width = size.getWidth();
            int height = size.getHeight();

            if (width <= maxAllowedWidth && height <= maxAllowedHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = (int) width;
                    calcHeight = (int) height;
                }
            }
        }

        return new android.util.Size(calcWidth, calcHeight);
    }


    //取最低16:9分辨率
    private android.util.Size getPreferredPreviewSize(android.util.Size[] sizes, int width, int height) {
        List<android.util.Size> collectorSizes = new ArrayList<>();
        for (android.util.Size option : sizes) {
            //计算啥,只有最低分辨率的才最流畅...
            //取16:9分辨率
            int w = option.getWidth();
            int h = option.getHeight();
            int s = w * 100 / h;
            if (s == 177) {
                collectorSizes.add(option);
            }
        }
        if (collectorSizes.size() > 0) {

            android.util.Size size = Collections.min(collectorSizes, new Comparator<android.util.Size>() {
                @Override
                public int compare(android.util.Size s1, android.util.Size s2) {
                    return Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight());
                }
            });

            Log.e(TAG, "size width: "+ size.getWidth() + " size height: "+size.getHeight());

            return size;
        }


        Log.e(TAG, "sizes[0] width: "+ sizes[0].getWidth() + " sizes[0] height: "+sizes[0].getHeight());

        return sizes[0];
    }

    public void checkBluetoothChannelAndDetectParams()
    {
        if(openCVWrapper == null){
            openCVWrapper = new OpenCVWrapper();
        }

        if(bleController == null)
        {
            Air thisApp = (Air)getContext().getApplicationContext();
            bleController = thisApp.getBleController();
        }

        if(channels == null)
        {
            SimpleDatabase simpleDatabase = new SimpleDatabase();
            ParamsFilter paramsFilter = new ParamsFilter();

            settingData = paramsFilter.getCurrentSetting(simpleDatabase);
            channels = settingData.getTotalChannels();

            setDetectParams(settingData);
        }
    }

    public boolean isOpenCamera() {
        return mEnabled;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        checkBluetoothChannelAndDetectParams();

        SimpleDatabase simpleDatabase = new SimpleDatabase();
        ParamsFilter paramsFilter = new ParamsFilter();

        settingData = paramsFilter.getCurrentSetting(simpleDatabase);
        channels = settingData.getTotalChannels();

        setDetectParams(settingData);

        mRgba = new Mat();
    }

    public void updateSetting(){
        SimpleDatabase simpleDatabase = new SimpleDatabase();
        ParamsFilter paramsFilter = new ParamsFilter();

        settingData = paramsFilter.getCurrentSetting(simpleDatabase);
        channels = settingData.getTotalChannels();

        openCVWrapper.loadSaveParamsData(settingData);

        DETECTION_ERROR_STOP = settingData.getDetectionErrorStop();
        MAX_FORWARD_POWER = settingData.getMaxForwardPower();
        MAX_LEFT_RIGHT_POWER = settingData.getMaxLeftRightPower();

        Log.d(TAG,"channels");
    }

    @Override
    public void onCameraViewStopped() {
        openCVWrapper = null;
        mRgba.release();
    }

    public OpenCVWrapper getOpenCVWrapper()
    {
        checkBluetoothChannelAndDetectParams();
        return openCVWrapper;
    }

    public void setDetectParams(CustomerSetting customerSetting)
    {
        checkBluetoothChannelAndDetectParams();
        openCVWrapper.loadSaveParamsData(customerSetting);

        DETECTION_ERROR_STOP = customerSetting.getDetectionErrorStop();
        MAX_FORWARD_POWER = customerSetting.getMaxForwardPower();
        MAX_LEFT_RIGHT_POWER = customerSetting.getMaxLeftRightPower();
    }

    public void setTextParams()
    {
        checkBluetoothChannelAndDetectParams();

        double scaleValueX = getScaleXValue();
        double scaleValueY = getScaleYValue();

        if(scaleValueX < scaleValueY)
        {
            textSize = 0.7 * scaleValueX;
            scaleMin = scaleValueX;
        }
        else
        {
            textSize = 0.7 * scaleValueY;
            scaleMin = scaleValueY;
        }

//        baseLineWidth = (int)((double)(baseLineWidth) * scaleMin);
//        detectLineWidth = (int)((double)(detectLineWidth) * scaleMin);

        openCVWrapper.baseLineWidth = baseLineWidth;
        openCVWrapper.detectedLineWidth = detectedLineWidth;

        openCVWrapper.setTextSize(textSize);
        openCVWrapper.setScaleMin(scaleMin);
        openCVWrapper.scaleTextPosition(scaleValueX, scaleValueY);
    }

    public double getScaleXValue()
    {
        return (double)(Constants.videoWidth) / (double)(defaultVideoWidth);
    }

    public double getScaleYValue()
    {
        return (double)(Constants.videoHeight) / (double)(defaultVideoHeight);
    }

    public CustomerSetting settingData = null;
    private static Channel[] channels = null;
    private V7RCLiteController bleController = null;

    //跑車子用的正常模式:
    public static final int MODE_NORMAL = 0;

    //調整辨識參數用模式
    public static final int MODE_PARAMS = 1;

    public int DETECTION_ERROR_STOP = 5;

    /**
     * 調整辨識參數模式用的參數，表示只處理到哪個步驟，>8 表示全部處理完
     * 0: 只做完高斯模糊
     * 1: 只做完 autoCanny 邊缘偵測
     * 2: 只做完 HoughLinesP 將修改RHO顯示出來
     * 3: 只做完 HoughLinesP 將修改THETA顯示出來
     * 4: 只做完 HoughLinesP 將修改THRESHOLD顯示出來
     * 5: 只做完 HoughLinesP 將修改MIN_LINE_LEN顯示出來
     * 6: 只做完 HoughLinesP 將修改MAX_LINE_GAP顯示出來
     * 7: 只做完 extrapolate 將修改checkPosSlopes顯示出來
     * 8: 只做完 extrapolate 將修改checkNegSlopes顯示出來
     */
    public static int CURRENT_PROCESS_STEP = 9;

    public static int MODE = MODE_NORMAL;

    public static final int defaultVideoWidth = 960;
    public static final int defaultVideoHeight = 540;

    private DetectionResultJava detectionResultJava = null;
    boolean canWrite = true;
    boolean isDrawFPS = true;
    double fps = 1000.0;
    boolean isSavingVideo = false;

    double rightSlopeValue = 0.0;
    double leftSlopeValue = 0.0;
    double rightIntercpetValue = 0.0;
    double leftIntercpetValue = 0.0;

    int rightTopX = 0;
    int rightTopY = 0;
    int rightBottomX = 0;
    int rightBottomY = 0;
    int leftBottomX = 0;
    int leftBottomY = 0;
    int leftTopX = 0;
    int leftTopY = 0;

    //前進方向運算的基本參數
    int MAX_BRAKES_POWER = -100;
    int UN_DETECTION_LEFT_RIGHT_POWER = 0;
    int UN_DETECTION_FORWARD_POWER = 0;
    int DETECT_ERROR_COUNTING = 0;
    double DETECT_ERROR_RANGE = 0.5;

    //紀錄最後一次方向運算後的數值
    double lastLeftSlope = 0.0;
    double lastLeftIntercept = 0.0;
    int lastLeftTopX = 0;
    int lastLeftTopY = 0;
    int lastLeftBottomX = 0;
    int lastLeftBottomY = 0;

    double lastRightSlope = 0.0;
    double lastRightIntercept = 0.0;
    int lastRightTopX = 0;
    int lastRightTopY = 0;
    int lastRightBottomX = 0;
    int lastRightBottomY = 0;

    double lastTurnValue = 0;
    double lastForward = 0;

    //這裡的預設指的是 "第一次抓到" 左右兩條車道線
    double defaultLeftSlope = 0.0;
    double defaultLeftIntercept = 0.0;
    int defaultLeftTopX = 0;
    int defaultLeftTopY = 0;
    int defaultLeftBottomX = 0;
    int defaultLeftBottomY = 0;

    double defaultRightSlope = 0.0;
    double defaultRightIntercept = 0.0;
    int defaultRightTopX = 0;
    int defaultRightTopY = 0;
    int defaultRightBottomX = 0;
    int defaultRightBottomY = 0;

    double defaultOffset = 0.0;

    //方向動力及輸出的數值

    ///運算後的方向
    public double channel1Value = 0;
    ///輸出給搖控車的方向數值
    public int afterComputeChannel1 = 0;

    ///運算後的前進數值
    public double channel2Value = 0;
    ///輸出給搖控車的前進數值
    public int afterComputeChannel2 = 0;

    int MAX_LEFT_RIGHT_POWER = 50;
    int MAX_FORWARD_POWER = 50;

    public boolean isLeftRefernceLine = false;
    public boolean isRightRefernceLine = false;

    double textSize = 0.7;
    double scaleMin = 1;
    int baseLineWidth = 2;
    int detectedLineWidth = 4;

    private OpenCVWrapper openCVWrapper = null;

    private Mat mRgba;

    private DetectLaneTask testTask = null;

    /**
     * 開始啟動，意思是指，辨識後方向，動力的數值才會被設定進V7RCController中
     */
    public boolean isActive = false;

//    private SendDataDelegate delegate = null;
//
//    /**
//     * 這個是用來發送給Servo資料的interface
//     * 回傳值為陣int的陣列
//     * 0：為channel1計算後的結果
//     * 1：為channel2計算後的結果
//     */
//    public interface SendDataDelegate
//    {
//        int[] sendValue(double channel1Value, double channel2Value);
//    }
//
//    public void setDelegate(SendDataDelegate delegate)
//    {
//        this.delegate = delegate;
//    }


    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        super.surfaceChanged(arg0, arg1, arg2, arg3);

//        Log.d(TAG, "arg2: "+arg2+ " arg3: "+arg3);
//
//        Rect rect = getHolder().getSurfaceFrame();
//
//        int previewWidth = videoWidth * 2;
//        int previewHeight = videoHeight * 2;
//
//        mCamera.stopPreview();
//        Camera.Size PreviewSize = mCamera.getParameters().getPreviewSize();
//        mCamera.getParameters().setPreviewSize(previewWidth, previewHeight);
//
//        mCamera.startPreview();
//
//        Log.d(TAG, "PreviewSize.width: "+PreviewSize.width+ " PreviewSize.height: "+PreviewSize.height);
    }

    public class CameraFrame
    {
        public long createTime = -1l;
        public int sendDataCount = -1;
        public int imageIdentify = -1;

        public byte[] dataArray = null;

        public Mat imageMat = null;

        public void setData(Mat rgbMat, int imageIdentify)
        {
            this.imageIdentify = imageIdentify;
            this.createTime = System.currentTimeMillis();
            this.imageMat = rgbMat;
        }

        public void prepareSendData(int bufferSize)
        {
            if(imageMat != null)
            {
                long startCovertMatToJPG = System.currentTimeMillis();

//                MatOfByte mob = new MatOfByte();
//                MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
//
//                Imgcodecs.imencode(".jpeg", imageMat, mob, params);
//                dataArray = mob.toArray();

                Bitmap bitmap = Bitmap.createBitmap(imageMat.width(), imageMat.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(imageMat, bitmap);

                if(bitmap != null)
                {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    dataArray = byteArrayOutputStream.toByteArray();

//                    int index = Client.getByteIndex(dataArray,Client.SOI_MARKER,0);
//
//                    DebugLog.e(TAG, "index: "+index);

                    long endCovertMatToJPG = System.currentTimeMillis();

                    DebugLog.e(TAG, "prepare time: "+(endCovertMatToJPG - startCovertMatToJPG));

                    this.sendDataCount = this.dataArray.length / bufferSize;
                }
            }
        }

        public void release()
        {
            if(this.imageMat != null)
            {
                this.imageMat.release();
            }

            this.dataArray = null;
        }
    }

    public CameraFrame cameraFrame = new CameraFrame();

    public void paramsModeDetectionFunction(long matAddress){
        DetectionResultJava temopDetectionResult = openCVWrapper.changeParamsModeAndResultValue(matAddress, CURRENT_PROCESS_STEP);

        int tLeftTopX = temopDetectionResult.leftTopX;
        int tLeftTopY = temopDetectionResult.leftTopY;
        int tLeftBottomX = temopDetectionResult.leftBottomX;
        int tLeftBottomY = temopDetectionResult.leftBottomY;

        int tRightTopX = temopDetectionResult.rightTopX;
        int tRightTopY = temopDetectionResult.rightTopY;
        int tRightBottomX = temopDetectionResult.rightBottomX;
        int tRightBottomY = temopDetectionResult.rightBottomY;

        if(isLeftRefernceLine == false)
        {
            recordLeftDefaultParams(0, 0, tLeftTopX,
                    tLeftTopY, tLeftBottomX, tLeftBottomY);
        }

        if(isRightRefernceLine == false)
        {
            recordRightDefaultParams(0, 0, tRightTopX,
                    tRightTopY, tRightBottomX, tRightBottomY);
        }

        openCVWrapper.isLeftRefernceLine = isLeftRefernceLine;
        openCVWrapper.isRightRefernceLine = isRightRefernceLine;

        openCVWrapper.defaultLeftBottomX = defaultLeftBottomX;
        openCVWrapper.defaultLeftBottomY = defaultLeftBottomY;
        openCVWrapper.defaultLeftTopX = defaultLeftTopX;
        openCVWrapper.defaultLeftTopY = defaultLeftTopY;

        openCVWrapper.defaultRightTopX = defaultRightTopX;
        openCVWrapper.defaultRightTopY = defaultRightTopY;
        openCVWrapper.defaultRightBottomX = defaultRightBottomX;
        openCVWrapper.defaultRightBottomY = defaultRightBottomY;

        openCVWrapper.drawChangeParamsModeReferenceLine(matAddress);
    }


    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        Mat resizeImage = new Mat();

        if(mRgba.width() > Constants.videoWidth){
            Size size = new Size(Constants.videoWidth, Constants.videoHeight);
            Imgproc.resize(mRgba, resizeImage, size);
        }else{
            mRgba.copyTo(resizeImage);
        }

//        Log.e(TAG, "resizeImage width: " + resizeImage.width() + " height: " + resizeImage.height());

        if(frameIdentify == 255)
        {
            frameIdentify = 0;
        }

        cameraFrame.setData(resizeImage.clone(), frameIdentify);

        frameIdentify++;

        long matAddress = resizeImage.getNativeObjAddr();

//        Log.e(TAG, "channels: "+resizeImage.channels());

//        Log.e(TAG, "before drawResultJava address: "+matAddress);
//        Log.e(TAG, "resizeImage width: " + resizeImage.cols() + " resizeImage height: " + resizeImage.rows());
//        Log.e(TAG, "MODE: "+MODE);

        if(MODE == MODE_PARAMS)
        {
            paramsModeDetectionFunction(matAddress);
        }
        else
        {
            if(testTask != null)
            {
                if(testTask.getStatus() != AsyncTask.Status.RUNNING)
                {
                    testTask.cancel(true);
                    testTask = null;
                    testTask = new DetectLaneTask();
                    testTask.execute(matAddress);
                }
            }
            else
            {
                testTask = new DetectLaneTask();
                testTask.execute(matAddress);
            }

            //線跟資料可能在JNI畫比較不會有雜訊
            openCVWrapper.rightSlopeValue = rightSlopeValue;
            openCVWrapper.rightIntercpetValue = rightIntercpetValue;
            openCVWrapper.rightTopX = rightTopX;
            openCVWrapper.rightTopY = rightTopY;
            openCVWrapper.rightBottomX = rightBottomX;
            openCVWrapper.rightBottomY = rightBottomY;

            openCVWrapper.leftSlopeValue = leftSlopeValue;
            openCVWrapper.leftIntercpetValue = leftIntercpetValue;
            openCVWrapper.leftTopX = leftTopX;
            openCVWrapper.leftTopY = leftTopY;
            openCVWrapper.leftBottomX = leftBottomX;
            openCVWrapper.leftBottomY = leftBottomY;

            openCVWrapper.isLeftRefernceLine = isLeftRefernceLine;
            openCVWrapper.defaultLeftSlope = defaultLeftSlope;
            openCVWrapper.defaultLeftIntercept = defaultLeftIntercept;
            openCVWrapper.defaultLeftBottomX = defaultLeftBottomX;
            openCVWrapper.defaultLeftBottomY = defaultLeftBottomY;
            openCVWrapper.defaultLeftTopX = defaultLeftTopX;
            openCVWrapper.defaultLeftTopY = defaultLeftTopY;

            openCVWrapper.isRightRefernceLine = isRightRefernceLine;
            openCVWrapper.defaultRightIntercept = defaultRightIntercept;
            openCVWrapper.defaultRightSlope = defaultRightSlope;
            openCVWrapper.defaultRightTopX = defaultRightTopX;
            openCVWrapper.defaultRightTopY = defaultRightTopY;
            openCVWrapper.defaultRightBottomX = defaultRightBottomX;
            openCVWrapper.defaultRightBottomY = defaultRightBottomY;

            openCVWrapper.channel1Value = channel1Value;
            openCVWrapper.afterComputeChannel1 = afterComputeChannel1;
            openCVWrapper.channel2Value = channel2Value;
            openCVWrapper.afterComputeChannel2 = afterComputeChannel2;

            Log.e(TAG, "channel1Value: "+channel1Value);
            Log.e(TAG, "channel2Value: "+channel2Value);

            //以下是將資料畫在畫面上
            openCVWrapper.drawResultJava(resizeImage.getNativeObjAddr());

            if(showValueOnLayout != null){
                DetectionValue leftValue = new DetectionValue();

                leftValue.slopeValue = leftSlopeValue;
                leftValue.interceptValue = leftIntercpetValue;
                leftValue.topX = leftTopX;
                leftValue.topY = leftTopY;
                leftValue.bottomX = leftBottomX;
                leftValue.bottomY = leftBottomY;

                leftValue.defaultSlope = defaultLeftSlope;
                leftValue.defaultIntercept = defaultLeftIntercept;
                leftValue.defaultBottomX = defaultLeftBottomX;
                leftValue.defaultBottomY = defaultLeftBottomY;
                leftValue.defaultTopX = defaultLeftTopX;
                leftValue.defaultTopY = defaultLeftTopY;

                DetectionValue rightValue = new DetectionValue();

                rightValue.slopeValue = rightSlopeValue;
                rightValue.interceptValue = rightIntercpetValue;
                rightValue.topX = rightTopX;
                rightValue.topY = rightTopY;
                rightValue.bottomX = rightBottomX;
                rightValue.bottomY = rightBottomY;

                rightValue.defaultSlope = defaultRightSlope;
                rightValue.defaultIntercept = defaultRightIntercept;
                rightValue.defaultBottomX = defaultRightBottomX;
                rightValue.defaultBottomY = defaultRightBottomY;
                rightValue.defaultTopX = defaultRightTopX;
                rightValue.defaultTopY = defaultRightTopY;

                showValueOnLayout.leftValue(leftValue);
                showValueOnLayout.rightValue(rightValue);
                showValueOnLayout.fpsValue(fps);
                showValueOnLayout.channelsValue(afterComputeChannel1, afterComputeChannel2);
            }

        }

        return resizeImage;
    }

    public ShowValueOnLayout showValueOnLayout = null;

    public interface ShowValueOnLayout{
        public void isSuccessValue(boolean isLeftSuccess, boolean isRightSuccess);
        public void leftValue(DetectionValue detectionValue);
        public void rightValue(DetectionValue detectionValue);
        public void fpsValue(double fpsValue);
        public void channelsValue(int channel1, int channel2);
    }

    private class DetectLaneTask extends AsyncTask<Long, Void, Void>
    {
        public int cancelCount = 0;

        @Override
        protected Void doInBackground(Long... params)
        {
            long startTime = System.currentTimeMillis();

            DetectionResultJava temopDetectionResult = null;

            //if (fps >= 20) {

            int tempLastLeftTopX = lastLeftTopX;
            int tempLastRightTopX = lastRightTopX;

            temopDetectionResult = openCVWrapper.detectionLane(params[0]);

            double tRightSlopeValue = temopDetectionResult.rightSlope;
            double tLeftSlopeValue = temopDetectionResult.leftSlope;
            double tRightIntercpetValue = temopDetectionResult.rightIntercept;
            double tLeftIntercpetValue = temopDetectionResult.leftIntercept;

            int tLeftTopX = temopDetectionResult.leftTopX;
            int tLeftTopY = temopDetectionResult.leftTopY;
            int tLeftBottomX = temopDetectionResult.leftBottomX;
            int tLeftBottomY = temopDetectionResult.leftBottomY;

            int tRightTopX = temopDetectionResult.rightTopX;
            int tRightTopY = temopDetectionResult.rightTopY;
            int tRightBottomX = temopDetectionResult.rightBottomX;
            int tRightBottomY = temopDetectionResult.rightBottomY;


            rightSlopeValue = tRightSlopeValue;
            leftSlopeValue = tLeftSlopeValue;
            rightIntercpetValue = tRightIntercpetValue;
            leftIntercpetValue = tLeftIntercpetValue;

            leftTopX = tLeftTopX;
            leftTopY = tLeftTopY;

            leftBottomX = tLeftBottomX;
            leftBottomY = tLeftBottomY;

            rightTopX = tRightTopX;
            rightTopY = tRightTopY;

            rightBottomX = tRightBottomX;
            rightBottomY = tRightBottomY;

            boolean[] lineSuccessResult = carDirect(tRightSlopeValue, tRightIntercpetValue, tRightTopX,
                    tRightTopY, tRightBottomX, tRightBottomY, tLeftSlopeValue, tLeftIntercpetValue,
                    tLeftTopX, tLeftTopY, tLeftBottomX, tLeftBottomY, tempLastLeftTopX, tempLastRightTopX);

            if(lineSuccessResult != null  && lineSuccessResult.length > 0)
            {
                boolean isLeftSuccess = lineSuccessResult[0];
                boolean isRightSuccess = lineSuccessResult[1];

                if(isRightSuccess)
                {
                    lastRightSlope = tRightSlopeValue;
                    lastRightIntercept = tRightIntercpetValue;
                    lastRightTopX = tRightTopX;
                    lastRightTopY = tRightTopY;
                    lastRightBottomX = tRightBottomX;
                    lastRightBottomY = tRightBottomY;

                    openCVWrapper.rightSlopeTextColorUseNormalColor();
                }
                else
                {
                    openCVWrapper.rightSlopeTextColorUseDangerColor();
                }

                if(isLeftSuccess)
                {
                    lastLeftSlope = tLeftSlopeValue;
                    lastLeftIntercept = tLeftIntercpetValue;
                    lastLeftTopX = tLeftTopX;
                    lastLeftTopY = tLeftTopY;
                    lastLeftBottomX = tLeftBottomX;
                    lastLeftBottomY = tLeftBottomY;

                    openCVWrapper.leftSlopeTextColorUseNormalColor();
                }
                else
                {
                    openCVWrapper.leftSlopeTextColorUsedDangerColor();
                }

                if(showValueOnLayout != null){
                    showValueOnLayout.isSuccessValue(isLeftSuccess, isRightSuccess);
                }
            }


            if(isLeftRefernceLine == false)
            {
                recordLeftDefaultParams(tLeftSlopeValue, tLeftIntercpetValue, tLeftTopX,
                        tLeftTopY, tLeftBottomX, tLeftBottomY);

                lastLeftSlope = tLeftSlopeValue;
                lastLeftIntercept = tLeftIntercpetValue;
                lastLeftTopX = tLeftTopX;
                lastLeftTopY = tLeftTopY;
                lastLeftBottomX = tLeftBottomX;
                lastLeftBottomY = tLeftBottomY;
            }

            if(isRightRefernceLine == false)
            {
                recordRightDefaultParams(tRightSlopeValue, tRightIntercpetValue, tRightTopX,
                        tRightTopY, tRightBottomX, tRightBottomY);

                lastRightSlope = tRightSlopeValue;
                lastRightIntercept = tRightIntercpetValue;
                lastRightTopX = tRightTopX;
                lastRightTopY = tRightTopY;
                lastRightBottomX = tRightBottomX;
                lastRightBottomY = tRightBottomY;
            }



            double endTime = System.currentTimeMillis();
            double timeRange = endTime - startTime;

            fps = 1000 / timeRange;

            openCVWrapper.fps = fps;

            Log.e(TAG, "fps: "+fps+" timeRange: "+timeRange);

            cancel(true);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

//            Log.e(TAG, "this task cancel!!");
        }
    }

    public CameraBridgeViewBase.DrawValue getWrapperDrawValue()
    {
        return cameraWrapperDrawValue;
    }



    private CameraBridgeViewBase.DrawValue cameraWrapperDrawValue = new DrawValue() {
        @Override
        public void drawValue(int marginLeft, int marginRight, int bitmapWidth, int bitmapHeight, Canvas canvas) {

            Paint testPain = new Paint();
            testPain.setColor(Color.WHITE);
            testPain.setAlpha(175);

            float scaleX = bitmapWidth / 640 ;
            float scaleY = bitmapHeight / 360;

            canvas.drawRect(marginLeft, 0, marginRight, 50 * scaleY, testPain);



            Date date = new Date();
            String dateString = "  DATE -> " + date.toString();

            String gpsString = "   GPS -> ";

            String leftValueString = "  LEFT -> M: " + String.format("%.2f", leftSlopeValue) + " (" + String.format("%.2f", defaultLeftSlope) + ")" +
                    ", C: " + String.format("%.2f", leftIntercpetValue) + " ("+defaultLeftIntercept+") " +
                    ", PT1: " + leftTopX + "," + leftTopY + " ("+defaultLeftTopX+","+defaultLeftTopY+")" +
                    ", PT2: " + leftBottomX + "," + leftBottomY + " ("+defaultLeftBottomX+","+defaultLeftBottomY+")";

            String rightValueString = "RIGHT -> M:" + String.format("%.2f", rightSlopeValue) + " (" + String.format("%.2f", defaultRightSlope) + ")" +
                    ", C: " + String.format("%.2f", rightIntercpetValue) + " ("+defaultRightIntercept+") " +
                    ", PT1: " + rightTopX + "," + rightTopY + " ("+defaultRightTopX+","+defaultRightTopY+")" +
                    ", PT2: " + rightBottomX + "," + rightBottomY + " ("+defaultRightBottomX+","+defaultRightBottomY+")";

            String unUseValueString = "FRONT -> N/A  BACK -> N/A";

            canvas.drawRect(marginLeft, 250f * scaleY, marginRight, canvas.getHeight(), testPain);

            canvas.drawText(dateString, 50f * scaleX + marginLeft, 275f * scaleY, paint);

            canvas.drawText(gpsString, 50f * scaleX + marginLeft, 290f * scaleY, paint);

            canvas.drawText(leftValueString, 50f * scaleX + marginLeft, 305f * scaleY, paint);

            canvas.drawText(rightValueString, 50f * scaleX + marginLeft, 320f * scaleY, paint);

            canvas.drawText(unUseValueString, 50f * scaleX + marginLeft, 335f * scaleY, paint);
        }
    };

    public DetectionValue[] getCurrentDetectionValues(){
        DetectionValue[] currentDetectionValue = new DetectionValue[2];

        currentDetectionValue[0] = new DetectionValue();//left
        currentDetectionValue[0].slopeValue = leftSlopeValue;
        currentDetectionValue[0].interceptValue = leftIntercpetValue;
        currentDetectionValue[0].topX = leftTopX;
        currentDetectionValue[0].topY = leftTopY;
        currentDetectionValue[0].bottomX = leftBottomX;
        currentDetectionValue[0].bottomY = leftBottomY;

        currentDetectionValue[0].defaultSlope = defaultLeftSlope;
        currentDetectionValue[0].defaultIntercept = defaultLeftIntercept;
        currentDetectionValue[0].defaultBottomX = defaultLeftTopX;
        currentDetectionValue[0].defaultBottomY = defaultLeftTopY;
        currentDetectionValue[0].defaultTopX = defaultLeftBottomX;
        currentDetectionValue[0].defaultTopY = defaultLeftBottomY;

        currentDetectionValue[1] = new DetectionValue();//right
        currentDetectionValue[1].slopeValue = rightSlopeValue;
        currentDetectionValue[1].interceptValue = rightIntercpetValue;
        currentDetectionValue[1].topX = rightTopX;
        currentDetectionValue[1].topY = rightTopY;
        currentDetectionValue[1].bottomX = rightBottomX;
        currentDetectionValue[1].bottomY = rightBottomY;

        currentDetectionValue[1].defaultSlope = defaultRightSlope;
        currentDetectionValue[1].defaultIntercept = defaultRightIntercept;
        currentDetectionValue[1].defaultBottomX = defaultRightBottomX;
        currentDetectionValue[1].defaultBottomY = defaultRightBottomY;
        currentDetectionValue[1].defaultTopX = defaultRightTopX;
        currentDetectionValue[1].defaultTopY = defaultRightTopY;

        return currentDetectionValue;
    }


    private void recordLeftDefaultParams( double leftSlopeValue, double leftIntercept, int topLeftX,
                                          int topLeftY, int bottomLeftX, int bottomLeftY)
    {
        defaultLeftSlope = leftSlopeValue;
        defaultLeftIntercept = leftIntercept;
        defaultLeftTopX = topLeftX;
        defaultLeftTopY = topLeftY;
        defaultLeftBottomX = bottomLeftX;
        defaultLeftBottomY = bottomLeftY;
    }

    private void recordRightDefaultParams(double rightSlopeValue, double rightIntercept, int topRightX,
                                          int topRightY, int bottomRightX, int bottomRightY)
    {
        defaultRightSlope = rightSlopeValue;
        defaultRightIntercept = rightIntercept;
        defaultRightTopX = topRightX;
        defaultRightTopY = topRightY;
        defaultRightBottomX = bottomRightX;
        defaultRightBottomY = bottomRightY;
    }


    boolean[] carDirect(double rightSlopeValue, double rightIntercept, int topRightX,
                        int topRightY, int bottomRightX, int bottomRightY,
                        double leftSlopeValue, double leftIntercept, int topLeftX,
                        int topLeftY, int bottomLeftX, int bottomLeftY,
                        int carLastLeftTopX, int carLastRightTopX)
    {
        //1. 先判斷有沒有線，沒有線哪來的有沒有效辨識，true表示有線
        boolean[] linesSuccessArray = new boolean[2];

        boolean isGetRightLine = (!Double.isNaN(rightSlopeValue) && !Double.isInfinite(rightSlopeValue) && rightSlopeValue != 0);
        boolean isGetLeftLine = (!Double.isNaN(leftSlopeValue) && !Double.isInfinite(leftSlopeValue) && leftSlopeValue != 0);

        //    printf("carDirect isGetRightLine: %d  \n", isGetRightLine);
        //    printf("carDirect isGetLeftLine: %d  \n", isGetLeftLine);

        if(isGetRightLine && isGetLeftLine && topLeftX > topRightX)
        {
            isGetRightLine = false;
            isGetLeftLine = false;
        }

        if(isLeftRefernceLine == false)
        {
            isGetLeftLine = false;
        }

        if(isRightRefernceLine == false)
        {
            isGetRightLine = false;
        }

        boolean isDetectLeft = false;
        boolean isDetectRight = false;

        //2. 確定有線後，再判定是否有效辨識
        if(isGetRightLine || isGetLeftLine)
        {
            //        isDetectLeft = [self checkLineInEffect:topLeftX lastX:lastLeftTopX];
            //        isDetectRight = [self checkLineInEffect:topRightX lastX:lastRightTopX];

            isDetectLeft = checkLineInEffect(topLeftX, bottomLeftX, carLastLeftTopX);
            isDetectRight = checkLineInEffect(topRightX, bottomRightX, carLastRightTopX);

            if(isGetLeftLine == false)
            {
                isDetectLeft = false;
            }

            if(isGetRightLine == false)
            {
                isDetectRight = false;
            }

            if(isDetectLeft || isDetectRight)
            {
                DETECT_ERROR_COUNTING = 0;

                //當兩條線都會有效的情況
                if(isDetectLeft && isDetectRight)
                {
                    //                double currentOffset = ((topLeftX + topRightX) - (bottomLeftX + bottomRightX)) / 2;
                    defaultOffset = ((defaultLeftTopX + defaultRightTopX)) / 2;
                    double currentOffset = ((topLeftX + topRightX)) / 2;
                    double value = ((currentOffset - defaultOffset) * MAX_LEFT_RIGHT_POWER * 2);
                    channel1Value = value / Constants.videoWidth;
                }
                else if(isDetectLeft && defaultLeftTopX != 0)//左邊 線 有效的情況
                {
                    double currentOffset = ((topLeftX - defaultLeftTopX));
                    //                double value = ((currentOffset) * MAX_LEFT_RIGHT_POWER * 2) // Louis說拿掉*2  2017/06/30;
                    double value = ((currentOffset) * MAX_LEFT_RIGHT_POWER);
                    channel1Value = value / Constants.videoWidth;

                    // 原本的演算法
                    //channel1Value = ((defaultLeftSlope - leftSlopeValue) / defaultLeftSlope);
                    //channel1Value = channel1Value + lastTurnValue;
                }
                else if(isDetectRight && defaultRightTopX != 0)//右邊 線 有效的情況
                {

                    double currentOffset = ((topRightX - defaultRightTopX));
                    //                double value = ((currentOffset) * MAX_LEFT_RIGHT_POWER * 2);// Louis說拿掉*2  2017/06/30;
                    double value = ((currentOffset) * MAX_LEFT_RIGHT_POWER);
                    channel1Value = value / Constants.videoWidth;

                    //會有壘加的情況發生，應該要有個限制，要不然車子會無法前進
                    // channel1Value = ((rightSlopeValue - defaultRightSlope) / defaultRightSlope);
                    // channel1Value = channel1Value + lastTurnValue;
                } else {


                }

                if(Double.isNaN(channel1Value) == false && Double.isInfinite(channel1Value) == false)
                {
                    if(channel1Value > MAX_LEFT_RIGHT_POWER)
                    {
                        channel1Value = MAX_LEFT_RIGHT_POWER;
                    }
                    else if(channel1Value < 0 && channel1Value < -MAX_LEFT_RIGHT_POWER)
                    {
                        channel1Value = -MAX_LEFT_RIGHT_POWER;
                    }

                    lastTurnValue = channel1Value;
                }

                channel2Value = getChannel2Value(channel1Value);

//                Log.e(TAG, "carDirect last Turn Value: "+lastTurnValue);
//                Log.e(TAG, "carDirect channel 1 Value: "+channel1Value);
//                Log.e(TAG, "carDirect channel 2 Value: "+channel2Value);

                UN_DETECTION_LEFT_RIGHT_POWER = (int)(lastTurnValue);
            }
            else
            {
                DETECT_ERROR_COUNTING ++;

                if(DETECT_ERROR_COUNTING >= DETECTION_ERROR_STOP)
                {
                    DETECT_ERROR_COUNTING = DETECTION_ERROR_STOP;

                    channel1Value = UN_DETECTION_LEFT_RIGHT_POWER;
                    channel2Value = UN_DETECTION_FORWARD_POWER;
                }
            }
        }
        else
        {
            DETECT_ERROR_COUNTING ++;

            if(DETECT_ERROR_COUNTING >= DETECTION_ERROR_STOP)
            {
                DETECT_ERROR_COUNTING = DETECTION_ERROR_STOP;

                channel1Value = UN_DETECTION_LEFT_RIGHT_POWER;
                channel2Value = UN_DETECTION_FORWARD_POWER;
            }
        }

        if(isActive == false)
        {
            channel1Value = 0;
            channel2Value = 0;
        }

        //計算channel1 , channel2 給Servo的數值
        //方向
        channels[0].calculateResult((int)(channel1Value * 10));
        //動力
        channels[1].calculateResult((int)(channel2Value * 10));

        int afterComputeChannel1 = (int)(channels[0].getChannelValue());
        int afterComputeChannel2 = (int)(channels[1].getChannelValue());

        bleController.setChannel1(afterComputeChannel1);
        bleController.setChannel2(afterComputeChannel2);

        this.afterComputeChannel1 = afterComputeChannel1;
        this.afterComputeChannel2 = afterComputeChannel2;

//        Log.e(TAG, "carDirect isDetectLeft: "+isDetectLeft);
//        Log.e(TAG, "carDirect isDetectRight: "+isDetectRight);

        linesSuccessArray[0] = isDetectLeft;
        linesSuccessArray[1] = isDetectRight;

        return linesSuccessArray;
    }

    boolean checkLineInEffect(int currentTopX, int currentBottomX, int lastX)
    {
        if(currentTopX < 0 || currentTopX > Constants.videoWidth){
            return false;
        }
        //    else if(currentBottomX < 0 || currentBottomX > _videoWidth){
        //        return false;
        //    }

        //    if(currentTopX == 0 && lastX == 0)//把lastX拿掉 問題出在這
        //    {
        //        return false;
        //    }

        if(currentTopX == 0)
        {
            return false;
        }

        int range = Math.abs(currentTopX - lastX);
        int errorRange = (int)((double)(Constants.videoWidth) * DETECT_ERROR_RANGE);

        if(range < errorRange)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    //0.3 , 0.5 , 0.6 , 代表意義為何，不記得了。
    //
    private  double getChannel2Value(double channel1Value)
    {
        double percent = Math.abs(channel1Value);

        if(percent <= (MAX_FORWARD_POWER * 0.3))
        {
            return MAX_FORWARD_POWER;
        }
        else if(percent >= (MAX_FORWARD_POWER * 0.3) && percent <= (MAX_FORWARD_POWER * 0.6))
        {
            return MAX_FORWARD_POWER * 0.5;
        }
        else //if(percent > 60)
        {
            return MAX_FORWARD_POWER * 0.2;//2018/7/3 Louis說先改成這樣(原本是 0), 原因為何？沒問。
        }
    }
}

//        預設 左右最大出力值 （max： 100）
//        預設 動力最大出力 （max： 100）
//        預期 煞車最大出力（max： 100）
//        預設 無辨識狀況左右出力、 動力出力 （max： 100）
//
//        預設: 左線參考物件
//        斜率、截距、上面的點座標， 下面點座標
//
//        預設: 右邊參考物件
//        斜率、截距、上面的點座標， 下面點座標
//
//        以上參考物件可以在按下開始按鈕後取得數據）
//
//        ————
//
//        預設偏差數值, 計算公式為 （上放X座標相加 - 下方x座標相加)/2。
//        例如: （(左上座標X + 右上座標）- （左下座標X- 右下座標X)) / 2
//
//        演算法：
//
//        1. 檢查是否為有效辨識： 當有辨識到兩邊時，若與最後一次的上方數值 (例如： 左上方X  與 最後一次有效的辨識的左上方X ) 比較大於畫面寬度的50%， 則當作辨識失敗。 右邊也是相同處理。
//
//        2. 如果本次左右兩邊有成功辨識，則
//
//        a. 計算偏差值:
//        （上放X座標相加 - 下方x座標相加)/2。
//        例如 （（左上座標X + 右上座標）- （左下座標X- 右下座標X)) / 2
//
//        b. （（本次偏差值 - 預設偏差數值）* 左右最大出力* 2 ）/ 畫面的寬度 =
//        就是左右出力數值
//
//        3. 如果只有成功辨識到左邊，則以（預設左邊斜率 - 本次左邊斜率）* 最大左右出力 /  預設斜率
//
//        4. 如果只有成功辨識到右邊，則以（本次右邊斜率-右邊預設斜率）* 最大左右出力 /  預設斜率
//
//        5. 如果兩邊界無法辨識且累積次數超過50次，則停止辨識，動力回到無辨識狀況出力
//
//        6. 只要有成功辨識1條線以上，累積無辨識次數歸零。
//
//
//        補充一下，左右輸出的數值在最大值的30%, 則動力出力為預設出力的100%， 左右數值的30-60%,則動力出力為50%， 以上刞動力輸出為0％
