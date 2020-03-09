package com.android.settings.display;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.os.UserManager.DISALLOW_SET_WALLPAPER;

import java.util.List;

public class TouchScreenCalibrationPreferenceController extends AbstractPreferenceController 
        implements PreferenceControllerMixin {
    
    private static final String TAG = "TouchScreenCalibrationPreferenceController";
    private Context mContext;
    public static final String KEY_TOUCH_CALIBRATION = "touch_screen_calibration";
    private static final String COMMAND_PATH = "/sys/devices/soc/c178000.i2c/i2c-4/4-0024/command";
    private static final String STATUS_PATH = "/sys/devices/soc/c178000.i2c/i2c-4/4-0024/status";
    private String mTouchScreenCalibrationKey;
    /**calibration test
    1.Suspend scanning , send 04 00 05 00 2F 00 03 to COMMAND_PATH, get STATUS_PATH 0 or 1, sleep 1s
    2.mutual-cap Sensors IDAC, send 04 00 06 00 2F 00 28 00 to COMMAND_PATH, get STATUS_PATH 0 or 1, sleep 1s
    3.mutual-cap Button IDAC,send 04 00 06 00 2F 00 28 01 to COMMAND_PATH, get STATUS_PATH 0 or 1, sleep 1s
    4.self-cap Sensors IDAC,send 04 00 06 00 2F 00 28 02 to COMMAND_PATH, get STATUS_PATH 0 or 1, sleep 1s
    5.initialize baseline,send 04 00 06 00 2F 00 29 03 to COMMAND_PATH, get STATUS_PATH 0 or 1, sleep 1s
    6.Resume scanning,send 04 00 05 00 2F 00 04 to COMMAND_PATH, get STATUS_PATH 0 or 1, sleep 1s
    */
    private static final String command_suspend         = "04 00 05 00 2F 00 03";
    private static final String command_mutual_sensors  = "04 00 06 00 2F 00 28 00";
    private static final String command_mutual_button   = "04 00 06 00 2F 00 28 01";
    private static final String command_self_sensors    = "04 00 06 00 2F 00 28 02";
    private static final String command_init_baseline   = "04 00 06 00 2F 00 29 03";
    private static final String command_resume          = "04 00 05 00 2F 00 04";
    private static final String sleep                   = "sleep 1";
    
    private final String mWallpaperPackage;
    private final String mWallpaperClass;
    
    
    public TouchScreenCalibrationPreferenceController(Context context, String key) {
        super(context);
        mContext = context;
        mTouchScreenCalibrationKey = key;
        mWallpaperPackage = mContext.getString(R.string.config_wallpaper_picker_package);
        mWallpaperClass = mContext.getString(R.string.config_wallpaper_picker_class);
    }
    
    @Override
    public boolean isAvailable() {
        /**if (TextUtils.isEmpty(mWallpaperPackage) || TextUtils.isEmpty(mWallpaperClass)) {
            Log.e(TAG, "No Wallpaper picker specified!");
            return false;
        }*/
        //final ComponentName componentName =
         //       new ComponentName(mWallpaperPackage, mWallpaperClass);
        final ComponentName componentName = new ComponentName("com.android.settings.display", "com.android.settings.display.TouchScreenCalibration");
        final PackageManager pm = mContext.getPackageManager();
        final Intent intent = new Intent();
        intent.setComponent(componentName);
        final List<ResolveInfo> resolveInfos =
                pm.queryIntentActivities(intent, 0 /* flags */);
        return resolveInfos != null && resolveInfos.size() != 0;
    }

    @Override
    public String getPreferenceKey() {
        return mTouchScreenCalibrationKey;
    }
    
    @Override
    public void updateState(Preference preference) {
        
    }
    /**
    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        System.out.println("jiangning handlePreferenceTreeClick 111112");
        if (!TextUtils.equals(preference.getKey(), KEY_TOUCH_CALIBRATION)) {
            return false;
        }
        Intent mIntent = new Intent();
        ComponentName comp = new ComponentName("com.android.settings.display", "com.android.settings.display.TouchScreenCalibration");
        mIntent.setComponent(comp);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(mIntent);
        return true;
        
        final ComponentName componentName =
                new ComponentName(mWallpaperPackage, mWallpaperClass);
        final PackageManager pm = mContext.getPackageManager();
        final Intent intent = new Intent();
        intent.setComponent(componentName);
        final List<ResolveInfo> resolveInfos =
                pm.queryIntentActivities(intent, 0);
        System.out.println("jiangning handlePreferenceTreeClick 222223");
        return resolveInfos != null && resolveInfos.size() != 0;
        //return true;
        
        
    }*/

    
}