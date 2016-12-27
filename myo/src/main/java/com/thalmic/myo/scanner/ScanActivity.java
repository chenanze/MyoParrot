package com.thalmic.myo.scanner;

import android.app.Activity;
import android.os.Bundle;
import chenanze.com.myo.R;

public class ScanActivity
        extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        int width = getResources().getDimensionPixelSize(R.dimen.myosdk__fragment_scan_window_width);
        int height = getResources().getDimensionPixelSize(R.dimen.myosdk__fragment_scan_window_height);
        if ((width > 0) && (height > 0)) {
            getWindow().setLayout(width, height);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myosdk__activity_scan);

        getActionBar().setDisplayOptions(0, 2);
    }
}
