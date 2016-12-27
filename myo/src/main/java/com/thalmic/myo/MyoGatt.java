package com.thalmic.myo;

import android.os.Build;

import com.thalmic.myo.internal.ble.BleManager;

import java.util.UUID;

class MyoGatt
{
    private Hub mHub;
    private BleManager mBleManager;

    public MyoGatt(Hub hub)
    {
        this.mHub = hub;
    }

    public void setBleManager(BleManager bleManager)
    {
        this.mBleManager = bleManager;
    }

    public boolean connect(String address)
    {
        return connect(address, false);
    }

    public boolean connect(String address, boolean autoConnect)
    {
        boolean connecting = this.mBleManager.connect(address, autoConnect);
        if (connecting)
        {
            Myo myo = this.mHub.getDevice(address);
            myo.setConnectionState(Myo.ConnectionState.CONNECTING);
        }
        return connecting;
    }

    public void disconnect(String address)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            configureDataAcquisition(address, ControlCommand.EmgMode.DISABLED, false, true);
        }
        this.mBleManager.disconnect(address);

        Myo myo = this.mHub.getDevice(address);
        if (myo.getConnectionState() == Myo.ConnectionState.CONNECTING)
        {
            myo.setConnectionState(Myo.ConnectionState.DISCONNECTED);

            this.mHub.getScanner().getScanListAdapter().notifyDeviceChanged();
        }
        myo.setAttached(false);
    }

    public void configureDataAcquisition(String address, ControlCommand.EmgMode streamEmg, boolean streamImu, boolean enableClassifier)
    {
        byte[] enableCommand = ControlCommand.createForSetMode(streamEmg, streamImu, enableClassifier);

        writeControlCommand(address, enableCommand);
    }

    public void requestRssi(String address)
    {
        this.mBleManager.getBleGatt().readRemoteRssi(address);
    }

    public void vibrate(String address, Myo.VibrationType vibrationType)
    {
        byte[] vibrateCommand = ControlCommand.createForVibrate(vibrationType);
        writeControlCommand(address, vibrateCommand);
    }

    public void unlock(String address, Myo.UnlockType unlockType)
    {
        byte[] unlockCommand = ControlCommand.createForUnlock(unlockType);
        writeControlCommand(address, unlockCommand);
    }

    public void notifyUserAction(String address)
    {
        byte[] command = ControlCommand.createForUserAction();
        writeControlCommand(address, command);
    }

    private void writeControlCommand(String address, byte[] controlCommand)
    {
        UUID serviceUuid = GattConstants.CONTROL_SERVICE_UUID;
        UUID charUuid = GattConstants.COMMAND_CHAR_UUID;
        this.mBleManager.getBleGatt().writeCharacteristic(address, serviceUuid, charUuid, controlCommand);
    }
}
