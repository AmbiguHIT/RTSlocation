package com.ambigu.rtslocation;

import com.ambigu.listener.MyOrientationListener;
import com.ambigu.listener.MyOrientationListener.OnOrientationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class Ed_driveActivity extends Activity {

	private BitmapDescriptor mIconLocation;
	private LocationClient mLocationClient;
	private MyOrientationListener myOrientationListener;
	boolean isFirstIn = true;
	private MyLocationListener mLocationListener;
	private float mCurrentX;
	public BaiduMap mBaiduMap;
	private MapView mapview;
	private LocationMode mLocationMode;// 锟斤拷锟斤拷模式
	public double mLatitude;
	public double mLongtitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		SDKInitializer.initialize(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ed_drive);
		mapview = (MapView) findViewById(R.id.ed_drive_map);
		mBaiduMap = mapview.getMap();
		// 锟斤拷始锟斤拷锟斤拷位
		initLocation();
	}

	private void initLocation() {
		// TODO Auto-generated method stub
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(1000);

		mLocationClient.setLocOption(option);

		// 锟斤拷始锟斤拷图锟斤拷
		mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
		myOrientationListener = new MyOrientationListener(this);
		myOrientationListener.setOnOrientationListener(new OnOrientationListener() {
			@Override
			public void onOrientationChanged(float x) {
				mCurrentX = x;
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 锟斤拷activity执锟斤拷onDestroy时执锟斤拷mMapView.onDestroy()锟斤拷实锟街碉拷图锟斤拷锟斤拷锟斤拷锟节癸拷锟斤拷
		mapview.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 锟斤拷锟斤拷锟斤拷位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted())
			mLocationClient.start();
		// 锟斤拷锟斤拷锟斤拷锟津传革拷锟斤拷
		myOrientationListener.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 停止锟斤拷位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止锟斤拷锟津传革拷锟斤拷
		myOrientationListener.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 锟斤拷activity执锟斤拷onResume时执锟斤拷mMapView. onResume ()锟斤拷实锟街碉拷图锟斤拷锟斤拷锟斤拷锟节癸拷锟斤拷
		mapview.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 锟斤拷activity执锟斤拷onPause时执锟斤拷mMapView. onPause ()锟斤拷实锟街碉拷图锟斤拷锟斤拷锟斤拷锟节癸拷锟斤拷
		mapview.onPause();
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			MyLocationData data = new MyLocationData.Builder()//
					.direction(mCurrentX)//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			mBaiduMap.setMyLocationData(data);
			// 锟斤拷锟斤拷锟皆讹拷锟斤拷图锟斤拷
			MyLocationConfiguration config = new MyLocationConfiguration(mLocationMode.NORMAL, true, mIconLocation);
			mBaiduMap.setMyLocationConfigeration(config);
			// 锟斤拷取锟斤拷锟铰撅拷纬锟斤拷
			mLatitude = location.getLatitude();
			mLongtitude = location.getLongitude();
			// 锟叫讹拷锟角凤拷锟揭伙拷味锟轿�
			if (isFirstIn) {
				// 锟斤拷锟矫撅拷纬锟斤拷
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(msu);
				// 锟斤拷锟角凤拷锟揭伙拷味锟轿伙拷谋锟街� 锟斤拷为锟斤拷锟角碉拷一锟轿讹拷位
				isFirstIn = false;
				// 锟斤拷示锟斤拷前锟斤拷位锟斤拷位锟斤拷
			}

		}

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

	}

}
