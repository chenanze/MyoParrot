package com.thalmic.myo;

import java.util.ArrayList;

class MultiListener
        implements DeviceListener {
    private ArrayList<DeviceListener> mListeners = new ArrayList();

    public void add(DeviceListener listener) {
        this.mListeners.add(listener);
    }

    public void remove(DeviceListener listener) {
        this.mListeners.remove(listener);
    }

    public boolean contains(DeviceListener listener) {
        return this.mListeners.contains(listener);
    }

    public void clear() {
        this.mListeners.clear();
    }

    public void onAttach(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onAttach(myo, timestamp);
        }
    }

    public void onDetach(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onDetach(myo, timestamp);
        }
    }

    public void onConnect(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onConnect(myo, timestamp);
        }
    }

    public void onDisconnect(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onDisconnect(myo, timestamp);
        }
    }

    public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onArmSync(myo, timestamp, arm, xDirection);
        }
    }

    public void onArmUnsync(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onArmUnsync(myo, timestamp);
        }
    }

    public void onUnlock(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onUnlock(myo, timestamp);
        }
    }

    public void onLock(Myo myo, long timestamp) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onLock(myo, timestamp);
        }
    }

    public void onPose(Myo myo, long timestamp, Pose pose) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onPose(myo, timestamp, pose);
        }
    }

    public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onOrientationData(myo, timestamp, rotation);
        }
    }

    public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onAccelerometerData(myo, timestamp, accel);
        }
    }

    public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onGyroscopeData(myo, timestamp, gyro);
        }
    }

    public void onRssi(Myo myo, long timestamp, int rssi) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((DeviceListener) this.mListeners.get(i)).onRssi(myo, timestamp, rssi);
        }
    }
}
