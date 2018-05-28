package com.ambigu.rtslocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.ambigu.adapter.AuthInfoAdapter;
import com.ambigu.adapter.PinnedHeaderExpandableAdapter;
import com.ambigu.model.AuthNode;
import com.ambigu.model.Info;
import com.ambigu.view.PinnedHeaderExpandableListView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class AuthInfoActivity extends Activity{

	private HashMap<String, ArrayList<AuthNode>> authNodes;
	private PinnedHeaderExpandableListView listview;
	private ArrayList<String> groups;
	private AuthInfoAdapter adapter;
	private boolean isMulChoice=false;
	private ArrayList<HashMap<Integer, Boolean>> isChecked;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authinfo_history);
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		Intent intent=getIntent();
		Bundle bundle =intent.getExtras();
		Info info=(Info) bundle.get("info");
		authNodes=info.getAuthNodes();
		groups=new ArrayList<String>();
		Iterator<Entry<String, ArrayList<AuthNode>>> iterator=authNodes.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, ArrayList<AuthNode>> entry=iterator.next();
			groups.add(entry.getKey());
		}
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		listview = (PinnedHeaderExpandableListView) findViewById(R.id.listview);
		Button sure=(Button)findViewById(R.id.btn_enter);
		Button cancel=(Button)findViewById(R.id.btn_cancel);
		
		// 设置悬浮头部VIEW
		listview.setHeaderView(getLayoutInflater().inflate(R.layout.auth_history_group_head, listview, false));
		adapter = new AuthInfoAdapter(authNodes, groups, AuthInfoActivity.this, sure, cancel);
		listview.setAdapter(adapter);
		
		listview.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// TODO Auto-generated method stub
				Log.e("ppp",""+groupPosition);
				if(!listview.isGroupExpanded(groupPosition))listview.expandGroup(groupPosition);
				else listview.collapseGroup(groupPosition);
				// 设置被选中的group置于顶端
				listview.setSelectedGroup(groupPosition);
				return true;
			}
		});
	}

	

}
