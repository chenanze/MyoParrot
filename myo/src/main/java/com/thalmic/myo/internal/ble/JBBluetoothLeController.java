package com.thalmic.myo.internal.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@TargetApi(18)
class JBBluetoothLeController implements BleGatt {
    private static final String TAG = "JBBluetoothLeController";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, BluetoothGatt> mGattConnections = new HashMap();
    private ExecutorService mOperationExecutor;
    private boolean mOperationPending;
    private Handler mCallbackHandler;
    private BleGattCallback mExternalCallback;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            JBBluetoothLeController.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Address address = addressOf(gatt);
                    if(status != 0) {
                        Log.e("JBBluetoothLeController", "Received error status=" + status + " for onConnectionStateChange on address=" + address);
                        if(newState == 0) {
                            JBBluetoothLeController.this.mExternalCallback.onDeviceConnectionFailed(address);
                        }
                    } else if(newState == 2) {
                        JBBluetoothLeController.this.mExternalCallback.onDeviceConnected(address);
                    } else if(newState == 0) {
                        JBBluetoothLeController.this.mExternalCallback.onDeviceDisconnected(address);
                    }

                }
            });
        }

        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            JBBluetoothLeController.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Address address = addressOf(gatt);
                    boolean success = status == 0;
                    if(!success) {
                        Log.e("JBBluetoothLeController", "Received error status=" + status + " for onServicesDiscovered on address=" + address);
                    }

                    JBBluetoothLeController.this.mExternalCallback.onServicesDiscovered(address, success);
                }
            });
        }

        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            JBBluetoothLeController.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    JBBluetoothLeController.this.mExternalCallback.onCharacteristicChanged(addressOf(gatt), characteristic.getUuid(), characteristic.getValue());
                }
            });
        }

        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            this.onOperationFinished();
            JBBluetoothLeController.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Address address = addressOf(gatt);
                    boolean success = status == 0;
                    if(!success) {
                        Log.e("JBBluetoothLeController", "Received error status=" + status + " for onCharacteristicRead of " + characteristic.getUuid() + " on address=" + address);
                    }

                    JBBluetoothLeController.this.mExternalCallback.onCharacteristicRead(address, characteristic.getUuid(), characteristic.getValue(), success);
                }
            });
        }

        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            this.onOperationFinished();
            JBBluetoothLeController.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Address address = addressOf(gatt);
                    boolean success = status == 0;
                    if(!success) {
                        Log.e("JBBluetoothLeController", "Received error status=" + status + " for onCharacteristicWrite of " + characteristic.getUuid() + " on address=" + address);
                    }

                    JBBluetoothLeController.this.mExternalCallback.onCharacteristicWrite(address, characteristic.getUuid(), success);
                }
            });
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            this.onOperationFinished();
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            this.onOperationFinished();
        }

        public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
            this.onOperationFinished();
            JBBluetoothLeController.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Address address = addressOf(gatt);
                    boolean success = status == 0;
                    if(!success) {
                        Log.e("JBBluetoothLeController", "Received error status=" + status + " for onReadRemoteRssi on address=" + address);
                    }

                    JBBluetoothLeController.this.mExternalCallback.onReadRemoteRssi(address, rssi, success);
                }
            });
        }

        private void onOperationFinished() {
            JBBluetoothLeController.this.mOperationPending = false;
        }

        private Address addressOf(BluetoothGatt gatt) {
            return new Address(gatt.getDevice().getAddress());
        }
    };

    JBBluetoothLeController(Context context) {
        this.mContext = context;
        this.mOperationExecutor = Executors.newSingleThreadExecutor();
        this.mCallbackHandler = new Handler();
        BluetoothManager bluetoothManager = (BluetoothManager)this.mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void setBleGattCallback(BleGattCallback callback) {
        this.mExternalCallback = callback;
    }

    public void close() {
        this.submitTask(new Runnable() {
            public void run() {
                HashSet keySet = new HashSet(JBBluetoothLeController.this.mGattConnections.keySet());
                Iterator i$ = keySet.iterator();

                while(i$.hasNext()) {
                    String address = (String)i$.next();
                    BluetoothGatt bluetoothGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.remove(address);
                    bluetoothGatt.close();
                }

            }
        });
        this.mOperationExecutor.shutdown();
    }

    public boolean connect(final String address, final boolean autoConnect) {
        if(this.mOperationExecutor.isShutdown()) {
            Log.w("JBBluetoothLeController", "Could not connect to address " + address + ". Executor shutdown.");
            return false;
        } else {
            Future result = this.mOperationExecutor.submit(new Callable() {
                public Boolean call() throws Exception {
                    if(JBBluetoothLeController.this.mBluetoothAdapter != null && address != null) {
                        BluetoothGatt existingGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                        BluetoothDevice device;
                        if(existingGatt != null) {
                            existingGatt.close();
                            device = existingGatt.getDevice();
                        } else {
                            device = JBBluetoothLeController.this.mBluetoothAdapter.getRemoteDevice(address);
                        }

                        BluetoothGatt bluetoothGatt = device.connectGatt(JBBluetoothLeController.this.mContext, autoConnect, JBBluetoothLeController.this.mGattCallback);
                        JBBluetoothLeController.this.mGattConnections.put(address, bluetoothGatt);
                        return Boolean.valueOf(bluetoothGatt != null);
                    } else {
                        Log.w("JBBluetoothLeController", "BluetoothAdapter not initialized or unspecified address.");
                        return Boolean.valueOf(false);
                    }
                }
            });

            try {
                return ((Boolean)result.get()).booleanValue();
            } catch (InterruptedException var5) {
                Log.w("JBBluetoothLeController", "GATT connect interrupted for address: " + address, var5);
                return false;
            } catch (ExecutionException var6) {
                Log.e("JBBluetoothLeController", "Problem during GATT connect for address: " + address, var6);
                return false;
            }
        }
    }

    public void disconnect(final String address) {
        this.submitTask(new Runnable() {
            public void run() {
                BluetoothGatt bluetoothGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                if(JBBluetoothLeController.this.mBluetoothAdapter != null && bluetoothGatt != null) {
                    bluetoothGatt.disconnect();
                } else {
                    Log.w("JBBluetoothLeController", "BluetoothAdapter not initialized");
                }
            }
        });
    }

    public void discoverServices(final String address) {
        this.submitTask(new Runnable() {
            public void run() {
                BluetoothGatt bluetoothGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                bluetoothGatt.discoverServices();
            }
        });
    }

    public void readCharacteristic(final String address, final UUID serviceUuid, final UUID charUuid) {
        this.submitTask(new Runnable() {
            public void run() {
                BluetoothGatt bluetoothGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                JBBluetoothLeController.this.readCharacteristic(bluetoothGatt, characteristic);
            }
        });
    }

    public void writeCharacteristic(final String address, final UUID serviceUuid, final UUID charUuid, final byte[] value) {
        this.submitTask(new Runnable() {
            public void run() {
                BluetoothGatt bluetoothGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                JBBluetoothLeController.this.writeCharacteristic(bluetoothGatt, characteristic, value);
            }
        });
    }

    public void setCharacteristicNotification(final String address, final UUID serviceUuid, final UUID charUuid, final boolean enable, final boolean indicate) {
        this.submitTask(new Runnable() {
            public void run() {
                BluetoothGatt bluetoothGatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                JBBluetoothLeController.this.setCharacteristicNotification(bluetoothGatt, characteristic, enable, indicate);
            }
        });
    }

    public void readRemoteRssi(final String address) {
        this.submitTask(new Runnable() {
            public void run() {
                BluetoothGatt gatt = (BluetoothGatt)JBBluetoothLeController.this.mGattConnections.get(address);
                if(gatt.readRemoteRssi()) {
                    JBBluetoothLeController.this.mOperationPending = true;
                    JBBluetoothLeController.this.waitForOperationCompletion();
                } else {
                    Log.e("JBBluetoothLeController", "Failed reading remote rssi");
                }

            }
        });
    }

    private Future<?> submitTask(Runnable task) {
        if(this.mOperationExecutor.isShutdown()) {
            Log.w("JBBluetoothLeController", "Could not submit task. Executor shutdown.");
            return null;
        } else {
            return this.mOperationExecutor.submit(task);
        }
    }

    private void readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(gatt.readCharacteristic(characteristic)) {
            this.mOperationPending = true;
            this.waitForOperationCompletion();
        } else {
            Log.e("JBBluetoothLeController", "Failed reading characteristic " + characteristic.getUuid());
        }

    }

    private void writeCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        characteristic.setValue(value);
        if(gatt.writeCharacteristic(characteristic)) {
            this.mOperationPending = true;
            this.waitForOperationCompletion();
        } else {
            Log.e("JBBluetoothLeController", "Failed writing characteristic " + characteristic.getUuid());
        }

    }

    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enable, boolean indicate) {
        if(!gatt.setCharacteristicNotification(characteristic, enable)) {
            Log.e("JBBluetoothLeController", "Failed setting characteristic notification " + characteristic.getUuid());
        } else {
            BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            byte[] value = enable?(indicate?BluetoothGattDescriptor.ENABLE_INDICATION_VALUE:BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE):BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            clientConfig.setValue(value);
            if(gatt.writeDescriptor(clientConfig)) {
                this.mOperationPending = true;
                this.waitForOperationCompletion();
            } else {
                Log.e("JBBluetoothLeController", "Failed writing descriptor " + clientConfig.getUuid());
            }

        }
    }

    private void waitForOperationCompletion() {
        long timeout = 1000L;
        long interval = 10L;

        long t;
        for(t = 1000L; t > 0L && this.mOperationPending; t -= 10L) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException var8) {
                var8.printStackTrace();
            }
        }

        if(t == 0L) {
            Log.w("JBBluetoothLeController", "Wait for operation completion timed out.");
        }

    }
}
