package com.v7idea.tool;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class CustomerSetting implements Serializable {
    private static final String TAG = CustomerSetting.class.getSimpleName();

    private ExpotentialMode CExpotentialMode;
    private ArrayList<StrimConfig> StrimArrayList;
    private MaxingConfig CMaxingConfig;
    private ServoReverse CServoReverse;
    private String ScreeanControlMode;
    private String Kind;
    private String Name;
    private String MixingConfigReverse = "NotThing";
    private int id;
    private String autoResetTHR = "0";

    private String sensorInductionAngle = "90";

    private String ifReverseCamera = "0";

    private String isAutoResetTHR = "1";

    private String[] isAutoResetChannel = null;

    private int roiLeftUpX = 75;
    private int roiLeftUpY = 90;

    private int roiLeftDownX = 0;
    private int roiLeftDownY = 180;

    private int roiRightUpX = 280;
    private int roiRightUpY = 90;

    private int roiRightDownX = 360;
    private int roiRightDownY = 180;

    private double checkPosSlopes = 0.5;
    private double checkNegSlopes = -0.5;
    private int gausianKernelValue = 5;
    private double autoCannyValue = 0.33;
    private double rho = 1.0;
    private int thetaValue = 1;
    private int theresHoldValue = 20;
    private int minLineLen = 5;
    private int maxLineGap = 5;
    private int maxLeftRightPower = 50;
    private int maxForwardPower = 50;
    private int detectionErrorStop = 5;

    private boolean isFilterColor = false;
    private String filterColorLower = "#ffffff";
    private String filterColorUpper = "#ffffff";

    public CustomerSetting()
    {
        this.StrimArrayList = new ArrayList<StrimConfig>();
        this.id = 0;
    }

    public CustomerSetting(String Name,
                           String ScreeanControlMode, String MixingConfigReverse,
                           ExpotentialMode CExpotentialMode,
                           MaxingConfig CMaxingConfig,
                           ServoReverse CServoReverse, String thisTHRMode)
    {
        this.CExpotentialMode = CExpotentialMode;
        this.StrimArrayList = new ArrayList<StrimConfig>();
        this.CMaxingConfig = CMaxingConfig;
        this.CServoReverse = CServoReverse;
        this.ScreeanControlMode = ScreeanControlMode;
        this.MixingConfigReverse = MixingConfigReverse;
        this.Name = Name;
        this.autoResetTHR = thisTHRMode;

    }

    public void StrimConfigAdd(StrimConfig obj)
    {
        StrimArrayList.add(obj);
    }

    public boolean setStrimConfigArray(ArrayList<StrimConfig> strimArrayList)
    {
        if(strimArrayList != null && strimArrayList.size() == 8)
        {
            if(StrimArrayList != null)
            {
                StrimArrayList.clear();
                StrimArrayList = null;
            }

            StrimArrayList = strimArrayList;

            return true;
        }
        else
        {
            return false;
        }
    }

    public Channel[] getTotalChannels()
    {
        Channel[] channels = new Channel[Constants.TOTAL_CHANNELS];

        for(int i = 0 ; i < Constants.TOTAL_CHANNELS ; i++)
        {
            channels[i] = getChannel(i);
        }

        return channels;
    }

    public Channel getChannel(int intChannelIndex)
    {
        StrimConfig strimConfig = getStrimConfigItem(intChannelIndex);

        if(strimConfig != null)
        {
            String Expotenail = getExpotentialModeItem().getChannelData(intChannelIndex);
            float Upper = Float.valueOf(getStrimConfigItem(intChannelIndex).getUpper());
            float Middle = Float.valueOf(getStrimConfigItem(intChannelIndex).getMiddle());
            float Lower = Float.valueOf(getStrimConfigItem(intChannelIndex).getLower());
            float failSaveData = Float.valueOf(getStrimConfigItem(intChannelIndex).getFailSafe());
            boolean isMixingConfigReverse = getMixingConfigReverse();

            boolean[] servoReverseData = getChannelsReverse();

//			Log.d(TAG,"Expotenail:   "+Expotenail);
            DebugLog.d(TAG,"Upper:   "+Upper);
			DebugLog.d(TAG,"Middle:   "+Middle);
            DebugLog.d(TAG,"Lower:   "+Lower);
// 			Log.d(TAG","ServoReverseData  Channel " + i + ";"+ServoReverseData[i]);
//			Log.d(TAG,"MCReverse:   "+MCReverse);

            Channel channel = new Channel((intChannelIndex+1)
                    , ("ch"+(intChannelIndex+1))
                    , Lower
                    , Middle
                    , Upper
                    , Integer.valueOf(Expotenail)
                    , servoReverseData[intChannelIndex]
                    , isMixingConfigReverse
                    , failSaveData);

            return channel;
        }

        return null;
    }



//    /**
//     * 將要使用的設定(CustmerSetting)載入這個方法
//     */
//    private void getChannels()
//    {
//        if(mx != null)
//        {
//            for(int i = 0 ; i < SumOfChannel ; i++)
//            {
//                String channelData = mx.getMaxingConfigItem().getChannelData(i);
//                // Log.d("Mixing","channel"+(i+1)+":   "+ channelData);
//
//                if(!channelData.equals("off"))
//                {
//                    for(int j = 0 ; j < SumOfChannel ; j++)
//                    {
//                        if(channelData.equals("CH"+(j+1)))
//                        {
//                            channel[i].setChannelChild(channel[j]);
//                            // Log.d("Mixing","channel"+(i+1)+":   "+ "channel"+(j+1));
//                        }
//                    }
//                }
//            }
//        }
//    }

    public boolean[] getChannelsReverse()
    {
        boolean[] reverse = new boolean[StrimArrayList.size()];

        for(int i = 0 ; i < reverse.length ; i++)
        {
            String Stmp = getServoReverseItem().getChannelData(i);
            if(Stmp.contentEquals("Yes"))
            {
                reverse[i] = true;

            }
            else
            {
                reverse[i] = false;
            }
//             DebugLog.d("Channel Reverse","Channel:" + i + ";Reverse:" + reverse[i] );
        }

        return reverse;
    }

    public StrimConfig getStrimConfigItem(int channel)
    {
//        DebugLog.d("getStrimConfigItem", "channel:" + channel);
        return StrimArrayList.get(channel);
    }

    public int getSizeOfStrimConfigItem()
    {
        return StrimArrayList.size();
    }

    public ExpotentialMode getExpotentialModeItem()
    {
        return CExpotentialMode;
    }

    public void setExpotentialModeItem(ExpotentialMode em)
    {
        CExpotentialMode = em;
    }

    public MaxingConfig getMaxingConfigItem()
    {
        return CMaxingConfig;
    }

    public void setMaxingConfigItem(MaxingConfig mc)
    {
        CMaxingConfig = mc;
    }

    public ServoReverse getServoReverseItem()
    {
        return CServoReverse;
    }

    public void setServoReverseItem(ServoReverse sr)
    {
        CServoReverse = sr;
    }

    public String getScreeanControlMode()
    {
        return ScreeanControlMode;
    }

    public void setScreeanControlMode(String screeanControlMode)
    {
        ScreeanControlMode = screeanControlMode;
    }

    public String getName()
    {
        return Name;
    }

    public void setName(String name)
    {
        Name = name;
    }

    public String getKind()
    {
        return Kind;
    }

    public void setKind(String kind)
    {
        Kind = kind;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getMixingConfigReverse() {
        if(MixingConfigReverse.contentEquals("ON"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setMixingConfigReverse(String mixingReverse) {
        MixingConfigReverse = mixingReverse;
    }

    public String getAutoResetTHRState() {
        return autoResetTHR;
    }

    public void setAutoResetTHRState(String thisState) {

        autoResetTHR = thisState;

    }

    public String getSensorInductionAngle()
    {
        return sensorInductionAngle;
    }

    public void setSensorInductionAngle(String sensorInductionAngle)
    {
        this.sensorInductionAngle = sensorInductionAngle;
    }

    public String getIsAutoResetChannel(int index)
    {
        return isAutoResetChannel[index];
    }

    public void setIsAutoResetChannel(String[] isAutoResetChannelArray)
    {
        this.isAutoResetChannel = isAutoResetChannelArray;
    }

    public String getIfReverseCamera() {
        return ifReverseCamera;
    }

    public void setIfReverseCamera(String ifReverseCamera) {
        this.ifReverseCamera = ifReverseCamera;
    }

    public String getIsAutoResetTHR() {
        return isAutoResetTHR;
    }

    public void setIsAutoResetTHR(String isAutoResetTHR) {
        this.isAutoResetTHR = isAutoResetTHR;
    }

    public int getROILeftUpX() {
        return roiLeftUpX;
    }

    public void setROILeftUpX(int roiLeftUpX) {
        this.roiLeftUpX = roiLeftUpX;
    }

    public int getROILeftUpY() {
        return roiLeftUpY;
    }

    public void setROILeftUpY(int roiLeftUpY) {
        this.roiLeftUpY = roiLeftUpY;
    }

    public int getROILeftDownX() {
        return roiLeftDownX;
    }

    public void setROILeftDownX(int roiLeftDownX) {
        this.roiLeftDownX = roiLeftDownX;
    }

    public int getROILeftDownY() {
        return roiLeftDownY;
    }

    public void setROILeftDownY(int roiLeftDownY) {
        this.roiLeftDownY = roiLeftDownY;
    }

    public int getROIRightUpX() {
        return roiRightUpX;
    }

    public void setROIRightUpX(int roiRightUpX) {
        this.roiRightUpX = roiRightUpX;
    }

    public int getROIRightUpY() {
        return roiRightUpY;
    }

    public void setROIRightUpY(int roiRightUpY) {
        this.roiRightUpY = roiRightUpY;
    }

    public int getROIRightDownX() {
        return roiRightDownX;
    }

    public void setROIRightDownX(int roiRightDownX) {
        this.roiRightDownX = roiRightDownX;
    }

    public int getROIRightDownY() {
        return roiRightDownY;
    }

    public void setROIRightDownY(int roiRightDownY) {
        this.roiRightDownY = roiRightDownY;
    }

    public double getCheckPosSlopes() {
        return checkPosSlopes;
    }

    public void setCheckPosSlopes(double checkPosSlopes) {
        this.checkPosSlopes = checkPosSlopes;
    }

    public double getCheckNegSlopes() {
        return checkNegSlopes;
    }

    public void setCheckNegSlopes(double checkNegSlopes) {
        this.checkNegSlopes = checkNegSlopes;
    }

    public int getGausianKernelValue() {
        return gausianKernelValue;
    }

    public void setGausianKernelValue(int gausianKernelValue) {
        this.gausianKernelValue = gausianKernelValue;
    }

    public double getAutoCannyValue() {
        return autoCannyValue;
    }

    public void setAutoCannyValue(double autoCannyValue) {
        this.autoCannyValue = autoCannyValue;
    }

    public double getRhoValue() {
        return rho;
    }

    public void setRhoValue(double rho) {
        this.rho = rho;
    }

    public int getThetaValue() {
        return thetaValue;
    }

    public void setThetaValue(int thetaValue) {
        this.thetaValue = thetaValue;
    }

    public int getTheresHoldValue() {
        return theresHoldValue;
    }

    public void setTheresHoldValue(int theresHoldValue) {
        this.theresHoldValue = theresHoldValue;
    }

    public int getMinLineLen() {
        return minLineLen;
    }

    public void setMinLineLen(int minLineLen) {
        this.minLineLen = minLineLen;
    }

    public int getMaxLineGap() {
        return maxLineGap;
    }

    public void setMaxLineGap(int maxLineGap) {
        this.maxLineGap = maxLineGap;
    }

    public int getMaxLeftRightPower() {
        return maxLeftRightPower;
    }

    public void setMaxLeftRightPower(int maxLeftRightPower) {
        this.maxLeftRightPower = maxLeftRightPower;
    }

    public int getMaxForwardPower() {
        return maxForwardPower;
    }

    public void setMaxForwardPower(int maxForwardPower) {
        this.maxForwardPower = maxForwardPower;
    }

    public int getDetectionErrorStop()
    {
        return detectionErrorStop;
    }

    public void setDetectionErrorStop(int detectionErrorStopValue)
    {
        detectionErrorStop = detectionErrorStopValue;
    }

    public boolean isFilterColor() {
        return isFilterColor;
    }

    public void setFilterColor(boolean filterColor) {
        isFilterColor = filterColor;
    }

    public String getFilterColorLower() {
        return filterColorLower;
    }

    public void setFilterColorLower(String filterColorLower) {
        this.filterColorLower = filterColorLower;
    }

    public String getFilterColorUpper() {
        return filterColorUpper;
    }

    public void setFilterColorUpper(String filterColorUpper) {
        this.filterColorUpper = filterColorUpper;
    }

    public String toJSONString()
    {
        return "";
    }

    public String showDetectionParameter()
    {
        return Constants.Left_Top_X_Value + " : " + getROILeftUpX() + "\n" +
            Constants.Left_Top_Y_Value + " : " + getROILeftUpY() + "\n" +
            Constants.Left_Bottom_X_Value + " : " + getROILeftDownX() + "\n" +
            Constants.Left_Bottom_Y_Value + " : " + getROILeftDownY() + "\n" +
            Constants.Right_Top_X_Value + " : " + getROIRightUpX() + "\n" +
            Constants.Right_Top_Y_Value + " : " + getROIRightUpY() + "\n" +
            Constants.Right_Bottom_X_Value + " : " + getROIRightDownX() + "\n" +
            Constants.Right_Bottom_Y_Value + " : " + getROIRightDownY() + "\n" +
            Constants.Gausian_Last_Value_Int + " : " + getGausianKernelValue() + "\n" +
            Constants.AutoCanny_Last_Value_Float + " : " + getAutoCannyValue() + "\n" +
            Constants.HoughLine_RHO_Last_Value_Double + " : " + getRhoValue() + "\n" +
            Constants.HoughLine_THETA_Last_Value_Int + " : " + getThetaValue() + "\n" +
            Constants.HoughLine_Threshold_Last_Value_Int + " : " + getTheresHoldValue() + "\n" +
            Constants.HoughLine_MinLineLength_Last_Value_Int + " : " + getMinLineLen() + "\n" +
            Constants.HoughLine_MaxLineGap_Last_Value_Int + " : " + getMaxLineGap() + "\n" +
            Constants.PosSlopes_Last_Value_Double + " : " + getCheckPosSlopes() + "\n" +
            Constants.NegSlopes_Last_Value_Double + " : " + getCheckNegSlopes() + "\n" +
            Constants.DETECTION_ERROR_STOP + " : " + getDetectionErrorStop() + "\n" +
            Constants.MAX_LEFT_RIGHT_POWER + " : " + getMaxLeftRightPower() + "\n" +
            Constants.MAX_FORWARD_POWER + " : " + getMaxForwardPower();
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Kind : "+getKind()+"\n");
        stringBuilder.append("Name : "+getName()+"\n");

        for(int i = 0 ; i < 8 ; i++)
        {
            int showIndex = i+1;

            String expotentialChannel = "ExpotentialModeCh"+showIndex+" : "+CExpotentialMode.getChannelData(i)+"\n";

            stringBuilder.append(expotentialChannel);
        }

        for(int i = 0 ; i < 8 ; i++)
        {
            int showIndex = i+1;

            String mixingConfigChannel = "MixingConfigCH"+showIndex+" : "+CMaxingConfig.getChannelData(i)+"\n";

            stringBuilder.append(mixingConfigChannel);
        }

        for(int i = 0 ; i < 8 ; i++)
        {
            int showIndex = i+1;

            String servoReverseChannel = "ServoReverseCh"+showIndex+" : "+CServoReverse.getChannelData(i)+"\n";

            stringBuilder.append(servoReverseChannel);
        }

        for(int i = 0 ; i < StrimArrayList.size() ; i++)
        {
            int channelIndex = i+1;

            String strimConfigChannelUpper = "StrimConfigCh"+channelIndex+"Upper : "+StrimArrayList.get(i).getUpper()+"\n";
            String strimConfigChannelLower = "StrimConfigCh"+channelIndex+"Lower : "+StrimArrayList.get(i).getLower()+"\n";
            String strimConfigChannelMiddle = "StrimConfigCh"+channelIndex+"Middle : "+StrimArrayList.get(i).getMiddle()+"\n";
            String strimConfigChannelFailSafe = "StrimConfigCh"+channelIndex+"FailSafe : "+StrimArrayList.get(i).getFailSafe()+"\n";

            stringBuilder.append(strimConfigChannelUpper);
            stringBuilder.append(strimConfigChannelLower);
            stringBuilder.append(strimConfigChannelMiddle);
            stringBuilder.append(strimConfigChannelFailSafe);
        }


        for(int i = 0 ; i < isAutoResetChannel.length ; i++)
        {
            int channelIndex = i+1;
            String isAutoResetToChannel = "isAutoResetToChannel"+channelIndex+" : "+isAutoResetChannel[i]+"\n";
            stringBuilder.append(isAutoResetToChannel);
        }

        //27
        String screeanControlModevalue = "ScreenControlMode : " + getScreeanControlMode()+"\n";
        stringBuilder.append(screeanControlModevalue);

        //28
        String mixingConfigReverseValue = "MixingConfigReverse : " + getMixingConfigReverse()+"\n";
        stringBuilder.append(mixingConfigReverseValue);

        String sensorInductionAngle = "sensorInductionAngle : " + getSensorInductionAngle()+"\n";
        stringBuilder.append(sensorInductionAngle);

        String ifReverseCamera = "IfReverseCamera : " + getIfReverseCamera()+"\n";
        stringBuilder.append(ifReverseCamera);

        String isAutoResetTHR = "isAutoResetTHR : " + getIsAutoResetTHR()+"\n";
        stringBuilder.append(isAutoResetTHR);

        String detectionParamsString =   Constants.Left_Top_X_Value + " : " + getROILeftUpX() + "\n" +
                Constants.Left_Top_Y_Value + " : " + getROILeftUpY() + "\n" +
                Constants.Left_Bottom_X_Value + " : " + getROILeftDownX() + "\n" +
                Constants.Left_Bottom_Y_Value + " : " + getROILeftDownY() + "\n" +
                Constants.Right_Top_X_Value + " : " + getROIRightUpX() + "\n" +
                Constants.Right_Top_Y_Value + " : " + getROIRightUpY() + "\n" +
                Constants.Right_Bottom_X_Value + " : " + getROIRightDownX() + "\n" +
                Constants.Right_Bottom_Y_Value + " : " + getROIRightDownY() + "\n" +
                Constants.Gausian_Last_Value_Int + " : " + getGausianKernelValue() + "\n" +
                Constants.AutoCanny_Last_Value_Float + " : " + getAutoCannyValue() + "\n" +
                Constants.HoughLine_RHO_Last_Value_Double + " : " + getRhoValue() + "\n" +
                Constants.HoughLine_THETA_Last_Value_Int + " : " + getThetaValue() + "\n" +
                Constants.HoughLine_Threshold_Last_Value_Int + " : " + getTheresHoldValue() + "\n" +
                Constants.HoughLine_MinLineLength_Last_Value_Int + " : " + getMinLineLen() + "\n" +
                Constants.HoughLine_MaxLineGap_Last_Value_Int + " : " + getMaxLineGap() + "\n" +
                Constants.PosSlopes_Last_Value_Double + " : " + getCheckPosSlopes() + "\n" +
                Constants.NegSlopes_Last_Value_Double + " : " + getCheckNegSlopes() + "\n" +
                Constants.DETECTION_ERROR_STOP + " : " + getDetectionErrorStop() + "\n" +
                Constants.MAX_LEFT_RIGHT_POWER + " : " + getMaxLeftRightPower() + "\n" +
                Constants.MAX_FORWARD_POWER + " : " + getMaxForwardPower();

        stringBuilder.append(detectionParamsString);

        return stringBuilder.toString();
    }
}


