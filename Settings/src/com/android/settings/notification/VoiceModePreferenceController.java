package com.android.settings.notification;

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

import android.media.AudioManager;

public class VoiceModePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
    
    private static final String TAG = "VoiceModePreferenceController";

    private static final String PROP_VOICE_BOOST_ENABLE = "persist.vendor.audio.voip.boost";

    private static final String KEY_VOICE_MODE = "voice_mode";

    private static final String HEADSET_PLAYBACK_BOOST = "headset_playback_boost";

    private AudioManager mAudioManager;
    
    private String mVoiceModeEntries[];
    private String mVoiceModeValues[];
    
    public VoiceModePreferenceController(Context context) {
        super(context);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mVoiceModeEntries = context.getResources().getStringArray(R.array.voice_mode_entries);
        mVoiceModeValues = context.getResources().getStringArray(R.array.voice_mode_values);
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_VOICE_MODE;
    }
    
    @Override
    public void updateState(Preference preference) {
        String value = SystemProperties.get(PROP_VOICE_BOOST_ENABLE);
        ((ListPreference)preference).setValue(value.equals("true") ? mVoiceModeValues[1] : mVoiceModeValues[0]);
        preference.setSummary(value.equals("true") ? mVoiceModeEntries[1] : mVoiceModeEntries[0]);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        ((ListPreference)preference).setValue((String)newValue);

        SystemProperties.set(PROP_VOICE_BOOST_ENABLE, (String)newValue);
        mAudioManager.setParameters(HEADSET_PLAYBACK_BOOST + "=" + (String)newValue);
        preference.setSummary(((String)newValue).equals("true") ? mVoiceModeEntries[1] : mVoiceModeEntries[0]);

        return true;
    }
}