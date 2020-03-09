/**
 *@file: EthernetConfig.java
 *@author: liujiangang
 *@bugID: 16249 16250 16251
 *@version: SLB767
 *@date: 2018-07-14
 *@brief: EthernetSetting item UI display
 */

package com.android.settings.ethernet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.R;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.dream.DreamBackend.DreamInfo;
import com.android.settings.ethernet.EthernetConfig;
import android.net.EthernetManager;
import android.net.ConnectivityManager;
import java.util.List;

public class EthernetSettings extends SettingsPreferenceFragment implements
        SwitchBar.OnSwitchChangeListener {
    private static final String TAG = EthernetSettings.class.getSimpleName();
    static final boolean DEBUG = false;
    private static final int DIALOG_WHEN_TO_DREAM = 1;
    private static final String NOTSET = "Not set";
    private static final String PACKAGE_SCHEME = "package";

    private Context mContext;
    private SwitchBar mSwitchBar;
    private boolean mRefreshing;
    private boolean EthernetEnabled = false;
    private boolean mSwitchChanged = false;
    private EthernetConfig mEthernetConfig;

    private final String IP_TYPE_KEY = "toggle_ip_type";
    private final String IP_ADDRESS_KEY = "toggle_ip_address";
    private final String IP_GATEWAY_KEY = "toggle_gateway";
    private final String IP_PREFIX_KEY = "toggle_network_prefix_length";
    private final String IP_DNS1_KEY = "toggle_dns1";
    private final String IP_DNS2_KEY = "toggle_dns2";

    private Preference type;
    private Preference address;
    private Preference gateway;
    private Preference prefix;
    private Preference dns1;
    private Preference dns2;
    private BroadcastReceiver mConnectReceiver;

    @Override
    public int getHelpResource() {
        return R.string.help_url_dreams;
    }

    @Override
    public void onAttach(Activity activity) {
        logd("onAttach(%s)", activity.getClass().getSimpleName());
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DREAM;
    }

    @Override
    public void onCreate(Bundle icicle) {
        logd("onCreate(%s)", icicle);
        super.onCreate(icicle);

        mEthernetConfig = new EthernetConfig(getContext());
        EthernetEnabled = mEthernetConfig.isEthernetEnable();
        mConnectReceiver = new ConnectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mConnectReceiver,intentFilter);
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (!mRefreshing) {
            refreshFromBackend();
        }
        if(mSwitchChanged){
            mEthernetConfig.saveEtherMode(isChecked?1:0);
        }
    }

    @Override
    public void onStart() {
        logd("onStart()");
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        logd("onDestroyView()");
        super.onDestroyView();

        mSwitchBar.removeOnSwitchChangeListener(this);
        mSwitchBar.hide();
        getActivity().unregisterReceiver(mConnectReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        logd("onActivityCreated(%s)", savedInstanceState);
        super.onActivityCreated(savedInstanceState);

        TextView emptyView = (TextView) getView().findViewById(android.R.id.empty);
        emptyView.setText(R.string.ethernet_on_empty);
        setEmptyView(emptyView);

        final SettingsActivity sa = (SettingsActivity) getActivity();
        mSwitchBar = sa.getSwitchBar();
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.show();
        mSwitchBar.setChecked(mEthernetConfig.getEtherMode()==1);
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == DIALOG_WHEN_TO_DREAM) {
            return MetricsEvent.DIALOG_DREAM_START_DELAY;
        }
        return 0;
    }


    @Override
    public void onPause() {
        logd("onPause()");
        super.onPause();
        mSwitchChanged = false;
    }

    @Override
    public void onResume() {
        logd("onResume()");
        super.onResume();
        int EthernetMode = mEthernetConfig.getEtherMode();
        mSwitchBar.setChecked(EthernetMode == 1 ? true : false);
        refreshFromBackend();
        mSwitchChanged = true;
    }


    private void refreshFromBackend() {
        logd("refreshFromBackend()");
        mRefreshing = true;
        if (getPreferenceScreen() == null) {
            setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        }
        getPreferenceScreen().removeAll();
        if (mSwitchBar.isChecked()) {
            if(mEthernetConfig.isEthernetEnable()) {
                addPreferencesFromResource(R.xml.ethernet_data);
                updateText();
            }
        }

        mRefreshing = false;
    }

    private static void logd(String msg, Object... args) {
        if (DEBUG) Log.d(TAG, args == null || args.length == 0 ? msg : String.format(msg, args));
    }


    private class ConnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                refreshFromBackend();
            }
	}
    }
    private void updateText(){

        type = getPreferenceManager().findPreference(IP_TYPE_KEY);
        address = getPreferenceManager().findPreference(IP_ADDRESS_KEY);
        gateway = getPreferenceManager().findPreference(IP_GATEWAY_KEY);
        prefix = getPreferenceManager().findPreference(IP_PREFIX_KEY);
        dns1 = getPreferenceManager().findPreference(IP_DNS1_KEY);
        dns2 = getPreferenceManager().findPreference(IP_DNS2_KEY);
        
        if(type==null){
	   return;
	}
        int ip_mode = 0;
        String ip_address = "";
        String ip_gareway = "";
        String ip_prefix = "";
        String ip_dns1 = "";
        String ip_dns2 = "";

        ip_mode = mEthernetConfig.getIpMode();

        if(ip_mode == 1) {
            type.setSummary("Static");
            ip_address = mEthernetConfig.getAddress();
            ip_gareway = mEthernetConfig.getGateway();
            ip_prefix = mEthernetConfig.getPrefix();
            ip_dns1 = mEthernetConfig.getDns1();
            ip_dns2 = mEthernetConfig.getDns2();
        }
        if(ip_mode == 0){
            type.setSummary("DHCP");
            ip_address = mEthernetConfig.getEthernetAddress();
            ip_gareway = mEthernetConfig.getEthernetGateWay();
            ip_prefix = mEthernetConfig.getEthernetMask();
            ip_dns1 = mEthernetConfig.getEthernetDns1();
            ip_dns2 = mEthernetConfig.getEthernetDns2();
        }
        address.setSummary(ip_address);
        gateway.setSummary(ip_gareway);
        prefix.setSummary(ip_prefix);
        dns1.setSummary(ip_dns1);
        dns2.setSummary(ip_dns2);
    }
}
