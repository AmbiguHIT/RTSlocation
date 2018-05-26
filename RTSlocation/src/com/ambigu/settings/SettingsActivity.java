package com.ambigu.settings;

import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myinfo);
		TextView tv_username=(TextView) findViewById(R.id.username);
		TextView tv_sex=(TextView) findViewById(R.id.sex);
		TextView tv_age=(TextView) findViewById(R.id.age);
		TextView tv_birthday=(TextView) findViewById(R.id.birthday);
		TextView tv_email=(TextView) findViewById(R.id.email);
		TextView tv_adress=(TextView) findViewById(R.id.adress);
		SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
		tv_username.setText(sharedPreferences.getString("username","游客"));
		tv_sex.setText(sharedPreferences.getString("sex","保密"));
		tv_age.setText(sharedPreferences.getString("age","0"));
		tv_birthday.setText(sharedPreferences.getString("birthday","1970-01-01"));
		tv_email.setText(sharedPreferences.getString("email",""));
		tv_adress.setText(sharedPreferences.getString("adress",""));
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
	
}
