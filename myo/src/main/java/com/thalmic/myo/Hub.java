package com.thalmic.myo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.thalmic.myo.ControlCommand.EmgMode;
import com.thalmic.myo.GattCallback.ValueListener;
import com.thalmic.myo.Myo.ConnectionState;
import com.thalmic.myo.internal.ble.Address;
import com.thalmic.myo.internal.ble.BleFactory;
import com.thalmic.myo.internal.ble.BleGatt;
import com.thalmic.myo.internal.ble.BleManager;
import com.thalmic.myo.scanner.Scanner;
import com.thalmic.myo.scanner.Scanner.OnMyoClickedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class Hub {
    private static final String TAG = "Hub";
    private static final String PREF_INSTALL_UUID_KEY = "INSTALL_UUID";
    private static final String PREF_FILE_NAME = "com.thalmic.myosdk";
    private static final int MAX_APPLICATION_IDENTIFIER_LENGTH = 255;
    private String mApplicationIdentifier;
    private String mInstallUuid;
    private BleManager mBleManager;
    private Handler mHandler;
    private int mMyoAttachAllowance;
    private Hub.LockingPolicy mLockingPolicy;
    private Scanner mScanner;
    private final ScanListener mScanListener;
    private final HashMap<String, Myo> mKnownDevices;
    private final MultiListener mListeners;
    private final MyoUpdateParser mParser;
    private final GattCallback mGattCallback;
    private final MyoGatt mMyoGatt;
    private final Reporter mReporter;

    public static Hub getInstance() {
        return Hub.InstanceHolder.INSTANCE;
    }

    static Hub createInstanceForTests(BleManager bleManager, Handler handler, Scanner scanner, ScanListener scanListener, MultiListener listeners, MyoUpdateParser parser, GattCallback gattCallback, MyoGatt myoGatt) {
        return new Hub(bleManager, handler, scanner, scanListener, listeners, parser, gattCallback, myoGatt);
    }

    private Hub() {
        this.mKnownDevices = new HashMap();
        this.mReporter = new Reporter();
        this.mListeners = new MultiListener();
        this.mParser = new MyoUpdateParser(this, this.mListeners);
        this.mGattCallback = new GattCallback(this);
        this.mMyoGatt = new MyoGatt(this);
        this.mScanListener = new ScanListener(this);
        this.mParser.setReporter(this.mReporter);
        this.mGattCallback.setUpdateParser(this.mParser);
        this.mGattCallback.setMyoGatt(this.mMyoGatt);
    }

    private Hub(BleManager bleManager, Handler handler, Scanner scanner, ScanListener scanListener, MultiListener listeners, MyoUpdateParser parser, GattCallback gattCallback, MyoGatt myoGatt) {
        this.mKnownDevices = new HashMap();
        this.mReporter = new Reporter();
        this.mBleManager = bleManager;
        this.mHandler = handler;
        this.mScanner = scanner;
        this.mScanListener = scanListener;
        this.mListeners = listeners;
        this.mParser = parser;
        this.mGattCallback = gattCallback;
        this.mMyoGatt = myoGatt;
        this.setMyoAttachAllowance(1);
    }

    public boolean init(Context context) {
        return this.init(context, "");
    }

    public boolean init(Context context, String applicationIdentifier) throws IllegalArgumentException {
        if(this.isValidApplicationIdentifier(applicationIdentifier)) {
            this.mApplicationIdentifier = applicationIdentifier;
            if(this.mBleManager == null) {
                this.mBleManager = BleFactory.createBleManager(context.getApplicationContext());
            }

            if(this.mBleManager == null) {
                Log.e("Hub", "Could not create BleManager");
                return false;
            } else if(!this.mBleManager.isBluetoothSupported()) {
                Log.e("Hub", "Bluetooth not supported");
                return false;
            } else {
                if(this.mScanner == null) {
                    this.setMyoAttachAllowance(1);
                    this.setLockingPolicy(Hub.LockingPolicy.STANDARD);
                    SharedPreferences bleGatt = context.getSharedPreferences("com.thalmic.myosdk", 0);
                    if(bleGatt != null) {
                        this.mInstallUuid = bleGatt.getString("INSTALL_UUID", "");
                        if(TextUtils.isEmpty(this.mInstallUuid)) {
                            this.mInstallUuid = UUID.randomUUID().toString();
                            bleGatt.edit().putString("INSTALL_UUID", this.mInstallUuid).apply();
                        }
                    } else {
                        this.mInstallUuid = UUID.randomUUID().toString();
                    }

                    this.mHandler = new Handler();
                    this.mScanner = new Scanner(this.mBleManager, this.mScanListener, new Hub.ScanItemClickListener());
                    this.mScanner.addOnScanningStartedListener(this.mScanListener);
                    this.addListener(new AbstractDeviceListener() {
                        public void onConnect(Myo myo, long timestamp) {
                            Hub.this.mScanner.getScanListAdapter().notifyDeviceChanged();
                        }

                        public void onDisconnect(Myo myo, long timestamp) {
                            Hub.this.mScanner.getScanListAdapter().notifyDeviceChanged();
                        }
                    });
                    this.mParser.setScanner(this.mScanner);
                }

                BleGatt bleGatt1 = this.mBleManager.getBleGatt();
                bleGatt1.setBleGattCallback(this.mGattCallback);
                this.mGattCallback.setBleGatt(bleGatt1);
                this.mMyoGatt.setBleManager(this.mBleManager);
                this.mScanner.setBleManager(this.mBleManager);
                return true;
            }
        } else {
            throw new IllegalArgumentException("Invalid application identifier");
        }
    }

    public void setSendUsageData(boolean sendUsageData) {
        this.mReporter.setSendUsageData(sendUsageData);
    }

    public boolean isSendingUsageData() {
        return this.mReporter.isSendingUsageData();
    }

    public void shutdown() {
        Iterator i$;
        Myo myo;
        if(VERSION.SDK_INT >= 21) {
            i$ = this.mKnownDevices.values().iterator();

            while(i$.hasNext()) {
                myo = (Myo)i$.next();
                if(myo.isConnected()) {
                    this.mMyoGatt.configureDataAcquisition(myo.getMacAddress(), EmgMode.DISABLED, false, true);
                }
            }
        }

        this.mBleManager.dispose();
        this.mBleManager = null;
        this.mListeners.clear();
        i$ = this.mKnownDevices.values().iterator();

        while(i$.hasNext()) {
            myo = (Myo)i$.next();
            myo.setAttached(false);
            myo.setConnectionState(ConnectionState.DISCONNECTED);
        }

    }

    String getApplicationIdentifier() {
        return this.mApplicationIdentifier;
    }

    String getInstallUuid() {
        return this.mInstallUuid;
    }

    MyoGatt getMyoGatt() {
        return this.mMyoGatt;
    }

    public Scanner getScanner() {
        return this.mScanner;
    }

    Myo getDevice(String address) {
        return (Myo)this.mKnownDevices.get(address);
    }

    public ArrayList<Myo> getConnectedDevices() {
        ArrayList connectedMyos = new ArrayList();
        Iterator i$ = this.mKnownDevices.values().iterator();

        while(i$.hasNext()) {
            Myo myo = (Myo)i$.next();
            if(myo.isConnected()) {
                connectedMyos.add(myo);
            }
        }

        return connectedMyos;
    }

    public void addListener(final DeviceListener listener) {
        if(this.mListeners.contains(listener)) {
            throw new IllegalArgumentException("Trying to add a listener that is already registered.");
        } else {
            this.mListeners.add(listener);
            this.mHandler.post(new Runnable() {
                public void run() {
                    long timestamp = Hub.this.now();
                    Iterator i$ = Hub.this.mKnownDevices.values().iterator();

                    while(i$.hasNext()) {
                        Myo myo = (Myo)i$.next();
                        if(myo.isAttached()) {
                            listener.onAttach(myo, timestamp);
                            if(myo.getConnectionState() == ConnectionState.CONNECTED) {
                                listener.onConnect(myo, timestamp);
                            }
                        }
                    }

                }
            });
        }
    }

    public void removeListener(DeviceListener listener) {
        this.mListeners.remove(listener);
    }

    public void attachToAdjacentMyo() {
        this.attachToAdjacentMyos(1);
    }

    public void attachToAdjacentMyos(int count) {
        if(count < 1) {
            throw new IllegalArgumentException("The number of Myos to attach must be greater than 0.");
        } else {
            int numAttachedDevices = this.getMyoAttachCount();
            if(numAttachedDevices + count > this.mMyoAttachAllowance) {
                Log.w("Hub", String.format("Myo attach allowance is set to %d. There are currently %d attached Myos. Ignoring attach request.", new Object[]{Integer.valueOf(this.mMyoAttachAllowance), Integer.valueOf(numAttachedDevices)}));
            } else {
                this.mScanListener.attachToAdjacent(count);
            }
        }
    }

    public void attachByMacAddress(String macAddress) {
        int numAttachedDevices = this.getMyoAttachCount();
        if(numAttachedDevices >= this.mMyoAttachAllowance) {
            Log.w("Hub", String.format("Myo attach allowance is set to %d. There are currently %dattached Myo. Ignoring attach request.", new Object[]{Integer.valueOf(this.mMyoAttachAllowance), Integer.valueOf(numAttachedDevices)}));
        } else {
            Myo myo = this.getDevice(macAddress);
            if(myo != null && myo.isConnected()) {
                Log.w("Hub", "Already attached to the Myo at address=" + macAddress + ". Ignoring attach request.");
            } else {
                this.mScanListener.attachByMacAddress(macAddress);
            }

        }
    }

    public void detach(String macAddress) {
        Myo myo = this.getDevice(macAddress);
        if(myo != null && myo.isAttached()) {
            this.mMyoGatt.disconnect(myo.getMacAddress());
        } else {
            Log.w("Hub", "No attached Myo at address=" + macAddress + ". Nothing to detach.");
        }

    }

    public long now() {
        return SystemClock.elapsedRealtime();
    }

    public void setLockingPolicy(Hub.LockingPolicy lockingPolicy) {
        this.mLockingPolicy = lockingPolicy;
    }

    public Hub.LockingPolicy getLockingPolicy() {
        return this.mLockingPolicy;
    }

    public int getMyoAttachAllowance() {
        return this.mMyoAttachAllowance;
    }

    public void setMyoAttachAllowance(int myoAttachAllowance) {
        this.mMyoAttachAllowance = myoAttachAllowance;
    }

    int getMyoAttachCount() {
        int count = 0;
        Iterator i$ = this.mKnownDevices.values().iterator();

        while(i$.hasNext()) {
            Myo myo = (Myo)i$.next();
            if(myo.isAttached()) {
                ++count;
            }
        }

        return count;
    }

    boolean isInitialized() {
        return this.mBleManager != null;
    }

    void addGattValueListener(ValueListener listener) {
        this.mGattCallback.addValueListener(listener);
    }

    Myo addKnownDevice(Address address) {
        Myo myo = (Myo)this.mKnownDevices.get(address.toString());
        if(myo == null) {
            myo = new Myo(this, address);
            this.mKnownDevices.put(myo.getMacAddress(), myo);
        }

        return myo;
    }

    ArrayList<Myo> getKnownDevices() {
        return new ArrayList(this.mKnownDevices.values());
    }

    static boolean allowedToConnectToMyo(Hub hub, String address) {
        Myo myo = hub.getDevice(address);
        if(myo != null && myo.isAttached()) {
            return true;
        } else {
            int numAttachedDevices = hub.getMyoAttachCount();
            int attachAllowance = hub.getMyoAttachAllowance();
            if(numAttachedDevices >= attachAllowance) {
                Log.w("Hub", String.format("Myo attach allowance is set to %d. There are currently %d attached Myos.", new Object[]{Integer.valueOf(attachAllowance), Integer.valueOf(numAttachedDevices)}));
                return false;
            } else {
                return true;
            }
        }
    }

    void connectToScannedMyo(String address) {
        if(allowedToConnectToMyo(this, address)) {
            boolean connecting = this.mMyoGatt.connect(address);
            if(connecting) {
                this.mScanner.getScanListAdapter().notifyDeviceChanged();
            }

        }
    }

    void disconnectFromScannedMyo(String address) {
        this.mMyoGatt.disconnect(address);
        this.mScanner.getScanListAdapter().notifyDeviceChanged();
    }

    private boolean isValidApplicationIdentifier(String applicationIdentifier) {
        if(applicationIdentifier == null) {
            return false;
        } else if(applicationIdentifier.isEmpty()) {
            return true;
        } else if(applicationIdentifier.length() > 255) {
            return false;
        } else {
            char prevChar = 46;
            int fullStopCount = 0;

            for(int i = 0; i < applicationIdentifier.length(); ++i) {
                char c = applicationIdentifier.charAt(i);
                if((prevChar == 46 || i == applicationIdentifier.length() - 1) && (c == 45 || c == 95 || c == 46)) {
                    return false;
                }

                if(c == 46) {
                    if(prevChar == 45 || prevChar == 95 || prevChar == 46 || i < 2) {
                        return false;
                    }

                    ++fullStopCount;
                }

                if(fullStopCount == 0 && !Character.isLetter(c)) {
                    return false;
                }

                if(!Character.isLetterOrDigit(c) && c != 45 && c != 95 && c != 46) {
                    return false;
                }

                prevChar = c;
            }

            if(fullStopCount < 2) {
                return false;
            } else {
                return true;
            }
        }
    }

    private class ScanItemClickListener implements OnMyoClickedListener {
        private ScanItemClickListener() {
        }

        public void onMyoClicked(Myo myo) {
            // TODO: 2016/12/6 反编译
//            switch(Hub.SyntheticClass_1.$SwitchMap$com$thalmic$myo$Myo$ConnectionState[myo.getConnectionState().ordinal()]) {
//                case 1:
//                case 2:
//                    Hub.this.disconnectFromScannedMyo(myo.getMacAddress());
//                    break;
//                case 3:
//                    Hub.this.connectToScannedMyo(myo.getMacAddress());
//            }
            Log.d("test1", "onMyoClicked: before");
            switch(myo.getConnectionState()) {
                case CONNECTED:
                    Hub.this.disconnectFromScannedMyo(myo.getMacAddress());
                    Log.d("test1", "onMyoClicked: disconnectFromScannedMyo");
                    break;
                case DISCONNECTED:
                    Log.d("test1", "onMyoClicked: connectToScannedMyo");
                    Hub.this.connectToScannedMyo(myo.getMacAddress());
            }
            Log.d("test1", "onMyoClicked: after");

        }
    }

    private static class InstanceHolder {
        private static final Hub INSTANCE = new Hub();

        private InstanceHolder() {
        }
    }

    public static enum LockingPolicy {
        NONE,
        STANDARD;

        private LockingPolicy() {
        }
    }
}
