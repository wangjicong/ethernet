/*
 * Added by yangfeiya for JIRA 177
 */

package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.android.internal.app.ColorDisplayController;
import com.android.settings.core.SliderPreferenceController;
import com.android.settings.widget.SeekBarPreference;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.os.SystemProperties;
import android.util.Log;
import android.provider.Settings;

public class BacklightBrightnessLevelPreferenceController extends SliderPreferenceController {

    private int initBrightness=255;
    private static final String BACK_LIGHT_LEVEL="/sys/class/leds/aw9106_led/backlight_brightness";
    private static final String TAG= "BacklightBrightnessLevelPreferenceController";
    private Context mContext=null;
    public BacklightBrightnessLevelPreferenceController(Context context, String key) {
        super(context, key);
        mContext=context;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "backlight_brightness_level");
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        initBrightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.BACKLIGHT_BRIGHTNESS , getBackLightBrightness());
        final SeekBarPreference preference = (SeekBarPreference) screen.findPreference(
                getPreferenceKey());
        preference.setContinuousUpdates(true);
        preference.setMax(getMaxSteps());
    }

    @Override
    public final void updateState(Preference preference) {
        super.updateState(preference);
    }

    @Override
    public int getSliderPosition() {
        return initBrightness;
    }

    @Override
    public boolean setSliderPosition(int position) {
        Settings.System.putInt( mContext.getContentResolver(), Settings.System.BACKLIGHT_BRIGHTNESS , position);
        return setBackLightBrightness(position);
    }

    @Override
    public int getMaxSteps() {
        return 255;
    }


    private boolean setBackLightBrightness(int level){
        String tmp =""+level;
        try {
            BufferedWriter bufWriter = null;
            bufWriter = new BufferedWriter(new FileWriter(BACK_LIGHT_LEVEL));
            bufWriter.write(tmp);
            bufWriter.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error write: "+BACK_LIGHT_LEVEL+" Error: "+e.toString());
            return false;
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getBackLightBrightness(){
        String res="";
        try{
            FileReader fr=new FileReader(BACK_LIGHT_LEVEL);
            BufferedReader br=new BufferedReader(fr);
            String temp=null;
            while((temp=br.readLine())!=null){
                res+=temp;
                if(!res.equals("")){
                    return Integer.parseInt(res);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG,"Can not read BACK_LIGHT_LEVEL="+BACK_LIGHT_LEVEL);
            return 255;
        }
        return 255;
    }
}