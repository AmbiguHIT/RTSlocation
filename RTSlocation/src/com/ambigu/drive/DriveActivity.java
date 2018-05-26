package com.ambigu.drive;

import java.util.concurrent.Semaphore;

import com.ambigu.client.RTSClient;
import com.ambigu.listener.MyOrientationListener;
import com.ambigu.listener.MyOrientationListener.OnOrientationListener;
import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Info;
import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.DateUtil;
import com.ambigu.util.EnumInfoType;
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
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DriveActivity extends Activity {

	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private MapStatusUpdate msu = null;
	private Button ipbt = null;
	private EditText ipet = null;
	// 简化代码 用于this.context=this;
	private Context context;
	// 定位相关
	private LocationClient mLocationClient;

	private MyLocationListener mLocationListener;
	
	private static LatLng oldlatLng;
	// 是否第一次定位的标志
	private boolean isFirstIn = true;
	// 自定义定位图标
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	Thread latlngtThread = null;

	double oldlat = 0, oldlng = 0;
	private int times=10;
	// 通信信息
	Info info = null;
	static String fid="";

	// 地点检索
	boolean isfirstloc = true;
	static Semaphore drivesemaphore = new Semaphore(1);
	static Semaphore latlngsemaphore = new Semaphore(1);

	private static Handler drive_handler;
	private static Handler latLng_Handler;
	private boolean isPressed=false;
	private Handler overlayhandler;
	private static boolean sendable=false;
	private LocationClientOption option;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.ed_drive);
		this.context = this;
		initView();
		// 初始化定位
		initLocation();
		initSharing(ipbt);
		
	}
	

	private void initSharing(final Button ipbt) {
		// TODO Auto-generated method stub
		ipbt.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("HandlerLeak")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isPressed) {//点击了开始共享
					sendable=true;
					fid = ipet.getText().toString();
					if (fid == "" || fid == null) {
						AlertDialog.Builder builder1 = new Builder(DriveActivity.this);
						builder1.setTitle("提示信息");
						builder1.setMessage("未输入好友ID！");
						builder1.setNegativeButton("确定", null);
						builder1.show();
					} else {
						final ProgressDialog pDialog = new ProgressDialog(DriveActivity.this);
						pDialog.setMessage("正在连接中！");
						pDialog.setTitle("连接提示");
						Window window = pDialog.getWindow();
						WindowManager.LayoutParams lParams = window.getAttributes();
						lParams.alpha = 0.7f;
						lParams.dimAmount = 0.4f;
						window.setAttributes(lParams);
						// 隐藏键盘
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
								DriveActivity.this.getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
						pDialog.show();

						try {
							drivesemaphore.acquire();
							drive_handler = new Handler() {
								@Override
								public void handleMessage(android.os.Message msg) {
									info = (Info) msg.obj;
									if (info.getInfoType() == EnumInfoType.SHARING_RES && info.isState()) {
										pDialog.dismiss();
										// 开始全面发送数据
										if(sendable){
											isPressed=true;
											ipbt.setText("停止共享");
											// 向子线程发送信息，同时要求子线程发送当前位置
											Message _msg = Message.obtain();
											_msg.obj = info;
											_msg.what = 2;
											sendMsgToLatLngThread(_msg);
										}
									} else if (info.getInfoType() == EnumInfoType.SHARING_RES && !info.isState()) {
										if (pDialog.isShowing())
											pDialog.dismiss();
										isPressed=false;
										AlertDialog.Builder normalDialog = new AlertDialog.Builder(DriveActivity.this);
										normalDialog.setIcon(R.drawable.ic_launcher);
										normalDialog.setTitle("提示");
										normalDialog.setMessage("共享失败");
										normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// ...To-do
											}
										});
										normalDialog.show();
									}
								};
							};
							drivesemaphore.release();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							isPressed=false;
						}
						new Thread(new Runnable() {
							@Override
							public void run() {
								mLocationClient.requestLocation();
								BDLocation location = mLocationClient.getLastKnownLocation();
								double lat = location.getLatitude();
								double lng = location.getLongitude();
								oldlatLng=new LatLng(lat, lng);
								addOverLay(oldlatLng,1);
								Info reinfo = new Info();
								reinfo.setFromUser(ApplicationVar.getId());
								reinfo.setInfoType(EnumInfoType.SHARING_REQ);
								reinfo.setDrivingScheme(DrivingScheme.DRIVING);
								reinfo.setLat(lat);
								reinfo.setLng(lng);
								reinfo.setCity_now(location.getCity());
								reinfo.setTime(DateUtil.getTime());
								reinfo.setToUser(fid);
								reinfo.setfirst(true);
								Message msg = Message.obtain();
								msg.obj = reinfo;
								RTSClient.writeAndFlush(reinfo);
							}
						}).start();
					}
				} else {
					ipbt.setText("发起共享");
					sendable=false;
					isPressed=false;
					Message _msg=Message.obtain();
					_msg.what=0;
					_msg.obj=info;
					sendMsgToLatLngThread(_msg);
					addOverLay(oldlatLng,2);
					latlngtThread.interrupt();
				}
			}
		});
		
		
		latlngtThread = new Thread(new Runnable() {//此线程持续发送定位信息
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 共享回复
				try {
					latlngsemaphore.acquire();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Looper.prepare();
				latLng_Handler = new Handler() {// 2、绑定handler到CustomThread实例的Looper对象
					public void handleMessage(Message msg) {// 3、定义处理消息的方法
						Info info = (Info) msg.obj;
						if (info.getInfoType() == EnumInfoType.SHARING_RES && info.isState()) {
							try {
								Log.e("buttontag",isPressed+"");
								mLocationClient.requestLocation();
								BDLocation location = mLocationClient.getLastKnownLocation();
								double lat = location.getLatitude();
								double lng = location.getLongitude();
								oldlatLng=new LatLng(lat, lng);
								Info reinfo = new Info();
								reinfo.setFromUser(ApplicationVar.getId());
								reinfo.setLat(lat);
								reinfo.setLng(lng);
								reinfo.setToUser(fid);
								reinfo.setCity_now(location.getCity());
								reinfo.setTime(DateUtil.getTime());
								reinfo.setInfoType(EnumInfoType.SHARING_REQ);
								reinfo.setDrivingScheme(DrivingScheme.DRIVING);
								reinfo.setDrivingstate(msg.what);
								RTSClient.writeAndFlush(reinfo);
								
								//生成路线
								
								
								Thread.sleep(2000);
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
				};
				latlngsemaphore.release();
				Looper.loop();
			}
		});
	}
	
	private void addOverLay(LatLng latLng,int start_end) {
		
		Message message=Message.obtain();
		if(start_end==1)	message.what=1;
		else message.what=2;
		message.obj=latLng;
		overlayhandler.sendMessage(message);
	}

	private void initView() {
		// TODO Auto-generated method stub
		mMapView = (MapView) findViewById(R.id.ed_drive_map);
		mBaiduMap = mMapView.getMap();
		// 设置打开时的显示比列 这里显示500m左右
		msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		ipbt = (Button) findViewById(R.id.ip_bt);
		ipet = (EditText) findViewById(R.id.ip_in);
		
		overlayhandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				LatLng latLng=(LatLng)msg.obj;
				MarkerOptions marker = new MarkerOptions();
				if(msg.what==1)marker.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.end_point));
				else marker.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.start_point));
				mMapView.getMap().addOverlay(marker);
				mMapView.invalidate();
			}
		};
	}

	private void initLocation() {
		// TODO Auto-generated method stub
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);
		option = new LocationClientOption();
		option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式  
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(5000);

		mLocationClient.setLocOption(option);

		// 初始化图标
		mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
		myOrientationListener = new MyOrientationListener(context);
		myOrientationListener.setOnOrientationListener(new OnOrientationListener() {
			@Override
			public void onOrientationChanged(float x) {
				mCurrentX = x;
			}
		});

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
		// 开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted())
			mLocationClient.start();
		// 开启方向传感器
		myOrientationListener.start();
		latlngtThread.start();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止方向传感器
		myOrientationListener.stop();
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

	private class MyLocationListener implements BDLocationListener {

		@SuppressWarnings("deprecation")
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
			// 设置自定义图标
			MyLocationConfiguration config = new MyLocationConfiguration(LocationMode.NORMAL, true, mIconLocation);
			mBaiduMap.setMyLocationConfigeration(config);
			location.getLatitude();
			location.getLongitude();
			
			SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
			times=sharedPreferences.getInt("update_times", 10);
			option.setScanSpan(times*1000);
			mLocationClient.setLocOption(option);
			
			// 判断是否第一次定位
			if (isFirstIn) {
				// 设置经纬度
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(msu);
				// 将是否第一次定位的标志 变为不是第一次定位
				isFirstIn = false;
				// 显示当前定位的位置
				Toast.makeText(context, location.getAddrStr(), Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
		}
	}


	public static void sendMsgToDriveHandler(Message msg) {
		if (drive_handler == null) {
			try {
				drivesemaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		drive_handler.sendMessage(msg);
	}

	public static void sendMsgToLatLngThread(Message msg) {
		if (drive_handler == null) {
			try {
				latlngsemaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		latLng_Handler.sendMessage(msg);
	}

}