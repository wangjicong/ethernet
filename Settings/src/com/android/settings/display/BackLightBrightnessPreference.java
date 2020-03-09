/*
 * Added by yangfeiya for JIRA 177
 */


package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class BackLightBrightnessPreference extends Preference {

    public BackLightBrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*@Override
    protected void onClick() {
		Intent intent = new Intent();
		intent.setClass(getContext(),BackLightBrightness.class)
        getContext().startActivity(intent);
    }*/
}