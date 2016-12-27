package com.thalmic.myo;

import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.thalmic.myo.internal.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Reporter {
    private static final String TAG = "Reporter";
    public static final String EVENT_NAME_ATTACHED_MYO = "AttachedMyo";
    public static final String EVENT_NAME_DETACHED_MYO = "DetachedMyo";
    public static final String EVENT_NAME_SYNCED_MYO = "SyncedMyo";
    public static final String EVENT_NAME_UNSYNCED_MYO = "UnsyncedMyo";
    private static final String EVENT_URL = "http://devices.thalmic.com/event";
    private static final String MYOSDK_PLATFORM;
    private static final String MYOSDK_VERSION = "0.10.0";
    private static final String REPORTING_MAC_ADDRESS_KEY = "macAddress";
    private static final String REPORTING_PLATFORM_KEY = "softwarePlatform";
    private static final String REPORTING_SDK_VERSION_KEY = "softwareVersion";
    private static final String REPORTING_APP_ID_KEY = "appId";
    private static final String REPORTING_UUID_KEY = "uuid";
    private static final String REPORTING_DATA_KEY = "data";
    private static final String REPORTING_EVENT_NAME_KEY = "eventName";
    private static final String REPORTING_TIMESTAMP_KEY = "timestamp";
    private static final String REPORTING_FIRMWARE_VERSION_KEY = "firmwareVersion";
    private ExecutorService mExecutor;
    private NetworkUtil mUtil;
    private boolean mSendUsageData;

    public Reporter() {
        this(new NetworkUtil());
    }

    public Reporter(NetworkUtil util) {
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.mSendUsageData = true;
        this.mUtil = util;
    }

    public void setSendUsageData(boolean sendUsageData) {
        this.mSendUsageData = sendUsageData;
    }

    public boolean isSendingUsageData() {
        return this.mSendUsageData;
    }

    public void sendMyoEvent(final String appId, final String uuid, final String eventName, final Myo myo) {
        if(this.mSendUsageData) {
            if(myo != null && !TextUtils.isEmpty(myo.getMacAddress())) {
                final long timestamp = System.currentTimeMillis() * 1000L;
                (new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            JSONObject e = Reporter.buildEventJsonObject(appId, uuid, eventName, myo, timestamp);
                            return Boolean.valueOf(Reporter.this.sendJsonPostRequest(e.toString(), "http://devices.thalmic.com/event"));
                        } catch (Exception var3) {
                            Log.e("Reporter", "Exception in sending event:" + var3.toString());
                            return Boolean.valueOf(false);
                        }
                    }
                }).executeOnExecutor(this.mExecutor, new Void[0]);
            } else {
                Log.e("Reporter", "Could not send Myo event. Invalid Myo.");
            }
        }
    }

    private boolean sendJsonPostRequest(String jsonString, String urlString) throws IOException {
        int responseCode = this.mUtil.postJsonToUrl(jsonString, urlString);
        boolean success = responseCode == 200;
        if(!success) {
            Log.e("Reporter", "Unsuccessful sending post request to " + urlString + ". Received non-200 " + "(" + responseCode + ") response code from server.");
        }

        return success;
    }

    private static JSONObject buildEventJsonObject(String appId, String uuid, String eventName, Myo myo, long timestamp) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("appId", appId);
        jo.put("softwarePlatform", MYOSDK_PLATFORM);
        jo.put("softwareVersion", "0.10.0");
        jo.put("uuid", uuid);
        JSONArray eventArray = new JSONArray();
        jo.put("data", eventArray);
        JSONObject event = new JSONObject();
        event.put("eventName", eventName);
        event.put("timestamp", timestamp);
        eventArray.put(event);
        JSONObject eventData = new JSONObject();
        eventData.put("macAddress", macAddressForReporting(myo.getMacAddress()));
        eventData.put("firmwareVersion", firmwareVersionForReporting(myo.getFirmwareVersion()));
        event.put("data", eventData);
        return jo;
    }

    private static String macAddressForReporting(String address) {
        return address.toLowerCase().replace(':', '-');
    }

    private static String firmwareVersionForReporting(FirmwareVersion v) {
        return "" + v.major + "." + v.minor + "." + v.patch + "." + v.hardwareRev;
    }

    static {
        MYOSDK_PLATFORM = "Android_" + VERSION.RELEASE;
    }
}
