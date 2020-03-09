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

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothCodecStatus;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.development.SystemPropPoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v7.preference.Preference;

public class DevelopmentSettingsDashboardFragment extends RestrictedDashboardFragment
        implements SwitchBar.OnSwitchChangeListener, OemUnlockDialogHost, AdbDialogHost,
        AdbClearKeysDialogHost, LogPersistDialogHost,
        BluetoothA2dpHwOffloadRebootDialog.OnA2dpHwDialogConfirmedListener {

    private static final String TAG = "DevSettingsDashboard";

    private static final String QCT_DLT_KEY = "qct_dlt";
    
    private static final String FUNCTION_TEST_KEY = "function_test";
	
	//add by maohaojie on 2019.07.22 for bug 1151
	private static final String LCDM_BRIGHT_TEST_KEY = "lcdm_bright_test";

    private static final String QTI_LOGKIT_KEY = "qti_logkit";
    
    private static final String HOST_LOGGER_KEY = "host_logger";

    private final BluetoothA2dpConfigStore mBluetoothA2dpConfigStore =
            new BluetoothA2dpConfigStore();

    private boolean mIsAvailable = true;
    private SwitchBar mSwitchBar;
    private DevelopmentSwitchBarController mSwitchBarController;
    private List<AbstractPreferenceController> mPreferenceControllers = new ArrayList<>();
    private BluetoothA2dp mBluetoothA2dp;

    private final BroadcastReceiver mEnableAdbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (AbstractPreferenceController controller : mPreferenceControllers) {
                if (controller instanceof AdbOnChangeListener) {
                    ((AdbOnChangeListener) controller).onAdbSettingChanged();
                }
            }
        }
    };

    private final BroadcastReceiver mBluetoothA2dpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mBluetoothA2dpReceiver.onReceive intent=" + intent);
            String action = intent.getAction();

            if (BluetoothA2dp.ACTION_CODEC_CONFIG_CHANGED.equals(action)) {
                BluetoothCodecStatus codecStatus = intent.getParcelableExtra(
                        BluetoothCodecStatus.EXTRA_CODEC_STATUS);
                Log.d(TAG, "Received BluetoothCodecStatus=" + codecStatus);
                for (AbstractPreferenceController controller : mPreferenceControllers) {
                    if (controller instanceof BluetoothServiceConnectionListener) {
                        ((BluetoothServiceConnectionListener) controller).onBluetoothCodecUpdated();
                    }
                }
            }
        }
    };


    private final BluetoothProfile.ServiceListener mBluetoothA2dpServiceListener =
            new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile,
                        BluetoothProfile proxy) {
                    synchronized (mBluetoothA2dpConfigStore) {
                        mBluetoothA2dp = (BluetoothA2dp) proxy;
                    }
                    for (AbstractPreferenceController controller : mPreferenceControllers) {
                        if (controller instanceof BluetoothServiceConnectionListener) {
                            ((BluetoothServiceConnectionListener) controller)
                                    .onBluetoothServiceConnected(mBluetoothA2dp);
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(int profile) {
                    synchronized (mBluetoothA2dpConfigStore) {
                        mBluetoothA2dp = null;
                    }
                    for (AbstractPreferenceController controller : mPreferenceControllers) {
                        if (controller instanceof BluetoothServiceConnectionListener) {
                            ((BluetoothServiceConnectionListener) controller)
                                    .onBluetoothServiceDisconnected();
                        }
                    }
                }
            };

    public DevelopmentSettingsDashboardFragment() {
        super(UserManager.DISALLOW_DEBUGGING_FEATURES);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (Utils.isMonkeyRunning()) {
            getActivity().finish();
            return;
        }
		//add by zhaohairuo for jira 228 start
	    if("true".equals(SystemProperties.get("ro.build.meig.feature.wifi"))){
            removePreference(QCT_DLT_KEY);
        }
	    //add by zhaohairuo for jira 228 end
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        // Apply page-level restrictions
        setIfOnlyAvailableForAdmins(true);
        if (isUiRestricted() || !Utils.isDeviceProvisioned(getActivity())) {
            // Block access to developer options if the user is not the owner, if user policy
            // restricts it, or if the device has not been provisioned
            mIsAvailable = false;
            // Show error message
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(R.string.development_settings_not_available);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        // Set up master switch
        mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        mSwitchBarController = new DevelopmentSwitchBarController(
                this /* DevelopmentSettings */, mSwitchBar, mIsAvailable, getLifecycle());
        mSwitchBar.show();

        // Restore UI state based on whether developer options is enabled
        if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(getContext())) {
            enableDeveloperOptions();
        } else {
            disableDeveloperOptions();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        registerReceivers();

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(getActivity(), mBluetoothA2dpServiceListener,
                    BluetoothProfile.A2DP);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceivers();

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
            mBluetoothA2dp = null;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DEVELOPMENT;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (switchView != mSwitchBar.getSwitch()) {
            return;
        }
        final boolean developmentEnabledState =
                DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(getContext());
        if (isChecked != developmentEnabledState) {
            if (isChecked) {
                EnableDevelopmentSettingWarningDialog.show(this /* host */);
            } else {
                disableDeveloperOptions();
            }
        }
    }

    @Override
    public void onOemUnlockDialogConfirmed() {
        final OemUnlockPreferenceController controller = getDevelopmentOptionsController(
                OemUnlockPreferenceController.class);
        controller.onOemUnlockConfirmed();
    }

    @Override
    public void onOemUnlockDialogDismissed() {
        final OemUnlockPreferenceController controller = getDevelopmentOptionsController(
                OemUnlockPreferenceController.class);
        controller.onOemUnlockDismissed();
    }

    @Override
    public void onEnableAdbDialogConfirmed() {
        final AdbPreferenceController controller = getDevelopmentOptionsController(
                AdbPreferenceController.class);
        controller.onAdbDialogConfirmed();

    }

    @Override
    public void onEnableAdbDialogDismissed() {
        final AdbPreferenceController controller = getDevelopmentOptionsController(
                AdbPreferenceController.class);
        controller.onAdbDialogDismissed();
    }

    @Override
    public void onAdbClearKeysDialogConfirmed() {
        final ClearAdbKeysPreferenceController controller = getDevelopmentOptionsController(
                ClearAdbKeysPreferenceController.class);
        controller.onClearAdbKeysConfirmed();
    }

    @Override
    public void onDisableLogPersistDialogConfirmed() {
        final LogPersistPreferenceController controller = getDevelopmentOptionsController(
                LogPersistPreferenceController.class);
        controller.onDisableLogPersistDialogConfirmed();
    }

    @Override
    public void onDisableLogPersistDialogRejected() {
        final LogPersistPreferenceController controller = getDevelopmentOptionsController(
                LogPersistPreferenceController.class);
        controller.onDisableLogPersistDialogRejected();
    }

    @Override
    public void onA2dpHwDialogConfirmed() {
        final BluetoothA2dpHwOffloadPreferenceController controller =
                getDevelopmentOptionsController(BluetoothA2dpHwOffloadPreferenceController.class);
        controller.onA2dpHwDialogConfirmed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handledResult = false;
        for (AbstractPreferenceController controller : mPreferenceControllers) {
            if (controller instanceof OnActivityResultListener) {
                // We do not break early because it is possible for multiple controllers to
                // handle the same result code.
                handledResult |=
                        ((OnActivityResultListener) controller).onActivityResult(
                                requestCode, resultCode, data);
            }
        }
        if (!handledResult) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getHelpResource() {
        return 0;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return Utils.isMonkeyRunning()? R.xml.placeholder_prefs : R.xml.development_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        if (Utils.isMonkeyRunning()) {
            mPreferenceControllers = new ArrayList<>();
            return null;
        }
        mPreferenceControllers = buildPreferenceControllers(context, getActivity(), getLifecycle(),
                this /* devOptionsDashboardFragment */,
                new BluetoothA2dpConfigStore());
        return mPreferenceControllers;
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mEnableAdbReceiver, new IntentFilter(
                        AdbPreferenceController.ACTION_ENABLE_ADB_STATE_CHANGED));

        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothA2dp.ACTION_CODEC_CONFIG_CHANGED);
        getActivity().registerReceiver(mBluetoothA2dpReceiver, filter);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mEnableAdbReceiver);
        getActivity().unregisterReceiver(mBluetoothA2dpReceiver);
    }

    private void enableDeveloperOptions() {
        if (Utils.isMonkeyRunning()) {
            return;
        }
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(getContext(), true);
        for (AbstractPreferenceController controller : mPreferenceControllers) {
            if (controller instanceof DeveloperOptionsPreferenceController) {
                ((DeveloperOptionsPreferenceController) controller).onDeveloperOptionsEnabled();
            }
        }
    }

    private void disableDeveloperOptions() {
        if (Utils.isMonkeyRunning()) {
            return;
        }
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(getContext(), false);
        final SystemPropPoker poker = SystemPropPoker.getInstance();
        poker.blockPokes();
        for (AbstractPreferenceController controller : mPreferenceControllers) {
            if (controller instanceof DeveloperOptionsPreferenceController) {
                ((DeveloperOptionsPreferenceController) controller)
                        .onDeveloperOptionsDisabled();
            }
        }
        poker.unblockPokes();
        poker.poke();
    }

    void onEnableDevelopmentOptionsConfirmed() {
        enableDeveloperOptions();
    }

    void onEnableDevelopmentOptionsRejected() {
        // Reset the toggle
        mSwitchBar.setChecked(false);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Lifecycle lifecycle, DevelopmentSettingsDashboardFragment fragment,
            BluetoothA2dpConfigStore bluetoothA2dpConfigStore) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new MemoryUsagePreferenceController(context));
        controllers.add(new BugReportPreferenceController(context));
        controllers.add(new LocalBackupPasswordPreferenceController(context));
        controllers.add(new StayAwakePreferenceController(context, lifecycle));
        controllers.add(new HdcpCheckingPreferenceController(context));
        controllers.add(new DarkUIPreferenceController(context));
        controllers.add(new BluetoothSnoopLogPreferenceController(context));
        controllers.add(new OemUnlockPreferenceController(context, activity, fragment));
        controllers.add(new FileEncryptionPreferenceController(context));
        controllers.add(new PictureColorModePreferenceController(context, lifecycle));
        controllers.add(new WebViewAppPreferenceController(context));
        controllers.add(new CoolColorTemperaturePreferenceController(context));
        controllers.add(new DisableAutomaticUpdatesPreferenceController(context));
        controllers.add(new AdbPreferenceController(context, fragment));
        controllers.add(new ClearAdbKeysPreferenceController(context, fragment));
        controllers.add(new LocalTerminalPreferenceController(context));
        controllers.add(new BugReportInPowerPreferenceController(context));
        controllers.add(new MockLocationAppPreferenceController(context, fragment));
        controllers.add(new DebugViewAttributesPreferenceController(context));
        controllers.add(new SelectDebugAppPreferenceController(context, fragment));
        controllers.add(new WaitForDebuggerPreferenceController(context));
        controllers.add(new EnableGpuDebugLayersPreferenceController(context));
        controllers.add(new VerifyAppsOverUsbPreferenceController(context));
        controllers.add(new LogdSizePreferenceController(context));
        controllers.add(new LogPersistPreferenceController(context, fragment, lifecycle));
        controllers.add(new CameraLaserSensorPreferenceController(context));
        ///add by wangxing P_CENTAURUS_E-32 Wifi Log start
        controllers.add(new WifiLogEnablePreferenceController(context));
        controllers.add(new DefaultLaunchPreferenceController(context,"wifi_log_settings"));

        ///add by wangxing P_CENTAURUS_E-32 Wifi Log end
        
        controllers.add(new WifiDisplayCertificationPreferenceController(context));
        controllers.add(new WifiCoverageExtendPreferenceController(context));
        controllers.add(new WifiVerboseLoggingPreferenceController(context));
        controllers.add(new WifiConnectedMacRandomizationPreferenceController(context));
        controllers.add(new MobileDataAlwaysOnPreferenceController(context));
        controllers.add(new TetheringHardwareAccelPreferenceController(context));
        controllers.add(new BluetoothDeviceNoNamePreferenceController(context));
        controllers.add(new BluetoothAbsoluteVolumePreferenceController(context));
        controllers.add(new BluetoothAvrcpVersionPreferenceController(context));
        controllers.add(new BluetoothA2dpHwOffloadPreferenceController(context, fragment));
        controllers.add(new BluetoothAudioCodecPreferenceController(context, lifecycle,
                bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioSampleRatePreferenceController(context, lifecycle,
                bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioBitsPerSamplePreferenceController(context, lifecycle,
                bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioChannelModePreferenceController(context, lifecycle,
                bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioQualityPreferenceController(context, lifecycle,
                bluetoothA2dpConfigStore));
        controllers.add(new BluetoothMaxConnectedAudioDevicesPreferenceController(context));
        controllers.add(new NfcPowerPreferenceController(context));
        controllers.add(new ShowTapsPreferenceController(context));
        controllers.add(new PointerLocationPreferenceController(context));
        controllers.add(new ShowSurfaceUpdatesPreferenceController(context));
        controllers.add(new ShowLayoutBoundsPreferenceController(context));
        controllers.add(new RtlLayoutPreferenceController(context));
        controllers.add(new WindowAnimationScalePreferenceController(context));
        controllers.add(new EmulateDisplayCutoutPreferenceController(context));
        controllers.add(new TransitionAnimationScalePreferenceController(context));
        controllers.add(new AnimatorDurationScalePreferenceController(context));
        controllers.add(new SecondaryDisplayPreferenceController(context));
        controllers.add(new ForceGpuRenderingPreferenceController(context));
        controllers.add(new GpuViewUpdatesPreferenceController(context));
        controllers.add(new HardwareLayersUpdatesPreferenceController(context));
        controllers.add(new DebugGpuOverdrawPreferenceController(context));
        controllers.add(new DebugNonRectClipOperationsPreferenceController(context));
        controllers.add(new ForceMSAAPreferenceController(context));
        controllers.add(new HardwareOverlaysPreferenceController(context));
        controllers.add(new SimulateColorSpacePreferenceController(context));
        controllers.add(new UsbAudioRoutingPreferenceController(context));
        controllers.add(new StrictModePreferenceController(context));
        controllers.add(new ProfileGpuRenderingPreferenceController(context));
        controllers.add(new KeepActivitiesPreferenceController(context));
        controllers.add(new BackgroundProcessLimitPreferenceController(context));
        controllers.add(new ShowFirstCrashDialogPreferenceController(context));
        controllers.add(new AppsNotRespondingPreferenceController(context));
        controllers.add(new NotificationChannelWarningsPreferenceController(context));
        controllers.add(new AllowAppsOnExternalPreferenceController(context));
        controllers.add(new ResizableActivityPreferenceController(context));
        controllers.add(new FreeformWindowsPreferenceController(context));
        controllers.add(new ShortcutManagerThrottlingPreferenceController(context));
        controllers.add(new EnableGnssRawMeasFullTrackingPreferenceController(context));
        controllers.add(new DefaultLaunchPreferenceController(context, "running_apps"));
        controllers.add(new DefaultLaunchPreferenceController(context, "demo_mode"));
        controllers.add(new DefaultLaunchPreferenceController(context, "quick_settings_tiles"));
        controllers.add(new DefaultLaunchPreferenceController(context, "feature_flags_dashboard"));
        controllers.add(
            new DefaultLaunchPreferenceController(context, "default_usb_configuration"));
        controllers.add(new DefaultLaunchPreferenceController(context, "density"));
        controllers.add(new DefaultLaunchPreferenceController(context, "background_check"));
        controllers.add(new DefaultLaunchPreferenceController(context, "inactive_apps"));
        return controllers;
    }

    //<!-- add by zoujiacai for E-11 start
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (QCT_DLT_KEY.equals(preference.getKey())) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.qualcomm.qct.dlt");
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            return false;
        }else if (QTI_LOGKIT_KEY.equals(preference.getKey())) {//add by wangxing for qti logkit
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.qualcomm.qti.logkit.lite");
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            return false;
        } else if (preference.getKey().equals(HOST_LOGGER_KEY)) {
            /*
            Intent intent = new Intent("android.provider.Telephony.SECRET_CODE",
                    Uri.parse("android_secret_code://" + "325"));
            getActivity().getApplicationContext().sendBroadcast(intent);
            */
                       Intent intent = new Intent();
            intent.setClassName("com.cipherlab.logger", "com.cipherlab.logger.LoggerRootActivity");
            startActivity(intent);
        }else if (FUNCTION_TEST_KEY.equals(preference.getKey())) {
            // modify by maohaojie on 2019.06.19 for bug 1775 start
			Intent intent = new Intent();
            intent.setClassName("com.meigsmart.meigrs32","com.meigsmart.meigrs32.activity.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
			// modify by maohaojie on 2019.06.19 for bug 1775 end
		  //add by maohaojie on 2019.07.22 for bug 1151 start
        }else if(LCDM_BRIGHT_TEST_KEY.equals(preference.getKey())){
			Intent intent = new Intent();
            intent.setClassName("com.android.settings","com.android.settings.development.Lcdm_Bright_Test");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
			//add by maohaojie on 2019.07.22 for bug 1151 end
		}
        return super.onPreferenceTreeClick(preference);
    }
    //<!-- add by zoujiacai for E-11 end

    @VisibleForTesting
    <T extends AbstractPreferenceController> T getDevelopmentOptionsController(Class<T> clazz) {
        return use(clazz);
    }

    /**
     * For Search.
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(context);
                }

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {

                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.development_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(Context
                        context) {
                    return buildPreferenceControllers(context, null /* activity */,
                            null /* lifecycle */, null /* devOptionsDashboardFragment */,
                            null /* bluetoothA2dpConfigStore */);
                }
            };
}
