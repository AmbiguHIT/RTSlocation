
package com.ambigu.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.drive.SharingOptions;
import com.ambigu.listener.MyOrientationListener;
import com.ambigu.listener.OnSharingMessageListener;
import com.ambigu.listener.MyOrientationListener.OnOrientationListener;
import com.ambigu.listener.OnGetSelfLocationListener;
import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.MessageOfPerson;
import com.ambigu.model.Settings_Type;
import com.ambigu.model.User;
import com.ambigu.route.RouteSimulate;
import com.ambigu.rtslocation.AcquireAuthLatlngActivity;
import com.ambigu.rtslocation.LoginActivity;
import com.ambigu.rtslocation.MessageActivity;
import com.ambigu.rtslocation.R;
import com.ambigu.rtslocation.SharingActivity;
import com.ambigu.rtslocation.SharingActivity.MapReceiver;
import com.ambigu.settings.SettingsActivity;
import com.ambigu.settings.SharingHistoryActivity;
import com.ambigu.settings.SupportSettingsActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.RTSMessage;
import com.ambigu.view.CircleImageView;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.ambigu.view.SwipeListView;
import com.baidu.android.bbalbs.common.a.a;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * @author
 * @version
 */
public class MyPagerAdapter extends PagerAdapter implements OnSharingMessageListener, OnGetGeoCoderResultListener,OnGetSelfLocationListener {

	private List<View> views = new ArrayList<View>();
	private Activity a;
	private HashMap<String, RTSMessage> data = new HashMap<String, RTSMessage>();
	private SwipeListView mListView;
	private PinnedHeaderExpandableListView explistview;
	private ArrayList<ArrayList<String>> childrenData = null;
	private ArrayList<ArrayList<String>> childrenImg = null;
	private ArrayList<String> groupData = null;
	private int expandFlag = -1;// 控制列表的展开
	private PinnedHeaderExpandableAdapter adapter;
	private volatile static Semaphore msgSemaphore = new Semaphore(1);
	private static Handler msgHandler = null;
	private Info info;
	private SwipeAdapter mAdapter;
	private ImageButton sharing_ib;
	private LocationClient mLocationClient = null;
	private LocationClientOption option;
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX = 0.0f;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private boolean isFirstLoc = true;
	private MyLocationData locData;
	private MapReceiver receiver;
	private EditText search_et;
	private GeoCoder mSearch;
	private LatLng mylocation;
	protected Builder builder;
	protected AlertDialog dialog;
	private ImageButton following;
	private BitmapDescriptor start_descriptor;
	private BitmapDescriptor end_descriptor;
	private MarkerOptions start_marker;
	private MarkerOptions end_marker;
	private static LatLng endLatLng;
	private static LatLng startLatLng;
	private volatile static boolean isFollowing=false;
	private OnGetNaviDataListener onGetNaviDataListener;

