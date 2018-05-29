package com.ambigu.navi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.drive.SharingOptions;
import com.ambigu.listener.OnReceiveSharingMessageListener;
import com.ambigu.listener.OnSharingResListener;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.ReqScheme;
import com.ambigu.model.Settings_Type;
import com.ambigu.model.User;
import com.ambigu.rtslocation.R;
import com.ambigu.service.SharingService;
import com.ambigu.settings.SupportSettingsActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.LogUtils;
import com.ambigu.util.Utils;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRouteGuideManager.CustomizedLayerItem;
import com.baidu.navisdk.adapter.BNRouteGuideManager.OnNavigationListener;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviBaseCallbackModel;
import com.baidu.navisdk.adapter.BaiduNaviCommonModule;
import com.baidu.navisdk.adapter.NaviModuleFactory;
import com.baidu.navisdk.adapter.NaviModuleImpl;
import com.google.gson.Gson;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 * �յ�����
 * 
 * @author hgy
 *
 */
public class NaviGuideActivity extends Activity implements OnSharingResListener{

	private final String TAG = NaviGuideActivity.class.getName();
	private BNRoutePlanNode mBNRoutePlanNode = null;
	private BaiduNaviCommonModule mBaiduNaviCommonModule = null;
	private FrameLayout rl_root;
	private String users[];
	private boolean useCommonInterface = true;
	private View sharingView;
	private ImageButton sharingButton;
	private boolean isSharing=false;
	private boolean isClicked=false;
	private boolean isShow=false;
	private SharingOptions sharingOptions;
	private LocationClient mLocationClient;
	private String toUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createHandler();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		}
		View view = null;
		if (useCommonInterface) {
			// ʹ��ͨ�ýӿ�
			mBaiduNaviCommonModule = NaviModuleFactory.getNaviModuleManager().getNaviCommonModule(
					NaviModuleImpl.BNaviCommonModuleConstants.ROUTE_GUIDE_MODULE, this,
					BNaviBaseCallbackModel.BNaviBaseCallbackConstants.CALLBACK_ROUTEGUIDE_TYPE, mOnNavigationListener);
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onCreate();
				view = mBaiduNaviCommonModule.getView();
			}

		} else {
			// ʹ�ô�ͳ�ӿ�
			view = BNRouteGuideManager.getInstance().onCreate(this, mOnNavigationListener);
		}

		if (view != null) {
			// 添加一个TextView
			rl_root = new FrameLayout(this);
			sharingView = getLayoutInflater().inflate(R.layout.navi_help, null);
			sharingView.setAlpha(1.0f);
			rl_root.addView(view);
			rl_root.addView(sharingView);
			setContentView(rl_root);
			
			initData();
			sharingButton=(ImageButton) sharingView.findViewById(R.id.sharing_bt);
			sharingButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(!isClicked){
						sharingButton.setBackgroundResource(R.drawable.sharing_pressed);
						if(!isSharing){

							AlertDialog dialog = new AlertDialog.Builder(NaviGuideActivity.this).setIcon(R.drawable.qq_icon)// 设置标题的图片
									.setTitle("请选择好友")// 设置对话框的标题
									.setItems(users, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											isSharing=true;//正在共享
											Info info=new Info();
											info.setfirst(true);
											info.setFromUser(ApplicationVar.getId());
											info.setToUser(users[which]);
											toUser=users[which];
											info.setReqScheme(ReqScheme.SHARE_PARTY);
											info.setInfoType(EnumInfoType.SHARING_REQ);
											// 发送广播
											Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
											Bundle broadCastBundle = new Bundle();
											broadCastBundle.putSerializable("info", info);
											broadCastIntent.putExtra("info", broadCastBundle);
											sendBroadcast(broadCastIntent);
										}
									}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
											sharingButton.setBackgroundResource(R.drawable.sharing);
											isClicked=false;
											
										}
									}).create();
							dialog.show();
						}else{
							AlertDialog.Builder builder = new Builder(NaviGuideActivity.this);
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
										sharingButton.setBackgroundResource(R.drawable.sharing);
							        }
							});
							
							//使用builder创建出对话框对象
							AlertDialog dialog = builder.create();
							//显示对话框
							dialog.show();
						}
					}else{
						isSharing=false;
						sharingButton.setBackgroundResource(R.drawable.sharing);
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
						broadCastIntent.putExtra("info", broadCastBundle);
						sendBroadcast(broadCastIntent);
						
						//清除程序共享状态
						Utils.clearSharingState(NaviGuideActivity.this);
					}
					isClicked=!isClicked;
				}
			});
		}

		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				// mBNRoutePlanNode = (BNRoutePlanNode)
				// bundle.getSerializable(WalkWithMeActivity.ROUTE_PLAN_NODE);
			}
		}
		// ��ʾ�Զ���ͼ��
		if (hd != null) {
			hd.sendEmptyMessageAtTime(MSG_SHOW, 5000);
		}

		NaviEventHandler.getInstance().getDialog(this);
		// BNEventHandler.getInstance().showDialog();
	}

	private void initData() {
		// TODO Auto-generated method stub

		//获得数据
		users=initFriendData();
		DiscardClientHandler.getInstance().setOnSharingResListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onResume();
			}
		} else {
			BNRouteGuideManager.getInstance().onResume();
		}

	}

	protected void onPause() {
		super.onPause();

		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onPause();
			}
		} else {
			BNRouteGuideManager.getInstance().onPause();
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onDestroy();
			}
		} else {
			BNRouteGuideManager.getInstance().onDestroy();
		}
		NaviEventHandler.getInstance().disposeDialog();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onStop();
			}
		} else {
			BNRouteGuideManager.getInstance().onStop();
		}

	}

	/*
	 * / (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 * �˴�onBackPressed����false��ʾǿ���˳���true��ʾ������һ������ǿ���˳�
	 */
	@Override
	public void onBackPressed() {
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onBackPressed(true);
			}
		} else {
			BNRouteGuideManager.getInstance().onBackPressed(false);
		}
	}

	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onConfigurationChanged(newConfig);
			}
		} else {
			BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
		}

	};

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				Bundle mBundle = new Bundle();
				mBundle.putInt(RouteGuideModuleConstants.KEY_TYPE_KEYCODE, keyCode);
				mBundle.putParcelable(RouteGuideModuleConstants.KEY_TYPE_EVENT, event);
				mBaiduNaviCommonModule.setModuleParams(RouteGuideModuleConstants.METHOD_TYPE_ON_KEY_DOWN, mBundle);
				try {
					Boolean ret = (Boolean) mBundle.get(RET_COMMON_MODULE);
					if (ret) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// TODO Auto-generated method stub
		if (useCommonInterface) {
			if (mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onStart();
			}
		} else {
			BNRouteGuideManager.getInstance().onStart();
		}
	}

	@SuppressWarnings("deprecation")
	private void addCustomizedLayerItems() {
		List<CustomizedLayerItem> items = new ArrayList<CustomizedLayerItem>();
		CustomizedLayerItem item1 = null;
		if (mBNRoutePlanNode != null) {
			item1 = new CustomizedLayerItem(mBNRoutePlanNode.getLongitude(), mBNRoutePlanNode.getLatitude(),
					mBNRoutePlanNode.getCoordinateType(), getResources().getDrawable(R.drawable.ic_launcher),
					CustomizedLayerItem.ALIGN_CENTER);
			items.add(item1);

			BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
		}
		BNRouteGuideManager.getInstance().showCustomizedLayer(true);
	}

	private static final int MSG_SHOW = 1;
	private static final int MSG_HIDE = 2;
	private static final int MSG_RESET_NODE = 3;
	private Handler hd = null;

	private void createHandler() {
		if (hd == null) {
			hd = new Handler(getMainLooper()) {
				public void handleMessage(android.os.Message msg) {
					if (msg.what == MSG_SHOW) {
						addCustomizedLayerItems();
					} else if (msg.what == MSG_HIDE) {
						BNRouteGuideManager.getInstance().showCustomizedLayer(false);
					} else if (msg.what == MSG_RESET_NODE) {
						BNRouteGuideManager.getInstance().resetEndNodeInNavi(
								new BNRoutePlanNode(116.21142, 40.85087, "�ٶȴ���11", null, CoordinateType.GCJ02));
					}
				};
			};
		}
	}

	private OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

		@Override
		public void onNaviGuideEnd() {
			// �˳�����
			finish();
		}

		@Override
		public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {

			if (actionType == 0) {
				// ��������Ŀ�ĵ� �Զ��˳�
				Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",��������Ŀ�ĵأ�");
			}

			Log.i(TAG, "actionType:" + actionType + "arg1:" + arg1 + "arg2:" + arg2 + "obj:" + obj.toString());
		}

	};
	private Thread reqthread;

	private final static String RET_COMMON_MODULE = "module.ret";

	private interface RouteGuideModuleConstants {
		final static int METHOD_TYPE_ON_KEY_DOWN = 0x01;
		final static String KEY_TYPE_KEYCODE = "keyCode";
		final static String KEY_TYPE_EVENT = "event";
	}
	
	private  String[] initFriendData() {
		// TODO Auto-generated method stub
		String []users=null;
		ArrayList<String> userList=new ArrayList<String>();
		//得到用户好友信息
		Gson gson=new Gson();
		SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
		String infoString=sharedPreferences.getString("info", "{}");
		Info info=gson.fromJson(infoString, Info.class);
		HashMap<String, Group> groups=info.getFriendsList();
		Iterator<Map.Entry<String,Group>> iterator=groups.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String,Group> entry=iterator.next();
			Group group=entry.getValue();
			ArrayList<User> usr=group.getItems();
			for(User u:usr){
				userList.add(u.getUserid());
			}
		}
		users=(String[]) userList.toArray(new String[userList.size()]);
		return users;
	}

	@Override
	public void getSharingRes(final Info info) {
		// TODO Auto-generated method stub
		LogUtils.showLoG(NaviGuideActivity.this, "回来了1");
		if(info.getInfoType()==EnumInfoType.SHARING_RES&&info.isState()&&!info.isend()&&info.getReqScheme()==ReqScheme.BE_SHARED_PARTY){//正常情况
			//读取系统设置中的共享频率
			LogUtils.showLoG(NaviGuideActivity.this, "回来了");
			reqthread=new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
					int times=sharedPreferences.getInt("update_times", 10);
					try {
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

					AlertDialog.Builder builder = new Builder(NaviGuideActivity.this);
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
								sharingButton.setBackgroundResource(R.drawable.sharing);
								//清除程序共享状态
								Utils.clearSharingState(NaviGuideActivity.this);
					        }
					});
					//使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					//显示对话框
					dialog.show();
				}
			});
		}else if(info.getReqScheme()==ReqScheme.SERVER&&info.getInfoType()==EnumInfoType.SHARING_RES){//程序异常结束共享发送最后一条信息以结束行程
			isSharing=false;
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
				broadCastIntent.putExtra("info", broadCastBundle);
				sendBroadcast(broadCastIntent);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub

						AlertDialog.Builder builder = new Builder(NaviGuideActivity.this);
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
									sharingButton.setBackgroundResource(R.drawable.sharing);
									isClicked=false;
									isShow=false;
									isSharing=false;
									
									//清除程序共享状态
									Utils.clearSharingState(NaviGuideActivity.this);
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

					AlertDialog.Builder builder = new Builder(NaviGuideActivity.this);
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
								sharingButton.setBackgroundResource(R.drawable.sharing);
								isClicked=false;
								isShow=false;
								isSharing=false;
								

								//清除程序共享状态
								Utils.clearSharingState(NaviGuideActivity.this);
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
		}
	}
}
