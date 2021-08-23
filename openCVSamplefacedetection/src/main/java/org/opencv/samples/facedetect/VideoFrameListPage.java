package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.tool.CVCameraWrapper;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.ParamsFilter;
import com.v7idea.tool.ViewScaling;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class VideoFrameListPage extends Activity {

    private static final String TAG = "VideoFrameListPage";

    private ImageView OriginalImage = null;
    private TextView OriginalImageValue = null;

    private Button PrePage = null;
    private Button Revision = null;
    private Button NextPage = null;

    private EditText inputLeftTopX = null;
    private EditText inputLeftTopY = null;
    private EditText inputLeftDownX = null;
    private EditText inputLeftDownY = null;
    private EditText inputRightTopX = null;
    private EditText inputRightTopY = null;
    private EditText inputRightDownX = null;
    private EditText inputRightDownY = null;

    private TextView KernelValue = null;
    private SeekBar changeKernelValue = null;

    private TextView AutoCannyValue = null;
    private SeekBar changeAutoCannyValue = null;

    private ImageView yellowToWhiteImage = null;
    private ImageView GrayScaleImage = null;
    private ImageView BlurImage = null;
    private ImageView AutoCannyImage = null;
    private ImageView roiImage = null;
    private TextView RhoValue = null;
    private SeekBar changeRhoValue = null;

    private TextView ThetaValue = null;
    private SeekBar changeThetaValue = null;

    private TextView ThresholdValue = null;
    private SeekBar changeThresholdValue = null;

    private TextView MinLineLengthValue = null;
    private SeekBar changeMinLineLengthValue = null;

    private TextView MaxLineGapValue = null;
    private SeekBar MaxLineGap = null;

    private ImageView OnlyHoughLineImage = null;

    private TextView CheckPosSlopesValue = null;
    private SeekBar CheckPosSlopes = null;

    private TextView CheckNegSlopesValue = null;
    private SeekBar CheckNegSlopes = null;

    private ImageView HoughLineImage = null;

    private ImageView Result = null;

    private ImageView Result2 = null;

    private TextView LinesOutput = null;

    private TextView SlopeOutput = null;
    private TextView SlopeOutput2 = null;

    private TextView InterceptOutput = null;
    private TextView InterceptOutput2 = null;

    private TextView P1Output = null;
    private TextView P2Output = null;
    private TextView P3Output = null;
    private TextView P4Output = null;

    private EditText inputPower = null;
    private EditText inputTurnArea = null;

    private OpenCVWrapper openCVWrapper = null;
    private SimpleDatabase simpleDatabase = null;
    private ParamsFilter paramsFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.video_frame_list_layout);

        simpleDatabase = new SimpleDatabase();
        paramsFilter = new ParamsFilter();

        openCVWrapper = new OpenCVWrapper();

        CustomerSetting customerSetting = paramsFilter.getCurrentSetting(simpleDatabase);

        openCVWrapper.loadSaveParamsData(customerSetting);

        ViewScaling.setScaleValue(this);

        ViewScaling.findViewByIdAndScale(this, R.id.ButtonLinearLayout);

        OriginalImage = (ImageView) findViewById(R.id.mainActivity_ImageView_Original);
        OriginalImageValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Original);

        Bitmap sourceBitmap = getSourceBitmap();

        OriginalImage.setImageBitmap(sourceBitmap);

        OriginalImageValue.setText("width:"+sourceBitmap.getWidth()+" height:"+sourceBitmap.getHeight());

        PrePage = (Button) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_Button_PrePage);
        Revision = (Button) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_Button_Revision);
        NextPage = (Button) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_Button_NextPage);

        Button toChannelPage = (Button) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_Button_toChannelPage);

        PrePage.setOnClickListener(onButtonClickListener);
        Revision.setOnClickListener(onButtonClickListener);
        NextPage.setOnClickListener(onButtonClickListener);
        toChannelPage.setOnClickListener(onButtonClickListener);

        TextView leftTopXTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_LeftTopX);
        inputLeftTopX = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_LeftTopX_editText);

        TextView leftTopYTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_LeftTopY);
        inputLeftTopY = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_LeftTopY_editText);

        TextView leftDownXTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_LeftDownX);
        inputLeftDownX = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_LeftDownX_editText);

        TextView leftDownYTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_LeftDownY);
        inputLeftDownY = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_LeftDownY_editText);

        TextView rightTopXTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_RightTopX);
        inputRightTopX = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_RightTopX_editText);

        TextView rightTopYTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_RightTopY);
        inputRightTopY = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_RightTopY_editText);

        TextView rightDownXTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_RightDownX);
        inputRightDownX = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_RightDownX_editText);

        TextView rightDownYTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_RightDownY);
        inputRightDownY = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_EditText_RightDownY_editText);

        TextView KernelText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_KernelText);
        KernelValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_KernelValue);
        TextView KernelMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_KernelMin);
        changeKernelValue = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_SeekBar_Kernel);
        TextView KernelMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_KernelMax);

        TextView AutoCannyText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_AutoCannyText);
        AutoCannyValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_AutoCannyValue);
        TextView AutoCannyMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_AutoCannyMin);
        changeAutoCannyValue = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_SeekBar_AutoCanny);
        TextView AutoCannyMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_AutoCannyMax);

        yellowToWhiteImage = (ImageView) findViewById(R.id.mainActivity_ImageView_YellowToWhile);
        TextView yellowToWhileText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_YellowToWhile);

        GrayScaleImage = (ImageView) findViewById(R.id.mainActivity_ImageView_GrayScale);
        TextView GrayScaleText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_GrayScale);

        BlurImage = (ImageView) findViewById(R.id.mainActivity_ImageView_Gaussian_Blur);
        TextView BlurText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Gaussian_Blur);

        AutoCannyImage = (ImageView) findViewById(R.id.mainActivity_ImageView_Auto_Canny);
        TextView AutoCannyTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Auto_Canny);

        roiImage = (ImageView) findViewById(R.id.mainActivity_ImageView_Region_Of_Interest);
        TextView roiTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Region_Of_Interest);

        TextView RhoText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_RhoText);
        RhoValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_RhoValue);
        TextView RhoMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_RhoMin);
        changeRhoValue = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_Rho);
        TextView RhoMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_RhoMax);
        TextView RhoCaption = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_RhoCaption);

        TextView ThetaText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThetaText);
        ThetaValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThetaValue);
        TextView ThetaMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThetaMin);
        changeThetaValue = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_Theta);
        TextView ThetaMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThetaMax);
        TextView ThetaCaption = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThetaCaption);

        TextView ThresholdText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThresholdText);
        ThresholdValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThresholdValue);
        TextView ThresholdMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThresholdMin);
        changeThresholdValue = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_Threshold);
        TextView ThresholdMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThresholdMax);
        TextView ThresholdCaption = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_ThresholdCaption);

        TextView MinLineLengthText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MinLineLengthText);
        MinLineLengthValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MinLineLengthValue);
        TextView MinLineLengthMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MinLineLengthMin);
        changeMinLineLengthValue = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_MinLineLength);
        TextView MinLineLengthMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MinLineLengthMax);
        TextView MinLineLengthCaption = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MinLineLengthCaption);

        TextView MaxLineGapText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MaxLinrGapText);
        MaxLineGapValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MaxLinrGapValue);
        TextView MaxLineGapMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MaxLinrGapMin);
        MaxLineGap = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_MaxLinrGap);
        TextView MaxLineGapMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MaxLinrGapMax);
        TextView MaxLineGapCaption = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_MaxLinrGapCaption);

        OnlyHoughLineImage = (ImageView) findViewById(R.id.mainActivity_ImageView_Only_Hough_Line);
        TextView OnlyHoughLineText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Only_Hough_Line);

        TextView CheckPosSlopesText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckPosSlopesText);
        CheckPosSlopesValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckPosSlopesValue);
        TextView CheckPosSlopesMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckPosSlopesMin);
        CheckPosSlopes = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_CheckPosSlopes);
        TextView CheckPosSlopesMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckPosSlopesMax);

        TextView CheckNegSlopesText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckNegSlopesText);
        CheckNegSlopesValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckNegSlopesValue);
        TextView CheckNegSlopesMin = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckNegSlopesMin);
        CheckNegSlopes = (SeekBar) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_SeekBar_CheckNegSlopes);
        TextView CheckNegSlopesMax = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainactivity_TextView_CheckNegSlopesMax);

        HoughLineImage = (ImageView) findViewById(R.id.mainActivity_ImageView_Hough_Lines);
        TextView HoughLineText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Hough_Lines);

        Result = (ImageView) findViewById(R.id.mainActivity_ImageView_Result);
        TextView ResultText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Result);

        Result2 = (ImageView) findViewById(R.id.mainActivity_ImageView_Result2);
        TextView Result2Text = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_Result2);

        TextView LinesText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_LinesText);
        LinesOutput = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_LinesOutput);

        TextView SlopeText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_SlopeText);
        SlopeOutput = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_SlopeOutput);
        SlopeOutput2 = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_SlopeOutput2);

        TextView InterceptText = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_InterceptText);
        InterceptOutput = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_InterceptOutput);
        InterceptOutput2 = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_InterceptOutput2);

        TextView P1Text = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P1Text);
        P1Output = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P1Output);

        TextView P2Text = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P2Text);
        P2Output = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P2Output);

        TextView P3Text = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P3Text);
        P3Output = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P3Output);

        TextView P4Text = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P4Text);
        P4Output = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_P4Output);

        TextView powerTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_powerTitle);
        inputPower = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_inputPower);

        TextView turnAreaTitle = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_turnAreaTitle);
        inputTurnArea = (EditText) ViewScaling.findViewByIdAndScale(this, R.id.mainActivity_TextView_inputTurnArea);

        loadData(customerSetting);

        changeKernelValue.setOnSeekBarChangeListener(onSeekBarChangeListener);
        changeAutoCannyValue.setOnSeekBarChangeListener(onSeekBarChangeListener);
        changeRhoValue.setOnSeekBarChangeListener(onSeekBarChangeListener);
        changeThetaValue.setOnSeekBarChangeListener(onSeekBarChangeListener);
        changeThresholdValue.setOnSeekBarChangeListener(onSeekBarChangeListener);
        changeMinLineLengthValue.setOnSeekBarChangeListener(onSeekBarChangeListener);
        MaxLineGap.setOnSeekBarChangeListener(onSeekBarChangeListener);
        CheckPosSlopes.setOnSeekBarChangeListener(onSeekBarChangeListener);
        CheckNegSlopes.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private void loadData(CustomerSetting customerSetting)
    {
        inputLeftTopX.setText("" + customerSetting.getROILeftUpX());
        inputLeftTopY.setText("" + customerSetting.getROILeftUpY());

        inputLeftDownX.setText("" + customerSetting.getROILeftDownX());
        inputLeftDownY.setText("" + customerSetting.getROILeftDownY());

        inputRightTopX.setText("" + customerSetting.getROIRightUpX());
        inputRightTopY.setText("" + customerSetting.getROIRightUpY());

        inputRightDownX.setText("" + customerSetting.getROIRightDownX());
        inputRightDownY.setText("" + customerSetting.getROIRightDownY());

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

        int gausianProgressValue = changeValueTo0To100(Float.valueOf(gausianChangeValue), 100, changeKernelValue);
        KernelValue.setText("" + gausianChangeValue);
        changeKernelValue.setProgress(Integer.valueOf(gausianProgressValue));

        int autoCannyProgressValue = changeValueTo0To100((float)(autoCannyValue), 500, changeAutoCannyValue);
        AutoCannyValue.setText("" + autoCannyValue);
        changeAutoCannyValue.setProgress(autoCannyProgressValue);

        int rhoProgressValue = changeValueTo0To100((float)(rhoValue), 100, changeRhoValue);
        RhoValue.setText("" + rhoValue);
        changeRhoValue.setProgress(rhoProgressValue);

        int thetaProgressValue = changeValueTo0To100(Float.valueOf(thetaValue), 100, changeThetaValue);
        ThetaValue.setText("" + thetaValue);
        changeThetaValue.setProgress(thetaProgressValue);

        int thresholdProgressValue = changeValueTo0To100(Float.valueOf(thresholdValue), 100, changeThresholdValue);
        ThresholdValue.setText("" + thresholdValue);
        changeThresholdValue.setProgress(thresholdProgressValue);

        int minLineLengthProgressValue = changeValueTo0To100(Float.valueOf(minLineLengthValue), 100, changeMinLineLengthValue);
        MinLineLengthValue.setText("" + minLineLengthValue);
        changeMinLineLengthValue.setProgress(minLineLengthProgressValue);

        int maxLineGapProgressValue = changeValueTo0To100((float)(maxLineGapValue), 100, MaxLineGap);
        MaxLineGapValue.setText("" + maxLineGapValue);
        MaxLineGap.setProgress(maxLineGapProgressValue);

        int rightSlopesProgressValue = changeValueTo0To100((float)(rightSlopesValue), 10, CheckPosSlopes);
        CheckPosSlopesValue.setText("" + rightSlopesValue);
        CheckPosSlopes.setProgress(rightSlopesProgressValue);

        int leftSlopesProgressValue = changeValueTo0To100((float)(-leftSlopesValue), 10, CheckNegSlopes);
        CheckNegSlopesValue.setText("" + leftSlopesValue);
        CheckNegSlopes.setProgress(leftSlopesProgressValue);

        inputPower.setText("" + maxForwardPowerValue);
        inputTurnArea.setText("" + maxLeftRightPowerValue);
    }

    private void saveData()
    {
        saveROI();
        saveMaxPower();
    }

    private void saveROI()
    {
        double inputLeftTopXValue = Double.valueOf(inputLeftTopX.getText().toString()) / Constants.videoWidth;
        double inputLeftTopYValue = Double.valueOf(inputLeftTopY.getText().toString()) / Constants.videoHeight;

        double inputLeftDownXValue = Double.valueOf(inputLeftDownX.getText().toString()) / Constants.videoWidth;
        double inputLeftDownYValue = Double.valueOf(inputLeftDownY.getText().toString()) / Constants.videoHeight;

        double inputRightTopXValue = Double.valueOf(inputRightTopX.getText().toString()) / Constants.videoWidth;
        double inputRightTopYValue = Double.valueOf(inputRightTopY.getText().toString()) / Constants.videoHeight;

        double inputRightDownXValue = Double.valueOf(inputRightDownX.getText().toString()) / Constants.videoWidth;
        double inputRightDownYValue = Double.valueOf(inputRightDownY.getText().toString()) / Constants.videoHeight;

        simpleDatabase.setValueByKey(Constants.Left_Top_X_Value, String.format("%.4f", inputLeftTopXValue));
        simpleDatabase.setValueByKey(Constants.Left_Top_Y_Value, String.format("%.4f", inputLeftTopYValue));

        simpleDatabase.setValueByKey(Constants.Left_Bottom_X_Value, String.format("%.4f", inputLeftDownXValue));
        simpleDatabase.setValueByKey(Constants.Left_Bottom_Y_Value, String.format("%.4f", inputLeftDownYValue));

        simpleDatabase.setValueByKey(Constants.Right_Top_X_Value, String.format("%.4f", inputRightTopXValue));
        simpleDatabase.setValueByKey(Constants.Right_Top_Y_Value, String.format("%.4f", inputRightTopYValue));

        simpleDatabase.setValueByKey(Constants.Right_Bottom_X_Value, String.format("%.4f", inputRightDownXValue));
        simpleDatabase.setValueByKey(Constants.Right_Bottom_Y_Value, String.format("%.4f", inputRightDownYValue));
    }

    private void saveMaxPower()
    {
        simpleDatabase.setValueByKey(Constants.MAX_FORWARD_POWER, inputPower.getText().toString());
        simpleDatabase.setValueByKey(Constants.MAX_LEFT_RIGHT_POWER, inputTurnArea.getText().toString());
    }

    private void loadParamsAndDetect()
    {
        saveData();

        if(openCVWrapper == null)
        {
            openCVWrapper = new OpenCVWrapper();
        }

        CustomerSetting customerSetting = paramsFilter.getCurrentSetting(simpleDatabase);

        openCVWrapper.loadSaveParamsData(customerSetting);

        Bitmap sourceBitmap = getSourceBitmap();

        Mat sourceMat = new Mat();
        Utils.bitmapToMat(sourceBitmap, sourceMat);

        Mat[] matArray = openCVWrapper.getAllStepImage(sourceMat.getNativeObjAddr());

        Bitmap[] showStepBitmaps = new Bitmap[matArray.length];

        for(int i = 0 ; i < showStepBitmaps.length ; i++)
        {
            int matCols = matArray[i].cols();
            int matRows = matArray[i].rows();

            Log.e(TAG, "matCols: " + matCols + "  matRows: "+matRows);
            Log.e(TAG, "matWidth: " + matArray[i].width() + "  matHeight: "+matArray[i].height());

            showStepBitmaps[i] = Bitmap.createBitmap(matCols, matRows, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matArray[i], showStepBitmaps[i]);
        }

        yellowToWhiteImage.setImageBitmap(showStepBitmaps[0]);
        GrayScaleImage.setImageBitmap(showStepBitmaps[1]);
        BlurImage.setImageBitmap(showStepBitmaps[2]);
        AutoCannyImage.setImageBitmap(showStepBitmaps[3]);
        roiImage.setImageBitmap(showStepBitmaps[4]);
        OnlyHoughLineImage.setImageBitmap(showStepBitmaps[5]);
        HoughLineImage.setImageBitmap(showStepBitmaps[6]);
        Result.setImageBitmap(showStepBitmaps[7]);
        Result2.setImageBitmap(showStepBitmaps[8]);

        double[] slopesInterceptGroup = openCVWrapper.getSlopesInterceptGroup();

        int[] pointArray = openCVWrapper.getPointArray();


        LinesOutput.setText("Detected lines is: "+openCVWrapper.getLineCount());

        SlopeOutput.setText("PosSlope is: "+ String.format("%.2f", slopesInterceptGroup[0]));
        SlopeOutput2.setText("NegSlope is: "+ String.format("%.2f", slopesInterceptGroup[2]));

        InterceptOutput.setText("PosIntercept is: "+ String.format("%.2f", slopesInterceptGroup[1]));
        InterceptOutput2.setText("NegIntercept is: "+ String.format("%.2f", slopesInterceptGroup[3]));

        P1Output.setText("x: "+pointArray[0]+" y: "+pointArray[1]);
        P2Output.setText("x: "+pointArray[2]+" y: "+pointArray[3]);
        P3Output.setText("x: "+pointArray[4]+" y: "+pointArray[5]);
        P4Output.setText("x: "+pointArray[6]+" y: "+pointArray[7]);

        Log.d(TAG, "DONE !!");
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

    private Bitmap getSourceBitmap()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inJustDecodeBounds = false;
        options.inScaled = false;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.solidyellowcurve, options);

