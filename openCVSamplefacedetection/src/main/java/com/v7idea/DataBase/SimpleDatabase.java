package com.v7idea.DataBase;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.samples.facedetect.Air;

/**
 * Created by mortal on 15/3/12.
 */
public class SimpleDatabase {
    private String tag = "DefaultSetting";

    private Context context;
    private SharedPreferences mDataBase = null;

    public SimpleDatabase() {
        this.context = Air.getAppContext();
        init();
    }

    public SimpleDatabase(String tableName) {
        this.context = Air.getAppContext();
        init(tableName);
    }

    private void init(String tableName) {
        if (mDataBase == null) {
            tag = tableName;
            mDataBase = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        }
    }

    private void init() {
        if (mDataBase == null) {
            mDataBase = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        }
    }

    public JSONArray getJSONArrayByKeyIfNullReturnNull(String strKeyName) {
        init();

        String strKeyData = mDataBase.getString(strKeyName, "");

        JSONArray DataJSON = null;

        try {
            DataJSON = new JSONArray(strKeyData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return DataJSON;
    }

    public JSONObject getJSONObjectByKeyIfNullReturnEmpty(String keyName) {
        init();

        String strKeyData = mDataBase.getString(keyName, "");

        JSONObject DataJSON = null;

        try {
            DataJSON = new JSONObject(strKeyData);
        } catch (JSONException e) {
            e.printStackTrace();
            DataJSON = new JSONObject();
        }

        return DataJSON;
    }

    public void setValueByKey(String keyName, long value) {
        init();

        SharedPreferences.Editor editor = mDataBase.edit();
        editor.putLong(keyName, value);
        editor.commit();
    }

    public void setValueByKey(String keyName, String value) {
        init();

        SharedPreferences.Editor editor = mDataBase.edit();
        editor.putString(keyName, value);
        editor.commit();
    }

    public void setValueByKey(String keyName, boolean value) {
        init();

        SharedPreferences.Editor editor = mDataBase.edit();
        editor.putBoolean(keyName, value);
        editor.commit();
    }

    public void setValueByKey(String keyName, int value) {
        init();

        SharedPreferences.Editor editor = mDataBase.edit();
        editor.putInt(keyName, value);
        editor.commit();
    }

    public String getStringValueByKey(String keyName, String defaultValue) {
        init();
        return mDataBase.getString(keyName, defaultValue);
    }

    public boolean getBooleanValueByKey(String keyName, boolean defaultValue) {
        init();
        return mDataBase.getBoolean(keyName, defaultValue);
    }

    public int getIntValueByKey(String keyName, int defaultValue) {
        init();
        return mDataBase.getInt(keyName, defaultValue);
    }

    public long getLongValueByKey(String keyName, long defaultValue) {
        init();
        return mDataBase.getLong(keyName, defaultValue);
    }

    public void clearDataBase() {
        init();

        SharedPreferences.Editor editor = mDataBase.edit();
        editor.clear();
        editor.commit();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
    {
        init();
        mDataBase.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
    {
        init();
        mDataBase.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
}
