/**
 *@file: WifiLogSettings.java
 *@author: liujiangang
 *@bugID: 16225
 *@version: SLB767
 *@date: 2018-07-26
 *@brief: Display WLAN log Enabled item
 */

package com.android.settings.wifi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.widget.GridLayoutManager;
import android.widget.LinearLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.wifi.WifiLogConfig;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface;
/**
 * Wifi information menu item on the diagnostic screen
 */
public class WifiLogSettings extends SettingsPreferenceFragment {
  private Button mSave;
  private Button mCancel;
  private RecyclerView mRecyclerView;
  private Context mContext;
  private List<String> wifiMode;
  private WifiLogConfig mWifiLogConfig;
  private ItemAdapter adapter;
  private LinearLayout mButtonLayout;
  private boolean EditChanged = false;
  //add by zhaohairuo for bug21650 @2018-12-15 start
  private int LOCAL_WMI = -1;
  private int LOCAL_HDD = -1;
  private int LOCAL_SME = -1;
  private int LOCAL_PE = -1;
  private int LOCAL_WMA = -1;
  private int LOCAL_SYS = -1;
  private int LOCAL_QDF = -1;
  private int LOCAL_SAP = -1;
  private int LOCAL_HDD_SOFTAP = -1;
  private int LOCAL_HDD_DATA = -1;  
  private int LOCAL_HDD_SAP_DATA = -1;
  private int LOCAL_HIF = -1;  
  private int LOCAL_HTC = -1;
  private int LOCAL_TXRX = -1;
  private int LOCAL_QDF_DEVICES = -1;
  private int LOCAL_CFG = -1;
  private int LOCAL_BMI = -1;
  private int LOCAL_EPPING = -1;
  //add by zhaohairuo for bug21650 @2018-12-15 end
  

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.wifi_log_level_settings, container, false);
    mRecyclerView = (RecyclerView)view.findViewById(R.id.wifilog);
    mSave = (Button)view.findViewById(R.id.save_wifi);
    mCancel = (Button)view.findViewById(R.id.cancel_wifi);
    mButtonLayout = (LinearLayout)view.findViewById(R.id.button_layout);
    initData();
    initView();
    setListener();
    return view;
  }
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mContext = activity;
  }

  private void initView() {

    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    adapter=new ItemAdapter(wifiMode,mContext);
    mRecyclerView.setAdapter(adapter);

  }

  private void setListener() {
    mSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
		//modified by zhaohairuo for bug21650 @2018-12-15 start
        mWifiLogConfig.setValue(mWifiLogConfig.WMI_KEY,LOCAL_WMI);
        mWifiLogConfig.setValue(mWifiLogConfig.HDD_KEY,LOCAL_HDD);
        mWifiLogConfig.setValue(mWifiLogConfig.SME_KEY,LOCAL_SME);
        mWifiLogConfig.setValue(mWifiLogConfig.PE_KEY,LOCAL_PE);
        mWifiLogConfig.setValue(mWifiLogConfig.WMA_KEY,LOCAL_WMA);
        mWifiLogConfig.setValue(mWifiLogConfig.SYS_KEY,LOCAL_SYS);
        mWifiLogConfig.setValue(mWifiLogConfig.QDF_KEY,LOCAL_QDF);
        mWifiLogConfig.setValue(mWifiLogConfig.SAP_KEY,LOCAL_SAP);
        mWifiLogConfig.setValue(mWifiLogConfig.HDD_SOFTAP_KEY,LOCAL_HDD_SOFTAP);
        mWifiLogConfig.setValue(mWifiLogConfig.HDD_DATA_KEY,LOCAL_HDD_DATA);
        mWifiLogConfig.setValue(mWifiLogConfig.HDD_SAP_DATA_KEY,LOCAL_HDD_SAP_DATA);
        mWifiLogConfig.setValue(mWifiLogConfig.HIF_KEY,LOCAL_HIF);
      	mWifiLogConfig.setValue(mWifiLogConfig.HTC_KEY,LOCAL_HTC);
        mWifiLogConfig.setValue(mWifiLogConfig.TXRX_KEY,LOCAL_TXRX);
        mWifiLogConfig.setValue(mWifiLogConfig.QDF_DEVICES_KEY,LOCAL_QDF_DEVICES);
        mWifiLogConfig.setValue(mWifiLogConfig.CFG_KEY,LOCAL_CFG);
        mWifiLogConfig.setValue(mWifiLogConfig.BMI_KEY,LOCAL_BMI);
        mWifiLogConfig.setValue(mWifiLogConfig.EPPING_KEY,LOCAL_EPPING);
		//modified by zhaohairuo for bug21650 @2018-12-15 end
        mWifiLogConfig.setLogLevel();
	}
    });
    mCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().finish();
      }
    });
    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        int lastPosition = -1;
         mButtonLayout.setVisibility(View.VISIBLE);
        if(newState == RecyclerView.SCROLL_STATE_IDLE){
          RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
          if(layoutManager instanceof GridLayoutManager){
            lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
          }else if(layoutManager instanceof LinearLayoutManager){
            lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
          }else if(layoutManager instanceof StaggeredGridLayoutManager){
            int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(lastPositions);
            lastPosition = findMax(lastPositions);
          }
		  //delete by zhaohairuo for bug 21650 @2018-12-10 
          /*if(lastPosition == recyclerView.getLayoutManager().getItemCount()-1){
            mButtonLayout.setVisibility(View.GONE);
          }*/

        }
      }

      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

      }

    });
  }
  private int findMax(int[] lastPositions) {
    int max = lastPositions[0];
    for (int value : lastPositions) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  private void initData() {
    mWifiLogConfig = new WifiLogConfig(mContext);
    mWifiLogConfig.readCfgInit();
    wifiMode = new ArrayList<String>();
    wifiMode.add("WMI");
    wifiMode.add("HDD");
    wifiMode.add("SME");
    wifiMode.add("PE");
    wifiMode.add("WMA");
    wifiMode.add("SYS");
    wifiMode.add("QDF");
    wifiMode.add("SAP");
    wifiMode.add("HDD_SOFTAP");
    wifiMode.add("HDD_DATA");
    wifiMode.add("HDD_SAP_DATA");
    wifiMode.add("HIF");
    wifiMode.add("HTC");
    wifiMode.add("TXRX");
    wifiMode.add("QDF_DEVICES");
    wifiMode.add("CFG");
    wifiMode.add("BMI");
    wifiMode.add("EPPING");
	//add by zhaohairuo for bug21650 @2018-12-15 start
		LOCAL_WMI = mWifiLogConfig.getIntValue(mWifiLogConfig.WMI_KEY);  
		LOCAL_HDD = mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_KEY);
		LOCAL_SME = mWifiLogConfig.getIntValue(mWifiLogConfig.SME_KEY);
		LOCAL_PE = mWifiLogConfig.getIntValue(mWifiLogConfig.PE_KEY);
		LOCAL_WMA = mWifiLogConfig.getIntValue(mWifiLogConfig.WMA_KEY);
		LOCAL_SYS = mWifiLogConfig.getIntValue(mWifiLogConfig.SYS_KEY);
		LOCAL_QDF = mWifiLogConfig.getIntValue(mWifiLogConfig.QDF_KEY);
		LOCAL_SAP = mWifiLogConfig.getIntValue(mWifiLogConfig.SAP_KEY);
		LOCAL_HDD_SOFTAP = mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_SOFTAP_KEY);
		LOCAL_HDD_DATA = mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_DATA_KEY);
		LOCAL_HDD_SAP_DATA = mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_SAP_DATA_KEY);
		LOCAL_HIF = mWifiLogConfig.getIntValue(mWifiLogConfig.HIF_KEY);
		LOCAL_HTC = mWifiLogConfig.getIntValue(mWifiLogConfig.HTC_KEY);
		LOCAL_TXRX = mWifiLogConfig.getIntValue(mWifiLogConfig.TXRX_KEY);
		LOCAL_QDF_DEVICES = mWifiLogConfig.getIntValue(mWifiLogConfig.QDF_DEVICES_KEY);
		LOCAL_CFG = mWifiLogConfig.getIntValue(mWifiLogConfig.CFG_KEY);
		LOCAL_BMI = mWifiLogConfig.getIntValue(mWifiLogConfig.BMI_KEY);
		LOCAL_EPPING = mWifiLogConfig.getIntValue(mWifiLogConfig.EPPING_KEY);
	//add by zhaohairuo for bug21650 @2018-12-15 end
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.TESTING;
  }

  public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {

    List<String> list;
    Context context;

    public ItemAdapter(List<String> list, Context context) {
      this.list = wifiMode;
      this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.wifi_log_level_item, parent, false));
      return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
	  //modified by zhaohairuo for bug21650 @2018-12-10
      holder.textView.setText(list.get(position) + ":0~255(default:255)");
      switch (position){
		 //modified by zhaohairuo for bug21650 @2018-12-15 start
        case 0:
          holder.editText.setText(EditChanged?LOCAL_WMI+"":mWifiLogConfig.getIntValue(mWifiLogConfig.WMI_KEY)+"");
          break;
        case 1:
          holder.editText.setText(EditChanged?LOCAL_HDD+"":mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_KEY)+"");
          break;
        case 2:
          holder.editText.setText(EditChanged?LOCAL_SME+"":mWifiLogConfig.getIntValue(mWifiLogConfig.SME_KEY)+"");
          break;
        case 3:
          holder.editText.setText(EditChanged?LOCAL_PE+"":mWifiLogConfig.getIntValue(mWifiLogConfig.PE_KEY)+"");
          break;
        case 4:
          holder.editText.setText(EditChanged?LOCAL_WMA+"":mWifiLogConfig.getIntValue(mWifiLogConfig.WMA_KEY)+"");
          break;
        case 5:
          holder.editText.setText(EditChanged?LOCAL_SYS+"":mWifiLogConfig.getIntValue(mWifiLogConfig.SYS_KEY)+"");
          break;
        case 6:
          holder.editText.setText(EditChanged?LOCAL_QDF+"":mWifiLogConfig.getIntValue(mWifiLogConfig.QDF_KEY)+"");
          break;
        case 7:
          holder.editText.setText(EditChanged?LOCAL_SAP+"":mWifiLogConfig.getIntValue(mWifiLogConfig.SAP_KEY)+"");
          break;
        case 8:
          holder.editText.setText(EditChanged?LOCAL_HDD_SOFTAP+"":mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_SOFTAP_KEY)+"");
          break;
        case 9:
          holder.editText.setText(EditChanged?LOCAL_HDD_DATA+"":mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_DATA_KEY)+"");
          break;
        case 10:
          holder.editText.setText(EditChanged?LOCAL_HDD_SAP_DATA+"":mWifiLogConfig.getIntValue(mWifiLogConfig.HDD_SAP_DATA_KEY)+"");
          break;
        case 11:
	        holder.editText.setText(EditChanged?LOCAL_HIF+"":mWifiLogConfig.getIntValue(mWifiLogConfig.HIF_KEY)+"");
          break;
        case 12:  
	        holder.editText.setText(EditChanged?LOCAL_HTC+"":mWifiLogConfig.getIntValue(mWifiLogConfig.HTC_KEY)+"");
          break;
        case 13:
          holder.editText.setText(EditChanged?LOCAL_TXRX+"":mWifiLogConfig.getIntValue(mWifiLogConfig.TXRX_KEY)+"");
          break;
        case 14:
          holder.editText.setText(EditChanged?LOCAL_QDF_DEVICES+"":mWifiLogConfig.getIntValue(mWifiLogConfig.QDF_DEVICES_KEY)+"");
          break;
        case 15:
          holder.editText.setText(EditChanged?LOCAL_CFG+"":mWifiLogConfig.getIntValue(mWifiLogConfig.CFG_KEY)+"");
          break;
        case 16:
          holder.editText.setText(EditChanged?LOCAL_BMI+"":mWifiLogConfig.getIntValue(mWifiLogConfig.BMI_KEY)+"");
          break;
        case 17:
          holder.editText.setText(EditChanged?LOCAL_EPPING+"":mWifiLogConfig.getIntValue(mWifiLogConfig.EPPING_KEY)+"");
          break;
		//modified by zhaohairuo for bug21650 @2018-12-15 end
      }
      holder.editText.addTextChangedListener(new Watcher(position));
    }

    @Override
    public int getItemViewType(int position) {
      return position;
    }

    @Override
    public int getItemCount() {
      return wifiMode.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

      TextView textView;
      EditText editText;

      public MyViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.wifilog_item);
        editText = (EditText) itemView.findViewById(R.id.wifilog_edit);

      }
    }
  }

  public class Watcher implements TextWatcher {
    private int EditID = 0;

    public Watcher(int Edit) {
      EditID = Edit;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
	
	//modified by zhaohairuo for bug21650 @2018-12-15 start
    @Override
    public void afterTextChanged(Editable s) {
      try {
	      int x = Integer.parseInt(s + "");
        if (!s.equals("")) {
	        if(x>=255){
		        x = 255;
	        }
	        if(x<=0){
		        x = 0;
	        }
	        EditChanged = true;
	        switch (EditID) {
	          case 0:
	            LOCAL_WMI = x;
	            break;
	          case 1:
	            LOCAL_HDD = x;
	            break;
	          case 2:
	            LOCAL_SME = x;
	            break;
	          case 3:
	            LOCAL_PE = x;
	            break;
	          case 4:
	            LOCAL_WMA = x;
	            break;
	          case 5:
	            LOCAL_SYS = x;
	            break;
	          case 6:
	            LOCAL_QDF = x;
	            break;
	          case 7:
	            LOCAL_SAP = x;
	            break;
	          case 8:
	            LOCAL_HDD_SOFTAP = x;
	            break;
	          case 9:
	            LOCAL_HDD_DATA = x;
	            break;
	          case 10:
	            LOCAL_HDD_SAP_DATA = x;
	            break;
	          case 11:
	            LOCAL_HIF = x;
	            break;
	          case 12:
	            LOCAL_HTC = x;
	            break;
	          case 13:
	            LOCAL_TXRX = x;
	            break;
	          case 14:
	            LOCAL_QDF_DEVICES = x;
	            break;
	          case 15:
	            LOCAL_CFG = x;
	            break;
	          case 16:
	            LOCAL_BMI = x;
	            break;
	          case 17:
	            LOCAL_EPPING = x;
	            break;   
	        }
        }
      }catch (Exception e){
        e.fillInStackTrace();
      }

    }
	//modified by zhaohairuo for bug21650 @2018-12-15 end
  }
}
