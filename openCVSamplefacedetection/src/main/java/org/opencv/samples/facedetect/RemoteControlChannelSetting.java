package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.v7idea.DataBase.DataBase;
import com.v7idea.template.View.Banner;
import com.v7idea.template.View.BottomMenuLayout;
import com.v7idea.template.View.SpecialImageView;
import com.v7idea.template.View.V7TitleView;
import com.v7idea.tool.Channel;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.DebugLog;
import com.v7idea.tool.ViewScaling;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

public class RemoteControlChannelSetting extends Activity implements View.OnClickListener{
    public static final String TAG = RemoteControlChannelSetting.class.getSimpleName();

    private static final float GRADUATION_START = 850.0f;
    private static final float GRADUATION_MIDDLE_START = 1350.0f;
    private static final float GRADUATION_MIDDLE_END = 1650.0f;
    private static final float GRADUATION_END = 2150.0f;
    private static final float TOTAL_GRADUATION = GRADUATION_END - GRADUATION_START;

    private float controlBarWidth = 834.0f;
    private float valueMinimum = 0f;
    private float valueMaxmun = 0f;
    private float offSet = 0;
    private int expotentailIndex = 0;

    private final int[] expotentailImageArray = {R.mipmap.static_type_3, R.mipmap.static_type_2
            , R.mipmap.static_type_1, R.mipmap.static_type_4, R.mipmap.static_type_5};
    /**
     * 每個圖表所對應到的數值
     */
    private int[] expotentialValue = {3, 2, 1, 4, 5};

    /**
     * 這裡是用來產生畫面用的
     */
    private Channel currentChannel = null;
    private CustomerSetting settingData = null;
    private Air thisApp = null;
    private DataBase dataBase = null;
    private V7RCLiteController bleController = null;
    private SendDataRunnable sendDataRunnable = null;
    private Handler handler = null;

    private SpecialImageView subTrimBackground = null;
    private V7TitleView lowerValue = null;
    private SpecialImageView lowerLimitBar = null;
    private V7TitleView upperValue = null;
    private SpecialImageView upperLimitBar = null;
    private V7TitleView middleValue = null;
    private SpecialImageView middleLimitBar = null;
    private SpecialImageView failSafeBar = null;
    private V7TitleView failSafeValue = null;
    private SpecialImageView tempControlBar = null;
    private V7TitleView tempShowValue = null;
    private SpecialImageView ExpotentailImage = null;
    private SpecialImageView reverseSwitch = null;
    private BottomMenuLayout bottomMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 鎖定螢幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        thisApp = (Air) getApplication();

        bleController = thisApp.getBleController();
        bleController.startCommand();

        dataBase = new DataBase(this);
        dataBase.Open();

        settingData = thisApp.getCurrentSetting(dataBase);

        handler = new Handler();

        sendDataRunnable = new SendDataRunnable();

        setContentView(R.layout.activity_remote_control_channel_setting);

        ViewScaling.setScaleValue(this);

