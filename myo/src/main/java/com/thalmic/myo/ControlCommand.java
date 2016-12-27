package com.thalmic.myo;

import com.thalmic.myo.Myo.UnlockType;
import com.thalmic.myo.Myo.VibrationType;

abstract class ControlCommand {
    private static final byte COMMAND_SET_MODE = 1;
    private static final byte COMMAND_VIBRATION = 3;
    private static final byte COMMAND_UNLOCK = 10;
    private static final byte COMMAND_USER_ACTION = 11;
    private static final byte VIBRATION_NONE = 0;
    private static final byte VIBRATION_SHORT = 1;
    private static final byte VIBRATION_MEDIUM = 2;
    private static final byte VIBRATION_LONG = 3;
    private static final byte EMG_MODE_DISABLED = 0;
    private static final byte EMG_MODE_RAW_FV = 1;
    private static final byte EMG_MODE_RAW_EMG = 2;
    private static final byte IMU_MODE_DISABLED = 0;
    private static final byte IMU_MODE_ENABLED = 1;
    private static final byte CLASSIFIER_MODE_DISABLED = 0;
    private static final byte CLASSIFIER_MODE_ENABLED = 1;
    private static final byte UNLOCK_LOCK = 0;
    private static final byte UNLOCK_TIMEOUT = 1;
    private static final byte UNLOCK_HOLD = 2;
    private static final byte USER_ACTION_GENERIC = 0;

    private ControlCommand() {
    }

    static byte[] createForSetMode(ControlCommand.EmgMode streamEmg, boolean streamImu, boolean enableClassifier) {
        byte emgMode = 0;
//        switch(ControlCommand.SyntheticClass_1.$SwitchMap$com$thalmic$myo$ControlCommand$EmgMode[streamEmg.ordinal()]) {
//            case 1:
//                emgMode = 1;
//                break;
//            case 2:
//                emgMode = 2;
//        }

        switch(streamEmg.ordinal()) {
            case 1:
                emgMode = 1;
                break;
            case 2:
                emgMode = 2;
        }


        int imuMode = streamImu?1:0;
        int classifierMode = enableClassifier?1:0;
        return createForSetMode(emgMode, (byte)imuMode, (byte)classifierMode);
    }

    private static byte[] createForSetMode(byte emgMode, byte imuMode, byte classifierMode) {
        byte[] controlCommand = new byte[ControlCommand.SetMode.values().length];
        controlCommand[ControlCommand.SetMode.COMMAND_TYPE.ordinal()] = 1;
        controlCommand[ControlCommand.SetMode.PAYLOAD_SIZE.ordinal()] = (byte)(controlCommand.length - 2);
        controlCommand[ControlCommand.SetMode.EMG_MODE.ordinal()] = emgMode;
        controlCommand[ControlCommand.SetMode.IMU_MODE.ordinal()] = imuMode;
        controlCommand[ControlCommand.SetMode.CLASSIFIER_MODE.ordinal()] = classifierMode;
        return controlCommand;
    }

    static byte[] createForVibrate(VibrationType vibrationType) {
        byte[] command = new byte[ControlCommand.Vibration.values().length];
        command[ControlCommand.Vibration.COMMAND_TYPE.ordinal()] = 3;
        command[ControlCommand.Vibration.PAYLOAD_SIZE.ordinal()] = 1;
        command[ControlCommand.Vibration.VIBRATION_TYPE.ordinal()] = getVibrationType(vibrationType);
        return command;
    }

    private static byte getVibrationType(VibrationType vibrationType) {
//        switch(ControlCommand.SyntheticClass_1.$SwitchMap$com$thalmic$myo$Myo$VibrationType[vibrationType.ordinal()]) {
//            case 1:
//                return (byte)1;
//            case 2:
//                return (byte)2;
//            case 3:
//                return (byte)3;
//            default:
//                return (byte)0;
//        }
        switch(vibrationType) {
            case SHORT:
                return (byte)1;
            case MEDIUM:
                return (byte)2;
            case LONG:
                return (byte)3;
            default:
                return (byte)0;
        }
    }

    static byte[] createForUnlock(UnlockType unlockType) {
        byte[] command = new byte[ControlCommand.Unlock.values().length];
        command[ControlCommand.Unlock.COMMAND_TYPE.ordinal()] = 10;
        command[ControlCommand.Unlock.PAYLOAD_SIZE.ordinal()] = 1;
        command[ControlCommand.Unlock.UNLOCK_TYPE.ordinal()] = getUnlockTypeType(unlockType);
        return command;
    }

    private static byte getUnlockTypeType(UnlockType unlockType) {
        if(unlockType == null) {
            return (byte)0;
        } else {
//            switch(ControlCommand.SyntheticClass_1.$SwitchMap$com$thalmic$myo$Myo$UnlockType[unlockType.ordinal()]) {
//                case 1:
//                    return (byte)1;
//                case 2:
//                    return (byte)2;
//                default:
//                    throw new IllegalArgumentException("Unknown UnlockType: " + unlockType);
//            }
            switch(unlockType) {
                case TIMED:
                    return (byte)1;
                case HOLD:
                    return (byte)2;
                default:
                    throw new IllegalArgumentException("Unknown UnlockType: " + unlockType);
            }
        }
    }

    static byte[] createForUserAction() {
        byte[] command = new byte[ControlCommand.Unlock.values().length];
        command[ControlCommand.UserAction.COMMAND_TYPE.ordinal()] = 11;
        command[ControlCommand.UserAction.PAYLOAD_SIZE.ordinal()] = 1;
        command[ControlCommand.UserAction.USER_ACTION.ordinal()] = 0;
        return command;
    }

    private static enum UserAction {
        COMMAND_TYPE,
        PAYLOAD_SIZE,
        USER_ACTION;

        private UserAction() {
        }
    }

    private static enum Unlock {
        COMMAND_TYPE,
        PAYLOAD_SIZE,
        UNLOCK_TYPE;

        private Unlock() {
        }
    }

    private static enum Vibration {
        COMMAND_TYPE,
        PAYLOAD_SIZE,
        VIBRATION_TYPE;

        private Vibration() {
        }
    }

    private static enum SetMode {
        COMMAND_TYPE,
        PAYLOAD_SIZE,
        EMG_MODE,
        IMU_MODE,
        CLASSIFIER_MODE;

        private SetMode() {
        }
    }

    public static enum EmgMode {
        DISABLED,
        FV,
        EMG;

        private EmgMode() {
        }
    }
}
