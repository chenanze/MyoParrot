package com.thalmic.myo;

import android.util.Log;
import android.util.Pair;

import com.thalmic.myo.ControlCommand.EmgMode;
import com.thalmic.myo.Myo.ConnectionState;
import com.thalmic.myo.internal.ble.Address;
import com.thalmic.myo.internal.ble.BleGatt;
import com.thalmic.myo.internal.ble.BleGattCallback;
import com.thalmic.myo.internal.util.ByteUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

class GattCallback extends BleGattCallback {
    private static final String TAG = "GattCallback";
    private Hub mHub;
    private BleGatt mBleGatt;
    private MyoGatt mMyoGatt;
    private GattCallback.UpdateParser mParser;
    private HashMap<Address, LinkedHashMap<UUID, GattCallback.InitReadChar>> mInitializingMyos = new HashMap();
    private HashSet<GattCallback.ValueListener> mListeners = new HashSet();

    public GattCallback(Hub hub) {
        this.mHub = hub;
    }

    void setBleGatt(BleGatt bleGatt) {
        this.mBleGatt = bleGatt;
    }

    void setMyoGatt(MyoGatt myoGatt) {
        this.mMyoGatt = myoGatt;
    }

    void setUpdateParser(GattCallback.UpdateParser updateParser) {
        this.mParser = updateParser;
    }

    void addValueListener(GattCallback.ValueListener listener) {
        this.mListeners.add(listener);
    }

    void removeValueListener(GattCallback.ValueListener listener) {
        this.mListeners.remove(listener);
    }

    public void onDeviceConnectionFailed(Address address) {
        this.onDeviceDisconnected(address);
    }

    public void onDeviceConnected(Address address) {
        if(!Hub.allowedToConnectToMyo(this.mHub, address.toString())) {
            this.onMyoInitializationFailed(address);
        } else {
            this.mInitializingMyos.put(address, new LinkedHashMap());
            this.mBleGatt.discoverServices(address.toString());
        }
    }

    public void onDeviceDisconnected(Address address) {
        if(!this.mInitializingMyos.containsKey(address)) {
            Myo myo = this.getMyoDevice(address);
            this.mParser.onMyoDisconnected(myo);
        } else {
            this.mInitializingMyos.remove(address);
        }

    }

    public void onServicesDiscovered(Address address, boolean success) {
        if(!success) {
            this.onMyoInitializationFailed(address);
        } else {
            this.readNecessaryCharacteristics(address);
        }
    }

    public void onCharacteristicRead(Address address, UUID uuid, byte[] value, boolean success) {
        Myo myo = this.getMyoDevice(address);
        if(!success) {
            if(myo.getConnectionState() == ConnectionState.CONNECTING) {
                this.onMyoInitializationFailed(address);
            }

        } else {
            if(myo.getConnectionState() == ConnectionState.CONNECTING && ((LinkedHashMap)this.mInitializingMyos.get(address)).remove(uuid) != null) {
                if(GattConstants.DEVICE_NAME_CHAR_UUID.equals(uuid)) {
                    myo.setName(ByteUtil.getString(value, 0));
                } else if(GattConstants.FIRMWARE_VERSION_CHAR_UUID.equals(uuid)) {
                    boolean firmwareSupported = this.onFirmwareVersionRead(myo, value);
                    if(!firmwareSupported) {
                        this.onMyoInitializationFailed(address);
                        return;
                    }
                } else if(GattConstants.FIRMWARE_INFO_CHAR_UUID.equals(uuid)) {
                    this.onFirmwareInfoRead(myo, value);
                }

                if(!this.readNextInitializationCharacteristic(address)) {
                    this.mInitializingMyos.remove(address);
                    this.onMyoInitializationSucceeded(address);
                }
            }

        }
    }

    public void onCharacteristicChanged(Address address, UUID uuid, byte[] value) {
        Myo myo = this.getMyoDevice(address);
        this.mParser.onCharacteristicChanged(myo, uuid, value);
        Iterator i$ = this.mListeners.iterator();

        while(i$.hasNext()) {
            GattCallback.ValueListener listener = (GattCallback.ValueListener)i$.next();
            listener.onCharacteristicChanged(myo, uuid, value);
        }

    }

    public void onReadRemoteRssi(Address address, int rssi, boolean success) {
        if(success) {
            Myo myo = this.getMyoDevice(address);
            this.mParser.onReadRemoteRssi(myo, rssi);
        }
    }

    private Myo getMyoDevice(Address address) {
        Myo device = this.mHub.getDevice(address.toString());
        if(device == null) {
            device = this.mHub.addKnownDevice(address);
        }

        return device;
    }

