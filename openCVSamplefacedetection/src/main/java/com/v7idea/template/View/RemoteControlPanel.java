package com.v7idea.template.View;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by mortal on 2017/3/9.
 */

public class RemoteControlPanel extends RelativeLayout
{
    private static final String TAG = RemoteControlPanel.class.getSimpleName();

    /**
     * 操控模式，汽車的加速模式
     */
    public static final int TYPE_CAR_ACCELERATOR = 0;

    /**
     * 操控模式，汽車的方向模式
     */
    public static final int TYPE_CAR_DIRECTION = 1;

    /**
     * 表示該元件未被手指按壓
     */
    public static final int STATUS_UN_TOUCH = -1;

    /**
     * 表示該元件被手指按壓
     */
    public static final int STATUS_TOUCH_DOWN = 0;

    /**
     * 表示該元件被手指按壓且手指移動中
     */
    public static final int STATUS_TOUCH_MOVE = 1;

    /**
     * 總刻度
     */
    public static final float TotalGraduation = 2000f;

    /**
     * 總刻度的中間值
     */
    public static final float HalfTotalGraduation = TotalGraduation / 2f;

    /**
     * 操控模式，預設為汽車的加速模式
     */
    private int type = TYPE_CAR_ACCELERATOR;

    /**
     * 該元件在營幕上，距離營幕左側邊緣的距離
     */
    private int marginLeft = 0;

    /**
     * 該元件在營幕上，距離營幕上方邊緣的距離
     */
    private int marginTop = 0;

    /**
     * 該元件在營幕上的右邊緣，距離營幕左側邊緣的距離
     */
    private int marginRight = 0;

    /**
     * 該元件在營幕上的下邊緣，距離營幕上方邊緣的距離
     */
    private int marginBottom = 0;

    /**
     * 要換算的View的寬度
     */
    private float viewWidth  = 0;

    /**
     * 要換算的View的高度
     */
    private float viewHeight = 0;

    /**
     * 水平刻度
     */
    private float horizontalScale = 0f;

    /**
     * 垂直刻度
     */
    private float verticalScale = 0f;

    //控制點的參數
    private int ControlPointHeightRadius = 0;
    private int ControlPointWidthRadius = 0;
    private int ControlPointHeight = 0;
    private int ControlPointWidth = 0;

    /**
     * 手指按壓在View上的記錄的點
     */
    private PointF firstTouchPoint;

    /**
     * 手指按壓在View上移動的點
     */
    private PointF currentTouchPoint;

    /**
     * 中心點的位置
     */
    private PointF centerPoint = null;

    /**
     * 手指按壓的點，離中心點的距離
     */
    private PointF offSet = null;

    private int status = STATUS_UN_TOUCH;

    /**
     * 輸出力道
     */
    private float powerRate = 100.0f;

    public RemoteControlPanel(Context context) {
        super(context);
    }

    public RemoteControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteControlPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        //取得元件在營幕上4個角的位置
        marginLeft = l;
        marginTop = t;
        marginRight = r;
        marginBottom = b;

        //換算成長寬
        viewWidth = marginRight - marginLeft;
        viewHeight = marginBottom - marginTop;

        //計算刻度
        horizontalScale = TotalGraduation / viewWidth;
        verticalScale = TotalGraduation / viewHeight;

        //計算並記錄中心點的位置，目地是用來將第一個壓下去的點的位置轉換成中心點
        float centerPointX = viewWidth / 2.0f;
        float centerPointY = viewHeight / 2.0f;