//        float scale = 1.0f;

        float xScale = 360f / 960f;
        float yScale = 240f / 540f;

//        if(xScale < yScale)
//        {
//            scale = xScale;
//        }
//        else
//        {
//            scale = yScale;
//        }

        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);

        Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap = null;
        Log.d("MainActivtiy", "Bitmap width: " + finalBitmap.getWidth() + " Bitmap height: "+finalBitmap.getHeight());

        return  finalBitmap;
    }

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.mainActivity_Button_PrePage:
                    break;

                case R.id.mainActivity_Button_Revision:
                    saveData();
                    loadParamsAndDetect();
                    break;

                case R.id.mainActivity_Button_NextPage:
                    break;

                case R.id.mainActivity_Button_toChannelPage:
                    saveData();
                    Intent intent = new Intent(VideoFrameListPage.this, ChannelSettingPage.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            float currentProgress = (float) (progress) / (float)(seekBar.getMax());

            switch (seekBar.getId())
            {
                case  R.id.mainActivity_SeekBar_Kernel:

                    Log.e(TAG, "Kernel percent: "+currentProgress);

                    int saveKernelValue = (int)(currentProgress * 100);

                    Log.e(TAG, "Kernel percent: "+saveKernelValue);

                    if(saveKernelValue % 2 == 0)
                    {
                        saveKernelValue += 1;
                        seekBar.setProgress(saveKernelValue);
                    }


                    KernelValue.setText((""+saveKernelValue));
                    simpleDatabase.setValueByKey(Constants.Gausian_Last_Value_Int, (""+saveKernelValue));

                    break;

                case  R.id.mainActivity_SeekBar_AutoCanny:

                    float cannyProgress = (float) (progress) / 100f;

                    Log.e(TAG, "AutoCanny percent: "+cannyProgress);

                    Log.e(TAG, "AutoCanny saveAutoCannyValue: "+cannyProgress);

                    AutoCannyValue.setText((""+cannyProgress));
                    simpleDatabase.setValueByKey(Constants.AutoCanny_Last_Value_Float, (""+cannyProgress));

                    break;

                case  R.id.mainactivity_SeekBar_Rho:
                    Log.e(TAG, "Rho percent: "+currentProgress);

                    float saveRhoValue = currentProgress * 100f;

                    Log.e(TAG, "Rho value: "+saveRhoValue);

                    RhoValue.setText((""+saveRhoValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_RHO_Last_Value_Double, (""+saveRhoValue));

                    break;

                case  R.id.mainactivity_SeekBar_Theta:
                    Log.e(TAG, "Theta percent: "+currentProgress);

                    int saveThetaValue = (int)(currentProgress * 100);

                    Log.e(TAG, "Theta value: "+saveThetaValue);

                    ThetaValue.setText((""+saveThetaValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_THETA_Last_Value_Int, (""+saveThetaValue));
                    break;

                case  R.id.mainactivity_SeekBar_Threshold:
                    Log.e(TAG, "Threshold percent: "+currentProgress);

                    int saveThresholdValue = (int)(currentProgress * 100);

                    Log.e(TAG, "Threshold value: "+saveThresholdValue);

                    ThresholdValue.setText((""+saveThresholdValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_Threshold_Last_Value_Int, (""+saveThresholdValue));
                    break;

                case  R.id.mainactivity_SeekBar_MinLineLength:
                    Log.e(TAG, "MinLineLength percent: "+currentProgress);

                    int saveMinLineLengthValue = (int)(currentProgress * 100);

                    Log.e(TAG, "MinLineLength value: "+saveMinLineLengthValue);

                    MinLineLengthValue.setText((""+saveMinLineLengthValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_MinLineLength_Last_Value_Int, (""+saveMinLineLengthValue));
                    break;

                case  R.id.mainactivity_SeekBar_MaxLinrGap:
                    Log.e(TAG, "MaxLinrGap percent: "+currentProgress);

                    int saveMaxLinrGapValue = (int)(currentProgress * 100);

                    Log.e(TAG, "MaxLinrGap value: "+saveMaxLinrGapValue);

                    MaxLineGapValue.setText((""+saveMaxLinrGapValue));
                    simpleDatabase.setValueByKey(Constants.HoughLine_MaxLineGap_Last_Value_Int, (""+saveMaxLinrGapValue));
                    break;

                case  R.id.mainactivity_SeekBar_CheckPosSlopes:
                    Log.e(TAG, "CheckPosSlopes percent: "+currentProgress);

                    float saveCheckPosSlopes = currentProgress * 10;

                    Log.e(TAG, "CheckPosSlopes value: "+saveCheckPosSlopes);

                    CheckPosSlopesValue.setText(String.format("%.2f", saveCheckPosSlopes));
                    simpleDatabase.setValueByKey(Constants.PosSlopes_Last_Value_Double, CheckPosSlopesValue.getText().toString());
                    break;

                case  R.id.mainactivity_SeekBar_CheckNegSlopes:

                    Log.e(TAG, "CheckNegSlopes percent: "+currentProgress);

                    float saveCheckNegSlopes = -currentProgress * 10f;

                    Log.e(TAG, "CheckNegSlopes value: "+saveCheckNegSlopes);

                    CheckNegSlopesValue.setText(String.format("%.2f", saveCheckNegSlopes));
                    simpleDatabase.setValueByKey(Constants.NegSlopes_Last_Value_Double, CheckNegSlopesValue.getText().toString());

                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            Log.e(TAG, "onStartTrackingTouch 開始移動");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.e(TAG, "onStopTrackingTouch 手指離開 儲存資料 開始辨識");
            loadParamsAndDetect();
        }
    };

    @Override
    public void onBackPressed() {
        saveData();
        VideoFrameListPage.this.finish();
    }
}
