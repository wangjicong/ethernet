/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.DisplaySettings;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;
import com.android.settings.R;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;


public class AutoBrightnessPreferenceController extends TogglePreferenceController {

	private SensorManager mSensorManager;
    private Sensor mSensorLight;
	private Intent intent2;
    private final String SYSTEM_KEY = SCREEN_BRIGHTNESS_MODE;
    private final int DEFAULT_VALUE = SCREEN_BRIGHTNESS_MODE_MANUAL;
	private float mGamaData;
	private float lastGamaData;
	private Context mContext;
	
    public AutoBrightnessPreferenceController(Context context, String key) {
        super(context, key);
		//MEIG@jiangdanyang auto brightness 20190711 --- start
		mContext = context;
		/*mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (mSensorManager != null)
            	mSensorManager.registerListener(mLightListener, mSensorLight, 2);*/
		//MEIG@jiangdanyang auto brightness 20190711 --- end	
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                SYSTEM_KEY, DEFAULT_VALUE) != DEFAULT_VALUE;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.System.putInt(mContext.getContentResolver(), SYSTEM_KEY,
                isChecked ? SCREEN_BRIGHTNESS_MODE_AUTOMATIC : DEFAULT_VALUE);
		
		//MEIG@jiangdanyang auto brightness 20190711 --- start
		/*if (isChecked){
        	if (mGamaData > (lastGamaData + 1000) || mGamaData < (lastGamaData - 1000)){
                Log.i("William","mGamaData == " + mGamaData);
                lastGamaData = mGamaData;
				intent2 = new Intent();
				//intent2.setAction("com.android.auto.brightness");
				intent2.setAction(Intent.ACTION_SHOW_KEYBOARD_SHORTCUTS);
				intent2.putExtra("gamadata", mGamaData);
				mContext.sendBroadcast(intent2);
            }
		} else {
			if (mSensorManager != null) 
				mSensorManager.unregisterListener(mLightListener);
		}*/
		//MEIG@jiangdanyang auto brightness 20190711 --- end
        return true;
    }

    @Override
    @AvailabilityStatus
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "auto_brightness");
    }

    @Override
    public ResultPayload getResultPayload() {
        // TODO remove result payload
        final Intent intent = DatabaseIndexingUtils.buildSearchResultPageIntent(mContext,
                DisplaySettings.class.getName(), getPreferenceKey(),
                mContext.getString(R.string.display_settings));

        return new InlineSwitchPayload(SYSTEM_KEY,
                ResultPayload.SettingsSource.SYSTEM, SCREEN_BRIGHTNESS_MODE_AUTOMATIC, intent,
                isAvailable(), DEFAULT_VALUE);
    }
	
	//MEIG@jiangdanyang auto brightness 20190711 --- start
	/*private SensorEventListener mLightListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_LIGHT) {
                return;
            }
            mGamaData = (float)event.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };*/
	//MEIG@jiangdanyang auto brightness 20190711 --- end
}