package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;
import com.v7idea.Activity.BaseActivity;
import com.v7idea.DataBase.DataBase;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.template.View.CustomView_SeekBar;
import com.v7idea.tool.CVCameraWrapper;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.ParamsFilter;
import com.v7idea.tool.UsbCameraFrame;
import com.v7idea.tool.ViewScaling;

import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

public class ChangeDetectionParamsPage extends BaseActivity {

    private static final String TAG = "ChangeDetectionParamsPage";
    private int Page = 1;

    private DataBase dataBase = null;
    private SimpleDatabase simpleDatabase = null;
    private ParamsFilter paramsFilter = null;
    private CustomerSetting settingData = null;

    private Air thisApp = null;

    private CVCameraWrapper mOpenCvCameraView;
    private RelativeLayout PageAll = null;
    private LinearLayout Page1, Page2, Page3, Page4, Page5;
    private CustomView_SeekBar Kernel, AutoCanny, Rho, Theta, Threshold, MinLineLength, MaxLinrGap, CheckPositiveSlopes, CheckNegitiveSlopes, ForwardPower, LeftAndRight, DetectionError;
    private Button Button_Left, Button_Right;
    private Button Button_Clear, Button_Ok, Button_Paint;
    private Button Button_Save, Button_Load, Button_Servo, Button_Back;
    private Button ButtonScanDevice = null;
    private Button ButtonSendParameters = null;

    private TextView LeftTopPoint = null;
    private TextView LeftBottomPoint = null;
    private TextView RightTopPoint = null;
    private TextView RightBottomPoint = null;

    private ImageView touchPannel = null;

    private Button leftReferenceLine = null;
    private Button rightReferentLine = null;

    private Boolean LBoolean = false, RBoolean = false;

    private float ROIscaleValueX = 0.0f;
    private float ROIscaleValueY = 0.0f;

    private float pointPressedAlpha = 0.6f;
    private float pointUnPressedAlpha = 0.8f;

    /**
     * 0:車子模式，1:搖控模式（又稱 Command模式）
     */
    private int AppMode = 0;

    private boolean isStartDetectionImage = false;

    private Thread detectionImageThread = null;

