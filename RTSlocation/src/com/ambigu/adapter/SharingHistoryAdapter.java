package com.ambigu.adapter;

import java.util.ArrayList;

import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnDeleteSharingHistory;
import com.ambigu.model.Info;
import com.ambigu.model.SharingHistoryOfPerson;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.rtslocation.AcquireAuthLatlngActivity;
import com.ambigu.rtslocation.MessageActivity;
import com.ambigu.rtslocation.R;
import com.ambigu.settings.HistoryMap;
import com.ambigu.settings.SharingHistoryActivity;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.ambigu.view.PinnedHeaderExpandableListView.HeaderAdapter;

import android.R.integer;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SharingHistoryAdapter extends BaseExpandableListAdapter implements HeaderAdapter,OnDeleteSharingHistory {
	private ArrayList<SharingHistoryOfPerson> sharingHistorys;
	private Context context;
	private PinnedHeaderExpandableListView listView;
	private LayoutInflater inflater;
	private Handler handler;

	public SharingHistoryAdapter(ArrayList<SharingHistoryOfPerson> sharingHistorys, Context context,
			PinnedHeaderExpandableListView listView) {
		this.sharingHistorys = sharingHistorys;
		this.context = context;
		this.listView = listView;
		inflater = LayoutInflater.from(this.context);
		initHandler();
		DiscardClientHandler.getInstance().setOnDeleteSharingHistory(this);
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final Info info=(Info)msg.obj;
				if(info.isState()){//删除成功
					AlertDialog.Builder builder = new Builder(context);
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
							int groupPos=info.getGroupPos();
							int childPos=info.getChildPos();
							SharingHistoryOfPerson sharingHistory = sharingHistorys.get(groupPos);
							sharingHistory.getSharingHistoryInfos().remove(childPos);
							if(sharingHistory.getSharingHistoryInfos().size()==0){
								sharingHistorys.remove(groupPos);
							}
							notifyDataSetChanged();
						}
					});
					// 使用builder创建出对话框对象
					AlertDialog dialog = builder.create();
					// 显示对话框
					dialog.show();
				}else{
					AlertDialog.Builder builder = new Builder(context);
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
	public Object getChild(int groupPosition, int childPosition) {
		return sharingHistorys.get(groupPosition).getSharingHistoryInfos().get(childPosition);
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
		view.setTag(R.id.tag_grouppositon,groupPosition);//设置tag
		view.setTag(R.id.tag_childpositon,childPosition);//设置tag
		SharingHistoryOfPerson sharingHistory = sharingHistorys.get(groupPosition);
		SingleSharingHistoryInfo singleSharingHistoryInfo = sharingHistory.getSharingHistoryInfos().get(childPosition);
		TextView start_time = (TextView) view.findViewById(R.id.start_time);
		TextView stop_time = (TextView) view.findViewById(R.id.stop_time);
		TextView start_point = (TextView) view.findViewById(R.id.start_point);
		TextView stop_point = (TextView) view.findViewById(R.id.end_point);
		TextView distance = (TextView) view.findViewById(R.id.distance);

		start_time.setText(singleSharingHistoryInfo.getStart_time());
		stop_time.setText(singleSharingHistoryInfo.getEnd_time());
		start_point.setText(singleSharingHistoryInfo.getStart_point());
		stop_point.setText(singleSharingHistoryInfo.getEnd_point());
		distance.setText(singleSharingHistoryInfo.getDistance());
		if(singleSharingHistoryInfo.isFromMe()) view.setBackgroundColor(Color.argb(100, 0, 250, 150));
		else view.setBackgroundColor(Color.argb(100, 255, 99, 71));
		view.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				final String items[] = { "打开", "删除"};
				AlertDialog dialog = new AlertDialog.Builder(context).setIcon(R.drawable.qq_icon)// 设置标题的图片
						.setTitle("操作")// 设置对话框的标题
						.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									SingleSharingHistoryInfo singleSharingHistoryInfo=sharingHistorys.get(groupPosition).getSharingHistoryInfos().get(childPosition);
									Intent intent =new Intent(context,HistoryMap.class);
									Bundle bundle=new Bundle();
									bundle.putSerializable("mapinfo", singleSharingHistoryInfo);
									intent.putExtras(bundle);
									context.startActivity(intent);
									break;
								case 1:
									deleteSharingHistory(groupPosition,childPosition);
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
		
		return view;
	}



	private void deleteSharingHistory(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		SingleSharingHistoryInfo singleSharingHistoryInfo=sharingHistorys.get(groupPosition).getSharingHistoryInfos().get(childPosition);
		Info info=new Info();
		info.setTime(singleSharingHistoryInfo.getStart_time());
		if(singleSharingHistoryInfo.isFromMe()){
			info.setFromUser(ApplicationVar.getId());
			info.setToUser(singleSharingHistoryInfo.getToUser());
		}else{
			info.setToUser(ApplicationVar.getId());
			info.setFromUser(singleSharingHistoryInfo.getToUser());
		}
		info.setGroupPos(groupPosition);
		info.setChildPos(childPosition);
		info.setInfoType(EnumInfoType.DEL_SHARING_MES);
		info.setState(false);
		RTSClient.writeAndFlush(info);
		
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		if (sharingHistorys.size() == 0)
			return 0;
		return sharingHistorys.get(groupPosition).getSharingHistoryInfos().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return sharingHistorys.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return sharingHistorys.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = createGroupView();
		}
		Log.e("groupPosition", "" + groupPosition);
		SharingHistoryOfPerson sharingHistory = sharingHistorys.get(groupPosition);
		// 设置icon
		ImageView groupIcon = (ImageView) view.findViewById(R.id.groupIcon);
		Drawable bitmap = BitmapDrawable.createFromPath(context.getCacheDir().getAbsolutePath() + "//"
				+ ApplicationVar.getId() + "//Icon" + "//" + sharingHistory.getToUser() + ".png");
		groupIcon.setBackground(bitmap);
		TextView expand = (TextView) view.findViewById(R.id.groupexpand);
		TextView groupname = (TextView) view.findViewById(R.id.groupname);
		groupname.setText(sharingHistory.getToUser());
		if (isExpanded) {
			expand.setText("关闭");
			expand.setTextColor(0xfdFF6A6A);
		} else {
			expand.setText("展开");
			expand.setTextColor(0xfd666666);
		}

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
		return inflater.inflate(R.layout.sharing_history_child, null);
	}

	private View createGroupView() {
		return inflater.inflate(R.layout.sharing_history_group, null);
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
		SharingHistoryOfPerson sharingHistory = sharingHistorys.get(groupPosition);
		// 设置icon
		ImageView groupIcon = (ImageView) view.findViewById(R.id.groupIcon);
		Drawable bitmap = BitmapDrawable.createFromPath(context.getCacheDir().getAbsolutePath() + "//"
				+ ApplicationVar.getId() + "//Icon" + "//" + sharingHistory.getToUser() + ".png");
		groupIcon.setBackground(bitmap);
		TextView expand = (TextView) view.findViewById(R.id.groupexpand);
		TextView groupname = (TextView) view.findViewById(R.id.groupname);
		groupname.setText(sharingHistory.getToUser());
		expand.setText("关闭");
		expand.setTextColor(0xfdFF6A6A);

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
	public void deleteHistory(Info info) {
		// TODO Auto-generated method stub
		Message message=Message.obtain();
		message.obj=info;
		handler.sendMessage(message);
	}

}
