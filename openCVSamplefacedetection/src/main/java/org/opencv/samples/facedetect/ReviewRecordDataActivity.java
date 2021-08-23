package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.v7idea.Data.SaveRecord;
import com.v7idea.DataBase.DataBase;
import com.v7idea.tool.ViewScaling;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class ReviewRecordDataActivity extends Activity {

    private ListView ShowRecordFileName = null;
    private RelativeLayout VideoFrameContainer = null;

    private ImageView ShowFrame = null;
    private LinearLayout ShowFrameInfo = null;

    private TextView ShowFileName = null;
    private TextView ShowChannel1Value = null;
    private TextView ShowChannel2Value = null;
    private TextView ShowSaveTime = null;

    private LinearLayout ButtonContainer = null;
    private Button PreFrame = null;
    private Button NextFrame = null;

    private Air thisApp = null;

    private RecordFileAdapter arrayAdapter = null;

    private DataBase appDB = null;

    private RelativeLayout listContainer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_record_data);

        ViewScaling.setScaleValue(this);

        thisApp = (Air) getApplication();

        listContainer = (RelativeLayout) ViewScaling.findViewByIdAndScale(this,R.id.ReviewRecordDataActivity_RelativeLayout_ListContainer);

        ShowRecordFileName = (ListView) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_ListView_ShowRecordFileName);

        VideoFrameContainer = (RelativeLayout) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_RelativeLayout_VideoFrameContainer);

        ShowFrame = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_ImageView_ShowFrame);

        ShowFrameInfo = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_LinearLayout_ShowFrameInfo);

        ShowFileName = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_TextView_ShowFileName);

        ShowChannel1Value = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_TextView_ShowChannel1Value);

        ShowChannel2Value = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_TextView_ShowChannel2Value);

        ShowSaveTime = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_TextView_ShowSaveTime);

        ButtonContainer = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_LinearLayout_ButtonContainer);

        PreFrame = (Button) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_Button_PreFrame);

        NextFrame = (Button) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_Button_NextFrame);

        Button importDBButton = (Button) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_Button_importDataBase);
        Button exportDBButton = (Button) ViewScaling.findViewByIdAndScale(this, R.id.ReviewRecordDataActivity_Button_exportDataBase);

        importDBButton.setOnClickListener(onPressButton);
        exportDBButton.setOnClickListener(onPressButton);

        appDB = new DataBase(this);
        appDB.Open();

        String[] fileNames = appDB.getTotalFileNameFromDataBase();

        arrayAdapter = new RecordFileAdapter(fileNames);
        ShowRecordFileName.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        ShowRecordFileName.setOnItemClickListener(onItemClickListener);

        PreFrame.setOnClickListener(onClickListener);
        NextFrame.setOnClickListener(onClickListener);

