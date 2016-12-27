//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.thalmic.myo.scanner;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thalmic.myo.Myo;
import com.thalmic.myo.Myo.ConnectionState;
import com.thalmic.myo.scanner.Scanner.ScanListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import chenanze.com.myo.R;

class MyoDeviceListAdapter extends BaseAdapter implements ScanListAdapter {
    private ArrayList<MyoDeviceListAdapter.Item> mItems = new ArrayList();
    private MyoDeviceListAdapter.RssiComparator mComparator = new MyoDeviceListAdapter.RssiComparator();

    public MyoDeviceListAdapter() {
    }

    public void addDevice(Myo myo, int rssi) {
        if(myo == null) {
            throw new IllegalArgumentException("Myo cannot be null.");
        } else {
            MyoDeviceListAdapter.Item item = this.getItem(myo);
            if(item != null) {
                item.rssi = rssi;
            } else {
                item = new MyoDeviceListAdapter.Item(myo, rssi);
                this.mItems.add(item);
            }

            this.notifyDataSetChanged();
        }
    }

    public Myo getMyo(int position) {
        return ((MyoDeviceListAdapter.Item)this.mItems.get(position)).myo;
    }

    public void clear() {
        this.mItems.clear();
        this.notifyDataSetChanged();
    }

    public void notifyDeviceChanged() {
        this.notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        this.sortByRssi();
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return this.mItems.size();
    }

    public Object getItem(int i) {
        return this.mItems.get(i);
    }

    public long getItemId(int i) {
        return (long)i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        MyoDeviceListAdapter.ViewHolder viewHolder;
        if(view == null) {
            view = View.inflate(context, R.layout.myosdk__device_list_item, (ViewGroup)null);
            viewHolder = new MyoDeviceListAdapter.ViewHolder();
            viewHolder.deviceName = (TextView)view.findViewById(R.id.myosdk__device_name_tv);
            viewHolder.deviceVersion = (TextView)view.findViewById(R.id.myosdk__device_version_tv);
            viewHolder.requiredDeviceVersion = (TextView)view.findViewById(R.id.myosdk__required_firmware_version_text);
            viewHolder.progressBar = (ProgressBar)view.findViewById(R.id.myosdk__progress);
            viewHolder.connectionStateDot = view.findViewById(R.id.myosdk__connection_state_dot);
            view.setTag(viewHolder);
        } else {
            viewHolder = (MyoDeviceListAdapter.ViewHolder)view.getTag();
        }

        Myo myo = ((MyoDeviceListAdapter.Item)this.mItems.get(i)).myo;
        viewHolder.deviceVersion.setText("");
        viewHolder.requiredDeviceVersion.setText("");
        viewHolder.deviceVersion.setVisibility(View.GONE);
        viewHolder.requiredDeviceVersion.setVisibility(View.GONE);
        viewHolder.connectionStateDot.setVisibility(View.GONE);
        viewHolder.progressBar.setVisibility(View.GONE);
        String deviceName;
        String versionString;
        if(myo.getConnectionState() == ConnectionState.DISCONNECTED) {
            if(!myo.isFirmwareVersionSupported()) {
                viewHolder.deviceVersion.setVisibility(View.VISIBLE);
                viewHolder.requiredDeviceVersion.setVisibility(View.VISIBLE);
                viewHolder.connectionStateDot.setVisibility(View.VISIBLE);
                viewHolder.connectionStateDot.setBackgroundResource(R.drawable.myosdk__firmware_incompatible_dot);
                deviceName = myo.getFirmwareVersion().toDisplayString();
                versionString = "1.1.0";
                String versionString1 = String.format(context.getString(R.string.myosdk__firmware_version_format), new Object[]{deviceName});
                String requiredVersionString = String.format(context.getString(R.string.myosdk__firmware_required_format), new Object[]{versionString});
                viewHolder.deviceVersion.setText(versionString1);
                viewHolder.requiredDeviceVersion.setText(requiredVersionString);
            }
        } else if(myo.getConnectionState() == ConnectionState.CONNECTING) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        } else if(myo.getConnectionState() == ConnectionState.CONNECTED) {
            viewHolder.deviceVersion.setVisibility(View.VISIBLE);
            viewHolder.connectionStateDot.setVisibility(View.VISIBLE);
            viewHolder.connectionStateDot.setBackgroundResource(R.drawable.myosdk__connected_dot);
            deviceName = myo.getFirmwareVersion().toDisplayString();
            versionString = String.format(context.getString(R.string.myosdk__firmware_version_format), new Object[]{deviceName});
            viewHolder.deviceVersion.setText(versionString);
        }

        deviceName = myo.getName();
        if(TextUtils.isEmpty(deviceName)) {
            deviceName = context.getString(R.string.myosdk__unknown_myo);
        }

        viewHolder.deviceName.setText(deviceName);
        return view;
    }

    private MyoDeviceListAdapter.Item getItem(Myo myo) {
        int i = 0;

        for(int size = this.mItems.size(); i < size; ++i) {
            if(((MyoDeviceListAdapter.Item)this.mItems.get(i)).myo.equals(myo)) {
                return (MyoDeviceListAdapter.Item)this.mItems.get(i);
            }
        }

        return null;
    }

    private void sortByRssi() {
        Collections.sort(this.mItems, this.mComparator);
    }

    private static class RssiComparator implements Comparator<MyoDeviceListAdapter.Item> {
        private RssiComparator() {
        }

        public int compare(MyoDeviceListAdapter.Item lhs, MyoDeviceListAdapter.Item rhs) {
            return lhs.myo.equals(rhs.myo)?0:rhs.rssi - lhs.rssi;
        }

        public boolean equals(Object object) {
            return super.equals(object);
        }
    }

    private static class ViewHolder {
        TextView deviceName;
        TextView deviceVersion;
        TextView requiredDeviceVersion;
        ProgressBar progressBar;
        View connectionStateDot;

        private ViewHolder() {
        }
    }

    private class Item {
        final Myo myo;
        int rssi;

        public Item(Myo myo, int rssi) {
            this.myo = myo;
            this.rssi = rssi;
        }
    }
}
