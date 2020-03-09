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

public class RoamTriggerPreferenceController extends AbstractPreferenceController 
		implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
	
	private static final String TAG = "RoamTriggerPreferenceController";

	private static final String KEY_ROAM_TRIGGER = "roam_trigger";
	
	private Context mContext;
	private WifiManager mWifiManager;

	private String mRoamTriggerTitles[];
	private String mRoamTriggerValues[];
	
	public RoamTriggerPreferenceController(Context context, WifiManager wifiManager) {
		super(context);
		mContext = context;
		mWifiManager = wifiManager;

		mRoamTriggerTitles = mContext.getResources().getStringArray(R.array.roam_trigger_entries);
		mRoamTriggerValues = mContext.getResources().getStringArray(R.array.roam_trigger_values);

	}
	
	@Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ROAM_TRIGGER;
    }
	
	@Override
    public void updateState(Preference preference) {

		String value = String.valueOf(mWifiManager.getRoamTrigger());

		((ListPreference)preference).setValue(value);

		for(int i = 0; i < mRoamTriggerValues.length; i++){
			if(value.equals(mRoamTriggerValues[i])){
				preference.setSummary(mRoamTriggerTitles[i]);
				break;
			}
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		((ListPreference)preference).setValue((String)newValue);
		try {
			mWifiManager.setRoamTrigger(Integer.parseInt((String)newValue), true);
		}catch(Exception e){
			e.printStackTrace();
		}
		for(int i = 0; i < mRoamTriggerValues.length; i++){
			if(((String)newValue).equals(mRoamTriggerValues[i])){
				preference.setSummary(mRoamTriggerTitles[i]);
				break;
			}
		}

		return true;
	}
}