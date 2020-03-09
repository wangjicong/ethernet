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

package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import com.android.settings.R;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settings.wifi.WifiLogConfig;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.app.Dialog;

public class WifiLogEnablePreferenceController extends
        DeveloperOptionsPreferenceController implements
        Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    private static final String WIFI_LOG_ENABLED_KEY = "wifi_log_enabled";

    private WifiLogConfig mWifiLogConfig;

    public WifiLogEnablePreferenceController(Context context) {
        super(context);
        mWifiLogConfig = new WifiLogConfig(context);
    }

    @Override
    public String getPreferenceKey() {
        return WIFI_LOG_ENABLED_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean isEnabled = (Boolean) newValue;
        new AlertDialog.Builder(mContext)
            .setTitle(R.string.wifi_log_settings)
            .setMessage(R.string.wifi_log_settings_enable)
            .setPositiveButton(R.string.wifi_log_cancel, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                     ((SwitchPreference) mPreference).setChecked(!isEnabled);
                 }
            }).setNegativeButton(R.string.wifi_log_restart, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mWifiLogConfig.setConf(((SwitchPreference) mPreference).isChecked());
                    mWifiLogConfig.setValue(mWifiLogConfig.WIFILOG_KEY,((SwitchPreference) mPreference).isChecked()?1:0);
                    Thread thr = new Thread() {
                        @Override
                        public void run() {
                            IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                            try {
                                Thread.sleep(1000);
                                pm.reboot(false, null, false);    
                            }catch (RemoteException e) {
                            }catch(InterruptedException ex){
                            }
                        }	
                    };
       			        thr.start();
                }
            }).create().show(); 
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        boolean enabled = (mWifiLogConfig.getIntValue(mWifiLogConfig.WIFILOG_KEY))==1;
        ((SwitchPreference) mPreference).setChecked(enabled);
    }

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        mWifiLogConfig.setConf(false);
        mWifiLogConfig.setValue(mWifiLogConfig.WIFILOG_KEY,0); 
        ((SwitchPreference) mPreference).setChecked(false);
    }
}
