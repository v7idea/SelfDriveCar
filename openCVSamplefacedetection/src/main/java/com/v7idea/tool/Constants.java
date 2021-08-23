package com.v7idea.tool;

/**
 * Created by mortal on 2017/3/10.
 *
 * 常數物件
 */
public class Constants
{
    public final static String APP_MODE = "appMode";
    public final static int CAR_MODE = 0;
    public final static int COMMAND_MODE = 1;
    public final static int WEBCAM_MODE = 2;

    public static final int DEFAULT_PORT = 5050;
    public final static int DEFAULT_VIDEO_PORT = 5049;
    public final static int BUFFER_SIZE = 1024;
    public final static int SEND_DATA_BUFFER_SIZE = BUFFER_SIZE + 2;

    public static final int videoWidth = 360;
    public static final int videoHeight = 240;

    public static final int ENTRY_PAGE_DELAY_TIME = 3000;

    public static String Left_Bottom_X_Value = "leftBottomXValue";
    public static String Left_Bottom_Y_Value  = "leftBottomYValue";
    public static String Right_Bottom_X_Value  = "rightBottomXValue";
    public static String Right_Bottom_Y_Value  = "rightBottomYValue";
    public static String Right_Top_X_Value  = "rightTopXValue";
    public static String Right_Top_Y_Value  = "rightTopYValue";
    public static String Left_Top_X_Value  = "leftTopXValue";
    public static String Left_Top_Y_Value  = "leftTopYValue";

    public static String Gausian_Last_Value_Int  = "GausianLastValue";
    public static String AutoCanny_Last_Value_Float  = "AutoCannyLastValue";
    public static String HoughLine_RHO_Last_Value_Double  = "HoughLineRHOLastValue";
    public static String HoughLine_THETA_Last_Value_Int  = "HoughLineTHETALastValue";
    public static String HoughLine_Threshold_Last_Value_Int  = "HoughLineThresholdLastValue";
    public static String HoughLine_MinLineLength_Last_Value_Int  = "HoughLineMinLineLengthLastValue";
    public static String HoughLine_MaxLineGap_Last_Value_Int  = "HoughLineMaxLineGapLastValue";
    public static String PosSlopes_Last_Value_Double  = "PosSlopesLastValue";
    public static String NegSlopes_Last_Value_Double  = "NegSlopesLastValue";

    public static String MAX_LEFT_RIGHT_POWER  = "maxLeftRightPower";
    public static String MAX_FORWARD_POWER = "maxForwardPower";
    public static String DETECTION_ERROR_STOP = "detectionErrorStop";

    public static String IS_FILTER_COLOR = "isFillterColor";
    public static String FILTER_COLOR_UPPER = "isFillterColor";
    public static String FILTER_COLOR_LOWER = "isFillterColor";

    /**
     * 在掃瞄藍牙BLE頁面用，確認權限是否開起的回傳辨示碼
     */
    public static final int REQUEST_CODE = 9991;

    /**
     * Channel的總數量
     */
    public static final int TOTAL_CHANNELS = 8;

    /**
     * 發送資料的延遲時間的預設值
     */
    public static final int DEFAULT_SEND_DATA_DELAY_TIME = 25;

    public static long startSendDataTime = 0l;
    public static long endSendDataTime = 0l;

    public class Debug
    {
        public static final boolean IS_DEBUG_SEND_DATA = false;
    }

    public class BottomMenu
    {
        public static final int BUTTON_COUNT = 6;
    }

    public class Setting
    {
        public static final String DEFAULT_NAME = "DefaultName";
        public static final String DEFAULT_KIND = "DefaultKind";
        public static final String DELAY_TIME = "DelayTime";
    }

    //Channel物件的預設值
    public class Channel{
        public static final float RANGE = 1000.0f;

        public static final float MINIMUM_VALUE = 1000;
        public static final float MIDDLE_VALUE = 1500;
        public static final float MAXMUN_VALUE = 2000;
    }

    public static final String CHANNEL_SETTING = "ChannelSetting";

    public static final String KIND = "Kind";
    public static final String SETTING_NAME = "Name";

    public static final String getExpotentialModeKeyName(int channleIndex)
    {
        return ("ExpotentialModeCh"+channleIndex);
    }

    public static final String getMixingConfigKeyName(int channleIndex)
    {
        return ("MixingConfigCH"+channleIndex);
    }

    public static final String getServoReverseKeyName(int channleIndex)
    {
        return ("ServoReverseCh"+channleIndex);
    }

    public static final String SCREEN_CONTROL_MODE = "ScreenControlMode";

    public static final String MIXING_CONFIG_REVERSE = "MixingConfigReverse";

    public static final String IF_REVERSE_CAMERA = "IfReverseCamera";
    public static final String IS_AUTO_RESET_THR = "isAutoResetTHR";
    public static final String SENSOR_INDUCTION_ANGLE = "sensorInductionAngle";

    public static String getStrimConfigUpperKeyName(int channleIndex)
    {
        return ("StrimConfigCh"+channleIndex+"Upper");
    }

    public static String getStrimConfigLowerKeyName(int channleIndex)
    {
        return ("StrimConfigCh"+channleIndex+"Lower");
    }

    public static String getStrimConfigMiddleKeyName(int channleIndex)
    {
        return ("StrimConfigCh"+channleIndex+"Middle");
    }

    public static String getStrimConfigFailSafeKeyName(int channleIndex)
    {
        return ("StrimConfigCh"+channleIndex+"FailSafe");
    }

    public static String getIsAutoResetToChannelKeyName(int channleIndex)
    {
        return ("isAutoResetToChannel" + channleIndex);
    }
}
