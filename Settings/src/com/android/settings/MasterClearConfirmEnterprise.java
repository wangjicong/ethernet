/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.oemlock.OemLockManager;
import android.service.persistentdata.PersistentDataBlockManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settingslib.RestrictedLockUtils;
import android.view.inputmethod.EditorInfo;
import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.os.RecoverySystem;

/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the confirmation screen.
 */
public class MasterClearConfirmEnterprise extends InstrumentedFragment {

    private View mContentView;
    private boolean mEraseSdCard;
    private boolean mEraseEsims;
	private String VERIFY_CODE = "23058518";

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Checkin Service to reset the device to its factory-default
     * state (rebooting in the process).
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }
						
						showDialog(); //MEIG:jiangdanyang@input password if erase everything 20190408
				}
    };
		
		//MEIG:jiangdanyang@input password if erase everything 20190408 --- start
		public void showDialog(){
				final EditText et = new EditText(getActivity());
				et.setInputType(EditorInfo.TYPE_CLASS_PHONE);
				new AlertDialog.Builder(getActivity()).setTitle(R.string.verify_code).setView(et).setPositiveButton(R.string.meig_ok, new OnClickListener() {		
						@Override
						public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								if(isVerifyCode(et.getText().toString())){
										makeClear();
								}else{
										showErrDialog();
								}
						}
			})
			.setNegativeButton(R.string.meig_cancel, null).show();
		}
		
		private void showErrDialog(){
				new AlertDialog.Builder(getActivity()).setMessage(R.string.wrong_code).setNegativeButton(R.string.meig_ok, null).show();
		}
	 
	  private boolean isVerifyCode(String code){
				boolean ret = false;
				if(VERIFY_CODE.equals(code))ret = true;
				return ret;
	  }
    //MEIG:jiangdanyang@input password if erase everything 20190408 --- end
    
		private void makeClear(){
            final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
                    getActivity().getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);
            final OemLockManager oemLockManager = (OemLockManager)
                    getActivity().getSystemService(Context.OEM_LOCK_SERVICE);

            if (pdbManager != null && !oemLockManager.isOemUnlockAllowed() &&
                    Utils.isDeviceProvisioned(getActivity())) {
                // if OEM unlock is allowed, the persistent data block will be wiped during FR
                // process. If disabled, it will be wiped here, unless the device is still being
                // provisioned, in which case the persistent data block will be preserved.
                new AsyncTask<Void, Void, Void>() {
                    int mOldOrientation;
                    ProgressDialog mProgressDialog;

                    @Override
                    protected Void doInBackground(Void... params) {
                        pdbManager.wipe();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mProgressDialog.hide();
                        if (getActivity() != null) {
                            getActivity().setRequestedOrientation(mOldOrientation);
                            doMasterClear();
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        mProgressDialog = getProgressDialog();
                        mProgressDialog.show();

                        // need to prevent orientation changes as we're about to go into
                        // a long IO request, so we won't be able to access inflate resources on flash
                        mOldOrientation = getActivity().getRequestedOrientation();
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    }
                }.execute();
            } else {
                doMasterClear();
            }
    }
    
    private ProgressDialog getProgressDialog() {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(
                    getActivity().getString(R.string.master_clear_progress_title));
            progressDialog.setMessage(
                    getActivity().getString(R.string.master_clear_progress_text));
            return progressDialog;
    }
       
    private void doMasterClear() {
        try {
            RecoverySystem.rebootWipeEnterprise(getActivity(),"MasterClearConfirmEnterprise");
        } catch (Exception e) {
            System.out.println("MasterClearConfirmEnterprise Can't perform master clear/factory reset " + e);
        }

    }

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        mContentView.findViewById(R.id.execute_master_clear)
                .setOnClickListener(mFinalClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(
                getActivity(), UserManager.DISALLOW_FACTORY_RESET, UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(),
                UserManager.DISALLOW_FACTORY_RESET, UserHandle.myUserId())) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, null);
        } else if (admin != null) {
            new ActionDisabledByAdminDialogHelper(getActivity())
                    .prepareDialogBuilder(UserManager.DISALLOW_FACTORY_RESET, admin)
                    .setOnDismissListener(__ -> getActivity().finish())
                    .show();
            return new View(getActivity());
        }
        mContentView = inflater.inflate(R.layout.master_clear_confirm, null);
        establishFinalConfirmationState();
        setAccessibilityTitle();
        return mContentView;
    }

    private void setAccessibilityTitle() {
        CharSequence currentTitle = getActivity().getTitle();
        TextView confirmationMessage =
                (TextView) mContentView.findViewById(R.id.master_clear_confirm);
        if (confirmationMessage != null) {
            String accessibleText = new StringBuilder(currentTitle).append(",").append(
                    confirmationMessage.getText()).toString();
            getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, accessibleText));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mEraseSdCard = args != null
                && args.getBoolean(MasterClear.ERASE_EXTERNAL_EXTRA);
        mEraseEsims = args != null
                && args.getBoolean(MasterClear.ERASE_ESIMS_EXTRA);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.MASTER_CLEAR_CONFIRM;
    }
}
