package com.ambigu.rtslocation;

import java.util.ArrayList;

import com.ambigu.adapter.SwipeLocationHistoryAdapter;
import com.ambigu.client.DiscardClientHandler;
import com.ambigu.listener.OnDeleteLocationMessageListener;
import com.ambigu.model.Info;
import com.ambigu.model.Point;
import com.ambigu.model.ShareMessage;
import com.ambigu.model.ShareMessageOfPerson;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.settings.HistoryMap;
import com.ambigu.settings.SharingHistoryActivity;
import com.ambigu.view.SwipeListView;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MyLocationHistoryActivity extends Activity  implements OnDeleteLocationMessageListener  {

	private ArrayList<ShareMessage> shareMessages;
	private SwipeListView swipeListView=null;
	private SwipeLocationHistoryAdapter adapter=null;
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.self_location_history);
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		Info info=(Info) bundle.getSerializable("info");
		Gson gson=new Gson();
		Log.e("infodas",gson.toJson(info));
		ShareMessageOfPerson smo=info.getShareMessageOfPersons().get(0);
		if(smo!=null){
			shareMessages=smo.getShareMessages();
		}else{
			shareMessages=new ArrayList<ShareMessage>();
		}
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		swipeListView=(SwipeListView)findViewById(R.id.listview);
		adapter=new SwipeLocationHistoryAdapter(shareMessages, MyLocationHistoryActivity.this);
		swipeListView.setAdapter(adapter);
		initHandler();
		DiscardClientHandler.getInstance().setOnDeleteLocationMessageListener(this);
		
	}
	private void initHandler() {
		// TODO Auto-generated method stub
		handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final Info info=(Info)msg.obj;
				if(info.isState()){//删除成功
					AlertDialog.Builder builder = new Builder(MyLocationHistoryActivity.this);
					// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					// 设置对话框标题
					builder.setTitle("提示信息");
					// 设置对话框内的文本
					builder.setMessage("删除成功！");
					// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int childPos=info.getChildPos();
							Log.e("childPos",shareMessages.get(childPos).getStart_time());
							shareMessages.remove(childPos);
							adapter.notifyDataSetChanged();
						}
					});
					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				}else{
					AlertDialog.Builder builder = new Builder(MyLocationHistoryActivity.this);
					// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					// 设置对话框标题
					builder.setTitle("提示信息");
					// 设置对话框内的文本
					builder.setMessage("删除失败！");
					// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					});
					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				}
			}
		};
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void deleteLocation(Info info) {
		// TODO Auto-generated method stub
		Message message=Message.obtain();
		message.obj=info;
		handler.sendMessage(message);
	}
	
}