    private UsbCameraFrame usbCameraFrame = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isHideStatusBar(true);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_detection_params_page);

        thisApp = (Air) getApplication();

        ViewScaling.setScaleValue(this);

        ROIscaleValueX = (float) Constants.videoWidth / (float) ViewScaling.getScreenWidth();
        ROIscaleValueY = (float) Constants.videoHeight / (float) ViewScaling.getScreenHeight();

        mOpenCvCameraView = (CVCameraWrapper) findViewById(R.id.ChangeDetectionParams_CVCameraWrapper_SurfaceView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(Constants.videoWidth * 2, Constants.videoHeight * 2);
        mOpenCvCameraView.MODE = CVCameraWrapper.MODE_PARAMS;
        mOpenCvCameraView.setCvCameraViewListener(mOpenCvCameraView);

        touchPannel = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.ChangeDetectionParams_ImageView_touchPannel);


        Page1 = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.Page1);
        Page2 = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.Page2);
        Page3 = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.Page3);
        Page4 = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.Page4);
        Page5 = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.Page5);
        PageAll = (RelativeLayout) ViewScaling.findViewByIdAndScale(this, R.id.PageAll);

        Button_Left = (Button) ViewScaling.findViewByIdAndScale(this, R.id.Button_Left);
        Button_Right = (Button) ViewScaling.findViewByIdAndScale(this, R.id.Button_Right);
        Button_Paint = (Button) ViewScaling.findViewByIdAndScale(this, R.id.Button_Paint);
        Button_Ok = (Button) ViewScaling.findViewByIdAndScale(this, R.id.Button_Ok);
        Button_Clear = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_Clear);
        Button_Save = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_Save);
        Button_Load = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_Load);
        Button_Servo = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_Servo);
        Button_Back = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_Back);
        ButtonScanDevice = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_ScaneDevice);
        ButtonSendParameters = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_SendParameters);

        leftReferenceLine = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_leftReferenceLine);
        rightReferentLine = (Button) ViewScaling.findViewByIdAndScale(this,R.id.Button_rightReferentLine);

        LeftTopPoint = (TextView) ViewScaling.findViewByIdAndScale(this,R.id.customView1);
        LeftTopPoint.setAlpha(pointUnPressedAlpha);

        LeftBottomPoint = (TextView) ViewScaling.findViewByIdAndScale(this,R.id.customView2);
        LeftBottomPoint.setAlpha(pointUnPressedAlpha);

        RightTopPoint = (TextView) ViewScaling.findViewByIdAndScale(this,R.id.customView3);
        RightTopPoint.setAlpha(pointUnPressedAlpha);

        RightBottomPoint = (TextView) ViewScaling.findViewByIdAndScale(this,R.id.customView4);
        RightBottomPoint.setAlpha(pointUnPressedAlpha);

        Button_Left.setOnClickListener(click);
        Button_Right.setOnClickListener(click);
        Button_Paint.setOnClickListener(click);
        Button_Ok.setOnClickListener(click);
        Button_Clear.setOnClickListener(click);
        Button_Save.setOnClickListener(click);
        Button_Load.setOnClickListener(click);
        Button_Servo.setOnClickListener(click);
        Button_Back.setOnClickListener(click);
        ButtonScanDevice.setOnClickListener(click);
        ButtonSendParameters.setOnTouchListener(onTouchSendParameter);

        leftReferenceLine.setOnClickListener(click);
        rightReferentLine.setOnClickListener(click);

        dataBase = new DataBase(this);
        dataBase.Open();

        simpleDatabase = new SimpleDatabase();
        paramsFilter = new ParamsFilter();

        init();

        if(getIntent() != null){
            AppMode = getIntent().getIntExtra(Constants.APP_MODE, Constants.CAR_MODE);

            if(AppMode == Constants.COMMAND_MODE)
            {
                Button_Servo.setVisibility(View.INVISIBLE);
                ButtonScanDevice.setVisibility(View.INVISIBLE);

                leftReferenceLine.setVisibility(View.VISIBLE);
                rightReferentLine.setVisibility(View.VISIBLE);
                ButtonSendParameters.setVisibility(View.VISIBLE);

                if(thisApp.client == null){
                    thisApp.client = new Client();
                }

                thisApp.client.setIpAddress(thisApp.targetIP);
                thisApp.client.setPort(Constants.DEFAULT_PORT);
//                client.setHandler(handler);
                thisApp.client.showImage = touchPannel;
            }
            else if(AppMode == Constants.WEBCAM_MODE){
                if(thisApp.mUVCCamera != null){
                    thisApp.mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);
                }
            }
        }
    }

    public void init() {
        Page1();
        Page2();
        Page3();
        Page4();
        Page5();
    }

    private void PageCh() {
//        Toast.makeText(this, "Page：" + Page + "，LBoolean：" + LBoolean + "，RBoolean：" + RBoolean, Toast.LENGTH_SHORT).show();
        Animation amL1 = new TranslateAnimation(0.0f, -(float)(ViewScaling.getScreenWidth()), 0.0f, 0.0f);//從左消失
        Animation amL2 = new TranslateAnimation((float)(ViewScaling.getScreenWidth()), 0.0f, 0.0f, 0.0f);//從右出現

        Animation amR1 = new TranslateAnimation(0.0f, (float)(ViewScaling.getScreenWidth()), 0.0f, 0.0f);//從左出現
        Animation amR2 = new TranslateAnimation(-(float)(ViewScaling.getScreenWidth()), 0.0f, 0.0f, 0.0f);//從右消失

        amL1.setDuration(500);
        amL1.setRepeatCount(0);
        amL2.setDuration(500);
        amL2.setRepeatCount(0);
        amR1.setDuration(500);
        amR1.setRepeatCount(0);
        amR2.setDuration(500);
        amR2.setRepeatCount(0);

        switch (Page) {
            case 1:

                if (LBoolean && !RBoolean) {
                    Page1.setVisibility(View.GONE);
                    Page1.startAnimation(amL1);
                    Page2.setVisibility(View.VISIBLE);
                    Page2.startAnimation(amL2);
                    Page = 2;
                    break;
                }
                if (RBoolean && !LBoolean) {
                    Page1.setVisibility(View.GONE);
                    Page1.startAnimation(amR1);
                    Page5.setVisibility(View.VISIBLE);
                    Page5.startAnimation(amR2);
                    Page = 5;
                    break;
                }

            case 2:

                if (LBoolean && !RBoolean) {
                    Page2.setVisibility(View.GONE);
                    Page2.startAnimation(amL1);
                    Page3.setVisibility(View.VISIBLE);
                    Page3.startAnimation(amL2);
                    Page = 3;
                    break;
                }
                if (RBoolean && !LBoolean) {
                    Page2.setVisibility(View.GONE);
                    Page2.startAnimation(amR1);
                    Page1.setVisibility(View.VISIBLE);
                    Page1.startAnimation(amR2);
                    Page = 1;
                    break;
                }

            case 3:

                if (LBoolean && !RBoolean) {
                    Page3.setVisibility(View.GONE);
                    Page3.startAnimation(amL1);
                    Page4.setVisibility(View.VISIBLE);
                    Page4.startAnimation(amL2);
                    Page = 4;
                    break;
                }
                if (RBoolean && !LBoolean) {
                    Page3.setVisibility(View.GONE);
                    Page3.startAnimation(amR1);
                    Page2.setVisibility(View.VISIBLE);
                    Page2.startAnimation(amR2);
                    Page = 2;
                    break;
                }
            case 4:

                if (LBoolean && !RBoolean) {
                    Page4.setVisibility(View.GONE);
                    Page4.startAnimation(amL1);
                    Page5.setVisibility(View.VISIBLE);
                    Page5.startAnimation(amL2);
                    Page = 5;
                    break;
                }
                if (RBoolean && !LBoolean) {
                    Page4.setVisibility(View.GONE);
                    Page4.startAnimation(amR1);
                    Page3.setVisibility(View.VISIBLE);
                    Page3.startAnimation(amR2);
                    Page = 3;
                    break;
                }
            case 5:

                if (LBoolean && !RBoolean) {
                    Page5.setVisibility(View.GONE);
                    Page5.startAnimation(amL1);
                    Page1.setVisibility(View.VISIBLE);
                    Page1.startAnimation(amL2);
                    Page = 1;
                    break;
                }
                if (RBoolean && !LBoolean) {
                    Page5.setVisibility(View.GONE);
                    Page5.startAnimation(amR1);
                    Page4.setVisibility(View.VISIBLE);
                    Page4.startAnimation(amR2);
                    Page = 4;
                    break;
                }

        }
        RBoolean = false;
        LBoolean = false;
    }

    private void Page1() {

        Kernel = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.Kernel);
        Kernel.scaleLayout();
        Kernel.seekBar.setTag(R.id.Kernel);
        Kernel.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        Kernel.setText("高斯模糊");
        Kernel.setMax(100);


        AutoCanny = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.AutoCanny);
        AutoCanny.scaleLayout();
        AutoCanny.seekBar.setTag(R.id.AutoCanny);
        AutoCanny.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        AutoCanny.setText("AutoCanny係數");
        AutoCanny.setMax(500);

    }

    private void Page2() {

        Rho = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.Rho);
        Rho.scaleLayout();
        Rho.seekBar.setTag(R.id.Rho);
        Rho.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        Rho.setText("RHO");
        Rho.setMax(100);


        Theta = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.Theta);
        Theta.scaleLayout();
        Theta.seekBar.setTag(R.id.Theta);
        Theta.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        Theta.setText("THETA");
        Theta.setMax(100);


        Threshold = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.Threshold);
        Threshold.scaleLayout();
        Threshold.seekBar.setTag(R.id.Threshold);
        Threshold.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        Threshold.setText("THRESHOLD");
        Threshold.setMax(100);


    }


    private void Page3() {

        MinLineLength = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.MinLineLength);
        MinLineLength.scaleLayout();
        MinLineLength.seekBar.setTag(R.id.MinLineLength);
        MinLineLength.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        MinLineLength.setText("minLineLength");
        MinLineLength.setMax(100);


        MaxLinrGap = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.MaxLinrGap);
        MaxLinrGap.scaleLayout();
        MaxLinrGap.seekBar.setTag(R.id.MaxLinrGap);
        MaxLinrGap.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        MaxLinrGap.setText("maxLinrGap");
        MaxLinrGap.setMax(100);

    }

    private void Page4() {
        CheckPositiveSlopes = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.CheckPositiveSlopes);
        CheckPositiveSlopes.scaleLayout();
        CheckPositiveSlopes.seekBar.setTag(R.id.CheckPositiveSlopes);
        CheckPositiveSlopes.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        CheckPositiveSlopes.setText("CheckPosSlopes");
        CheckPositiveSlopes.setMax(100);

        CheckNegitiveSlopes = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.CheckNegitiveSlopes);
        CheckNegitiveSlopes.scaleLayout();
        CheckNegitiveSlopes.seekBar.setTag(R.id.CheckNegitiveSlopes);
        CheckNegitiveSlopes.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        CheckNegitiveSlopes.setText("CheckNegSlopes");
        CheckNegitiveSlopes.setMax(100);

    }

    private void Page5() {
        DetectionError = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.DetectionError);
        DetectionError.scaleLayout();
        DetectionError.seekBar.setTag(R.id.DetectionError);
        DetectionError.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        DetectionError.setText("連續錯誤停止次數");
        DetectionError.setMax(100);

        ForwardPower = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.ForwardPower);
        ForwardPower.scaleLayout();
        ForwardPower.seekBar.setTag(R.id.ForwardPower);
        ForwardPower.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        ForwardPower.setText("ForwardPower");
        ForwardPower.setMax(100);


        LeftAndRight = (CustomView_SeekBar) ViewScaling.findViewByIdAndScale(this,R.id.LeftAndRight);
        LeftAndRight.scaleLayout();
        LeftAndRight.seekBar.setTag(R.id.LeftAndRight);
        LeftAndRight.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        LeftAndRight.setText("LeftAndRight");
        LeftAndRight.setMax(100);


    }

    private int changeValueTo0To100(float value, float maxValue, SeekBar seekBar)
    {
        if(seekBar != null)
        {
            float percentValue = value / maxValue;

            float max = seekBar.getMax();

            return (int)(max * percentValue);
        }

        return 0;
    }

    private void loadData(CustomerSetting customerSetting)
    {
        float rioLeftTopX = (float) customerSetting.getROILeftUpX();
        float rioLeftTopY = (float) customerSetting.getROILeftUpY();

        float rioRightTopX = (float) customerSetting.getROIRightUpX();
        float rioRightTopY = (float) customerSetting.getROIRightUpY();

        float rioLeftDownX = (float) customerSetting.getROILeftDownX();
        float rioLeftDownY = (float) customerSetting.getROILeftDownY();

        float rioRightDownX = (float) customerSetting.getROIRightDownX();
        float rioRightDownY = (float) customerSetting.getROIRightDownY();

        ViewScaling.changeLeftMarginAndTopMargin(LeftTopPoint, (int)((rioLeftTopX / (float) Constants.videoWidth) * (float) ViewScaling.getScreenWidth()) - (LeftTopPoint.getLayoutParams().width / 2)
                , (int)((rioLeftTopY / (float) Constants.videoHeight) * (float) ViewScaling.getScreenHeight()) - (LeftTopPoint.getLayoutParams().height / 2));
        LeftTopPoint.setBackgroundColor(Color.GREEN);

        ViewScaling.changeLeftMarginAndTopMargin(RightTopPoint, (int)((rioRightTopX / Constants.videoWidth) * ViewScaling.getScreenWidth()) - (RightTopPoint.getLayoutParams().width / 2)
                , (int)((rioRightTopY / Constants.videoHeight) * ViewScaling.getScreenHeight()) - (RightTopPoint.getLayoutParams().height / 2));
        RightTopPoint.setBackgroundColor(Color.YELLOW);

        ViewScaling.changeLeftMarginAndTopMargin(LeftBottomPoint, (int)((rioLeftDownX / Constants.videoWidth) * ViewScaling.getScreenWidth()) - (LeftTopPoint.getLayoutParams().width / 2)
                , (int)((rioLeftDownY / Constants.videoHeight) * ViewScaling.getScreenHeight()) - (LeftTopPoint.getLayoutParams().height / 2));
        LeftBottomPoint.setBackgroundColor(Color.RED);

        ViewScaling.changeLeftMarginAndTopMargin(RightBottomPoint, (int)((rioRightDownX / Constants.videoWidth) * ViewScaling.getScreenWidth()) - (RightBottomPoint.getLayoutParams().width / 2)
                , (int)((rioRightDownY / Constants.videoHeight) * ViewScaling.getScreenHeight()) - (RightBottomPoint.getLayoutParams().height / 2));
        RightBottomPoint.setBackgroundColor(Color.BLUE);


        mOpenCvCameraView.getOpenCVWrapper().setROILeftUpX((int)rioLeftTopX);
        mOpenCvCameraView.getOpenCVWrapper().setROILeftUpY((int)rioLeftTopY);
        mOpenCvCameraView.getOpenCVWrapper().setROIRightUpX((int)rioRightTopX);
        mOpenCvCameraView.getOpenCVWrapper().setROIRightUpY((int)rioRightTopY);
        mOpenCvCameraView.getOpenCVWrapper().setROILeftDownX((int)rioLeftDownX);
        mOpenCvCameraView.getOpenCVWrapper().setROILeftDownY((int)rioLeftDownY);
        mOpenCvCameraView.getOpenCVWrapper().setROIRightDownX((int)rioRightDownX);
        mOpenCvCameraView.getOpenCVWrapper().setROIRightDownY((int)rioRightDownY);

        int maxForwardPowerValue = customerSetting.getMaxForwardPower();
        int maxLeftRightPowerValue = customerSetting.getMaxLeftRightPower();

        double rightSlopesValue = customerSetting.getCheckPosSlopes();
        double leftSlopesValue = customerSetting.getCheckNegSlopes();
        int gausianChangeValue = customerSetting.getGausianKernelValue();
        double autoCannyValue = customerSetting.getAutoCannyValue();
        double rhoValue = customerSetting.getRhoValue();
        int thetaValue = customerSetting.getThetaValue();
        int thresholdValue = customerSetting.getTheresHoldValue();
        int minLineLengthValue = customerSetting.getMinLineLen();
        int maxLineGapValue = customerSetting.getMaxLineGap();
        int detectionErrorStopValue = customerSetting.getDetectionErrorStop();

        int gausianProgressValue = changeValueTo0To100(Float.valueOf(gausianChangeValue), 100, Kernel.seekBar);
        Kernel.showValue.setText("" + gausianChangeValue);
        Kernel.seekBar.setProgress(Integer.valueOf(gausianProgressValue));

//        int autoCannyProgressValue = (int)(autoCannyValue);
        AutoCanny.showValue.setText("" + autoCannyValue);
        AutoCanny.seekBar.setProgress((int)(autoCannyValue * 100));

        int rhoProgressValue = changeValueTo0To100((float)(rhoValue), 100, Rho.seekBar);
        Rho.showValue.setText("" + rhoValue);
        Rho.seekBar.setProgress(rhoProgressValue);

        int thetaProgressValue = changeValueTo0To100(Float.valueOf(thetaValue), 100, Theta.seekBar);
        Theta.showValue.setText("" + thetaValue);
        Theta.seekBar.setProgress(thetaProgressValue);

        int thresholdProgressValue = changeValueTo0To100(Float.valueOf(thresholdValue), 100, Threshold.seekBar);
        Threshold.showValue.setText("" + thresholdValue);
        Threshold.seekBar.setProgress(thresholdProgressValue);

        int minLineLengthProgressValue = changeValueTo0To100(Float.valueOf(minLineLengthValue), 100, MinLineLength.seekBar);
        MinLineLength.showValue.setText("" + minLineLengthValue);
        MinLineLength.seekBar.setProgress(minLineLengthProgressValue);

        int maxLineGapProgressValue = changeValueTo0To100((float)(maxLineGapValue), 100, MaxLinrGap.seekBar);
        MaxLinrGap.showValue.setText("" + maxLineGapValue);
        MaxLinrGap.seekBar.setProgress(maxLineGapProgressValue);

        int rightSlopesProgressValue = changeValueTo0To100((float)(rightSlopesValue), 10, CheckPositiveSlopes.seekBar);
        CheckPositiveSlopes.showValue.setText("" + rightSlopesValue);
        CheckPositiveSlopes.seekBar.setProgress(rightSlopesProgressValue);

        int leftSlopesProgressValue = changeValueTo0To100((float)(-leftSlopesValue), 10, CheckNegitiveSlopes.seekBar);
        CheckNegitiveSlopes.showValue.setText("" + leftSlopesValue);
        CheckNegitiveSlopes.seekBar.setProgress(leftSlopesProgressValue);

        LeftAndRight.showValue.setText("" + maxLeftRightPowerValue);
        LeftAndRight.seekBar.setProgress(maxLeftRightPowerValue);

        DetectionError.showValue.setText("" + detectionErrorStopValue);
        DetectionError.seekBar.setProgress(detectionErrorStopValue);

        ForwardPower.showValue.setText("" + maxForwardPowerValue);
        ForwardPower.seekBar.setProgress(maxForwardPowerValue);

//        inputPower.setText("" + maxForwardPowerValue);
//        inputTurnArea.setText("" + maxLeftRightPowerValue);
    }

    private void loadParamsAndDetect()
    {
//        saveData();


        CustomerSetting customerSetting = paramsFilter.getCurrentSetting(simpleDatabase);
        mOpenCvCameraView.setDetectParams(customerSetting);

        Log.d(TAG, "DONE !!");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null && AppMode == 0)
            mOpenCvCameraView.disableView();

        if(AppMode == Constants.COMMAND_MODE){
            if(thisApp.client != null)
            {
                thisApp.client.setStartStream(false);
                thisApp.client.stopSendMessage();
                thisApp.client.release();

                isStartDetectionImage = false;

                if(detectionImageThread != null){
                    detectionImageThread.interrupt();
                }

                detectionImageThread = null;
            }
        }
        else if(AppMode == Constants.WEBCAM_MODE){
            isStartDetectionImage = false;

            if(detectionImageThread != null){
                detectionImageThread.interrupt();
            }

            detectionImageThread = null;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        settingData = paramsFilter.getCurrentSetting(simpleDatabase);
        loadData(settingData);
        mOpenCvCameraView.setDetectParams(settingData);
        mOpenCvCameraView.CURRENT_PROCESS_STEP = 9;

        if(AppMode == Constants.CAR_MODE)
        {
            mOpenCvCameraView.enableView();
        }
        else if(AppMode == Constants.WEBCAM_MODE){
            if(detectionImageThread == null){
                detectionImageThread = new Thread(TestRunnable);
                isStartDetectionImage = true;
                detectionImageThread.start();
            }
        }
        else if(AppMode == Constants.COMMAND_MODE)
        {
            thisApp.client.init();
            thisApp.client.startSendMessage();
            thisApp.client.setStartStream(true);

            if(detectionImageThread == null){
                detectionImageThread = new Thread(reDetectionImageRunnable);
            }

            isStartDetectionImage = true;
            detectionImageThread.start();
        }
    }

    private Runnable reDetectionImageRunnable = new Runnable() {
        @Override
        public void run() {

            while (isStartDetectionImage)
            {
                if(thisApp.client.receiveBitmap != null)
                {
                    Mat frameMat = new Mat();
                    Utils.bitmapToMat(thisApp.client.receiveBitmap, frameMat);

                    mOpenCvCameraView.paramsModeDetectionFunction(frameMat.getNativeObjAddr());

                    final Bitmap showBitmap = Bitmap.createBitmap(frameMat.width(), frameMat.height(), Bitmap.Config.RGB_565);

                    Utils.matToBitmap(frameMat, showBitmap);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            touchPannel.setImageBitmap(showBitmap);
                        }
                    });
                }
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();

        if(dataBase != null)
        {
            dataBase.Close();
        }
    }

    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            frame.clear();

            if(usbCameraFrame == null){
                usbCameraFrame = new UsbCameraFrame();
            }

            synchronized (usbCameraFrame) {
                usbCameraFrame.setByteBuffer(frame);
            }

