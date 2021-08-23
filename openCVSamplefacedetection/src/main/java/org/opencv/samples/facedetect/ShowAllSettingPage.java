package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.v7idea.DataBase.DataBase;
import com.v7idea.DataBase.SimpleDatabase;
import com.v7idea.template.View.Banner;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.ParamsFilter;
import com.v7idea.tool.ViewScaling;

import java.util.ArrayList;

public class ShowAllSettingPage extends Activity implements View.OnClickListener{

    private ListView showSettingList = null;
    private Banner header = null;

    private DataBase dataBase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewScaling.setScaleValue(this);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 鎖定螢幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_show_all_setting_page);

        header = (Banner) ViewScaling.findViewByIdAndScale(this, R.id.ShowAllSetting_Banner_Header);
        header.initV7rcStyle();
        header.addButton("", R.mipmap.button_menu_back, this);

        dataBase = new DataBase(this);
        dataBase.Open();

        SettingAdapter settingAdapter = new SettingAdapter();

        showSettingList = (ListView) ViewScaling.findViewByIdAndScale(this, R.id.ScanBluetoothDevicePage_ListView_ShowDeviceList);
        showSettingList.setAdapter(settingAdapter);
        settingAdapter.setDataArray(dataBase.getAllSetting());
//        showSettingList.setOnItemClickListener(onItemClickListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

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
        finish();
    }

    private class SettingAdapter extends BaseAdapter
    {
        private ArrayList<CustomerSetting> settingArray = null;

        private SimpleDatabase simpleDataBase = null;
        private ParamsFilter paramsFilter = null;

        public SettingAdapter()
        {
            simpleDataBase = new SimpleDatabase();
            paramsFilter = new ParamsFilter();
        }

//        SimpleDatabase mSimpleDatabase;

        public void setDataArray(ArrayList<CustomerSetting> settingArray)
        {
            if(this.settingArray != null)
            {
                this.settingArray.clear();
                this.settingArray = null;
            }

            this.settingArray = settingArray;

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {

            if( this.settingArray != null)
            {
                return  this.settingArray.size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public CustomerSetting getItem(int position) {

            if( this.settingArray != null && position >= 0 && position < this.settingArray.size())
            {
                return this.settingArray.get(position);
            }
            else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        private class ViewHolder {
            public TextView Name;
            public Button Data;
            public Button Setting;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.custom_setting_item_layout, null);

                holder.Name = (TextView) convertView.findViewById(R.id.TextView_Name);
                holder.Data = (Button) convertView.findViewById(R.id.Button_Data);
                holder.Setting = (Button) convertView.findViewById(R.id.Button_Setting);

                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            final CustomerSetting customerSetting = getItem(position);

            if(customerSetting != null)
            {
                String strName = customerSetting.getName();

                holder.Name.setText(strName);
                holder.Data.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                        alert.setCancelable(false);

                        alert.setMessage(customerSetting.toString());

                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });

                        alert.show();
                    }
                });

                holder.Setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paramsFilter.changeSettingToContentValues(simpleDataBase, customerSetting);
                    }
                });
            }

            return convertView;
        }
    }
}
