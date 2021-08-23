package com.v7idea.tool;

/**
 * Created by mortal on 2017/3/9.
 *
 * 計算最後要輸出的數值的Model
 */
public class Channel
{
    private static final String TAG = Channel.class.getSimpleName();

    //各大畫面的主要設定
    private int ChannelId = 0;
    private String ChannelName = "";
    private float minimunValue = Constants.Channel.MINIMUM_VALUE;
    private float middleValue = Constants.Channel.MIDDLE_VALUE;
    private float maximunValue = Constants.Channel.MAXMUN_VALUE;

    private int ExpotentailSelect = 1;
    private int oldValue = (int)Constants.Channel.MIDDLE_VALUE;
    private float safeModeValue = (int)Constants.Channel.MIDDLE_VALUE;

    private boolean Reverse = false;


    //最終輸出值會用倒的參數
    // private float AreaWidthHalf;
    private float InputRange; //  用來表示輸入的刻度區間	; 如果是1000，那輸入值將是-1000 到 1000

    private float ChannelValue = 0;
    private boolean MixingConfigReverse = false;
    private Channel MixingChannel = null;
    private int parentCoordinateValue = 0;
    private boolean isGetValueFromParent = false;

    private float lowScaleValue = 0.0f;			// 由最小直到中間值的刻度
    private float highScaleValue = 0.0f;			// 由最大值到最小值的刻度


    //2014/01/06 祐鑫增加
//    private float lowAngleRage = 0.0f;
//    private float highAngleRage = 0.0f;

    public Channel(int inputRange)
    {
        this.InputRange = inputRange;
        calculateScaleValue();
    }

    public Channel(int thisChannelId, String thisChannelName, float Lower, float Middle, float Upper, int ExpotentailSelect, boolean Reverse
            , boolean MixingConfigReverse, float safeModeValue)
    {
        ChannelId = thisChannelId;
        ChannelName = thisChannelName;
        this.minimunValue = Lower;
        this.middleValue = Middle;
        this.maximunValue = Upper;
        this.ExpotentailSelect = ExpotentailSelect;
        this.Reverse = Reverse;
        //this.AreaWidthHalf = AreaWidthHalf;
        this.InputRange = Constants.Channel.RANGE;
        ChannelValue = Middle;
        this.MixingConfigReverse = MixingConfigReverse;
        this.safeModeValue = safeModeValue;

        calculateScaleValue();
    }

    public static Channel[] createInstance(int intNumber, int inputRange)
    {
        Channel[] channelArray = new Channel[intNumber];

        for(int i = 0 ; i < intNumber ; i++)
        {
            channelArray[i] = new Channel(inputRange);
            channelArray[i].setChannelId(i);
            channelArray[i].rest();
        }

        return channelArray;
    }

    public static Channel createInstance(int inputRange){
        return new Channel(inputRange);
    }

    /***
     * 取得目前這個Channel的數值
     * @return
     */
    public float getChannelValue()
    {
        return ChannelValue;
    }

    /**
     * 取得設定好的failSave數值;
     * @return fail save data
     */
    public float getFailSaveData() {

        return safeModeValue;

    }

    public int getExpotentailSelect()
    {
        return ExpotentailSelect;
    }

    public float getOldValue()
    {
        return oldValue;
    }

//    public float getLowAngleRage()
//    {
//        return lowAngleRage;
//    }
//
//    public float getHighAngleRage()
//    {
//        return highAngleRage;
//    }

    public boolean isReverse()
    {
        return Reverse;
    }

    /***
     * 取得目前的channelId
     * @return
     */
    public int getChannelId() {
        return ChannelId;
    }

    /***
     * 取得Channel名字
     * @return
     */
    public String getChannelName() {
        return ChannelName;
    }

    /***
     * 取得目前的最小值設定
     * @return 最小值
     */
    public float getMinimunValue() {

        return minimunValue;

    }

    /***
     * 取得目前的中間值設定
     * @return 中間值
     */
    public float getMiddleValue() {

        return middleValue;

    }

    /***
     * 計算目前的刻度
     */
    public void calculateScaleValue() {

        lowScaleValue = (middleValue -  minimunValue) / InputRange;
        highScaleValue = (maximunValue -  middleValue) / InputRange;

//        //2013/01/06 祐鑫增加
//        lowAngleRage = middleValue -  minimunValue;
//        highAngleRage = maximunValue -  middleValue;
    }

    /***
     * 重新設定這個Channel，回復初始狀態
     */
    public void rest()
    {
        calculateResult(0);
    }

    /***
     * 設定這個Channel的數值
     * @param value
     */
    public void setChannelValue(float value)
    {
        ChannelValue = value;
    }

    public void setChannelChild(Channel ChannelChild)
    {
        if(MixingChannel != null)
        {
//			Log.d("Channel", "this channel is set:"+this.MixingChannel.getChannelName());
            this.MixingChannel = null;
        }

        this.MixingChannel = ChannelChild;
    }

    /***
     * 設定Channel名字
     * @param channelName
     */
    public void setChannelName(String channelName) {
        ChannelName = channelName;
    }

    /***
     * 設定Channel Id
     * @param channelId
     */
    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    /***
     *  設定這個Channel的最小值設定
     * @param value
     */
    public void setMinimunValue(float value) {

        minimunValue = value;
        calculateScaleValue() ;

    }

    public void calculateResult(int Coordinate, boolean callByParent) {

        if(callByParent)  {

            int newValue = 0;

            if(isGetValueFromParent) {			// 如果有從Mix得到數值，那就以父親的數值為主;

                newValue = (parentCoordinateValue + oldValue );

                if( newValue > (int) InputRange) {

                    newValue = (int) InputRange;					//

                } else if(newValue < (int)( -1.0f * InputRange)) {

                    newValue = (int) (-1.0f * InputRange);

                }

            } else {

                newValue = oldValue;

            }

            calExpontenialValue(newValue);

        }

    }

