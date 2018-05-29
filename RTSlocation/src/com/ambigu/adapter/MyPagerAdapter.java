
package com.ambigu.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnAuthNoteListener;
import com.ambigu.listener.OnDeleteFriendListener;
import com.ambigu.listener.OnGetSelfLocationListener;
import com.ambigu.listener.OnSharingMessageListener;
import com.ambigu.listener.OnSharingResListener;
import com.ambigu.model.DrivingScheme;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.MessageOfPerson;
import com.ambigu.model.ReqScheme;
import com.ambigu.model.User;
import com.ambigu.navi.NaviGuideActivity;
import com.ambigu.route.RouteSimulate;
import com.ambigu.rtslocation.AcquireAuthLatlngActivity;
import com.ambigu.rtslocation.AuthInfoActivity;
import com.ambigu.rtslocation.LoginActivity;
import com.ambigu.rtslocation.MainActivity;
import com.ambigu.rtslocation.MessageActivity;
import com.ambigu.rtslocation.MyLocationHistoryActivity;
import com.ambigu.rtslocation.R;
import com.ambigu.rtslocation.RegisterActivity;
import com.ambigu.rtslocation.ResetPwdActivity;
import com.ambigu.rtslocation.SharingPartyActivity;
import com.ambigu.rtslocation.SharingPartyActivity.SharingReceiver;
import com.ambigu.settings.SettingsActivity;
import com.ambigu.settings.SharingHistoryActivity;
import com.ambigu.settings.SupportSettingsActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.LogUtils;
import com.ambigu.util.RTSMessage;
import com.ambigu.util.Utils;
import com.ambigu.view.CircleImageView;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.ambigu.view.SwipeListView;
import com.baidu.location.a.r;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
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
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.panosdk.plugin.indoor.R.string;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * @author
 * @version
 */
