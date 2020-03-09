package com.android.settings.display;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.preference.Preference;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class TouchSwitchPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {

    private static final String TAG = "TouchSwitchPreferenceController";
    private static final String SYSTEM_TOUCH_MODE = "persist.vendor.touch.mode";
    private static final String KEY_TOUCH_SWITCH_MODE = "touch_switch";
    private static String TOUCH_MODE_NODE = "/sys/devices/soc/c178000.i2c/i2c-4/4-0024/tp_mode";
                                            

    private String mTouchSwitchKey;
    private Context mContext;
    private int touchModeID = -1;
    private String[] items;
    private byte[] Normal = { '3' };
    private byte[] Auto = { '2' };
    private byte[] Wet = { '1' };
    private byte[] ledData;
    
    public TouchSwitchPreferenceController(Context context, String key) {
        super(context);
        mTouchSwitchKey = key;
        mContext = context;

        items = mContext.getResources().getStringArray(R.array.touch_mode);
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mTouchSwitchKey;
    }
    
    @Override
    public void updateState(Preference preference) {
        updatePreferenceDescription(preference);
    }
    
    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_TOUCH_SWITCH_MODE)) {
            return false;
        }
        showSingleChoiceDialog(preference);
        return true;
    }
    
    private void updatePreferenceDescription(Preference preference) {
        int i = getTouchModeItem();
        preference.setSummary(items[i]);
    }
    
    private void touchSwitch(String fileNode, int switchId) {

        int touchModeProp = 3;

        if (switchId == 0) {
            ledData = Normal;
            touchModeProp = 3;
        } else if (switchId == 1) {
            ledData = Wet;
            touchModeProp = 1;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileNode);
            fileOutputStream.write(ledData);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }

        SystemProperties.set(SYSTEM_TOUCH_MODE, "" + touchModeProp);
    }
    
    private String readTouchMode() {
        String value = "";
        try {
            FileReader file = new FileReader(TOUCH_MODE_NODE);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            value = new String(buffer, 0, len);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value.trim();
    }
    
    
    private int getTouchModeItem() {
    	  
    	  String touchMode = readTouchMode();
    	  Log.i("Settings","getTouchModeItem()  touchMode="+touchMode);
        if (touchMode.startsWith("0x03")) {
            return 0;
        }else if(touchMode.startsWith("0x01")){
            return 1;
        }
        return 0;
    }

    private void showSingleChoiceDialog(Preference preference) {
        new AlertDialog.Builder(mContext)
            .setTitle(mContext.getString(R.string.touch_mode_title))
            .setSingleChoiceItems(items, getTouchModeItem(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    touchModeID = which;
                    preference.setSummary(items[touchModeID]);
                    touchSwitch(TOUCH_MODE_NODE, which);
                    dialog.dismiss();
                }
            })
            .setPositiveButton(android.R.string.cancel, null)
            .show();
    }
}