    //數值的計算這處理,如何限制輸入在MyRemoteControler
    public void calculateResult(int Coordinate)
    {
        if(oldValue != Coordinate)
        {

            oldValue = Coordinate;

        }

        int newValue = 0;

        if(isGetValueFromParent) {			// 如果有從Mix得到數值，那就以父親的數值為主;

            newValue = (parentCoordinateValue + Coordinate );

            if( newValue > (int) InputRange) {

                newValue = (int) InputRange;					//

            } else if(newValue < (int)( -1.0f * InputRange)) {

                newValue = (int) (-1.0f * InputRange);

            }

        } else {

            newValue = Coordinate;

        }

//		 Log.d("Coordinate","Coordinate: "+Coordinate);

        //ServerReverse的反相
        if(!Reverse)
        {
            //ChannelValue = (maximunValue - ChannelValue) + minimunValue;
            newValue = newValue * -1;
        }

//		//MixingConfigReverse的反相
//				if(MixingConfigReverse)
//				{
//					// ChannelValue = (maximunValue - ChannelValue) + minimunValue;
//					newValue = newValue * -1;
//
//				}

        if(this.MixingChannel != null)
        {


            if(MixingConfigReverse == true) {

                MixingChannel.setParentCoordinate(newValue);
                MixingChannel.calculateResult(newValue, true);

            } else {

                MixingChannel.setParentCoordinate(newValue * -1);
                MixingChannel.calculateResult(newValue * -1,  true);

            }


        }

        calExpontenialValue(newValue);

    }

    /***
     * 設定這個Channel的中間值
     * @param value 中間值
     */
    public void setMiddleValue(float value) {

        middleValue = value;
        calculateScaleValue() ;

    }

    /***
     * 取得這個頻道的最大值
     * @return 這個頻道的最大值
     */
    public float getMaximunValue() {

        return maximunValue;

    }

    /***
     * 設定這個頻道的最大值
     * @param value 最大值
     */
    public void setMaximunValue(float value) {

        maximunValue = value;
        calculateScaleValue() ;
    }


    /***
     * 設定這個父親傳送過來的數值
     * @param value
     */
    public void setParentCoordinate(int value) {

        parentCoordinateValue = value;
        isGetValueFromParent = true;

    }

    public void setExpotentailSelect(int intExpotentailSelect)
    {
        ExpotentailSelect = intExpotentailSelect;

//        DebugLog.e(TAG, "ExpotentailSelect: "+ExpotentailSelect);
    }


    public void calExpontenialValue(int newValue) {


        // ExpotentailSelect:
        // 1. 線性
        // 2. 慢速
        // 3. 慢速x2
        // 4. 快速
        // 5. 快速x2


        if(newValue == 0) {		// 表示在正中間，那就是middleValue;

            ChannelValue = middleValue;

        } else
        {
            switch(ExpotentailSelect)
            {

                case 1:	// 第一種線性的曲線

                    if(newValue > 0)
                    {
                        ChannelValue = middleValue + (newValue * highScaleValue) ; //未反向
                    }
                    else
                    {
                        ChannelValue = middleValue + (newValue * lowScaleValue); //未反向
                    }

                    break;

                case 2: //慢速的線性曲線

                    if(newValue > 0)
                    {
                        newValue = (SlowDownValue(newValue) + newValue) / 2;
                        ChannelValue = middleValue + (newValue * highScaleValue);
                    }
                    else
                    {
                        newValue =( (-1 * SlowDownValue((-1 * newValue))) + newValue ) / 2;
                        ChannelValue = middleValue + (newValue * lowScaleValue);
                    }


                    break;

                case 3: //慢速x2的線性曲線

                    if(newValue > 0)
                    {
                        newValue = SlowDownValue(newValue);
                        ChannelValue = middleValue + (newValue * highScaleValue) ;

                    }
                    else
                    {
                        newValue =  -1 * SlowDownValue( newValue);
                        ChannelValue = middleValue + (newValue * lowScaleValue) ;
                    }

                    break;

                case 4: //快速的線性曲線

                    if(newValue > 0)
                    {
                        newValue =  (speedUpValue(newValue) +  newValue) / 2;
                        ChannelValue = middleValue + (newValue * highScaleValue);
                    }
                    else
                    {
                        newValue = ((-1 * speedUpValue((-1 * newValue))) +  newValue) / 2;
                        ChannelValue = middleValue + (newValue * lowScaleValue);
                    }

                    break;

                case 5: //快速x2的線性曲線

                    if(newValue > 0)
                    {
                        newValue = speedUpValue(newValue);
                        ChannelValue = middleValue + (newValue * highScaleValue);
                    }
                    else
                    {
                        newValue = -1 * speedUpValue((-1 * newValue));
                        ChannelValue = middleValue + (newValue * lowScaleValue);
                    }

                    break;
            }
        }
    }

    /**
     *  有關於這個expertienal的數值改變，加快;
     * @param thisValue
     * @return
     */

    public int speedUpValue(float thisValue) {

        float resultValue = 0.0f;

        resultValue = thisValue / 10.0f ;
        resultValue = (float) (Math.sqrt(resultValue) * 100.0f);

        return (int) resultValue;

    }


    /**
     *  有關於這個expertienal的數值改變，變慢;
     * @param thisValue
     * @return
     */
    public int SlowDownValue(float thisValue) {

        float resultValue = 0.0f;

        resultValue = thisValue / 10.0f ;
        resultValue = resultValue * resultValue * 0.1f;

        return (int) resultValue;
    }
}
