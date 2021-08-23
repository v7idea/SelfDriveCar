package com.v7idea.tool;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by mortal on 2017/3/13.
 */

public interface IBleService
{
    public UUID getDeviceUUID();
    public UUID getServiceUUID();
    public UUID getWriteUUID();
    public UUID getNotifyUUID();

    public void setDeviceUUID();
    public void setServiceUUID();
    public void setWriteUUID();
    public void setNotifyUUID();

    public BluetoothGattCharacteristic getWriteBluetoothGattCharacteristic();
    public BluetoothGattCharacteristic getNotifyUUIDBluetoothGattCharacteristic();
}