//			mImageView.post(TestRunnable);
        }
    };

    private final Runnable TestRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStartDetectionImage){
                if (usbCameraFrame != null && usbCameraFrame.rgba() != null){

                    Mat rgba = usbCameraFrame.rgba();

                    int width = rgba.width();
                    int height = rgba.height();

                    Log.e(TAG, "width: "+width+"  height: "+height);

                    if(width > 0 && height > 0){

                        UsbCameraFrame detectionFrame = usbCameraFrame.copy();

                        Mat afterDetectionMat = mOpenCvCameraView.onCameraFrame(detectionFrame);

                        final Bitmap bitmap = Bitmap.createBitmap(afterDetectionMat.width(), afterDetectionMat.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(afterDetectionMat, bitmap);

                        touchPannel.post(new Runnable() {
                            @Override
                            public void run() {
                                touchPannel.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            }
        }
    };

    private View.OnTouchListener onTouchSendParameter = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:{

                    JSONObject currentDetectionParameter = paramsFilter.getCurrentDetectionJSON(simpleDatabase);

                    if(currentDetectionParameter != null && currentDetectionParameter.length() > 0)
                    {
                        thisApp.client.currentDetectionParameter = currentDetectionParameter;
                    }
                }break;

                case MotionEvent.ACTION_UP:{
                    thisApp.client.currentDetectionParameter = null;
                }break;
            }

            return true;
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            float currentProgress = (float) (progress) / (float)(seekBar.getMax());

            if(seekBar.getTag() == null || fromUser == false)
            {
                return;
            }

            int tagID = (int)seekBar.getTag();

            switch (tagID)
            {
                case  R.id.Kernel:

                    Log.e(TAG, "Kernel percent: "+currentProgress);

                    int saveKernelValue = (int)(currentProgress * 100);

                    Log.e(TAG, "Kernel percent: "+saveKernelValue);

                    if(saveKernelValue % 2 == 0)
                    {
                        saveKernelValue += 1;
                        seekBar.setProgress(saveKernelValue);
                    }

                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 0;
                    Kernel.showValue.setText((""+saveKernelValue));
                    simpleDatabase.setValueByKey(Constants.Gausian_Last_Value_Int, (""+saveKernelValue));

                    break;

                case  R.id.AutoCanny:

                    float cannyProgress = (float) (progress) / 100f;

                    if(cannyProgress <= 0.1f)
                    {
                        cannyProgress = 0.1f;
                    }

                    Log.e(TAG, "AutoCanny percent: "+cannyProgress);

                    Log.e(TAG, "AutoCanny saveAutoCannyValue: "+cannyProgress);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 1;
                    AutoCanny.showValue.setText((""+cannyProgress));
                    simpleDatabase.setValueByKey(Constants.AutoCanny_Last_Value_Float, (""+cannyProgress));

                    break;

                case  R.id.Rho:
                    Log.e(TAG, "Rho percent: "+currentProgress);

                    float saveRhoValue = currentProgress * 100f;

                    if(saveRhoValue <= 1f)
                    {
                        saveRhoValue = 1f;
                    }

                    Log.e(TAG, "Rho value: "+saveRhoValue);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 2;
                    Rho.showValue.setText((""+saveRhoValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_RHO_Last_Value_Double, (""+saveRhoValue));

                    break;

                case  R.id.Theta:
                    Log.e(TAG, "Theta percent: "+currentProgress);

                    int saveThetaValue = (int)(currentProgress * 100);

                    if(saveThetaValue <= 1)
                    {
                        saveThetaValue = 1;
                    }

                    Log.e(TAG, "Theta value: "+saveThetaValue);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 3;
                    Theta.showValue.setText((""+saveThetaValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_THETA_Last_Value_Int, (""+saveThetaValue));
                    break;

                case  R.id.Threshold:
                    Log.e(TAG, "Threshold percent: "+currentProgress);

                    int saveThresholdValue = (int)(currentProgress * 100);

                    if(saveThresholdValue <= 1)
                    {
                        saveThresholdValue = 1;
                    }

                    Log.e(TAG, "Threshold value: "+saveThresholdValue);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 4;
                    Threshold.showValue.setText((""+saveThresholdValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_Threshold_Last_Value_Int, (""+saveThresholdValue));
                    break;

                case  R.id.MinLineLength:
                    Log.e(TAG, "MinLineLength percent: "+currentProgress);

                    int saveMinLineLengthValue = (int)(currentProgress * 100);

                    if(saveMinLineLengthValue <= 1)
                    {
                        saveMinLineLengthValue = 1;
                    }

                    Log.e(TAG, "MinLineLength value: "+saveMinLineLengthValue);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 5;
                    MinLineLength.showValue.setText((""+saveMinLineLengthValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_MinLineLength_Last_Value_Int, (""+saveMinLineLengthValue));
                    break;

                case  R.id.MaxLinrGap:
                    Log.e(TAG, "MaxLinrGap percent: "+currentProgress);

                    int saveMaxLinrGapValue = (int)(currentProgress * 100);

                    if(saveMaxLinrGapValue <= 1)
                    {
                        saveMaxLinrGapValue = 1;
                    }

                    Log.e(TAG, "MaxLinrGap value: "+saveMaxLinrGapValue);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 6;
                    MaxLinrGap.showValue.setText((""+saveMaxLinrGapValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_MaxLineGap_Last_Value_Int, (""+saveMaxLinrGapValue));
                    break;

                case  R.id.CheckPositiveSlopes:
                    Log.e(TAG, "CheckPosSlopes percent: "+currentProgress);

                    float saveCheckPosSlopes = currentProgress * 10;

                    Log.e(TAG, "CheckPosSlopes value: "+saveCheckPosSlopes);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 7;
                    CheckPositiveSlopes.showValue.setText(String.format("%.2f", saveCheckPosSlopes));
                    simpleDatabase.setValueByKey(Constants.PosSlopes_Last_Value_Double, CheckPositiveSlopes.getValue());
                    break;

                case  R.id.CheckNegitiveSlopes:

                    Log.e(TAG, "CheckNegSlopes percent: "+currentProgress);

                    float saveCheckNegSlopes = -currentProgress * 10f;

                    Log.e(TAG, "CheckNegSlopes value: "+saveCheckNegSlopes);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 8;
                    CheckNegitiveSlopes.showValue.setText(String.format("%.2f", saveCheckNegSlopes));
                    simpleDatabase.setValueByKey(Constants.NegSlopes_Last_Value_Double, CheckNegitiveSlopes.getValue());

                    break;

                case R.id.DetectionError:
                    int detectionErrorCount = (int)(currentProgress * 100);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 9;
                    DetectionError.showValue.setText("" + detectionErrorCount);
                    simpleDatabase.setValueByKey(Constants.DETECTION_ERROR_STOP, DetectionError.getValue());
                    break;

                case R.id.ForwardPower:
                    int saveForwardPowerValue = (int)(currentProgress * 100);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 9;
                    ForwardPower.showValue.setText("" + saveForwardPowerValue);
                    simpleDatabase.setValueByKey(Constants.MAX_FORWARD_POWER, ForwardPower.getValue());
                    break;

                case R.id.LeftAndRight:
                    int saveLeftAndRightValue = (int)(currentProgress * 100);
                    mOpenCvCameraView.CURRENT_PROCESS_STEP = 9;
                    LeftAndRight.showValue.setText("" + saveLeftAndRightValue);
                    simpleDatabase.setValueByKey(Constants.MAX_LEFT_RIGHT_POWER, LeftAndRight.getValue());
                    break;
            }

            loadParamsAndDetect();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            Log.e(TAG, "onStartTrackingTouch 開始移動");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.e(TAG, "onStopTrackingTouch 手指離開 儲存資料 開始辨識");
        }
    };

    private View.OnClickListener click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.Button_rightReferentLine:{
                    if(v.isSelected())
                    {
                        v.setSelected(false);
                        mOpenCvCameraView.isRightRefernceLine = false;

                        if(thisApp.client != null){
                            thisApp.client.setRightReferenceLine(false);
                        }
                    }
                    else
                    {
                        v.setSelected(true);
                        mOpenCvCameraView.isRightRefernceLine = true;

                        if(thisApp.client != null){
                            thisApp.client.setRightReferenceLine(true);
                        }
                    }
                }break;

                case R.id.Button_leftReferenceLine:{
                    if(v.isSelected())
                    {
                        v.setSelected(false);
                        mOpenCvCameraView.isLeftRefernceLine = false;

                        if(thisApp.client != null){
                            thisApp.client.setLeftReferenceLine(false);
                        }
                    }
                    else
                    {
                        v.setSelected(true);
                        mOpenCvCameraView.isLeftRefernceLine = true;

                        if(thisApp.client != null){
                            thisApp.client.setLeftReferenceLine(true);
                        }
                    }
                }break;

                case R.id.Button_Left:
                    LBoolean = true;
                    PageCh();
                    break;
                case R.id.Button_Right:
                    RBoolean = true;
                    PageCh();
                    break;

                case R.id.Button_Paint:
                    PageAll.setVisibility(View.GONE);

                    Button_Left.setVisibility(View.GONE);
                    Button_Right.setVisibility(View.GONE);
                    Button_Save.setVisibility(View.GONE);
                    Button_Load.setVisibility(View.GONE);
                    Button_Paint.setVisibility(View.GONE);
                    Button_Servo.setVisibility(View.INVISIBLE);
                    Button_Ok.setVisibility(View.VISIBLE);
                    Button_Clear.setVisibility(View.VISIBLE);

                    touchPannel.setOnTouchListener(null);
                    LeftTopPoint.setOnTouchListener(getPointTouchListener());
                    LeftBottomPoint.setOnTouchListener(getPointTouchListener());
                    RightTopPoint.setOnTouchListener(getPointTouchListener());
                    RightBottomPoint.setOnTouchListener(getPointTouchListener());

                    break;

                case R.id.Button_Clear:
                    mOpenCvCameraView.getOpenCVWrapper().setROILeftUpX(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROILeftUpY(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROIRightUpX(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROIRightUpY(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROILeftDownX(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROILeftDownY(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROIRightDownX(0);
                    mOpenCvCameraView.getOpenCVWrapper().setROIRightDownY(0);

                    ViewScaling.changeLeftMarginAndTopMargin(LeftTopPoint, -LeftTopPoint.getLayoutParams().width
                            , -LeftTopPoint.getLayoutParams().height);

                    ViewScaling.changeLeftMarginAndTopMargin(LeftBottomPoint, -LeftBottomPoint.getLayoutParams().width
                            , -LeftBottomPoint.getLayoutParams().height);

                    ViewScaling.changeLeftMarginAndTopMargin(RightTopPoint, -RightTopPoint.getLayoutParams().width
                            , -RightTopPoint.getLayoutParams().height);

                    ViewScaling.changeLeftMarginAndTopMargin(RightBottomPoint, -RightBottomPoint.getLayoutParams().width
                            , -RightBottomPoint.getLayoutParams().height);

                    LeftTopPoint.setTag(null);
                    LeftBottomPoint.setTag(null);
                    RightTopPoint.setTag(null);
                    RightBottomPoint.setTag(null);

                    touchPannel.setOnTouchListener(touch);
                    break;

                case R.id.Button_Ok:

                    PageAll.setVisibility(View.VISIBLE);
                    Button_Left.setVisibility(View.VISIBLE);
                    Button_Right.setVisibility(View.VISIBLE);
                    Button_Save.setVisibility(View.VISIBLE);
                    Button_Load.setVisibility(View.VISIBLE);
                    Button_Paint.setVisibility(View.VISIBLE);
                    Button_Ok.setVisibility(View.GONE);
                    Button_Clear.setVisibility(View.GONE);

                    if(AppMode == Constants.CAR_MODE)
                    {
                        Button_Servo.setVisibility(View.VISIBLE);
                    }

                    touchPannel.setOnTouchListener(null);
                    LeftTopPoint.setOnTouchListener(null);
                    LeftBottomPoint.setOnTouchListener(null);
                    RightTopPoint.setOnTouchListener(null);
                    RightBottomPoint.setOnTouchListener(null);

                    double[] leftTopPointValue = getViewXYToScreenPercent(LeftTopPoint);
                    double[] leftBottomPointValue = getViewXYToScreenPercent(LeftBottomPoint);
                    double[] rightTopPointValue = getViewXYToScreenPercent(RightTopPoint);
                    double[] rightBottomPointValue = getViewXYToScreenPercent(RightBottomPoint);

                    simpleDatabase.setValueByKey(Constants.Left_Top_X_Value, "" + leftTopPointValue[0]);
                    simpleDatabase.setValueByKey(Constants.Left_Top_Y_Value, "" + leftTopPointValue[1]);
                    simpleDatabase.setValueByKey(Constants.Left_Bottom_X_Value, "" + leftBottomPointValue[0]);
                    simpleDatabase.setValueByKey(Constants.Left_Bottom_Y_Value, "" + leftBottomPointValue[1]);
                    simpleDatabase.setValueByKey(Constants.Right_Top_X_Value, "" + rightTopPointValue[0]);
                    simpleDatabase.setValueByKey(Constants.Right_Top_Y_Value, "" + rightTopPointValue[1]);
                    simpleDatabase.setValueByKey(Constants.Right_Bottom_X_Value, "" + rightBottomPointValue[0]);
                    simpleDatabase.setValueByKey(Constants.Right_Bottom_Y_Value, "" + rightBottomPointValue[1]);

                    break;

                case R.id.Button_Save:
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(v.getContext());
                    final EditText inputView = new EditText(v.getContext());
                    alertBuilder.setView(inputView);
                    alertBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strInputName = inputView.getText().toString();

                            if(strInputName.isEmpty() == false)
                            {
                                ContentValues saveData = paramsFilter.checkAndChangeSettingToContentValues(simpleDatabase, strInputName);

                                if(dataBase != null)
                                {
                                    dataBase.insertSetting(saveData);
                                }
                            }
                            else
                            {
                                Toast.makeText(inputView.getContext(), "請輸入您要儲存設定的名稱", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    alertBuilder.setNegativeButton("取消", null);
                    alertBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();

                    break;
                case R.id.Button_Load:
                    Intent intentTo = new Intent(ChangeDetectionParamsPage.this, ShowAllSettingPage.class);
                    startActivity(intentTo);
                    break;

                case R.id.Button_Servo:
                    Intent intent = new Intent(ChangeDetectionParamsPage.this, ChannelSettingPage.class);
                    startActivity(intent);
                    break;

                case R.id.Button_Back:
                    onBackPressed();
                    break;

                case R.id.Button_ScaneDevice:
                    Intent intentToScanDevice = new Intent(ChangeDetectionParamsPage.this, ScanBluetoothDevicePage.class);
                    startActivity(intentToScanDevice);
                    break;

                default:
                    break;
            }
        }
    };

    private double[] getViewXYToScreenPercent(View view){
        double[] result = new double[2];

        int widthHalf = ((RelativeLayout.LayoutParams)view.getLayoutParams()).width / 2;
        int heightHalf = ((RelativeLayout.LayoutParams)view.getLayoutParams()).height / 2;

        int viewLeft = ((RelativeLayout.LayoutParams)view.getLayoutParams()).leftMargin;
        int viewTop = ((RelativeLayout.LayoutParams)view.getLayoutParams()).topMargin;

        int realX = viewLeft + widthHalf;
        int realY = viewTop + heightHalf;

        result[0] = (double)(realX) / (double)(ViewScaling.getScreenWidth());
        result[1] = (double)(realY) / (double)(ViewScaling.getScreenHeight());

        return result;
    }

    private View.OnTouchListener touch = new View.OnTouchListener() {
        // 定义手指开始触摸的坐标
        float OneStartX;
        float OneStartY;
        float StopX;
        float StopY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    // 记录开始触摸的点的坐标
                    OneStartX = event.getX();
                    OneStartY = event.getY();

//                    Log.e("OneStart", "X  " + OneStartX + "  Y  " + OneStartY);

                    int LeftTopPointX = (int)(OneStartX - (LeftTopPoint.getLayoutParams().width / 2));
                    int LeftTopPointY = (int)(OneStartY - (LeftTopPoint.getLayoutParams().height / 2));

                    ViewScaling.changeLeftMarginAndTopMargin(LeftTopPoint, LeftTopPointX, LeftTopPointY);
                    LeftTopPoint.setBackgroundColor(Color.GREEN);

                    mOpenCvCameraView.getOpenCVWrapper().setROILeftUpX((int)(OneStartX * ROIscaleValueX));
                    mOpenCvCameraView.getOpenCVWrapper().setROILeftUpY((int)(OneStartY * ROIscaleValueY));

                    break;
                // 用户手指在屏幕上移动的动作
                case MotionEvent.ACTION_MOVE:
                    // 记录移动位置的点的坐标
                    StopX = event.getX();
                    StopY = event.getY();

//                    Log.e("Stop", "X  " + StopX + "  Y  " + StopY);

                    ViewScaling.changeLeftMarginAndTopMargin(LeftBottomPoint, (int)(OneStartX - (LeftBottomPoint.getWidth() / 2)), (int)(StopY - (LeftBottomPoint.getHeight() / 2)));
                    LeftBottomPoint.setBackgroundColor(Color.RED);

                    mOpenCvCameraView.getOpenCVWrapper().setROILeftDownX((int)(OneStartX * ROIscaleValueX));
                    mOpenCvCameraView.getOpenCVWrapper().setROILeftDownY((int)(StopY * ROIscaleValueY));

                    ViewScaling.changeLeftMarginAndTopMargin(RightTopPoint, (int)(StopX - (RightTopPoint.getWidth() / 2)), (int)(OneStartY - (RightTopPoint.getHeight() / 2)));
                    RightTopPoint.setBackgroundColor(Color.YELLOW);

                    mOpenCvCameraView.getOpenCVWrapper().setROIRightUpX((int)(StopX * ROIscaleValueX));
                    mOpenCvCameraView.getOpenCVWrapper().setROIRightUpY((int)(OneStartY * ROIscaleValueY));

                    ViewScaling.changeLeftMarginAndTopMargin(RightBottomPoint, (int)(StopX - (RightBottomPoint.getWidth() / 2)), (int)(StopY - (RightBottomPoint.getHeight() / 2)));
                    RightBottomPoint.setBackgroundColor(Color.BLUE);

                    mOpenCvCameraView.getOpenCVWrapper().setROIRightDownX((int)(StopX * ROIscaleValueX));
                    mOpenCvCameraView.getOpenCVWrapper().setROIRightDownY((int)(StopY * ROIscaleValueY));


                    break;
                case MotionEvent.ACTION_UP:
                    touchPannel.setOnTouchListener(null);
                    LeftTopPoint.setOnTouchListener(getPointTouchListener());
                    LeftBottomPoint.setOnTouchListener(getPointTouchListener());
                    RightTopPoint.setOnTouchListener(getPointTouchListener());
                    RightBottomPoint.setOnTouchListener(getPointTouchListener());
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private View.OnTouchListener getPointTouchListener()
    {
        return new View.OnTouchListener() {


            float downX = 0;
            float downY = 0;

            float viewOriginX = 0;
            float viewOriginY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float rawX = event.getRawX();
                float rawY = event.getRawY();

                float halfViewWidth = v.getLayoutParams().width / 2;
                float halfViewHeight = v.getLayoutParams().height / 2;

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(pointPressedAlpha);

                        downX = rawX;
                        downY = rawY;
                        //按下時，記錄坐標
                        viewOriginX = ((RelativeLayout.LayoutParams)v.getLayoutParams()).leftMargin;
                        viewOriginY = ((RelativeLayout.LayoutParams)v.getLayoutParams()).topMargin;
                        break;


                    case MotionEvent.ACTION_MOVE:

                        float offsetX = rawX - downX;
                        float offsetY = rawY - downY;

                        float currentLeft = viewOriginX + offsetX;
                        float currentTop = viewOriginY + offsetY;

                        float roiX = currentLeft + halfViewWidth;
                        float roiY = currentTop + halfViewHeight;

                        ViewScaling.changeLeftMarginAndTopMargin(v, (int)currentLeft, (int)currentTop);

                        switch (v.getId())
                        {
                            case R.id.customView1:
                                mOpenCvCameraView.getOpenCVWrapper().setROILeftUpX((int)(roiX * ROIscaleValueX));
                                mOpenCvCameraView.getOpenCVWrapper().setROILeftUpY((int)(roiY * ROIscaleValueY));
                                break;

                            case R.id.customView2:
                                mOpenCvCameraView.getOpenCVWrapper().setROILeftDownX((int)(roiX * ROIscaleValueX));
                                mOpenCvCameraView.getOpenCVWrapper().setROILeftDownY((int)(roiY * ROIscaleValueY));
                                break;

                            case R.id.customView3:
                                mOpenCvCameraView.getOpenCVWrapper().setROIRightUpX((int)(roiX * ROIscaleValueX));
                                mOpenCvCameraView.getOpenCVWrapper().setROIRightUpY((int)(roiY * ROIscaleValueY));
                                break;

                            case R.id.customView4:
                                mOpenCvCameraView.getOpenCVWrapper().setROIRightDownX((int)(roiX * ROIscaleValueX));
                                mOpenCvCameraView.getOpenCVWrapper().setROIRightDownY((int)(roiY * ROIscaleValueY));
                                break;
                        }

                        break;


                    case MotionEvent.ACTION_UP:
                        v.setAlpha(pointUnPressedAlpha);
                        break;


                    default:
                        break;
                }
                return true;
            }

        };
    }


    @Override
    public void onBackPressed() {
        if(AppMode == Constants.COMMAND_MODE){
            ChangeDetectionParamsPage.this.finish();
        }
        else if(AppMode == Constants.CAR_MODE){
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.MODE = CVCameraWrapper.MODE_NORMAL;
            ChangeDetectionParamsPage.this.finish();
        }
        else if(AppMode == Constants.WEBCAM_MODE){
            mOpenCvCameraView.MODE = CVCameraWrapper.MODE_NORMAL;
            ChangeDetectionParamsPage.this.finish();
        }
    }
}
