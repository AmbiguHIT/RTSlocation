package com.ambigu.rtslocation;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnAddFriendListener;
import com.ambigu.model.Info;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddFriendActivity extends Activity implements OnAddFriendListener{
	private EditText edt_search;
	private EditText edt_group;
	private Button btn_send;
	private Handler mhandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_friend);
		initView();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		edt_search=(EditText) findViewById(R.id.et_userid);
		edt_group=(EditText)findViewById(R.id.et_group);
		btn_send=(Button) findViewById(R.id.btn_enter);
		
		mhandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
	        	final Info info = (Info)msg.obj;
				switch (msg.what) {
				case 0:
					AlertDialog.Builder builder = new Builder(AddFriendActivity.this);
					//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					//设置对话框标题
					builder.setTitle("提示信息");
					//设置对话框内的文本
					builder.setMessage("添加成功");
					//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					                // 执行点击确定按钮的业务逻辑
					        	Intent intentTemp = new Intent();
					        	Bundle bundle=new Bundle();
					        	bundle.putSerializable("info", info);
					    		Log.e("addfriend1",info.getFromUser()+"");
				                intentTemp.putExtras(bundle);
				                setResult(1,intentTemp);
				                finish();
					        }
					});
					//使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					//显示对话框
					dialog.show();
					break;
				case 1:
					AlertDialog.Builder builder1 = new Builder(AddFriendActivity.this);
					//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder1.setIcon(R.drawable.ic_launcher);
					//设置对话框标题
					builder1.setTitle("提示信息");
					//设置对话框内的文本
					builder1.setMessage("添加失败");
					//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder1.setPositiveButton("确定", new OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					                // 执行点击确定按钮的业务逻辑
					        	Intent intentTemp = new Intent();
					        	Bundle bundle=new Bundle();
					        	bundle.putSerializable("info", info);
					    		Log.e("addfriend1",info.getFromUser()+"");
				                intentTemp.putExtras(bundle);
				                setResult(1,intentTemp);
				                finish();
					        }
					});
					//使用builder创建出对话框对象
					AlertDialog dialog1 = builder1.create();
					//显示对话框
					dialog1.show();
					break;

				default:
					break;
				}
			}
		};
		
		btn_send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String id=edt_search.getText().toString();
				String group=edt_group.getText().toString();
            	Info info=new Info();
            	info.setFromUser(ApplicationVar.getId());
            	info.setToUser(id);
            	info.setState(false);
            	info.setGroup(group);
            	info.setInfoType(EnumInfoType.ADD_FRIEND);
            	RTSClient.writeAndFlush(info);//添加好友
			}
		});
		
		DiscardClientHandler.getInstance().setOnAddFriendListener(this);
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
	public void onAddFriendState(Info info) {
		// TODO Auto-generated method stub
		if(info.isState()){
			//处理成功
			Message message=Message.obtain();
			message.obj=info;
			message.what=0;
			mhandler.sendMessage(message);
		}else{
			//处理失败
			Message message=Message.obtain();
			message.obj=info;
			message.what=1;
			mhandler.sendMessage(message);
		}
	}
}
