package com.android.settings.fuelgauge;

import android.os.Handler;

public class BatteryHandler {

    public static final String TAG = "BatteryHandler";
    private static final int SUB_BATTERY_OK = 1;
    private static final int SUB_BATTERY_FAIL = 0;

    private static BatteryHandler mInstance;

    public Handler mHandler;

    public synchronized static BatteryHandler getInstance() {
        if (mInstance == null) {
            mInstance = new BatteryHandler();
        }
        return mInstance;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void sendSuccessMessage() {
        mHandler.sendEmptyMessage(SUB_BATTERY_OK);
    }

    public void sendFailMessage() {
        mHandler.sendEmptyMessage(SUB_BATTERY_FAIL);
    }
}
