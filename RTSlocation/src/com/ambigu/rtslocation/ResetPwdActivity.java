package com.ambigu.rtslocation;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnResetPwdListener;
import com.ambigu.model.Info;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.RegisterUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ResetPwdActivity extends Activity implements OnResetPwdListener{

	private EditText pwd,repwd;
	private Button enter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpwd);
		initView();
		DiscardClientHandler.getInstance().setOnResetPwdListener(this);
	}

	private void initView() {
		// TODO Auto-generated method stub
		pwd=(EditText)findViewById(R.id.et_pwd);
		repwd=(EditText)findViewById(R.id.et_repwd);
		enter=(Button)findViewById(R.id.btn_enter);
		enter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String pwds=pwd.getText().toString();
				String repwds=repwd.getText().toString();
				if(!pwds.equals(repwds)){
					Toast.makeText(ResetPwdActivity.this, "两次密码输入不一致！", Toast.LENGTH_LONG).show();
					return;
				}else{
					if(!RegisterUtils.validatePassword(pwds)){
						Toast.makeText(ResetPwdActivity.this, "密码格式不对！", Toast.LENGTH_LONG).show();
						return;
					}else{//发送密码更新时间
						Info info =new Info();
						info.setFromUser(ApplicationVar.getId());
						info.setPwd(pwds);
						info.setInfoType(EnumInfoType.RESET_PWD);
						RTSClient.writeAndFlush(info);
					}
				}
			}
		});
	}

	@Override
	public void resetPwd(final Info info) {
		// TODO Auto-generated method stub
		if(info.isState()){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(ResetPwdActivity.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示信息");
					normalDialog.setMessage("修改成功，请重新登录！");
					normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
							Editor editor = sharedPreferences.edit();
							//重新登录
							// 清除sharepreference
							editor.clear();
							editor.putBoolean("isLogin", false);
							editor.commit();
							// 返回到登录界面
							Intent intent = new Intent(ResetPwdActivity.this, LoginActivity.class);
							startActivity(intent);
							finish();
						}
					});
					normalDialog.show();
				}
			});
		}else{
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(ResetPwdActivity.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示信息");
					normalDialog.setMessage("修改失败");
					normalDialog.setPositiveButton("确定", null);
					normalDialog.show();
				}
			});
		}
	}


}
