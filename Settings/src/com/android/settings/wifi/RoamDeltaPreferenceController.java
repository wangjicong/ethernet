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

public class RoamDeltaPreferenceController extends AbstractPreferenceController 
		implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
	
	private static final String TAG = "RoamDeltaPreferenceController";

	private static final String KEY_ROAM_DELTA = "roam_delta";
	
	private Context mContext;
	private WifiManager mWifiManager;

	private String mRoamDeltaTitles[];
	private String mRoamDeltaValues[];
	
	public RoamDeltaPreferenceController(Context context, WifiManager wifiManager) {
		super(context);
		mContext = context;
		mWifiManager = wifiManager;

		mRoamDeltaTitles = mContext.getResources().getStringArray(R.array.roam_delta_entries);
		mRoamDeltaValues = mContext.getResources().getStringArray(R.array.roam_delta_values);

	}
	
	@Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ROAM_DELTA;
    }
	
	@Override
    public void updateState(Preference preference) {

		String value = String.valueOf(mWifiManager.getRoamDelta());

		((ListPreference)preference).setValue(value);

		for(int i = 0; i < mRoamDeltaValues.length; i++){
			if(value.equals(mRoamDeltaValues[i])){
				preference.setSummary(mRoamDeltaTitles[i]);
				break;
			}
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		((ListPreference)preference).setValue((String)newValue);
		try {
			mWifiManager.setRoamDelta(Integer.parseInt((String)newValue), true);
		}catch(Exception e){
			e.printStackTrace();
		}
		for(int i = 0; i < mRoamDeltaValues.length; i++){
			if(((String)newValue).equals(mRoamDeltaValues[i])){
				preference.setSummary(mRoamDeltaTitles[i]);
				break;
			}
		}

		return true;
	}
}