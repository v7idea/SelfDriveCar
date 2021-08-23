package com.v7idea.tool;

import android.content.ContentValues;
import android.graphics.Color;
import android.util.Log;

import com.v7idea.DataBase.SimpleDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mortal on 2017/8/4.
 */

public class ParamsFilter
{
    private final static String TAG = "ParamsFilter";

    public CustomerSetting getCurrentSetting(SimpleDatabase simpleDatabase)
    {
        CustomerSetting result = new CustomerSetting();

        String settingKind = simpleDatabase.getStringValueByKey(Constants.KIND, "car");
        String settingName = simpleDatabase.getStringValueByKey(Constants.SETTING_NAME, "TmpName");

        result.setKind(settingKind);
        result.setName(settingName);

        ExpotentialMode expotentialMode = loadAndCheckExpotentialMode(simpleDatabase);

        if(expotentialMode != null)
        {
            result.setExpotentialModeItem(expotentialMode);
        }
        else
        {
            DebugLog.e(TAG, "create ExpotentialMode fail !!");
        }

        //11~18
        //check的部分還沒寫，還沒查存取的值
        MaxingConfig maxingConfig = loadAndCheckMaxingConfig(simpleDatabase);

        if(maxingConfig != null)
        {
            result.setMaxingConfigItem(maxingConfig);
        }
        else
        {
            DebugLog.e(TAG, "create maxingConfig fail !!");
        }

        //19~26
        ServoReverse servoReverse = loadAndCheckServoReverse(simpleDatabase);

        if(servoReverse != null)
        {
            result.setServoReverseItem(servoReverse);
        }
        else
        {
            DebugLog.e(TAG, "create servoReverse fail !!");
        }

        ArrayList<StrimConfig> strimConfigArray = loadAndCheckStrimConfig(simpleDatabase);
        result.setStrimConfigArray(strimConfigArray);

        // 找出目前油門自動回復的狀態;
        String[] isAutoResetArray = loadAndIsAutoResetChannel(simpleDatabase);
        result.setIsAutoResetChannel(isAutoResetArray);

        //27
        String screeanControlModevalue = loadAndCheckScreenControlModeValue(simpleDatabase);
        result.setScreeanControlMode(screeanControlModevalue);

        //28
        String mixingConfigReverseValue = loadAndCheckMixingConfigReverseValue(simpleDatabase);
        result.setMixingConfigReverse(mixingConfigReverseValue);

        String thisResetState = loadAndCheckIsAutoResetTHRValue(simpleDatabase);
        result.setAutoResetTHRState(thisResetState);

        String sensorInductionAngle = loadAndCheckSensorInductionAngle(simpleDatabase);
        result.setSensorInductionAngle(sensorInductionAngle);

        String ifReverseCamera = loadAndCheckIfReverseCameraValue(simpleDatabase);
        result.setIfReverseCamera(ifReverseCamera);

        String isAutoResetTHR = loadAndCheckIsAutoResetTHRValue(simpleDatabase);
        result.setIsAutoResetTHR(isAutoResetTHR);

        double roiLeftUpXPercent = loadAndCheckKeyNameValue(Constants.Left_Top_X_Value, simpleDatabase, 100, 0, 0.2083);
        int roiLeftUpX = getPercentToPoint(Constants.videoWidth, roiLeftUpXPercent);

        double roiLeftUpYPercent = loadAndCheckKeyNameValue(Constants.Left_Top_Y_Value, simpleDatabase, 100, 0, 0.375);
        int roiLeftUpY = getPercentToPoint(Constants.videoHeight, roiLeftUpYPercent);

        double roiLeftDownXPercent = loadAndCheckKeyNameValue(Constants.Left_Bottom_X_Value, simpleDatabase, 100, 0, 0);
        int roiLeftDownX = getPercentToPoint(Constants.videoWidth, roiLeftDownXPercent);

        double roiLeftDownYPercent = loadAndCheckKeyNameValue(Constants.Left_Bottom_Y_Value, simpleDatabase, 100, 0, 0.75);
        int roiLeftDownY = getPercentToPoint(Constants.videoHeight, roiLeftDownYPercent);

        double roiRightUpXPercent = loadAndCheckKeyNameValue(Constants.Right_Top_X_Value, simpleDatabase, 100, 0, 0.7777);
        int roiRightUpX = getPercentToPoint(Constants.videoWidth, roiRightUpXPercent);

        double roiRightUpYPercent = loadAndCheckKeyNameValue(Constants.Right_Top_Y_Value, simpleDatabase, 100, 0, 0.375);
        int roiRightUpY = getPercentToPoint(Constants.videoHeight, roiRightUpYPercent);

        double roiRightDownXPercent = loadAndCheckKeyNameValue(Constants.Right_Bottom_X_Value, simpleDatabase, 100, 0, 1);
        int roiRightDownX = getPercentToPoint(Constants.videoWidth, roiRightDownXPercent);

        double roiRightDownYPercent = loadAndCheckKeyNameValue(Constants.Right_Bottom_Y_Value, simpleDatabase, 100, 0, 0.75);
        int roiRightDownY = getPercentToPoint(Constants.videoHeight, roiRightDownYPercent);

        result.setROILeftUpX(roiLeftUpX);
        result.setROILeftUpY(roiLeftUpY);
        result.setROILeftDownX(roiLeftDownX);
        result.setROILeftDownY(roiLeftDownY);
        result.setROIRightUpX(roiRightUpX);
        result.setROIRightUpY(roiRightUpY);
        result.setROIRightDownX(roiRightDownX);
        result.setROIRightDownY(roiRightDownY);

        double rightSlopesValue = loadAndCheckKeyNameValue(Constants.PosSlopes_Last_Value_Double, simpleDatabase, 10, 0, 0.5);
        double leftSlopesValue = loadAndCheckKeyNameValue(Constants.NegSlopes_Last_Value_Double, simpleDatabase, 0, -10, -0.5);
        int gausianChangeValue = (int)loadAndCheckKeyNameValue(Constants.Gausian_Last_Value_Int, simpleDatabase, 99, 0, 5);

        if(gausianChangeValue % 2 == 0)
        {
            gausianChangeValue = gausianChangeValue + 1;
        }

        double autoCannyValue = loadAndCheckKeyNameValue(Constants.AutoCanny_Last_Value_Float, simpleDatabase, 5, 1, 0.33);
        double rhoValue = loadAndCheckKeyNameValue(Constants.HoughLine_RHO_Last_Value_Double, simpleDatabase, 100, 1, 1.0);
        int thetaValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_THETA_Last_Value_Int, simpleDatabase, 100, 1, 1);
        int thresholdValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_Threshold_Last_Value_Int, simpleDatabase, 100, 1, 20);
        int minLineLengthValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_MinLineLength_Last_Value_Int, simpleDatabase, 100, 1, 5);
        int maxLineGapValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_MaxLineGap_Last_Value_Int, simpleDatabase, 100, 1, 5);

        result.setCheckPosSlopes(rightSlopesValue);
        result.setCheckNegSlopes(leftSlopesValue);
        result.setGausianKernelValue(gausianChangeValue);
        result.setAutoCannyValue(autoCannyValue);
        result.setRhoValue(rhoValue);
        result.setThetaValue(thetaValue);
        result.setTheresHoldValue(thresholdValue);
        result.setMinLineLen(minLineLengthValue);
        result.setMaxLineGap(maxLineGapValue);

        int maxForwardPowerValue = (int)loadAndCheckKeyNameValue(Constants.MAX_FORWARD_POWER, simpleDatabase, 100, 0, 50);
        result.setMaxForwardPower(maxForwardPowerValue);

        int maxLeftRightPowerValue = (int)loadAndCheckKeyNameValue(Constants.MAX_LEFT_RIGHT_POWER, simpleDatabase, 100, 0, 50);
        result.setMaxLeftRightPower(maxLeftRightPowerValue);

        int detectionErrorStopValue = (int)loadAndCheckKeyNameValue(Constants.DETECTION_ERROR_STOP, simpleDatabase, 100, 0, 5);
        result.setDetectionErrorStop(detectionErrorStopValue);

        boolean isFilterColor = loadAndCheckBooleanString(Constants.IS_FILTER_COLOR, simpleDatabase, "false");
        result.setFilterColor(isFilterColor);

        String filterColorLower = loadAndCheckIsColorCode(Constants.FILTER_COLOR_LOWER, simpleDatabase,"#FFFF64");
        result.setFilterColorLower(filterColorLower);

        String filterColorUpper = loadAndCheckIsColorCode(Constants.FILTER_COLOR_UPPER, simpleDatabase,"#646450");
        result.setFilterColorUpper(filterColorUpper);

        return result;
    }

    public ContentValues checkAndChangeSettingToContentValues(SimpleDatabase simpleDatabase, String strSaveName)
    {
        ContentValues write = new ContentValues();

        String settingKind = simpleDatabase.getStringValueByKey(Constants.KIND, "car");
        String settingName = simpleDatabase.getStringValueByKey(Constants.SETTING_NAME, "TmpName");

        write.put("Kind", settingKind);

        if(strSaveName == null || strSaveName.isEmpty())
        {
            write.put("Name", settingName);
        }
        else
        {
            write.put("Name", strSaveName);
        }

        ExpotentialMode expotentialMode = loadAndCheckExpotentialMode(simpleDatabase);

        write.put("ExpotentialModeCh1", expotentialMode.getChannelData(0));//塞入預設值
        write.put("ExpotentialModeCh2", expotentialMode.getChannelData(1));//塞入預設值
        write.put("ExpotentialModeCh3", expotentialMode.getChannelData(2));//塞入預設值
        write.put("ExpotentialModeCh4", expotentialMode.getChannelData(3));//塞入預設值
        write.put("ExpotentialModeCh5", expotentialMode.getChannelData(4));//塞入預設值
        write.put("ExpotentialModeCh6", expotentialMode.getChannelData(5));//塞入預設值
        write.put("ExpotentialModeCh7", expotentialMode.getChannelData(6));//塞入預設值
        write.put("ExpotentialModeCh8", expotentialMode.getChannelData(7));//塞入預設值

        MaxingConfig maxingConfig = loadAndCheckMaxingConfig(simpleDatabase);

        write.put("MixingConfigCH1", maxingConfig.getChannelData(0));//塞入預設值
        write.put("MixingConfigCH2", maxingConfig.getChannelData(1));//塞入預設值
        write.put("MixingConfigCH3", maxingConfig.getChannelData(2));//塞入預設值
        write.put("MixingConfigCH4", maxingConfig.getChannelData(3));//塞入預設值
        write.put("MixingConfigCH5", maxingConfig.getChannelData(4));//塞入預設值
        write.put("MixingConfigCH6", maxingConfig.getChannelData(5));//塞入預設值
        write.put("MixingConfigCH7", maxingConfig.getChannelData(6));//塞入預設值
        write.put("MixingConfigCH8", maxingConfig.getChannelData(7));//塞入預設值

        ServoReverse servoReverse = loadAndCheckServoReverse(simpleDatabase);

        write.put("ServoReverseCh1", servoReverse.getChannelData(0));//塞入預設值
        write.put("ServoReverseCh2", servoReverse.getChannelData(1));//塞入預設值
        write.put("ServoReverseCh3", servoReverse.getChannelData(2));//塞入預設值
        write.put("ServoReverseCh4", servoReverse.getChannelData(3));//塞入預設值
        write.put("ServoReverseCh5", servoReverse.getChannelData(4));//塞入預設值
        write.put("ServoReverseCh6", servoReverse.getChannelData(5));//塞入預設值
        write.put("ServoReverseCh7", servoReverse.getChannelData(6));//塞入預設值
        write.put("ServoReverseCh8", servoReverse.getChannelData(7));//塞入預設值

        ArrayList<StrimConfig> strimConfigArray = loadAndCheckStrimConfig(simpleDatabase);

        for(int i = 0 ; i < strimConfigArray.size() ; i++)
        {
            int channelIndex = i+1;

            write.put("StrimConfigCh"+channelIndex+"Upper", strimConfigArray.get(i).getUpper());//預設值
            write.put("StrimConfigCh"+channelIndex+"Lower", strimConfigArray.get(i).getLower());//預設值
            write.put("StrimConfigCh"+channelIndex+"Middle", strimConfigArray.get(i).getMiddle());//預設值
            write.put("StrimConfigCh"+channelIndex+"FailSafe", strimConfigArray.get(i).getFailSafe());//預設值
        }


        // 找出目前油門自動回復的狀態;
        String[] isAutoResetArray = loadAndIsAutoResetChannel(simpleDatabase);

        for(int i = 0 ; i < isAutoResetArray.length ; i++)
        {
            int channelIndex = i+1;
            write.put("isAutoResetToChannel"+channelIndex, isAutoResetArray[i]);//預設值
        }

        //27
        String screeanControlModevalue = loadAndCheckScreenControlModeValue(simpleDatabase);
        write.put("ScreenControlMode", screeanControlModevalue);//預設值

        //28
        String mixingConfigReverseValue = loadAndCheckMixingConfigReverseValue(simpleDatabase);
        write.put("MixingConfigReverse", mixingConfigReverseValue);

        String sensorInductionAngle = loadAndCheckSensorInductionAngle(simpleDatabase);
        write.put("sensorInductionAngle", sensorInductionAngle);//預設值

        String ifReverseCamera = loadAndCheckIfReverseCameraValue(simpleDatabase);
        write.put("IfReverseCamera", ifReverseCamera);//預設值

        String isAutoResetTHR = loadAndCheckIsAutoResetTHRValue(simpleDatabase);
        write.put("isAutoResetTHR", isAutoResetTHR);//預設值

        double roiLeftUpXPercent = loadAndCheckKeyNameValue(Constants.Left_Top_X_Value, simpleDatabase, 100, 0, 0.2083);
        double roiLeftUpYPercent = loadAndCheckKeyNameValue(Constants.Left_Top_Y_Value, simpleDatabase, 100, 0, 0.375);
        double roiLeftDownXPercent = loadAndCheckKeyNameValue(Constants.Left_Bottom_X_Value, simpleDatabase, 100, 0, 0);
        double roiLeftDownYPercent = loadAndCheckKeyNameValue(Constants.Left_Bottom_Y_Value, simpleDatabase, 100, 0, 0.75);
        double roiRightUpXPercent = loadAndCheckKeyNameValue(Constants.Right_Top_X_Value, simpleDatabase, 100, 0, 0.7777);
        double roiRightUpYPercent = loadAndCheckKeyNameValue(Constants.Right_Top_Y_Value, simpleDatabase, 100, 0, 0.375);
        double roiRightDownXPercent = loadAndCheckKeyNameValue(Constants.Right_Bottom_X_Value, simpleDatabase, 100, 0, 1);
        double roiRightDownYPercent = loadAndCheckKeyNameValue(Constants.Right_Bottom_Y_Value, simpleDatabase, 100, 0, 0.75);

        double rightSlopesValue = loadAndCheckKeyNameValue(Constants.PosSlopes_Last_Value_Double, simpleDatabase, 10, 0, 0.5);
        double leftSlopesValue = loadAndCheckKeyNameValue(Constants.NegSlopes_Last_Value_Double, simpleDatabase, 0, -10, -0.5);
        int gausianChangeValue = (int)loadAndCheckKeyNameValue(Constants.Gausian_Last_Value_Int, simpleDatabase, 99, 0, 5);

        if(gausianChangeValue % 2 == 0)
        {
            gausianChangeValue = gausianChangeValue + 1;
        }

        double autoCannyValue = loadAndCheckKeyNameValue(Constants.AutoCanny_Last_Value_Float, simpleDatabase, 5, 0, 0.33);
        double rhoValue = loadAndCheckKeyNameValue(Constants.HoughLine_RHO_Last_Value_Double, simpleDatabase, 100, 1, 1.0);
        int thetaValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_THETA_Last_Value_Int, simpleDatabase, 100, 1, 1);
        int thresholdValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_Threshold_Last_Value_Int, simpleDatabase, 100, 1, 20);
        int minLineLengthValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_MinLineLength_Last_Value_Int, simpleDatabase, 100, 1, 5);
        int maxLineGapValue = (int)loadAndCheckKeyNameValue(Constants.HoughLine_MaxLineGap_Last_Value_Int, simpleDatabase, 100, 1, 5);


        int maxForwardPowerValue = (int)loadAndCheckKeyNameValue(Constants.MAX_FORWARD_POWER, simpleDatabase, 100, 0, 50);
        int maxLeftRightPowerValue = (int)loadAndCheckKeyNameValue(Constants.MAX_LEFT_RIGHT_POWER, simpleDatabase, 100, 0, 50);
        int detectionErrorStopValue = (int)loadAndCheckKeyNameValue(Constants.DETECTION_ERROR_STOP, simpleDatabase, 100, 0, 5);

        boolean isFilterColor = loadAndCheckBooleanString(Constants.IS_FILTER_COLOR, simpleDatabase, "false");
        String filterColorLower = loadAndCheckIsColorCode(Constants.FILTER_COLOR_LOWER, simpleDatabase,"#FFFF64");
        String filterColorUpper = loadAndCheckIsColorCode(Constants.FILTER_COLOR_UPPER, simpleDatabase,"#646450");

        String detectionParamsSaveString = "{\n" +
                "  \"roiArea\": {\n" +
                "    \"leftTopXValue\": \""+roiLeftUpXPercent+"\",\n" +
                "    \"leftTopYValue\": \""+roiLeftUpYPercent+"\",\n" +
                "    \"leftBottomXValue\": \""+roiLeftDownXPercent+"\",\n" +
                "    \"leftBottomYValue\": \""+roiLeftDownYPercent+"\",\n" +
                "    \"rightTopXValue\": \""+roiRightUpXPercent+"\",\n" +
                "    \"rightTopYValue\": \""+roiRightUpYPercent+"\",\n" +
                "    \"rightBottomXValue\": \""+roiRightDownXPercent+"\",\n" +
                "    \"rightBottomYValue\": \""+roiRightDownYPercent+"\"\n" +
                "  },\n" +
                "  \"GausianLastValue\": \""+gausianChangeValue+"\",\n" +
                "  \"AutoCannyLastValue\": \""+autoCannyValue+"\",\n" +
                "  \"HoughLineRHOLastValue\": \""+rhoValue+"\",\n" +
                "  \"HoughLineTHETALastValue\": \""+thetaValue+"\",\n" +
                "  \"HoughLineThresholdLastValue\": \""+thresholdValue+"\",\n" +
                "  \"HoughLineMinLineLengthLastValue\": \""+minLineLengthValue+"\",\n" +
                "  \"HoughLineMaxLineGapLastValue\": \""+maxLineGapValue+"\",\n" +
                "  \"PosSlopesLastValue\": \""+rightSlopesValue+"\",\n" +
                "  \"NegSlopesLastValue\": \""+leftSlopesValue+"\",\n" +
                "  \"maxLeftRightPower\": \""+maxLeftRightPowerValue+"\",\n" +
                "  \"maxForwardPower\": \""+maxForwardPowerValue+"\",\n" +
                "  \"detectionErrorStop\": \""+detectionErrorStopValue+"\",\n" +
                "  \"isFillterColor\": \""+isFilterColor+"\",\n" +
                "  \"fillterColorLower\": \""+filterColorLower+"\",\n" +
                "  \"fillterColorUpper\": \""+filterColorUpper+"\"\n" +
                "}";

        write.put("detectionParams", detectionParamsSaveString);//預設值

        return write;
    }

    public CustomerSetting parseJSONObjectToCustomerSetting(String jsonString)
    {
        try {
            JSONObject customerSettingJSON = new JSONObject(jsonString);

            return parseJSONObjectToCustomerSetting(customerSettingJSON);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getCurrentDetectionJSON(SimpleDatabase simpleDatabase)
    {
        String settingName = simpleDatabase.getStringValueByKey(Constants.SETTING_NAME, "TmpName");

        ContentValues contentValues = checkAndChangeSettingToContentValues(simpleDatabase, settingName);

        String strDetectionParams = contentValues.getAsString("detectionParams");

        try {
            JSONObject detectionJSON = new JSONObject(strDetectionParams);
            return detectionJSON;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CustomerSetting parseJSONObjectToCustomerSetting(JSONObject jsonObject)
    {
        if(jsonObject != null)
        {
            Log.e(TAG, "jsonObject: "+jsonObject);

            CustomerSetting result = new CustomerSetting();
            result.setId(-1);

            String settingKind = jsonObject.optString(Constants.KIND, "car");
            String settingName = jsonObject.optString(Constants.SETTING_NAME, "TmpName");

            result.setKind(settingKind);
            result.setName(settingName);

            JSONObject roiAreaParameter = jsonObject.optJSONObject("roiArea");

            if(roiAreaParameter != null)
            {
                String strLeftTopXValue = roiAreaParameter.optString(Constants.Left_Top_X_Value, "0.2083");
                double roiLeftUpXPercent = Double.valueOf(checkDoubleValue(strLeftTopXValue, 100, 0, 0.2083));
                int roiLeftUpX = getPercentToPoint(Constants.videoWidth, roiLeftUpXPercent);

                Log.d(TAG, "strLeftTopXValue: "+strLeftTopXValue+" roiLeftUpXPercent: "+roiLeftUpXPercent+"  roiLeftUpX: "+roiLeftUpX);

                String strLeftTopYValue = roiAreaParameter.optString(Constants.Left_Top_Y_Value, "0.375");
                double roiLeftUpYPercent = Double.valueOf(checkDoubleValue(strLeftTopYValue, 100, 0, 0.375));
                int roiLeftUpY = getPercentToPoint(Constants.videoHeight, roiLeftUpYPercent);

                Log.d(TAG, "roiLeftUpYPercent: "+roiLeftUpYPercent+"  roiLeftUpY: "+roiLeftUpY);

                String strLeftBottomXValue = roiAreaParameter.optString(Constants.Left_Bottom_X_Value, "0");
                double roiLeftDownXPercent = Double.valueOf(checkDoubleValue(strLeftBottomXValue, 100, 0, 0));
                int roiLeftDownX = getPercentToPoint(Constants.videoWidth, roiLeftDownXPercent);

                Log.d(TAG, "roiLeftDownXPercent: "+roiLeftDownXPercent+"  roiLeftDownX: "+roiLeftDownX);

                String strLeftBottomYValue = roiAreaParameter.optString(Constants.Left_Bottom_Y_Value, "0.75");
                double roiLeftDownYPercent = Double.valueOf(checkDoubleValue(strLeftBottomYValue, 100, 0, 0.75));
                int roiLeftDownY = getPercentToPoint(Constants.videoHeight, roiLeftDownYPercent);

                Log.d(TAG, "roiLeftDownYPercent: "+roiLeftDownYPercent+"  roiLeftDownY: "+roiLeftDownY);

                String strRightTopXValue = roiAreaParameter.optString(Constants.Right_Top_X_Value, "0.7777");
                double roiRightUpXPercent = Double.valueOf(checkDoubleValue(strRightTopXValue, 100, 0, 0.7777));
                int roiRightUpX = getPercentToPoint(Constants.videoWidth, roiRightUpXPercent);

                Log.d(TAG, "roiRightUpXPercent: "+roiRightUpXPercent+"  roiRightUpX: "+roiRightUpX);

                String strRightTopYValue = roiAreaParameter.optString(Constants.Right_Top_Y_Value, "0.375");
                double roiRightUpYPercent = Double.valueOf(checkDoubleValue(strRightTopYValue, 100, 0, 0.375));
                int roiRightUpY = getPercentToPoint(Constants.videoHeight, roiRightUpYPercent);

                Log.d(TAG, "roiRightUpYPercent: "+roiRightUpYPercent+"  roiRightUpY: "+roiRightUpY);

                String strRightBottomXValue = roiAreaParameter.optString(Constants.Right_Bottom_X_Value, "1");
                double roiRightDownXPercent = Double.valueOf(checkDoubleValue(strRightBottomXValue, 100, 0, 1));
                int roiRightDownX = getPercentToPoint(Constants.videoWidth, roiRightDownXPercent);

                Log.d(TAG, "strRightBottomXValue: "+strRightBottomXValue+" roiRightDownXPercent: "+roiRightDownXPercent+"  roiRightDownX: "+roiRightDownX);

                String strRightBottomYValue = roiAreaParameter.optString(Constants.Right_Bottom_Y_Value, "0.75");
                double roiRightDownYPercent = Double.valueOf(checkDoubleValue(strRightBottomYValue, 100, 0, 0.75));
                int roiRightDownY = getPercentToPoint(Constants.videoHeight, roiRightDownYPercent);

                Log.d(TAG, "strRightBottomYValue: "+strRightBottomYValue+" roiRightDownYPercent: "+roiRightDownYPercent+"  roiRightDownY: "+roiRightDownY);

                result.setROILeftUpX(roiLeftUpX);
                result.setROILeftUpY(roiLeftUpY);
                result.setROILeftDownX(roiLeftDownX);
                result.setROILeftDownY(roiLeftDownY);
                result.setROIRightUpX(roiRightUpX);
                result.setROIRightUpY(roiRightUpY);
                result.setROIRightDownX(roiRightDownX);
                result.setROIRightDownY(roiRightDownY);
            }

            String strPosSlopesLastValue = jsonObject.optString(Constants.PosSlopes_Last_Value_Double, "0.5");
            double rightSlopesValue = Double.valueOf(checkDoubleValue(strPosSlopesLastValue, 10, 0, 0.5));

            String strNegSlopesLastValue = jsonObject.optString(Constants.NegSlopes_Last_Value_Double, "-0.5");
            double leftSlopesValue = Double.valueOf(checkDoubleValue(strNegSlopesLastValue, 0, -10, -0.5));

            String strGausianLastValue = jsonObject.optString(Constants.Gausian_Last_Value_Int, "5");
            int gausianChangeValue = Integer.valueOf(checkDoubleValue(strGausianLastValue, 99, 0, 5));

            if(gausianChangeValue % 2 == 0)
            {
                gausianChangeValue = gausianChangeValue + 1;
            }

            String strAutoCannyLastValue = jsonObject.optString(Constants.AutoCanny_Last_Value_Float, "0.33");
            double autoCannyValue = Double.valueOf(checkDoubleValue(strAutoCannyLastValue, 5, 1, 0.33));

            String strHoughLineRHOLastValueDouble = jsonObject.optString(Constants.HoughLine_RHO_Last_Value_Double, "1");
            double rhoValue = Double.valueOf(checkDoubleValue(strHoughLineRHOLastValueDouble, 100, 1, 1.0));

            String strHoughLineTHETALastValue = jsonObject.optString(Constants.HoughLine_THETA_Last_Value_Int, "1");
            int thetaValue = Integer.valueOf(checkDoubleValue(strHoughLineTHETALastValue, 100, 1, 1));

            String strHoughLineThresholdLastValue = jsonObject.optString(Constants.HoughLine_Threshold_Last_Value_Int, "20");
            int thresholdValue = Integer.valueOf(checkDoubleValue(strHoughLineThresholdLastValue, 100, 1, 20));

            String strHoughLineMinLineLengthLastValue = jsonObject.optString(Constants.HoughLine_MinLineLength_Last_Value_Int, "5");
            int minLineLengthValue = Integer.valueOf(checkDoubleValue(strHoughLineMinLineLengthLastValue, 100, 1, 5));

            String strHoughLineMaxLineGapLastValue = jsonObject.optString(Constants.HoughLine_MaxLineGap_Last_Value_Int, "5");
            int maxLineGapValue = Integer.valueOf(checkDoubleValue(strHoughLineMaxLineGapLastValue, 100, 1, 5));

            result.setCheckPosSlopes(rightSlopesValue);
            result.setCheckNegSlopes(leftSlopesValue);
            result.setGausianKernelValue(gausianChangeValue);
            result.setAutoCannyValue(autoCannyValue);
            result.setRhoValue(rhoValue);
            result.setThetaValue(thetaValue);
            result.setTheresHoldValue(thresholdValue);
            result.setMinLineLen(minLineLengthValue);
            result.setMaxLineGap(maxLineGapValue);

            String strMaxForwardPowerValue = jsonObject.optString(Constants.MAX_FORWARD_POWER, "50");
            int maxForwardPowerValue = Integer.valueOf(checkDoubleValue(strMaxForwardPowerValue, 100, 0, 50));
            result.setMaxForwardPower(maxForwardPowerValue);

            String strMaxLeftRightPowerValue = jsonObject.optString(Constants.MAX_LEFT_RIGHT_POWER, "50");
            int maxLeftRightPowerValue = Integer.valueOf(checkDoubleValue(strMaxLeftRightPowerValue, 100, 0, 50));
            result.setMaxLeftRightPower(maxLeftRightPowerValue);

            String strDetectionErrorStopValue = jsonObject.optString(Constants.DETECTION_ERROR_STOP, "5");
            int detectionErrorStopValue = Integer.valueOf(checkDoubleValue(strDetectionErrorStopValue, 100, 0, 5));
            result.setDetectionErrorStop(detectionErrorStopValue);

            String strIsFilterColor = jsonObject.optString(Constants.IS_FILTER_COLOR, "false");
            boolean isFilterColor = Boolean.valueOf(checkIsBooleanString(strIsFilterColor, "false"));
            result.setFilterColor(isFilterColor);

            String strFilterColorLower = jsonObject.optString(Constants.FILTER_COLOR_LOWER, "#FFFF64");
            String filterColorLower = checkIsColorCode(strFilterColorLower, "#FFFF64");
            result.setFilterColorLower(filterColorLower);

            String strFilterColorUpper = jsonObject.optString(Constants.FILTER_COLOR_UPPER, "#646450");
            String filterColorUpper = checkIsColorCode(strFilterColorUpper, "#646450");
            result.setFilterColorUpper(filterColorUpper);

            return result;
        }
        else
        {
            return null;
        }
    }

    /**
     * 這裡有關ROI的部分都沒有轉換成百分比，所以一定要確定存入資料庫的ROI己轉換成百分比了
     * @param simpleDatabase
     * @param customerSetting
     */
    public void changeSettingToContentValues(SimpleDatabase simpleDatabase, CustomerSetting customerSetting)
    {
        simpleDatabase.setValueByKey(Constants.KIND, customerSetting.getKind());
        simpleDatabase.setValueByKey(Constants.SETTING_NAME, customerSetting.getName());

        ExpotentialMode expotentialMode = customerSetting.getExpotentialModeItem();

        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(1), expotentialMode.getChannelData(0));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(2), expotentialMode.getChannelData(1));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(3), expotentialMode.getChannelData(2));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(4), expotentialMode.getChannelData(3));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(5), expotentialMode.getChannelData(4));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(6), expotentialMode.getChannelData(5));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(7), expotentialMode.getChannelData(6));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getExpotentialModeKeyName(8), expotentialMode.getChannelData(7));//塞入預設值

        MaxingConfig maxingConfig = customerSetting.getMaxingConfigItem();

        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(1), maxingConfig.getChannelData(0));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(2), maxingConfig.getChannelData(1));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(3), maxingConfig.getChannelData(2));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(4), maxingConfig.getChannelData(3));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(5), maxingConfig.getChannelData(4));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(6), maxingConfig.getChannelData(5));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(7), maxingConfig.getChannelData(6));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getMixingConfigKeyName(8), maxingConfig.getChannelData(7));//塞入預設值

        ServoReverse servoReverse = customerSetting.getServoReverseItem();

        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(1), servoReverse.getChannelData(0));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(2), servoReverse.getChannelData(1));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(3), servoReverse.getChannelData(2));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(4), servoReverse.getChannelData(3));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(5), servoReverse.getChannelData(4));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(6), servoReverse.getChannelData(5));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(7), servoReverse.getChannelData(6));//塞入預設值
        simpleDatabase.setValueByKey(Constants.getServoReverseKeyName(8), servoReverse.getChannelData(7));//塞入預設值

        for(int i = 0 ; i < 8 ; i++)
        {
            int channelIndex = i+1;

            simpleDatabase.setValueByKey(Constants.getStrimConfigUpperKeyName(channelIndex), customerSetting.getStrimConfigItem(i).getUpper());//預設值
            simpleDatabase.setValueByKey(Constants.getStrimConfigLowerKeyName(channelIndex), customerSetting.getStrimConfigItem(i).getLower());//預設值
            simpleDatabase.setValueByKey(Constants.getStrimConfigMiddleKeyName(channelIndex), customerSetting.getStrimConfigItem(i).getMiddle());//預設值
            simpleDatabase.setValueByKey(Constants.getStrimConfigFailSafeKeyName(channelIndex), customerSetting.getStrimConfigItem(i).getFailSafe());//預設值
        }


        // 找出目前油門自動回復的狀態;
        for(int i = 0 ; i < 8 ; i++)
        {
            int channelIndex = i+1;
            simpleDatabase.setValueByKey(Constants.getIsAutoResetToChannelKeyName(channelIndex), customerSetting.getIsAutoResetChannel(i));//預設值
        }

        //27
        simpleDatabase.setValueByKey(Constants.SCREEN_CONTROL_MODE, customerSetting.getScreeanControlMode());//預設值

        //28
        String mixingConfigReverseValue = "off";

        if(customerSetting.getMixingConfigReverse())
        {
            mixingConfigReverseValue = "ON";
        }
        else
        {
            mixingConfigReverseValue = "off";
        }

        simpleDatabase.setValueByKey(Constants.MIXING_CONFIG_REVERSE, mixingConfigReverseValue);

        simpleDatabase.setValueByKey(Constants.SENSOR_INDUCTION_ANGLE, customerSetting.getSensorInductionAngle());//預設值

        simpleDatabase.setValueByKey(Constants.IF_REVERSE_CAMERA, customerSetting.getIfReverseCamera());//預設值

        simpleDatabase.setValueByKey(Constants.IS_AUTO_RESET_THR, customerSetting.getIsAutoResetTHR());//預設值

