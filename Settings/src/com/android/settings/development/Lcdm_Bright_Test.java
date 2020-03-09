/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.development;

import android.provider.Settings;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.settings.R;

public class Lcdm_Bright_Test extends Activity {
    private SeekBar seekbar = null;
    private TextView light = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_lcdm_test);
        light = findViewById(R.id.light);
        seekbar = findViewById(R.id.seekbar);
        seekbar.setMax(100);
        seekbar.setOnSeekBarChangeListener(new SeekBarChangeListenerImp());
    }

    public class SeekBarChangeListenerImp implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            int cur = seekBar.getProgress();
            Lcdm_Bright_Test.this.setScreenBrightness(Lcdm_Bright_Test.this,cur * 255 / 100);
            Lcdm_Bright_Test.this.light.setText(getResources().getString(R.string.current_brightness)+":" + cur * 255 / 100);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    //set lcd brightness
     public static void setScreenBrightness(Activity activity, int value) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.screenBrightness = value / 255f;
        activity.getWindow().setAttributes(params);
    }
}