        if(centerPoint == null)
        {
            centerPoint = new PointF(centerPointX, centerPointY);
        }
        else
        {
            centerPoint.set(centerPointX, centerPointY);
        }

//        DebugLog.d(TAG, getTag() + "centerPointX: "+centerPointX);
//        DebugLog.d(TAG, getTag() + "centerPointY: "+centerPointY);
//        DebugLog.d(TAG, getTag() + "marginLeft: "+marginLeft);
//        DebugLog.d(TAG, getTag() + "marginTop: "+marginTop);
//        DebugLog.d(TAG, getTag() + "marginRight: "+marginRight);
//        DebugLog.d(TAG, getTag() + "marginBottom: "+marginBottom);
//        DebugLog.d(TAG, getTag() + "viewWidth: "+viewWidth);
//        DebugLog.d(TAG, getTag() + "viewHeight: "+viewHeight);
//        DebugLog.d(TAG, getTag() + "horizontalScale: "+horizontalScale);
//        DebugLog.d(TAG, getTag() + "verticalScale: "+verticalScale);
    }

    /**
     * 取得該元件的模式
     * @return TYPE_CAR_ACCELERATOR or TYPE_CAR_DIRECTION
     */
    public int getType() {
        return type;
    }

    /**
     * 取得目前該元件手指被按壓的狀態
     * @return
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * 依據座標，將座標轉換成長 TotalGraduation 的刻度表上的數值 （0～TotalGraduation）
     * @param floatPointX 從touch event 中取得手指當下的座標值X
     * @param floatPointY 從touch event 中取得手指當下的座標值Y
     */
    public void calculateSendPoint(float floatPointX, float floatPointY)
    {
        if(status == STATUS_TOUCH_DOWN || status == STATUS_TOUCH_MOVE)
        {
            //將取得的座標減去偏移量
//            DebugLog.d(TAG, getTag() + " floatPointX: " + floatPointX);

            //將傳進來的點，減去偏移量，轉換成中心點開始移動的點
            float newX = floatPointX - offSet.x;
            float newY = floatPointY - offSet.y;

//            DebugLog.d(TAG, getTag() + " newX: " + newX);

            float lowerLimitX = readLowerLimitX();
            float lowerLimitY = readLowerLimitY();

//            DebugLog.e(TAG, getTag() + "lowerLimitX: " + lowerLimitX);
//            DebugLog.e(TAG, getTag() + "lowerLimitY: " + lowerLimitY);

            newX = checkValueXRange(type, newX, lowerLimitX);
            newY = checkValueYRange(type, newY, lowerLimitY);

            //TODO 如果有放上控制球，位移的部分在這處理
            changeControlBallPosition(floatPointX, floatPointY);

            newY = countCoordinateYByPowerRate(newY);
            newX = countCoordinateXByPowerRate(newX);

//            DebugLog.d(TAG, getTag() + " newX: " + newX);

            if(currentTouchPoint == null)
            {
                currentTouchPoint = new PointF(0, 0);
            }

            /**
             * 將中心點開始移動的點轉換成
             *
             * 0                         2000
             * |---------------------------|
             *
             * 這樣的刻度
             */
            currentTouchPoint.x = newX * horizontalScale;
            currentTouchPoint.y = newY * verticalScale;

//            DebugLog.e(TAG, getTag() + "lowerLimitX: " + lowerLimitX);
//            DebugLog.e(TAG, getTag() + "lowerLimitY: " + lowerLimitY);
//            DebugLog.d(TAG, getTag() + " currentTouchPoint.x: " + currentTouchPoint.x);
//            DebugLog.d(TAG, getTag() + "currentTouchPoint.y: " + currentTouchPoint.y);

            /**
             * 將刻度
             * 0                         2000
             * |---------------------------|
             * 轉換成
             * -1000                    -1000
             * |---------------------------|
             * 這樣的刻度
             */
            currentTouchPoint = changeToStandardScale(currentTouchPoint);
        }
    }

    public PointF getCurrentTouchPoint()
    {
        return currentTouchPoint;
    }

    public int getScaleOffsetX()
    {
        return (int)(currentTouchPoint.x) / 10;
    }

    public int getScaleOffsetY()
    {
        return (int)(currentTouchPoint.y) / 10;
    }

    private float readLowerLimitX()
    {
        float value = 0;

        switch (type)
        {
            case TYPE_CAR_ACCELERATOR:
                value = (viewWidth/2) - (ControlPointWidthRadius);
                break;

            case TYPE_CAR_DIRECTION:
                value = viewWidth - ControlPointWidth;
                break;
        }

        return value;
    }

    private float readLowerLimitY()
    {
        float value = 0;

        switch (type)
        {
            case TYPE_CAR_ACCELERATOR:
                value = viewHeight - (ControlPointHeight);
                break;

            case TYPE_CAR_DIRECTION:
                value = (viewHeight/2) - (ControlPointHeightRadius);
                break;
        }

        return value;
    }

    private float checkValueXRange(int type, float floatValueX, float floatLimitValue)
    {
        switch (type)
        {
            case TYPE_CAR_ACCELERATOR:
                floatValueX = floatLimitValue;
                break;

            case TYPE_CAR_DIRECTION:
                if(floatValueX < 0)
                {
                    floatValueX = 0;
                }

                if(floatValueX > floatLimitValue)
                {
                    floatValueX = floatLimitValue;
                }
                break;
        }

        return floatValueX;
    }

    private float checkValueYRange(int type, float floatValueY, float floatLimitValue)
    {
        switch (type)
        {
            case TYPE_CAR_ACCELERATOR:
                if(floatValueY < 0)
                {
                    floatValueY = 0;
                }

                if(floatValueY > floatLimitValue)
                {
                    floatValueY = floatLimitValue;
                }
                break;

            case TYPE_CAR_DIRECTION:
                floatValueY = floatLimitValue;
                break;
        }

        return floatValueY;
    }

    private void changeControlBallPosition(float floatValueX, float floatValueY)
    {

    }

    private float countCoordinateXByPowerRate(float floatValueX)
    {
        if(floatValueX - centerPoint.x != 0 && powerRate != 100.0f) {

            floatValueX = centerPoint.x + (floatValueX - centerPoint.x) * powerRate / 100.0f;
        }

        return floatValueX;
    }

    private float countCoordinateYByPowerRate(float floatValueY)
    {
        if(floatValueY - centerPoint.y != 0 && powerRate != 100.0f) {

            return centerPoint.y  + (floatValueY - centerPoint.y) * powerRate / 100.0f;
        }

        return floatValueY;
    }

    /**
     * 取出轉換後的刻度
     *
     * 將刻度
     * 0                         2000
     * |---------------------------|
     * 轉換成
     * -1000                    -1000
     * |---------------------------|
     * @param coordinate
     * @return
     */
    public PointF changeToStandardScale(PointF coordinate)
    {
        if(coordinate != null)
        {
            float thisX = coordinate.x;
            float thisY = coordinate.y;

//            DebugLog.d(TAG, "coordinateX: " + thisX);

            thisX = thisX - HalfTotalGraduation;
            thisY = thisY - HalfTotalGraduation;

//            DebugLog.d(TAG, "after minus coordinateX: " + thisX);
//            DebugLog.d(TAG, "coordinateY: " + thisY);

            coordinate.set(thisX, thisY);

            return coordinate;
        }
        else
        {
            return null ;
        }
    }

    /**
     * 設定該元件的運作模式
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 記錄手指按壓的點，並計算偏移量，目地是將手指按壓的點轉換成中心點
     * @param floatValueX 從touch event 中取得手指當下的座標值X
     * @param floatValueY 從touch event 中取得手指當下的座標值Y
     */
    public void setFirstTouchPoint(float floatValueX, float floatValueY)
    {
        if(offSet == null)
        {
            offSet = new PointF(0, 0);
        }

        if(firstTouchPoint == null)
        {
            firstTouchPoint = new PointF(floatValueX, floatValueY);
        }
        else
        {
            firstTouchPoint.set(floatValueX, floatValueY);
        }

        offSet.set((firstTouchPoint.x - centerPoint.x), (firstTouchPoint.y - centerPoint.y));
    }

    /***
     * 設定為手指按到
     */
    public void setTouch(){
        status = STATUS_TOUCH_DOWN;

//        DebugLog.d(TAG, getTag() + " STATUS_TOUCH_DOWN");
    }

    /***
     * 設定為手指沒有按到
     */
    public void setUnTouch() {
        status = STATUS_UN_TOUCH;

//        DebugLog.d(TAG, getTag() + " STATUS_UN_TOUCH");
    }

    /***
     * 設定為手指中元件上移動中
     */
    public void setTouchMove()
    {
        status = STATUS_TOUCH_MOVE;

//        DebugLog.d(TAG, getTag() + " STATUS_TOUCH_MOVE");
    }

    /**
     * 將記錄輸的出數值歸回原點
     */
    public void reset()
    {
        currentTouchPoint.x = (firstTouchPoint.x - centerPoint.x) * horizontalScale;
        currentTouchPoint.y = (firstTouchPoint.y - centerPoint.y) * verticalScale;
    }
}