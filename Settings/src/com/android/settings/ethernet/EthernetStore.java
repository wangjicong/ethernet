/**
 *@file: EthernetConfig.java
 *@author: liujiangang
 *@bugID: 16249 16250 16251
 *@version: SLB767
 *@date: 2018-07-14
 *@brief: EthernetSetting item UI display
 */

package com.android.settings.ethernet;

import android.util.Log;
import android.content.Context;
import android.provider.Settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.content.Intent;

public class EthernetStore {
    private static final String TAG = "leo.EthernetStore";
    private final String STORE_NAME = "EthernetStore";
    private final int STORE_MODE = Context.MODE_PRIVATE;

    //def
    private Context mContext;
    private SharedPreferences mStore;

    public static class KeyName{
        public static final int DEF_PROXYMODE =  0;
        public static final int DEF_IPMODE = 0;
        public static final int DEF_ETHERNET = 1;


        public static final String ETHERNET = "ethernet_mode";

        public static final String PROXYMODE = "proxy_mode";
        public static final String IPMODE = "ip_mode";
        public static final String HOSTNAME = "hostname";
        public static final String PORT = "port";
        public static final String BYPASS = "bypass";

        public static final String ADDRESS = "address";
        public static final String GATEWAY = "gateway";
        public static final String PREFIX = "prefix";
        public static final String DNS1 = "dns1";
        public static final String DNS2 = "dns2";
    }
    public EthernetStore(Context context){
        mContext = context;
        init();
    }

    private void init(){
        SharedPreferences store = getShare();
        Editor editor = store.edit();

        if(!store.contains(KeyName.ETHERNET))
            editor.putInt(KeyName.ETHERNET,KeyName.DEF_ETHERNET);
        if(!store.contains(KeyName.PROXYMODE))
            editor.putInt(KeyName.PROXYMODE,KeyName.DEF_PROXYMODE);
        if(!store.contains(KeyName.IPMODE))
            editor.putInt(KeyName.IPMODE,KeyName.DEF_IPMODE);
        editor.commit();
    }

    private SharedPreferences getShare(){
        if(mStore==null)	mStore= mContext.getSharedPreferences(STORE_NAME,STORE_MODE);
        return mStore;
    }

    protected void setValue(String name , int value){
        SharedPreferences store = getShare();
        Editor editor = store.edit();
        editor.putInt(name,value);
        editor.commit();
    }

    protected void setValue(String name , String value){
        SharedPreferences store = getShare();
        Editor editor = store.edit();
        editor.putString(name,value);
        editor.commit();
    }

    protected int getIntValue(String name){
        SharedPreferences store = getShare();
        if(!store.contains(name)){
            return -1;
        }
        return store.getInt(name,3);
    }

    protected String getStringValue(String name){
        SharedPreferences store = getShare();
        if(!store.contains(name)){
            return "Not set";
        }
        return store.getString(name,"Not set");
    }
}
