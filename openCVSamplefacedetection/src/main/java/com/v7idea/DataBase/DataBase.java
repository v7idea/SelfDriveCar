package com.v7idea.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.v7idea.Data.SaveRecord;
import com.v7idea.tool.CVCameraWrapper;
import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.DebugLog;
import com.v7idea.tool.ExpotentialMode;
import com.v7idea.tool.MaxingConfig;
import com.v7idea.tool.ServoReverse;
import com.v7idea.tool.StrimConfig;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/***
 * 這個是用來設定儲存使用者自定的資料
 * @author louischuang
 *
 */
public class DataBase
{
	private static final String TAG = "DataBase";

	private static SQLiteOPenHelper MSP;
	private Context context;
	private final int mTotalChannel = 8;
	private SQLiteDatabase db;

	public static final String MySettingTableName = "MySetting";
	public static final String RemoteSettingTableName = "RemoteSetting";

	public static final String VIDEO_DETECT_VALUE_TABLE = "DetectValueTable";

	private String DetectionValueTable = "CREATE TABLE IF NOT EXISTS " + VIDEO_DETECT_VALUE_TABLE + "("+
			"id       INTEGER PRIMARY KEY ASC AUTOINCREMENT,"+
			"fileName TEXT,"+
			"channel1Value INTEGER,"+
			"channel2Value INTEGER,"+
			"picture BLOB,"+ //存成 bitmap 的 byte array
			"saveTime INTEGER," +
			"grayPixel TEXT);";

	private String MySetting = "CREATE TABLE IF NOT EXISTS "+MySettingTableName+"("+
			   "id       INTEGER PRIMARY KEY ASC AUTOINCREMENT,"+ //0
			   "Kind                    TEXT,"+ //1
			   "Name                    TEXT,"+ //2

			   "ExpotentialModeCh1      TEXT,"+ //3
			   "ExpotentialModeCh2      TEXT,"+ //4
			   "ExpotentialModeCh3      TEXT,"+ //5
			   "ExpotentialModeCh4      TEXT,"+ //6
			   "ExpotentialModeCh5      TEXT,"+ //7
			   "ExpotentialModeCh6      TEXT,"+ //8
			   "ExpotentialModeCh7      TEXT,"+ //9
			   "ExpotentialModeCh8      TEXT,"+ //10

			   "MixingConfigCH1         TEXT,"+ //11
			   "MixingConfigCH2         TEXT,"+ //12
			   "MixingConfigCH3         TEXT,"+ //13
			   "MixingConfigCH4         TEXT,"+ //14
			   "MixingConfigCH5         TEXT,"+ //15
			   "MixingConfigCH6         TEXT,"+ //16
			   "MixingConfigCH7         TEXT,"+ //17
			   "MixingConfigCH8         TEXT,"+ //18

			   "ServoReverseCh1         TEXT,"+ //19
			   "ServoReverseCh2         TEXT,"+ //20
			   "ServoReverseCh3         TEXT,"+ //21
			   "ServoReverseCh4         TEXT,"+ //22
			   "ServoReverseCh5         TEXT,"+ //23
			   "ServoReverseCh6         TEXT,"+ //24
			   "ServoReverseCh7         TEXT,"+ //25
			   "ServoReverseCh8         TEXT,"+ //26

			   "ScreenControlMode       TEXT,"+ //27
			   "MixingConfigReverse     TEXT,"+ //28

			   "StrimConfigCh1Upper     TEXT,"+ //29
			   "StrimConfigCh1Lower     TEXT,"+ //30
			   "StrimConfigCh1Middle    TEXT,"+ //31
			   "StrimConfigCh1FailSafe  TEXT,"+ //32

			   "StrimConfigCh2Upper     TEXT,"+ //33
			   "StrimConfigCh2Lower     TEXT,"+ //34
			   "StrimConfigCh2Middle    TEXT,"+ //35
			   "StrimConfigCh2FailSafe  TEXT,"+ //36

			   "StrimConfigCh3Upper     TEXT,"+ //37
			   "StrimConfigCh3Lower     TEXT,"+ //38
			   "StrimConfigCh3Middle    TEXT,"+ //39
			   "StrimConfigCh3FailSafe  TEXT,"+ //40

			   "StrimConfigCh4Upper     TEXT,"+ //41
			   "StrimConfigCh4Lower     TEXT,"+ //42
			   "StrimConfigCh4Middle    TEXT,"+ //43
			   "StrimConfigCh4FailSafe  TEXT,"+ //44

			   "StrimConfigCh5Upper     TEXT,"+ //45
			   "StrimConfigCh5Lower     TEXT,"+ //46
			   "StrimConfigCh5Middle    TEXT,"+ //47
			   "StrimConfigCh5FailSafe  TEXT,"+ //48

			   "StrimConfigCh6Upper     TEXT,"+ //49
			   "StrimConfigCh6Lower     TEXT,"+ //50
			   "StrimConfigCh6Middle    TEXT,"+ //51
			   "StrimConfigCh6FailSafe  TEXT,"+ //52

			   "StrimConfigCh7Upper     TEXT,"+ //53
			   "StrimConfigCh7Lower     TEXT,"+ //54
			   "StrimConfigCh7Middle    TEXT,"+ //55
			   "StrimConfigCh7FailSafe  TEXT,"+ //56

			   "StrimConfigCh8Upper     TEXT,"+ //57
			   "StrimConfigCh8Lower     TEXT,"+ //58
			   "StrimConfigCh8Middle    TEXT,"+ //59
	           "StrimConfigCh8FailSafe  TEXT,"+ //60

	           "IfReverseCamera         TEXT,"+ //61
	           "isAutoResetTHR          TEXT,"+ //62

