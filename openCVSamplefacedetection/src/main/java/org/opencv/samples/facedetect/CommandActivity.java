package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.v7idea.tool.Constants;
import com.v7idea.tool.CustomerSetting;
import com.v7idea.tool.ParamsFilter;
import com.v7idea.tool.ViewScaling;

public class CommandActivity extends Activity {

    private static final String TAG = "CommandActivity";

    private TextView connectedApp = null;
    private TextView connectedDevice = null;
    private TextView ShowCommandValue = null;

    private ImageView IsActiveCar = null;
    private ImageView showCarSendImage = null;

    private Air thisApp = null;

    private ParamsFilter paramsFilter = null;

    private Handler disconnectedAppHandler = new Handler();
    private boolean isRunning = false;
    private int disconnectedCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = this.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_command);

        ViewScaling.setScaleValue(this);
        thisApp = (Air)getApplication();

        showCarSendImage = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_ImageView_ShowCarSendImage);

        ImageView toChangeDetectionParams = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_ImageView_ToChangeDetectionParams);
        toChangeDetectionParams.setOnClickListener(onClickListener);

        LinearLayout statusGroup = (LinearLayout) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_LinearLayout_statusGroup);

        connectedApp = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_TextView_ConnectedApp);
        connectedDevice = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_TextView_ConnectedDevice);
        ShowCommandValue = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_TextView_ShowCommandValue);
        ShowCommandValue.setMovementMethod(new ScrollingMovementMethod());

        IsActiveCar = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_ImageView_IsActive);
        IsActiveCar.setOnClickListener(onClickListener);

        ImageView isRecordVideo = (ImageView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_ImageView_IsRecord);
        isRecordVideo.setOnClickListener(onClickListener);

        TextView LeftReferenceLine = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_TextView_isSetLeftReferenceLine);
        LeftReferenceLine.setOnClickListener(onClickListener);
        LeftReferenceLine.setVisibility(View.GONE);

        TextView RightReferenceLine = (TextView) ViewScaling.findViewByIdAndScale(this, R.id.CommandActivity_TextView_isSetRightReferenceLine);
        RightReferenceLine.setOnClickListener(onClickListener);
        RightReferenceLine.setVisibility(View.GONE);

        if(thisApp.client == null){
            thisApp.client = new Client();
            thisApp.client.setHandler(handler);
            thisApp.client.setIpAddress(thisApp.targetIP);
            thisApp.client.setPort(Constants.DEFAULT_PORT);
            thisApp.client.setCheckConnectedStatus(true);
            thisApp.client.setRequestParameter(true);
        }

        paramsFilter = new ParamsFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();

        thisApp.client.init();

        //準備接收CarApp來的Message Thread
        thisApp.client.startReceiveClientMessage();

        //準備發送指令的Thread
        thisApp.client.startSendMessage();

        if(ConnectedAppRunnable != null && isRunning == false)
        {
            disconnectedAppHandler.postDelayed(ConnectedAppRunnable, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        thisApp.client.stopReceiveClientMessage();

        //關閉發送指令的Thread
        thisApp.client.stopSendMessage();

        thisApp.client.release();

        if(ConnectedAppRunnable != null && isRunning)
        {
            disconnectedAppHandler.removeCallbacks(ConnectedAppRunnable);
            isRunning = false;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.CommandActivity_ImageView_ToChangeDetectionParams:{
                    Intent intent = new Intent(v.getContext(), ChangeDetectionParamsPage.class);
                    intent.putExtra(Constants.APP_MODE, Constants.COMMAND_MODE);
                    startActivity(intent);
                }
                    break;

                case R.id.CommandActivity_ImageView_IsActive:{
                    if(v.isSelected())
                    {
                        v.setSelected(false);
                        thisApp.client.setIsActive(false);
                    }
                    else
                    {
                        v.setSelected(true);
                        thisApp.client.setIsActive(true);
                    }
                }
                    break;

                case R.id.CommandActivity_ImageView_IsRecord:{
                    if(v.isSelected())
                    {
                        v.setSelected(false);
                        thisApp.client.setIsRecord(false);
                    }
                    else
                    {
                        v.setSelected(true);
                        thisApp.client.setIsRecord(true);
                    }
                }
                    break;

                case R.id.CommandActivity_TextView_isSetLeftReferenceLine:{
                    if(v.isSelected())
                    {
                        v.setSelected(false);
                        thisApp.client.setLeftReferenceLine(false);
                    }
                    else
                    {
                        v.setSelected(true);
                        thisApp.client.setLeftReferenceLine(true);
                    }
                }
                break;

                case R.id.CommandActivity_TextView_isSetRightReferenceLine:{
                    if(v.isSelected())
                    {
                        v.setSelected(false);
                        thisApp.client.setRightReferenceLine(false);
                    }
                    else
                    {
                        v.setSelected(true);
                        thisApp.client.setRightReferenceLine(true);
                    }
                }
                break;
            }
        }
    };

    private Runnable ConnectedAppRunnable = new Runnable()
    {

        @Override
        public void run() {
            isRunning = true;
            disconnectedCount++;

            if(disconnectedCount >= 2)
            {
                connectedApp.setSelected(false);
                connectedApp.setText(R.string.disconnected_app);
                disconnectedCount = 0;
            }

            disconnectedAppHandler.postDelayed(ConnectedAppRunnable, 1000);
        }
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case Client.GET_MESSAGE:{
                    String message = (String)msg.obj;

                    if(message != null && message.isEmpty() == false)
                    {
                        String[] commandArray = message.split(";");

                        if(commandArray != null && commandArray.length > 0)
                        {
                            for(int i = 0 ; i < commandArray.length ; i++)
                            {
                                String subMessage = commandArray[i];

                                int startedIndex = subMessage.indexOf(":");

                                if(startedIndex > -1)
                                {
                                    String strCommand = subMessage.substring(0, startedIndex);

                                    String strValue = subMessage.substring((startedIndex + 1), subMessage.length());

                                    if(strCommand.contentEquals("isConnectedDevice"))
                                    {
                                        if(strValue.contentEquals("true"))
                                        {
                                            connectedDevice.setSelected(true);
                                            connectedDevice.setText(R.string.connected_device);
                                        }
                                        else
                                        {
                                            connectedDevice.setSelected(false);
                                            connectedDevice.setText(R.string.disconnected_device);
                                        }
                                    }
                                    else if(strCommand.contentEquals("isConnectedApp"))
                                    {
                                        Log.e(TAG, "isConnectedApp strValue: "+strValue);

                                        if(strValue.contentEquals("true"))
                                        {
                                            disconnectedCount = 0;

                                            connectedApp.setSelected(true);
                                            connectedApp.setText(R.string.connected_app);
                                        }
                                    }
                                    else if(strCommand.contentEquals("responseDetectionParameter"))
                                    {
                                        CustomerSetting customerSetting = paramsFilter.parseJSONObjectToCustomerSetting(strValue);

                                        if(customerSetting != null)
                                        {
                                            ShowCommandValue.setText(customerSetting.showDetectionParameter());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }

            removeMessages(msg.what);
        }
    };
}
