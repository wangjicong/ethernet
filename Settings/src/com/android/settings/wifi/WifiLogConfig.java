/**
 *@file: WifiLogConfig.java
 *@author: liujiangang
 *@bugID: 16225
 *@version: SLB767
 *@date: 2018-07-26
 *@brief: Display WLAN log Enabled item
 */

package com.android.settings.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import android.util.Slog;
import java.lang.ProcessBuilder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
/**
 * Wifi information menu item on the diagnostic screen
 */
public class WifiLogConfig{


  private final String STORE_NAME = "WifiLogConfig";
  private final int STORE_MODE = Context.MODE_PRIVATE;
  
  public static boolean WIFILOG = true;
  private final int SET_WIFILOG_ENABLE = 1;
  private final int SET_WIFILOG_LEVEL = 2;
  //def
  private Context mContext;
  private SharedPreferences mStore;
  public int WMI = 255;
  public int HDD = 255;
  public int SME = 255;
  public int PE = 255;
  public int WMA = 255;
  public int SYS = 255;
  public int QDF = 255;
  public int SAP = 255;
  public int HDD_SOFTAP = 255;
  public int HDD_DATA = 255;  
  public int HDD_SAP_DATA = 255;
  public int HIF = 255;  
  public int HTC = 255;
  public int TXRX = 255;
  public int QDF_DEVICES = 255;
  public int CFG = 255;
  public int BMI = 255;
  public int EPPING = 255;


  public static final String CFG_FILE = "/data/vendor/wifi/WCNSS_qcom_cfg.ini";
  public static final String CFG_QDF_TRACE_ENABLE_WDI_NAME = "qdf_trace_enable_wdi";
  public static final String CFG_QDF_TRACE_ENABLE_HDD_NAME = "qdf_trace_enable_hdd";
  public static final String CFG_QDF_TRACE_ENABLE_SME_NAME = "qdf_trace_enable_sme";
  public static final String CFG_QDF_TRACE_ENABLE_PE_NAME = "qdf_trace_enable_pe";
  public static final String CFG_QDF_TRACE_ENABLE_WMA_NAME = "qdf_trace_enable_wma";
  public static final String CFG_QDF_TRACE_ENABLE_SYS_NAME = "qdf_trace_enable_sys";
  public static final String CFG_QDF_TRACE_ENABLE_QDF_NAME = "qdf_trace_enable_qdf";
  public static final String CFG_QDF_TRACE_ENABLE_SAP_NAME = "qdf_trace_enable_sap";
  public static final String CFG_QDF_TRACE_ENABLE_HDD_SAP_NAME = "qdf_trace_enable_hdd_sap";
  public static final String CFG_QDF_TRACE_ENABLE_BMI_NAME = "qdf_trace_enable_bmi";
  public static final String CFG_QDF_TRACE_ENABLE_CFG_NAME = "qdf_trace_enable_cfg";
  public static final String CFG_QDF_TRACE_ENABLE_EPPING = "qdf_trace_enable_epping";
  public static final String CFG_QDF_TRACE_ENABLE_QDF_DEVICES = "qdf_trace_enable_qdf_devices";
  public static final String CFG_QDF_TRACE_ENABLE_TXRX_NAME = "cfd_trace_enable_txrx";
  public static final String CFG_QDF_TRACE_ENABLE_HTC_NAME = "qdf_trace_enable_htc";
  public static final String CFG_QDF_TRACE_ENABLE_HIF_NAME = "qdf_trace_enable_hif";
  public static final String CFG_CDR_TRACE_ENABLE_HDD_SAP_DATA_NAME = "qdf_trace_enable_hdd_sap_data";
  public static final String CFG_QDF_TRACE_ENABLE_HDD_DATA_NAME = "qdf_trace_enable_hdd_data";
  

  public static final String WMI_KEY = "WMI";
  public static final String HDD_KEY = "HDD";
  public static final String SME_KEY = "SME";
  public static final String PE_KEY = "PE";
  public static final String WMA_KEY = "WMA";
  public static final String SYS_KEY = "SYS";
  public static final String QDF_KEY = "QDF";
  public static final String SAP_KEY = "SAP";
  public static final String HDD_SOFTAP_KEY = "HDD_SOFTAP";
  public static final String HDD_DATA_KEY = "HDD_DATA";
  public static final String HDD_SAP_DATA_KEY = "HDD_SAP_DATA";
  public static final String HIF_KEY = "HIF";
  public static final String HTC_KEY = "HTC";
  public static final String TXRX_KEY = "TXRX";                                            
  public static final String QDF_DEVICES_KEY = "QDF_DEVICE";                                    
  public static final String CFG_KEY = "CFG";                                            
  public static final String BMI_KEY = "BMI";                                              
  public static final String EPPING_KEY = "EPPING";

