package com.ambigu.adapter;

import java.util.ArrayList;

import com.ambigu.model.Point;
import com.ambigu.model.ShareMessage;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.rtslocation.MyLocationHistoryActivity;
import com.ambigu.rtslocation.R;
import com.ambigu.settings.HistoryMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SwipeLocationHistoryAdapter extends BaseAdapter {

	private ArrayList<ShareMessage> shareMessages;
	private Context context;
	private ViewHolder holder;
	
	public SwipeLocationHistoryAdapter(ArrayList<ShareMessage> shareMessages,Context context) {
		// TODO Auto-generated constructor stub
		this.shareMessages=shareMessages;
		this.context=context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return shareMessages.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return shareMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(getCount()!=0){
			if(convertView==null){
				convertView = LayoutInflater.from(context).inflate(R.layout.self_location_history_item, parent, false);
				holder = new ViewHolder();
				holder.distance=(TextView)convertView.findViewById(R.id.distance);
				holder.end_point=(TextView)convertView.findViewById(R.id.end_point);
				holder.start_point=(TextView)convertView.findViewById(R.id.start_point);
				holder.end_time=(TextView)convertView.findViewById(R.id.stop_time);
				holder.start_time=(TextView)convertView.findViewById(R.id.start_time);
				convertView.setTag(R.id.tag_holder, holder);
				convertView.setTag(R.id.tag_childpositon,position);
			}
			ShareMessage shareMessage=shareMessages.get(position);
			ArrayList<Point> points=shareMessage.getLatlngList();
			
			holder.distance.setText(points.get(points.size()-1).getDistance());
			holder.start_point.setText(shareMessage.getStart_point());
			holder.start_time.setText(shareMessage.getStart_time());
			holder.end_point.setText(shareMessage.getEnd_point());
			holder.end_time.setText(shareMessage.getEnd_time());
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SingleSharingHistoryInfo singleSharingHistoryInfo=new SingleSharingHistoryInfo();
					ShareMessage shareMessage=shareMessages.get(position);
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
					if(points.size()!=0){
						String diss=points.get(points.size()-1).getDistance();
						if(diss!=null) dis=Double.parseDouble(diss);
						else dis=0;
					}
					singleSharingHistoryInfo.setDistance(Double.toString(dis));
					Intent intent =new Intent(context,HistoryMap.class);
					Bundle bundle=new Bundle();
					bundle.putSerializable("mapinfo", singleSharingHistoryInfo);
					intent.putExtras(bundle);
					context.startActivity(intent);
					Log.e("tag","doclick");
				}
			});
			
		}
		
		return convertView;
	}

	private class ViewHolder{
		public TextView start_time;
		public TextView end_time;
		public TextView start_point;
		public TextView end_point;
		public TextView distance;
		
	}
}
