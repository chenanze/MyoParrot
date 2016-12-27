package com.thalmic.myo;

import com.thalmic.myo.internal.ble.Address;

public class Myo
{
    private final MyoGatt mMyoGatt;
    private String mName;
    private final String mAddress;
    private boolean mAttached;

    public static enum VibrationType
    {
        SHORT,  MEDIUM,  LONG;

        private VibrationType() {}
    }

    public static enum UnlockType
    {
        TIMED,  HOLD;

        private UnlockType() {}
    }

    private ConnectionState mConnState = ConnectionState.DISCONNECTED;
    private FirmwareVersion mFirmwareVersion;
    private Pose mCurrentPose = Pose.UNKNOWN;
    private Arm mCurrentArm = Arm.UNKNOWN;
    private XDirection mCurrentXDirection = XDirection.UNKNOWN;
    private boolean mUnlocked;
    private Pose mUnlockPose = Pose.UNKNOWN;

    Myo(Hub hub, Address address)
    {
        this.mMyoGatt = hub.getMyoGatt();
        this.mName = "";
        this.mAddress = address.toString();
    }

    public String getName()
    {
        return this.mName;
    }

    public String getMacAddress()
    {
        return this.mAddress;
    }

    public FirmwareVersion getFirmwareVersion()
    {
        return this.mFirmwareVersion;
    }

    public boolean isUnlocked()
    {
        return this.mUnlocked;
    }

    public Arm getArm()
    {
        return this.mCurrentArm;
    }

    public XDirection getXDirection()
    {
        return this.mCurrentXDirection;
    }

    public Pose getPose()
    {
        return this.mCurrentPose;
    }

    public void requestRssi()
    {
        this.mMyoGatt.requestRssi(this.mAddress);
    }

    public void vibrate(VibrationType vibrationType)
    {
        this.mMyoGatt.vibrate(this.mAddress, vibrationType);
    }

    public void unlock(UnlockType unlockType)
    {
        this.mMyoGatt.unlock(this.mAddress, unlockType);
    }

    public void lock()
    {
        this.mMyoGatt.unlock(this.mAddress, null);
    }

    public void notifyUserAction()
    {
        this.mMyoGatt.notifyUserAction(this.mAddress);
    }

    public boolean isFirmwareVersionSupported()
    {
        if (this.mFirmwareVersion == null) {
            return true;
        }
        if (this.mFirmwareVersion.isNotSet()) {
            return false;
        }
        if ((this.mFirmwareVersion.major == 1) && (this.mFirmwareVersion.minor >= 1)) {
            return true;
        }
        return false;
    }

    public static enum ConnectionState
    {
        CONNECTED,  CONNECTING,  DISCONNECTED;

        private ConnectionState() {}
    }

    public boolean isConnected()
    {
        return getConnectionState() == ConnectionState.CONNECTED;
    }

    public ConnectionState getConnectionState()
    {
        return this.mConnState;
    }

    void setAttached(boolean attached)
    {
        this.mAttached = attached;
    }

    boolean isAttached()
    {
        return this.mAttached;
    }

    void setName(String name)
    {
        this.mName = name;
    }

    void setConnectionState(ConnectionState state)
    {
        this.mConnState = state;
    }

    void setCurrentPose(Pose pose)
    {
        this.mCurrentPose = pose;
    }

    void setCurrentArm(Arm arm)
    {
        this.mCurrentArm = arm;
    }

    void setCurrentXDirection(XDirection xDirection)
    {
        this.mCurrentXDirection = xDirection;
    }

    void setUnlocked(boolean unlocked)
    {
        this.mUnlocked = unlocked;
    }

    Pose getUnlockPose()
    {
        return this.mUnlockPose;
    }

    void setUnlockPose(Pose unlockPose)
    {
        this.mUnlockPose = unlockPose;
    }

    void setFirmwareVersion(FirmwareVersion firmwareVersion)
    {
        this.mFirmwareVersion = firmwareVersion;
    }
}
