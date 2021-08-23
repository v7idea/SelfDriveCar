package org.opencv.samples.facedetect;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.v7idea.DataBase.DataBase;
import com.v7idea.tool.ViewScaling;

import java.util.ArrayList;

public class ChooseModeActivity extends Activity {

    private static final int REQUEST_CODE = 9998;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewScaling.setScaleValue(this);

        setContentView(R.layout.activity_choose_mode);

        requestPermission();

    }

    private void requestPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int permissionCAMERA = checkSelfPermission(android.Manifest.permission.CAMERA);
            int permissionWRITESTORAGE = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionACCESSFINELOCATION = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);


            ArrayList<String> permissionList = new ArrayList<String>();

            if(permissionCAMERA != PackageManager.PERMISSION_GRANTED)
            {
                permissionList.add(android.Manifest.permission.CAMERA);
            }

            if(permissionWRITESTORAGE != PackageManager.PERMISSION_GRANTED)
            {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if(permissionACCESSFINELOCATION != PackageManager.PERMISSION_GRANTED)
            {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }


            if(permissionList.size() > 0)
            {
                String[] permissionArray = new String[permissionList.size()];

                for(int i = 0 ; i < permissionList.size() ; i++)
                {
                    permissionArray[i] = permissionList.get(i);
                }

                requestPermissions(permissionArray, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                int permissionCAMERA = checkSelfPermission(android.Manifest.permission.CAMERA);
                int permissionWRITESTORAGE = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int permissionACCESSFINELOCATION = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

                if(permissionCAMERA == PackageManager.PERMISSION_GRANTED
                        && permissionWRITESTORAGE == PackageManager.PERMISSION_GRANTED
                        && permissionACCESSFINELOCATION == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {
                    String strAlertString = "";

                    if(permissionCAMERA != PackageManager.PERMISSION_GRANTED)
                    {
//                        Toast.makeText(this, getResources().getString(R.string.cancel_CAMERA_permission), Toast.LENGTH_SHORT).show();
                        strAlertString += (getResources().getString(R.string.cancel_CAMERA_permission) + "\n");
                    }

                    if(permissionWRITESTORAGE != PackageManager.PERMISSION_GRANTED)
                    {
//                        Toast.makeText(this, getResources().getString(R.string.cancel_WRITE_EXTERNAL_STORAGE_permission), Toast.LENGTH_SHORT).show();
                        strAlertString += (getResources().getString(R.string.cancel_WRITE_EXTERNAL_STORAGE_permission) + "\n");
                    }

                    if(permissionACCESSFINELOCATION != PackageManager.PERMISSION_GRANTED)
                    {
//                        Toast.makeText(this, getResources().getString(R.string.cancel_ACCESS_FINE_LOCATION_permission), Toast.LENGTH_SHORT).show();
                        strAlertString += (getResources().getString(R.string.cancel_ACCESS_FINE_LOCATION_permission) + "\n");
                    }

                    if(strAlertString.isEmpty() == false){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChooseModeActivity.this);
                        builder.setMessage(strAlertString);
                        builder.setPositiveButton("確定", null);
                        final AlertDialog thisDialog = builder.create();
                        thisDialog.setCancelable(false);
                        thisDialog.show();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onPressButton(final View view)
    {
        switch (view.getId())
        {
            case R.id.ChooseMode_Button_RecordFrame:{
                Intent intent = new Intent(view.getContext(), ReviewRecordDataActivity.class);
                startActivity(intent);
                finish();
            }
                break;

            case R.id.ChooseMode_Button_Car:
                {
                    Intent intent = new Intent(view.getContext(), FdActivity.class);
                    startActivity(intent);
                    finish();
                }

                break;

            case R.id.ChooseMode_Button_WebCam:{
                    Intent intent = new Intent(view.getContext(), UsbWebCamActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;

            case R.id.ChooseMode_Button_Command:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage(R.string.請輸入要連線的手機IP);

                    int paddingValue = (int)(10 * ViewScaling.getScaleValue());

                    final EditText editText = new EditText(view.getContext());
                    editText.setText(Client.DEFAULT_IP);
                    editText.setPadding(paddingValue, paddingValue, paddingValue, paddingValue);

                    builder.setView(editText);

                    builder.setPositiveButton(R.string.determine, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String targetIP = editText.getText().toString();

                            if(targetIP.isEmpty() == false)
                            {
                                Air thisApp = (Air)view.getContext().getApplicationContext();
                                thisApp.targetIP = targetIP;

                                Intent intent = new Intent(view.getContext(), CommandActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(view.getContext(), R.string.target_ip_is_empty, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    final AlertDialog thisDialog = builder.create();
                    thisDialog.setCancelable(false);
                    thisDialog.show();
                }

                break;

            case R.id.ChooseMode_Button_Remote:
                {
                    Intent intent = new Intent(view.getContext(), RemoteControlPage.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }
}