//        appDB.recreateTable();
    }

    private ArrayList<SaveRecord> saveRecordArrayList = null;

    private int currentIndex = 0;

    private View.OnClickListener onPressButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ReviewRecordDataActivity_Button_importDataBase:

                    appDB.Close();
                    importDB();

                    appDB.Open();


                    String[] fileNames = appDB.getTotalFileNameFromDataBase();
                    arrayAdapter.setData(fileNames);

                    break;

                case R.id.ReviewRecordDataActivity_Button_exportDataBase:
                    exportDB();
                    break;
            }
        }
    };

    private void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/" + getPackageName()
                        + "/databases/" + "MySQL.db";
                String backupDBPath = "/BackupFolder/MySQL.db";
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString(),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/" + getPackageName()
                        + "/databases/" + "MySQL.db";
                String backupDBPath = "/BackupFolder/MySQL.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                File pathFile = new File(sd + "/BackupFolder");
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }

                if (!backupDB.exists()) {
                    backupDB.createNewFile();
                }

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder isExportFile = new AlertDialog.Builder(view.getContext());
            isExportFile.setMessage("請問是否要滙出成.txt檔案？");
            isExportFile.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String fileName = arrayAdapter.getItem(position);

                    ExportFileToTxt exportFileToTxt = new ExportFileToTxt();
                    exportFileToTxt.execute(fileName);
                }
            });

            isExportFile.setNegativeButton("取消", null);
            isExportFile.setCancelable(false);
            isExportFile.show();
        }
    };

    private class ExportFileToTxt extends AsyncTask<String, String, String>{

        private ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(ReviewRecordDataActivity.this, "", "資料讀取中", true);
        }

        public void appendLog(String fileName, String text) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();

            File logFile = new File(filePath + "/BackupFolder/" + fileName + ".txt");
            if (logFile.exists()) {
                logFile.delete();
            }

            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.flush();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            ArrayList<SaveRecord> recordList = appDB.getSaveRecordsByFileName(strings[0]);

            if(recordList != null && recordList.size() > 0){

                Log.e("ExportFileToTxt","recordList size : "+recordList.size());

                StringBuilder stringBuilder = new StringBuilder();

                String strFileName = "";

                for(int i = 0 ; i < recordList.size() ; i++){
                    SaveRecord saveRecord = recordList.get(i);

                    strFileName = saveRecord.fileName;
                    String strSaveTime = "" + saveRecord.saveTime;
                    String strChannel1Value = "" + saveRecord.channel1Value;
                    String strChannel2Value = "" + saveRecord.channel2Value;

                    DateTime dateTime = new DateTime(Long.valueOf(strSaveTime));
                    String logFileName = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS");

                    Log.e("ExportFileToTxt", "logFileName: "+logFileName);

//                    Log.e("ExportFileToTxt","strFileName : "+strFileName);
//                    Log.e("ExportFileToTxt","strSaveTime : "+strSaveTime);
//                    Log.e("ExportFileToTxt","strChannel1Value : "+strChannel1Value);
//                    Log.e("ExportFileToTxt","strChannel2Value : "+strChannel2Value);

                    stringBuilder.append(logFileName + " ");
                    stringBuilder.append("1:" + strChannel1Value + " ");
                    stringBuilder.append("2:" + strChannel2Value + " ");


//                    Log.e("ExportFileToTxt","stringBuilder : "+stringBuilder.toString());

//                    stringBuilder.append("3:" + parseByteArrayToInArrayString(saveRecord.picture) + "\n");

                    if(saveRecord.grayPixels != null && saveRecord.grayPixels.length > 0){
                        for (int j = 0 ; j < saveRecord.grayPixels.length ; j++){
                            stringBuilder.append((j + 3) + ":" + saveRecord.grayPixels[j] + " ");
                        }
                    }

                    stringBuilder.append("\n");
                }

                appendLog(strFileName, stringBuilder.toString());

                recordList.clear();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(dialog != null){
                dialog.dismiss();
                dialog = null;
            }

            Toast.makeText(ReviewRecordDataActivity.this, "轉換完畢", Toast.LENGTH_SHORT).show();
        }

        private String parseByteArrayToInArrayString(byte[] bytes){
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < bytes.length; i++){
                int value = bytes[i++] & 0xff;
                stringBuilder.append(value);
            }

            return stringBuilder.toString();
        }
    }

    private class LoadSaveRecordData extends AsyncTask<String, String, ArrayList<SaveRecord>>{

        private ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(ReviewRecordDataActivity.this, "", "資料讀取中", true);
        }

        @Override
        protected ArrayList<SaveRecord> doInBackground(String... strings) {
            return appDB.getSaveRecordsByFileName(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<SaveRecord> s) {
            super.onPostExecute(s);

            if(dialog != null){
                dialog.dismiss();
                dialog = null;
            }

            saveRecordArrayList = s;

            currentIndex = 0;

            if(saveRecordArrayList != null){
                showData(saveRecordArrayList);
            }
        }
    }

    private void showData(ArrayList<SaveRecord> saveRecordArrayList){
        VideoFrameContainer.setVisibility(View.VISIBLE);

        SaveRecord saveRecord = saveRecordArrayList.get(currentIndex);

        Bitmap bitmap = BitmapFactory.decodeByteArray(saveRecord.picture, 0 , saveRecord.picture.length);

        if(bitmap == null){
            bitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(saveRecord.picture));
        }


        ShowFrame.setImageBitmap(bitmap);

        ShowFileName.setText(saveRecord.fileName);

        ShowChannel1Value.setText("" + saveRecord.channel1Value);

        ShowChannel2Value.setText("" + saveRecord.channel2Value);

        Log.e("ReviewRecordDataActivity", "saveRecord.saveTime: "+saveRecord.saveTime);

        DateTime dateTime = new DateTime(saveRecord.saveTime);

        ShowSaveTime.setText(dateTime.toString("yyyy/MM/dd HH:mm:ss"));
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ReviewRecordDataActivity_Button_PreFrame:{
                    currentIndex--;

                    if(currentIndex <= 0){
                        currentIndex = 0;
                    }
                }
                break;

                case R.id.ReviewRecordDataActivity_Button_NextFrame:{
                    currentIndex++;

                    if(currentIndex >= (saveRecordArrayList.size() - 1)){
                        currentIndex = saveRecordArrayList.size() - 1;

                        if(currentIndex <= 0){
                            currentIndex = 0;
                        }
                    }
                }
                break;
            }

            showData(saveRecordArrayList);
        }
    };

    @Override
    public void onBackPressed() {
        if(VideoFrameContainer.getVisibility() == View.VISIBLE){
            VideoFrameContainer.setVisibility(View.GONE);
        }
        else{
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isFinishing()){

            if(saveRecordArrayList != null){
                saveRecordArrayList.clear();
                saveRecordArrayList = null;
            }

            if(appDB != null){
                appDB.Close();
            }

            System.gc();
        }
    }

    private class RecordFileAdapter extends BaseAdapter{

        private String[] fileNames = null;

        public RecordFileAdapter(String[] fileNames){
            this.fileNames = fileNames;
        }

        public void setData(String[] fileNames){
            this.fileNames = fileNames;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(fileNames != null){

                Log.e("RecordFileAdapter", "fileNames.length: "+fileNames.length);
                return fileNames.length;
            }

            return 0;
        }

        @Override
        public String getItem(int position) {

            if(fileNames != null && position > -1 && position < fileNames.length){
                return fileNames[position];
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            if(convertView == null){
                viewHolder = new ViewHolder();

                convertView = getLayoutInflater().inflate(R.layout.file_name_item_layout, null);
                ViewScaling.setPadding(convertView);

                viewHolder.showFileName = (TextView) ViewScaling.findViewByIdAndScale(convertView, R.id.FileNameItemLayout_TextView_ShowFileName);
                viewHolder.showRecordDate = (TextView) ViewScaling.findViewByIdAndScale(convertView, R.id.FileNameItemLayout_TextView_RecordDateTime);

                viewHolder.deleteFileButton = (Button) ViewScaling.findViewByIdAndScale(convertView, R.id.ReviewRecordDataActivity_Button_deleteFile);
                viewHolder.viewFileButton = (Button) ViewScaling.findViewByIdAndScale(convertView, R.id.ReviewRecordDataActivity_Button_viewFile);

                convertView.setTag(viewHolder);
            }
            else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final String fileName = getItem(position);

            if(fileName != null && fileName.isEmpty() == false){

                long recordTime = Long.valueOf(fileName);
                DateTime dateTime = new DateTime(recordTime);

                String recordDateTime = dateTime.toString("yyyy/MM/dd HH:mm:ss");

                viewHolder.showFileName.setText("檔案名稱： " + fileName + ".mp4");
                viewHolder.showRecordDate.setText("記錄時間： " + recordDateTime);

                viewHolder.deleteFileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                    }
                });

                viewHolder.viewFileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoadSaveRecordData loadSaveRecordData = new LoadSaveRecordData();
                        loadSaveRecordData.execute(fileName);
                    }
                });
            }
            else{
                viewHolder.deleteFileButton.setOnClickListener(null);
                viewHolder.viewFileButton.setOnClickListener(null);
            }

            return convertView;
        }

        public class ViewHolder{
            public TextView showFileName = null;
            public TextView showRecordDate = null;

            public Button deleteFileButton = null;
            public Button viewFileButton = null;
        }
    }
}
