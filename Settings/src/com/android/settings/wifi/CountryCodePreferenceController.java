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

public class CountryCodePreferenceController extends AbstractPreferenceController 
    implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
    
    private static final String TAG = "CountryCodePreferenceController";

    private static final String KEY_COUNTRY_CODE = "country_code";
    private static final String COUNTRY_CODE_PROPERTY = "persist.sys.country_code";
    
    private Context mContext;
    private WifiManager mWifiManager;

    private String mCountryCodeTitles[];
    private String mCountryCodeValues[];
    
    public CountryCodePreferenceController(Context context, WifiManager wifiManager) {
        super(context);
        mContext = context;
        mWifiManager = wifiManager;

        mCountryCodeTitles = mContext.getResources().getStringArray(R.array.country_code_titles);
        mCountryCodeValues = mContext.getResources().getStringArray(R.array.country_code_values);

    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_COUNTRY_CODE;
    }
    
    @Override
    public void updateState(Preference preference) {

        String value = SystemProperties.get(COUNTRY_CODE_PROPERTY);
        Log.e(TAG+"updateState","systemproperty country_code_property="+value);
        if(value == null || value.equals("")){
            value = mContext.getResources().getString(R.string.config_default_country_code);
        }

        ((ListPreference)preference).setValue(value);

        for(int i = 0; i < mCountryCodeValues.length; i++){
            if(value.equals(mCountryCodeValues[i])){
                preference.setSummary(mCountryCodeTitles[i]);
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

            String value = SystemProperties.get(COUNTRY_CODE_PROPERTY);
            if(value == null || value.equals("")){
                value = mContext.getResources().getString(R.string.config_default_country_code);
            }
            ((ListPreference)preference).setValue(value);

            dialog.cancel();
        })
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {

            ((ListPreference)preference).setValue((String)newValue);
            try {
                SystemProperties.set(COUNTRY_CODE_PROPERTY, (String)newValue);
                mWifiManager.setCountryCode((String)newValue);
            }catch(Exception e){
                e.printStackTrace();
            }

            for(int i = 0; i < mCountryCodeValues.length; i++){
                if(((String)newValue).equals(mCountryCodeValues[i])){
                    preference.setSummary(mCountryCodeTitles[i]);
                    break;
                }
            }

            PowerManager pManager = (PowerManager)(mContext.getSystemService(Context.POWER_SERVICE));
            pManager.reboot("country_code");

        }).show();

        return true;
    }
}