/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.datetime;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.os.SystemProperties;
import com.android.settings.R;
import android.app.Dialog;
import android.provider.Settings;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.content.DialogInterface;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import android.util.Log;
import com.android.settings.datetime.SntpClient;
import java.util.Date;
import android.os.SystemClock;

public class NtpServerPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_NTP_SERVER = "ntp_server";
    private Context mContext;
    Preference pref;

    private void showntpDialog(Preference preference) {
        final EditText editText = new EditText(mContext);
        String ntpnull = Settings.Global.getString(
                mContext.getContentResolver(), Settings.Global.NTP_SERVER);
        editText.setText(ntpnull);
        editText.setSingleLine(true);
        AlertDialog.Builder ntpinputDialog =new AlertDialog.Builder(mContext);
        ntpinputDialog.setTitle("NTP server address setting").setView(editText);
        ntpinputDialog.setNegativeButton("CANCLE", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        ntpinputDialog.setPositiveButton("OK", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ntpstring = editText.getText().toString();
                Settings.Global.putString(mContext.getContentResolver(), Settings.Global.NTP_SERVER,
                    ntpstring);
                pref.setSummary(getSumary());
				// add by maohaojie on 2018.10.31 for bug 19927
				new Thread(new MyRunnable()).start();
				
            }
        });
        ntpinputDialog.show();
        }

    public NtpServerPreferenceController(Context context) {
        super(context);
        mContext = context;
    }
	
	

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        pref = screen.findPreference(KEY_NTP_SERVER);
        pref.setSummary(getSumary());
    }

    @Override
    public String getPreferenceKey() {
        return KEY_NTP_SERVER;
    }
    
    private String getSumary(){
        String ntpSumary = Settings.Global.getString(
            mContext.getContentResolver(), Settings.Global.NTP_SERVER);
        if(ntpSumary == null || ntpSumary.equals(""))
            ntpSumary="asia.pool.ntp.org";
        return ntpSumary;
    }


    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_NTP_SERVER)) {
            return false;
        }
        showntpDialog(preference);
        return true;
    }
      // add by maohaojie on 2018.10.31 for bug 19927
	 public class MyRunnable implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            final Resources res = mContext.getResources();
            String mm=Settings.Global.getString(
                mContext.getContentResolver(), Settings.Global.NTP_SERVER);
            final long defaultTimeout = res.getInteger(
                    com.android.internal.R.integer.config_ntpTimeout);
            final long mTimeout = Settings.Global.getLong(
                    mContext.getContentResolver(), Settings.Global.NTP_TIMEOUT, defaultTimeout);
            SntpClient client = new SntpClient();
            boolean ntp_boo=client.requestTime(mm, (int) mTimeout);
			if(ntp_boo){
				 long now = client.getNtpTime();
                 SystemClock.setCurrentTimeMillis(now);
           
				
			}
        }
    }
	
	
	
	
}