    private void readNecessaryCharacteristics(Address address) {
        ((LinkedHashMap)this.mInitializingMyos.get(address)).put(GattConstants.FIRMWARE_VERSION_CHAR_UUID, new GattCallback.InitReadChar(GattConstants.CONTROL_SERVICE_UUID, GattConstants.FIRMWARE_VERSION_CHAR_UUID));
        ((LinkedHashMap)this.mInitializingMyos.get(address)).put(GattConstants.FIRMWARE_INFO_CHAR_UUID, new GattCallback.InitReadChar(GattConstants.CONTROL_SERVICE_UUID, GattConstants.FIRMWARE_INFO_CHAR_UUID));
        ((LinkedHashMap)this.mInitializingMyos.get(address)).put(GattConstants.DEVICE_NAME_CHAR_UUID, new GattCallback.InitReadChar(GattConstants.GAP_SERVICE_UUID, GattConstants.DEVICE_NAME_CHAR_UUID));
        this.readNextInitializationCharacteristic(address);
    }

    private boolean readNextInitializationCharacteristic(Address address) {
        Iterator iterator = ((LinkedHashMap)this.mInitializingMyos.get(address)).values().iterator();
        if(!iterator.hasNext()) {
            return false;
        } else {
            GattCallback.InitReadChar readChar = (GattCallback.InitReadChar)iterator.next();
            this.mBleGatt.readCharacteristic(address.toString(), readChar.getService(), readChar.getCharacteristic());
            return true;
        }
    }

    boolean onFirmwareVersionRead(Myo myo, byte[] value) {
        FirmwareVersion fwVersion;
        try {
            fwVersion = new FirmwareVersion(value);
        } catch (IllegalArgumentException var5) {
            Log.e("GattCallback", "Problem reading FirmwareVersion.", var5);
            fwVersion = new FirmwareVersion();
        }

        myo.setFirmwareVersion(fwVersion);
        if(!myo.isFirmwareVersionSupported()) {
            String format = "Myo (address=%s) firmware version (%s) is not supported. The SDK requires firmware version %d.x.x, minimum %d.%d.0.";
            Log.e("GattCallback", String.format(format, new Object[]{myo.getMacAddress(), fwVersion.toDisplayString(), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1)}));
            return false;
        } else {
            return true;
        }
    }

    void onFirmwareInfoRead(Myo myo, byte[] value) {
        try {
            FirmwareInfo e = new FirmwareInfo(value);
            myo.setUnlockPose(e.unlockPose);
        } catch (IllegalArgumentException var4) {
            Log.e("GattCallback", "Problem reading FirmwareInfo.", var4);
        }

    }

    private void onMyoInitializationSucceeded(Address address) {
        Myo myo = this.getMyoDevice(address);
        String addressString = address.toString();
        this.mBleGatt.setCharacteristicNotification(addressString, GattConstants.EMG_SERVICE_UUID, GattConstants.EMG0_DATA_CHAR_UUID, true, false);
        this.mBleGatt.setCharacteristicNotification(addressString, GattConstants.FV_SERVICE_UUID, GattConstants.FV_DATA_CHAR_UUID, true, false);
        this.mBleGatt.setCharacteristicNotification(addressString, GattConstants.IMU_SERVICE_UUID, GattConstants.IMU_DATA_CHAR_UUID, true, false);
        this.mBleGatt.setCharacteristicNotification(addressString, GattConstants.CLASSIFIER_SERVICE_UUID, GattConstants.CLASSIFIER_EVENT_CHAR_UUID, true, true);
        this.mMyoGatt.configureDataAcquisition(addressString, EmgMode.DISABLED, true, true);
        this.mParser.onMyoConnected(myo);
    }

    private void onMyoInitializationFailed(Address address) {
        Log.e("GattCallback", "Failure in initialization of Myo. Disconnecting from Myo with address=" + address);
        this.mMyoGatt.disconnect(address.toString());
    }

    interface UpdateParser extends GattCallback.ValueListener {
        void onMyoConnected(Myo var1);

        void onMyoDisconnected(Myo var1);

        void onReadRemoteRssi(Myo var1, int var2);
    }

    interface ValueListener {
        void onCharacteristicChanged(Myo var1, UUID var2, byte[] var3);
    }

    private static class InitReadChar extends Pair<UUID, UUID> {
        public InitReadChar(UUID serviceUuid, UUID characteristicUuid) {
            super(serviceUuid, characteristicUuid);
        }

        public UUID getService() {
            return (UUID)this.first;
        }

        public UUID getCharacteristic() {
            return (UUID)this.second;
        }
    }
}
