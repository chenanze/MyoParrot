package com.thalmic.myo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class FirmwareInfo
{
    static final int EXPECTED_BYTE_LENGTH = 8;
    public Pose unlockPose;

    FirmwareInfo(byte[] array)
    {
        if (array.length < 8) {
            throw new IllegalArgumentException("Unexpected length=" + array.length + " of array. Expecting length of " + 8);
        }
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int unlockPoseValue = buffer.getShort(6) & 0xFFFF;
        this.unlockPose = ClassifierEvent.poseFromClassifierPose(unlockPoseValue);
    }

    FirmwareInfo(Pose pose)
    {
        if (pose == null) {
            throw new IllegalArgumentException("pose cannot be null");
        }
        this.unlockPose = pose;
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        FirmwareInfo that = (FirmwareInfo)o;
        if (this.unlockPose != that.unlockPose) {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        return this.unlockPose.hashCode();
    }
}
