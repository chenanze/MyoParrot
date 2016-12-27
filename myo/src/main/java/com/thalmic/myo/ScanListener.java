package com.thalmic.myo;

import com.thalmic.myo.internal.ble.Address;
import com.thalmic.myo.scanner.Scanner;

class ScanListener
        implements Scanner.OnScanningStartedListener, Scanner.OnMyoScannedListener
{
    private static final String TAG = "ScanListener";
    private static final int MINIMUM_ADJACENT_RSSI = -39;
    private static final long ADJACENT_SCAN_INTERVAL = 500L;
    private final Hub mHub;
    private AttachMode mAttachMode = AttachMode.NONE;
    private int mAttachCount;
    private Address mTargetAddress;

    public ScanListener(Hub hub)
    {
        this.mHub = hub;
    }

    private Scanner getScanner()
    {
        return this.mHub.getScanner();
    }

    public void attachToAdjacent(int count)
    {
        if (count == 0) {
            throw new IllegalArgumentException("Attach count must be greater than 0");
        }
        this.mAttachMode = AttachMode.ADJACENT;
        this.mAttachCount = count;

        getScanner().startScanning(0L, 500L);
    }

    public void attachByMacAddress(String targetAddress)
    {
        this.mAttachMode = AttachMode.ADDRESS;
        this.mAttachCount = 1;
        this.mTargetAddress = new Address(targetAddress);

        getScanner().startScanning(0L);
    }

    public void onScanningStarted()
    {
        Scanner.ScanListAdapter adapter = this.mHub.getScanner().getScanListAdapter();
        for (Myo myo : this.mHub.getKnownDevices()) {
            switch (myo.getConnectionState())
            {
                case CONNECTED:
                case CONNECTING:
                    adapter.addDevice(myo, 0);
            }
        }
    }

    public void onScanningStopped()
    {
        this.mAttachMode = AttachMode.NONE;
    }

    public Myo onMyoScanned(Address address, String name, int rssi)
    {
        Myo myo = this.mHub.addKnownDevice(address);
        myo.setName(name);
        if ((this.mAttachMode != AttachMode.NONE) && (myo.getConnectionState() == Myo.ConnectionState.DISCONNECTED)) {
            if (shouldAttach(address, rssi))
            {
                this.mAttachCount -= 1;
                if ((this.mAttachCount == 0) && (getScanner().isScanning())) {
                    getScanner().stopScanning();
                }
                this.mHub.connectToScannedMyo(address.toString());
            }
        }
        return myo;
    }

    private boolean shouldAttach(Address address, int rssi)
    {
        switch (this.mAttachMode)
        {
            case ADJACENT:
                return rssi >= -39;
            case ADDRESS:
                return address.equals(this.mTargetAddress);
        }
        return false;
    }

    private static enum AttachMode
    {
        NONE,  ADJACENT,  ADDRESS;

        private AttachMode() {}
    }
}
