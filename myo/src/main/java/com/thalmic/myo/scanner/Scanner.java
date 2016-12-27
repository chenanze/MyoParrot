//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.thalmic.myo.scanner;

import android.os.Handler;
import android.util.Log;
import com.thalmic.myo.Myo;
import com.thalmic.myo.internal.ble.Address;
import com.thalmic.myo.internal.ble.BleManager;
import com.thalmic.myo.internal.ble.BleManager.BleScanCallback;
import com.thalmic.myo.internal.util.ByteUtil;
import com.thalmic.myo.scanner.MyoDeviceListAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class Scanner {
    private static final String TAG = "Scanner";
    private static UUID sAdvertisedUuid;
    private static final long SCAN_PERIOD = 5000L;
    private BleManager mBleManager;
    private Handler mHandler;
    private boolean mScanning;
    private final Runnable mStopRunnable = new Runnable() {
        public void run() {
            Scanner.this.stopScanning();
        }
    };
    private long mRestartInterval;
    private final Runnable mRestartRunnable = new Runnable() {
        public void run() {
            Scanner.this.restartScanning();
            if(Scanner.this.mRestartInterval > 0L) {
                Scanner.this.mHandler.postDelayed(Scanner.this.mRestartRunnable, Scanner.this.mRestartInterval);
            }

        }
    };
    private ArrayList<Scanner.OnScanningStartedListener> mScanningStartedListeners = new ArrayList();
    private Scanner.OnMyoScannedListener mMyoScannedListener;
    private Scanner.OnMyoClickedListener mMyoClickedListener;
    private MyoDeviceListAdapter mListAdapter = new MyoDeviceListAdapter();
    private BleScanCallback mBleScanCallback = new Scanner.ScanCallback();

    public Scanner(BleManager bleManager, Scanner.OnMyoScannedListener scannedListener, Scanner.OnMyoClickedListener clickedListener) {
        this.mBleManager = bleManager;
        this.mMyoScannedListener = scannedListener;
        this.mMyoClickedListener = clickedListener;
        this.mHandler = new Handler();
    }

    public void setBleManager(BleManager bleManager) {
        this.mBleManager = bleManager;
    }

    public void startScanning() {
        this.startScanning(5000L);
    }

    public void startScanning(long scanPeriod) {
        this.startScanning(scanPeriod, 0L);
    }

    public void startScanning(long scanPeriod, long restartInterval) {
        if(this.mScanning) {
            Log.w("Scanner", "Scan is already in progress. Ignoring call to startScanning.");
        } else {
            this.mHandler.removeCallbacks(this.mStopRunnable);
            if(scanPeriod > 0L) {
                this.mHandler.postDelayed(this.mStopRunnable, scanPeriod);
            }

            this.mHandler.removeCallbacks(this.mRestartRunnable);
            if(restartInterval > 0L) {
                this.mHandler.postDelayed(this.mRestartRunnable, restartInterval);
            }

            this.mRestartInterval = restartInterval;
            boolean started = this.mBleManager.startBleScan(this.mBleScanCallback);
            if(started) {
                this.mScanning = true;
                this.mListAdapter.clear();
                Iterator i$ = this.mScanningStartedListeners.iterator();

                while(i$.hasNext()) {
                    Scanner.OnScanningStartedListener listener = (Scanner.OnScanningStartedListener)i$.next();
                    listener.onScanningStarted();
                }
            }

        }
    }

    public void stopScanning() {
        this.mScanning = false;
        this.mHandler.removeCallbacks(this.mStopRunnable);
        this.mHandler.removeCallbacks(this.mRestartRunnable);
        this.mBleManager.stopBleScan(this.mBleScanCallback);
        Iterator i$ = this.mScanningStartedListeners.iterator();

        while(i$.hasNext()) {
            Scanner.OnScanningStartedListener listener = (Scanner.OnScanningStartedListener)i$.next();
            listener.onScanningStopped();
        }

    }

    private void restartScanning() {
        this.mBleManager.stopBleScan(this.mBleScanCallback);
        boolean started = this.mBleManager.startBleScan(this.mBleScanCallback);
        if(!started) {
            Iterator i$ = this.mScanningStartedListeners.iterator();

            while(i$.hasNext()) {
                Scanner.OnScanningStartedListener listener = (Scanner.OnScanningStartedListener)i$.next();
                listener.onScanningStopped();
            }
        }

    }

    public boolean isScanning() {
        return this.mScanning;
    }

    public void addOnScanningStartedListener(Scanner.OnScanningStartedListener listener) {
        this.mScanningStartedListeners.add(listener);
        if(this.mScanning) {
            listener.onScanningStarted();
        }

    }

    public void removeOnScanningStartedListener(Scanner.OnScanningStartedListener listener) {
        this.mScanningStartedListeners.remove(listener);
    }

    Scanner.OnMyoClickedListener getOnMyoClickedListener() {
        return this.mMyoClickedListener;
    }

    public Scanner.ScanListAdapter getScanListAdapter() {
        return this.mListAdapter;
    }

    MyoDeviceListAdapter getAdapter() {
        return this.mListAdapter;
    }

    static boolean isMyo(UUID serviceUuid) {
        if(sAdvertisedUuid == null) {
            byte[] uuidBytes = getServiceInfoUuidBytes();
            sAdvertisedUuid = ByteUtil.getUuidFromBytes(uuidBytes, 0);
        }

        return sAdvertisedUuid.equals(serviceUuid);
    }

    private static native byte[] getServiceInfoUuidBytes();

    private static boolean isDalvikVm() {
        return "Dalvik".equals(System.getProperty("java.vm.name"));
    }

    static {
        try {
            System.loadLibrary("gesture-classifier");
        } catch (UnsatisfiedLinkError var1) {
            if(isDalvikVm()) {
                throw var1;
            }
        }

    }

    public interface ScanListAdapter {
        void addDevice(Myo var1, int var2);

        void notifyDeviceChanged();
    }

    public interface OnMyoClickedListener {
        void onMyoClicked(Myo var1);
    }

    public interface OnMyoScannedListener {
        Myo onMyoScanned(Address var1, String var2, int var3);
    }

    public interface OnScanningStartedListener {
        void onScanningStarted();

        void onScanningStopped();
    }

    private class ScanCallback implements BleScanCallback {
        private ScanCallback() {
        }

        public void onBleScan(final Address address, final String name, final int rssi, final UUID serviceUuid) {
            Scanner.this.mHandler.post(new Runnable() {
                public void run() {
                    if(Scanner.isMyo(serviceUuid)) {
                        Myo myo = Scanner.this.mMyoScannedListener.onMyoScanned(address, name, rssi);
                        Scanner.this.mListAdapter.addDevice(myo, rssi);
                    }

                }
            });
        }
    }
}
