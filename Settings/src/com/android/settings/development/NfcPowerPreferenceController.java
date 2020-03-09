/*
 * Copyright (C) 2018 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.development;

import android.app.UiModeManager;
import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

import android.nfc.NfcAdapter;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.Message;

public class NfcPowerPreferenceController extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    private static final String NFC_POWER_KEY = "toggle_nfc_power";

    private static final String PROP_NFC_POWER_MODE = "persist.sys.nfcpower.mode";
    private static final String FULL_MODE = "0";
    private static final String LOW_MODE = "1";
    private final int MSG_KILL_NFC_SERVICE = 0x0001;
    private final int MSG_RESTART_NFC = 0x0002;
    private final int MSG_CLOSE_NFC = 0x0004;

    private NfcAdapter mNfcAdapter;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_KILL_NFC_SERVICE:
                    break;
                case MSG_CLOSE_NFC:
                    mNfcAdapter.disable();
                    break;
                case MSG_RESTART_NFC:
                    if(mNfcAdapter.isEnabled()) {
                        mNfcAdapter.enable();
                    }
                    break;
            }
        }
    };

    public NfcPowerPreferenceController(Context context) {
        super(context);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    @Override
    public String getPreferenceKey() {
        return NFC_POWER_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        int prop = Integer.parseInt(newValue + "");
        SystemProperties.set(PROP_NFC_POWER_MODE, prop == 0 ? FULL_MODE : LOW_MODE);

        if(mNfcAdapter.isEnabled()){
            mHandler.sendMessage(mHandler.obtainMessage(MSG_CLOSE_NFC));
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RESTART_NFC), 500);
        }else{
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RESTART_NFC));
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CLOSE_NFC), 500);
        }

        CharSequence[] entries = ((ListPreference)preference).getEntries();
        int index = ((ListPreference)preference).findIndexOfValue((String)newValue);
        preference.setSummary(entries[index]);

        return true;
    }

    @Override
    public void updateState(Preference preference) {
        CharSequence[] entries = ((ListPreference)preference).getEntries();
        ((ListPreference)preference).setValue("" + SystemProperties.getInt(PROP_NFC_POWER_MODE, 0));
        preference.setSummary(entries[SystemProperties.getInt(PROP_NFC_POWER_MODE, 0)]);
    }
}
