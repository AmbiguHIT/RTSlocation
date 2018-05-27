package com.ambigu.settings;

import java.util.ArrayList;

import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Point;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.route.RouteSimulate;
import com.ambigu.rtslocation.R;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class HistoryMap extends Activity {

	private MapView mapView = null;
	private TextView start_time;
	private TextView stop_time;
	private TextView start_point;
	private TextView end_point;
	private TextView distance;
	private SingleSharingHistoryInfo sharingHistoryInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.history_map);
		initData();

	}

	private void initData() {
		// TODO Auto-generated method stub
		Intent intent = this.getIntent();
		sharingHistoryInfo = (SingleSharingHistoryInfo) intent.getExtras().get("mapinfo");
		ArrayList<Point> latlngList = sharingHistoryInfo.getLatlngList();
		initView(latlngList);
	}

	private void initView(ArrayList<Point> latlngList) {
		// TODO Auto-generated method stub
		mapView = (MapView) findViewById(R.id.history_map);
		start_time=(TextView)findViewById(R.id.start_time);
		stop_time=(TextView)findViewById(R.id.stop_time);
		start_point=(TextView)findViewById(R.id.start_point);
		end_point=(TextView)findViewById(R.id.end_point);
		distance=(TextView)findViewById(R.id.distance);
		start_time.setText(sharingHistoryInfo.getStart_time());
		stop_time.setText(sharingHistoryInfo.getEnd_time());
		start_point.setText(sharingHistoryInfo.getStart_point());
		end_point.setText(sharingHistoryInfo.getEnd_point());
		distance.setText(sharingHistoryInfo.getDistance());
		
		BaiduMap mBaiduMap = mapView.getMap();
		LatLng firlatLng = null;
		boolean f = true;
		for (int i = 0; i < latlngList.size(); i++) {
			Point point = latlngList.get(i);
			LatLng latLng = new LatLng(Double.parseDouble(point.getLat()), Double.parseDouble(point.getLng()));
			if(f){
				MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(15.0f).build();
				// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
				MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
				// 改变地图状态
				mBaiduMap.setMapStatus(mMapStatusUpdate);
			}
			if (i == latlngList.size() / 2) {
				f = false;
			}
			if (i == 0) {
				firlatLng = latLng;
				MarkerOptions marker = new MarkerOptions();
				marker.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.start_point));
				mBaiduMap.addOverlay(marker);
			} else {
				addLine(firlatLng, latLng);
				firlatLng = latLng;
				if (i == latlngList.size() - 1) {
					MarkerOptions marker = new MarkerOptions();
					marker.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.end_point));
					mBaiduMap.addOverlay(marker);
				}
			}
		}
	}


	private void addLine(LatLng old, LatLng neww) {
		RouteSimulate rs = new RouteSimulate(this, old, neww,true);
		rs.init(mapView, DrivingScheme.DRIVING);
		rs.doClick(DrivingScheme.DRIVING);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
