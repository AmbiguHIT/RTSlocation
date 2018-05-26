package com.ambigu.rtslocation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.ambigu.adapter.MyPagerAdapter;
import com.ambigu.adapter.MyPagerAdapter.OnGetNaviDataListener;
import com.ambigu.client.RTSClient;
import com.ambigu.drive.DriveActivity;
import com.ambigu.listener.OnAddFriendListener;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.User;
import com.ambigu.navi.NaviEventHandler;
import com.ambigu.navi.NaviGuideActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.FaceConversionUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.google.gson.Gson;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnGetNaviDataListener {
	// private MapView mMapView = null;

	private static final String APP_FOLDER_NAME = "RTSlocation";
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

	private final static String authBaseArr[] = { Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE};
	private final static String authComArr[] = { Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE};
	private final static int authBaseRequestCode = 1;
	private final static int authComRequestCode = 2;

	private boolean hasInitSuccess = false;
	private boolean hasRequestComAuth = false;
	private List<View> views = new ArrayList<View>();
	private ViewPager viewPager;
	private LinearLayout sharing, message, contacts, settings;
	private ImageView ivsharing, ivmessage, ivcontacts, ivsettings, ivCurrent;
	private static Handler mhandler;
	private OnAddFriendListener onAddFriendListener;
	private MyPagerAdapter adapter;
	private Info adapterinfo;
	private LatLng st, ed;

	private String mSDCardPath = null;
	private ProgressDialog pDialog;
	private volatile static Semaphore semaphore = new Semaphore(1);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();

		initview();
		initData();

		if (initDir()) {
			initNavi();
		}
	}

	@Override
	public void onClick(View v) {
		changeTab(v.getId());
	}

	private void initview() {
		// TODO Auto-generated method stub

		viewPager = (ViewPager) findViewById(R.id.viewPager);

		sharing = (LinearLayout) findViewById(R.id.sharing);
		message = (LinearLayout) findViewById(R.id.message);
		contacts = (LinearLayout) findViewById(R.id.contacts);
		settings = (LinearLayout) findViewById(R.id.settings);

		sharing.setOnClickListener(this);
		message.setOnClickListener(this);
		contacts.setOnClickListener(this);
		settings.setOnClickListener(this);

		ivsharing = (ImageView) findViewById(R.id.ivsharing);
		ivmessage = (ImageView) findViewById(R.id.ivmessage);
		ivcontacts = (ImageView) findViewById(R.id.ivcontacts);
		ivsettings = (ImageView) findViewById(R.id.ivsettings);

		ivsharing.setSelected(true);
		ivCurrent = ivsharing;

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				changeTab(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private void changeTab(int id) {
		ivCurrent.setSelected(false);
		switch (id) {
		case R.id.sharing:
			viewPager.setCurrentItem(0);
		case 0:
			ivsharing.setSelected(true);
			ivCurrent = ivsharing;
			break;
		case R.id.message:
			viewPager.setCurrentItem(1);
		case 1:
			ivmessage.setSelected(true);
			ivCurrent = ivmessage;
			break;
		case R.id.contacts:
			viewPager.setCurrentItem(2);
		case 2:
			ivcontacts.setSelected(true);
			ivCurrent = ivcontacts;
			break;
		case R.id.settings:
			viewPager.setCurrentItem(3);
		case 3:
			ivsettings.setSelected(true);
			ivCurrent = ivsettings;
			break;
		default:
			break;
		}
	}

	private void initData() {
		LayoutInflater mInflater = LayoutInflater.from(this);
		View tab_sharing = mInflater.inflate(R.layout.tab_sharing, null);
		View tab_message = mInflater.inflate(R.layout.tab_message, null);
		View tab_contacts = mInflater.inflate(R.layout.tab_contacts, null);
		View tab_setting = mInflater.inflate(R.layout.tab_setting, null);
		tab_message.setTag(1);
		tab_contacts.setTag(2);
		tab_sharing.setTag(0);
		tab_setting.setTag(3);
		views.add(tab_sharing);
		views.add(tab_message);
		views.add(tab_contacts);
		views.add(tab_setting);

		Info info = new Info();
		info.setFromUser(ApplicationVar.getId());
		info.setInfoType(EnumInfoType.GET_FRIEND_AND_MSG);
		RTSClient.writeAndFlush(info);

		try {
			semaphore.acquire();
			mhandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					adapterinfo = (Info) msg.obj;
					SharedPreferences sharedPreferences = getSharedPreferences("rtslocation", Context.MODE_PRIVATE);
					Editor editor = sharedPreferences.edit();
					editor.putString("info", (new Gson()).toJson(adapterinfo));
					editor.commit();
					adapter = new MyPagerAdapter(views, MainActivity.this, viewPager, adapterinfo);
					viewPager.setAdapter(adapter);
				}
			};
			semaphore.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// MyPagerAdapter adapter = new MyPagerAdapter(views, MainActivity.this,
		// viewPager);
		// viewPager.setAdapter(adapter);
	}

	public static void initAdapter(Message msg) {
		if (mhandler == null) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mhandler.sendMessage(msg);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		// mMapView.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add_friend) {
			Intent intent = new Intent(this, AddFriendActivity.class);
			startActivityForResult(intent, 1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && data != null) {
			Bundle bundle = data.getExtras();
			Info info = (Info) bundle.get("info");
			Log.e("add", info.getFromUser());
			Toast.makeText(this, "hello", Toast.LENGTH_LONG).show();
			HashMap<String, Group> friends = adapterinfo.getFriendsList();
			User user = new User();
			user.setUserid(info.getToUser());
			user.setFriend(info.getFromUser());
			user.setIcon(info.getIcon());
			Group group = friends.get(info.getGroup());
			if (group == null) {
				group = new Group();
				group.setGroupname(info.getGroup());
				ArrayList<User> items = new ArrayList<User>();
				items.add(user);
				group.setItems(items);
				friends.put(info.getGroup(), group);
			} else {
				group.getItems().add(user);
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void dorouteplanToNavi(LatLng st, LatLng ed) {
		// TODO Auto-generated method stub
		this.st = st;
		this.ed = ed;
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("正在处理中！");
		pDialog.setTitle("导航提示");
		Window window = pDialog.getWindow();
		WindowManager.LayoutParams lParams = window.getAttributes();
		lParams.alpha = 0.7f;
		lParams.dimAmount = 0.4f;
		window.setAttributes(lParams);
		pDialog.show();
		routeplanToNavi(CoordinateType.BD09LL, st, ed);
	}

	private boolean initDir() {// 创建一个文件夹用于保存在路线导航过程中语音导航语音文件的缓存，防止用户再次开启同样的导航直接从缓存中读取即可
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			mSDCardPath = Environment.getExternalStorageDirectory().toString();
		} else {
			mSDCardPath = null;
		}
		if (mSDCardPath == null) {
			return false;
		}
		File file = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!file.exists()) {
			try {
				file.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		Log.e("mSDCardPath", mSDCardPath);
		return true;
	}

	String authinfo = null;

	/**
	 * 内部TTS播报状态回传handler
	 */
	private Handler ttsHandler = new Handler() {
		public void handleMessage(Message msg) {
			int type = msg.what;
			switch (type) {
			case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
				// showToastMsg("Handler : TTS play start");
				break;
			}
			case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
				// showToastMsg("Handler : TTS play end");
				break;
			}
			default:
				break;
			}
		}
	};

	/**
	 * 内部TTS播报状态回调接口
	 */
	private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

		@Override
		public void playEnd() {
			// showToastMsg("TTSPlayStateListener : TTS play end");
		}

		@Override
		public void playStart() {
			// showToastMsg("TTSPlayStateListener : TTS play start");
		}
	};

	public void showToastMsg(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean hasBasePhoneAuth() {
		// TODO Auto-generated method stub

		PackageManager pm = this.getPackageManager();
		for (String auth : authBaseArr) {
			if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCompletePhoneAuth() {
		// TODO Auto-generated method stub

		PackageManager pm = this.getPackageManager();
		for (String auth : authComArr) {
			if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(23)
	private void initNavi() {
		// 申请权限
		
		
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			if (!hasBasePhoneAuth()) {

				this.requestPermissions(authBaseArr, authBaseRequestCode);
				return;

			}
		}
		BaiduNaviManager.getInstance().init(MainActivity.this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				MainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(MainActivity.this, authinfo, Toast.LENGTH_LONG).show();
					}
				});
			}

			public void initSuccess() {
				initSetting();
				hasInitSuccess = true;
				Toast.makeText(MainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
			}

			public void initStart() {
				Toast.makeText(MainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
			}

			public void initFailed() {
				Toast.makeText(MainActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}
		}, mTTSCallback);
	}

	private CoordinateType mCoordinateType = null;

	@SuppressLint("NewApi")
	private void routeplanToNavi(CoordinateType coType, LatLng st, LatLng ed) {
		mCoordinateType = coType;
		if (!hasInitSuccess) {
			Toast.makeText(MainActivity.this, "还未初始化!", Toast.LENGTH_SHORT).show();
		}
		// 权限申请
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			// 保证导航功能完备
			if (!hasCompletePhoneAuth()) {
				if (!hasRequestComAuth) {
					hasRequestComAuth = true;
					this.requestPermissions(authComArr, authComRequestCode);
					return;
				} else {
					Toast.makeText(MainActivity.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
				}
			}

		}
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
		sNode = new BNRoutePlanNode(st.longitude, st.latitude, "北京", null, coType);
		eNode = new BNRoutePlanNode(ed.longitude, ed.latitude, "北京", null, coType);
		if (sNode != null && eNode != null) {
			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
			list.add(sNode);
			list.add(eNode);

			// 开发者可以使用旧的算路接口，也可以使用新的算路接口,可以接收诱导信息等
			// BaiduNaviManager.getInstance().launchNavigator(this, list, 1,
			// true, new DemoRoutePlanListener(sNode));
			BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new MyRoutePlanListener(sNode),
					eventListerner);
		} else {

		}
	}

	BaiduNaviManager.NavEventListener eventListerner = new BaiduNaviManager.NavEventListener() {

		@Override
		public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
			NaviEventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
		}
	};

	public class MyRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public MyRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */
			Log.e("即将导航","hahha");
			if(pDialog!=null&&pDialog.isShowing()) pDialog.dismiss();
			Intent intent = new Intent(MainActivity.this, NaviGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);

		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
		}
	}

	private void initSetting() {
		// BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
		BNaviSettingManager
				.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
		BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
		// BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
		BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
		BNaviSettingManager.setIsAutoQuitWhenArrived(true);
		Bundle bundle = new Bundle();
		// 必须设置APPID，否则会静音
		bundle.putString(BNCommonSettingParam.TTS_APP_ID, "11265902");
		BNaviSettingManager.setNaviSdkParam(bundle);
		
	}

	private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

		@Override
		public void stopTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "stopTTS");
		}

		@Override
		public void resumeTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "resumeTTS");
		}

		@Override
		public void releaseTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "releaseTTSPlayer");
		}

		@Override
		public int playTTSText(String speech, int bPreempt) {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

			return 1;
		}

		@Override
		public void phoneHangUp() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneHangUp");
		}

		@Override
		public void phoneCalling() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneCalling");
		}

		@Override
		public void pauseTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "pauseTTS");
		}

		@Override
		public void initTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "initTTSPlayer");
		}

		@Override
		public int getTTSState() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "getTTSState");
			return 1;
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (requestCode == authBaseRequestCode) {
			for (int ret : grantResults) {
				
				if (ret == PackageManager.PERMISSION_GRANTED) {

					Toast.makeText(this, "申请权限chengg", Toast.LENGTH_LONG).show();
					continue;
				} else {
					Toast.makeText(MainActivity.this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			initNavi();
		} else if (requestCode == authComRequestCode) {
			for (int ret : grantResults) {
				if (ret == 0) {
					continue;
				}
			}
			routeplanToNavi(mCoordinateType, st, ed);
		}

	}

}
