/**
 *@file: EthernetConfig.java
 *@author: liujiangang
 *@bugID: 16249 16250 16251
 *@version: SLB767
 *@date: 2018-07-14
 *@brief: EthernetSetting item UI display
 */
package com.android.settings.ethernet;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.annotation.Nullable;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.util.Slog;
import com.android.settings.ethernet.EthernetConfig;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Toast;

public class EthernetConfigDialog extends Activity{

    private Spinner proxy_spinner;
    private Spinner ip_spinner;
    private LinearLayout proxy_layout;
    private LinearLayout ip_layout;
    private View mView;
    private AlertDialog mDialog;
    private LayoutInflater mLayoutInflater;
    private EthernetConfig mEthernetConfig;
    private int proxy_mode = 0;
    private int Ip_mode = 0;
    private EditText hostname;
    private EditText port;
    private EditText bypass;
    private EditText address;
    private EditText gateway;
    private EditText prefix;
    private EditText dns1;
    private EditText dns2;
    private final String NOTSET = "Not set";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.ethernet_configuration_summary)
                .setView(R.layout.ethernet_configuration)
                .setPositiveButton(R.string.ethernet_configuration_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                    }
                }).setNegativeButton(R.string.ethernet_configuration_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }).create();
        mDialog.show();
//	mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                save();
//            }
//        });

        proxy_spinner = (Spinner) mDialog.findViewById(R.id.configuration_proxy_spinner);
        ip_spinner = (Spinner) mDialog.findViewById(R.id.configuration_ip_spinner);
        proxy_layout = (LinearLayout) mDialog.findViewById(R.id.proxy_layout);
        ip_layout = (LinearLayout) mDialog.findViewById(R.id.ip_settings_layout);
        proxy_spinner.setOnItemSelectedListener(new OnItemSelectedListenerImpl());
        ip_spinner.setOnItemSelectedListener(new OnItemSelectedListenerImpl());

        hostname = (EditText)proxy_layout.findViewById(R.id.proxy_hostname_edit);
        port = (EditText)proxy_layout.findViewById(R.id.proxy_port_edit);
        bypass = (EditText)proxy_layout.findViewById(R.id.proxy_bypass_edit);
        address = (EditText)ip_layout.findViewById(R.id.ip_address_edit);
        gateway = (EditText)ip_layout.findViewById(R.id.ip_gateway_edit);
        prefix = (EditText)ip_layout.findViewById(R.id.ip_prefix_edit);
        dns1 = (EditText)ip_layout.findViewById(R.id.ip_dns1_edit);
        dns2 = (EditText)ip_layout.findViewById(R.id.ip_dns2_edit);
        mEthernetConfig = new EthernetConfig(this);

        update();
    }
    private void update(){
        int prox_mode = mEthernetConfig.getProxyMode();
        int ip_mode = mEthernetConfig.getIpMode();
        if(prox_mode == 0){
            proxy_spinner.setSelection(0,true);
            proxy_layout.setVisibility(View.GONE);
        }else if(prox_mode == 1){
            proxy_layout.setVisibility(View.VISIBLE);
            proxy_spinner.setSelection(1,true);
            updateProxyText();
        }
        if(ip_mode == 0){
            proxy_layout.setVisibility(View.GONE);
            ip_spinner.setSelection(0,true);
        }else if(ip_mode == 1){
            ip_spinner.setSelection(1,true);
            proxy_layout.setVisibility(View.VISIBLE);
            updateIpText();
        }
    }
    private void save() {
        Slog.d("EthernetConfigDialog","save()");
        if (proxy_mode == 0) {
            mEthernetConfig.saveProxy(0);
        } else {
            saveProxy();
        }
        if (Ip_mode == 0) {
            mEthernetConfig.saveIPsettings(0);
        } else {
            if(TextUtils.isEmpty(address.getText().toString())
                    || TextUtils.isEmpty(gateway.getText().toString())
                    || TextUtils.isEmpty(prefix.getText().toString())
                    || (TextUtils.isEmpty(dns1.getText().toString()) && TextUtils.isEmpty(dns2.getText().toString()))) {
                Toast.makeText(this, R.string.eth_settings_empty, Toast.LENGTH_SHORT).show();
                return;
           } else if ((!isIpAddress(address.getText().toString())&&!TextUtils.isEmpty(address.getText().toString()))
                    || (!isIpAddress(gateway.getText().toString())&&!TextUtils.isEmpty(gateway.getText().toString()))
                    || (!isIpAddress(prefix.getText().toString())&&!TextUtils.isEmpty(prefix.getText().toString()))
                    || (!isIpAddress(dns1.getText().toString())&&!TextUtils.isEmpty(dns1.getText().toString()))
                    || (!isIpAddress(dns2.getText().toString())&&!TextUtils.isEmpty(dns2.getText().toString()))) {
                Toast.makeText(this, R.string.eth_settings_error, Toast.LENGTH_LONG).show();
                return;
            } else if (!isIpAddress(address.getText().toString())
                    || !isIpAddress(gateway.getText().toString())
                    || !isIpAddress(prefix.getText().toString())
                    || !(isIpAddress(dns1.getText().toString()) || isIpAddress(dns2.getText().toString()))) {
                Toast.makeText(this, R.string.eth_settings_not_complete, Toast.LENGTH_SHORT).show();
                return;
            }else if (isIpAddress(address.getText().toString())
                    && isIpAddress(gateway.getText().toString())
                    && isIpAddress(prefix.getText().toString())
                    && (isIpAddress(dns1.getText().toString()) || isIpAddress(dns2.getText().toString()))) {
                    saveIP();
            } else {
                Toast.makeText(this, R.string.eth_settings_error, Toast.LENGTH_LONG).show();
                return;
            }
        }
        mEthernetConfig.setEthernetProxy();
        mEthernetConfig.setEthernet();
        finish();
    }

    private boolean isIpAddress(String value) {
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;

        while (start < value.length()) {
            if (end == -1) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            numBlocks++;

            start = end + 1;
            end = value.indexOf('.', start);
        }
        return numBlocks == 4;
    }
    private class OnItemSelectedListenerImpl implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Slog.d("etdia",""+view.getId()+"  "+position+"  "+id);
            switch (adapterView.getId()){
                case R.id.configuration_proxy_spinner:
                    if(position == 1){
                        proxy_layout.setVisibility(View.VISIBLE);
                        proxy_mode = 1;
                    }else if(position == 0){
                        Slog.d("etdia",""+position);
                        proxy_layout.setVisibility(View.GONE);
                        proxy_mode = 0;
                    }
                    break;
                case R.id.configuration_ip_spinner:
                    if(position == 1){
                        Slog.d("etdia",""+position);
                        ip_layout.setVisibility(View.VISIBLE);
                        Ip_mode = 1;
                    }else if(position == 0){
                        Slog.d("etdia",""+position);
                        ip_layout.setVisibility(View.GONE);
                        Ip_mode = 0;
                    }
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }
    private void updateProxyText(){
        String hostnametext = mEthernetConfig.getHostname();
        int porttext = mEthernetConfig.getPort();
        String bypasstext = mEthernetConfig.getBypass();
        if(!hostnametext.equals(NOTSET)) {
            hostname.setText(hostnametext);
        }
        if(!(porttext == -1)) {
            port.setText(porttext+"");
        }
        if(!bypasstext.equals(NOTSET)) {
            bypass.setText(bypasstext);
        }

    }
    private void updateIpText(){

        String addresstext = mEthernetConfig.getAddress();
        String gatewaytext = mEthernetConfig.getGateway();
        String prefixtext = mEthernetConfig.getPrefix();
        String dns1text = mEthernetConfig.getDns1();
        String dns2text = mEthernetConfig.getDns2();
        address.setEnabled(true);
        gateway.setEnabled(true);
        prefix.setEnabled(true);
        dns1.setEnabled(true);
        dns2.setEnabled(true);
        if(!addresstext.equals(NOTSET)) {
            address.setText(addresstext);
        }
        if(!gatewaytext.equals(NOTSET)) {
            gateway.setText(gatewaytext);
        }
        if(!prefix.equals(NOTSET)) {
            prefix.setText(prefixtext);
        }
        if(!dns1text.equals(NOTSET)) {
            dns1.setText(dns1text);
        }
        if(!dns2text.equals(NOTSET)) {
            dns2.setText(dns2text);
        }

    }
    private void saveProxy(){
        String proxy_hostname = hostname.getText().toString();
        String c_port = port.getText().toString();
        int proxy_port = -1;
        if(!c_port.equals("")){
            proxy_port = Integer.parseInt(c_port);
        }
        String proxy_bypass = bypass.getText().toString();

        mEthernetConfig.saveProxy(1,proxy_hostname,proxy_port,proxy_bypass);

    }
    private void saveIP(){
        String ip_address = address.getText().toString();
        String ip_prefix = prefix.getText().toString();
        String ip_gateway = gateway.getText().toString();
        String ip_dns1 = dns1.getText().toString();
        String ip_dns2 = dns2.getText().toString();

        mEthernetConfig.saveIPsettings(1,ip_address,ip_gateway,ip_prefix,ip_dns1,ip_dns2);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    } 
}
