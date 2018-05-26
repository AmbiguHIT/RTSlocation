package com.ambigu.service;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnMessageSendState;
import com.ambigu.listener.OnReceiveSharingMessageListener;
import com.ambigu.listener.OnSupportSettingsChangeListener;
import com.ambigu.model.Info;
import com.ambigu.model.ReqScheme;
import com.ambigu.model.Settings_Type;
import com.ambigu.rtslocation.R;
import com.ambigu.rtslocation.SharingActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.DateUtil;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.LogUtils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

public class SharingService extends Service implements SensorEventListener, OnMessageSendState, BDLocationListener,
		OnSupportSettingsChangeListener, OnReceiveSharingMessageListener {

	String latlng = "";
	static Handler handler = null;
	private static Handler sharing_handler;
	boolean flag = false;
	public static boolean isfirst = true;
	boolean isShow = false;
	Intent intent = null;
	Info info = null;
	public boolean f = true;
	private static Handler ip_Handler;
	private static boolean sendable = true;
	private double oldlat;
	private double oldlng;
	private boolean isFirst = true;
	private float mCurrentX;
	private volatile boolean isAuth;

	private LocationClient mLocationClient;
	private double lastX;
	private SensorManager mSensorManager;
	private MsgReceiver msgReceiver;
	private int inviupdate_times;
	private Thread authThread;
	private SharingReceiver sharingReceiver;
	private static double distance = 0;
	private LatLng oldLatlng = null;
	private boolean isSharing = false;
	private int update_times;

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
		initData();
		initBroadCast();
		initHandler();
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		sharing_handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final Info info = (Info) msg.obj;
				Bundle bundle = new Bundle();
				bundle.putSerializable("info", info);
				intent = new Intent(SharingService.this, SharingActivity.class);
				intent.putExtras(bundle);
				if (info.isfirst() && !isShow) {
					LogUtils.showLoG(SharingService.this, "first");
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(SharingService.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示");
					normalDialog.setMessage(info.getFromUser() + "请求与您共享TA的行程，是否接受?");
					normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// ...To-do
							Info reinfo = new Info();
							reinfo.setFromUser(ApplicationVar.getId());
							reinfo.setToUser(info.getFromUser());
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setReqScheme(ReqScheme.BE_SHARED_PARTY);
							reinfo.setState(true);
							reinfo.setfirst(true);
							reinfo.setend(false);
							reinfo.setLat(info.getLat());
							reinfo.setLng(info.getLng());
							reinfo.setCity_now(info.getCity_now());
							reinfo.setTime(DateUtil.getTime());
							RTSClient.writeAndFlush(reinfo);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
					normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// ...To-do
							dialog.dismiss();

							Info reinfo = new Info();
							reinfo.setFromUser(ApplicationVar.getId());
							reinfo.setToUser(info.getFromUser());
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setReqScheme(ReqScheme.BE_SHARED_PARTY);
							reinfo.setState(false);
							reinfo.setfirst(true);
							reinfo.setend(true);
							reinfo.setLat(info.getLat());
							reinfo.setLng(info.getLng());
							reinfo.setCity_now(info.getCity_now());
							reinfo.setTime(DateUtil.getTime());
							RTSClient.writeAndFlush(reinfo);
							isShow = false;
						}
					});
					// 显示
					AlertDialog ad = normalDialog.create();
					ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					ad.show();
					isShow = true;
				} else {
					// 发送广播
					Intent broadCastIntent = new Intent("com.ambigu.service.SharingService");
					Bundle broadCastBundle = new Bundle();
					broadCastBundle.putSerializable("info", info);
					broadCastIntent.putExtra("info", broadCastBundle);
					sendBroadcast(broadCastIntent);
					synchronized (broadCastIntent) {
						if (info.isend()) {// 行驶状态或者请求停止
							Info reinfo = new Info();
							reinfo.setFromUser(ApplicationVar.getId());
							reinfo.setToUser(info.getFromUser());
							reinfo.setReqScheme(ReqScheme.BE_SHARED_PARTY);
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setState(true);
							reinfo.setLat(info.getLat());
							reinfo.setLng(info.getLng());
							reinfo.setCity_now(info.getCity_now());
							reinfo.setTime(DateUtil.getTime());
							reinfo.setend(true);
							RTSClient.writeAndFlush(reinfo);
							isShow=false;
						} else {
							Log.e("发送广播", info.toString());
							Info reinfo = new Info();
							reinfo.setFromUser(ApplicationVar.getId());
							reinfo.setToUser(info.getFromUser());
							reinfo.setReqScheme(ReqScheme.BE_SHARED_PARTY);
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setState(true);
							reinfo.setLat(info.getLat());
							reinfo.setLng(info.getLng());
							reinfo.setCity_now(info.getCity_now());
							reinfo.setTime(DateUtil.getTime());
							reinfo.setend(false);
							RTSClient.writeAndFlush(reinfo);
						}
					}
				}
			}
		};
	}

	private void initBroadCast() {
		// TODO Auto-generated method stub
		msgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.ambigu.rtslocation.settings");
		registerReceiver(msgReceiver, intentFilter);

		sharingReceiver = new SharingReceiver();
		IntentFilter sharingFilter = new IntentFilter();
		sharingFilter.addAction("com.ambigu.rtslocation.RealTimeSharing");
		registerReceiver(sharingReceiver, sharingFilter);
	}

	/**
	 * 获得所在位置经纬度及详细地址
	 * 
	 * @param times
	 */
	private void initLocation() {
		// TODO Auto-generated method stub
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(2000);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	public void initData() {
		SharedPreferences sharedPreferences = getSharedPreferences("rtslocation", Context.MODE_PRIVATE);
		boolean isAuth = sharedPreferences.getBoolean("isAuth", false);
		inviupdate_times = sharedPreferences.getInt("inviupdate_times", 10);// 权限共享
		update_times=sharedPreferences.getInt("update_times", 10);
		initLocation();
		Log.e("isAuth", isAuth + "");
		if (isAuth) {
			this.isAuth = true;
			initAuthSharing();
		}
		DiscardClientHandler.getInstance().setOnReceiveSharingMessageListener(this);
	}

	public void initAuthSharing() {
		authThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {

					while (isAuth) {

						try {
							Log.e("hrewd", "daasdad");
							Info info = new Info();
							BDLocation location = mLocationClient.getLastKnownLocation();
							while (location == null) {
								Thread.sleep(100);
								location = mLocationClient.getLastKnownLocation();
							}
							info.setFromUser(ApplicationVar.getId());
							info.setLat(location.getLatitude());
							info.setLng(location.getLongitude());
							info.setAccuracy(location.getRadius());
							info.setCity_now(location.getAddrStr());
							info.setInfoType(EnumInfoType.AUTH_LATLNG);
							info.setDirection(mCurrentX);
							info.setTime(DateUtil.getTime());
							RTSClient.writeAndFlush(info);
							Thread.sleep(inviupdate_times * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					try {
						Thread.sleep(inviupdate_times * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		authThread.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mSensorManager.unregisterListener(this);
		// ApplicationVar.getBinder().getSharingService().onDestroy();
		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		mSensorManager.unregisterListener(this);
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
		return null;
	}

	@Override
	public void sendMessageState(Info info) {
		// TODO Auto-generated method stub
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("测试标题")// 设置通知栏标题
				.setContentText("测试内容") // 设置通知栏显示内容
				.setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL)) // 设置通知栏点击意图
				// .setNumber(number) //设置通知集合的数量
				.setTicker("测试通知来啦") // 通知首次出现在通知栏，带上升动画效果的
				.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
				.setPriority(Notification.PRIORITY_DEFAULT) // 设置该通知优先级
				// .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
				.setOngoing(false)// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
				.setDefaults(Notification.DEFAULT_VIBRATE)// 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
				// Notification.DEFAULT_ALL Notification.DEFAULT_SOUND 添加声音 //
				// requires VIBRATE permission
				.setSmallIcon(R.drawable.ic_launcher);// 设置通知小ICON
	}

	public PendingIntent getDefalutIntent(int flags) {
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
		return pendingIntent;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		// TODO Auto-generated method stub
		// 发送广播
		Intent broadCastIntent = new Intent("com.ambigu.service.SharingLocation");
		Bundle broadCastBundle = new Bundle();
		info = new Info();
		info.setLat(location.getLatitude());
		info.setLng(location.getLongitude());
		info.setAccuracy(location.getRadius());
		info.setCity_now(location.getAddrStr());
		info.setDirection(mCurrentX);
		broadCastBundle.putSerializable("info", info);
		broadCastIntent.putExtra("info", broadCastBundle);
		sendBroadcast(broadCastIntent);
	}

	@Override
	public void inviTimesChange() {
		// TODO Auto-generated method stub
		Log.e("inviupdate_times", "变化");
		SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
		inviupdate_times = sharedPreferences.getInt("inviupdate_times", 10);
	}

	@Override
	public void updateTimesChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void allowShareChange(boolean isAllow) {
		// TODO Auto-generated method stub
		Log.e("inviupdate_times1213", "变化");
		if (isAllow) {
			SharingService.this.isAuth = true;
		} else {
			SharingService.this.isAuth = false;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// TODO Auto-generated method stub
		double x = sensorEvent.values[SensorManager.DATA_X];
		if (Math.abs(x - lastX) > 1.0) {
			mCurrentX = (float) x;
		}
		lastX = x;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public class MsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 拿到系统设置改变内容
			Settings_Type type = (Settings_Type) intent.getSerializableExtra("type");
			SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
			boolean isAuth = sharedPreferences.getBoolean("isAuth", false);
			switch (type) {
			case UPDATE_INVITIMES:
				inviTimesChange();
				break;

			case AUTH_ALLOW:
				Log.e("isAuth", isAuth + "HELLO");
				allowShareChange(isAuth);
				break;

			default:
				break;
			}

		}

	}

	@Override
	public void onConnectHotSpotMessage(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dealMessage(final Info info) {
		// TODO Auto-generated method stub
		
		if(isSharing){//已经在共享状态
			
			return;
		}
		
		if (info.getInfoType() == EnumInfoType.SHARING_REQ && info.getReqScheme() == ReqScheme.SHARE_PARTY) {
			Message message = Message.obtain();
			message.obj = info;
			sharing_handler.sendMessage(message);
		} else if (info.getInfoType() == EnumInfoType.SHARING_RES && info.getReqScheme() == ReqScheme.SHARE_PARTY) {// 共享方同意取消行程
			if (!info.isState()) {

			}
		}
	}

	public class SharingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			boolean clearSharingState=intent.getBooleanExtra("clearSharingState", false);
			if(clearSharingState){
				isSharing=false;
				return;//这条广播信息不需要发送任何信息
			}

			Bundle bundle = intent.getBundleExtra("info");
			Info info = (Info) bundle.get("info");
			
			BDLocation location = mLocationClient.getLastKnownLocation();
			info.setLat(location.getLatitude());
			info.setLng(location.getLongitude());
			info.setAccuracy(location.getRadius());
			info.setCity_now(location.getAddrStr());
			info.setDirection(mCurrentX);
			info.setTime(DateUtil.getTime());
			info.setSpeed(location.getSpeed());
			if (info.isend())
				isSharing = false;
			if (info.isfirst()) {
				if (isSharing) {
					final AlertDialog.Builder normalDialog = new AlertDialog.Builder(SharingService.this);
					normalDialog.setIcon(R.drawable.ic_launcher);
					normalDialog.setTitle("提示");
					normalDialog.setMessage("您当前已经在共享行程，不可以再次邀请！");
					normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// ...To-do
							dialog.dismiss();
						}
					});
					// 显示
					AlertDialog ad = normalDialog.create();
					ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					ad.show();
					return;
				} else {
					distance = 0;
				}
			} else {
				distance+= info.getSpeed()*(double)update_times/3600;
			}
			isSharing = true;
			info.setDistance(distance + "");
			RTSClient.writeAndFlush(info);
		}

	}

}
