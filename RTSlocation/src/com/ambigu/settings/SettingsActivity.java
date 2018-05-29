package com.ambigu.settings;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnModifyInfoListener;
import com.ambigu.model.Info;
import com.ambigu.rtslocation.R;
import com.ambigu.rtslocation.RegisterActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.RegisterUtils;
import com.ambigu.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnModifyInfoListener {

	private Button btn_modify;
	private Button btn_center;
	private Button btn_cancel;
	private String username;
	private String sex;
	private String age;
	private String birthday;
	private String email;
	private String adress;
	private EditText tv_adress;
	private EditText tv_email;
	private EditText tv_birthday;
	private EditText tv_age;
	private RadioGroup tv_sex;
	private TextView tv_username1;
	private TextView tv_sex1;
	private TextView tv_age1;
	private TextView tv_birthday1;
	private TextView tv_email1;
	private TextView tv_adress1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myinfo);

		tv_sex = (RadioGroup) findViewById(R.id.radioGroup_sex);
		tv_age = (EditText) findViewById(R.id.age);
		tv_birthday = (EditText) findViewById(R.id.birthday);
		tv_email = (EditText) findViewById(R.id.email);
		tv_adress = (EditText) findViewById(R.id.adress);
		btn_center = (Button) findViewById(R.id.btn_enter);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_modify = (Button) findViewById(R.id.btn_modify);

		tv_username1 = (TextView) findViewById(R.id.username1);
		tv_sex1 = (TextView) findViewById(R.id.sex1);
		tv_age1 = (TextView) findViewById(R.id.age1);
		tv_birthday1 = (TextView) findViewById(R.id.birthday1);
		tv_email1 = (TextView) findViewById(R.id.email1);
		tv_adress1 = (TextView) findViewById(R.id.adress1);

		SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
		username = sharedPreferences.getString("username", "游客");
		sex = sharedPreferences.getString("sex", "保密");
		age = sharedPreferences.getString("age", "0");
		birthday = sharedPreferences.getString("birthday", "1970-01-01");
		email = sharedPreferences.getString("email", "");
		adress = sharedPreferences.getString("adress", "");

		if (sex.equals("Boy")) {
			((RadioButton) tv_sex.getChildAt(0)).setChecked(true);
		} else {
			((RadioButton) tv_sex.getChildAt(1)).setChecked(true);
		}
		tv_age.setText(age);
		tv_birthday.setText(birthday);
		tv_email.setText(email);
		tv_adress.setText(adress);

		// 设置不可编辑
		setTextView();

		btn_modify.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_center.setVisibility(View.VISIBLE);
				btn_cancel.setVisibility(View.VISIBLE);
				btn_modify.setVisibility(View.GONE);
				setEditText();
			}
		});

		btn_center.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 更新username等信息
				RadioButton radioButton = (RadioButton) findViewById(tv_sex.getCheckedRadioButtonId());

				if(RegisterUtils.validateEmailAddress(tv_email.getText().toString())){
					Toast.makeText(SettingsActivity.this, "邮箱格式不正确", Toast.LENGTH_LONG).show();
					return ;
				}
				
				// 发送信息给服务器
				Info info = new Info();
				info.setInfoType(EnumInfoType.MODIFY_INFO);
				info.setFromUser(ApplicationVar.getId());
				info.setSex(radioButton.getText().toString());
				info.setAge(tv_age.getText().toString());
				info.setAdress(tv_adress.getText().toString());
				info.setEmail(tv_email.getText().toString());
				info.setBirthday(tv_birthday.getText().toString());
				RTSClient.writeAndFlush(info);

				// 设置不可编辑
				setTextView();

				btn_center.setVisibility(View.GONE);
				btn_cancel.setVisibility(View.GONE);
				btn_modify.setVisibility(View.VISIBLE);

			}
		});

		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setTextView();
				btn_center.setVisibility(View.GONE);
				btn_cancel.setVisibility(View.GONE);
				btn_modify.setVisibility(View.VISIBLE);
			}
		});

		DiscardClientHandler.getInstance().setOnModifyInfoListener(this);
		
	}

	public void setEditText() {
		tv_sex.setVisibility(View.VISIBLE);
		tv_age.setVisibility(View.VISIBLE);
		tv_adress.setVisibility(View.VISIBLE);
		tv_email.setVisibility(View.VISIBLE);
		tv_birthday.setVisibility(View.VISIBLE);

		if (sex.equals("Boy")) {
			((RadioButton) tv_sex.getChildAt(0)).setChecked(true);
		} else {
			((RadioButton) tv_sex.getChildAt(1)).setChecked(true);
		}
		tv_age.setText(age);
		tv_adress.setText(adress);
		tv_email.setText(email);
		tv_birthday.setText(birthday);

		tv_sex1.setVisibility(View.GONE);
		tv_age1.setVisibility(View.GONE);
		tv_adress1.setVisibility(View.GONE);
		tv_email1.setVisibility(View.GONE);
		tv_birthday1.setVisibility(View.GONE);
	}

	public void setTextView() {
		tv_sex.setVisibility(View.GONE);
		tv_age.setVisibility(View.GONE);
		tv_adress.setVisibility(View.GONE);
		tv_email.setVisibility(View.GONE);
		tv_birthday.setVisibility(View.GONE);

		tv_username1.setVisibility(View.VISIBLE);
		tv_sex1.setVisibility(View.VISIBLE);
		tv_age1.setVisibility(View.VISIBLE);
		tv_adress1.setVisibility(View.VISIBLE);
		tv_email1.setVisibility(View.VISIBLE);
		tv_birthday1.setVisibility(View.VISIBLE);

		tv_username1.setText(username);
		tv_sex1.setText(sex);
		tv_age1.setText(age);
		tv_adress1.setText(adress);
		tv_email1.setText(email);
		tv_birthday1.setText(birthday);
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
	public void modifyInfo(final Info info) {
		// TODO Auto-generated method stub
		if (info.isState()) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(SettingsActivity.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示信息");
					normalDialog.setMessage("修改成功");
					normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							sex = info.getSex();
							age = info.getAge();
							adress = info.getAdress();
							email = info.getEmail();
							birthday = info.getBirthday();
							setTextView();
						}
					});
					normalDialog.show();
				}
			});
		} else {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(SettingsActivity.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示信息");
					normalDialog.setMessage("修改失败");
					normalDialog.setPositiveButton("确定", null);
					normalDialog.show();
					setTextView();
				}
			});
		}
	}

}
