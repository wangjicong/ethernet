/**
 *@file: EthernetConfig.java
 *@author: liujiangang
 *@bugID: 16249 16250 16251
 *@version: SLB767
 *@date: 2018-07-14
 *@brief: EthernetSetting item UI display
 */

package com.android.settings.ethernet;

import android.content.ContentValues;
import android.os.Handler;//meig:jicong.wang add
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.net.Uri;
import android.os.SystemProperties;
import android.content.Context;
import android.content.pm.PackageManager;

import android.bluetooth.BluetoothAdapter;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.StatusBarManager;
import android.app.ActivityManager;

import com.android.internal.view.RotationPolicy;
import android.content.ContentResolver;

import android.net.IpConfiguration;
import android.net.IpConfiguration.ProxySettings;
import android.net.IpConfiguration.IpAssignment;
import android.net.StaticIpConfiguration;
import android.net.EthernetManager;
import android.net.LinkAddress;
import java.net.InetAddress;
import android.net.RouteInfo;
import android.net.ProxyInfo;
import android.net.NetworkUtils;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.net.Inet4Address;
import android.text.TextUtils;
import java.net.UnknownHostException;

public class EthernetConfig {
    private static final String TAG = "leo.EthernetConfig";
    //def
    private static Context mContext;
    private ContentResolver mCR;
    private static EthernetStore mStore;
    private static final String NOTSET = "Not set";
    private final int noValue = -1;
    private EthernetManager mEthernetManager;
    private ConnectivityManager mConnectivityManager;
    private ProxyInfo mHttpProxy = null;
    /*meig:jicong.wang modify for bug P_RK95_CC_E-59 start {@*/
    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isEthernetEnable()) {
                mEthernetManager.updateIface("eth0", mStore.getIntValue(EthernetStore.KeyName.ETHERNET) == 1 ? true : false);
            }
        }
    };
    /*meig:jicong.wang modify for bug P_RK95_CC_E-59 end @}*/
    //add by wangxing@20191213 for bug P_RK95_E-793
    public static final String MEIG_ETHERNET_ENABLED = "meig_ethernet_enabled";

    public EthernetConfig(Context context){
        mContext = context;
        mStore = new EthernetStore(context);
        mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public synchronized void saveProxy(int proxymode,String hostname,int port,String bypass){
        mStore.setValue(EthernetStore.KeyName.PROXYMODE,proxymode);
        mStore.setValue(EthernetStore.KeyName.HOSTNAME,hostname);
        mStore.setValue(EthernetStore.KeyName.PORT,port);
        mStore.setValue(EthernetStore.KeyName.BYPASS,bypass);

    }
    public synchronized void saveProxy(int proxymode){
        mStore.setValue(EthernetStore.KeyName.PROXYMODE,proxymode);
        mStore.setValue(EthernetStore.KeyName.HOSTNAME,NOTSET);
        mStore.setValue(EthernetStore.KeyName.PORT,noValue);
        mStore.setValue(EthernetStore.KeyName.BYPASS,NOTSET);
    }

    public synchronized void saveIPsettings(int ipmode,String address,String gateway,String prefix,String dns1,String dns2){
        mStore.setValue(EthernetStore.KeyName.IPMODE,ipmode);
        mStore.setValue(EthernetStore.KeyName.ADDRESS,address);
        mStore.setValue(EthernetStore.KeyName.GATEWAY,gateway);
        mStore.setValue(EthernetStore.KeyName.PREFIX,prefix);
        mStore.setValue(EthernetStore.KeyName.DNS1,dns1);
        mStore.setValue(EthernetStore.KeyName.DNS2,dns2);
    }
    public synchronized void saveIPsettings(int ipmode){
        mStore.setValue(EthernetStore.KeyName.IPMODE,ipmode);
        mStore.setValue(EthernetStore.KeyName.ADDRESS,NOTSET);
        mStore.setValue(EthernetStore.KeyName.GATEWAY,NOTSET);
        mStore.setValue(EthernetStore.KeyName.PREFIX,noValue);
        mStore.setValue(EthernetStore.KeyName.DNS1,NOTSET);
        mStore.setValue(EthernetStore.KeyName.DNS2,NOTSET);
    }

    public void saveEtherMode(int value){
        //add by wangxing@20191213 for bug P_RK95_E-793 start
        Settings.Global.putInt(mContext.getContentResolver(),MEIG_ETHERNET_ENABLED, value);
        //add by wangxing@20191213 for bug P_RK95_E-793 end
        mStore.setValue(EthernetStore.KeyName.ETHERNET,value);
        /*meig:jicong.wang modify for crash start {@*/
        if (isEthernetEnable()) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable,1000);
        }
        /*meig:jicong.wang modify for crash end @}*/
    }

    public int getEtherMode(){
        return mStore.getIntValue(EthernetStore.KeyName.ETHERNET);
    }
    public int getProxyMode(){
        return mStore.getIntValue(EthernetStore.KeyName.PROXYMODE);
    }
    public int getIpMode(){
        return mStore.getIntValue(EthernetStore.KeyName.IPMODE);
    }
    public String getHostname(){
        return mStore.getStringValue(EthernetStore.KeyName.HOSTNAME);
    }
    public int getPort(){
        return mStore.getIntValue(EthernetStore.KeyName.PORT);
    }
    public String getBypass(){
        return mStore.getStringValue(EthernetStore.KeyName.BYPASS);
    }
    public String getAddress(){
        return mStore.getStringValue(EthernetStore.KeyName.ADDRESS);
    }
    public String getGateway(){
        return mStore.getStringValue(EthernetStore.KeyName.GATEWAY);
    }
    public String getPrefix(){
        return mStore.getStringValue(EthernetStore.KeyName.PREFIX);
    }
    public String getDns1(){
        return mStore.getStringValue(EthernetStore.KeyName.DNS1);
    }
    public String getDns2(){
        return mStore.getStringValue(EthernetStore.KeyName.DNS2);
    }

    public boolean isEthernetEnable(){
        return mEthernetManager.isAvailable();
    }
    public String getEthernetAddress(){
        int ip_mode = getIpMode();
        String address = "";
        try {
            if(ip_mode == 1){
                mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                StaticIpConfiguration mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();
                address = mStaticIpConfiguration.ipAddress.getAddress().getHostAddress();
            }
            if(ip_mode == 0) {
                LinkProperties lp = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);
                if (lp != null) {
                    address = formatIpAddresses(lp.getAllAddresses());
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            Slog.d("ethernetx","error: "+ex.toString());
        }
        if(address.equals("")){
            address = NOTSET;
        }
        return address;

    }
    public String getEthernetGateWay(){
        int ip_mode = getIpMode();
        String gateway = "";
        try {
            if(ip_mode == 1){
                mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                StaticIpConfiguration mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();
                gateway = mStaticIpConfiguration.gateway.getHostAddress();
            }
            if(ip_mode == 0) {
                LinkProperties lp = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);
                if (lp != null) {
                    List<InetAddress> gate = new ArrayList<InetAddress>();
                    for (RouteInfo s : lp.getAllRoutes()) {
                        gate.add(s.getGateway());
                    }
                    gateway = formatIpAddresses(gate);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            Slog.d("ethernetx","error: "+ex.toString());
        }
        if(gateway.equals("")){
            gateway = NOTSET;
        }
        return gateway;

    }
    public int getEthernetPrefix(){
        int ip_mode = getIpMode();
        int prefix = -1;
        try {
            if(ip_mode == 1){
                mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                StaticIpConfiguration mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();
                prefix = mStaticIpConfiguration.ipAddress.getPrefixLength();
            }
            if(ip_mode == 0) {
                LinkProperties lp = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);
                if (lp != null) {
                    List<LinkAddress> mLinkAddresses = lp.getLinkAddresses();
                    for (LinkAddress s : mLinkAddresses) {
                        prefix = s.getNetworkPrefixLength();
                    }
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            Slog.d("ethernetx","error: "+ex.toString());
        }
        return prefix;
    }
    public String getEthernetMask(){
        int ip_mode = getIpMode();
        int prefix = -1;
        try {
            if(ip_mode == 1){
                mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                StaticIpConfiguration mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();
                return mStaticIpConfiguration.domains;
            }
            if(ip_mode == 0) {
                LinkProperties lp = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);
                if (lp != null) {
                    List<LinkAddress> mLinkAddresses = lp.getLinkAddresses();
                    for (LinkAddress s : mLinkAddresses) {
                        prefix = s.getNetworkPrefixLength();
                    }
                }
                return calcMaskByPrefixLength(prefix);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            Slog.d("ethernetx","error: "+ex.toString());
        }
        return NOTSET;
    }
    public String getEthernetDns1(){
        int ip_mode = getIpMode();
        String dns1="";
        try {
            if (ip_mode == 1) {
                mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                StaticIpConfiguration mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();
                List<InetAddress> mDns = mStaticIpConfiguration.dnsServers;
                for(InetAddress t:mDns){
                    dns1 = t.getHostAddress();
                    return dns1;
                }
            }
            if (ip_mode == 0) {
                LinkProperties lp = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);
                if (lp != null) {
                    List<InetAddress> mDnses = lp.getDnsServers();
                    for (InetAddress s : mDnses) {
                        dns1 = s.getHostAddress();
                        return dns1;
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            Slog.d("ethernetx", "error: " + ex.toString());
        }
        if(dns1.equals("")){
            dns1 = NOTSET;
        }
        return dns1;
    }
    public String getEthernetDns2(){
        int ip_mode = getIpMode();
        String dns2 = "";
        try {
            if (ip_mode == 1) {
                mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                StaticIpConfiguration mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();
                List<InetAddress> mDns = mStaticIpConfiguration.dnsServers;
                for(InetAddress t:mDns){
                    dns2 = t.getHostAddress();
                }
            }
            if (ip_mode == 0) {
                LinkProperties lp = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);
                if (lp != null) {
                    List<InetAddress> mDnses = lp.getDnsServers();
                    for (InetAddress s : mDnses) {
                        dns2 = s.getHostAddress();
                    }
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            Slog.d("ethernetx","error: "+ex.toString());
        }
        if(dns2.equals("")){
            dns2 = NOTSET;
        }
        return dns2;
    }
    public synchronized void setEthernet(){
        mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        int ip_mode = getIpMode();
        if (ip_mode == 1) {

            String ip_address = getAddress();
            String ip_gateway = getGateway();
            String ip_prefix = getPrefix();
            String ip_dns1 = getDns1();
            String ip_dns2 = getDns2();

            try {
                
                StaticIpConfiguration mStaticIpConfiguration = new StaticIpConfiguration();
                IpConfiguration mIpConfiguration = new IpConfiguration();
                mStaticIpConfiguration.ipAddress = new LinkAddress(getIPv4Address(ip_address), 24);
                mStaticIpConfiguration.gateway = (InetAddress)getIPv4Address(ip_gateway);
                mStaticIpConfiguration.dnsServers.add((InetAddress)getIPv4Address(ip_dns1));
                if(!TextUtils.isEmpty(ip_dns2)){
                    mStaticIpConfiguration.dnsServers.add((InetAddress)getIPv4Address(ip_dns2));
                }
                /*meig:jicong.wang modify for bug P_RK95_CC_E-59 start {@*/
                if (null != mHttpProxy){
                    mEthernetManager.setConfiguration("eth0", new IpConfiguration(IpAssignment.STATIC, ProxySettings.STATIC, mStaticIpConfiguration, mHttpProxy));
                } else {
                    mEthernetManager.setConfiguration("eth0", new IpConfiguration(IpAssignment.STATIC, ProxySettings.NONE, mStaticIpConfiguration, mHttpProxy));
                }
                /*meig:jicong.wang modify for bug P_RK95_CC_E-59 end @}*/
            } catch (Exception ex) {
                ex.printStackTrace();
                Slog.d("ethernetx", "errorxxxxx: " + ex.toString());
            }
        }
        if(ip_mode == 0){
            mEthernetManager.setConfiguration("eth0", new IpConfiguration(IpAssignment.DHCP, ProxySettings.NONE,null,null));
        }
        /*meig:jicong.wang modify for bug P_RK95_CC_E-59 start {@*/
        mEthernetManager.updateIface("eth0",false);
        mHandler.postDelayed(mRunnable,100);
        /*meig:jicong.wang modify for bug P_RK95_CC_E-59 end @}*/
    }
    public synchronized void setEthernetProxy(){
        int proxy_mode = getProxyMode();
        if(proxy_mode == 1){
            List<String> mList = new ArrayList<String>();
            String host = getHostname();
            int port = getPort();
            String exclList = getBypass();
            mList.add(exclList);
            try {
                mHttpProxy = ProxyInfo.buildDirectProxy(host, port, mList);
                IpConfiguration mIpConfiguration = mEthernetManager.getConfiguration("eth0");
                mIpConfiguration.setHttpProxy(mHttpProxy);
            } catch (Exception ex) {
                ex.printStackTrace();
                Slog.d("ethernetx", "errorxxxxx: " + ex.toString());
            }
        }
    }
    private String formatIpAddresses(List<InetAddress> prop) {
        if (prop == null) return null;
        Iterator<InetAddress> iter = prop.iterator();
        if (!iter.hasNext()) return null;
        String addresses = "";
        while (iter.hasNext()) {
            addresses = iter.next().getHostAddress();
            //if (iter.hasNext()) addresses += "\n";
        }
        return addresses;
    }
    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }
    public static String calcMaskByPrefixLength(int length) {
        int mask = -1 << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }

    public static String calcSubnetAddress(String ip, String mask) {
        String result = "";
        try {
            // calc sub-net IP
            InetAddress ipAddress = InetAddress.getByName(ip);
            InetAddress maskAddress = InetAddress.getByName(mask);

            byte[] ipRaw = ipAddress.getAddress();
            byte[] maskRaw = maskAddress.getAddress();

            int unsignedByteFilter = 0x000000ff;
            int[] resultRaw = new int[ipRaw.length];
            for (int i = 0; i < resultRaw.length; i++) {
                resultRaw[i] = (ipRaw[i] & maskRaw[i] & unsignedByteFilter);
            }

            // make result string
            result = result + resultRaw[0];
            for (int i = 1; i < resultRaw.length; i++) {
                result = result + "." + resultRaw[i];
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return result;
    }
}
