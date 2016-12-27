package com.thalmic.myo.internal.ble;

import android.content.Context;
import android.util.Log;

public abstract class BleFactory
{
    private static final String TAG = "BleFactory";

    public static BleManager createBleManager(Context context)
    {
        if ("jb".equals("ss1")) {
            try
            {
                Class manager = Class.forName("com.thalmic.myo.internal.ble.SS1BleManager");
                return (BleManager)manager.newInstance();
            }
            catch (Exception e)
            {
                Log.e("BleFactory", "Failed creating SS1BleManager", e);
                return null;
            }
        }
        return new JBBleManager(context);
    }
}
