package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.serenegiant.usb.UVCCamera;
import com.v7idea.DataBase.DataBase;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.tool.CVCameraWrapper;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.DebugLog;
import com.v7idea.v7rcliteandroidsdk.V7RCLiteController;

import java.util.ArrayList;


/**
 * Created by mortal on 2017/3/10.
 */

public class Air extends Application
{
    private static final String TAG = Air.class.getSimpleName();
    private String targetDeviceMacAddress = "";

    /**
     * why: 這個是用來記錄並修改發送資料的時間
     * 發送資料的延遲時間
     */
    public static int SEND_DATA_DELAY_TIME = Constants.DEFAULT_SEND_DATA_DELAY_TIME;

    private static Context appContext = null;
    private CustomerSetting currentSetting = null;
    private static V7RCLiteController bleController = null;

    public String targetIP = null;

    public Client client = null;

    public UVCCamera mUVCCamera = null;

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e(TAG, "onTerminate !!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        bleController = new V7RCLiteController(this);
        SimpleDatabase simpleDatabase = new SimpleDatabase();

        SEND_DATA_DELAY_TIME = simpleDatabase.getIntValueByKey(Constants.Setting.DELAY_TIME
                , Constants.DEFAULT_SEND_DATA_DELAY_TIME);

//載入資料庫相關類別
        DataBase appDB = new DataBase(this);
        appDB.Open();

        Cursor thisCursor =  appDB.getAllCustmerSettingCursor(DataBase.RemoteSettingTableName);
        boolean ifNeedAddData = true;
        int totalCustomerNum = 0;

        if(thisCursor != null) {

            totalCustomerNum = thisCursor.getCount();

            if(totalCustomerNum > 0) {

                ifNeedAddData = false;

            }

            thisCursor.close();

        }

        if(ifNeedAddData) {

            appDB.insertRec(DataBase.RemoteSettingTableName, "car", "My first 2CH");				// 新增加一筆汽車
            appDB.insertRecForEZSeries(DataBase.RemoteSettingTableName, "car", "EZ Series");	// 新增加一筆汽車，使用EzSeries的資料庫
            appDB.insertRec(DataBase.RemoteSettingTableName, "airplane", "My first 6CH");		// 新增加一筆飛機

            thisCursor =  appDB.getAllCustmerSettingCursor(DataBase.RemoteSettingTableName);

            if(thisCursor != null && thisCursor.getCount() > 0) {		// 找到已經存入的資料

                thisCursor.moveToFirst();
                currentSetting = appDB.getRecord(thisCursor);

                DebugLog.d(TAG, "get default setting!!");

                simpleDatabase.setValueByKey(Constants.Setting.DEFAULT_NAME, currentSetting.getName());
                simpleDatabase.setValueByKey(Constants.Setting.DEFAULT_KIND, currentSetting.getKind());

                thisCursor.close();

            }

        }

        appDB.Close();
    }

    public static Context getAppContext()
    {
        return appContext;
    }

    /**
     * 取得目前的使用設定
     *
     * @param dataBase 當Application裡的 CustomerSetting 為null時，可以再用DataBase重抓
     * @return CustomerSetting
     */
    public CustomerSetting getCurrentSetting(DataBase dataBase)
    {
        if(currentSetting == null)
        {
            SimpleDatabase simpleDatabase = new SimpleDatabase();
            String defaultName = simpleDatabase.getStringValueByKey(Constants.Setting.DEFAULT_NAME, "My first 2CH");
            String defaultKind = simpleDatabase.getStringValueByKey(Constants.Setting.DEFAULT_KIND, "car");

            DebugLog.d(TAG, "defaultName: "+defaultName);
            DebugLog.d(TAG, "defaultKind: "+defaultKind);

            currentSetting = dataBase.getFirstCustomerSetting(DataBase.RemoteSettingTableName, defaultName, defaultKind);
        }

        return currentSetting;
    }

    public String getTargetDeviceMacAddress()
    {
        if(targetDeviceMacAddress != null)
        {
            return targetDeviceMacAddress;
        }
        else
        {
            return "";
        }
    }

    public V7RCLiteController getBleController()
    {
        SimpleDatabase simpleDatabase = new SimpleDatabase();

        bleController.DEBUG = false;
        bleController.setScanTime(2);
        bleController.setCommandPeriod(simpleDatabase.getIntValueByKey(Constants.Setting.DELAY_TIME, Air.SEND_DATA_DELAY_TIME));
        bleController.setCurrentWriteCharacteristicType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

        return bleController;
    }

    public void setBleController(V7RCLiteController bleController)
    {
        this.bleController = bleController;
    }

    public void setTargetDeviceMacAddress(String strTargetDeviceAddress)
    {
        this.targetDeviceMacAddress = strTargetDeviceAddress;
    }

    /***
     * 目前的使用設定
     * @param currentSetting 用戶的設定
     */
    public void setCurrentSetting(CustomerSetting currentSetting) {
        this.currentSetting = currentSetting;

        SharedPreferences mDefault = getSharedPreferences("DefaultSetting", Activity.MODE_PRIVATE);

        SharedPreferences.Editor thisEditor = mDefault.edit();
        thisEditor.putString("DefaultName", currentSetting.getName());
        thisEditor.putString("DefaultKind", currentSetting.getKind());
        thisEditor.commit();
    }
}
