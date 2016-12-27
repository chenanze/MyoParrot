package com.thalmic.myo.internal.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.thalmic.myo.internal.util.ByteUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@TargetApi(18)
class JBBleManager implements BleManager {
    private Context mContext;
    private BluetoothAdapter mAdapter;
    private JBBluetoothLeController mController;
    private HashMap<BleScanCallback, android.bluetooth.BluetoothAdapter.LeScanCallback> mCallbacks = new HashMap();

    JBBleManager(Context context) {
        this.mContext = context.getApplicationContext();
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mAdapter = bluetoothManager.getAdapter();
        this.mController = new JBBluetoothLeController(context);
    }

    public boolean isBluetoothSupported() {
        return !this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le") ? false : this.mAdapter != null;
    }

    public BleGatt getBleGatt() {
        return this.mController;
    }

    public boolean startBleScan(BleScanCallback callback) {
        android.bluetooth.BluetoothAdapter.LeScanCallback leScanCallback = (android.bluetooth.BluetoothAdapter.LeScanCallback) this.mCallbacks.get(callback);
        if (leScanCallback == null) {
            leScanCallback = this.createCallback(callback);
            this.mCallbacks.put(callback, leScanCallback);
        }

        return this.mAdapter.startLeScan(leScanCallback);
    }

    public void stopBleScan(BleScanCallback callback) {
        android.bluetooth.BluetoothAdapter.LeScanCallback leScanCallback = (android.bluetooth.BluetoothAdapter.LeScanCallback) this.mCallbacks.remove(callback);
        this.mAdapter.stopLeScan(leScanCallback);
    }

    public boolean connect(String address, boolean autoConnect) {
        return this.mController.connect(address, autoConnect);
    }

    public void disconnect(String address) {
        this.mController.disconnect(address);
    }

    public void dispose() {
        this.mController.close();
    }

    private android.bluetooth.BluetoothAdapter.LeScanCallback createCallback(BleScanCallback callback) {
        return new JBBleManager.LeScanCallback(callback);
    }

    static List<UUID> parseServiceUuids(byte[] adv_data) {
        ArrayList uuids = new ArrayList();

        int len;
        for (int offset = 0; offset < adv_data.length - 2; offset += len - 1) {
            len = adv_data[offset++];
            if (len == 0) {
                break;
            }

            byte type = adv_data[offset++];
            switch (type) {
                case 2:
                case 3:
                    while (len > 1) {
                        byte var6 = adv_data[offset++];
                        int var7 = var6 + (adv_data[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", new Object[]{Integer.valueOf(var7)})));
                    }
                case 4:
                case 5:
                default:
                    break;
                case 6:
                case 7:
                    while (len > 15) {
                        UUID uuid = ByteUtil.getUuidFromBytes(adv_data, offset);
                        len -= 16;
                        offset += 16;
                        uuids.add(uuid);
                    }
            }
        }

        return uuids;
    }

    static class LeScanCallback implements android.bluetooth.BluetoothAdapter.LeScanCallback {
        private BleScanCallback mCallback;

        LeScanCallback(BleScanCallback callback) {
            this.mCallback = callback;
        }

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Address address = new Address(device.getAddress());
            List uuids = JBBleManager.parseServiceUuids(scanRecord);
            UUID serviceUuid = uuids.isEmpty() ? null : (UUID) uuids.get(0);
            this.mCallback.onBleScan(address, device.getName(), rssi, serviceUuid);
        }
    }
}