        Banner Header = (Banner) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_Banner_Header);
        Header.initV7rcStyle();
        Header.addButton("", R.mipmap.button_menu_back, this);
        Header.setTitleString(settingData.getName());

        SpecialImageView background = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_background);

        subTrimBackground = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_SubTrimBackground);
        offSet = ((RelativeLayout.LayoutParams)subTrimBackground.getLayoutParams()).leftMargin;
        controlBarWidth = (float) (subTrimBackground.getLayoutParams().width);

        lowerValue = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_V7TitleView_lowerValue);

        lowerLimitBar = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_lowerLimitBar);

        upperValue = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_V7TitleView_upperValue);

        upperLimitBar = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_upperLimitBar);

        middleValue = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_V7TitleView_middleValue);

        middleLimitBar = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_middleLimitBar);

        failSafeBar = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_failSafeBar);

        failSafeValue = (V7TitleView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_V7TitleView_failSafeValue);

        ExpotentailImage = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_Expotentail);
        ExpotentailImage.setOnClickListener(onPressExpotentail);

        reverseSwitch = (SpecialImageView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_SpecialImageView_ReverseSwitch);
        reverseSwitch.setOnClickListener(onPressSwitch);

        bottomMenu = (BottomMenuLayout) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlChannelSetting_BottomMenuLayout_BottomMenu);
        bottomMenu.setting();
        bottomMenu.setVisibility(View.VISIBLE);
        bottomMenu.setOnPressCallBack(onPressCallBack);
        bottomMenu.cancelOnClickListenerByIndex(bottomMenu.getPageIndex());

        currentChannel = settingData.getChannel(bottomMenu.getPageIndex());
        changeBarProcess(currentChannel);

        lowerLimitBar.setOnTouchListener(onPressControlButton);
        middleLimitBar.setOnTouchListener(onPressControlButton);
        upperLimitBar.setOnTouchListener(onPressControlButton);
        failSafeBar.setOnTouchListener(onPressControlButton);
    }

    /***
     * 顯示目前的 Expotentail 圖型在 expotentialValue陣列中的Index
     * @param expotentailValue 原來的設定數據
     * @return 新的類別值
     */
    public int changeExpotentailToImageArrayIndex(int expotentailValue) {

        int thisNum = 0;

        for (int i = 0; i < expotentialValue.length; i ++) {
            if(expotentailValue == expotentialValue[i]) {
                thisNum = i ;
                break;
            }
        }

        DebugLog.d("重新取得編號","編號：" + thisNum);
        return thisNum;

    }

    private int changeImageResourceIDByChannel()
    {
        int imageID = 0;

        expotentailIndex ++;

        if(expotentailIndex <= 0)
        {
            expotentailIndex = 0;
        }

        int limit = expotentailImageArray.length - 1;

        if(expotentailIndex > limit)
        {
            expotentailIndex = 0;
        }

        int channelIndex = bottomMenu.getPageIndex();

        settingData.getExpotentialModeItem().setChannelData(channelIndex
                , String.valueOf(expotentialValue[expotentailIndex]));

        String ExpotentailChannelName = "ExpotentialModeCh"+(channelIndex+1);

        dataBase.editUpdateRemoteSettingTable(settingData.getId()
                , ExpotentailChannelName
                , String.valueOf(expotentialValue[expotentailIndex]));

        imageID = expotentailImageArray[expotentailIndex];

//        DebugLog.e(TAG, "expotentailIndex: "+expotentailIndex);

        return imageID;
    }

    /***
     * 有關於載入時的預設設定
     */
    public void changeBarProcess(Channel currentChannel) {
        RelativeLayout.LayoutParams upperBarParams = (RelativeLayout.LayoutParams)upperLimitBar.getLayoutParams();
        RelativeLayout.LayoutParams middleBarParams = (RelativeLayout.LayoutParams)middleLimitBar.getLayoutParams();
        RelativeLayout.LayoutParams LowerBarParams = (RelativeLayout.LayoutParams)lowerLimitBar.getLayoutParams();
        RelativeLayout.LayoutParams failSafeBarParams = (RelativeLayout.LayoutParams)failSafeBar.getLayoutParams();

        float channelMaxValue = currentChannel.getMaximunValue();
        float channelMiddleValue = currentChannel.getMiddleValue();
        float channelMinimumValue = currentChannel.getMinimunValue();
        float channelFailSafeValue = currentChannel.getFailSaveData();

        //將換算出來的位置減掉 upperLimitBar 的寬/2才是 "實際" 顯示的位置
        int upperLimitBarPosition = (int)(getBarPosition(channelMaxValue) - upperBarParams.width);
        int middleLimitBarPosition = (int)(getBarPosition(channelMiddleValue) - (middleBarParams.width /2));
        int lowerLimitBarPosition = (int)(getBarPosition(channelMinimumValue));
        int failSafeBarPosition = (int)(getBarPosition(channelFailSafeValue)) - (failSafeBarParams.width / 2);

//        DebugLog.e(TAG, "upperLimitBarPosition: "+upperLimitBarPosition);
//        DebugLog.e(TAG, "middleLimitBarPosition: "+middleLimitBarPosition);
//        DebugLog.e(TAG, "lowerLimitBarPosition: "+lowerLimitBarPosition);
//        DebugLog.e(TAG, "failSafeBarPosition: "+failSafeBarPosition);

        upperBarParams.leftMargin = upperLimitBarPosition;
        middleBarParams.leftMargin = middleLimitBarPosition;
        LowerBarParams.leftMargin = lowerLimitBarPosition;
        failSafeBarParams.leftMargin = failSafeBarPosition;

        upperLimitBar.setLayoutParams(upperBarParams);
        middleLimitBar.setLayoutParams(middleBarParams);
        lowerLimitBar.setLayoutParams(LowerBarParams);
        failSafeBar.setLayoutParams(failSafeBarParams);

        String showMaxValue = (int)(channelMaxValue) + "(" + trimValueTranslateToPercentage((int)(channelMaxValue)) + ")";
        String showMiddleValue = (int)(channelMiddleValue) + "(" + trimValueTranslateToPercentage((int)(channelMiddleValue)) + ")";
        String showMinimumValue = (int)(channelMinimumValue) + "(" + trimValueTranslateToPercentage((int)(channelMinimumValue)) + ")";
        String showFailSafeValue = (int)(channelFailSafeValue) + "(" + trimValueTranslateToPercentage((int)(channelFailSafeValue)) + ")";

        checkValueState((int)(channelMaxValue), upperValue);
        checkValueState((int)(channelMiddleValue), middleValue);
        checkValueState((int)(channelMinimumValue), lowerValue);
        checkValueState((int)(channelFailSafeValue), failSafeValue);

        upperValue.setText(showMaxValue);
        middleValue.setText(showMiddleValue);
        lowerValue.setText(showMinimumValue);
        failSafeValue.setText(showFailSafeValue);

        //設定 Expotentail 要顯示的圖
        int currentExpotentailValue = currentChannel.getExpotentailSelect();

        expotentailIndex = changeExpotentailToImageArrayIndex(currentExpotentailValue);

//        DebugLog.d(TAG, "expotentailIndex: "+expotentailIndex);

        ExpotentailImage.setImageResource(expotentailImageArray[expotentailIndex]);

        boolean isChannelReverse = currentChannel.isReverse();

        DebugLog.d(TAG," isChannelReverse:" + isChannelReverse );

        reverseSwitch.setSelected(isChannelReverse);
    }

    /**
     * 換算成在控制列上的位置
     * 原本的算法是將View的長或寬轉換成下圖的尺
     * 0                1000              2000
     * |_________________|_________________|
     *
     * 各減1000轉換成
     * -1000             0                1000
     * |_________________|_________________|
     *
     * 最後再交給Channel元件計算
     *
     * 在這裡是將View的長或寬轉換成下圖的尺
     *
     * 總長度 = GRADUATION_END(2150) - GRADUATION_START(850)
     * 中間值 = 總長度 / 2
     *
     * 0                中間值             總長度
     * |_________________|_________________|
     *
     * 各減850轉換成這樣的尺
     *
     * 850              1500              2150
     * |_________________|_________________|
     *
     * 所以在運算時，要特別注意。
     * 原本的算法，起點是中間值，這裡的算法 0 是起點。
     *
     * @param value
     * @return
     */
    public float getBarPosition(float value) {

        float unitValue = TOTAL_GRADUATION / controlBarWidth;

//        DebugLog.e(TAG, "value: "+value);
//        DebugLog.e(TAG, "TOTAL_GRADUATION: "+TOTAL_GRADUATION);
//        DebugLog.e(TAG, "controlBarWidth: "+controlBarWidth);
//        DebugLog.e(TAG, "unitValue: "+unitValue);

        float marginLeft = (value - GRADUATION_START) / unitValue;

//        DebugLog.e(TAG, "GRADUATION_START: "+GRADUATION_START);
//        DebugLog.e(TAG, "marginLeft: "+marginLeft);

        return marginLeft;
    }

    public float countValue(float floatEventX, float multiple)
    {
        return  (multiple * floatEventX) + GRADUATION_START;
    }


    /**
     * 依照目前的數值變化給予顏色上的呈現
     */
    public void checkValueState(int intCurrentValue, V7TitleView showValueView ) {

        int valuePercentage = Integer.valueOf(trimValueTranslateToPercentage(Integer.valueOf(intCurrentValue)) );

        if(intCurrentValue < -100 || valuePercentage > 100) {

            showValueView.setTextColor(Color.RED);

        } else {

            showValueView.setTextColor(Color.BLACK);

        }
    }

    /**
     * 改變要被移動的ImageView的marginLeft
     * @param view
     * @param intMarginLeft
     */
    private void changeMarginLeftByView(View view, int intMarginLeft)
    {
        RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if(view.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_lowerLimitBar)
        {
            viewParams.leftMargin = intMarginLeft;
        }
        else if(view.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_upperLimitBar)
        {
            viewParams.leftMargin = intMarginLeft - viewParams.width;
        }
        else if(view.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_middleLimitBar)
        {
            viewParams.leftMargin = intMarginLeft - (viewParams.width / 2);
            //設定要被移動的ImageView
        }
        else if(view.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_failSafeBar)
        {
            //過濾FailSaveBar移動的範圍
            int failSaveBarStart = (int)(24f * ViewScaling.getScaleValue());
            int failSaveBarWidth = (int)(controlBarWidth - failSaveBarStart);

            if(intMarginLeft <= failSaveBarStart)
            {
                intMarginLeft = failSaveBarStart;
            }

            if(intMarginLeft >= failSaveBarWidth)
            {
                intMarginLeft = failSaveBarWidth;
            }

            viewParams.leftMargin = intMarginLeft + (int)(failSaveBarStart) - viewParams.width;
        }
    }

    /**
     * 鎖定數值， View.OnTouchListener onPressControlButton 會有影响
     * @param floatValue
     * @return
     */
    private float filterValue(float floatValue)
    {
        if(floatValue < valueMinimum)
        {
            floatValue = valueMinimum;
        }

        if(floatValue > valueMaxmun)
        {
            floatValue = valueMaxmun;
        }

        return floatValue;
    }

    /***
     * 將數值轉換成百分比(為什麼這樣算？？？？？)
     * @param thisValue
     * @return 已經轉換成百分比的數字
     */
    public String trimValueTranslateToPercentage(int thisValue) {

        String thisResult = null;

        float tempValue = (float) thisValue - 2000.0f;
        thisResult = String.valueOf( (int) (tempValue * 0.2f) + 100 );

        return thisResult;

    }

    private View.OnClickListener onPressSwitch = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if(v.isSelected())
            {
                v.setSelected(false);
            }
            else
            {
                v.setSelected(true);
            }

            boolean isReverse = v.isSelected();

            DebugLog.d(TAG," Reverse:" + isReverse );

            reverseSwitch.setSelected(isReverse);

            int channelIndex = bottomMenu.getPageIndex();
            String fieldName = "ServoReverseCh" + (channelIndex + 1);

            if(isReverse)
            {
                settingData.getServoReverseItem().setChannelData(channelIndex,"Yes");
                dataBase.editUpdateRemoteSettingTable(settingData.getId(), fieldName, "Yes");
            }
            else
            {
                settingData.getServoReverseItem().setChannelData(channelIndex, "NO");
                dataBase.editUpdateRemoteSettingTable(settingData.getId(), fieldName, "NO");
            }
        }
    };

    private View.OnClickListener onPressExpotentail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(currentChannel != null)
            {
                int imageResourceID = changeImageResourceIDByChannel();

                ExpotentailImage.setImageResource(imageResourceID);
            }
        }
    };

    private BottomMenuLayout.OnPressCallBack onPressCallBack = new BottomMenuLayout.OnPressCallBack() {
        @Override
        public void onPressChildViewID(int viewID) {

            currentChannel = settingData.getChannel(viewID);
            changeBarProcess(currentChannel);
        }
    };

    private View.OnTouchListener onPressControlButton = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
