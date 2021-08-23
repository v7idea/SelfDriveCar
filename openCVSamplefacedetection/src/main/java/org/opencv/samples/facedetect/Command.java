package org.opencv.samples.facedetect;

/**
 * Created by mortal on 2017/9/11.
 */

public class Command
{
    public static String isActive(boolean isActive)
    {
        if(isActive)
        {
            return "Active:true";
        }
        else
        {
            return "Active:false";
        }
    }

    public static String checkConnectedState(boolean isCheck)
    {
        if(isCheck)
        {
            return "check:true";
        }
        else
        {
            return "check:false";
        }
    }

    public static String streamVideo(boolean isStartStream)
    {
        if(isStartStream)
        {
            return "stream:true";
        }
        else
        {
            return "stream:false";
        }
    }

    public static String isLeftReferentLine(boolean isSet){
        if(isSet)
        {
            return "leftReferentLine:true";
        }
        else
        {
            return "leftReferentLine:false";
        }
    }

    public static String isRightReferentLine(boolean isSet){
        if(isSet)
        {
            return "rightReferentLine:true";
        }
        else
        {
            return "rightReferentLine:false";
        }
    }

    public static String isRecordVideo(boolean isRecord){
        if(isRecord){
            return "isRecord:true";
        }
        else{
            return "isRecord:false";
        }
    }

    public static String requestDetectionParameter(boolean isRequestDetectionParameter)
    {
        if(isRequestDetectionParameter)
        {
            return "detectionParameter:true";
        }
        else
        {
            return "detectionParameter:false";
        }
    }

    public static String isConnectedDevice(boolean isConnectedDevice)
    {
        if(isConnectedDevice)
        {
            return "isConnectedDevice:true";
        }
        else
        {
            return "isConnectedDevice:false";
        }
    }

    public static String isConnectedApp(boolean isConnectedApp)
    {
        if(isConnectedApp)
        {
            return "isConnectedApp:true";
        }
        else
        {
            return "isConnectedApp:false";
        }
    }
}
