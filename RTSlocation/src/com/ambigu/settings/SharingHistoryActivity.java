package com.ambigu.settings;

import java.util.ArrayList;

import com.ambigu.adapter.SharingHistoryAdapter;
import com.ambigu.model.Info;
import com.ambigu.model.Point;
import com.ambigu.model.ShareMessage;
import com.ambigu.model.ShareMessageOfPerson;
import com.ambigu.model.SharingHistoryOfPerson;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.rtslocation.R;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.baidu.mapapi.model.LatLng;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class SharingHistoryActivity extends Activity {


	private PinnedHeaderExpandableListView sharinglist;
	private SharingHistoryAdapter adapter;
	private ArrayList<SharingHistoryOfPerson> sharingHistorys;
	private Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharing_history);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initData();//该操作较为复杂，开多线程去做
				mHandler.sendEmptyMessage(0);
			}
		}).start();
		
		mHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				initView();
				
			}
		};
	}

	private void initData() {
		// TODO Auto-generated method stub
		Intent intent=this.getIntent();
		Info info=(Info) intent.getExtras().get("info");
		//进行转换
		ArrayList<ShareMessageOfPerson> shareMessageOfPersons=info.getShareMessageOfPersons();
		sharingHistorys=new ArrayList<SharingHistoryOfPerson>();
		for(int i=0;i<shareMessageOfPersons.size();i++){
			ShareMessageOfPerson shareMessageOfPerson=shareMessageOfPersons.get(i);
			
			SharingHistoryOfPerson sharingHistoryOfPerson=new SharingHistoryOfPerson();
			sharingHistoryOfPerson.setFromUser(shareMessageOfPerson.getFromUser());
			sharingHistoryOfPerson.setToUser(shareMessageOfPerson.getToUser());
			Log.e("ToUser",shareMessageOfPerson.getToUser());
			ArrayList<ShareMessage> shareMessages =shareMessageOfPerson.getShareMessages();
			ArrayList<SingleSharingHistoryInfo> singleSharingHistoryInfos=new ArrayList<SingleSharingHistoryInfo>();
			for(int j=0;j<shareMessages.size();j++){
				ShareMessage shareMessage=shareMessages.get(j);
				SingleSharingHistoryInfo singleSharingHistoryInfo=new SingleSharingHistoryInfo();
				singleSharingHistoryInfo.setFromUser(shareMessage.getFromUser());
				singleSharingHistoryInfo.setToUser(shareMessage.getToUser());
				singleSharingHistoryInfo.setStart_point(shareMessage.getStart_point());
				singleSharingHistoryInfo.setEnd_point(shareMessage.getEnd_point());
				singleSharingHistoryInfo.setStart_time(shareMessage.getStart_time());
				singleSharingHistoryInfo.setEnd_time(shareMessage.getEnd_time());
				ArrayList<Point> points=shareMessage.getLatlngList();
				singleSharingHistoryInfo.setLatlngList(points);
				
				//生成距离
				double dis=0.0;
				for(int k=0;k<points.size();k++){
					if(points.get(k).getDistance()==null) dis+=0.0;
					else dis+=Double.parseDouble(points.get(k).getDistance());
				}
				singleSharingHistoryInfo.setDistance(Double.toString(dis));
				
				singleSharingHistoryInfos.add(singleSharingHistoryInfo);
			}
			sharingHistoryOfPerson.setSharingHistoryInfos(singleSharingHistoryInfos);
			sharingHistorys.add(sharingHistoryOfPerson);
		}
		Log.e("listsize",""+sharingHistorys.size());

	}

	private void initView() {
		// TODO Auto-generated method stub
		sharinglist=(PinnedHeaderExpandableListView)findViewById(R.id.sharinghistory);

		sharinglist.setHeaderView(getLayoutInflater().inflate(R.layout.sharing_history_group_head, sharinglist, false));
		adapter=new SharingHistoryAdapter(sharingHistorys, this, sharinglist);
		sharinglist.setAdapter(adapter);
		
		sharinglist.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				int groupposition=Integer.parseInt(""+view.getTag(R.id.tag_grouppositon));
				int childposition=Integer.parseInt(""+view.getTag(R.id.tag_childpositon));
				SingleSharingHistoryInfo singleSharingHistoryInfo=sharingHistorys.get(groupposition).getSharingHistoryInfos().get(childposition);
				Intent intent =new Intent(SharingHistoryActivity.this,HistoryMap.class);
				Bundle bundle=new Bundle();
				bundle.putSerializable("mapinfo", singleSharingHistoryInfo);
				intent.putExtras(bundle);
				startActivity(intent);
				return true;
			}
			
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

}
