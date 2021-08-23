package org.opencv.samples.facedetect;

import android.graphics.Bitmap;

import com.v7idea.Data.DetectionResultJava;
import com.v7idea.tool.CustomerSetting;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class OpenCVWrapper
{
    static
    {
        System.loadLibrary("OpenCV_Wrapper");
    }

    public double rightSlopeValue = 0;
    public double rightIntercpetValue = 0;
    public int rightTopX = 0;
    public int rightTopY = 0;
    public int rightBottomX = 0;
    public int rightBottomY = 0;

    public double leftSlopeValue = 0;
    public double leftIntercpetValue = 0.0;
    public int leftTopX = 0;
    public int leftTopY = 0;
    public int leftBottomX = 0;
    public int leftBottomY = 0;

    public boolean isLeftRefernceLine = false;
    public double defaultLeftSlope = 0.0;
    public double defaultLeftIntercept = 0.0;
    public int defaultLeftBottomX = 0;
    public int defaultLeftBottomY = 0;
    public int defaultLeftTopX = 0;
    public int defaultLeftTopY = 0;

    public boolean isRightRefernceLine = false;
    public double defaultRightSlope = 0.0;
    public double defaultRightIntercept = 0;
    public int defaultRightTopX = 0;
    public int defaultRightTopY = 0;
    public int defaultRightBottomX = 0;
    public int defaultRightBottomY = 0;

    ///運算後的方向
    public double channel1Value = 0;
    ///輸出給搖控車的方向數值
    public int afterComputeChannel1 = 0;

    ///運算後的前進數值
    public double channel2Value = 0;
    ///輸出給搖控車的前進數值
    public int afterComputeChannel2 = 0;

    public int baseLineWidth = 2;
    public int detectedLineWidth = 4;

    public double fps = 0.0;

    public void loadSaveParamsData(CustomerSetting customerSetting)
    {
        //讀取SimpleDataBase 裡的資料，直接將數值載入CPP


        if(customerSetting != null)
        {
            int roiLeftUpX = customerSetting.getROILeftUpX();
            int roiLeftUpY = customerSetting.getROILeftUpY();

            int roiLeftDownX = customerSetting.getROILeftDownX();
            int roiLeftDownY = customerSetting.getROILeftDownY();

            int roiRightUpX = customerSetting.getROIRightUpX();
            int roiRightUpY = customerSetting.getROIRightUpY();

            int roiRightDownX = customerSetting.getROIRightDownX();
            int roiRightDownY = customerSetting.getROIRightDownY();

            setROILeftUpX(roiLeftUpX);
            setROILeftUpY(roiLeftUpY);
            setROILeftDownX(roiLeftDownX);
            setROILeftDownY(roiLeftDownY);
            setROIRightUpX(roiRightUpX);
            setROIRightUpY(roiRightUpY);
            setROIRightDownX(roiRightDownX);
            setROIRightDownY(roiRightDownY);

            double rightSlopesValue = customerSetting.getCheckPosSlopes();
            double leftSlopesValue = customerSetting.getCheckNegSlopes();
            int gausianChangeValue = customerSetting.getGausianKernelValue();
            double autoCannyValue = customerSetting.getAutoCannyValue();
            double rhoValue = customerSetting.getRhoValue();
            int thetaValue = customerSetting.getThetaValue();
            int thresholdValue = customerSetting.getTheresHoldValue();
            int minLineLengthValue = customerSetting.getMinLineLen();
            int maxLineGapValue = customerSetting.getMaxLineGap();

            setCheckPosSlopes(rightSlopesValue);
            setCheckNegSlopes(leftSlopesValue);
            setGausianKernelValue(gausianChangeValue);
            setAutoCannyValue(autoCannyValue);
            setRhoValue(rhoValue);
            setThetaValue(thetaValue);
            setTheresHoldValue(thresholdValue);
            setMinLineLen(minLineLengthValue);
            setMaxLineGap(maxLineGapValue);
        }
    }


    public DetectionResultJava detectionLane(long image)
    {
        double[] resultArray = count4Points(image);

        //回傳的Array 0-5是左邊的數值，6-11是右邊的數值
        return new DetectionResultJava(resultArray);
    }

    private Mat interfaceImage = null;

    public void drawResultJava(long jMatObject)
    {
        drawResult(jMatObject, baseLineWidth, detectedLineWidth
                , rightSlopeValue, rightTopX, rightTopY, rightBottomX, rightBottomY
                , leftSlopeValue, leftTopX, leftTopY, leftBottomX, leftBottomY
                , isLeftRefernceLine,  defaultLeftBottomX, defaultLeftBottomY, defaultLeftTopX, defaultLeftTopY
                , isRightRefernceLine, defaultRightTopX, defaultRightTopY, defaultRightBottomX, defaultRightBottomY
                , defaultRightSlope
                , defaultRightIntercept, rightIntercpetValue
                , defaultLeftSlope
                , defaultLeftIntercept, leftIntercpetValue
                , channel1Value, channel2Value
                , afterComputeChannel1, afterComputeChannel2
                , fps);
    }

    public DetectionResultJava changeParamsModeAndResultValue(long matAddress, int whichStep){
        changeParamsModeUse(matAddress,whichStep);

        int[] pointArray = getPointArray();

        DetectionResultJava detectionResult = new DetectionResultJava(pointArray[2], pointArray[3], pointArray[0], pointArray[1], 0, 0
            , pointArray[4], pointArray[5], pointArray[6], pointArray[7], 0, 0);

        return detectionResult;
    }

    public void drawChangeParamsModeReferenceLine(long jMatObject){
        drawChangeParamsModeReferenceLine(jMatObject, detectedLineWidth
            , isLeftRefernceLine, defaultLeftBottomX, defaultLeftBottomY, defaultLeftTopX, defaultLeftTopY
            , isRightRefernceLine, defaultRightTopX, defaultRightTopY, defaultRightBottomX, defaultRightBottomY);
    }

    public native int changeParamsModeUse(long matAddress, int whichStep);

    private native void drawChangeParamsModeReferenceLine(long jMatObject, int baseLineWidth
            , boolean isLeftRefernceLine, int defaultLeftBottomX, int defaultLeftBottomY, int defaultLeftTopX, int defaultLeftTopY
            , boolean isRightRefernceLine, int defaultRightTopX, int defaultRightTopY, int defaultRightBottomX, int defaultRightBottomY);

    public native void drawResult(long jMatObject, int baseLineWidth, int detectLineWidth
            , double rightSlopeValue, int rightTopX, int rightTopY, int rightBottomX, int rightBottomY
            , double leftSlopeValue, int leftTopX, int leftTopY, int leftBottomX, int leftBottomY
            , boolean isLeftRefernceLine, int defaultLeftBottomX, int defaultLeftBottomY, int defaultLeftTopX, int defaultLeftTopY
            , boolean isRightRefernceLine, int defaultRightTopX, int defaultRightTopY, int defaultRightBottomX, int defaultRightBottomY
            , double defaultRightSlope
            , double defaultRightIntercept, double rightIntercpetValue
            , double defaultLeftSlope
            , double defaultLeftIntercept, double leftIntercpetValue
            , double channel1Value, double channel2Value
            , int afterComputeChannel1, int afterComputeChannel2
            , double fps);

    private native double[] count4Points(long image);

    //ROI區域設定
    public native void setROILeftUpX(int leftUpX);
    public native void setROILeftUpY(int leftUpY);
    public native void setROILeftDownX(int leftDownX);
    public native void setROILeftDownY(int leftDownY);
    public native void setROIRightUpX(int rightUpX);
    public native void setROIRightUpY(int rightUpY);
    public native void setROIRightDownX(int rightDownX);
    public native void setROIRightDownY(int rightDownY);

    public native void setCheckPosSlopes(double checkPosSlopes);
    public native void setCheckNegSlopes(double checkNegSlopes);
    public native void setGausianKernelValue(int gausianKernelValue);
    public native void setAutoCannyValue(double autoCannyValue);
    public native void setRhoValue(double rho);
    public native void setThetaValue(int thetaValue);
    public native void setTheresHoldValue(int theresHoldValue);
    public native void setMinLineLen(int minLineLen);
    public native void setMaxLineGap(int maxLineGap);

    public native Mat[] getAllStepImage(long jMatObject);
    public native double[] getSlopesInterceptGroup();
    public native int getLineCount();
    public native int[] getPointArray();

    public native void rightSlopeTextColorUseNormalColor();
    public native void rightSlopeTextColorUseDangerColor();
    public native void leftSlopeTextColorUseNormalColor();
    public native void leftSlopeTextColorUsedDangerColor();

    //變更字體大小及字的位置
    public native void setTextSize(double jTextSize);
    public native void setScaleMin(double jScaleMinValue);
    public native void scaleTextPosition(double jScaleXValue, double jScaleYValue);

    //for demo

    public Bitmap demoYellowToWhite(Bitmap image)
    {
        return yellowToWhite(image);
    }

    public Bitmap demoGaussianBlur(Bitmap image)
    {
        return gaussianBlur(image);
    }

    public Bitmap demoGrayscale(Bitmap image)
    {
        return grayscale(image);
    }

    public Bitmap demoAutoCanny(Bitmap image)
    {
        return autoCanny(image);
    }

    public Bitmap demoRegionOfInterest(Bitmap image)
    {
        return regionOfInterest(image);
    }

    public Bitmap demoHoughLines(Bitmap image)
    {
        return houghLines(image);
    }


    private native Bitmap yellowToWhite(Bitmap image);
    private native Bitmap grayscale(Bitmap image);
    private native Bitmap gaussianBlur(Bitmap image);
    private native Bitmap autoCanny(Bitmap image);
    private native Bitmap regionOfInterest(Bitmap image);
    private native Bitmap houghLines(Bitmap image);

    //for demo
}
