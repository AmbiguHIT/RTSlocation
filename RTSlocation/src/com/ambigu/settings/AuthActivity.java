package com.ambigu.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ambigu.adapter.AuthListAdapter;
import com.ambigu.listener.OnAuthChangedListener;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

public class AuthActivity extends Activity{
	private PinnedHeaderExpandableListView authlist;
	private AuthListAdapter adapter;
	private Button sure;
	ArrayList<Group> groups;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authlist);
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		//获取好友列表
		SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
		Gson gson=new Gson();
		Info info=(Info) gson.fromJson(sharedPreferences.getString("info", "{}"), Info.class);
		Log.e("infooo",sharedPreferences.getString("info", "{}"));
		HashMap<String, Group> friends=info.getFriendsList();
		groups=new ArrayList<Group>();
		Iterator<Entry<String, Group>> iterator=friends.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Group> entry=iterator.next();
			groups.add((Group)entry.getValue());//加入分组
		}
		Log.e("groupsize",groups.size()+"");
		initView(groups);
	}

	private void initView(final ArrayList<Group> groups) {
		// TODO Auto-generated method stub
		sure=(Button)findViewById(R.id.btn_enter);
		authlist=(PinnedHeaderExpandableListView)findViewById(R.id.auth_list);

		authlist.setHeaderView(getLayoutInflater().inflate(R.layout.auth_group_head, authlist, false));
		adapter=new AuthListAdapter(groups, this, authlist,sure);
		authlist.setAdapter(adapter);// 子条目点击事件
		//authlist.setOnChildClickListener(this);
		
		authlist.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// TODO Auto-generated method stub
				Log.e("ppp",""+groupPosition);
				if(!authlist.isGroupExpanded(groupPosition))authlist.expandGroup(groupPosition);
				else authlist.collapseGroup(groupPosition);
				// 设置被选中的group置于顶端
				authlist.setSelectedGroup(groupPosition);
				return true;
			}
		});
		
	}

}
