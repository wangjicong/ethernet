package com.android.settings.wifi;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import android.net.wifi.WifiManager;

import android.util.Log;

/**
 * A controller to manage the switch for showing battery percentage in the status bar.
 */

public class RoamEnablePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {


    private static final String KEY_ROAM_ENABLE = "roam_enable";
    private static final String KEY_ROAM_TRIGGER = "roam_trigger";
    private static final String KEY_ROAM_DELTA = "roam_delta";

    private Context mContext;
    private WifiManager mWifiManager;

    private ListPreference mRoamTriggerPreference;
    private ListPreference mRoamDeltaPreference;


    public RoamEnablePreferenceController(Context context, WifiManager wifiManager) {
        super(context);
        mContext = context;
        mWifiManager = wifiManager;

    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        mRoamTriggerPreference = (ListPreference)screen.findPreference(KEY_ROAM_TRIGGER);
        mRoamDeltaPreference = (ListPreference)screen.findPreference(KEY_ROAM_DELTA);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ROAM_ENABLE;
    }

    @Override
    public void updateState(Preference preference) {

        boolean roamEnalbe = mWifiManager.getRoamMode();

        ((SwitchPreference) preference).setChecked(roamEnalbe);
        mRoamTriggerPreference.setEnabled(roamEnalbe);
        mRoamDeltaPreference.setEnabled(roamEnalbe);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {


        boolean roamEnalbe = (boolean)newValue;
        
        mWifiManager.setRoamMode(roamEnalbe, true);
        ((SwitchPreference) preference).setChecked(roamEnalbe);
        mRoamTriggerPreference.setEnabled(roamEnalbe);
        mRoamDeltaPreference.setEnabled(roamEnalbe);

        return true;
    }
}