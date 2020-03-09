package com.android.settings.notification;

import static com.android.settings.notification.SettingPref.TYPE_SYSTEM;

import android.content.Context;

import android.provider.Settings.System;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class KeypadSoundPreferenceController extends SettingPrefController {

    private static final String KEY_KEYPAD_SOUNDS = "keypad_sounds";

    public KeypadSoundPreferenceController(Context context, SettingsPreferenceFragment parent,
            Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        mPreference = new SettingPref(
            TYPE_SYSTEM, KEY_KEYPAD_SOUNDS, System.KEYPAD_SOUND_EFFECTS_ENABLED, 0);
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_show_keypad_sounds);
    }
}