	public MyPagerAdapter(List<View> views, Activity a, ViewPager viewPager, Info info) {
		super();
		this.views = views;
		this.a = a;
		this.info = info;
		this.onGetNaviDataListener=(OnGetNaviDataListener)a;
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(views.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = views.get(position);
		container.addView(view);
		if ((view.getTag() + "").equals(1 + "")) {
			initData(view, info);
		}
		if ((view.getTag() + "").equals(0 + "")) {
			initView0(view);
		}
		if ((view.getTag() + "").equals(2 + "")) {
			initData1(view, info);
		}
		if ((view.getTag() + "").equals(3 + "")) {
			initListener(view, info);
			DiscardClientHandler.getInstance().setOnSharingMessageListener(this);
		}
		return view;
	}

	private void initView0(View view) {
		// TODO Auto-generated method stub
		sharing_ib = (ImageButton) view.findViewById(R.id.sharing_ib);
		if (mMapView == null)
			mMapView = (MapView) view.findViewById(R.id.sharingmap);
		else
			mMapView.onResume();
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		sharing_ib.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int[] location = new int[2];
					sharing_ib.getLocationOnScreen(location);
					int x = location[0];
					int y = location[1];
					Toast toast = Toast.makeText(a, "实时共享", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP | Gravity.LEFT, x, y + 10);
					toast.getView().getBackground().setAlpha(100);// 设置透明度
					toast.show();
				}
				return true;
			}
		});

		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		sharing_ib.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//发起共享
				
			}
		});

		// 输入框
		search_et = (EditText) view.findViewById(R.id.search_et);
		Drawable leftDrawable = search_et.getCompoundDrawables()[0];
		if (leftDrawable != null) {
			leftDrawable.setBounds(0, 0, 40, 40);
			search_et.setCompoundDrawables(leftDrawable, null, null, null);
		}

		search_et.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND
						|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					String adr = search_et.getText().toString();
					// 发起搜索
					Log.e("adr", adr);
					mSearch.geocode(new GeoCodeOption().city("北京").address(adr));
					return true;
				}
				return false;
			}
		});
		
		//
		ImageButton center=(ImageButton)view.findViewById(R.id.mylocation);
		center.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				centerToMyLocation();
			}
		});
		
		following=(ImageButton)view.findViewById(R.id.follwing);
		following.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int[] location = new int[2];
					following.getLocationOnScreen(location);
					int x = location[0];
					int y = location[1];
					Toast toast = Toast.makeText(a, "实时跟随", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP | Gravity.LEFT, x, y + 10);
					toast.getView().getBackground().setAlpha(100);// 设置透明度
					toast.show();
				}
				return false;
			}
		});
		
		following.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onGetNaviDataListener.dorouteplanToNavi(startLatLng, endLatLng);
			}
		});

		// 定位到当前位置
		// 初始化图标
		initReceiver();
		

		start_descriptor = BitmapDescriptorFactory.fromResource(R.drawable.start_point);
		end_descriptor = BitmapDescriptorFactory.fromResource(R.drawable.end_point);
		start_marker = new MarkerOptions();
		end_marker = new MarkerOptions();
	}
	

	private void initReceiver() {
		// TODO Auto-generated method stub
		receiver = new MapReceiver();
		IntentFilter filter = new IntentFilter("com.ambigu.service.SharingLocation");
		a.registerReceiver(receiver, filter);
	}

	private void initListener(View view, Info info) {
		// TODO Auto-generated method stub
		CircleImageView civ = (CircleImageView) view.findViewById(R.id.touxiang);

		// 设置头像
		Drawable bitmap = BitmapDrawable.createFromPath(a.getCacheDir().getAbsolutePath() + "//"
				+ ApplicationVar.getId() + "//Icon" + "//" + ApplicationVar.getId() + ".png");
		civ.setBackground(bitmap);
		civ.setBackground(bitmap);

		LinearLayout auth_history = (LinearLayout) view.findViewById(R.id.auth_history);
		LinearLayout driving_history = (LinearLayout) view.findViewById(R.id.driving_history);
		LinearLayout myinfo = (LinearLayout) view.findViewById(R.id.myinfo);
		LinearLayout message = (LinearLayout) view.findViewById(R.id.message);
		LinearLayout settings = (LinearLayout) view.findViewById(R.id.settings);
		LinearLayout loginout = (LinearLayout) view.findViewById(R.id.loginout);

		// 共享历史
		message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送请求共享历史信息
				Info info = new Info();
				info.setFromUser(ApplicationVar.getId());
				info.setInfoType(EnumInfoType.GET_SHARING_MES);
				RTSClient.writeAndFlush(info);
			}
		});

		myinfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(a, SettingsActivity.class);
				a.startActivity(intent);
			}
		});

		loginout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new Builder(a);
				// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
				builder.setIcon(R.drawable.ic_launcher);
				// 设置对话框标题
				builder.setTitle("提示信息");
				// 设置对话框内的文本
				builder.setMessage("您确定要退出吗？");
				// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 执行点击确定按钮的业务逻辑
						// 清除sharepreference
						SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
						Editor editor = sharedPreferences.edit();
						editor.clear();
						editor.putBoolean("isLogin", false);
						editor.commit();
						// 返回到登录界面
						Intent intent = new Intent(a, LoginActivity.class);
						a.startActivity(intent);
					}
				});
				// 使用builder创建出对话框对象
				AlertDialog dialog = builder.create();
				// 显示对话框
				dialog.show();

			}
		});

		settings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 辅助设置
				Intent intent = new Intent(a, SupportSettingsActivity.class);
				a.startActivity(intent);
			}
		});
		
		auth_history.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送请求共享历史信息
				Info info = new Info();
				info.setFromUser(ApplicationVar.getId());
				info.setInfoType(EnumInfoType.GET_SELF_AUTH_LATLNG);
				RTSClient.writeAndFlush(info);
			}
		});

	}

	private void initView1(View view) {
		// TODO Auto-generated method stub
		explistview = (PinnedHeaderExpandableListView) view.findViewById(R.id.explistview);

		// 设置悬浮头部VIEW
		explistview.setHeaderView(a.getLayoutInflater().inflate(R.layout.group_head, explistview, false));
		adapter = new PinnedHeaderExpandableAdapter(childrenData, groupData, childrenImg, a.getApplicationContext(),
				explistview);
		explistview.setAdapter(adapter);

		explistview.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
					long id) {
				// TODO Auto-generated method stub
				// v.setBackgroundColor(color.darker_gray);
				Toast.makeText(a, childPosition + "", Toast.LENGTH_LONG).show();
				return true;
			}
		});

		explistview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
				// TODO Auto-generated method stub
				final String items[] = { "打开", "删除", "发起共享" };
				AlertDialog dialog = new AlertDialog.Builder(a).setIcon(R.drawable.qq_icon)// 设置标题的图片
						.setTitle("操作")// 设置对话框的标题
						.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									Bundle bundle = new Bundle();
									bundle.putString("name", view.getTag() + "");
									bundle.putSerializable("info", info);
									// bundle.putSerializable("viewpager",
									// (Serializable) viewPager);
									Intent intent = new Intent(a, MessageActivity.class);
									intent.putExtras(bundle);
									a.startActivity(intent);
									break;
								case 1:
									view.setVisibility(View.GONE);

									break;
								case 2:
									String title = (String) view.getTag(R.id.tag_titile);
									Intent intent2 = new Intent(a, AcquireAuthLatlngActivity.class);
									bundle = new Bundle();
									bundle.putString("username", title);
									intent2.putExtras(bundle);
									a.startActivity(intent2);
									break;
								case 3:

									break;

								default:
									break;
								}
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create();
				dialog.show();
				return true;
			}

		});

	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
	}

	private void initData1(final View view, Info info) {
		// TODO Auto-generated method stub
		HashMap<String, Group> friends = info.getFriendsList();
		// 取messages的每一个对象的第一条记录展示
		Log.e("记录", friends.size() + "");
		Iterator<Map.Entry<String, Group>> iter = friends.entrySet().iterator();
		groupData = new ArrayList<String>();
		childrenImg = new ArrayList<ArrayList<String>>();
		childrenData = new ArrayList<ArrayList<String>>();
		while (iter.hasNext()) {
			Map.Entry<String, Group> entry = (Map.Entry<String, Group>) iter.next();
			Group group = (Group) entry.getValue();
			ArrayList<User> users2 = group.getItems();
			groupData.add(group.getGroupname());
			ArrayList<String> childlist = new ArrayList<String>();
			ArrayList<String> imglist = new ArrayList<String>();
			for (int j = 0; j < users2.size(); j++) {
				childlist.add(users2.get(j).getUserid());
				imglist.add(users2.get(j).getIcon());
			}
			childrenData.add(childlist);
			childrenImg.add(imglist);
		}
		initView1(view);

		// 设置单个分组展开
		// explistview.setOnGroupClickListener(new GroupClickListener());
	}

	public static void sendMsgToMesHander(Message msg) {
		if (msgHandler == null) {
			try {
				msgSemaphore.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		msgHandler.sendMessage(msg);
	}

	private void initData(final View view, Info info) {

		// 从数据库读取信息
		ArrayList<MessageOfPerson> messages = info.getFriendTalkContent();
		// 取messages的每一个对象的第一条记录展示
		for (int i = 0; i < messages.size(); i++) {
			RTSMessage _msg = null;
			MessageOfPerson mop = messages.get(i);
			Log.w("titile", mop.getToUser());
			_msg = new RTSMessage(mop.getToUser(),
					mop.getMessageContents().get(mop.getMessageContents().size() - 1).getContent(),
					mop.getMessageContents().get(mop.getMessageContents().size() - 1).getTime());
			_msg.setIcon(mop.getToUserIcon());
			data.put(mop.getToUser(), _msg);
		}
		initView(view);

	}

	/**
	 * 初始化界面
	 */
	private void initView(View view) {
		mListView = (SwipeListView) view.findViewById(R.id.listview);
		mAdapter = new SwipeAdapter(a.getApplicationContext(), data, mListView.getRightViewWidth());

		mListView.setAdapter(mAdapter);

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, final View view, final int arg2, long arg3) {
				// TODO Auto-generated method stub
				final String items[] = { "打开", "删除", "查看其位置", "发起共享" };
				AlertDialog dialog = new AlertDialog.Builder(a).setIcon(R.drawable.qq_icon)// 设置标题的图片
						.setTitle("操作")// 设置对话框的标题
						.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									Bundle bundle = new Bundle();
									Log.e("arg2", arg2 + "");
									bundle.putString("name", (String) view.getTag(R.id.tag_titile));
									bundle.putSerializable("info", info);
									// bundle.putSerializable("viewpager",
									// (Serializable) viewPager);
									Intent intent = new Intent(a, MessageActivity.class);
									intent.putExtras(bundle);
									a.startActivity(intent);
									break;
								case 1:
									String title = (String) view.getTag(R.id.tag_titile);
									data.remove(title);
									mAdapter.notifyDataSetChanged();
									break;
								case 2:
									// 看位置
									title = (String) view.getTag(R.id.tag_titile);
									Intent intent2 = new Intent(a, AcquireAuthLatlngActivity.class);
									bundle = new Bundle();
									bundle.putString("username", title);
									intent2.putExtras(bundle);
									a.startActivity(intent2);
									break;
								case 3:
									// 发起共享

									break;

								default:
									break;
								}
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create();
				dialog.show();
				return true;
			}
		});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(a.getApplicationContext(), "item onclick " + position, Toast.LENGTH_SHORT).show();
			}
		});
	}

	class GroupClickListener implements OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			if (expandFlag == -1) {
				// 展开被选的group
				explistview.expandGroup(groupPosition);
				// 设置被选中的group置于顶端
				explistview.setSelectedGroup(groupPosition);
				expandFlag = groupPosition;
			} else if (expandFlag == groupPosition) {
				explistview.collapseGroup(expandFlag);
				expandFlag = -1;
			} else {
				explistview.collapseGroup(expandFlag);
				// 展开被选的group
				explistview.expandGroup(groupPosition);
				// 设置被选中的group置于顶端
				explistview.setSelectedGroup(groupPosition);
				expandFlag = groupPosition;
			}
			return true;
		}
	}

	@Override
	public void onGetSharingMessage(Info info) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(a, SharingHistoryActivity.class);
		Bundle bundle = new Bundle();
		Log.e("sharing", "sharing");
		bundle.putSerializable("info", info);
		intent.putExtras(bundle);
		a.startActivity(intent);
	}

	public class MapReceiver extends BroadcastReceiver {

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getBundleExtra("info");
			Info info = (Info) bundle.get("info");
			mylocation = new LatLng(info.getLat(), info.getLng());
			locData = new MyLocationData.Builder().accuracy(info.getAccuracy())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(info.getDirection()).latitude(info.getLat()).longitude(info.getLng()).build();
			mBaiduMap.setMyLocationData(locData);
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation));
			
			if (isFirstLoc) {
				isFirstLoc = false;
				startLatLng=mylocation;
				LatLng ll = new LatLng(info.getLat(), info.getLng());
				MapStatus.Builder builder = new MapStatus.Builder();
				builder.target(ll).zoom(18.0f);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
			}

		}

	}

	private void addLine(LatLng old, LatLng neww) {
		mBaiduMap.clear();
		
		synchronized (mBaiduMap) {
			RouteSimulate rs = new RouteSimulate(a, old, neww,false);
			rs.init(mMapView, DrivingScheme.DRIVING);
			rs.doClick(DrivingScheme.DRIVING);
			start_marker.position(old).icon(start_descriptor);
			mBaiduMap.addOverlay(start_marker);
			end_marker.position(neww).icon(end_descriptor);
			mBaiduMap.addOverlay(end_marker);
		}

//		MapStatus.Builder builder = new MapStatus.Builder();
//		builder.target(old).zoom(18.0f);
//		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
		
		centerToMyLocation();
	}
	
	public void centerToMyLocation(){
		MapStatus.Builder builder = new MapStatus.Builder();
		builder.target(mylocation).zoom(18.0f);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		// 添加overlay
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(a, "位置不存在", Toast.LENGTH_SHORT).show();
		} else {
			// 定位
			MarkerOptions marker = new MarkerOptions();
			View view = a.getLayoutInflater().inflate(R.layout.self_point_marker, null, true);
			TextView tView = (TextView) view.findViewById(R.id.describe);
			Button button = (Button) view.findViewById(R.id.btn_enter);
			tView.setText(result.getAddress());

			//记录终点
			endLatLng=result.getLocation();
			
			// 将View转换为BitmapDescriptor
			BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(view);
			marker.title("点击发起导航");
			marker.position(result.getLocation()).icon(descriptor);
			mBaiduMap.clear();
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(result.getLocation());
			mBaiduMap.setMapStatus(msu);
			mBaiduMap.addOverlay(marker);
			mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				
				@Override
				public boolean onMarkerClick(final Marker marker) {
					// TODO Auto-generated method stub
					if(dialog==null||!dialog.isShowing()){
						builder = new Builder(a);
						//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						//设置对话框标题
						builder.setTitle("提示信息");
						//设置对话框内的文本
						builder.setMessage("确定要到这儿去吗？");
						//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						        @Override
						        public void onClick(DialogInterface dialog, int which) {
						           //发起路线规划
						        	mBaiduMap.clear();
						        	addLine(mylocation,marker.getPosition());
						        	following.setVisibility(View.VISIBLE);
						        }
						});
						
						//使用builder创建出对话框对象
						dialog = builder.create();
						//显示对话框
						dialog.show();
					}
					return true;
				}
			});
			
			mMapView.invalidate();

		}
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showSelfLocation(Info info) {
		// TODO Auto-generated method stub
		
	}

	
	public interface OnGetNaviDataListener{
		void dorouteplanToNavi(LatLng st,LatLng ed);
	}

}
