package com.ambigu.rtslocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnRegisterListener;
import com.ambigu.model.Info;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.FileUtils;
import com.ambigu.util.IpDeal;
import com.ambigu.util.RegisterUtils;
import com.ambigu.util.Utils;
import com.ambigu.view.CustomVideoView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnRegisterListener {

	private EditText et_usrid, et_usrpwd, et_usrrepwd, et_usrbirthday, et_age, et_email, et_adress;
	RadioGroup radioGroup;
	private Button btn_enter, btn_selecticon;
	// 调用系统相册-选择图片
	private static final int IMAGE = 1;
	private static String imagePath;
	private Handler handle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		initView();
	}

	private void initView() {

		// 初始化控件
		et_usrid = (EditText) findViewById(R.id.register_userid);
		et_usrpwd = (EditText) findViewById(R.id.register_pwd);
		et_usrrepwd = (EditText) findViewById(R.id.register_repwd);
		et_usrbirthday = (EditText) findViewById(R.id.register_birthday);
		et_age = (EditText) findViewById(R.id.register_age);
		et_email = (EditText) findViewById(R.id.register_email);
		et_adress = (EditText) findViewById(R.id.register_adress);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup_sex);
		btn_enter = (Button) findViewById(R.id.btn_enter);
		btn_selecticon = (Button) findViewById(R.id.btn_select);
		
		((RadioButton)radioGroup.getChildAt(0)).setChecked(true);

		btn_selecticon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Utils.getInstance().selectPicture(RegisterActivity.this);
			}
		});

		btn_enter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RadioButton radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
				String sex = radioButton.getText().toString();
				String userid = et_usrid.getText().toString();
				String pwd = et_usrpwd.getText().toString();
				String repwd = et_usrrepwd.getText().toString();
				String age = et_age.getText().toString();
				String email = et_email.getText().toString();
				String adress = et_adress.getText().toString();
				String birthday = et_usrbirthday.getText().toString();
				if (!pwd.equals(repwd)) {
					Toast.makeText(RegisterActivity.this, "密码不一致！", Toast.LENGTH_LONG).show();
				} else if (!RegisterUtils.validateEmailAddress(email)) {
					Toast.makeText(RegisterActivity.this, "邮箱格式不对！", Toast.LENGTH_LONG).show();
				} else if (!RegisterUtils.validatePassword(pwd)) {
					Toast.makeText(RegisterActivity.this, "密码格式不对！", Toast.LENGTH_LONG).show();
				} else {// 正式注册
					Info info = new Info();
					info.setFromUser(userid);
					info.setSex(sex);
					info.setPwd(pwd);
					info.setAdress(adress);
					info.setEmail(email);
					info.setBirthday(birthday);
					info.setAge(age);
					// 上传图标
					if (imagePath != null) {
						Log.e("icon",imagePath);
						String icon = FileUtils.imageToBase64(imagePath);
						info.setIcon(icon);
					} else {
						info.setIcon(null);
					}
					info.setIp(IpDeal.getHostIP());
					info.setInfoType(EnumInfoType.REGESTER);
					RTSClient.writeAndFlush(info);
				}
			}
		});

		DiscardClientHandler.getInstance().setOnRegisterListener(this);

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	// 接收选择的图片
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 获取图片路径
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (null == data) {
			return;
		}
		Uri uri = null;
		if (requestCode == ApplicationVar.KITKAT_LESS) {
			uri = data.getData();
			// 调用裁剪方法
			Utils.getInstance().cropPicture(this, uri);
		} else if (requestCode == ApplicationVar.KITKAT_ABOVE) {
			uri = data.getData();
			// 先将这个uri转换为path，然后再转换为uri
			String thePath = Utils.getInstance().getPath(this, uri);
			Utils.getInstance().cropPicture(this,
					Uri.fromFile(new File(thePath)));
		} else if (requestCode == ApplicationVar.INTENT_CROP) {
			Bitmap bitmap = data.getParcelableExtra("data");
			//ivResult.setImageBitmap(bitmap);
			File temp = new File(getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()+"//Icon"
					+"//");// 缓存文件夹
			if (!temp.exists()) {
				temp.mkdir();
			}
			File tempFile = new File(getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()+"//Icon"
					+"//"+"reg"+".png"); // 以默认注册名reg为文件名
			// 图像保存到文件中
			FileOutputStream foutput = null;
			try {
				foutput = new FileOutputStream(tempFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, foutput);
				imagePath=tempFile.getAbsolutePath();
				showImage(imagePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	// 加载图片
	private void showImage(String imaePath) {
		Bitmap bm = BitmapFactory.decodeFile(imaePath);
		ImageView iv_icon = ((ImageView) findViewById(R.id.image));
		iv_icon.setBackgroundResource(0);
		iv_icon.setImageBitmap(bm);
	}

	@Override
	public void callbackToRegister(Info info) {
		// TODO Auto-generated method stub
		if(info.isState()){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(RegisterActivity.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示信息");
					normalDialog.setMessage("注册成功");
					normalDialog.setPositiveButton("确定", null);
					normalDialog.show();
				}
			});
		}
	}

}
