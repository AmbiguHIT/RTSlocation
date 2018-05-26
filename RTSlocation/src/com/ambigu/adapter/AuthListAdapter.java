package com.ambigu.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnAuthChangedListener;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.SharingHistoryOfPerson;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.model.User;
import com.ambigu.rtslocation.AddFriendActivity;
import com.ambigu.rtslocation.R;
import com.ambigu.settings.SupportSettingsActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.ambigu.view.PinnedHeaderExpandableListView.HeaderAdapter;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class AuthListAdapter extends BaseExpandableListAdapter implements HeaderAdapter,OnClickListener,OnAuthChangedListener {
	private ArrayList<Group> friends;
	private Activity activity;
	private PinnedHeaderExpandableListView listView;
	private LayoutInflater inflater;
	private Button sure;
	private static HashMap<String, Group> friendsList=new HashMap<String, Group>();
	private Handler mhandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
        	final Info info = (Info)msg.obj;
			switch (msg.what) {
			case 1:
				AlertDialog.Builder builder = new Builder(activity);
				//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
				builder.setIcon(R.drawable.ic_launcher);
				//设置对话框标题
				builder.setTitle("提示信息");
				//设置对话框内的文本
				builder.setMessage("权限改变成功");
				//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				        @Override
				        public void onClick(DialogInterface dialog, int which) {
				                // 执行点击确定按钮的业务逻辑
				        	Intent intent= new Intent(activity,SupportSettingsActivity.class);
				        	activity.startActivity(intent);
				        	activity.finish();
				        }
				});
				//使用builder创建出对话框对象
				AlertDialog dialog = builder.create();
				//显示对话框
				dialog.show();
				break;
			case 0:
				AlertDialog.Builder builder1 = new Builder(activity);
				//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
				builder1.setIcon(R.drawable.ic_launcher);
				//设置对话框标题
				builder1.setTitle("提示信息");
				//设置对话框内的文本
				builder1.setMessage("权限改变失败");
				//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
				builder1.setPositiveButton("确定",null);
				//使用builder创建出对话框对象
				AlertDialog dialog1 = builder1.create();
				//显示对话框
				dialog1.show();
				break;

			default:
				break;
			}
		}
	};

	public AuthListAdapter(ArrayList<Group> friends, Activity activity, PinnedHeaderExpandableListView listView,Button sure) {
		this.friends = friends;
		this.activity = activity;
		this.listView = listView;
		this.sure=sure;
		this.sure.setOnClickListener(this);
		inflater = LayoutInflater.from(this.activity);
		DiscardClientHandler.getInstance().setOnAuthChangedListener(this);
	}
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return friends.get(groupPosition).getItems().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = createChildrenView();
		}
		view.setTag(R.id.tag_grouppositon, groupPosition);// 设置tag
		view.setTag(R.id.tag_childpositon, childPosition);// 设置tag
		User user=friends.get(groupPosition).getItems().get(childPosition);
		ImageButton checkImg = (ImageButton) view.findViewById(R.id.check);
		checkImg.setFocusable(false);
		if(user.isChoose()){
			checkImg.setBackgroundResource(R.drawable.check);
		}
		else{
			checkImg.setBackgroundResource(R.drawable.uncheck);
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Group group=friends.get(groupPosition);
				User user=group.getItems().get(childPosition);
				if(user.isChoose()){
					user.setChoose(false);
					group.setChoose(false);
				}else{
					user.setChoose(true);
					boolean f=false;
					for(User user1:group.getItems()){
						if(!user1.isChoose()){
							f=false;
							break;
						}
						f=true;
					}
					if(f) group.setChoose(true);
					else group.setChoose(false);
				}
				notifyDataSetChanged();
			}
		});

		// icon
		ImageView icon = (ImageView) view.findViewById(R.id.friendIcon);
		Drawable bitmap = BitmapDrawable
				.createFromPath(activity.getCacheDir().getAbsolutePath() + "//" + ApplicationVar.getId() + "//Icon"
						+ "//" + ((User) getChild(groupPosition, childPosition)).getUserid() + ".png");
		icon.setBackground(bitmap);

		// 设置文字
		TextView friendname = (TextView) view.findViewById(R.id.friendName);
		friendname.setText(((User) getChild(groupPosition, childPosition)).getUserid());
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (friends.size() == 0)
			return 0;
		Log.e("childPosition1", "" + friends.get(groupPosition).getItems().size());
		return friends.get(groupPosition).getItems().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return friends.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return friends.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = createGroupView();
		}
		Log.e("groupPosition", "" + groupPosition);
		Group group = friends.get(groupPosition);
		ImageButton checkImg = (ImageButton) view.findViewById(R.id.check);
		TextView checkTxt=(TextView)view.findViewById(R.id.check_txt);
		checkImg.setFocusable(false);
		if(group.isChoose()){
			checkImg.setBackgroundResource(R.drawable.check);
			checkTxt.setText("取消全选");
		}
		else{
			checkImg.setBackgroundResource(R.drawable.uncheck);
			checkTxt.setText("全选");
		}
		TextView groupname = (TextView) view.findViewById(R.id.groupname);
		groupname.setText(group.getGroupname());

		
		checkImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!friends.get(groupPosition).isChoose()){
					friends.get(groupPosition).setChoose(true);
					for(User user:friends.get(groupPosition).getItems()){
						user.setChoose(true);
					}
				}else{
					friends.get(groupPosition).setChoose(false);
					for(User user:friends.get(groupPosition).getItems()){
						user.setChoose(false);
					}
				}
				notifyDataSetChanged();
			}
		});
		
		
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private View createChildrenView() {
		return inflater.inflate(R.layout.auth_child, null);
	}

	private View createGroupView() {
		return inflater.inflate(R.layout.auth_group, null);
	}

	@Override
	public int getHeaderState(int groupPosition, int childPosition) {
		final int childCount = getChildrenCount(groupPosition);
		if (childPosition == childCount - 1) {
			return PINNED_HEADER_PUSHED_UP;
		} else if (childPosition == -1 && !listView.isGroupExpanded(groupPosition)) {
			return PINNED_HEADER_GONE;
		} else {
			return PINNED_HEADER_VISIBLE;
		}
	}

	@Override
	public void configureHeader(View view, int groupPosition, int childPosition, int alpha) {
		Group group = friends.get(groupPosition);

		ImageButton checkImg = (ImageButton) view.findViewById(R.id.check);
		checkImg.setFocusable(false);
		TextView checkTxt=(TextView)view.findViewById(R.id.check_txt);
		if(group.isChoose()){
			checkImg.setBackgroundResource(R.drawable.check);
			checkTxt.setText("取消全选");
		}
		else{
			checkImg.setBackgroundResource(R.drawable.uncheck);
			checkTxt.setText("全选");
		}
		TextView groupname = (TextView) view.findViewById(R.id.groupname);
		groupname.setText(group.getGroupname());

	}

	private SparseIntArray groupStatusMap = new SparseIntArray();

	@Override
	public void setGroupClickStatus(int groupPosition, int status) {
		groupStatusMap.put(groupPosition, status);
	}

	@Override
	public int getGroupClickStatus(int groupPosition) {
		if (groupStatusMap.keyAt(groupPosition) >= 0) {
			return groupStatusMap.get(groupPosition);
		} else {
			return 0;
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.e("dadasd","dsadadsadad");
		Info info=new Info();
		info.setFromUser(ApplicationVar.getId());
		for(Group group:friends){
			friendsList.put(group.getGroupname(), group);
		}
		info.setFriendsList(friendsList);
		info.setInfoType(EnumInfoType.CHANGE_AUTH);
		info.setState(false);
		RTSClient.writeAndFlush(info);
	}
	
	@Override
	public void notifyAuthChanged(Info info) {
		// TODO Auto-generated method stub
		if(info.isState()){
			Gson gson=new Gson();//更新权限
			SharedPreferences sharedPreferences=ApplicationVar.getSharedPreferences();
			Info info2=gson.fromJson(sharedPreferences.getString("info", "{}"), Info.class);
			info2.setFriendsList(friendsList);
			Editor editor=sharedPreferences.edit();
			editor.putString("info", gson.toJson(info2));
			editor.commit();
			mhandler.sendEmptyMessage(1);
		}else{
			mhandler.sendEmptyMessage(0);
		}
	}

}