	           "isAutoResetToChannel1   TEXT,"+ //63
	           "isAutoResetToChannel2   TEXT,"+ //64
	           "isAutoResetToChannel3   TEXT,"+ //65
	           "isAutoResetToChannel4   TEXT,"+ //66
	           "isAutoResetToChannel5   TEXT,"+ //67
	           "isAutoResetToChannel6   TEXT,"+ //68
	           "isAutoResetToChannel7   TEXT,"+ //69
	           "isAutoResetToChannel8   TEXT,"+ //70

				//2014/01/13 新增
			   "sensorInductionAngle    TEXT,"+ //71

				//2017/08/04
			   "detectionParams         TEXT" +
			   ");";

	// private String alterTableForVer1 = "Alter TABLE IF EXISTS MySetting";

	private String RemoteSettingTable = "CREATE TABLE IF NOT EXISTS "+RemoteSettingTableName+"("+
			"id       INTEGER PRIMARY KEY ASC AUTOINCREMENT,"+ //0
			"Kind                    TEXT,"+ //1
			"Name                    TEXT,"+ //2

			"ExpotentialModeCh1      TEXT,"+ //3
			"ExpotentialModeCh2      TEXT,"+ //4
			"ExpotentialModeCh3      TEXT,"+ //5
			"ExpotentialModeCh4      TEXT,"+ //6
			"ExpotentialModeCh5      TEXT,"+ //7
			"ExpotentialModeCh6      TEXT,"+ //8
			"ExpotentialModeCh7      TEXT,"+ //9
			"ExpotentialModeCh8      TEXT,"+ //10

			"MixingConfigCH1         TEXT,"+ //11
			"MixingConfigCH2         TEXT,"+ //12
			"MixingConfigCH3         TEXT,"+ //13
			"MixingConfigCH4         TEXT,"+ //14
			"MixingConfigCH5         TEXT,"+ //15
			"MixingConfigCH6         TEXT,"+ //16
			"MixingConfigCH7         TEXT,"+ //17
			"MixingConfigCH8         TEXT,"+ //18

			"ServoReverseCh1         TEXT,"+ //19
			"ServoReverseCh2         TEXT,"+ //20
			"ServoReverseCh3         TEXT,"+ //21
			"ServoReverseCh4         TEXT,"+ //22
			"ServoReverseCh5         TEXT,"+ //23
			"ServoReverseCh6         TEXT,"+ //24
			"ServoReverseCh7         TEXT,"+ //25
			"ServoReverseCh8         TEXT,"+ //26

			"ScreenControlMode       TEXT,"+ //27
			"MixingConfigReverse     TEXT,"+ //28

			"StrimConfigCh1Upper     TEXT,"+ //29
			"StrimConfigCh1Lower     TEXT,"+ //30
			"StrimConfigCh1Middle    TEXT,"+ //31
			"StrimConfigCh1FailSafe  TEXT,"+ //32

			"StrimConfigCh2Upper     TEXT,"+ //33
			"StrimConfigCh2Lower     TEXT,"+ //34
			"StrimConfigCh2Middle    TEXT,"+ //35
			"StrimConfigCh2FailSafe  TEXT,"+ //36

			"StrimConfigCh3Upper     TEXT,"+ //37
			"StrimConfigCh3Lower     TEXT,"+ //38
			"StrimConfigCh3Middle    TEXT,"+ //39
			"StrimConfigCh3FailSafe  TEXT,"+ //40

			"StrimConfigCh4Upper     TEXT,"+ //41
			"StrimConfigCh4Lower     TEXT,"+ //42
			"StrimConfigCh4Middle    TEXT,"+ //43
			"StrimConfigCh4FailSafe  TEXT,"+ //44

			"StrimConfigCh5Upper     TEXT,"+ //45
			"StrimConfigCh5Lower     TEXT,"+ //46
			"StrimConfigCh5Middle    TEXT,"+ //47
			"StrimConfigCh5FailSafe  TEXT,"+ //48

			"StrimConfigCh6Upper     TEXT,"+ //49
			"StrimConfigCh6Lower     TEXT,"+ //50
			"StrimConfigCh6Middle    TEXT,"+ //51
			"StrimConfigCh6FailSafe  TEXT,"+ //52

			"StrimConfigCh7Upper     TEXT,"+ //53
			"StrimConfigCh7Lower     TEXT,"+ //54
			"StrimConfigCh7Middle    TEXT,"+ //55
			"StrimConfigCh7FailSafe  TEXT,"+ //56

			"StrimConfigCh8Upper     TEXT,"+ //57
			"StrimConfigCh8Lower     TEXT,"+ //58
			"StrimConfigCh8Middle    TEXT,"+ //59
			"StrimConfigCh8FailSafe  TEXT,"+ //60

			"IfReverseCamera         TEXT,"+ //61
			"isAutoResetTHR          TEXT,"+ //62

			"isAutoResetToChannel1   TEXT,"+ //63
			"isAutoResetToChannel2   TEXT,"+ //64
			"isAutoResetToChannel3   TEXT,"+ //65
			"isAutoResetToChannel4   TEXT,"+ //66
			"isAutoResetToChannel5   TEXT,"+ //67
			"isAutoResetToChannel6   TEXT,"+ //68
			"isAutoResetToChannel7   TEXT,"+ //69
			"isAutoResetToChannel8   TEXT,"+ //70

			//2014/01/13 新增
			"sensorInductionAngle    TEXT"+ //71
			");";

	int thisVersion = 9;

	public void recreateTable(){
		String sql = "DROP TABLE IF EXISTS "+VIDEO_DETECT_VALUE_TABLE+";";

		db = MSP.getWritableDatabase();

		db.execSQL(sql);
		db.execSQL(DetectionValueTable);
	}

