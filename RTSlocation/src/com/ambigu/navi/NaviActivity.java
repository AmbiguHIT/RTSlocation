package com.ambigu.navi;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class NaviActivity extends Activity {

	public static List<Activity> activityList = new LinkedList<Activity>();

	private static final String APP_FOLDER_NAME = "MicroTrip";

	private String mSDCardPath = null;

	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

	private final static String authBaseArr[] = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.ACCESS_FINE_LOCATION };
	private final static String authComArr[] = { Manifest.permission.READ_PHONE_STATE };
	private final static int authBaseRequestCode = 1;
	private final static int authComRequestCode = 2;

	private boolean hasInitSuccess = false;
	private boolean hasRequestComAuth = false;

	// 导航起点和终点信息
	double stlat = 0.0;
	double stlng = 0.0;
	String stname = "";
	double enlat = 0.0;
	double enlng = 0.0;
	String enname = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activityList.add(this);

		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				Log.i("crug", name);
				delayTest();
			}
		}, 500);
		BNOuterLogUtil.setLogSwitcher(true);

		initListener();
		if (initDirs()) {
			initNavi();
		}

		// BNOuterLogUtil.setLogSwitcher(true);

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		stlat = bundle.getDouble("stNode.lng");
		stlng = bundle.getDouble("stNode.lat");
		stname = bundle.getString("stNode.name");
		enlat = bundle.getDouble("enNode.lat");
		enlng = bundle.getDouble("enNode.lng");
		enname = bundle.getString("enNode.name");
		// Toast.makeText(NaviActivity.this, Double.toString(stlat),
		// Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void delayTest() {
		// SDKInitializer.initialize(BNDemoMainActivity.this.getApplication());
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// Looper.prepare();
		// SDKInitializer.initialize(BNDemoMainActivity.this.getApplication());
		// }
		// }).start();
	}

	private void initListener() {
		routeplanToNavi(CoordinateType.BD09LL);
	}

	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
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
		NaviActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(NaviActivity.this, msg, Toast.LENGTH_SHORT).show();
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
		BaiduNaviManager.getInstance().init(NaviActivity.this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				NaviActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(NaviActivity.this, authinfo, Toast.LENGTH_LONG).show();
					}
				});
			}

			public void initSuccess() {
				initSetting();
				Toast.makeText(NaviActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
			}

			public void initStart() {
				Toast.makeText(NaviActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
			}

			public void initFailed() {
				Toast.makeText(NaviActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}
		}, mTTSCallback);
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private CoordinateType mCoordinateType = null;

	@SuppressLint("NewApi")
	private void routeplanToNavi(CoordinateType coType) {
		mCoordinateType = coType;
		if (!hasInitSuccess) {
			Toast.makeText(NaviActivity.this, "还未初始化!", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(NaviActivity.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
				}
			}

		}
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
		switch (coType) {

		case BD09LL: {
			sNode = new BNRoutePlanNode(stlng, stlat, stname, null, coType);
			eNode = new BNRoutePlanNode(enlng, enlat, enname, null, coType);
			break;
		}
		default:
			break;
		}
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

			for (Activity ac : activityList) {

				if (ac.getClass().getName().endsWith("NaviGuideActivity")) {

					return;
				}
			}
			Intent intent = new Intent(NaviActivity.this, NaviGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);

		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(NaviActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
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
		bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9504095");
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
				if (ret == 0) {
					continue;
				} else {
					Toast.makeText(NaviActivity.this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
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
			routeplanToNavi(mCoordinateType);
		}

	}
}
