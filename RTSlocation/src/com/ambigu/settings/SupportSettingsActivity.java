package com.ambigu.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnAuthChangedListener;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.Settings_Type;
import com.ambigu.model.User;
import com.ambigu.rtslocation.AddFriendActivity;
import com.ambigu.rtslocation.LoginActivity;
import com.ambigu.rtslocation.R;
import com.ambigu.service.SharingService;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.view.SpinerPopWindow;
import com.google.gson.Gson;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SupportSettingsActivity extends Activity implements OnCheckedChangeListener,OnAuthChangedListener{

	private SpinerPopWindow<String> mSpinerPopWindow;
	private SpinerPopWindow<String> minviPopWindow;
	private List<String> list;
	private List<String> invilist;
	private TextView Pinlv;
	private LinearLayout linearLayout;
	private TextView invi_Pinlv;
	private LinearLayout invi_linearLayout;
	private Switch allow;
	private boolean isAllow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_support);
		initData();
		initView();
	}
	
	/**
	 * 显示PopupWindow
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_value:
				mSpinerPopWindow.setWidth(Pinlv.getWidth());
				mSpinerPopWindow.showAsDropDown(Pinlv);
				setTextImage(Pinlv,R.drawable.icon_up);
				break;
			case R.id.invi_value:
				minviPopWindow.setWidth(invi_Pinlv.getWidth());
				minviPopWindow.showAsDropDown(invi_Pinlv);
				setTextImage(invi_Pinlv,R.drawable.icon_up);
				break;
			case R.id.invi:
				Intent intent=new Intent(SupportSettingsActivity.this,AuthActivity.class);
				startActivity(intent);
				finish();
				break;
			}
		}
	};
	
	/**
	 * popupwindow显示的ListView的item点击事件
	 */
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			mSpinerPopWindow.dismiss();
			int times=4+position;
			if(position==0){
				Pinlv.setText("请选择频率");
				SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
				Editor editor=sharedPreferences.edit();
				times=5;
				editor.putInt("update_times", times*2);//默认为10秒
				
				editor.commit();
			}
			else{
				Pinlv.setText(list.get(position));
				times=4+position;
				SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
				Editor editor=sharedPreferences.edit();
				editor.putInt("update_times", times*2);
				editor.commit();
			}
			
			Intent intent = new Intent("com.ambigu.rtslocation.settings");  
			intent.putExtra("type", Settings_Type.UPDATE_TIMES);
            sendBroadcast(intent);  //通知消息发生改变
		}
	};
	
	private OnItemClickListener itemInviClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			minviPopWindow.dismiss();
			int times=4+position;
			if(position==0){
				invi_Pinlv.setText("请选择频率");
				SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
				Editor editor=sharedPreferences.edit();
				times=5;
				editor.putInt("inviupdate_times", times*2);
				editor.commit();
			}
			else{
				invi_Pinlv.setText(invilist.get(position));
				SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
				Editor editor=sharedPreferences.edit();
				editor.putInt("inviupdate_times", times*2);
				editor.commit();
			}
			Intent intent = new Intent("com.ambigu.rtslocation.settings");  
			intent.putExtra("type", Settings_Type.UPDATE_INVITIMES);
            sendBroadcast(intent);  //通知消息发生改变
		}
	};

	/**
	 * 给TextView右边设置图片
	 * @param resId
	 */
	private void setTextImage(View view,int resId) {
		Drawable drawable =getDrawable(resId);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
		if(view.getId()==Pinlv.getId())	Pinlv.setCompoundDrawables(null, null, drawable, null);
		else if(view.getId()==invi_Pinlv.getId()) invi_Pinlv.setCompoundDrawables(null, null, drawable, null);
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		Pinlv = (TextView) findViewById(R.id.tv_value);
		invi_Pinlv=(TextView)findViewById(R.id.invi_value);
		Pinlv.setOnClickListener(clickListener);
		invi_Pinlv.setOnClickListener(clickListener);
		
		mSpinerPopWindow = new SpinerPopWindow<String>(this, list,itemClickListener);
		mSpinerPopWindow.setOnDismissListener(dismissListener);
		minviPopWindow=new SpinerPopWindow<String>(this, invilist, itemInviClickListener);
		minviPopWindow.setOnDismissListener(invi_dismissListener);
		
		linearLayout=(LinearLayout)findViewById(R.id.invi);
		invi_linearLayout=(LinearLayout)findViewById(R.id.invi_pinlv);
		allow=(Switch) findViewById(R.id.switch_shareable);
		allow.setOnCheckedChangeListener(this);
		linearLayout.setOnClickListener(clickListener);
		invi_linearLayout.setOnClickListener(clickListener);
		
		if(isAllow){
			invi_linearLayout.setVisibility(View.VISIBLE);
			linearLayout.setVisibility(View.VISIBLE);
			allow.setChecked(true);
		}
		
		SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
		int times=sharedPreferences.getInt("update_times", 0);
		int times1=sharedPreferences.getInt("inviupdate_times", 0);
		if(times==0) Pinlv.setText("请选择频率");
		else Pinlv.setText(times+"秒/次");
		if(times==0) invi_Pinlv.setText("请选择频率");
		else invi_Pinlv.setText(times1+"秒/次");
	}
	
	/**
	 * 监听popupwindow取消
	 */
	private OnDismissListener  dismissListener=new OnDismissListener() {
		@Override
		public void onDismiss() {
			setTextImage(Pinlv,R.drawable.icon_down);
		}
	};
	
	private OnDismissListener  invi_dismissListener=new OnDismissListener() {
		@Override
		public void onDismiss() {
			setTextImage(invi_Pinlv,R.drawable.icon_down);
		}
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(isChecked){
			linearLayout.setVisibility(View.VISIBLE);
			invi_linearLayout.setVisibility(View.VISIBLE);
			Intent intent = new Intent("com.ambigu.rtslocation.settings");  
			intent.putExtra("type", Settings_Type.AUTH_ALLOW);
            sendBroadcast(intent);  //通知消息发生改变
			SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
			Editor editor=sharedPreferences.edit();
			editor.putBoolean("isAuth", true);
			editor.commit();
		}else{
			linearLayout.setVisibility(View.GONE);
			invi_linearLayout.setVisibility(View.GONE);
			Info info=new Info();
			info.setFromUser(ApplicationVar.getId());
			info.setInfoType(EnumInfoType.CHANGE_AUTH);
			info.setFriendsList(null);//标志全部清除权限
			RTSClient.writeAndFlush(info);
		}
	}
	
	private void initData() {
		list = new ArrayList<String>();
		list.add("重置");
		for (int i = 5; i < 9; i++) {
			list.add(""+i*2+"秒/次");
		}
		invilist=new ArrayList<String>();
		invilist.add("重置");
		for (int i = 5; i < 9; i++) {
			invilist.add(""+i*2+"秒/次");
		}
		
		SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
		isAllow=sharedPreferences.getBoolean("isAuth", false);
		DiscardClientHandler.getInstance().setOnClearAuthListener(this);
	}

	@Override
	public void notifyAuthChanged(Info info) {
		// TODO Auto-generated method stub
		if(info.isState()){
			runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	AlertDialog.Builder builder = new Builder(SupportSettingsActivity.this);
					//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					//设置对话框标题
					builder.setTitle("提示信息");
					//设置对话框内的文本
					builder.setMessage("已清除全部权限");
					//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					                // 执行点击确定按钮的业务逻辑
					        	SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
					        	String infoString=sharedPreferences.getString("info", "{}");
					        	Info info=(new Gson()).fromJson(infoString, Info.class);
					        	HashMap<String, Group> groups=info.getFriendsList();
					        	Iterator<Entry<String, Group>> iterator=groups.entrySet().iterator();
					        	while(iterator.hasNext()){
					        		Entry<String, Group> entry=iterator.next();
					        		Group group=(Group)entry.getValue();
					        		group.setChoose(false);
					        		for(User user:group.getItems()){
					        			user.setChoose(false);
					        		}
					        	}
					        	info.setFriendsList(groups);
					        	Editor editor=sharedPreferences.edit();
					        	editor.putString("info", (new Gson()).toJson(info));
								editor.putBoolean("isAuth", false);
								editor.commit();

								Intent intent = new Intent("com.ambigu.rtslocation.settings");  
								intent.putExtra("type", Settings_Type.AUTH_ALLOW);
					            sendBroadcast(intent);  //通知消息发生改变
					        }
					});
					
					//使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					//显示对话框
					dialog.show();
	            }
	        });
		}else{
			runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	allow.setChecked(true);
	            }
			});
		}
	}
	
}