	public DataBase(Context context)
	{
		this.context = context;
	}
	
	public void Open()
	{
        if(MSP == null)
        {
        	MSP = new SQLiteOPenHelper(context, "MySQL.db", null, thisVersion);
        }

		db = MSP.getReadableDatabase();
    }
	
	public void Close()
	{
		if(MSP != null){
			MSP.close();
		}

		MSP = null;
	}

	public void editUpdateRemoteSettingTable(long id, String Field, String Value){
		EditUpdate(id, RemoteSettingTableName, Field, Value);
	}

	public void EditUpdate(long id, String tableName, String Field, String Value) // 更新資料庫中某個欄位的資料
	{
		db = MSP.getWritableDatabase();
		ContentValues EditU = new ContentValues();
		EditU.put(Field, Value);
		String where = "id =" + id;
		db.update(tableName, EditU, where, null);
		db.close();
	}

	public CustomerSetting getFirstCustomerSetting(String tableName, String name, String kind)
	{
		db = MSP.getReadableDatabase();

		String sql = "select * from "+tableName+" where Name = ? and Kind = ?;";
		Cursor result = db.rawQuery(sql, new String[]{name,kind});

		if(result != null && result.getCount() > 0)
		{
			CustomerSetting cms = null;

			if(result.moveToFirst())
			{
				cms = getRecord(result);
			}

			result.close();

			return cms;
		}

		return null;
	}

	public ArrayList<CustomerSetting> SelectSetting(String name, String kind)
	{
		ArrayList<CustomerSetting> CustomerSettingArray = new ArrayList<CustomerSetting>();

		db = MSP.getReadableDatabase();

		String sql = "select * from MySetting where Name = ? and Kind = ?;";
		Cursor result = db.rawQuery(sql, new String[]{name,kind});
		if(result != null)
		{
		    if(result.getCount() > 1)
		    {
			    if (result.moveToFirst())
	       	    {
	                do
	                {
						CustomerSetting cms = getRecord(result);
						CustomerSettingArray.add(cms);
	                }
	                while(result.moveToNext());
	       	    }
			    result.close();
		    }
		    else
		    {
		    	result.moveToFirst();
				CustomerSettingArray.add(getRecord(result));
		    }
		}
		return CustomerSettingArray;
	}
	
	public Cursor getAllCustmerSettingCursor(String strTableName)
	{      
	    db = MSP.getReadableDatabase();
	    Cursor result = db.query(strTableName, null, null, null, null, null, null);
	    return result;
	}

    public ArrayList<CustomerSetting> getAllSetting()
    {
        ArrayList<CustomerSetting> CustomerSettingArray = new ArrayList<CustomerSetting>();

        db = MSP.getReadableDatabase();

        Cursor result = db.query("MySetting", null, null, null, null, null, null);

        if(result != null && result.getCount() > 0)
        {
            result.moveToFirst();

            for(int i = 0 ; i < result.getCount() ; i++)
            {
                CustomerSetting cms = getRecord(result);
                CustomerSettingArray.add(cms);
                result.moveToNext();
            }
        }

        return CustomerSettingArray;
    }