//        simpleDatabase.setValueByKey(Constants.Left_Top_X_Value, "" + ((float)customerSetting.getROILeftUpX() / (float)CVCameraWrapper.videoWidth));
//        simpleDatabase.setValueByKey(Constants.Left_Top_Y_Value, "" + ((float)customerSetting.getROILeftUpY() / (float)CVCameraWrapper.videoHeight));
//        simpleDatabase.setValueByKey(Constants.Left_Bottom_X_Value, "" + ((float)customerSetting.getROILeftDownX() / (float)CVCameraWrapper.videoWidth));
//        simpleDatabase.setValueByKey(Constants.Left_Bottom_Y_Value, "" + ((float)customerSetting.getROILeftDownY() / (float)CVCameraWrapper.videoHeight));
//        simpleDatabase.setValueByKey(Constants.Right_Top_X_Value, "" + ((float)customerSetting.getROIRightUpX() / (float)CVCameraWrapper.videoWidth));
//        simpleDatabase.setValueByKey(Constants.Right_Top_Y_Value, "" + ((float)customerSetting.getROIRightUpY() / (float)CVCameraWrapper.videoHeight));
//        simpleDatabase.setValueByKey(Constants.Right_Bottom_X_Value, "" + ((float)customerSetting.getROIRightDownX() / (float)CVCameraWrapper.videoWidth));
//        simpleDatabase.setValueByKey(Constants.Right_Bottom_Y_Value, "" + ((float)customerSetting.getROIRightDownY() / (float)CVCameraWrapper.videoHeight));
//
//        simpleDatabase.setValueByKey(Constants.PosSlopes_Last_Value_Double, "" + customerSetting.getCheckPosSlopes());
//        simpleDatabase.setValueByKey(Constants.NegSlopes_Last_Value_Double, "" + customerSetting.getCheckNegSlopes());
//        simpleDatabase.setValueByKey(Constants.Gausian_Last_Value_Int, "" + customerSetting.getGausianKernelValue());
//
//        simpleDatabase.setValueByKey(Constants.AutoCanny_Last_Value_Float, "" + customerSetting.getAutoCannyValue());
//        simpleDatabase.setValueByKey(Constants.HoughLine_RHO_Last_Value_Double, "" + customerSetting.getRhoValue());
//        simpleDatabase.setValueByKey(Constants.HoughLine_THETA_Last_Value_Int, "" + customerSetting.getThetaValue());
//        simpleDatabase.setValueByKey(Constants.HoughLine_Threshold_Last_Value_Int, "" + customerSetting.getTheresHoldValue());
//        simpleDatabase.setValueByKey(Constants.HoughLine_MinLineLength_Last_Value_Int, "" + customerSetting.getMinLineLen());
//        simpleDatabase.setValueByKey(Constants.HoughLine_MaxLineGap_Last_Value_Int, "" + customerSetting.getMaxLineGap());
//        simpleDatabase.setValueByKey(Constants.MAX_FORWARD_POWER, "" + customerSetting.getMaxForwardPower());
//        simpleDatabase.setValueByKey(Constants.MAX_LEFT_RIGHT_POWER, "" + customerSetting.getMaxLeftRightPower());
//        simpleDatabase.setValueByKey(Constants.DETECTION_ERROR_STOP, "" + customerSetting.getDetectionErrorStop());
//
//        simpleDatabase.setValueByKey(Constants.IS_FILTER_COLOR, "" + customerSetting.isFilterColor());
//        simpleDatabase.setValueByKey(Constants.FILTER_COLOR_LOWER, "" + customerSetting.getFilterColorLower());
//        simpleDatabase.setValueByKey(Constants.FILTER_COLOR_UPPER, "" + customerSetting.getFilterColorUpper());

        setCustomSettingDetectionParamsToSimpleDataBase(simpleDatabase, customerSetting);
    }

    public void setCustomSettingDetectionParamsToSimpleDataBase(SimpleDatabase simpleDatabase, CustomerSetting customerSetting)
    {
        simpleDatabase.setValueByKey(Constants.Left_Top_X_Value, "" + ((float)customerSetting.getROILeftUpX() / (float)Constants.videoWidth));
        simpleDatabase.setValueByKey(Constants.Left_Top_Y_Value, "" + ((float)customerSetting.getROILeftUpY() / (float)Constants.videoHeight));
        simpleDatabase.setValueByKey(Constants.Left_Bottom_X_Value, "" + ((float)customerSetting.getROILeftDownX() / (float)Constants.videoWidth));
        simpleDatabase.setValueByKey(Constants.Left_Bottom_Y_Value, "" + ((float)customerSetting.getROILeftDownY() / (float)Constants.videoHeight));
        simpleDatabase.setValueByKey(Constants.Right_Top_X_Value, "" + ((float)customerSetting.getROIRightUpX() / (float)Constants.videoWidth));
        simpleDatabase.setValueByKey(Constants.Right_Top_Y_Value, "" + ((float)customerSetting.getROIRightUpY() / (float)Constants.videoHeight));
        simpleDatabase.setValueByKey(Constants.Right_Bottom_X_Value, "" + ((float)customerSetting.getROIRightDownX() / (float)Constants.videoWidth));
        simpleDatabase.setValueByKey(Constants.Right_Bottom_Y_Value, "" + ((float)customerSetting.getROIRightDownY() / (float)Constants.videoHeight));

        simpleDatabase.setValueByKey(Constants.PosSlopes_Last_Value_Double, "" + customerSetting.getCheckPosSlopes());
        simpleDatabase.setValueByKey(Constants.NegSlopes_Last_Value_Double, "" + customerSetting.getCheckNegSlopes());
        simpleDatabase.setValueByKey(Constants.Gausian_Last_Value_Int, "" + customerSetting.getGausianKernelValue());

        simpleDatabase.setValueByKey(Constants.AutoCanny_Last_Value_Float, "" + customerSetting.getAutoCannyValue());
        simpleDatabase.setValueByKey(Constants.HoughLine_RHO_Last_Value_Double, "" + customerSetting.getRhoValue());
        simpleDatabase.setValueByKey(Constants.HoughLine_THETA_Last_Value_Int, "" + customerSetting.getThetaValue());
        simpleDatabase.setValueByKey(Constants.HoughLine_Threshold_Last_Value_Int, "" + customerSetting.getTheresHoldValue());
        simpleDatabase.setValueByKey(Constants.HoughLine_MinLineLength_Last_Value_Int, "" + customerSetting.getMinLineLen());
        simpleDatabase.setValueByKey(Constants.HoughLine_MaxLineGap_Last_Value_Int, "" + customerSetting.getMaxLineGap());
        simpleDatabase.setValueByKey(Constants.MAX_FORWARD_POWER, "" + customerSetting.getMaxForwardPower());
        simpleDatabase.setValueByKey(Constants.MAX_LEFT_RIGHT_POWER, "" + customerSetting.getMaxLeftRightPower());
        simpleDatabase.setValueByKey(Constants.DETECTION_ERROR_STOP, "" + customerSetting.getDetectionErrorStop());

        simpleDatabase.setValueByKey(Constants.IS_FILTER_COLOR, "" + customerSetting.isFilterColor());
        simpleDatabase.setValueByKey(Constants.FILTER_COLOR_LOWER, "" + customerSetting.getFilterColorLower());
        simpleDatabase.setValueByKey(Constants.FILTER_COLOR_UPPER, "" + customerSetting.getFilterColorUpper());
    }

    public static ExpotentialMode loadAndCheckExpotentialMode(SimpleDatabase simpleDatabase)
    {
        String[] expotentialModeArray = new String[Constants.TOTAL_CHANNELS];

        for(int i = 0 ; i < Constants.TOTAL_CHANNELS ; i++)
        {
            int channelIndex = i+1;

            String expotentialModeKeyName = Constants.getExpotentialModeKeyName(channelIndex);

            String expotentialValue = simpleDatabase.getStringValueByKey(expotentialModeKeyName, "1");

            String afterCheckValue = checkExpotentialValue(expotentialValue);

            expotentialModeArray[i] = afterCheckValue;
        }

        return ExpotentialMode.getInstance(expotentialModeArray);
    }

    public static String checkExpotentialValue(String strValue)
    {
        if(strValue.contentEquals("3")
                || strValue.contentEquals("2")
                || strValue.contentEquals("1")
                || strValue.contentEquals("4")
                || strValue.contentEquals("5"))
        {
            return strValue;
        }
        else
        {
            return "1";
        }
    }

    public static MaxingConfig loadAndCheckMaxingConfig(SimpleDatabase simpleDatabase)
    {
        String[] maxingConfigArray = new String[Constants.TOTAL_CHANNELS];

        for(int i = 0 ; i < Constants.TOTAL_CHANNELS ; i++)
        {
            int channelIndex = i+1;

            String mixingConfigKeyName = Constants.getMixingConfigKeyName(channelIndex);

            String mixingConfigValue = simpleDatabase.getStringValueByKey(mixingConfigKeyName, "1");

            String afterCheckValue = checkMixingConfigValue(mixingConfigValue);

            maxingConfigArray[i] = afterCheckValue;
        }

        return MaxingConfig.getInstance(maxingConfigArray);
    }

    public static String checkMixingConfigValue(String strValue)
    {
        return strValue;
    }

    public static ServoReverse loadAndCheckServoReverse(SimpleDatabase simpleDatabase)
    {
        String[] servoReverseArray = new String[Constants.TOTAL_CHANNELS];

        for(int i = 0 ; i < Constants.TOTAL_CHANNELS ; i++)
        {
            int channelIndex = i+1;

            String servoReverseKeyName = Constants.getServoReverseKeyName(channelIndex);

            String servoReverseValue = simpleDatabase.getStringValueByKey(servoReverseKeyName, "1");

            String afterCheckValue = checkServoReverseValue(servoReverseValue);

            servoReverseArray[i] = afterCheckValue;
        }

        return ServoReverse.getInstance(servoReverseArray);
    }

    public static String checkServoReverseValue(String strValue)
    {
        if(strValue.contentEquals("Yes") || strValue.contentEquals("NO")){
            return strValue;
        }
        else{
            return "NO";
        }
    }

    public static ArrayList<StrimConfig> loadAndCheckStrimConfig(SimpleDatabase simpleDatabase)
    {
        ArrayList<StrimConfig> strimConfigArray = new ArrayList<StrimConfig>();

        for(int i = 0 ; i < Constants.TOTAL_CHANNELS ; i++)
        {
            int channelIndex = i+1;

            String upperKeyName = Constants.getStrimConfigUpperKeyName(channelIndex);
            String lowerKeyName = Constants.getStrimConfigLowerKeyName(channelIndex);
            String middleKeyKeyName = Constants.getStrimConfigMiddleKeyName(channelIndex);
            String failSafeKeyName = Constants.getStrimConfigFailSafeKeyName(channelIndex);

            String upperValue = simpleDatabase.getStringValueByKey(upperKeyName, "2000");
            String lowerValue = simpleDatabase.getStringValueByKey(lowerKeyName, "1000");
            String middleValue = simpleDatabase.getStringValueByKey(middleKeyKeyName, "1500");
            String failSafeValue = simpleDatabase.getStringValueByKey(failSafeKeyName, "1500");

            String afterCheckUpperValue = checkChannelUpperValue(upperValue);
            String afterCheckLowerValue = checkChannelLowerValue(lowerValue);
            String afterCheckMiddleValue = checkChannelMiddleValue(middleValue);
            String afterCheckFailSafeValue = checkChannelFailSaveValue(failSafeValue);

            StrimConfig strimConfig = new StrimConfig(("ch" + channelIndex)
                    , afterCheckUpperValue
                    , afterCheckLowerValue
                    , afterCheckMiddleValue
                    , afterCheckFailSafeValue);

            strimConfigArray.add(strimConfig);
        }

        return strimConfigArray;
    }

    public static String checkChannelUpperValue(String strValue)
    {
        return checkChannelValue(strValue, 2150, 1650, 2000);
    }

    public static String checkChannelMiddleValue(String strValue)
    {
        return checkChannelValue(strValue, 1650, 1350, 1500);
    }

    public static String checkChannelLowerValue(String strValue)
    {
        return checkChannelValue(strValue, 1350, 850, 1000);
    }

    public static String checkChannelFailSaveValue(String strValue)
    {
        return checkChannelValue(strValue, 2150, 850, 1500);
    }

    public static String checkChannelValue(String strValue, int upperValue, int lowerValue, int defaultValue)
    {
        try
        {
            int value = Integer.valueOf(strValue);

            if(value >= lowerValue && value <= upperValue)
            {
                return strValue;
            }
            else
            {
                return ("" + defaultValue);
            }
        }
        catch (Exception e)
        {
            return ("" + defaultValue);
        }
    }

    public static String[] loadAndIsAutoResetChannel(SimpleDatabase simpleDatabase)
    {
        String[] isAutoResetArray = new String[Constants.TOTAL_CHANNELS];

        for(int i = 0 ; i < Constants.TOTAL_CHANNELS ; i++)
        {
            int channelIndex = i+1;

            String isAutoResetKeyName = Constants.getIsAutoResetToChannelKeyName(channelIndex);

            String isAutoResetValue = simpleDatabase.getStringValueByKey(isAutoResetKeyName, "0");

            String afterIsAutoResetValue = checkIsAutoResetValue(isAutoResetValue);

            isAutoResetArray[i] = afterIsAutoResetValue;
        }

        return isAutoResetArray;
    }

    public static String checkIsAutoResetValue(String strValue)
    {
        if(strValue.contentEquals("1") || strValue.contentEquals("0"))
        {
            return strValue;
        }
        else
        {
            return "0";
        }
    }

    public static String loadAndCheckScreenControlModeValue(SimpleDatabase simpleDatabase)
    {
        String screenControlModeValue = simpleDatabase.getStringValueByKey(Constants.SCREEN_CONTROL_MODE, "301");

        String afterCheckValue = checkScreenControlModeValue(screenControlModeValue);

        return afterCheckValue;
    }

    public static String checkScreenControlModeValue(String strValue)
    {
        if(strValue.contentEquals("301") || strValue.contentEquals("302"))
        {
            return strValue;
        }
        else
        {
            return "302";
        }
    }

    public static String loadAndCheckMixingConfigReverseValue(SimpleDatabase simpleDatabase)
    {
        String screenControlModeValue = simpleDatabase.getStringValueByKey(Constants.MIXING_CONFIG_REVERSE, "off");

        String afterCheckValue = checkMixingConfigReverseValue(screenControlModeValue);

        return afterCheckValue;
    }

    public static String checkMixingConfigReverseValue(String strValue)
    {
        if(strValue.contentEquals("off") || strValue.contentEquals("ON"))
        {
            return strValue;
        }
        else
        {
            return "off";
        }
    }

    public static String loadAndCheckIsAutoResetTHRValue(SimpleDatabase simpleDatabase)
    {
        String isAutoResetTHRValue = simpleDatabase.getStringValueByKey(Constants.IS_AUTO_RESET_THR, "1");

        String afterCheckValue = checkIsAutoResetTHRValue(isAutoResetTHRValue);

        return afterCheckValue;
    }

    public static String checkIsAutoResetTHRValue(String strValue)
    {
        if(strValue.contentEquals("0") || strValue.contentEquals("1"))
        {
            return strValue;
        }
        else
        {
            return "1";
        }
    }

    public static String loadAndCheckSensorInductionAngle(SimpleDatabase simpleDatabase)
    {
        String sensorInductionAngleValue = simpleDatabase.getStringValueByKey(Constants.SENSOR_INDUCTION_ANGLE, "90");

        String afterCheckValue = checkSensorInductionAngleValue(sensorInductionAngleValue);

        return afterCheckValue;
    }

    private static String checkSensorInductionAngleValue(String strValue)
    {
        return checkChannelValue(strValue, 180, 0, 90);
    }

    public static String loadAndCheckIfReverseCameraValue(SimpleDatabase simpleDatabase)
    {
        String ifReverseCameraValue = simpleDatabase.getStringValueByKey(Constants.IF_REVERSE_CAMERA, "0");

        String afterCheckValue = checkIfReverseCameraValue(ifReverseCameraValue);

        return afterCheckValue;
    }

    public static String checkIfReverseCameraValue(String strValue)
    {
        if(strValue.contentEquals("0") || strValue.contentEquals("1"))
        {
            return strValue;
        }
        else
        {
            return "0";
        }
    }

    public double loadAndCheckKeyNameValue(String strKeyName, SimpleDatabase simpleDatabase, double upperValue, double lowerValue, double defaultValue)
    {
        String strValue = simpleDatabase.getStringValueByKey(strKeyName, (""+defaultValue));

        String afterCheckValue = checkDoubleValue(strValue, upperValue, lowerValue, defaultValue);

        return Double.valueOf(afterCheckValue);
    }

    public static String checkDoubleValue(String strValue, double upperValue, double lowerValue, double defaultValue)
    {
        try {
            double doubleValue = Double.valueOf(strValue);

            if(doubleValue <= upperValue && upperValue >= lowerValue){
                return strValue;
            }
            else
            {
                return ("" + defaultValue);
            }
        }
        catch (Exception e){
            return ("" + defaultValue);
        }
    }

    public String loadAndCheckIsColorCode(String strKeyName, SimpleDatabase simpleDatabase, String defaultColorCodeString)
    {
        String strValue = simpleDatabase.getStringValueByKey(strKeyName, defaultColorCodeString);

        String afterCheckValue = checkIsColorCode(strValue, defaultColorCodeString);

        return afterCheckValue;
    }

    public String checkIsColorCode(String strValue, String defaultColorCodeString)
    {
        try {
            int colorCode = Color.parseColor(strValue);
            return strValue;
        }
        catch (Exception e)
        {
            return defaultColorCodeString;
        }
    }

    public boolean loadAndCheckBooleanString(String strKeyName, SimpleDatabase simpleDatabase, String defaultBooleanString)
    {
        String strValue = simpleDatabase.getStringValueByKey(strKeyName, defaultBooleanString);

        String afterCheckValue = checkIsBooleanString(strValue, defaultBooleanString);

        return Boolean.valueOf(afterCheckValue);
    }

    public String checkIsBooleanString(String strValue, String defaultBooleanString)
    {
        if(strValue.contentEquals("true") || strValue.contentEquals("false"))
        {
            return strValue;
        }
        else
        {
            return defaultBooleanString;
        }
    }

    private int getPercentToPoint(int base, double percent)
    {
        return (int)((double)(base) * percent);
    }
}
