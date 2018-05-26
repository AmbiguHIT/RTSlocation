package com.ambigu.rtslocation;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnAcquireAuthLatLngListener;
import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Info;
import com.ambigu.route.RouteSimulate;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.DateUtil;
import com.ambigu.util.EnumInfoType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class AcquireAuthLatlngActivity extends Activity implements OnAcquireAuthLatLngListener {
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private String toUser;
	private Handler mHandler;
	private static boolean isFirst=true;
	double lat,lng;
	private LatLng oldLatlng;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.auth_latlng_map);
		initView();
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		toUser = bundle.getString("username");

		// 发送消息请求位置
		Info info = new Info();
		info.setFromUser(ApplicationVar.getId());
		info.setToUser(toUser);
		info.setAuthLatlng(true);
		info.setTime(DateUtil.getTime());
		info.setInfoType(EnumInfoType.GET_AUTH_LATLNG);
		RTSClient.writeAndFlush(info);

		DiscardClientHandler.getInstance().setOnAcquireAuthLatLngListener(this);
		
		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				Info info=(Info)msg.obj;
				switch (msg.what) {
				case 0:
					if(!AcquireAuthLatlngActivity.this.isFinishing()){
						AlertDialog.Builder builder = new Builder(AcquireAuthLatlngActivity.this);
						//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						//设置对话框标题
						builder.setTitle("提示信息");
						//设置对话框内的文本
						builder.setMessage("并未发现好友有共享记录");
						//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", null);
						//使用builder创建出对话框对象
						AlertDialog dialog = builder.create();
						//显示对话框
						dialog.show();
					}
					break;
				case 1:
					switch (info.getInfoType()) {
					case GET_AUTH_LATLNG:
						if(isFirst){
							Log.e("GET_AUTH_LATLNG","first");
							isFirst=false;
							lat=info.getLat();
							lng=info.getLng();
							oldLatlng=new LatLng(lat, lng);
							addOverLayer(oldLatlng,R.drawable.start_point);
							addNowLocation(oldLatlng,info.getAccuracy(),info.getDirection());
						}else{
							Log.e("GET_AUTH_LATLNG","later");
							double lat=info.getLat();
							double lng=info.getLng();
							LatLng latlng=new LatLng(lat,lng);
							addLine(oldLatlng, latlng);
							addNowLocation(latlng,info.getAccuracy(),info.getDirection());
							oldLatlng=latlng;
						}
						break;
					case CLOSE_AUTH_LATLNG:
						isFirst=true;
						if(!AcquireAuthLatlngActivity.this.isFinishing()){
							double lat=info.getLat();
							double lng=info.getLng();
							LatLng latlng=new LatLng(lat, lng);
							addLine(oldLatlng,latlng);
							oldLatlng=latlng;
							addNowLocation(latlng,info.getAccuracy(),info.getDirection());
							addOverLayer(oldLatlng,R.drawable.end_point);
						}else{
							Toast.makeText(getApplicationContext(), "您已经不在查看好友位置了", Toast.LENGTH_LONG).show();
						}
						break;

					default:
						break;
					}
					break;

				default:
					break;
				}
				
			}
		};
	}
	
	private void addOverLayer(LatLng latLng,int resid){
		//构建Marker图标  
		BitmapDescriptor bitmap = BitmapDescriptorFactory  
		    .fromResource(resid);  
		//构建MarkerOption，用于在地图上添加Marker 
		OverlayOptions option = new MarkerOptions()  
		    .position(latLng)  
		    .icon(bitmap); 
		//在地图上添加Marker，并显示  
		mBaiduMap.addOverlay(option);
	}
	
	private void addLine(LatLng old, LatLng neww) {
		RouteSimulate rs = new RouteSimulate(this, old, neww,true);
		rs.init(mMapView, DrivingScheme.DRIVING);
		rs.doClick(DrivingScheme.DRIVING);
	}
	
	private void addNowLocation(LatLng latLng,float accuracy,float direction){
		MyLocationData data = new MyLocationData.Builder()//
				.direction(direction)//
				.accuracy(accuracy)//
				.latitude(latLng.latitude)//
				.longitude(latLng.longitude)//
				.build();
		mBaiduMap.setMyLocationData(data);
		BitmapDescriptor mIconLocation=BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
		// 设置自定义图标
		MyLocationConfiguration config = new MyLocationConfiguration(LocationMode.NORMAL, true, mIconLocation);
		mBaiduMap.setMyLocationConfigeration(config);

		// 进入到中心点
		MapStatus mMapStatus = new MapStatus.Builder()// 定义地图状态
				.target(latLng).zoom(15.0f).build(); // 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		mBaiduMap.setMapStatus(mMapStatusUpdate);// 改变地图状态
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		mMapView = (MapView) findViewById(R.id.auth_map);
		mBaiduMap = mMapView.getMap();

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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
		// 结束当次查看
		Info info = new Info();
		info.setInfoType(EnumInfoType.CLOSE_AUTH_LATLNG);
		info.setFromUser(ApplicationVar.getId());
		info.setAuthLatlng(false);
		info.setToUser(toUser);
		info.setTime(DateUtil.getTime());
		RTSClient.writeAndFlush(info);
	}

	@Override
	public void OnAcquireAuthLatLng(Info info) {
		// TODO Auto-generated method stub
		if(info.isState()){
			Message message=Message.obtain();
			message.obj=info;
			message.what=1;
			mHandler.sendMessage(message);
		}else{
			mHandler.sendEmptyMessage(0);
		}
	}

}
