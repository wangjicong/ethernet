/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.BatteryStats;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.display.BatteryPercentagePreferenceController;
import com.android.settings.display.BatteryProtectionPreferenceController;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settings.fuelgauge.anomaly.AnomalyDetectionPolicy;
import com.android.settings.fuelgauge.batterytip.BatteryTipLoader;
import com.android.settings.fuelgauge.batterytip.BatteryTipPreferenceController;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.utils.PowerUtil;
import com.android.settingslib.utils.StringUtil;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.SystemProperties;
import static com.android.settings.fuelgauge.BatteryBroadcastReceiver.BatteryUpdateType;
import android.os.UserHandle;

/**
 * Displays a list of apps and subsystems that consume power, ordered by how much power was
 * consumed since the last time it was unplugged.
 */
public class PowerUsageSummary extends PowerUsageBase implements OnLongClickListener,
        BatteryTipPreferenceController.BatteryTipListener {

    static final String TAG = "PowerUsageSummary";

    private static final boolean DEBUG = false;
    private static final String KEY_BATTERY_HEADER = "battery_header";
    private static final String KEY_BATTERY_TIP = "battery_tip";

    private static final String KEY_SCREEN_USAGE = "screen_usage";
    private static final String KEY_TIME_SINCE_LAST_FULL_CHARGE = "last_full_charge";
    private static final String KEY_BATTERY_SAVER_SUMMARY = "battery_saver_summary";

    ///1363 add sub battery status preference@{
    private static final String KEY_SUB_BATTERY_STATUS = "sub_battery_status";
    private static final String KEY_SUB_BATTERY_VOLTAGE = "sub_battery_voltage";
    private static final String KEY_SUB_BATTERY_CHARGE_STATUS = "sub_battery_charge_status";
    private static final String NODE_SUB_BATTERY_CHARGE_STATUS  = "/sys/class/power_supply/battery/sub_voltage_chg";
    private static final String NODE_SUB_BATTERY_VOLTAGE = "/sys/class/power_supply/battery/sub_voltage_now";
    private static final String NODE_MAIN_BATTERY_VOLTAGE = "/sys/class/power_supply/battery/voltage_now";
    private static final String NODE_MAIN_BATTERY_CHARGE_STATUS  = "/sys/class/power_supply/battery/status";
    private static final String NODE_MAIN_BATTERY_CHARGE_PRESENT  = "/sys/class/power_supply/battery/present";
    private static final String NODE_MAIN_BATTERY_TEMP = "/sys/class/power_supply/battery/temp";
    private static final String NODE_MAIN_BATTERY_TECHNOLOGY = "/sys/class/power_supply/battery/technology";
    private static final String NODE_MAIN_BATTERY_RATEDCAPACITY = "/sys/class/power_supply/bq27542-0/charge_full";
    private static final String NODE_MAIN_BATTERY_TOTALCUMULATIVE = "/sys/class/power_supply/bq27542-0/charge_full_design";
    //add by wangxing@20191015
    private static final String NODE_MAIN_BATTERY_CYCLE_COUNT = "/sys/class/power_supply/battery/cycle_count";
    private static final String NODE_MAIN_BATTERY_HEALTH = "sys/class/power_supply/bq27542-0/soh";
    private static final String NODE_MAIN_BATTERY_PART_NUMBER = "/sys/class/power_supply/bq27542-0/part_number";
    private static final String NODE_MAIN_BATTERY_SERIAL_NUMBER = "/sys/class/power_supply/bq27542-0/serial_number";
    private static final int DECOMMISSION_THRESHOLD = 80;
    private static final String BATTERY_HEALTH_CHANGED_ACTION = "android.battery.HEALTH_CHANGE";
    private boolean mHealthy = true;
    private static final String SUB_BATTERY_NOT_CHARGEING = "0";
    private static final String SUB_BATTERY_CHARGING = "1";
    private static final String MAIN_BATTERY_NOT_CHARGEING = "DisCharging";
    private static final String MAIN_BATTERY_CHARGING = "Charging";
    private static final String MAIN_BATTERY_FULL_CHARGING = "Full";
    private static final int SUB_BATTERY_OK = 1;
    private static final int SUB_BATTERY_FAIL = 0;
    private static final int SUB_BATTERY_UPDATE = 3;//add by wangxing for bug P_RK95_E-175
    private static final int OTHER_UI_UPDATE = 4;//add by wangxing for bug P_RK95_E-238/239 
    ///@}

    private static final String KEY_MAIN_BATTERY_STATUS = "main_battery_status";
    private static final String KEY_MAIN_BATTERY_VOLTAGE = "main_battery_voltage";
    private static final String KEY_MAIN_BATTERY_CHARGE_STATUS = "main_battery_charge_status";

    private static final String KEY_MAIN_BATTERY_TEMP = "main_battery_temp";
    private static final String KEY_MAIN_BATTERY_TECHNOLOGY = "main_battery_technology";
    private static final String KEY_MAIN_BATTERY_RATED_CAPACITY = "main_battery_rated_capacity";
    private static final String KEY_MAIN_BATTERY_TOTAL_CUMULATIVE = "main_battery_total_cumulative";
    //add by wangxing@20191015
    private static final String KEY_MAIN_BATTERY_CYCLE_COUNT = "main_battery_cycle_count";
    private static final String KEY_MAIN_BATTERY_HEALTH = "main_battery_health";
    private static final String KEY_MAIN_BATTERY_PART_NUMBER = "main_battery_part_number";
    private static final String KEY_MAIN_BATTERY_SERIAL_NUMBER = "main_battery_serial_number";

    @VisibleForTesting
    static final int BATTERY_INFO_LOADER = 1;
    @VisibleForTesting
    static final int BATTERY_TIP_LOADER = 2;
    @VisibleForTesting
    static final int MENU_STATS_TYPE = Menu.FIRST;
    @VisibleForTesting
    static final int MENU_ADVANCED_BATTERY = Menu.FIRST + 1;
    public static final int DEBUG_INFO_LOADER = 3;

    @VisibleForTesting
    PowerGaugePreference mScreenUsagePref;
    @VisibleForTesting
    PowerGaugePreference mLastFullChargePref;
    @VisibleForTesting
    PowerUsageFeatureProvider mPowerFeatureProvider;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    @VisibleForTesting
    LayoutPreference mBatteryLayoutPref;
    @VisibleForTesting
    BatteryInfo mBatteryInfo;


    ///1363 add sub battery status preference@{
    PowerGaugePreference mSubBatteryStatus;
    PowerGaugePreference mSubBatteryVoltage;
    PowerGaugePreference mSubBatteryChargeStatus;
    ///@}

    PowerGaugePreference mMainBatteryStatus;
    PowerGaugePreference mMainBatteryVoltage;
    PowerGaugePreference mMainBatteryChargeStatus;

    PowerGaugePreference mMainBatteryTemp;
    PowerGaugePreference mMainBatteryTechnology;
    PowerGaugePreference mMainBatteryRatedCapacity;
    PowerGaugePreference mMainBatteryTotalCumulative;
    //add by wangxing@20191015
    PowerGaugePreference mMainBatteryCycleCount;
    PowerGaugePreference mMainBatteryHealth;
    PowerGaugePreference mMainBatteryPartNumber;
    PowerGaugePreference mMainBatterySerialNumber;

    /**
     * SparseArray that maps uid to {@link Anomaly}, so we could find {@link Anomaly} by uid
     */
    @VisibleForTesting
    SparseArray<List<Anomaly>> mAnomalySparseArray;
    @VisibleForTesting
    BatteryHeaderPreferenceController mBatteryHeaderPreferenceController;
    @VisibleForTesting
    boolean mNeedUpdateBatteryTip;
    @VisibleForTesting
    BatteryTipPreferenceController mBatteryTipPreferenceController;
    private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

    @VisibleForTesting
    LoaderManager.LoaderCallbacks<BatteryInfo> mBatteryInfoLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<BatteryInfo>() {

                @Override
                public Loader<BatteryInfo> onCreateLoader(int i, Bundle bundle) {
                    return new BatteryInfoLoader(getContext(), mStatsHelper);
                }

                @Override
                public void onLoadFinished(Loader<BatteryInfo> loader, BatteryInfo batteryInfo) {
                    mBatteryHeaderPreferenceController.updateHeaderPreference(batteryInfo);
                    mBatteryInfo = batteryInfo;
                    updateLastFullChargePreference();
                }

                @Override
                public void onLoaderReset(Loader<BatteryInfo> loader) {
                    // do nothing
                }
            };

    LoaderManager.LoaderCallbacks<List<BatteryInfo>> mBatteryInfoDebugLoaderCallbacks =
            new LoaderCallbacks<List<BatteryInfo>>() {
                @Override
                public Loader<List<BatteryInfo>> onCreateLoader(int i, Bundle bundle) {
                    return new DebugEstimatesLoader(getContext(), mStatsHelper);
                }

                @Override
                public void onLoadFinished(Loader<List<BatteryInfo>> loader,
                        List<BatteryInfo> batteryInfos) {
                    updateViews(batteryInfos);
                }

                @Override
                public void onLoaderReset(Loader<List<BatteryInfo>> loader) {
                }
            };

    protected void updateViews(List<BatteryInfo> batteryInfos) {
        final BatteryMeterView batteryView = mBatteryLayoutPref
            .findViewById(R.id.battery_header_icon);
        final TextView percentRemaining =
            mBatteryLayoutPref.findViewById(R.id.battery_percent);
        final TextView summary1 = mBatteryLayoutPref.findViewById(R.id.summary1);
        final TextView summary2 = mBatteryLayoutPref.findViewById(R.id.summary2);
        BatteryInfo oldInfo = batteryInfos.get(0);
        BatteryInfo newInfo = batteryInfos.get(1);
        percentRemaining.setText(Utils.formatPercentage(oldInfo.batteryLevel));

        // set the text to the old estimate (copied from battery info). Note that this
        // can sometimes say 0 time remaining because battery stats requires the phone
        // be unplugged for a period of time before being willing ot make an estimate.
        summary1.setText(mPowerFeatureProvider.getOldEstimateDebugString(
            Formatter.formatShortElapsedTime(getContext(),
                PowerUtil.convertUsToMs(oldInfo.remainingTimeUs))));

        // for this one we can just set the string directly
        summary2.setText(mPowerFeatureProvider.getEnhancedEstimateDebugString(
            Formatter.formatShortElapsedTime(getContext(),
                PowerUtil.convertUsToMs(newInfo.remainingTimeUs))));

        batteryView.setBatteryLevel(oldInfo.batteryLevel);
        batteryView.setCharging(!oldInfo.discharging);
    }

    private LoaderManager.LoaderCallbacks<List<BatteryTip>> mBatteryTipsCallbacks =
            new LoaderManager.LoaderCallbacks<List<BatteryTip>>() {

                @Override
                public Loader<List<BatteryTip>> onCreateLoader(int id, Bundle args) {
                    return new BatteryTipLoader(getContext(), mStatsHelper);
                }

                @Override
                public void onLoadFinished(Loader<List<BatteryTip>> loader,
                        List<BatteryTip> data) {
                    mBatteryTipPreferenceController.updateBatteryTips(data);
                }

                @Override
                public void onLoaderReset(Loader<List<BatteryTip>> loader) {

                }
            };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setAnimationAllowed(true);

        initFeatureProvider();
        mBatteryLayoutPref = (LayoutPreference) findPreference(KEY_BATTERY_HEADER);

        mScreenUsagePref = (PowerGaugePreference) findPreference(KEY_SCREEN_USAGE);
        mLastFullChargePref = (PowerGaugePreference) findPreference(
                KEY_TIME_SINCE_LAST_FULL_CHARGE);

        ///1363 add sub battery status preference@{
        //Modify by lijinhua @20191111 for P_RK95_E-780 start 
        //mSubBatteryStatus =(PowerGaugePreference) findPreference(KEY_SUB_BATTERY_STATUS); 
        mSubBatteryVoltage =(PowerGaugePreference) findPreference(KEY_SUB_BATTERY_VOLTAGE); 
        mSubBatteryChargeStatus =(PowerGaugePreference) findPreference(KEY_SUB_BATTERY_CHARGE_STATUS); 
        ///@}

        //mMainBatteryStatus =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_STATUS);
        //Modify by lijinhua @20191111 for P_RK95_E-780 end  
        // mMainBatteryVoltage =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_VOLTAGE);
        mMainBatteryChargeStatus =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_CHARGE_STATUS);

        mMainBatteryTemp =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_TEMP);
        mMainBatteryTechnology =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_TECHNOLOGY);
        // mMainBatteryRatedCapacity =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_RATED_CAPACITY);
        // mMainBatteryTotalCumulative =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_TOTAL_CUMULATIVE);
        //add by wangxing@20191015
        mMainBatteryCycleCount =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_CYCLE_COUNT);
        mMainBatteryHealth =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_HEALTH);
        mMainBatteryPartNumber =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_PART_NUMBER);
        mMainBatterySerialNumber =(PowerGaugePreference) findPreference(KEY_MAIN_BATTERY_SERIAL_NUMBER);
        

        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.battery_footer_summary);
        mBatteryUtils = BatteryUtils.getInstance(getContext());
        mAnomalySparseArray = new SparseArray<>();

        restartBatteryInfoLoader();
        mBatteryTipPreferenceController.restoreInstanceState(icicle);
        updateBatteryTipFlag(icicle);

        BatteryHandler.getInstance().setHandler(mSubBatteryHandler); //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721
        ///1316 anr
        ///1363 add sub battery status preference@{
        // Handler handler = new Handler();
        // Runnable runnalbe = new Runnable() {
        //     @Override
        //     public void run() {
        //         handler.postDelayed(this, 2000);
        //         refreshUi(BatteryUpdateType.MANUAL);
        //     }
        // };
        // handler.post(runnalbe);
        ///@}
    }

    //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ---- start
    private Handler mSubBatteryHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUB_BATTERY_OK:
                    refreshUi(BatteryUpdateType.MANUAL);
                    return;
                case SUB_BATTERY_FAIL:
                    refreshUi(BatteryUpdateType.MANUAL);
                    return;
                //add by wangxing for bug P_RK95_E-175 start 
                case SUB_BATTERY_UPDATE:
                    updateSubBattery();
                    return;
                //add by wangxing for bug P_RK95_E-175 end
                case OTHER_UI_UPDATE://add by wangxing for bug P_RK95_E-238/239 
                    updateOtherUI();
                    return;
                default:
                    break;
            }
        }
    };
    //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ---- end
    
    //add by wangxing for bug P_RK95_E-238/239 start     
    private void updateOtherUI(){
        updateMainBatteryTempPreference(); 
        updateMainBatteryHealthPreference();
        updateMainBatteryPartNumberPreference();
        updateMainBatterySerialNumberPreference();
        mSubBatteryHandler.removeMessages(OTHER_UI_UPDATE);
        mSubBatteryHandler.sendEmptyMessageDelayed(OTHER_UI_UPDATE, 5000);
    }
    
    public void onResume() {
         super.onResume(); 
         updateOtherUI();
    }
    //add by wangxing for bug P_RK95_E-238/239 end
    
       
    //add by wangxing for bug P_RK95_E-175 start     
    @Override
    public void onPause() {
        super.onPause();
        mSubBatteryHandler.removeMessages(SUB_BATTERY_UPDATE);
        mSubBatteryHandler.removeMessages(OTHER_UI_UPDATE);
    }
    //add by wangxing for bug P_RK95_E-175 end
    
    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY_V2;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.power_usage_summary;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final Lifecycle lifecycle = getLifecycle();
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        mBatteryHeaderPreferenceController = new BatteryHeaderPreferenceController(
                context, activity, this /* host */, lifecycle);
        controllers.add(mBatteryHeaderPreferenceController);
        mBatteryTipPreferenceController = new BatteryTipPreferenceController(context,
                KEY_BATTERY_TIP, (SettingsActivity) getActivity(), this /* fragment */, this /*
                BatteryTipListener */);
        controllers.add(mBatteryTipPreferenceController);
        controllers.add(new BatteryPercentagePreferenceController(context));
        ///P_RK95_E-37 Battery Protection Mode
        controllers.add(new BatteryProtectionPreferenceController(context));
        return controllers;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) {
            menu.add(Menu.NONE, MENU_STATS_TYPE, Menu.NONE, R.string.menu_stats_total)
                    .setIcon(com.android.internal.R.drawable.ic_menu_info_details)
                    .setAlphabeticShortcut('t');
        }

        menu.add(Menu.NONE, MENU_ADVANCED_BATTERY, Menu.NONE, R.string.advanced_battery_title);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_battery;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STATS_TYPE:
                if (mStatsType == BatteryStats.STATS_SINCE_CHARGED) {
                    mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
                } else {
                    mStatsType = BatteryStats.STATS_SINCE_CHARGED;
                }
                refreshUi(BatteryUpdateType.MANUAL);
                return true;
            case MENU_ADVANCED_BATTERY:
                new SubSettingLauncher(getContext())
                        .setDestination(PowerUsageAdvanced.class.getName())
                        .setSourceMetricsCategory(getMetricsCategory())
                        .setTitle(R.string.advanced_battery_title)
                        .launch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void refreshUi(@BatteryUpdateType int refreshType) {
        Log.d(TAG,"PowerUsageSummary refreshUi ============= " );
        final Context context = getContext();
        if (context == null) {
            return;
        }

        // Skip BatteryTipLoader if device is rotated or only battery level change
        if (mNeedUpdateBatteryTip
                && refreshType != BatteryUpdateType.BATTERY_LEVEL) {
            restartBatteryTipLoader();
        } else {
            mNeedUpdateBatteryTip = true;
        }

        // reload BatteryInfo and updateUI
        restartBatteryInfoLoader();
        updateLastFullChargePreference();

        //add by wangxing for bug P_RK95_E-175 start
        ///1363 add sub battery status preference@{
       // updateSubBatteryStatusPreference();
       // updateSubBatteryVoltagePreference();
       // updateSubBatteryChargeStatusPreference();
        ///@}
        updateSubBattery();
        //add by wangxing for bug P_RK95_E-175 end

        //updateMainBatteryStatusPreference();  //Modify by lijinhua @20191111 for P_RK95_E-780 start 
        // updateMainBatteryVoltagePreference();
        updateMainBatteryChargeStatusPreference();

        updateMainBatteryTempPreference();
        updateMainBatteryTechnologyPreference();
        // updateMainBatteryRatedCapacityPreference();
        // updateMainBatteryTotalCumulativePreference();
        //add by wangxing@20191015
        updateMainBatteryCycleCountPreference();


        mScreenUsagePref.setSubtitle(StringUtil.formatElapsedTime(getContext(),
                mBatteryUtils.calculateScreenUsageTime(mStatsHelper), false));
    }

    @VisibleForTesting
    void restartBatteryTipLoader() {
        getLoaderManager().restartLoader(BATTERY_TIP_LOADER, Bundle.EMPTY, mBatteryTipsCallbacks);
    }

    @VisibleForTesting
    void setBatteryLayoutPreference(LayoutPreference layoutPreference) {
        mBatteryLayoutPref = layoutPreference;
    }

    @VisibleForTesting
    AnomalyDetectionPolicy getAnomalyDetectionPolicy() {
        return new AnomalyDetectionPolicy(getContext());
    }

    @VisibleForTesting
    void updateLastFullChargePreference() {
        if (mBatteryInfo != null && mBatteryInfo.averageTimeToDischarge
                != Estimate.AVERAGE_TIME_TO_DISCHARGE_UNKNOWN) {
            mLastFullChargePref.setTitle(R.string.battery_full_charge_last);
            mLastFullChargePref.setSubtitle(
                    StringUtil.formatElapsedTime(getContext(), mBatteryInfo.averageTimeToDischarge,
                            false /* withSeconds */));
        } else {
            final long lastFullChargeTime = mBatteryUtils.calculateLastFullChargeTime(mStatsHelper,
                    System.currentTimeMillis());
            mLastFullChargePref.setTitle(R.string.battery_last_full_charge);
            mLastFullChargePref.setSubtitle(
                    StringUtil.formatRelativeTime(getContext(), lastFullChargeTime,
                            false /* withSeconds */));
        }
    }


    ///1363 add sub battery status preference@{
    @VisibleForTesting
    void updateSubBatteryStatusPreference() {
        String subBatteryVoltage = getSubBatteryVoltage();
        Float voltage = Float.parseFloat(subBatteryVoltage) / 1000;
        mSubBatteryStatus.setSubtitle(
            getText(
                (voltage < 2 || voltage > 5) ? 
                    R.string.sub_battery_status_abnormal : 
                    R.string.sub_battery_status_normal
            )
        );
    }

    @VisibleForTesting
    void updateMainBatteryStatusPreference() {
        String mainBatteryPresent = getMainBatteryPresent();
        Log.i(TAG,"mainBatteryPresent ===== " + mainBatteryPresent);
        if(mainBatteryPresent.equals("1")){
            mMainBatteryStatus.setSubtitle(getText(R.string.sub_battery_status_normal));
        } else{
            mMainBatteryStatus.setSubtitle(getText(R.string.sub_battery_status_abnormal));
        }
    }

    @VisibleForTesting
    private String getMainBatteryPresent(){
        String mainBatteryPresent = "1";
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_CHARGE_PRESENT);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            mainBatteryPresent = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainBatteryPresent.trim();
    }

    @VisibleForTesting
    void updateSubBatteryVoltagePreference() {
        Float voltage = Float.parseFloat(getSubBatteryVoltage()) / 1000;
        String voltageString = new DecimalFormat("##0.00").format(voltage);
        //modify by wangxing@20191010 for P_RK95_E-675
        if(voltage > 3.9){
            mSubBatteryVoltage.setSubtitle( getText(R.string.battery_voltage_good));
        }else if( voltage > 0 && voltage <= 3.9){
            mSubBatteryVoltage.setSubtitle( getText(R.string.battery_voltage_low));
        }else{
            mSubBatteryVoltage.setSubtitle(TextUtils.expandTemplate(getText(R.string.sub_charge_battery), voltageString));
        }
        
    }

    @VisibleForTesting
    void updateMainBatteryVoltagePreference(){
        if (getMainBatteryPresent().equals("1")){
            Float voltage = Float.parseFloat(getMainBatteryVoltage()) / 1000000;
            String voltageString = new DecimalFormat("##0.00").format(voltage);
            mMainBatteryVoltage.setSubtitle(TextUtils.expandTemplate(getText(R.string.sub_charge_battery), voltageString));
        } else {
            mMainBatteryVoltage.setSubtitle(TextUtils.expandTemplate(getText(R.string.sub_charge_battery), "0"));
        }
    }

    @VisibleForTesting
    void updateSubBatteryChargeStatusPreference() { 
        String subBatteryChargeStatus = getSubBatteryChargsStatus();
        Float subBatteryVoltage = Float.parseFloat(getSubBatteryVoltage())/1000;
        Log.d(TAG,"isCharging(getContext()) == " + isCharging(getContext()));
        Log.d(TAG,"subBatteryChargeStatus == " + subBatteryChargeStatus);

        //modify by wangxing for bug P_RK95_E-452 start
        if(subBatteryChargeStatus.equals(SUB_BATTERY_CHARGING) && subBatteryVoltage > 1){
            if(subBatteryVoltage < 4.2 ){
               mSubBatteryChargeStatus.setSubtitle(getText(R.string.sub_charging));
            }else{
               mSubBatteryChargeStatus.setSubtitle(getText(R.string.sub_full_charging));
            }
         //   writeSubBatteryChargeStatus(SUB_BATTERY_CHARGING);
        } else {
            mSubBatteryChargeStatus.setSubtitle(getText(R.string.sub_no_charging));
           // writeSubBatteryChargeStatus(SUB_BATTERY_NOT_CHARGEING);
        }
        //modify by wangxing for bug P_RK95_E-452 end
    }
    
    //add by wangxing for bug P_RK95_E-175 start
    public void updateSubBattery(){
        if(subBatteryIsChecking()){
            //mSubBatteryStatus.setSubtitle(getText(R.string.sub_battery_checking)); //Modify by lijinhua @20191111 for P_RK95_E-780
            mSubBatteryVoltage.setSubtitle(getText(R.string.sub_battery_checking));
            mSubBatteryChargeStatus.setSubtitle(getText(R.string.sub_battery_checking));
            mSubBatteryHandler.removeMessages(SUB_BATTERY_UPDATE);
            mSubBatteryHandler.sendEmptyMessageDelayed(SUB_BATTERY_UPDATE, 10000);
        }else{
            //updateSubBatteryStatusPreference(); //Modify by lijinhua @20191111 for P_RK95_E-780
            updateSubBatteryVoltagePreference();
            updateSubBatteryChargeStatusPreference();
        }
    }

    public boolean subBatteryIsChecking(){
        return SystemProperties.getBoolean("persist.sys.subbattery.checking", false);
    }
    //add by wangxing for bug P_RK95_E-175 end
    
    public void writeSubBatteryChargeStatus(String value) {
        try {
            BufferedWriter bufWriter = null;
            bufWriter = new BufferedWriter(new FileWriter(NODE_SUB_BATTERY_CHARGE_STATUS));
            bufWriter.write(value);
            bufWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    void updateMainBatteryChargeStatusPreference() {
        String mainBatteryChargeStatus = getMainBatteryChargsStatus();
        Log.d(TAG,"updateMainBatteryChargeStatusPreference == " + mainBatteryChargeStatus);
        if(mainBatteryChargeStatus.equals(MAIN_BATTERY_CHARGING)){
            mMainBatteryChargeStatus.setSubtitle(getText(R.string.sub_charging));
        } else if(mainBatteryChargeStatus.equals(MAIN_BATTERY_FULL_CHARGING)){
            mMainBatteryChargeStatus.setSubtitle(getText(R.string.sub_full_charging));
        } else {
            mMainBatteryChargeStatus.setSubtitle(getText(R.string.sub_no_charging));
        }
    }

    void updateMainBatteryTempPreference(){
        String mainBatteryTemp = "0";
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_TEMP);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            mainBatteryTemp = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Float temp = Float.parseFloat(mainBatteryTemp) / 10;
        String tempString = new DecimalFormat("##0.0").format(temp);
        mMainBatteryTemp.setSubtitle(tempString + " ℃");
    }

    void updateMainBatteryTechnologyPreference(){
        String mainBatteryTechnology = "0";
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_TECHNOLOGY);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            mainBatteryTechnology = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMainBatteryTechnology.setSubtitle(mainBatteryTechnology);
    }

    void updateMainBatteryRatedCapacityPreference(){
        String mainBatteryRatedCapacity = "0";
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_RATEDCAPACITY);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            mainBatteryRatedCapacity = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Float ratedCapacity = Float.parseFloat(mainBatteryRatedCapacity) / 1000;
        String ratedCapacityString = new DecimalFormat("##0").format(ratedCapacity);
        mMainBatteryRatedCapacity.setSubtitle(TextUtils.expandTemplate(getText(R.string.main_battery_capacity_mah), ratedCapacityString));
    }

    void updateMainBatteryTotalCumulativePreference(){
        String mainBatteryTotalCumulative = "0";
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_TOTALCUMULATIVE);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            mainBatteryTotalCumulative = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Float totalCumulative = Float.parseFloat(mainBatteryTotalCumulative) / 1000;
        String totalCumulativeString = new DecimalFormat("##0").format(totalCumulative);
        mMainBatteryTotalCumulative.setSubtitle(TextUtils.expandTemplate(getText(R.string.main_battery_capacity_mah), totalCumulativeString));
    }

    //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ----- start
    private boolean isCharging(Context context) {
        Intent batteryBroadcast = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // 0 means we are discharging, anything else means charging
        boolean isCharging = batteryBroadcast.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
        Log.d(TAG,"isCharging = " + isCharging );
        return isCharging;
    }
    //MEIG-jiangdanyang@Modifying the charging status of sub battery 20190721 ----- end

    //add by wangxing@20191015
    private void updateMainBatteryCycleCountPreference(){
        String cycleCount = "0";
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_CYCLE_COUNT);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            cycleCount = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cycleCount = cycleCount.trim();
        mMainBatteryCycleCount.setSubtitle(cycleCount);
    }
    
    //add by wangxing for bug P_RK95_E-673
    private void updateMainBatteryHealthPreference(){
        String health = "0";
        int healthValue = 100;
        try {
            FileReader file = new FileReader(NODE_MAIN_BATTERY_HEALTH);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            health = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        health = health.trim();
        int healthThreshold = SystemProperties.getInt("persist.sys.battery.threshold", DECOMMISSION_THRESHOLD);
        try {
            healthValue = Integer.parseInt(health);
        }catch(Exception e){
            e.printStackTrace();
        }
        boolean currentHealthy = healthValue > healthThreshold;
        if(currentHealthy != mHealthy){
            mHealthy = currentHealthy;
            getContext().sendBroadcastAsUser(new Intent(BATTERY_HEALTH_CHANGED_ACTION), UserHandle.ALL);
        }
        if(currentHealthy){
            mMainBatteryHealth.setSubtitle(getText(R.string.main_battery_health_good));
        }else{
            mMainBatteryHealth.setSubtitle(getText(R.string.main_battery_health_bad));
        }
    }
    
    private void updateMainBatteryPartNumberPreference(){         
        String partNumber = getNodeValueString(NODE_MAIN_BATTERY_PART_NUMBER);
        Log.d(TAG,"partNumber = " + partNumber ); 
        mMainBatteryPartNumber.setSubtitle(partNumber);
    }
    
    private void updateMainBatterySerialNumberPreference(){
        String serialNumber = getNodeValueString(NODE_MAIN_BATTERY_SERIAL_NUMBER);
        Log.d(TAG,"serialNumber = " + serialNumber );  
        mMainBatterySerialNumber.setSubtitle(serialNumber);
    }
    
    private String getNodeValueString(String node){
        String value = "";
        try {
            FileReader file = new FileReader(node);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            value = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value.trim();
    }

    private String getSubBatteryVoltage() {
        String subBatteryVoltage = "0";
        try {
            FileReader file = new FileReader(NODE_SUB_BATTERY_VOLTAGE);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            subBatteryVoltage = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subBatteryVoltage.trim();
    }

    private String getMainBatteryVoltage() {
        String mainBatteryVoltage = "0";
        if (getMainBatteryPresent().equals("1")){
            try {
                FileReader file = new FileReader(NODE_MAIN_BATTERY_VOLTAGE);
                char[] buffer = new char[1024];
                int len = file.read(buffer, 0, 1024);
                mainBatteryVoltage = new String(buffer, 0, len);
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mainBatteryVoltage = "0";
        }
        return mainBatteryVoltage.trim();
    }

    private String getSubBatteryChargsStatus() {
        String subBatterStatus = SUB_BATTERY_NOT_CHARGEING;
        try {
            FileReader file = new FileReader(NODE_SUB_BATTERY_CHARGE_STATUS);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            subBatterStatus = new String(buffer, 0, len);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subBatterStatus.trim();
    }
    ///@}

    private String getMainBatteryChargsStatus() {
        String subBatterStatus = MAIN_BATTERY_NOT_CHARGEING;
        if (getMainBatteryPresent().equals("1")){
            try {
                FileReader file = new FileReader(NODE_MAIN_BATTERY_CHARGE_STATUS);
                char[] buffer = new char[1024];
                int len = file.read(buffer, 0, 1024);
                subBatterStatus = new String(buffer, 0, len);
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            subBatterStatus = "error";
        }
        return subBatterStatus.trim();
    }

    @VisibleForTesting
    void showBothEstimates() {
        final Context context = getContext();
        if (context == null
                || !mPowerFeatureProvider.isEnhancedBatteryPredictionEnabled(context)) {
            return;
        }
        getLoaderManager().restartLoader(DEBUG_INFO_LOADER, Bundle.EMPTY,
                mBatteryInfoDebugLoaderCallbacks);
    }

    @VisibleForTesting
    void initFeatureProvider() {
        final Context context = getContext();
        mPowerFeatureProvider = FeatureFactory.getFactory(context)
                .getPowerUsageFeatureProvider(context);
    }

    @VisibleForTesting
    void updateAnomalySparseArray(List<Anomaly> anomalies) {
        mAnomalySparseArray.clear();
        for (final Anomaly anomaly : anomalies) {
            if (mAnomalySparseArray.get(anomaly.uid) == null) {
                mAnomalySparseArray.append(anomaly.uid, new ArrayList<>());
            }
            mAnomalySparseArray.get(anomaly.uid).add(anomaly);
        }
    }

    @VisibleForTesting
    void restartBatteryInfoLoader() {
        getLoaderManager().restartLoader(BATTERY_INFO_LOADER, Bundle.EMPTY,
                mBatteryInfoLoaderCallbacks);
        if (mPowerFeatureProvider.isEstimateDebugEnabled()) {
            // Set long click action for summary to show debug info
            View header = mBatteryLayoutPref.findViewById(R.id.summary1);
            header.setOnLongClickListener(this);
        }
    }

    @VisibleForTesting
    void updateBatteryTipFlag(Bundle icicle) {
        mNeedUpdateBatteryTip = icicle == null || mBatteryTipPreferenceController.needUpdate();
    }

    @Override
    public boolean onLongClick(View view) {
        showBothEstimates();
        view.setOnLongClickListener(null);
        return true;
    }

    @Override
    protected void restartBatteryStatsLoader(@BatteryUpdateType int refreshType) {
        super.restartBatteryStatsLoader(refreshType);
        mBatteryHeaderPreferenceController.quickUpdateHeaderPreference();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mBatteryTipPreferenceController.saveInstanceState(outState);
    }

    @Override
    public void onBatteryTipHandled(BatteryTip batteryTip) {
        restartBatteryTipLoader();
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;
        private final BatteryBroadcastReceiver mBatteryBroadcastReceiver;

        private SummaryProvider(Context context, SummaryLoader loader) {
            mContext = context;
            mLoader = loader;
            mBatteryBroadcastReceiver = new BatteryBroadcastReceiver(mContext);
            mBatteryBroadcastReceiver.setBatteryChangedListener(type -> {
                BatteryInfo.getBatteryInfo(mContext, new BatteryInfo.Callback() {
                    @Override
                    public void onBatteryInfoLoaded(BatteryInfo info) {
                        mLoader.setSummary(SummaryProvider.this, getDashboardLabel(mContext, info));
                    }
                }, true /* shortString */);
            });
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                mBatteryBroadcastReceiver.register();
            } else {
                mBatteryBroadcastReceiver.unRegister();
            }
        }
    }

    @VisibleForTesting
    static CharSequence getDashboardLabel(Context context, BatteryInfo info) {
        CharSequence label;
        final BidiFormatter formatter = BidiFormatter.getInstance();
        if (info.remainingLabel == null) {
            label = info.batteryPercentString;
        } else {
            label = context.getString(R.string.power_remaining_settings_home_page,
                    formatter.unicodeWrap(info.batteryPercentString),
                    formatter.unicodeWrap(info.remainingLabel));
        }
        return label;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.power_usage_summary;
                    return Collections.singletonList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> niks = super.getNonIndexableKeys(context);

                    final BatteryPercentagePreferenceController controller =
                            new BatteryPercentagePreferenceController(context);
                    if (!controller.isAvailable()) {
                        niks.add(controller.getPreferenceKey());
                    }

                    final BatteryProtectionPreferenceController controller1 =
                            new BatteryProtectionPreferenceController(context);
                    if (!controller1.isAvailable()) {
                        niks.add(controller1.getPreferenceKey());
                    }

                    niks.add(KEY_BATTERY_SAVER_SUMMARY);
                    return niks;
                }
            };

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
}
