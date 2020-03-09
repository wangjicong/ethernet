package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import android.net.wifi.WifiManager;

public class HeadsetPlaybackModePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener{
    
    private static final String TAG = "HeadsetPlaybackModePreferenceController";

    private static final String PROP_VOICE_BOOST_ENABLE = "persist.sys.headset.playback.boost";

    private static final String KEY_VOICE_MODE = "headset_playback_mode";

    private static final String HEADSET_PLAYBACK_BOOST = "headset_playback_boost";

    private AudioManager mAudioManager;

    private String mVoiceModeEntries[];
    
    public HeadsetPlaybackModePreferenceController(Context context) {
        super(context);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mVoiceModeEntries = context.getResources().getStringArray(R.array.headset_playback_mode_entries);
    }
    
    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_VOICE_MODE;
    }
    
    @Override
    public void updateState(Preference preference) {
        String value = SystemProperties.get(PROP_VOICE_BOOST_ENABLE);
        ((ListPreference)preference).setValue(value);
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