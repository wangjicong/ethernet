package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import android.net.wifi.WifiManager;

public class DeviceIdPreferenceController extends AbstractPreferenceController{
    
    private static final String PROP_DEVICE_ID = "sys.device.id";

    private static final String KEY_DEVICE_ID = "device_id";
    
    public DeviceIdPreferenceController(Context context) {
        super(context);
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_DEVICE_ID;
    }
    
    @Override
    public void updateState(Preference preference) {
        preference.setSummary(SystemProperties.get(PROP_DEVICE_ID));
    }
}