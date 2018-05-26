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

public class HistoryMap extends Activity {

	private MapView mapView = null;

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
		SingleSharingHistoryInfo sharingHistoryInfo = (SingleSharingHistoryInfo) intent.getExtras().get("mapinfo");
		ArrayList<Point> latlngList = sharingHistoryInfo.getLatlngList();
		initView(latlngList);
	}

	private void initView(ArrayList<Point> latlngList) {
		// TODO Auto-generated method stub
		mapView = (MapView) findViewById(R.id.history_map);
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