  public static final String WIFILOG_KEY = "wifi_log";

  private HandlerThread handlerThread;
  private Handler mHandler;


  public WifiLogConfig(Context context){
    mContext = context;
    init();
    handlerThread = new HandlerThread("WifiLog");
    handlerThread.start();
    mHandler = new Handler(handlerThread.getLooper()){
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
          case SET_WIFILOG_ENABLE:
            writeWifiLogEnable();
            break;
          case SET_WIFILOG_LEVEL:
            String file = CFG_FILE;
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_WDI_NAME, CFG_QDF_TRACE_ENABLE_WDI_NAME, Integer.toString(getIntValue(WMI_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_HDD_NAME, CFG_QDF_TRACE_ENABLE_HDD_NAME, Integer.toString(getIntValue(HDD_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_SME_NAME, CFG_QDF_TRACE_ENABLE_SME_NAME, Integer.toString(getIntValue(SME_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_PE_NAME, CFG_QDF_TRACE_ENABLE_PE_NAME, Integer.toString(getIntValue(PE_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_WMA_NAME, CFG_QDF_TRACE_ENABLE_WMA_NAME, Integer.toString(getIntValue(WMA_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_SYS_NAME, CFG_QDF_TRACE_ENABLE_SYS_NAME, Integer.toString(getIntValue(SYS_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_QDF_NAME, CFG_QDF_TRACE_ENABLE_QDF_NAME, Integer.toString(getIntValue(QDF_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_SAP_NAME, CFG_QDF_TRACE_ENABLE_SAP_NAME, Integer.toString(getIntValue(SAP_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_HDD_SAP_NAME, CFG_QDF_TRACE_ENABLE_HDD_SAP_NAME,Integer.toString(getIntValue(HDD_SOFTAP_KEY)));
            writeCfgValue(file, CFG_CDR_TRACE_ENABLE_HDD_SAP_DATA_NAME, CFG_CDR_TRACE_ENABLE_HDD_SAP_DATA_NAME, Integer.toString(getIntValue(HDD_SAP_DATA_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_HDD_DATA_NAME, CFG_QDF_TRACE_ENABLE_HDD_DATA_NAME,Integer.toString(getIntValue(HDD_DATA_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_BMI_NAME, CFG_QDF_TRACE_ENABLE_BMI_NAME, Integer.toString(getIntValue(BMI_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_CFG_NAME, CFG_QDF_TRACE_ENABLE_CFG_NAME, Integer.toString(getIntValue(CFG_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_HIF_NAME, CFG_QDF_TRACE_ENABLE_HIF_NAME, Integer.toString(getIntValue(HIF_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_HTC_NAME, CFG_QDF_TRACE_ENABLE_HTC_NAME, Integer.toString(getIntValue(HTC_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_TXRX_NAME, CFG_QDF_TRACE_ENABLE_TXRX_NAME, Integer.toString(getIntValue(TXRX_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_QDF_DEVICES, CFG_QDF_TRACE_ENABLE_QDF_DEVICES,Integer.toString(getIntValue(QDF_DEVICES_KEY)));
            writeCfgValue(file, CFG_QDF_TRACE_ENABLE_EPPING, CFG_QDF_TRACE_ENABLE_EPPING, Integer.toString(getIntValue(EPPING_KEY)));
            break;
        }
      }
    };
  }

  private void init(){
    SharedPreferences store = getShare();
    SharedPreferences.Editor editor = store.edit();
     
        
    if(!store.contains(WIFILOG_KEY))
      editor.putInt(WIFILOG_KEY,WIFILOG?1:0);
    editor.commit();
  }

  private SharedPreferences getShare(){
    if(mStore==null)	mStore= mContext.getSharedPreferences(STORE_NAME,STORE_MODE);
    return mStore;
  }

  public void setValue(String name , int value){
  	
      switch(name){
          case WMI_KEY:
              WMI = value;
              return ;
          case HDD_KEY:
              HDD = value;
              return ;
          case SME_KEY:
              SME = value;
              return ;
          case PE_KEY:
              PE = value;
              return ;
          case WMA_KEY:
              WMA = value;
              return ;
          case SYS_KEY:
              SYS = value;
              return ;
          case QDF_KEY:
              QDF = value;
              return ;
          case SAP_KEY:
              SAP = value;
              return ;
          case HDD_SOFTAP_KEY:
              HDD_SOFTAP = value;
              return ;
          case HDD_DATA_KEY:
              HDD_DATA = value;
              return ;
          case HDD_SAP_DATA_KEY:
              HDD_SAP_DATA = value;
              return ;
          case HIF_KEY:
              HIF = value;
              return ;
          case HTC_KEY:
              HTC = value;
              return ;
          case TXRX_KEY:
              TXRX = value;
              return ;
          case QDF_DEVICES_KEY:
              QDF_DEVICES = value;
              return ;             
          case CFG_KEY:
              CFG = value;
              return ;
          case BMI_KEY:
              BMI = value;
              return ;
          case EPPING_KEY:
              EPPING = value;
              return ;            
              
      }
  	
      SharedPreferences store = getShare();
      SharedPreferences.Editor editor = store.edit();
      editor.putInt(name,value);
      editor.commit();
  }

  public int getIntValue(String name){
      SharedPreferences store = getShare();     
      switch(name){
          case WMI_KEY:
              return WMI;
          case HDD_KEY:
              return HDD;
          case SME_KEY:
              return SME;
          case PE_KEY:
              return PE;
          case WMA_KEY:
              return WMA;
          case SYS_KEY:
              return SYS;
          case QDF_KEY:
              return QDF;
          case SAP_KEY:
              return SAP;
          case HDD_SOFTAP_KEY:
              return HDD_SOFTAP;
          case HDD_DATA_KEY:
              return HDD_DATA;
          case HDD_SAP_DATA_KEY:
              return HDD_SAP_DATA;
          case HIF_KEY:
              return HIF;
          case HTC_KEY:
              return HTC;
          case TXRX_KEY:
              return TXRX;
          case QDF_DEVICES_KEY:
              return QDF_DEVICES;             
          case CFG_KEY:
              return CFG;
          case BMI_KEY:
              return BMI;
          case EPPING_KEY:
              return EPPING;            
              
  	  }
  	  if(!store.contains(name)){
          return -1;
      }
      return store.getInt(name,255);
  }
  
  public void setConf(boolean value) {
    WIFILOG = value;
    mHandler.sendMessage(mHandler.obtainMessage(SET_WIFILOG_ENABLE));
  }
  public void setLogLevel(){
    mHandler.sendMessage(mHandler.obtainMessage(SET_WIFILOG_LEVEL));
  }
    private synchronized void writeCfgValue(String file, String section, String variable, String value){
      String fileContent, allLine, strLine, newLine, remarkStr = "";
      String getValue = null;
      BufferedReader bufferedReader = null;
     try{
       bufferedReader = new BufferedReader(new FileReader(file));
       boolean isInSection = false;
       boolean canAdd = true;
       fileContent = "";

        while ((allLine = bufferedReader.readLine()) != null) {
          allLine = allLine.trim();
          strLine = allLine.split(";")[0];
          Pattern p;
          Matcher m;
         /* p = Pattern.compile("\\[\\w+]");
          m = p.matcher((strLine));
          if (m.matches()) {
            p = Pattern.compile("\\[" + section + "\\]");
            m = p.matcher(allLine);*/
            if (strLine.contains(section)) {
              isInSection = true;
            } else {
              isInSection = false;
            }
         // }
          if (isInSection == true) {
            strLine = strLine.trim();
            String[] strArray = strLine.split("=");
            getValue = strArray[0].trim();
            if (getValue.equalsIgnoreCase(variable)) {
	            newLine = getValue + "=" + value;
              fileContent += newLine;
              while ((allLine = bufferedReader.readLine()) != null) {
                if(!strLine.equals("END")){
		              fileContent += "\r\n" + allLine;
		            }
              }
              bufferedReader.close();
              canAdd = false;
              System.out.println(fileContent);
              BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
              bufferedWriter.write(fileContent);
              bufferedWriter.flush();
              bufferedWriter.close();

            }

          }
	        if(strLine!=null&&!strLine.equals("END")){
            fileContent += allLine + "\r\n";
	        }
        }
        if (canAdd) {
          String str = "";
          if(variable.equals("qdf_trace_enable_epping")){
            str = variable + "=" + value+"\r\n"+"END";
          }else {
            str = variable + "=" + value;
          }
	        fileContent += str + "\r\n";
          System.out.println(fileContent);
          BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
          bufferedWriter.write(fileContent);
          bufferedWriter.flush();
          bufferedWriter.close();
        }
      } catch (IOException ex) {
        ex.fillInStackTrace();
	      Slog.d("wifiread",ex.toString());
      }finally{
	      try{
	      	if(bufferedReader != null){
	          bufferedReader.close();
	        }
        }catch(IOException ex){
	        ex.fillInStackTrace();
	      }
      }
    }
   private synchronized void writeWifiLogEnable(){
          int mark = 0;
          if(WIFILOG){
            mark = 1;
          }
          String string = "LOG_PATH_FLAG = "+mark+"\n"
                  +"MAX_LOG_FILE_SIZE = 60"+"\n"
                  +"MAX_ARCHIVES = 2"+"\n"
                  +"MAX_PKTLOG_ARCHIVES = 4"+"\n"
                  +"LOG_STORAGE_PATH = /sdcard/wlan_logs/"+"\n"
                  +"AVAILABLE_MEMORY_THRESHOLD = 100"+"\n"
                  +"MAX_LOG_BUFFER = 2"+"\n"
                  +"MAX_PKTLOG_BUFFER = 10"+"\n"
                  +"HOST_LOG_FILE = /sdcard/wlan_logs/buffered_cnsshost_log.txt"+"\n"
                  +"FIRMWARE_LOG_FILE = /sdcard/wlan_logs/buffered_cnssfw_log.txt";

          File file = new File("/data/vendor/wifi/cnss_diag.conf");
          if (!file.exists()) {
            try {
              file.createNewFile();
            } catch (IOException e) {
              e.fillInStackTrace();
            }
          }

          PrintWriter ini = null;
          try {
            ini = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            ini.print(string);
            ini.flush();
            ini.close();
          } catch (Exception e) {
           e.fillInStackTrace();
           Slog.d("wifiread","enable: "+e.toString());
         }
    }
    

  
    
    private void initCfgValue(String strLine){        
        strLine = strLine.trim();
        String[] strArray = strLine.split("=");
        String name;
        String value;
        
        int intValue = -1;
        if(strArray.length == 2){
            name = strArray[0].trim();
            value = strArray[1].trim();
            try {
                intValue = Integer.parseInt(value);
            }catch (Exception e){
            }
            if( intValue > 255 || intValue < 0 ){
               return;
            }
            if (name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_WDI_NAME)) {
                WMI = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_HDD_NAME)){
                HDD = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_SME_NAME)){
                SME = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_PE_NAME)){
                PE = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_WMA_NAME)){
                WMA = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_SYS_NAME)){
                SYS = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_QDF_NAME)){
                QDF = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_SAP_NAME)){
                SAP = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_HDD_SAP_NAME)){
                HDD_SOFTAP = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_BMI_NAME)){
                BMI = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_CFG_NAME)){
                CFG = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_EPPING)){
                EPPING = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_QDF_DEVICES)){
                QDF_DEVICES = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_TXRX_NAME)){
                TXRX = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_HTC_NAME)){
                HTC = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_HIF_NAME)){
                HIF = intValue;
            }else if(name.equalsIgnoreCase(CFG_CDR_TRACE_ENABLE_HDD_SAP_DATA_NAME)){
                HDD_SAP_DATA = intValue;
            }else if(name.equalsIgnoreCase(CFG_QDF_TRACE_ENABLE_HDD_DATA_NAME)){
                HDD_DATA = intValue;
            }
        } 
    }


      
    private boolean isTraceCfgLine(String strLine){
        if(strLine.contains(CFG_QDF_TRACE_ENABLE_WDI_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_HDD_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_SME_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_PE_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_WMA_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_SYS_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_QDF_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_SAP_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_HDD_SAP_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_BMI_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_CFG_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_EPPING)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_QDF_DEVICES)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_TXRX_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_HTC_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_HIF_NAME)||
           strLine.contains(CFG_CDR_TRACE_ENABLE_HDD_SAP_DATA_NAME)||
           strLine.contains(CFG_QDF_TRACE_ENABLE_HDD_DATA_NAME)
           ){
           	
            return true;
        }else{
            return false;
        }
    }
    public synchronized void readCfgInit(){
        String allLine, strLine = "";
        String getValue = null;
        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new FileReader(CFG_FILE));
            boolean isInSection = false;
            while ((allLine = bufferedReader.readLine()) != null) {
                allLine = allLine.trim();
                strLine = allLine.split(";")[0];
                if (isTraceCfgLine(strLine)) { 
                    initCfgValue(strLine);
                }
            }
        } catch (IOException ex) {
            ex.fillInStackTrace();
            Slog.d("wifiread",ex.toString());
        }finally{
            try{
                if(bufferedReader != null){
                    bufferedReader.close();
                }
            }catch(IOException ex){
                ex.fillInStackTrace();
            }
        }
    }
    
}

