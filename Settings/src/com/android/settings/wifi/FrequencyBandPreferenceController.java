package com.android.settings.wifi;

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

import android.os.PowerManager;

public class FrequencyBandPreferenceController extends AbstractPreferenceController 
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
    
    private static final String TAG = "FrequencyBandPreferenceController";

    private static final String KEY_FREQUENCY_BAND = "frequency_band";
    
    private Context mContext;
    private WifiManager mWifiManager;

    private String mFrequencyBandTitles[];
    private String mFrequencyBandValues[];
    
    public FrequencyBandPreferenceController(Context context, WifiManager wifiManager) {
        super(context);
        mContext = context;
        mWifiManager = wifiManager;

        mFrequencyBandTitles = mContext.getResources().getStringArray(R.array.frequency_band_entries);
        mFrequencyBandValues = mContext.getResources().getStringArray(R.array.frequency_band_values);

    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_FREQUENCY_BAND;
    }
    
    @Override
    public void updateState(Preference preference) {

        String value = String.valueOf(mWifiManager.getFrequencyBand());

        ((ListPreference)preference).setValue(value);

        for(int i = 0; i < mFrequencyBandValues.length; i++){
            if(value.equals(mFrequencyBandValues[i])){
                preference.setSummary(mFrequencyBandTitles[i]);
                break;
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        new AlertDialog.Builder(mContext)
        .setTitle(R.string.restart)
        .setMessage(R.string.restart_to_take_effect_message)
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            ((ListPreference)preference).setValue(String.valueOf(mWifiManager.getFrequencyBand()));
            dialog.cancel();
        })
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            try {
                mWifiManager.setFrequencyBand(Integer.parseInt((String)newValue), true);
            }catch(Exception e){
                e.printStackTrace();
            }
            PowerManager pManager=(PowerManager) (mContext.getSystemService(Context.POWER_SERVICE));
            pManager.reboot("frequency_band");
        }).show();

        return true;
    }
}