package com.v7idea.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;

/**
 * Created by mortal on 2017/9/25.
 */

public class BaseActivity extends FragmentActivity
{
    private boolean isHideStatusBar = false;
    protected Activity currentActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentActivity = this;

        ViewScaling.setScaleValue(currentActivity);

        if(isHideStatusBar){
            //取消螢幕上的那一槓
            final Window win = this.getWindow();
            win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    public static void setTurnInNextPageAnimation(Activity FinishActivity) {
        if (FinishActivity != null) {
            FinishActivity.overridePendingTransition(R.anim.activity_in_from_right, R.anim.activity_out_to_left);
        }
    }

    public static void setBackInPrePageAnimation(Activity FinishActivity) {
        if (FinishActivity != null) {
            FinishActivity.overridePendingTransition(R.anim.activity_in_from_left, R.anim.activity_out_to_right);
        }
    }

    public void showErrorAlert(String strMessage){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(currentActivity);
        alertBuilder.setMessage(strMessage);
        alertBuilder.setPositiveButton(R.string.determine, null);
        alertBuilder.setCancelable(false);
        alertBuilder.show();
    }

    public void showSoftKeyBoardWhenTouchEditText(EditText whichEditText){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(whichEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 在 super.onCreate(savedInstanceState) 之前執行
     * @param isHideStatusBar
     */
    public void isHideStatusBar(boolean isHideStatusBar){
        this.isHideStatusBar = isHideStatusBar;
    }

    public void keepScreenOn(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

//    public boolean isConnectedNetWork(){
//        boolean isConnected = DownLoad.isConnectInternet();
//
//        if(isConnected == false){
//            showErrorAlert(getResources().getString(R.string.no_internet_can_not_login));
//        }
//
//        return isConnected;
//    }
}
