package com.ambigu.drive;

import com.ambigu.client.RTSClient;
import com.ambigu.listener.MyOrientationListener;
import com.ambigu.listener.MyOrientationListener.OnOrientationListener;
import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Info;
import com.ambigu.route.RouteSimulate;
import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.DateUtil;
import com.ambigu.util.EnumInfoType;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import android.R.integer;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class SharingOptions {
	private LocationClient mLocationClient;
	private Activity activity;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private MyLocationListener mLocationListener;
	private LocationClientOption option;
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	private String toUser;
	private double distance;
	private LatLng oldLatLng;
	private boolean sendable=false;

	public SharingOptions(LocationClient mLocationClient, Activity activity, MapView mMapView,String toUser) {
		this.mLocationClient = mLocationClient;
		this.activity = activity;
		this.mMapView = mMapView;
		if(mMapView!=null) 
			this.mBaiduMap = mMapView.getMap();
		this.toUser=toUser;

	}

	// 杩涜location鐨勫垵濮嬪寲閰嶇疆
	public void init() {
		if (!mLocationClient.isStarted()) {
			Log.e("log","已结束");
			mLocationListener = new MyLocationListener();
			mLocationClient.registerLocationListener(mLocationListener);
			option = new LocationClientOption();
			option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);// 璁剧疆瀹氫綅妯″紡
			option.setCoorType("bd09ll");
			option.setIsNeedAddress(true);
			option.setOpenGps(true);
			option.setScanSpan(1000);
			mLocationClient.setLocOption(option);
		}

		if (myOrientationListener == null) {
			myOrientationListener = new MyOrientationListener(activity);
			myOrientationListener.setOnOrientationListener(new OnOrientationListener() {

				@Override
				public void onOrientationChanged(float x) {
					mCurrentX = x;
				}
			});
		}

		// 鍒濆鍖栧浘鏍�
		mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
		start();
	}

	// 寮�濮嬪畾浣�
	public void start() {
		myOrientationListener.start();
		mLocationClient.start();
	}

	// 鍋滄瀹氫綅
	public void stop() {
		myOrientationListener.stop();
		mLocationClient.stop();
	}

	public void send() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(sendable){
					BDLocation location=mLocationClient.getLastKnownLocation();
					Info info = new Info();
					info.setFromUser(ApplicationVar.getId());
					info.setToUser(toUser);
					info.setInfoType(EnumInfoType.SHARING_REQ);
					info.setTime(DateUtil.getTime());
					info.setSpeed(location.getSpeed());
					info.setLat(location.getLatitude());
					info.setLng(location.getLongitude());
					info.setAccuracy(location.getRadius());
					info.setDirection(mCurrentX);
					info.setDistance(distance+"");
					info.setfirst(false);
					info.setend(false);
					RTSClient.writeAndFlush(info);
					SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
					int times=sharedPreferences.getInt("update_times", 10);
					try {
						Thread.sleep(times*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public void sendFirst(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub

				BDLocation location=mLocationClient.getLastKnownLocation();
				while(location==null){
					try {
						Thread.sleep(100);
						location=mLocationClient.getLastKnownLocation();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				oldLatLng=new LatLng(location.getLatitude(), location.getLongitude());
				Info info = new Info();
				info.setFromUser(ApplicationVar.getId());
				info.setToUser(toUser);
				info.setInfoType(EnumInfoType.SHARING_REQ);
				info.setTime(DateUtil.getTime());
				info.setSpeed(location.getSpeed());
				info.setLat(location.getLatitude());
				info.setLng(location.getLongitude());
				info.setAccuracy(location.getRadius());
				info.setDirection(mCurrentX);
				info.setDistance(0+"");
				info.setfirst(true);
				info.setend(false);
				RTSClient.writeAndFlush(info);
			}
		}).start();
	}
	
	public void sendEnd(){
		BDLocation location=mLocationClient.getLastKnownLocation();
		Info info = new Info();
		info.setFromUser(ApplicationVar.getId());
		info.setToUser(toUser);
		info.setInfoType(EnumInfoType.SHARING_REQ);
		info.setTime(DateUtil.getTime());
		info.setSpeed(location.getSpeed());
		info.setLat(location.getLatitude());
		info.setLng(location.getLongitude());
		info.setAccuracy(location.getRadius());
		info.setDirection(mCurrentX);
		LatLng latLng=new LatLng(location.getLatitude(), location.getLongitude());
		oldLatLng=latLng;
		info.setDistance(distance+"");
		info.setfirst(false);
		info.setend(true);
		RTSClient.writeAndFlush(info);
	}

	private void searchRoute(LatLng sNode, LatLng eNode) {

		RoutePlanSearch search = RoutePlanSearch.newInstance(); // 鐧惧害鐨勬悳绱㈣矾绾跨殑绫�
		DrivingRoutePlanOption drivingRoutePlanOption = new DrivingRoutePlanOption();
		// 璧峰鍧愭爣鍜岀粓鐐瑰潗鏍�
		PlanNode startPlanNode = PlanNode.withLocation(sNode); // lat
																											// long
		PlanNode endPlanNode = PlanNode.withLocation(eNode);
		drivingRoutePlanOption.from(startPlanNode);
		drivingRoutePlanOption.to(endPlanNode);
		search.drivingSearch(drivingRoutePlanOption);

		search.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() { // 鎼滅储瀹屾垚鐨勫洖璋�
			@Override
			public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) { // 姝ヨ璺嚎
				if (walkingRouteResult.getRouteLines() == null)
					return;
				distance += walkingRouteResult.getRouteLines().get(0).getDistance();
			}

			@Override
			public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
			}

			@Override
			public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) { // 椹捐溅璺嚎
				if (drivingRouteResult.getRouteLines() == null) {
					return;
				}
				distance += drivingRouteResult.getRouteLines().get(0).getDistance();
			}

			@Override
			public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

			}

			@Override
			public void onGetIndoorRouteResult(IndoorRouteResult arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetMassTransitRouteResult(MassTransitRouteResult arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void addLineToMap(LatLng old,LatLng neww,boolean isClear){
		RouteSimulate rs = new RouteSimulate(activity, old, neww,isClear);
		rs.init(mMapView, DrivingScheme.DRIVING);
		rs.doClick(DrivingScheme.DRIVING);
	}

	public boolean isSendable() {
		return sendable;
	}

	public void setSendable(boolean sendable) {
		this.sendable = sendable;
	}

	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			LatLng latLng=new LatLng(location.getLatitude(), location.getLongitude());
			searchRoute(oldLatLng, latLng);//计算距离
			Log.e("sharingOptions","receivcer");
		}

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	}

}
