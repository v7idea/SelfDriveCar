package com.v7idea.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import org.opencv.samples.facedetect.Client;

/**
 * Created by mortal on 2017/10/18.
 */

public class ClientService extends Service
{
    private ClientBinder binder = new ClientBinder();

    private Client client = null;

    @Override
    public void onCreate() {
        super.onCreate();

        client = new Client();
    }

    public Client getClient(){
        return  client;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ClientBinder extends Binder {
        public ClientService getService(){
            return ClientService.this;
        }
    }
}
