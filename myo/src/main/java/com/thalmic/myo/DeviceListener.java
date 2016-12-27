package com.thalmic.myo;

public abstract interface DeviceListener
{
    public abstract void onAttach(Myo paramMyo, long paramLong);

    public abstract void onDetach(Myo paramMyo, long paramLong);

    public abstract void onConnect(Myo paramMyo, long paramLong);

    public abstract void onDisconnect(Myo paramMyo, long paramLong);

    public abstract void onArmSync(Myo paramMyo, long paramLong, Arm paramArm, XDirection paramXDirection);

    public abstract void onArmUnsync(Myo paramMyo, long paramLong);

    public abstract void onUnlock(Myo paramMyo, long paramLong);

    public abstract void onLock(Myo paramMyo, long paramLong);

    public abstract void onPose(Myo paramMyo, long paramLong, Pose paramPose);

    public abstract void onOrientationData(Myo paramMyo, long paramLong, Quaternion paramQuaternion);

    public abstract void onAccelerometerData(Myo paramMyo, long paramLong, Vector3 paramVector3);

    public abstract void onGyroscopeData(Myo paramMyo, long paramLong, Vector3 paramVector3);

    public abstract void onRssi(Myo paramMyo, long paramLong, int paramInt);
}
