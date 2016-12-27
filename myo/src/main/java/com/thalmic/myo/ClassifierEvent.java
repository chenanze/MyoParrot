package com.thalmic.myo;

class ClassifierEvent
{
    static final int EXPECTED_BYTE_LENGTH = 3;
    private static final byte TYPE_ARM_SYNCED = 1;
    private static final byte TYPE_ARM_UNSYNCED = 2;
    private static final byte TYPE_POSE = 3;
    private static final byte TYPE_UNLOCKED = 4;
    private static final byte TYPE_LOCKED = 5;
    private static final byte ARM_RIGHT = 1;
    private static final byte ARM_LEFT = 2;
    private static final byte X_DIRECTION_TOWARD_WRIST = 1;
    private static final byte X_DIRECTION_TOWARD_ELBOW = 2;
    private static final int POSE_REST = 0;
    private static final int POSE_FIST = 1;
    private static final int POSE_WAVE_IN = 2;
    private static final int POSE_WAVE_OUT = 3;
    private static final int POSE_FINGERS_SPREAD = 4;
    private static final int POSE_DOUBLE_TAP = 5;
    private static final int POSE_UNKNOWN = 65535;
    private Type mType;
    private Arm mArm;
    private XDirection mXDirection;
    private Pose mPose;

    ClassifierEvent(byte[] data)
            throws IllegalArgumentException
    {
        parseData(data);
    }

    public static enum Type
    {
        ARM_SYNCED,  ARM_UNSYNCED,  POSE,  UNLOCKED,  LOCKED;

        private Type() {}
    }

    private static enum ClassifierEventFormat
    {
        EVENT_TYPE,  DATA_1,  DATA_2;

        private ClassifierEventFormat() {}
    }

    public Type getType()
    {
        return this.mType;
    }

    public Arm getArm()
    {
        return this.mArm;
    }

    public XDirection getXDirection()
    {
        return this.mXDirection;
    }

    public Pose getPose()
    {
        return this.mPose;
    }

    public String toString()
    {
        return "ClassifierEvent{mType=" + this.mType + ", mArm=" + this.mArm + ", mXDirection=" + this.mXDirection + ", mPose=" + this.mPose + '}';
    }

    private void parseData(byte[] data)
            throws IllegalArgumentException
    {
        if (data.length < 3) {
            throw new IllegalArgumentException("bad classifier event data length: " + data.length);
        }
        byte type = data[ClassifierEventFormat.EVENT_TYPE.ordinal()];
        if (type == 1)
        {
            this.mType = Type.ARM_SYNCED;
            this.mArm = readArm(data);
            this.mXDirection = readXDirection(data);
        }
        else if (type == 2)
        {
            this.mType = Type.ARM_UNSYNCED;
        }
        else if (type == 3)
        {
            this.mType = Type.POSE;
            this.mPose = readPose(data);
        }
        else if (type == 4)
        {
            this.mType = Type.UNLOCKED;
        }
        else if (type == 5)
        {
            this.mType = Type.LOCKED;
        }
        else
        {
            throw new IllegalArgumentException("unknown classifier type value: " + type);
        }
    }

    private Arm readArm(byte[] data)
            throws IllegalArgumentException
    {
        byte arm = data[ClassifierEventFormat.DATA_1.ordinal()];
        if (arm == 1) {
            return Arm.RIGHT;
        }
        if (arm == 2) {
            return Arm.LEFT;
        }
        throw new IllegalArgumentException("unknown arm value: " + arm);
    }

    private XDirection readXDirection(byte[] data)
            throws IllegalArgumentException
    {
        byte xDirection = data[ClassifierEventFormat.DATA_2.ordinal()];
        if (xDirection == 1) {
            return XDirection.TOWARD_WRIST;
        }
        if (xDirection == 2) {
            return XDirection.TOWARD_ELBOW;
        }
        throw new IllegalArgumentException("unknown x-direction value: " + xDirection);
    }

    private Pose readPose(byte[] data)
            throws IllegalArgumentException
    {
        int pose = MyoUpdateParser.getShort(data, ClassifierEventFormat.DATA_1.ordinal()) & 0xFFFF;

        return poseFromClassifierPose(pose);
    }

    public static Pose poseFromClassifierPose(int classifierPose)
    {
        switch (classifierPose)
        {
            case 0:
                return Pose.REST;
            case 1:
                return Pose.FIST;
            case 2:
                return Pose.WAVE_IN;
            case 3:
                return Pose.WAVE_OUT;
            case 4:
                return Pose.FINGERS_SPREAD;
            case 5:
                return Pose.DOUBLE_TAP;
            case 65535:
                return Pose.UNKNOWN;
        }
        throw new IllegalArgumentException("unknown pose value: " + classifierPose);
    }

    static int classifierPoseFromPose(Pose pose)
    {
        switch (pose)
        {
            case REST:
                return 0;
            case FIST:
                return 1;
            case WAVE_IN:
                return 2;
            case WAVE_OUT:
                return 3;
            case FINGERS_SPREAD:
                return 4;
            case DOUBLE_TAP:
                return 5;
            case UNKNOWN:
                return 65535;
        }
        throw new IllegalArgumentException("unknown pose value: " + pose);
    }
}
