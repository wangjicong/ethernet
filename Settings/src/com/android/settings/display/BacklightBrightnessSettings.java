/*
 * Added by yangfeiya for JIRA 177
 */

package com.android.settings.display;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;

import com.android.internal.app.ColorDisplayController;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class BacklightBrightnessSettings extends DashboardFragment {

    private static final String TAG = "BacklightBrightnessSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getContext();
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.backlight_brightness_settings;
    }


    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BACKLIGHT_BRIGHTNESS_SETTINGS;
    }

}
