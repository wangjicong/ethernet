package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import android.os.BatteryManager;
import android.os.SystemProperties;

public class BatteryProtectionPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_BATTERY_PROTECTION = "battery_protection";

    public BatteryProtectionPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BATTERY_PROTECTION;
    }

    @Override
    public void updateState(Preference preference) {

        boolean prop = SystemProperties.getBoolean("persist.sys.battery.protection", false);

        ((SwitchPreference) preference).setChecked(prop);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Boolean isProtectionMode = (Boolean)newValue;
        SystemProperties.set("persist.sys.battery.protection", isProtectionMode.toString());

        return true;
    }
}
