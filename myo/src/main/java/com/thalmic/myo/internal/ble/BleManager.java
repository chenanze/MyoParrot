package com.thalmic.myo.internal.ble;

import java.util.UUID;

public abstract interface BleManager
{
    public abstract boolean isBluetoothSupported();

    public abstract BleGatt getBleGatt();

    public abstract boolean startBleScan(BleScanCallback paramBleScanCallback);

    public abstract void stopBleScan(BleScanCallback paramBleScanCallback);

    public abstract boolean connect(String paramString, boolean paramBoolean);

    public abstract void disconnect(String paramString);

    public abstract void dispose();

    public static abstract interface BleScanCallback
    {
        public abstract void onBleScan(Address paramAddress, String paramString, int paramInt, UUID paramUUID);
    }
}