	public CustomerSetting getRecord(Cursor c)
	{
		CustomerSetting result = new CustomerSetting();

		result.setId(c.getInt(c.getColumnIndex("id")));
		result.setKind(c.getString(c.getColumnIndex("Kind")));
		result.setName(c.getString(c.getColumnIndex("Name")));

		//3~10
		result.setExpotentialModeItem(new ExpotentialMode(mTotalChannel
				, c.getString(c.getColumnIndex("ExpotentialModeCh1"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh2"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh3"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh4"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh5"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh6"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh7"))
				, c.getString(c.getColumnIndex("ExpotentialModeCh8"))));


		//11~18
		result.setMaxingConfigItem(new MaxingConfig(mTotalChannel
				, c.getString(c.getColumnIndex("MixingConfigCH1"))
				, c.getString(c.getColumnIndex("MixingConfigCH2"))
				, c.getString(c.getColumnIndex("MixingConfigCH3"))
				, c.getString(c.getColumnIndex("MixingConfigCH4"))
				, c.getString(c.getColumnIndex("MixingConfigCH5"))
				, c.getString(c.getColumnIndex("MixingConfigCH6"))
				, c.getString(c.getColumnIndex("MixingConfigCH7"))
				, c.getString(c.getColumnIndex("MixingConfigCH8"))));

		//19~26
		result.setServoReverseItem(new ServoReverse(mTotalChannel
				, c.getString(c.getColumnIndex("ServoReverseCh1"))
				, c.getString(c.getColumnIndex("ServoReverseCh2"))
				, c.getString(c.getColumnIndex("ServoReverseCh3"))
				, c.getString(c.getColumnIndex("ServoReverseCh4"))
				, c.getString(c.getColumnIndex("ServoReverseCh5"))
				, c.getString(c.getColumnIndex("ServoReverseCh6"))
				, c.getString(c.getColumnIndex("ServoReverseCh7"))
				, c.getString(c.getColumnIndex("ServoReverseCh8"))));

		//27
		result.setScreeanControlMode(c.getString(c.getColumnIndex("ScreenControlMode")));

		//28
		result.setMixingConfigReverse(c.getString(c.getColumnIndex("MixingConfigReverse")));

		//29~32
		result.StrimConfigAdd(new StrimConfig("ch1"
				, c.getString(c.getColumnIndex("StrimConfigCh1Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh1Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh1Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh1FailSafe"))));

		//33~36
		result.StrimConfigAdd(new StrimConfig("ch2"
				, c.getString(c.getColumnIndex("StrimConfigCh2Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh2Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh2Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh2FailSafe"))));

		//37~40
		result.StrimConfigAdd(new StrimConfig("ch3"
				, c.getString(c.getColumnIndex("StrimConfigCh3Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh3Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh3Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh3FailSafe"))));

		//41~44
		result.StrimConfigAdd(new StrimConfig("ch4"
				, c.getString(c.getColumnIndex("StrimConfigCh4Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh4Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh4Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh4FailSafe"))));

		//45~48
		result.StrimConfigAdd(new StrimConfig("ch5"
				, c.getString(c.getColumnIndex("StrimConfigCh5Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh5Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh5Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh5FailSafe"))));

		//49~52
		result.StrimConfigAdd(new StrimConfig("ch6"
				, c.getString(c.getColumnIndex("StrimConfigCh6Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh6Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh6Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh6FailSafe"))));

		//53~56
		result.StrimConfigAdd(new StrimConfig("ch7"
				, c.getString(c.getColumnIndex("StrimConfigCh7Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh7Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh7Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh7FailSafe"))));

		//57~60
		result.StrimConfigAdd(new StrimConfig("ch8"
				, c.getString(c.getColumnIndex("StrimConfigCh8Upper"))
				, c.getString(c.getColumnIndex("StrimConfigCh8Lower"))
				, c.getString(c.getColumnIndex("StrimConfigCh8Middle"))
				, c.getString(c.getColumnIndex("StrimConfigCh8FailSafe"))));

		// 找出目前油門自動回復的狀態;


		String thisResetState = c.getString(c.getColumnIndex("isAutoResetTHR"));
		result.setAutoResetTHRState(thisResetState);

		result.setSensorInductionAngle(c.getString(c.getColumnIndex("sensorInductionAngle")));

        result.setIfReverseCamera(c.getString(c.getColumnIndex("IfReverseCamera"))); //61
        result.setIsAutoResetTHR(c.getString(c.getColumnIndex("isAutoResetTHR"))); //62

        String[] isAutoResetArray = new String[8];

        isAutoResetArray[0] = c.getString(c.getColumnIndex("isAutoResetToChannel1")); //63
        isAutoResetArray[1] = c.getString(c.getColumnIndex("isAutoResetToChannel2")); //64
        isAutoResetArray[2] = c.getString(c.getColumnIndex("isAutoResetToChannel3")); //65
        isAutoResetArray[3] = c.getString(c.getColumnIndex("isAutoResetToChannel4")); //66
        isAutoResetArray[4] = c.getString(c.getColumnIndex("isAutoResetToChannel5")); //67
        isAutoResetArray[5] = c.getString(c.getColumnIndex("isAutoResetToChannel6")); //68
        isAutoResetArray[6] = c.getString(c.getColumnIndex("isAutoResetToChannel7")); //69
        isAutoResetArray[7] = c.getString(c.getColumnIndex("isAutoResetToChannel8")); //70

        result.setIsAutoResetChannel(isAutoResetArray);

        int columnIndex = c.getColumnIndex("detectionParams");

		if(columnIndex > -1){
			String detectionParamsString = c.getString(columnIndex);

			if(detectionParamsString != null)
			{
				try {

					JSONObject jsonObject = new JSONObject(detectionParamsString);

					String GausianLastValue = jsonObject.optString(Constants.Gausian_Last_Value_Int, "5");
					String AutoCannyLastValue = jsonObject.optString(Constants.AutoCanny_Last_Value_Float, "0.33");
					String HoughLineRHOLastValue = jsonObject.optString(Constants.HoughLine_RHO_Last_Value_Double, "1.0");
					String HoughLineTHETALastValue = jsonObject.optString(Constants.HoughLine_THETA_Last_Value_Int, "1");
					String HoughLineThresholdLastValue = jsonObject.optString(Constants.HoughLine_Threshold_Last_Value_Int, "20");
					String HoughLineMinLineLengthLastValue = jsonObject.optString(Constants.HoughLine_MinLineLength_Last_Value_Int, "5");
					String HoughLineMaxLineGapLastValue = jsonObject.optString(Constants.HoughLine_MaxLineGap_Last_Value_Int, "5");
					String PosSlopesLastValue = jsonObject.optString(Constants.PosSlopes_Last_Value_Double, "-1.0");
					String NegSlopesLastValue = jsonObject.optString(Constants.NegSlopes_Last_Value_Double, "-1.0");
					String maxLeftRightPower = jsonObject.optString(Constants.MAX_LEFT_RIGHT_POWER, "0");
					String maxForwardPower = jsonObject.optString(Constants.MAX_FORWARD_POWER, "0");
					String isFilterColor = jsonObject.optString(Constants.IS_FILTER_COLOR, "false");
					String filterColorLower = jsonObject.optString(Constants.FILTER_COLOR_LOWER, "#FFFF64");
					String filterColorUpper = jsonObject.optString(Constants.FILTER_COLOR_UPPER, "#646450");

					result.setCheckPosSlopes(Double.valueOf(PosSlopesLastValue));
					result.setCheckNegSlopes(Double.valueOf(NegSlopesLastValue));
					result.setGausianKernelValue(Integer.valueOf(GausianLastValue));
					result.setAutoCannyValue(Double.valueOf(AutoCannyLastValue));
					result.setRhoValue(Double.valueOf(HoughLineRHOLastValue));
					result.setThetaValue(Integer.valueOf(HoughLineTHETALastValue));
					result.setTheresHoldValue(Integer.valueOf(HoughLineThresholdLastValue));
					result.setMinLineLen(Integer.valueOf(HoughLineMinLineLengthLastValue));
					result.setMaxLineGap(Integer.valueOf(HoughLineMaxLineGapLastValue));
					result.setMaxForwardPower(Integer.valueOf(maxForwardPower));
					result.setMaxLeftRightPower(Integer.valueOf(maxLeftRightPower));
					result.setFilterColor(Boolean.valueOf(isFilterColor));
					result.setFilterColorLower(filterColorLower);
					result.setFilterColorUpper(filterColorUpper);

					JSONObject innerJson = jsonObject.optJSONObject("roiArea");

					double roiLeftUpXPercent = Double.valueOf(innerJson.optString(Constants.Left_Top_X_Value, "0"));
					int roiLeftUpX = (int)(Constants.videoWidth * roiLeftUpXPercent);

					double roiLeftUpYPercent = Double.valueOf(innerJson.optString(Constants.Left_Top_Y_Value, "0"));
					int roiLeftUpY = (int)(Constants.videoHeight * roiLeftUpYPercent);

					double roiLeftDownXPercent = Double.valueOf(innerJson.optString(Constants.Left_Bottom_X_Value, "0"));
					int roiLeftDownX = (int)(Constants.videoWidth * roiLeftDownXPercent);

					double roiLeftDownYPercent = Double.valueOf(innerJson.optString(Constants.Left_Bottom_Y_Value, "0"));
					int roiLeftDownY = (int)(Constants.videoHeight * roiLeftDownYPercent);

					double roiRightUpXPercent = Double.valueOf(innerJson.optString(Constants.Right_Top_X_Value, "0"));
					int roiRightUpX = (int)(Constants.videoWidth * roiRightUpXPercent);

					double roiRightUpYPercent = Double.valueOf(innerJson.optString(Constants.Right_Top_Y_Value, "0"));
					int roiRightUpY = (int)(Constants.videoHeight * roiRightUpYPercent);

					double roiRightDownXPercent = Double.valueOf(innerJson.optString(Constants.Right_Bottom_X_Value, "0"));
					int roiRightDownX = (int)(Constants.videoWidth * roiRightDownXPercent);

					double roiRightDownYPercent = Double.valueOf(innerJson.optString(Constants.Right_Bottom_Y_Value, "0"));
					int roiRightDownY = (int)(Constants.videoHeight * roiRightDownYPercent);

					result.setROILeftUpX(roiLeftUpX);
					result.setROILeftUpY(roiLeftUpY);
					result.setROILeftDownX(roiLeftDownX);
					result.setROILeftDownY(roiLeftDownY);
					result.setROIRightUpX(roiRightUpX);
					result.setROIRightUpY(roiRightUpY);
					result.setROIRightDownX(roiRightDownX);
					result.setROIRightDownY(roiRightDownY);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}
	
//	public void EditUpdate(long id, String Field, String Value) // 更新資料庫中某個欄位的資料
//    {
//	    db = MSP.getWritableDatabase();
//	    ContentValues EditU = new ContentValues();
//		EditU.put(Field, Value);
//		String where = "id =" + id;
//        db.update("MySetting", EditU, where, null);
//        db.close();
//    }
	
	public void delete(long id)
	{
		db = MSP.getWritableDatabase();   
		String where = "id =" + id;
	    db.delete("MySetting", where , null) ;
	    db.close();
	}



	public ArrayList<SaveRecord> getSaveRecordsByFileName(String videoName){
		String strSql = "SELECT fileName, channel1Value, channel2Value, picture, saveTime, grayPixel FROM " + VIDEO_DETECT_VALUE_TABLE + " where fileName = '" + videoName + "';";

		Cursor cursor = db.rawQuery(strSql, null);

		if(cursor != null && cursor.moveToFirst()){
			ArrayList<SaveRecord> saveRecordArrayList = new ArrayList<SaveRecord>();

			for(int i = 0 ; i < cursor.getCount() ; i++){
				SaveRecord saveRecord = new SaveRecord();

				saveRecord.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
				saveRecord.channel1Value = cursor.getInt(cursor.getColumnIndex("channel1Value"));
				saveRecord.channel2Value = cursor.getInt(cursor.getColumnIndex("channel2Value"));
				saveRecord.picture = cursor.getBlob(cursor.getColumnIndex("picture"));
				saveRecord.saveTime = cursor.getLong(cursor.getColumnIndex("saveTime"));

				String stringPrayPixel = cursor.getString(cursor.getColumnIndex("grayPixel")).replace(" ", "");
				String toArrayValue = stringPrayPixel.substring(1, stringPrayPixel.length() - 1);
				String[] array = toArrayValue.split(",");

				int[] pixelArray = new int[array.length];

				for(int j = 0 ; j < array.length ; j++){
					String value = array[j];
					pixelArray[j] = Integer.valueOf(value);
				}

				saveRecord.grayPixels = pixelArray;

				saveRecordArrayList.add(saveRecord);
				cursor.moveToNext();
			}

			return saveRecordArrayList;
		}

		return null;
	}

	public String[] getTotalFileNameFromDataBase(){
		db = MSP.getReadableDatabase();

		String strSql = "SELECT DISTINCT fileName FROM "+VIDEO_DETECT_VALUE_TABLE+";";

		Cursor cursor = db.rawQuery(strSql, null);

		if(cursor != null && cursor.moveToFirst()){
			String[] fileNameList = new String[cursor.getCount()];

			for(int i = 0 ; i < cursor.getCount() ; i++){
				String fileName = cursor.getString(cursor.getColumnIndex("fileName"));
				fileNameList[i] = fileName;

				Log.e(TAG, "fileName: "+fileName);

				cursor.moveToNext();
			}

			return fileNameList;
		}

		return null;
	}

	public void insertDetectionData(SaveRecord[] recordList){

		if(MSP == null) {
			MSP = new SQLiteOPenHelper(context, "MySQL.db", null, thisVersion);
		}

		db = MSP.getWritableDatabase();

		String strSQL = "INSERT INTO " + VIDEO_DETECT_VALUE_TABLE + " (fileName, channel1Value, channel2Value, picture, saveTime, grayPixel) VALUES (?, ?, ?, ?, ?, ?)";

		db.beginTransaction();

		SQLiteStatement stmt = db.compileStatement(strSQL);

		for (int i = 0; i < recordList.length; i++) {
			SaveRecord saveRecord = recordList[i];

			stmt.bindString(1, saveRecord.fileName);
			stmt.bindLong(2, saveRecord.channel1Value);
			stmt.bindLong(3, saveRecord.channel2Value);
			stmt.bindBlob(4, saveRecord.picture);
			stmt.bindLong(5, saveRecord.saveTime);
			stmt.bindString(6, Arrays.toString(saveRecord.grayPixels));

			stmt.executeInsert();
			stmt.clearBindings();
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		stmt.close();
		stmt.releaseReference();
		stmt = null;
	}

	public void insertSetting(ContentValues contentValues)
	{
		SQLiteDatabase thisDb = MSP.getWritableDatabase();

		String strName = contentValues.getAsString("Name");

        String strKind = contentValues.getAsString("Kind");

        String checkSql2 = "select Name, Kind from MySetting where Name=? and Kind=?;";

        Cursor checkCursor = thisDb.rawQuery(checkSql2, new String[]{strName, strKind});

        if(checkCursor != null && checkCursor.getCount() > 0)
        {
            String  strNewName = strName + " " + (checkCursor.getCount() + 1);
            contentValues.put("Name", strNewName);
        }

		thisDb.insert("MySetting", null, contentValues);
	}

	public void insertRec(String tableName, String Kind, String Name){
		SQLiteDatabase thisDb = MSP.getWritableDatabase();
		insertRec(thisDb, tableName, Kind, Name);
	}

		
	public void insertRec(SQLiteDatabase thisDb, String tableName, String Kind, String Name)
	{
		ContentValues write = new ContentValues();
		write.put("Kind", Kind);
		write.put("Name", Name);
		
		write.put("ExpotentialModeCh1", "1");//塞入預設值
		write.put("ExpotentialModeCh2", "1");//塞入預設值
		write.put("ExpotentialModeCh3", "1");//塞入預設值
		write.put("ExpotentialModeCh4", "1");//塞入預設值
		write.put("ExpotentialModeCh5", "1");//塞入預設值
		write.put("ExpotentialModeCh6", "1");//塞入預設值
		write.put("ExpotentialModeCh7", "1");//塞入預設值
		write.put("ExpotentialModeCh8", "1");//塞入預設值
		
		write.put("MixingConfigCH1", "off");//塞入預設值
		write.put("MixingConfigCH2", "off");//塞入預設值
		write.put("MixingConfigCH3", "off");//塞入預設值
		write.put("MixingConfigCH4", "off");//塞入預設值
		write.put("MixingConfigCH5", "off");//塞入預設值
		write.put("MixingConfigCH6", "off");//塞入預設值
		write.put("MixingConfigCH7", "off");//塞入預設值
		write.put("MixingConfigCH8", "off");//塞入預設值
		
		write.put("ServoReverseCh1", "NO");//塞入預設值
		write.put("ServoReverseCh2", "NO");//塞入預設值
		write.put("ServoReverseCh3", "NO");//塞入預設值
		write.put("ServoReverseCh4", "NO");//塞入預設值
		write.put("ServoReverseCh5", "NO");//塞入預設值
		write.put("ServoReverseCh6", "NO");//塞入預設值
		write.put("ServoReverseCh7", "NO");//塞入預設值
		write.put("ServoReverseCh8", "NO");//塞入預設值
		
		if(Kind.contentEquals("car")) {
			
			write.put("ScreenControlMode", "302");//預設值
			
			
		} else {
		
			write.put("ScreenControlMode", "301");//預設值
			
		}
		
		
		write.put("MixingConfigReverse", "off");
		
		write.put("StrimConfigCh1Upper", "2000");//預設值
		write.put("StrimConfigCh1Lower", "1000");//預設值
		write.put("StrimConfigCh1Middle", "1500");//預設值
		write.put("StrimConfigCh1FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh2Upper", "2000");//預設值
		write.put("StrimConfigCh2Lower", "1000");//預設值
		write.put("StrimConfigCh2Middle", "1500");//預設值
		write.put("StrimConfigCh2FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh3Upper", "2000");//預設值
		write.put("StrimConfigCh3Lower", "1000");//預設值
		write.put("StrimConfigCh3Middle", "1500");//預設值
		write.put("StrimConfigCh3FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh4Upper", "2000");//預設值
		write.put("StrimConfigCh4Lower", "1000");//預設值
		write.put("StrimConfigCh4Middle", "1500");//預設值
		write.put("StrimConfigCh4FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh5Upper", "2000");//預設值
		write.put("StrimConfigCh5Lower", "1000");//預設值
		write.put("StrimConfigCh5Middle", "1500");//預設值
		write.put("StrimConfigCh5FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh6Upper", "2000");//預設值
		write.put("StrimConfigCh6Lower", "1000");//預設值
		write.put("StrimConfigCh6Middle", "1500");//預設值
		write.put("StrimConfigCh6FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh7Upper", "2000");//預設值
		write.put("StrimConfigCh7Lower", "1000");//預設值
		write.put("StrimConfigCh7Middle", "1500");//預設值
		write.put("StrimConfigCh7FailSafe", "1500");//預設值
		
		write.put("StrimConfigCh8Upper", "2000");//預設值
		write.put("StrimConfigCh8Lower", "1000");//預設值
		write.put("StrimConfigCh8Middle", "1500");//預設值
		write.put("StrimConfigCh8FailSafe", "1500");//預設值
		write.put("IfReverseCamera", "0");//預設值
		
		if(Kind.contentEquals("car")) {
			
			write.put("isAutoResetTHR", "1");//預設值
			write.put("isAutoResetToChannel1", "1");//預設值
			write.put("isAutoResetToChannel2", "1");//預設值
			write.put("isAutoResetToChannel3", "0");//預設值
			write.put("isAutoResetToChannel4", "0");//預設值
			write.put("isAutoResetToChannel5", "0");//預設值
			write.put("isAutoResetToChannel6", "0");//預設值
			write.put("isAutoResetToChannel7", "0");//預設值
			write.put("isAutoResetToChannel8", "0");//預設值
			
			//2014/01/13 新增
			write.put("sensorInductionAngle", "90");//預設值
			
		} else if(Kind.contentEquals("airplane")) {
			
			write.put("isAutoResetTHR", "0");//預設值
			write.put("isAutoResetToChannel1", "1");//預設值
			write.put("isAutoResetToChannel2", "1");//預設值
			write.put("isAutoResetToChannel3", "1");//預設值
			write.put("isAutoResetToChannel4", "1");//預設值
			write.put("isAutoResetToChannel5", "0");//預設值
			write.put("isAutoResetToChannel6", "0");//預設值
			write.put("isAutoResetToChannel7", "0");//預設值
			write.put("isAutoResetToChannel8", "0");//預設值
			
		}
		
		thisDb.insert(tableName, null, write);
		thisDb.close();
	}

	public void insertRecForEZSeries(String strTableName, String Kind, String Name){
		SQLiteDatabase thisDb = MSP.getWritableDatabase();
		insertRecForEZSeries(thisDb, strTableName, Kind, Name);
	}

	public void insertRecForEZSeries(SQLiteDatabase thisDb, String strTableName, String Kind, String Name)
	{
		ContentValues write = new ContentValues();
		write.put("Kind", Kind);
		write.put("Name", Name);

		write.put("ExpotentialModeCh1", "1");//塞入預設值
		write.put("ExpotentialModeCh2", "1");//塞入預設值
		write.put("ExpotentialModeCh3", "1");//塞入預設值
		write.put("ExpotentialModeCh4", "1");//塞入預設值
		write.put("ExpotentialModeCh5", "1");//塞入預設值
		write.put("ExpotentialModeCh6", "1");//塞入預設值
		write.put("ExpotentialModeCh7", "1");//塞入預設值
		write.put("ExpotentialModeCh8", "1");//塞入預設值

		write.put("MixingConfigCH1", "off");//塞入預設值
		write.put("MixingConfigCH2", "off");//塞入預設值
		write.put("MixingConfigCH3", "off");//塞入預設值
		write.put("MixingConfigCH4", "off");//塞入預設值
		write.put("MixingConfigCH5", "off");//塞入預設值
		write.put("MixingConfigCH6", "off");//塞入預設值
		write.put("MixingConfigCH7", "off");//塞入預設值
		write.put("MixingConfigCH8", "off");//塞入預設值

		write.put("ServoReverseCh1", "Yes");//塞入預設值
		write.put("ServoReverseCh2", "NO");//塞入預設值
		write.put("ServoReverseCh3", "NO");//塞入預設值
		write.put("ServoReverseCh4", "NO");//塞入預設值
		write.put("ServoReverseCh5", "NO");//塞入預設值
		write.put("ServoReverseCh6", "NO");//塞入預設值
		write.put("ServoReverseCh7", "NO");//塞入預設值
		write.put("ServoReverseCh8", "NO");//塞入預設值

		if(Kind.contentEquals("car")) {

			write.put("ScreenControlMode", "302");//預設值


		} else {

			write.put("ScreenControlMode", "301");//預設值

		}

		write.put("MixingConfigReverse", "off");

		write.put("StrimConfigCh1Upper", "1950");//預設值
		write.put("StrimConfigCh1Lower", "1050");//預設值
		write.put("StrimConfigCh1Middle", "1500");//預設值
		write.put("StrimConfigCh1FailSafe", "1500");//預設值

		write.put("StrimConfigCh2Upper", "2000");//預設值
		write.put("StrimConfigCh2Lower", "1000");//預設值
		write.put("StrimConfigCh2Middle", "1500");//預設值
		write.put("StrimConfigCh2FailSafe", "1500");//預設值

		write.put("StrimConfigCh3Upper", "2000");//預設值
		write.put("StrimConfigCh3Lower", "1000");//預設值
		write.put("StrimConfigCh3Middle", "1500");//預設值
		write.put("StrimConfigCh3FailSafe", "1500");//預設值

		write.put("StrimConfigCh4Upper", "2000");//預設值
		write.put("StrimConfigCh4Lower", "1000");//預設值
		write.put("StrimConfigCh4Middle", "1500");//預設值
		write.put("StrimConfigCh4FailSafe", "1500");//預設值

		write.put("StrimConfigCh5Upper", "2000");//預設值
		write.put("StrimConfigCh5Lower", "1000");//預設值
		write.put("StrimConfigCh5Middle", "1500");//預設值
		write.put("StrimConfigCh5FailSafe", "1500");//預設值

		write.put("StrimConfigCh6Upper", "2000");//預設值
		write.put("StrimConfigCh6Lower", "1000");//預設值
		write.put("StrimConfigCh6Middle", "1500");//預設值
		write.put("StrimConfigCh6FailSafe", "1500");//預設值

		write.put("StrimConfigCh7Upper", "2000");//預設值
		write.put("StrimConfigCh7Lower", "1000");//預設值
		write.put("StrimConfigCh7Middle", "1500");//預設值
		write.put("StrimConfigCh7FailSafe", "1500");//預設值

		write.put("StrimConfigCh8Upper", "2000");//預設值
		write.put("StrimConfigCh8Lower", "1000");//預設值
		write.put("StrimConfigCh8Middle", "1500");//預設值
		write.put("StrimConfigCh8FailSafe", "1500");//預設值
		write.put("IfReverseCamera", "0");//預設值

		if(Kind.contentEquals("car")) {

			write.put("isAutoResetTHR", "1");//預設值
			write.put("isAutoResetToChannel1", "1");//預設值
			write.put("isAutoResetToChannel2", "1");//預設值
			write.put("isAutoResetToChannel3", "0");//預設值
			write.put("isAutoResetToChannel4", "0");//預設值
			write.put("isAutoResetToChannel5", "0");//預設值
			write.put("isAutoResetToChannel6", "0");//預設值
			write.put("isAutoResetToChannel7", "0");//預設值
			write.put("isAutoResetToChannel8", "0");//預設值

		} else if(Kind.contentEquals("airplane")) {

			write.put("isAutoResetTHR", "0");//預設值
			write.put("isAutoResetToChannel1", "1");//預設值
			write.put("isAutoResetToChannel2", "1");//預設值
			write.put("isAutoResetToChannel3", "1");//預設值
			write.put("isAutoResetToChannel4", "1");//預設值
			write.put("isAutoResetToChannel5", "0");//預設值
			write.put("isAutoResetToChannel6", "0");//預設值
			write.put("isAutoResetToChannel7", "0");//預設值
			write.put("isAutoResetToChannel8", "0");//預設值

		}

		thisDb.insert(strTableName, null, write);
		thisDb.close();
	}

	private class SQLiteOPenHelper extends SQLiteOpenHelper
	{
		public SQLiteOPenHelper(Context context, String name,
                                CursorFactory factory, int version) {
			super(context, "MySQL.db", null, version);
			
		}

		@Override
		public void onCreate(SQLiteDatabase thisDb)
		{
			
			thisDb.execSQL(MySetting);
			thisDb.execSQL(RemoteSettingTable);
			thisDb.execSQL(DetectionValueTable);

//			if(db == null) {
//				
//				db = thisDb;
//				 
//			}
//			
//			insertRec("car", "default");
			
			// insertRec("car", "default");
			// insertRec("airplane", "default");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
//			versionUpdate();			// 更新資料庫;
			
			//2014/01/13 新增
			//updateToVersion3(oldVersion);

			String checkSql2 = "select * from MySetting;";

			Cursor checkColumnExist2 = db.rawQuery(checkSql2, null);

			int ifCustomDailyBreadStartDateExist = checkColumnExist2.getColumnIndex("detectionParams");

			if (ifCustomDailyBreadStartDateExist == -1) {
				String upgradeQuery1 = "ALTER TABLE MySetting ADD COLUMN detectionParams TEXT";
				db.execSQL(upgradeQuery1);
			}

			if(newVersion <= 7){
				db.execSQL(RemoteSettingTable);
			}

			if(newVersion <= 8){
				db.execSQL(DetectionValueTable);
			}

			if(newVersion <= 9){
				String checkColumn = "select * from " + VIDEO_DETECT_VALUE_TABLE;
				db.rawQuery(checkColumn, null);

				int ifColumnIsExist = checkColumnExist2.getColumnIndex("grayPixel");

				if(ifColumnIsExist == -1){
					String upgradeColumn = "ALTER TABLE " + VIDEO_DETECT_VALUE_TABLE + " ADD COLUMN grayPixel TEXT";
					db.execSQL(upgradeColumn);
				}
			}
		}
	}
	
	/***
	 * 更新這個DB的版本
	 */
	public void versionUpdate() {
		
		String Sqlcommand = "";
		
		// 增加一個新的欄位
		alterTable("IfReverseCamera", "TEXT");
		alterTable("isAutoResetToChannel1", "TEXT");
		alterTable("isAutoResetToChannel2", "TEXT");
		alterTable("isAutoResetToChannel3", "TEXT");
		alterTable("isAutoResetToChannel4", "TEXT");
		alterTable("isAutoResetToChannel5", "TEXT");
		alterTable("isAutoResetToChannel6", "TEXT");
		alterTable("isAutoResetToChannel7", "TEXT"); 
		alterTable("isAutoResetToChannel8", "TEXT");
		
	}
	
	//2014/01/13 新增
	public void updateToVersion3(int version)
	{
		switch(version)
		{
		case 1:
			
			versionUpdate();
			alterTable("sensorInductionAngle", "TEXT");
			version = 3;
			
			break;
			
		case 2:
			
			alterTable("sensorInductionAngle", "TEXT");
			version = 3;
			
			break;
		}
	}
	
	public boolean alterTable(String tableName, String tableType) {
		
		Log.d("Alter Table Sql command","Sql:" + tableName + ";type:" + tableType);
		
		boolean ifSuccess = true;
		
//		if(MSP != null) {
//			
//			if(db == null) {
//				
//				db = MSP.getWritableDatabase();
//							
//			}
//			
//		}
		
		if (db != null) {
			
			String sql =  "ALTER TABLE MySetting ADD " +  tableName + " " + tableType;
			Log.d("Alter Table Sql command","Sql:" + sql);
			
			db.execSQL(sql, null);
			
		} else {
			
			ifSuccess = false;
			
		}
		
		return ifSuccess;
		
	}
	
}