//            private float valueMinimum = 0f;
//            private float valueMaxmun = 0f;

            if(event.getAction() == MotionEvent.ACTION_DOWN)
            {
                if(v.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_lowerLimitBar)
                {
                    //設定要被移動的ImageView
                    tempControlBar = lowerLimitBar;

                    //設定要顯示數的TextView
                    tempShowValue = lowerValue;

                    //設定數值的上限及下限
                    valueMinimum = GRADUATION_START;
                    valueMaxmun = GRADUATION_MIDDLE_START;
                }
                else if(v.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_upperLimitBar)
                {
                    //設定要被移動的ImageView
                    tempControlBar = upperLimitBar;

                    //設定要顯示數的TextView
                    tempShowValue = upperValue;

                    //設定數值的上限及下限
                    valueMinimum = GRADUATION_MIDDLE_END;
                    valueMaxmun = GRADUATION_END;
                }
                else if(v.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_middleLimitBar)
                {
                    //設定要被移動的ImageView
                    tempControlBar = middleLimitBar;

                    //設定要顯示數的TextView
                    tempShowValue = middleValue;

                    //設定數值的上限及下限
                    valueMinimum = GRADUATION_MIDDLE_START;
                    valueMaxmun = GRADUATION_MIDDLE_END;
                }
                else if(v.getId() == R.id.RemoteControlChannelSetting_SpecialImageView_failSafeBar)
                {
                    //設定要被移動的ImageView
                    tempControlBar = failSafeBar;

                    //設定要顯示數的TextView
                    tempShowValue = failSafeValue;

                    //設定數值的上限及下限
                    valueMinimum = GRADUATION_START;
                    valueMaxmun = GRADUATION_END;
                }

//                DebugLog.d(TAG, "valueMinimum: "+valueMinimum);
//                DebugLog.d(TAG, "valueMaxmun: "+valueMaxmun);
            }

            return false;
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float moveScale = TOTAL_GRADUATION / controlBarWidth;

        if(tempControlBar != null && tempShowValue != null)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
            {
                float currentEventX = event.getX() - offSet;

                DebugLog.e(TAG, "currentEventX: "+currentEventX);

                float valueCurrent = countValue(currentEventX,moveScale);

                valueCurrent = filterValue(valueCurrent);

                if(valueCurrent <= valueMaxmun && valueCurrent >= valueMinimum)
                {
                    int viewMarginLeft = (int)((valueCurrent - GRADUATION_START)/ moveScale);

                    changeMarginLeftByView(tempControlBar, viewMarginLeft);
                    String showValue = (int)(valueCurrent) + "(" + trimValueTranslateToPercentage((int)(valueCurrent)) + ")";
                    tempShowValue.setText(showValue);
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_MOVE)
            {
                float currentEventX = event.getX() - offSet;

                DebugLog.e(TAG, "move currentEventX: "+currentEventX);

                float valueCurrent = countValue(currentEventX,moveScale);

                valueCurrent = filterValue(valueCurrent);

                DebugLog.e(TAG, "move valueCurrent: "+valueCurrent);
                DebugLog.d(TAG, "move valueMinimum: "+valueMinimum);
                DebugLog.d(TAG, "move valueMaxmun: "+valueMaxmun);

                if(valueCurrent <= valueMaxmun && valueCurrent >= valueMinimum)
                {
                    int viewMarginLeft = (int)((valueCurrent - GRADUATION_START)/ moveScale);

                    changeMarginLeftByView(tempControlBar, viewMarginLeft);

                    String showValue = (int)(valueCurrent) + "(" + trimValueTranslateToPercentage((int)(valueCurrent)) + ")";
                    tempShowValue.setText(showValue);

                    checkValueState((int)(valueCurrent), tempShowValue);

                    sendDataRunnable.setValue(("" + (int)(valueCurrent)));

                    handler.post(sendDataRunnable);

                    //                        //下面怪怪的
//                        switch(ChoiseChannel + 1)
//                        {
//                            case 1:
//
//                                if(thisApp.useWifiOrBluetooth)
//                                {
//                                    FSskf.SendProtocol(Integer.valueOf(ans)
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()), 0, 0);
//                                }
//                                else
//                                {
//                                    bluetoothController.sendPotocalToBluetooth(Integer.valueOf(ans)
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()), 0, 0);
//                                }
//
//                                break;
//
//                            case 2:
//
//                                if(thisApp.useWifiOrBluetooth)
//                                {
//                                    FSskf.SendProtocol(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle())
//                                            , Integer.valueOf(ans)
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()), 0, 0);
//                                }
//                                else
//                                {
//                                    bluetoothController.sendPotocalToBluetooth(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle())
//                                            , Integer.valueOf(ans)
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle())
//                                            , Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()), 0, 0);
//                                }
//
//                                break;
//
//                            case 3:
//
//                                if(thisApp.useWifiOrBluetooth)
//                                {
//                                    FSskf.SendProtocol(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(ans),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()), 0, 0);
//                                }
//                                else
//                                {
//                                    bluetoothController.sendPotocalToBluetooth(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(ans),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()), 0, 0);
//                                }
//
//                                break;
//
//                            case 4:
//
//                                if(thisApp.useWifiOrBluetooth)
//                                {
//                                    FSskf.SendProtocol(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle()),
//                                            Integer.valueOf(ans),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()),
//                                            0, 0);
//                                }
//                                else
//                                {
//                                    bluetoothController.sendPotocalToBluetooth(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle()),
//                                            Integer.valueOf(ans),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()),
//                                            0, 0);
//                                }
//
//                                break;
//
//                            case 5:
//
//                                if(thisApp.useWifiOrBluetooth)
//                                {
//                                    FSskf.SendProtocol(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle()),
//                                            Integer.valueOf(ans),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()),
//                                            0, 0);
//                                }
//                                else
//                                {
//                                    bluetoothController.sendPotocalToBluetooth(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle()),
//                                            Integer.valueOf(ans),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle()),
//                                            0, 0);
//                                }
//
//                                break;
//
//                            case 6:
//
//                                if(thisApp.useWifiOrBluetooth)
//                                {
//                                    FSskf.SendProtocol(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle()),
//                                            Integer.valueOf(ans), 0, 0);
//                                }
//                                else
//                                {
//                                    bluetoothController.sendPotocalToBluetooth(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle()),
//                                            Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle()),
//                                            Integer.valueOf(ans), 0, 0);
//                                }
//
//                                break;
//                        }
//                    }
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP)
            {
                float currentEventX = event.getX() - offSet;

                float valueCurrent = countValue(currentEventX,moveScale);

                valueCurrent = filterValue(valueCurrent);

                if(valueCurrent <= valueMaxmun && valueCurrent >= valueMinimum)
                {
                    int viewMarginLeft = (int)((valueCurrent - GRADUATION_START)/ moveScale);

                    changeMarginLeftByView(tempControlBar, viewMarginLeft);

                    String showValue = (int)(valueCurrent) + "(" + trimValueTranslateToPercentage((int)(valueCurrent)) + ")";
                    tempShowValue.setText(showValue);

                    checkValueState((int)(valueCurrent), tempShowValue);

                    String strSaveValue = "" + (int)(valueCurrent);

                    int settingDataInDBID = settingData.getId();

                    int currentChannelIndex = bottomMenu.getPageIndex();

                    switch(tempControlBar.getId())
                    {
                        case R.id.RemoteControlChannelSetting_SpecialImageView_lowerLimitBar:

                            settingData.getStrimConfigItem(currentChannelIndex).setLower(strSaveValue);

                            dataBase.editUpdateRemoteSettingTable(settingDataInDBID, "StrimConfigCh"+(currentChannelIndex+1)+"Lower"
                                    , settingData.getStrimConfigItem(currentChannelIndex).getLower());
                            DebugLog.d("SQL","tView.getId():   "+tempControlBar.getId());
                            break;

                        case R.id.RemoteControlChannelSetting_SpecialImageView_middleLimitBar:

                            settingData.getStrimConfigItem(currentChannelIndex).setMiddle(strSaveValue);

                            dataBase.editUpdateRemoteSettingTable(settingDataInDBID, "StrimConfigCh"+(currentChannelIndex+1)+"Middle"
                                    , settingData.getStrimConfigItem(currentChannelIndex).getMiddle());
                            DebugLog.d("SQL","tView.getId():   "+tempControlBar.getId());
                            break;

                        case R.id.RemoteControlChannelSetting_SpecialImageView_upperLimitBar:

                            settingData.getStrimConfigItem(currentChannelIndex).setUpper(strSaveValue);

                            dataBase.editUpdateRemoteSettingTable(settingDataInDBID, "StrimConfigCh"+(currentChannelIndex+1)+"Upper"
                                    , settingData.getStrimConfigItem(currentChannelIndex).getUpper());
                            DebugLog.d("SQL","tView.getId():   "+tempControlBar.getId());
                            break;

                        case R.id.RemoteControlChannelSetting_SpecialImageView_failSafeBar:

                            settingData.getStrimConfigItem(currentChannelIndex).setFailSafe(strSaveValue);

                            dataBase.editUpdateRemoteSettingTable(settingDataInDBID, "StrimConfigCh"+(currentChannelIndex+1)+"FailSafe"
                                    , settingData.getStrimConfigItem(currentChannelIndex).getFailSafe());
                            DebugLog.d("SQL","tView.getId():   "+tempControlBar.getId());
                            break;
                    }

                    sendDataRunnable.setValue(SendDataRunnable.DEFAULT_VALUE);

                    handler.post(sendDataRunnable);

//                    //讓車子的輪子停止轉動
//                    if(thisApp.useWifiOrBluetooth)
//                    {
//                        FSskf.SendProtocol( Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle())
//                                , 0, 0);
//                    }
//                    else
//                    {
//                        bluetoothController.sendPotocalToBluetooth(Integer.valueOf(S_EmCs.getStrimConfigItem(0).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(1).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(2).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(3).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(4).getMiddle())
//                                , Integer.valueOf(S_EmCs.getStrimConfigItem(5).getMiddle())
//                                , 0, 0);
//                    }
                }

                tempControlBar = null;
            }
        }

        return super.onTouchEvent(event);
    }

    private class SendDataRunnable implements Runnable
    {
        public static final String DEFAULT_VALUE = "" + (int)(GRADUATION_END + GRADUATION_START) / 2;

        private String value = DEFAULT_VALUE;

        public void setValue(String value)
        {
            this.value = value;

            if(this.value == null || this.value.isEmpty())
            {
                this.value = "0";
            }
        }

        @Override
        public void run()
        {
            //下面怪怪的
            int channelIndex = bottomMenu.getPageIndex();

            byte[] commandData = null;

            switch(channelIndex + 1)
            {
                case 1:

                    bleController.setChannel1(Integer.valueOf(value));
                    bleController.setChannel2(Integer.valueOf(settingData.getStrimConfigItem(1).getMiddle()));
                    bleController.setChannel3(Integer.valueOf(settingData.getStrimConfigItem(2).getMiddle()));
                    bleController.setChannel4(Integer.valueOf(settingData.getStrimConfigItem(3).getMiddle()));
                    bleController.setChannel5(Integer.valueOf(settingData.getStrimConfigItem(4).getMiddle()));
                    bleController.setChannel6(Integer.valueOf(settingData.getStrimConfigItem(5).getMiddle()));
                    bleController.setChannel7(0);
                    bleController.setChannel8(0);

                    break;

                case 2:

                    bleController.setChannel1(Integer.valueOf(settingData.getStrimConfigItem(0).getMiddle()));
                    bleController.setChannel2(Integer.valueOf(value));
                    bleController.setChannel3(Integer.valueOf(settingData.getStrimConfigItem(2).getMiddle()));
                    bleController.setChannel4(Integer.valueOf(settingData.getStrimConfigItem(3).getMiddle()));
                    bleController.setChannel5(Integer.valueOf(settingData.getStrimConfigItem(4).getMiddle()));
                    bleController.setChannel6(Integer.valueOf(settingData.getStrimConfigItem(5).getMiddle()));
                    bleController.setChannel7(0);
                    bleController.setChannel8(0);

                    break;

                case 3:

                    bleController.setChannel1(Integer.valueOf(settingData.getStrimConfigItem(0).getMiddle()));
                    bleController.setChannel2(Integer.valueOf(settingData.getStrimConfigItem(1).getMiddle()));
                    bleController.setChannel3(Integer.valueOf(value));
                    bleController.setChannel4(Integer.valueOf(settingData.getStrimConfigItem(3).getMiddle()));
                    bleController.setChannel5(Integer.valueOf(settingData.getStrimConfigItem(4).getMiddle()));
                    bleController.setChannel6(Integer.valueOf(settingData.getStrimConfigItem(5).getMiddle()));
                    bleController.setChannel7(0);
                    bleController.setChannel8(0);

                    break;

                case 4:

                    bleController.setChannel1(Integer.valueOf(settingData.getStrimConfigItem(0).getMiddle()));
                    bleController.setChannel2(Integer.valueOf(settingData.getStrimConfigItem(1).getMiddle()));
                    bleController.setChannel3(Integer.valueOf(settingData.getStrimConfigItem(2).getMiddle()));
                    bleController.setChannel4(Integer.valueOf(value));
                    bleController.setChannel5(Integer.valueOf(settingData.getStrimConfigItem(4).getMiddle()));
                    bleController.setChannel6(Integer.valueOf(settingData.getStrimConfigItem(5).getMiddle()));
                    bleController.setChannel7(0);
                    bleController.setChannel8(0);

                    break;

                case 5:

                    bleController.setChannel1(Integer.valueOf(settingData.getStrimConfigItem(0).getMiddle()));
                    bleController.setChannel2(Integer.valueOf(settingData.getStrimConfigItem(1).getMiddle()));
                    bleController.setChannel3(Integer.valueOf(settingData.getStrimConfigItem(2).getMiddle()));
                    bleController.setChannel4(Integer.valueOf(settingData.getStrimConfigItem(3).getMiddle()));
                    bleController.setChannel5(Integer.valueOf(value));
                    bleController.setChannel6(Integer.valueOf(settingData.getStrimConfigItem(5).getMiddle()));
                    bleController.setChannel7(0);
                    bleController.setChannel8(0);

                    break;

                case 6:

                    bleController.setChannel1(Integer.valueOf(settingData.getStrimConfigItem(0).getMiddle()));
                    bleController.setChannel2(Integer.valueOf(settingData.getStrimConfigItem(1).getMiddle()));
                    bleController.setChannel3(Integer.valueOf(settingData.getStrimConfigItem(2).getMiddle()));
                    bleController.setChannel4(Integer.valueOf(settingData.getStrimConfigItem(3).getMiddle()));
                    bleController.setChannel5(Integer.valueOf(settingData.getStrimConfigItem(3).getMiddle()));
                    bleController.setChannel6(Integer.valueOf(value));
                    bleController.setChannel7(0);
                    bleController.setChannel8(0);

                    break;
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();

        bleController.startCommand();
    }

    @Override
    protected void onPause() {
        super.onPause();

        bleController.closeCommand();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isFinishing())
        {
            dataBase.Close();
        }
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        bleController.closeCommand();
        Intent intent = new Intent(this, RemoteControlSettingPage.class);
        startActivity(intent);
        finish();
    }
}