public class MyPagerAdapter extends PagerAdapter implements OnSharingMessageListener, OnGetGeoCoderResultListener,
		OnGetSelfLocationListener, OnAuthNoteListener, OnDeleteFriendListener, OnSharingResListener {

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
	private Info adapterinfo;
	private SwipeAdapter mAdapter;
	private ImageButton sharing_ib;
	private BitmapDescriptor mIconLocation;
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
	private OnGetNaviDataListener onGetNaviDataListener;
	private Handler handler;
	private Thread reqthread = null;
	private boolean isShow = false;
	private boolean isSharing = false;
	private boolean isClicked = false;
	private String users[];
	private String toUser;
	private SharingReceiver receiver1;
	public LatLng oldLatlng;

	public MyPagerAdapter(List<View> views, Activity a, ViewPager viewPager, Info adapterinfo) {
		super();
		this.views = views;
		this.a = a;
		this.adapterinfo = adapterinfo;
		this.onGetNaviDataListener = (OnGetNaviDataListener) a;
		initHandler();
		initReceiver();
		DiscardClientHandler.getInstance().setOnSharingResListener(this);
		DiscardClientHandler.getInstance().setOnDeleteFriendListener(this);
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
			initData(view, adapterinfo);
		}
		if ((view.getTag() + "").equals(0 + "")) {
			initView0(view);
		}
		if ((view.getTag() + "").equals(2 + "")) {
			initData1(view, adapterinfo);
		}
		if ((view.getTag() + "").equals(3 + "")) {
			initListener(view, adapterinfo);
			DiscardClientHandler.getInstance().setOnSharingMessageListener(this);
			DiscardClientHandler.getInstance().setOnGetSelfLocationListener(this);
			DiscardClientHandler.getInstance().setOnAuthNoteListener(this);
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

		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		sharing_ib.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("click", "点击了");

				int[] location = new int[2];
				sharing_ib.getLocationOnScreen(location);
				int x = location[0];
				int y = location[1];
				Toast toast = Toast.makeText(a, "实时共享", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP | Gravity.LEFT, x, y + 10);
				toast.getView().getBackground().setAlpha(100);// 设置透明度
				toast.show();
				// 发起共享
				if (!isClicked) {// 点击
					users = initFriendData();
					if (!isSharing) {
						AlertDialog dialog = new AlertDialog.Builder(a).setIcon(R.drawable.qq_icon)// 设置标题的图片
								.setTitle("请选择好友")// 设置对话框的标题
								.setItems(users, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										toUser = users[which];
										isSharing = true;// 正在共享
										realTimeSharing(toUser, 0);
									}
								}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										sharing_ib.setBackgroundResource(R.drawable.sharing);
										isClicked = false;

									}
								}).create();
						dialog.show();
					} else {
						AlertDialog.Builder builder = new Builder(a);
						// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						// 设置对话框标题
						builder.setTitle("提示信息");
						// 设置对话框内的文本
						builder.setMessage("您已经在共享中，不可再次请求！");
						// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								sharing_ib.setBackgroundResource(R.drawable.sharing);
							}
						});

						// 使用builder创建出对话框对象
						AlertDialog dialog = builder.create();
						// 显示对话框
						dialog.show();
					}
				} else {// 取消
					isSharing = false;
					sharing_ib.setBackgroundResource(R.drawable.sharing);
					Info info1 = new Info();
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
					a.sendBroadcast(broadCastIntent);

					// 清除程序共享状态
					Utils.clearSharingState(a);
				}
				isClicked = !isClicked;
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
		ImageButton center = (ImageButton) view.findViewById(R.id.mylocation);
		center.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				centerToMyLocation();
			}
		});

		following = (ImageButton) view.findViewById(R.id.follwing);
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

		receiver1 = new SharingReceiver();
		IntentFilter filter1 = new IntentFilter("com.ambigu.service.ReturnLocation");
		a.registerReceiver(receiver1, filter1);
	}

	private void initListener(View view, Info info) {
		// TODO Auto-generated method stub
		// 设置头像
		String iconPath = a.getCacheDir().getAbsolutePath() + "//"
				+ ApplicationVar.getId() + "//Icon" + "//" + ApplicationVar.getId() + ".png";
		showImage(iconPath);

		LinearLayout auth_history = (LinearLayout) view.findViewById(R.id.auth_history);
		LinearLayout resetPwd = (LinearLayout) view.findViewById(R.id.resetPwd);
		LinearLayout driving_history = (LinearLayout) view.findViewById(R.id.driving_history);
		LinearLayout myinfo = (LinearLayout) view.findViewById(R.id.myinfo);
		LinearLayout message = (LinearLayout) view.findViewById(R.id.message);
		LinearLayout settings = (LinearLayout) view.findViewById(R.id.settings);
		LinearLayout loginout = (LinearLayout) view.findViewById(R.id.loginout);
		CircleImageView touxiang = (CircleImageView) view.findViewById(R.id.touxiang);

		touxiang.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final AlertDialog.Builder normalDialog = new AlertDialog.Builder(a);
				normalDialog.setIcon(R.drawable.ic_launcher);
				normalDialog.setTitle("提示信息");
				normalDialog.setMessage("您要修改头像吗？");
				normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Utils.getInstance().selectPicture(a);
					}
				});
				normalDialog.show();
			}
		});

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

		// 修改密码
		resetPwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(a, ResetPwdActivity.class);
				a.startActivity(intent);
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

		driving_history.setOnClickListener(new OnClickListener() {

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

		auth_history.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送请求共享历史信息
				Info info = new Info();
				info.setFromUser(ApplicationVar.getId());
				info.setInfoType(EnumInfoType.GET_AUTH_NOTE);
				RTSClient.writeAndFlush(info);
			}
		});

	}
	// 加载图片
	public void showImage(String imaePath) {
		Bitmap bm = BitmapFactory.decodeFile(imaePath);
		ImageView iv_icon = ((ImageView) a.findViewById(R.id.touxiang));
		iv_icon.setBackgroundResource(0);
		iv_icon.setImageBitmap(bm);
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
				final String items[] = { "打开", "删除", "查看其位置", "发起共享" };
				AlertDialog dialog = new AlertDialog.Builder(a).setIcon(R.drawable.qq_icon)// 设置标题的图片
						.setTitle("操作")// 设置对话框的标题
						.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									Bundle bundle = new Bundle();
									bundle.putString("name", view.getTag() + "");
									bundle.putSerializable("info", adapterinfo);
									// bundle.putSerializable("viewpager",
									// (Serializable) viewPager);
									Intent intent = new Intent(a, MessageActivity.class);
									intent.putExtras(bundle);
									a.startActivity(intent);
									break;
								case 1:
									// view.setVisibility(View.GONE);
									deleteFriend(view.getTag(R.id.tag_child) + "", view.getTag(R.id.tag_group) + "");
									break;
								case 2:
									String title = (String) view.getTag(R.id.tag_titile);
									Intent intent2 = new Intent(a, AcquireAuthLatlngActivity.class);
									bundle = new Bundle();
									bundle.putString("username", title);
									intent2.putExtras(bundle);
									a.startActivity(intent2);
									break;
								case 3:// 实时共享
									Intent intent3 = new Intent(a, SharingPartyActivity.class);
									Bundle bundle2 = new Bundle();
									bundle2.putString("toUser", (String) view.getTag(R.id.tag_titile));
									intent3.putExtras(bundle2);
									a.startActivity(intent3);
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
		if (view != null)
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
									bundle.putString("name", (String) view.getTag(R.id.tag_titile));
									bundle.putSerializable("info", adapterinfo);
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
									Intent intent3 = new Intent(a, SharingPartyActivity.class);
									Bundle bundle2 = new Bundle();
									bundle2.putString("toUser", (String) view.getTag(R.id.tag_titile));
									intent3.putExtras(bundle2);
									a.startActivity(intent3);
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

	private void realTimeSharing(String toUser, int tag) {// 开启实时共享
		// TODO Auto-generated method stub
		Info info = new Info();
		info.setFromUser(ApplicationVar.getId());
		info.setToUser(toUser);
		info.setView(tag);
		info.setInfoType(EnumInfoType.SHARING_REQ);
		info.setReqScheme(ReqScheme.SHARE_PARTY);
		info.setfirst(true);
		// 发送广播
		Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
		Bundle broadCastBundle = new Bundle();
		broadCastBundle.putSerializable("info", info);
		broadCastIntent.putExtra("info", broadCastBundle);
		a.sendBroadcast(broadCastIntent);
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

	private void deleteFriend(final String delid, final String delgroup) {
		// TODO Auto-generated method stub
		// 弹出提示
		AlertDialog.Builder builder = new Builder(a);
		// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
		builder.setIcon(R.drawable.ic_launcher);
		// 设置对话框标题
		builder.setTitle("提示信息");
		// 设置对话框内的文本
		builder.setMessage("确定删除吗？");
		// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Info info = new Info();
				info.setFromUser(ApplicationVar.getId());
				info.setToUser(delid);
				info.setGroup(delgroup);
				info.setState(false);
				info.setInfoType(EnumInfoType.DEL_FRIEND);
				RTSClient.writeAndFlush(info);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		// 使用builder创建出对话框对象
		AlertDialog dialog = builder.create();
		// 显示对话框
		dialog.show();
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

	private String[] initFriendData() {
		// TODO Auto-generated method stub
		String[] users = null;
		ArrayList<String> userList = new ArrayList<String>();
		// 得到用户好友信息
		Gson gson = new Gson();
		SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
		String infoString = sharedPreferences.getString("info", "{}");
		Info info = gson.fromJson(infoString, Info.class);
		HashMap<String, Group> groups = info.getFriendsList();
		Iterator<Map.Entry<String, Group>> iterator = groups.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Group> entry = iterator.next();
			Group group = entry.getValue();
			ArrayList<User> usr = group.getItems();
			for (User u : usr) {
				userList.add(u.getUserid());
			}
		}
		users = (String[]) userList.toArray(new String[userList.size()]);
		return users;
	}

	private void addLine(LatLng old, LatLng neww) {
		RouteSimulate rs = new RouteSimulate(a, old, neww, false);
		rs.init(mMapView, DrivingScheme.WALKING);
		rs.doClick(DrivingScheme.WALKING);

		centerToMyLocation();
	}

	private void addStartMarker(LatLng start) {
		start_marker.position(start).icon(start_descriptor);
		mBaiduMap.addOverlay(start_marker);
	}

	private void addEndMarker(LatLng end) {
		start_marker.position(end).icon(start_descriptor);
		mBaiduMap.addOverlay(start_marker);
	}

	public void centerToMyLocation() {
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

			// 记录终点
			endLatLng = result.getLocation();

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
					if (dialog == null || !dialog.isShowing()) {
						builder = new Builder(a);
						// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						// 设置对话框标题
						builder.setTitle("提示信息");
						// 设置对话框内的文本
						builder.setMessage("确定要到这儿去吗？");
						// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 发起路线规划
								mBaiduMap.clear();
								addLine(mylocation, marker.getPosition());
								following.setVisibility(View.VISIBLE);
							}
						});

						// 使用builder创建出对话框对象
						dialog = builder.create();
						// 显示对话框
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
		Intent intent = new Intent(a, MyLocationHistoryActivity.class);
		Bundle bundle = new Bundle();
		Log.e("sharing", "sharing");
		bundle.putSerializable("info", info);
		intent.putExtras(bundle);
		a.startActivity(intent);
	}

	public interface OnGetNaviDataListener {
		void dorouteplanToNavi(LatLng st, LatLng ed);
	}

	@Override
	public void getAuthNote(Info info) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(a, AuthInfoActivity.class);
		Bundle bundle = new Bundle();
		Log.e("sharing", "sharing");
		bundle.putSerializable("info", info);
		intent.putExtras(bundle);
		a.startActivity(intent);
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final Info info = (Info) msg.obj;
				if (info.isState()) {
					AlertDialog.Builder builder = new Builder(a);
					// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					// 设置对话框标题
					builder.setTitle("提示信息");
					// 设置对话框内的文本
					builder.setMessage("删除成功！");
					// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String delid = info.getFromUser();
							String group = info.getGroup();
							// 查找该ID
							label: for (int i = 0; i < childrenData.size(); i++) {
								for (int j = 0; j < childrenData.get(i).size(); j++) {
									String id = childrenData.get(i).get(j);
									if (id.equals(delid)) {
										childrenData.get(i).remove(j);
										childrenImg.get(i).remove(j);
										if (childrenData.get(i).size() == 0) {
											childrenData.remove(i);
											childrenImg.remove(i);
											groupData.remove(i);
										}
										break label;
									}
								}
							}
							dialog.dismiss();
							adapter.notifyDataSetChanged();
						}
					});
					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				} else {
					AlertDialog.Builder builder = new Builder(a);
					// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					// 设置对话框标题
					builder.setTitle("提示信息");
					// 设置对话框内的文本
					builder.setMessage("删除失败！");
					// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				}
			}
		};
	}

	@Override
	public void deleteFriendState(Info info) {
		// TODO Auto-generated method stub
		Log.e("main", "是哪出货哦");
		Message message = Message.obtain();
		message.obj = info;
		handler.sendMessage(message);
	}

	public void addFriend(Info info) {
		Log.e("add", info.getFromUser());
		Toast.makeText(a, "hello", Toast.LENGTH_LONG).show();
		HashMap<String, Group> friends = adapterinfo.getFriendsList();
		User user = new User();
		user.setUserid(info.getToUser());
		user.setFriend(info.getFromUser());
		user.setIcon(info.getIcon());
		Group group = friends.get(info.getGroup());
		int i = 0;
		if (group == null) {
			group = new Group();
			group.setGroupname(info.getGroup());
			ArrayList<User> items = new ArrayList<User>();
			items.add(user);
			group.setItems(items);
			friends.put(info.getGroup(), group);

			groupData.add(info.getGroup());
			ArrayList<String> childList = new ArrayList<String>();
			ArrayList<String> imgList = new ArrayList<String>();
			childList.add(info.getToUser());
			imgList.add(info.getIcon());
			childrenData.add(childList);
			childrenImg.add(imgList);

		} else {
			group.getItems().add(user);

			for (i = 0; i < groupData.size(); i++) {
				if (info.getGroup().equals(groupData.get(i))) {
					childrenData.get(i).add(info.getToUser());
					childrenImg.get(i).add(info.getIcon());
					break;
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void getSharingRes(final Info info) {
		// TODO Auto-generated method stub
		LogUtils.showLoG(a, "回来了1");
		if (info.getInfoType() == EnumInfoType.SHARING_RES && info.isState() && !info.isend()
				&& info.getReqScheme() == ReqScheme.BE_SHARED_PARTY) {// 正常情况
			// 读取系统设置中的共享频率
			a.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					sharing_ib.setBackgroundResource(R.drawable.sharing_pressed);
				}
			});

			reqthread = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SharedPreferences sharedPreferences = ApplicationVar.getSharedPreferences();
					int times = sharedPreferences.getInt("update_times", 10);
					try {
						LogUtils.showLoG(a, "回来了");
						Thread.sleep(times * 1000);
						Info info1 = new Info();
						info1.setFromUser(ApplicationVar.getId());
						info1.setToUser(info.getFromUser());
						info1.setInfoType(EnumInfoType.SHARING_REQ);
						info1.setReqScheme(ReqScheme.SHARE_PARTY);
						// 发送广播
						Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
						Bundle broadCastBundle = new Bundle();
						broadCastBundle.putSerializable("info", info1);
						broadCastIntent.putExtra("info", broadCastBundle);
						a.sendBroadcast(broadCastIntent);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			reqthread.start();
		} else if (info.getReqScheme() == ReqScheme.BE_SHARED_PARTY && info.isend()
				&& info.getInfoType() == EnumInfoType.SHARING_REQ) {// 对方请求结束共享
			// 应该发送回复信息
			a.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					AlertDialog.Builder builder = new Builder(a);
					// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					// 设置对话框标题
					builder.setTitle("提示信息");
					// 设置对话框内的文本
					builder.setMessage("对方已停止实时共享！");
					// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							sharing_ib.setBackgroundResource(R.drawable.sharing);
							// 清除程序共享状态
							Utils.clearSharingState(a);
						}
					});
					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				}
			});
		} else if (info.getReqScheme() == ReqScheme.SERVER && info.getInfoType() == EnumInfoType.SHARING_RES) {// 程序异常结束共享发送最后一条信息以结束行程
			isSharing = false;
			if (!isShow) {
				// 发送共享结束信息
				Info info1 = new Info();
				info1.setFromUser(ApplicationVar.getId());
				info1.setToUser(info.getFromUser());
				info1.setDrivingstate(0);
				info1.setend(true);
				info.setReqScheme(ReqScheme.SERVER);// 服务器端知道后设置结束
				info1.setInfoType(EnumInfoType.SHARING_REQ);
				// 发送广播
				Intent broadCastIntent = new Intent("com.ambigu.rtslocation.RealTimeSharing");
				Bundle broadCastBundle = new Bundle();
				broadCastBundle.putSerializable("info", info1);
				broadCastIntent.putExtra("info", broadCastBundle);
				a.sendBroadcast(broadCastIntent);
				a.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						AlertDialog.Builder builder = new Builder(a);
						// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
						builder.setIcon(R.drawable.ic_launcher);
						// 设置对话框标题
						builder.setTitle("提示信息");
						// 设置对话框内的文本
						if (info.isfirst() && !info.isend())
							builder.setMessage("发起共享失败，请检查网络状态或提示好友上线！");
						else if (!info.isfirst() && info.isend())
							builder.setMessage("共享遭遇意外情况退出！");// 这时应该写入数据库
						// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();

								sharing_ib.setBackgroundResource(R.drawable.sharing);
								// 清除程序共享状态
								Utils.clearSharingState(a);
							}
						});

						// 使用builder创建出对话框对象
						AlertDialog dialog = builder.create();
						// 显示对话框
						dialog.show();
					}
				});
				isShow = true;
			}
		} else if (!info.isState() && info.getInfoType() == EnumInfoType.SHARING_RES
				&& info.getReqScheme() == ReqScheme.BE_SHARED_PARTY) {
			// 对方拒绝请求
			a.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					AlertDialog.Builder builder = new Builder(a);
					// 设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
					builder.setIcon(R.drawable.ic_launcher);
					// 设置对话框标题
					builder.setTitle("提示信息");
					// 设置对话框内的文本
					builder.setMessage("对方拒绝您的请求！");
					// 设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							sharing_ib.setBackgroundResource(R.drawable.sharing);
							// 清除程序共享状态
							Utils.clearSharingState(a);
						}
					});

					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				}
			});
		} else if (info.isState() && info.isend() && info.getReqScheme() == ReqScheme.BE_SHARED_PARTY && info.isState()
				&& info.getInfoType() == EnumInfoType.SHARING_RES) {
			// 对方确认停止发送信息
			reqthread.interrupt();// 不应该再发了，即使刚刚仍有信息在睡眠中未发出去
		}
	}

	public class SharingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtils.showLoG(a, "收到了");
			Bundle bundle = intent.getBundleExtra("info");
			Info info = (Info) bundle.get("info");
			double lat = info.getLat();
			double lng = info.getLng();
			LatLng latLng = new LatLng(lat, lng);
			if (info.isfirst()) {
				oldLatlng = latLng;
				addStartMarker(oldLatlng);
			} else {
				addLine(oldLatlng, latLng);
				oldLatlng = latLng;
			}
			if (info.isend()) {
				addEndMarker(oldLatlng);
			}
		}

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
				startLatLng = mylocation;
				LatLng ll = new LatLng(info.getLat(), info.getLng());
				MapStatus.Builder builder = new MapStatus.Builder();
				builder.target(ll).zoom(18.0f);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
			}

		}

	}
}
