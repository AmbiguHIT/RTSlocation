package com.ambigu.rtslocation;

import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Info;
import com.ambigu.model.ReqScheme;
import com.ambigu.navi.NaviGuideActivity;
import com.ambigu.route.RouteSimulate;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SharingActivity extends Activity {

	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private MapStatusUpdate msu = null;
	private static boolean isOnDriving=false;
	// 简化代码 用于this.context=this;
	private TextView speed,distance,time;
	private Button stopsharing;

	// 地点检索
	Info info = null;
	LatLng oldLatLng = null;

	private MapReceiver receiver;// 广播

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.sharing_map);
		Intent intent = this.getIntent();
		info = (Info) intent.getExtras().get("info");
		oldLatLng = new LatLng(info.getLat(), info.getLng());
		

		setTitle("当前正在与 " + info.getFromUser() + " 共享");
		initView();
		initReceiver();
	}

	private void initReceiver() {
		// TODO Auto-generated method stub
		receiver = new MapReceiver();
		IntentFilter filter = new IntentFilter("com.ambigu.service.SharingService");
		SharingActivity.this.registerReceiver(receiver, filter);
	}

	private void initView() {
		// TODO Auto-generated method stub
		mMapView = (MapView) findViewById(R.id.sharingmap);
		mBaiduMap = mMapView.getMap();
		speed=(TextView)findViewById(R.id.speed);
		time=(TextView)findViewById(R.id.time);
		distance=(TextView)findViewById(R.id.dis);
		stopsharing=(Button)findViewById(R.id.stopsharing);
		//加上起点标记
		MarkerOptions marker = new MarkerOptions();
		marker.position(oldLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.start_point));
		mBaiduMap.addOverlay(marker);
		
		mMapView.invalidate();
		// 设置打开时的显示比列 这里显示500m左右
		MapStatus mMapStatus = new MapStatus.Builder()
		        .target(oldLatLng)
		        .zoom(15.0f)
		        .build();
		msu = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		mBaiduMap.setMapStatus(msu);
		

		if(info.isfirst()){
			isOnDriving=true;
			speed.setText(info.getSpeed()+"");
			distance.setText(info.getDistance()+"");
			String tString=info.getTime();
			time.setText(tString.substring(tString.indexOf(" ")+1));
			Log.e("ifn","kjdsj");
		}
		
		stopsharing.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new Builder(SharingActivity.this);
				//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
				builder.setIcon(R.drawable.ic_launcher);
				//设置对话框标题
				builder.setTitle("提示信息");
				//设置对话框内的文本
				builder.setMessage("您确定要结束行程？");
				//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
				builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				        @Override
				        public void onClick(DialogInterface dialog, int which) {
				        	dialog.dismiss();
				        	//发送共享结束信息
							Info info1=new Info();
							info1.setFromUser(ApplicationVar.getId());
							info1.setToUser(info.getFromUser());
							info1.setDrivingstate(0);
							info1.setend(true);
							info1.setState(true);
							info1.setReqScheme(ReqScheme.BE_SHARED_PARTY);
							info1.setInfoType(EnumInfoType.SHARING_REQ);
							// 发送广播
							Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
							Bundle broadCastBundle = new Bundle();
							broadCastBundle.putSerializable("info", info1);
							broadCastIntent.putExtra("info", broadCastBundle);
							sendBroadcast(broadCastIntent);
				        }
				});
				builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
				
				//使用builder创建出对话框对象
				AlertDialog dialog = builder.create();
				//显示对话框
				dialog.show();
			}
		});
	}

	private void addOverLay(LatLng latLng) {
		MarkerOptions marker = new MarkerOptions();
		marker.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.end_point));
		mMapView.getMap().addOverlay(marker);
		mMapView.invalidate();
	}

	private void addLine(LatLng old, LatLng neww) {
		RouteSimulate rs = new RouteSimulate(this, old, neww,true);
		rs.init(mMapView, DrivingScheme.DRIVING);
		rs.doClick(DrivingScheme.DRIVING);
	}

	// activity销毁时百度地图也销毁
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		SharingActivity.this.unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return true;
	}

	// 菜单按钮的响应事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
		}
	}

	public class MapReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getBundleExtra("info");
			Info info = (Info) bundle.get("info");
			if(isOnDriving){
				double lat = info.getLat();
				double lng = info.getLng();
				Log.e("cast", info.toString());
				LatLng latLng = new LatLng(lat, lng);
				
				addLine(oldLatLng, latLng);
				oldLatLng = latLng;
				//addOverLay(latLng);
				// 进入到中心点
				MapStatus mMapStatus = new MapStatus.Builder()// 定义地图状态
						.target(latLng).zoom(15.0f).build(); // 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
				MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
				mBaiduMap.setMapStatus(mMapStatusUpdate);// 改变地图状态
				
				speed.setText(info.getSpeed()+"");
				distance.setText(info.getDistance()+"");
				String tString=info.getTime();
				time.setText(tString.substring(tString.indexOf(" ")+1));
				
				//判断是否到终点
				if(info.isend()){
					addOverLay(latLng);
					isOnDriving=false;
				}
			}
		}

	}

}