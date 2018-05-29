package com.ambigu.rtslocation;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.listener.OnSharingResListener;
import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Info;
import com.ambigu.model.ReqScheme;
import com.ambigu.route.RouteSimulate;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.LogUtils;
import com.ambigu.util.Utils;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SharingPartyActivity extends Activity implements OnSharingResListener{

	private Button stop;
	private TextView speed;
	private TextView start_point;
	private TextView stop_point;
	private TextView distance;
	private SharingReceiver receiver;
	private Thread reqthread;
	private boolean isSharing;
	private boolean isShow;
	private boolean isFirst;
	private boolean isEnd;
	protected boolean isClicked=true;
	private String toUser;
	public LatLng oldLatLng;
	private BitmapDescriptor start_descriptor;
	private BitmapDescriptor end_descriptor;
	private MarkerOptions start_marker;
	private MarkerOptions end_marker;
	private BaiduMap mBaiduMap;
	private MapView mMapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.real_sharing_map);
		initView();
		initSharing();
		initReciver();
		DiscardClientHandler.getInstance().setOnSharingResListener(this);
		

		start_descriptor = BitmapDescriptorFactory.fromResource(R.drawable.start_point);
		end_descriptor = BitmapDescriptorFactory.fromResource(R.drawable.end_point);
		start_marker = new MarkerOptions();
		end_marker = new MarkerOptions();
	}

	private void initReciver() {
		// TODO Auto-generated method stub
		receiver = new SharingReceiver();
		IntentFilter filter = new IntentFilter("com.ambigu.service.ReturnLocation");
		this.registerReceiver(receiver, filter);
	}

	private void initSharing() {
		// TODO Auto-generated method stub
		isFirst=true;
		Intent intent=getIntent();
		Bundle bundle=intent.getExtras();
		toUser=bundle.getString("toUser");
		Info info = new Info();
		info.setFromUser(ApplicationVar.getId());
		info.setToUser(toUser);
		info.setInfoType(EnumInfoType.SHARING_REQ);
		info.setReqScheme(ReqScheme.SHARE_PARTY);
		info.setfirst(true);
		// 发送广播
		Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
		Bundle broadCastBundle = new Bundle();
		broadCastBundle.putSerializable("info", info);
		broadCastBundle.putBoolean("sharingParty", true);
		broadCastIntent.putExtra("info", broadCastBundle);
		sendBroadcast(broadCastIntent);
	}

	private void initView() {
		// TODO Auto-generated method stub
		stop=(Button)findViewById(R.id.btn_center);
		speed=(TextView)findViewById(R.id.speed);
		start_point=(TextView)findViewById(R.id.start_point);
		stop_point=(TextView)findViewById(R.id.end_point);
		distance=(TextView)findViewById(R.id.distance);
		mMapView=(MapView)findViewById(R.id.sharing_map);
		mBaiduMap=mMapView.getMap();
		
		stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发起共享
				if(!isClicked){//点击
					if(!isSharing){
						stop.setText("停止共享");
						initSharing();
					}else{
						AlertDialog.Builder builder = new Builder(SharingPartyActivity.this);
						//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						//设置对话框标题
						builder.setTitle("提示信息");
						//设置对话框内的文本
						builder.setMessage("您已经在共享中，不可再次请求！");
						//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						        @Override
						        public void onClick(DialogInterface dialog, int which) {
						        	dialog.dismiss();
						        	stop.setText("发起共享");
						        	unregisterReceiver(receiver);
						        	finish();
						        }
						});
						
						//使用builder创建出对话框对象
						AlertDialog dialog = builder.create();
						//显示对话框
						dialog.show();
					}
				}else{//取消
					isSharing=false;
					stop.setText("发起共享");
					Info info1=new Info();
					info1.setFromUser(ApplicationVar.getId());
					info1.setToUser(toUser);
					info1.setend(true);
					info1.setReqScheme(ReqScheme.SHARE_PARTY);
					info1.setInfoType(EnumInfoType.SHARING_REQ);
					// 发送广播
					Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
					Bundle broadCastBundle = new Bundle();
					broadCastBundle.putSerializable("info", info1);
					broadCastBundle.putBoolean("sharingParty", true);
					broadCastIntent.putExtra("info", broadCastBundle);
					sendBroadcast(broadCastIntent);
					
					//清除程序共享状态
					Utils.clearSharingState(SharingPartyActivity.this);
		        	unregisterReceiver(receiver);
		        	finish();
				}
				isClicked=!isClicked;
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
    	unregisterReceiver(receiver);
		finish();
	}

	public class SharingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("info");
			Info info = (Info) bundle.get("info");
			speed.setText(info.getSpeed()+"");
			distance.setText(info.getDistance());
			double lat=info.getLat();
			double lng=info.getLng();
			if(info.isfirst()){
				oldLatLng=new LatLng(lat,lng);
				addStartMarker(oldLatLng);
				start_point.setText(info.getCity_now());
			}
			if(info.isend()){
				addEndMarker(oldLatLng);
				stop_point.setText(info.getCity_now());
			}
			if(!info.isfirst()){
				LatLng latLng=new LatLng(lat, lng);
				addLine(oldLatLng, latLng);
				oldLatLng=latLng;
			}
		}

	}


	private void addLine(LatLng old, LatLng neww) {
		RouteSimulate rs = new RouteSimulate(this, old, neww, false);
		rs.init(mMapView, DrivingScheme.WALKING);
		rs.doClick(DrivingScheme.WALKING);

		centerToMyLocation();
	}
	
	private void addStartMarker(LatLng start){
		start_marker.position(start).icon(start_descriptor);
		mBaiduMap.addOverlay(start_marker);
	}
	
	private void addEndMarker(LatLng end){
		start_marker.position(end).icon(start_descriptor);
		mBaiduMap.addOverlay(start_marker);
	}

	public void centerToMyLocation() {
		MapStatus.Builder builder = new MapStatus.Builder();
		builder.target(oldLatLng).zoom(18.0f);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	}
	
	@Override
	public void getSharingRes(final Info info) {
		// TODO Auto-generated method stub
		LogUtils.showLoG(SharingPartyActivity.this, "回来了1");
		if(info.getInfoType()==EnumInfoType.SHARING_RES&&info.isState()&&!info.isend()&&info.getReqScheme()==ReqScheme.BE_SHARED_PARTY){//正常情况
			//读取系统设置中的共享频率

			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
		        	stop.setText("停止共享");
				}
			});
        	
			reqthread=new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
					int times=sharedPreferences.getInt("update_times", 10);
					try {
						LogUtils.showLoG(SharingPartyActivity.this, "回来了");
						Thread.sleep(times*1000);
						Info info1=new Info();
						info1.setFromUser(ApplicationVar.getId());
						info1.setToUser(info.getFromUser());
						info1.setInfoType(EnumInfoType.SHARING_REQ);
						info1.setReqScheme(ReqScheme.SHARE_PARTY);
						// 发送广播
						Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
						Bundle broadCastBundle = new Bundle();
						broadCastBundle.putSerializable("info", info1);
						broadCastBundle.putBoolean("sharingParty", true);
						broadCastIntent.putExtra("info", broadCastBundle);
						sendBroadcast(broadCastIntent);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			reqthread.start();
		}else if(info.getReqScheme()==ReqScheme.BE_SHARED_PARTY&&info.isend()&&info.getInfoType()==EnumInfoType.SHARING_REQ){//对方请求结束共享
			//应该发送回复信息
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub

					AlertDialog.Builder builder = new Builder(SharingPartyActivity.this);
					//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					//设置对话框标题
					builder.setTitle("提示信息");
					//设置对话框内的文本
					builder.setMessage("对方已停止实时共享！");
					//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					        	dialog.dismiss();
					        	isEnd=true;
					        	stop.setText("发起共享");
								//清除程序共享状态
								Utils.clearSharingState(SharingPartyActivity.this);
					        	finish();
					        }
					});
					//使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					//显示对话框
					dialog.show();
				}
			});
		}else if(info.getReqScheme()==ReqScheme.SERVER&&info.getInfoType()==EnumInfoType.SHARING_RES){//程序异常结束共享发送最后一条信息以结束行程
			
			if(!isShow){
	        	//发送共享结束信息
				Info info1=new Info();
				info1.setFromUser(ApplicationVar.getId());
				info1.setToUser(info.getFromUser());
				info1.setDrivingstate(0);
				info1.setend(true);
				info.setReqScheme(ReqScheme.SERVER);//服务器端知道后设置结束
				info1.setInfoType(EnumInfoType.SHARING_REQ);
				// 发送广播
				Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
				Bundle broadCastBundle = new Bundle();
				broadCastBundle.putSerializable("info", info1);
				broadCastBundle.putBoolean("sharingParty", true);
				broadCastIntent.putExtra("info", broadCastBundle);
				sendBroadcast(broadCastIntent);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub

						AlertDialog.Builder builder = new Builder(SharingPartyActivity.this);
						//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						//设置对话框标题
						builder.setTitle("提示信息");
						//设置对话框内的文本
						if(info.isfirst()&&!info.isend()) builder.setMessage("发起共享失败，请检查网络状态或提示好友上线！");
						else if(!info.isfirst()&&info.isend())  builder.setMessage("共享遭遇意外情况退出！");//这时应该写入数据库
						//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						        @Override
						        public void onClick(DialogInterface dialog, int which) {
						        	dialog.dismiss();

						        	isEnd=true;
						        	isSharing=false;
						        	stop.setText("发起共享");
									//清除程序共享状态
									Utils.clearSharingState(SharingPartyActivity.this);
						        	finish();
						        }
						});
						
						//使用builder创建出对话框对象
						AlertDialog dialog = builder.create();
						//显示对话框
						dialog.show();
					}
				});
				isShow=true;
			}
		}else if(!info.isState()&&info.getInfoType()==EnumInfoType.SHARING_RES&&info.getReqScheme()==ReqScheme.BE_SHARED_PARTY){
			//对方拒绝请求
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub

					AlertDialog.Builder builder = new Builder(SharingPartyActivity.this);
					//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					//设置对话框标题
					builder.setTitle("提示信息");
					//设置对话框内的文本
					builder.setMessage("对方拒绝您的请求！");
					//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					        	dialog.dismiss();
					        	stop.setText("发起共享");
					        	isSharing=false;
								//清除程序共享状态
								Utils.clearSharingState(SharingPartyActivity.this);
					        	finish();
					        }
					});
					
					//使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					//显示对话框
					dialog.show();
				}
			});
		}else if(info.isState()&&info.isend()&&info.getReqScheme()==ReqScheme.BE_SHARED_PARTY&&info.isState()&&info.getInfoType()==EnumInfoType.SHARING_RES){
			//对方确认停止发送信息
			reqthread.interrupt();//不应该再发了，即使刚刚仍有信息在睡眠中未发出去
			isSharing=false;
        	isEnd=true;
        	finish();
		}
	}
	
}
