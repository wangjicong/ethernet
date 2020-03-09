/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fuelgauge;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.Handler;
import android.util.Log;

import static com.android.settings.fuelgauge.BatteryBroadcastReceiver.BatteryUpdateType;

public class BatterySaverReceiver extends BroadcastReceiver {
    private static final String TAG = "BatterySaverReceiver";
    private static final boolean DEBUG = false;
    private boolean mRegistered;
    private Context mContext;
    private BatterySaverListener mBatterySaverListener;
    private static final String NODE_SUB_BATTERY_CHARGE_STATUS  = "/sys/class/power_supply/battery/sub_voltage_chg";
    private static final String SUB_BATTERY_NOT_CHARGEING = "0";
    private static final String SUB_BATTERY_CHARGING = "1";

    public BatterySaverReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "Received " + intent.getAction());
        String action = intent.getAction();
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGING.equals(action)) {
            if (mBatterySaverListener != null) {
                mBatterySaverListener.onPowerSaveModeChanged();
            }
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            // disable BSM switch if phone is plugged in
            if (mBatterySaverListener != null) {
                final boolean pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
                //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ----- start
                Log.i(TAG,"ACTION_BATTERY_CHANGED pluggedIn == " + pluggedIn);
                mBatterySaverListener.onBatteryChanged(pluggedIn);
                //modify by wangxing for bug P_RK95_E-452 start
               /* if (pluggedIn){
                    Log.i(TAG,"pluggedIn pluggedIn pluggedIn == ");
                    writeFile(NODE_SUB_BATTERY_CHARGE_STATUS, SUB_BATTERY_CHARGING);
                    //BatteryHandler.getInstance().sendSuccessMessage();//fixed bugE435
                } else {
                    writeFile(NODE_SUB_BATTERY_CHARGE_STATUS, SUB_BATTERY_NOT_CHARGEING);
                    //BatteryHandler.getInstance().sendFailMessage();//fixed bugE435
                }*/
                //modify by wangxing for bug P_RK95_E-452 end
                //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ----- end
            }
        }
    }

    //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ----- start
    private static void writeFile(String filePath, String status){
        try {
            BufferedWriter bfw = new BufferedWriter(new FileWriter(filePath));
            bfw.write(status);
            bfw.flush();
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ----- end

    public void setListening(boolean listening) {
        if (listening && !mRegistered) {
            final IntentFilter ifilter = new IntentFilter();
            ifilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
            ifilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            mContext.registerReceiver(this, ifilter);
            mRegistered = true;
        } else if (!listening && mRegistered) {
            mContext.unregisterReceiver(this);
            mRegistered = false;
        }
    }

    public void setBatterySaverListener(BatterySaverListener lsn) {
        mBatterySaverListener = lsn;
    }

    public interface BatterySaverListener {
        void onPowerSaveModeChanged();
        void onBatteryChanged(boolean pluggedIn);
    }
}
