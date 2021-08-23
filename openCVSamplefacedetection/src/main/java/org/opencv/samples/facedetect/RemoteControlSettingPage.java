package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.v7idea.template.View.Banner;
import com.v7idea.template.View.SpecialImageView;
import com.v7idea.template.View.V7TitleView;
import com.v7idea.tool.ButtonData;
import com.v7idea.tool.IButtonData;
import com.v7idea.tool.ViewScaling;

import java.util.ArrayList;

public class RemoteControlSettingPage extends Activity implements View.OnClickListener{

    public static final String TAG = RemoteControlSettingPage.class.getSimpleName();
    private MenuItemAdapter menuItemAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 鎖定螢幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_remote_control_setting_page);

        ViewScaling.setScaleValue(this);

        Banner Header = (Banner) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlSettingPage_Banner_Header);
        Header.initV7rcStyle();
        Header.addButton("", R.mipmap.button_menu_back, this);
//        Header.setTitleString(settingData.getName());

        menuItemAdapter = new MenuItemAdapter();

        GridView gridView = (GridView) ViewScaling.findViewByIdAndScale(this, R.id.RemoteControlSettingPage_GridView_MenuItem);
        gridView.setAdapter(menuItemAdapter);
        gridView.setOnItemClickListener(onItemClickListener);

        menuItemAdapter.addButton("SUB TRIM / END ADJ", R.drawable.menu_item_trim_adj_selector);
        menuItemAdapter.addButton("NET WORK", R.drawable.menu_item_net_work_selector);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(menuItemAdapter != null)
            {
                IButtonData iButtonData = menuItemAdapter.getItem(position);

                if(iButtonData != null)
                {
                    Intent intent = null;

                    switch (iButtonData.getButtonImageResourceID())
                    {
                        case R.drawable.menu_item_trim_adj_selector:
                            intent = new Intent(RemoteControlSettingPage.this, RemoteControlChannelSetting.class);
                            break;

                        case R.drawable.menu_item_net_work_selector:
                            intent = new Intent(RemoteControlSettingPage.this, ScanBluetoothDevicePage.class);
                            break;
                    }

                    startActivity(intent);
                    finish();
                }
            }
        }
    };

    private class MenuItemAdapter extends BaseAdapter
    {
        private ArrayList<IButtonData> buttonDataList = null;

        public MenuItemAdapter()
        {
            buttonDataList = new ArrayList<IButtonData>();
        }

        public void addButton(String strButtonText, int imageResourceID)
        {
            buttonDataList.add(new ButtonData(strButtonText, imageResourceID, null));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(buttonDataList != null)
            {
                return buttonDataList.size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public IButtonData getItem(int position)
        {
            if(buttonDataList != null && position > -1 && position < buttonDataList.size())
            {
                return buttonDataList.get(position);
            }
            else
            {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder = null;

            if(convertView == null)
            {
                viewHolder = new ViewHolder();

                convertView = getLayoutInflater().inflate(R.layout.menu_item_layout, null);

                viewHolder.Background = (SpecialImageView) ViewScaling.findViewByIdAndScale(convertView, R.id.MenuItem_SpecialImageView_Background);
                viewHolder.MenuName = (V7TitleView) ViewScaling.findViewByIdAndScale(convertView, R.id.MenuItem_V7TitleView_MenuName);
                viewHolder.MenuName.setTextColor(Color.YELLOW);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            IButtonData iButtonData = getItem(position);

            if(iButtonData != null)
            {
                viewHolder.Background.setImageResource(iButtonData.getButtonImageResourceID());
                viewHolder.MenuName.setText(iButtonData.getButtonText());
            }

            return convertView;
        }

        private class ViewHolder
        {
            public SpecialImageView Background = null;
            public V7TitleView MenuName = null;
        }
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RemoteControlPage.class);
        startActivity(intent);
        finish();
    }
}
