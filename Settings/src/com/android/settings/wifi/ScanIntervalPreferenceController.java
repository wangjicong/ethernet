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

public class ScanIntervalPreferenceController extends AbstractPreferenceController 
		implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
	
	private static final String TAG = "ScanIntervalPreferenceController";

	private static final String KEY_SCAN_INTERVAL = "scan_interval";
	
	private Context mContext;
	private WifiManager mWifiManager;

	private String mScanIntervalTitles[];
	private String mScanIntervalValues[];
	
	public ScanIntervalPreferenceController(Context context, WifiManager wifiManager) {
		super(context);
		mContext = context;
		mWifiManager = wifiManager;

		mScanIntervalTitles = mContext.getResources().getStringArray(R.array.scan_interval_entries);
		mScanIntervalValues = mContext.getResources().getStringArray(R.array.scan_interval_values);

	}
	
	@Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SCAN_INTERVAL;
    }
	
	@Override
    public void updateState(Preference preference) {

		String value = String.valueOf(mWifiManager.getScanInterval());

		((ListPreference)preference).setValue(value);

		for(int i = 0; i < mScanIntervalValues.length; i++){
			if(value.equals(mScanIntervalValues[i])){
				preference.setSummary(mScanIntervalTitles[i]);
				break;
			}
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		((ListPreference)preference).setValue((String)newValue);
		try {
			mWifiManager.setScanInterval(Integer.parseInt((String)newValue));
		}catch(Exception e){
			e.printStackTrace();
		}
		for(int i = 0; i < mScanIntervalValues.length; i++){
			if(((String)newValue).equals(mScanIntervalValues[i])){
				preference.setSummary(mScanIntervalTitles[i]);
				break;
			}
		}

		return true;
	}
}