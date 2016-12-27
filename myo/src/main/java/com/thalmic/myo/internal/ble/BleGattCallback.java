package com.thalmic.myo.internal.ble;

import java.util.UUID;

public abstract class BleGattCallback
{
    public void onDeviceConnectionFailed(Address address) {}

    public void onDeviceConnected(Address address) {}

    public void onDeviceDisconnected(Address address) {}

    public void onServicesDiscovered(Address address, boolean success) {}

    public void onCharacteristicRead(Address address, UUID uuid, byte[] value, boolean success) {}

    public void onCharacteristicWrite(Address address, UUID uuid, boolean success) {}

    public void onCharacteristicChanged(Address address, UUID uuid, byte[] value) {}

    public void onReadRemoteRssi(Address address, int rssi, boolean success) {}
}
