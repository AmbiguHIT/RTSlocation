package com.ambigu.rtslocation;

import java.util.ArrayList;

import com.ambigu.adapter.SwipeLocationHistoryAdapter;
import com.ambigu.model.Info;
import com.ambigu.model.ShareMessage;
import com.ambigu.model.ShareMessageOfPerson;
import com.ambigu.view.SwipeListView;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MyLocationHistoryActivity extends Activity {

	private ArrayList<ShareMessage> shareMessages;
	private SwipeListView swipeListView=null;
	private SwipeLocationHistoryAdapter adapter=null;
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
		
		swipeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				
			}
		});
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
