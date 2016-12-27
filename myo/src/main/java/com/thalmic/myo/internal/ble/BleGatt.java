package com.thalmic.myo.internal.ble;

import java.util.UUID;

public abstract interface BleGatt
{
    public abstract void setBleGattCallback(BleGattCallback paramBleGattCallback);

    public abstract void discoverServices(String paramString);

    public abstract void readCharacteristic(String paramString, UUID paramUUID1, UUID paramUUID2);

    public abstract void writeCharacteristic(String paramString, UUID paramUUID1, UUID paramUUID2, byte[] paramArrayOfByte);

    public abstract void setCharacteristicNotification(String paramString, UUID paramUUID1, UUID paramUUID2, boolean paramBoolean1, boolean paramBoolean2);

    public abstract void readRemoteRssi(String paramString);
}
