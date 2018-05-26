package com.ambigu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.User;
import com.ambigu.rtslocation.AcquireAuthLatlngActivity;
import com.ambigu.rtslocation.MessageActivity;
import com.ambigu.rtslocation.R;
import com.google.gson.Gson;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class DialogUtils {
	private static String toUser="";
	public static ProgressDialog showProgressDialog(Context context,String title,String message){
		ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setMessage(message);
		pDialog.setTitle(title);
		Window window = pDialog.getWindow();
		WindowManager.LayoutParams lParams = window.getAttributes();
		lParams.alpha = 0.7f;
		lParams.dimAmount = 0.4f;
		window.setAttributes(lParams);
		pDialog.show();
		return pDialog;
	}
	
	public static OnDismissListener creatDismissListener(){
		OnDismissListener onDismissListener=new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				
			}
		};
		return onDismissListener;
	}
	
	
	
}
