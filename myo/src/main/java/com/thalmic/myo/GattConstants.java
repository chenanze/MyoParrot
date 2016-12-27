package com.thalmic.myo;

import java.util.UUID;

class GattConstants
{
    static final UUID GAP_SERVICE_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    static final UUID DEVICE_NAME_CHAR_UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    static final UUID CONTROL_SERVICE_UUID = UUID.fromString("d5060001-a904-deb9-4748-2c7f4a124842");
    static final UUID FIRMWARE_INFO_CHAR_UUID = UUID.fromString("d5060101-a904-deb9-4748-2c7f4a124842");
    static final UUID FIRMWARE_VERSION_CHAR_UUID = UUID.fromString("d5060201-a904-deb9-4748-2c7f4a124842");
    static final UUID COMMAND_CHAR_UUID = UUID.fromString("d5060401-a904-deb9-4748-2c7f4a124842");
    static final UUID IMU_SERVICE_UUID = UUID.fromString("d5060002-a904-deb9-4748-2c7f4a124842");
    static final UUID IMU_DATA_CHAR_UUID = UUID.fromString("d5060402-a904-deb9-4748-2c7f4a124842");
    static final UUID CLASSIFIER_SERVICE_UUID = UUID.fromString("d5060003-a904-deb9-4748-2c7f4a124842");
    static final UUID CLASSIFIER_EVENT_CHAR_UUID = UUID.fromString("d5060103-a904-deb9-4748-2c7f4a124842");
    static final UUID FV_SERVICE_UUID = UUID.fromString("d5060004-a904-deb9-4748-2c7f4a124842");
    static final UUID FV_DATA_CHAR_UUID = UUID.fromString("d5060104-a904-deb9-4748-2c7f4a124842");
    static final UUID EMG_SERVICE_UUID = UUID.fromString("d5060005-a904-deb9-4748-2c7f4a124842");
    static final UUID EMG0_DATA_CHAR_UUID = UUID.fromString("d5060105-a904-deb9-4748-2c7f4a124842");
    static final UUID EMG1_DATA_CHAR_UUID = UUID.fromString("d5060205-a904-deb9-4748-2c7f4a124842");
    static final UUID EMG2_DATA_CHAR_UUID = UUID.fromString("d5060305-a904-deb9-4748-2c7f4a124842");
    static final UUID EMG3_DATA_CHAR_UUID = UUID.fromString("d5060405-a904-deb9-4748-2c7f4a124842");
}
