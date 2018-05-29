package com.ambigu.rtslocation;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.drive.DriveActivity;
import com.ambigu.listener.onLoginResult;
import com.ambigu.model.Info;
import com.ambigu.service.SharingService;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.FileUtils;
import com.ambigu.view.CustomVideoView;
import com.baidu.location.a.h;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener, onLoginResult {

	private CustomVideoView videoview;
	private Button btn_enter;
	private EditText usr_input;
	private EditText pwd_input;
	private TextView register_tv;
	private ProgressDialog pDialog;
	private Thread thread;
	private volatile boolean isStillLogin=true;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				pDialog.dismiss();
				Toast.makeText(LoginActivity.this, "未登录", Toast.LENGTH_LONG).show();
			} else {
				pDialog.dismiss();
				if(isStillLogin){
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					LoginActivity.this.finish();
				}
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initSharePreference();
		initService();
		initView();
	}
	
	private void initSharePreference() {
		// TODO Auto-generated method stub		
		SharedPreferences sharedPreferences = getSharedPreferences("rtslocation", Context.MODE_PRIVATE); // 私有数据
		ApplicationVar.setSharedPreferences(sharedPreferences);
	}

	private void initView() {
		btn_enter = (Button) findViewById(R.id.btn_enter);
		btn_enter.setOnClickListener(this);
		usr_input = (EditText) findViewById(R.id.et_userid);
		pwd_input = (EditText) findViewById(R.id.et_pwd);
		videoview = (CustomVideoView) findViewById(R.id.videoview);
		register_tv = (TextView) findViewById(R.id.tv_register);
		videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sport));

		pDialog = new ProgressDialog(LoginActivity.this);
		pDialog.setMessage("正在登录！");
		pDialog.setTitle("自动登录提示");
		Window window = pDialog.getWindow();
		WindowManager.LayoutParams lParams = window.getAttributes();
		lParams.alpha = 0.7f;
		lParams.dimAmount = 0.4f;
		window.setAttributes(lParams);
		pDialog.show();
		// 验证自动登录

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				SharedPreferences sharedPreferences = getSharedPreferences("rtslocation", Context.MODE_PRIVATE);
				ApplicationVar.setSharedPreferences(sharedPreferences);
				boolean isLogin = false;
				if (sharedPreferences != null) {
					isLogin = sharedPreferences.getBoolean("isLogin", false);
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (isLogin) {
					mHandler.sendEmptyMessage(1);
				} else {
					mHandler.sendEmptyMessage(0);
				}
			}
		});
		thread.start();
		pDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog){
				isStillLogin=false;
				mHandler.sendEmptyMessage(0);
			}
		});

		// 播放
		videoview.start();
		// 循环播放
		videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				videoview.start();
			}
		});

		register_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
			}
		});

		DiscardClientHandler.getInstance().setLoginInterface(this);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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

	private void initService() {
		// TODO Auto-generated method stub
		Intent service = new Intent(LoginActivity.this, SharingService.class);
		startService(service);
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_enter:
			String userid = usr_input.getText().toString();// 得到用户名
			String userpwd = pwd_input.getText().toString();// 得到密码
			if(userid==null){
				Toast.makeText(this, "请输入用户名！", Toast.LENGTH_LONG).show();
				return;
			}
			if(userpwd==null){
				Toast.makeText(this, "请输入用户名！", Toast.LENGTH_LONG).show();
				return;
			}
			Info logininfo = new Info();
			logininfo.setFromUser(userid);
			logininfo.setPwd(userpwd);
			logininfo.setInfoType(EnumInfoType.LOGIN);
			RTSClient.writeAndFlush(logininfo);

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.load, null);
			ImageView img_loading = (ImageView) view.findViewById(R.id.img_loading);
			img_loading.setBackgroundResource(R.anim.loadingimg);
			AnimationDrawable AniDraw = (AnimationDrawable) img_loading.getBackground();
			AniDraw.start();
			break;
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();

		videoview.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		videoview.start();
	}

	@Override
	public void callbackToLogin(Info info) {
		// TODO Auto-generated method stub
		if (info.isState()) {
			// 保存个人信息
			SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
			Editor editor = sharedPreferences.edit();
			editor.putString("username", info.getToUser());
			editor.putString("password", info.getPwd());
			editor.putString("sex", info.getSex());
			editor.putString("age", info.getAge());
			editor.putString("birthday", info.getBirthday());
			editor.putString("email", info.getEmail());
			editor.putString("adress", info.getAdress());
			editor.putBoolean("isLogin", true);
			editor.commit();

			// 存入个人头像
			String base64Image = info.getIcon();
			FileUtils.base64ToFile(base64Image, getCacheDir().getAbsolutePath() + "//" + ApplicationVar.getId()
					+ "//Icon" + "//" + ApplicationVar.getId() + ".png");

			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("info", info);
			intent.putExtras(bundle);
			startActivity(intent);
			LoginActivity.this.finish();
		} else {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
			new AlertDialog.Builder(LoginActivity.this).setTitle("提示").setMessage("登录失败，请检查用户名和密码是否正确！")
					.setPositiveButton("确定", null).show();
				}
			});
		}
	}

}
