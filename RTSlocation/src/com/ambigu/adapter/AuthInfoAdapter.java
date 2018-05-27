package com.ambigu.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ambigu.client.RTSClient;
import com.ambigu.model.AuthNode;
import com.ambigu.model.Info;
import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.EnumInfoType;
import com.ambigu.view.PinnedHeaderExpandableListView.HeaderAdapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class AuthInfoAdapter extends BaseExpandableListAdapter implements HeaderAdapter{
	
	private HashMap<String,ArrayList<AuthNode>> childNodes;
	private ArrayList<String> groups;
	private Context context;
	//private ArrayList<HashMap<Integer, Boolean>> isVisable;
	private ArrayList<HashMap<Integer, Boolean>> isChecked;
	private Button sure,cancel;
	private boolean isMulChoice=false;

	public AuthInfoAdapter(HashMap<String,ArrayList<AuthNode>> childNodes,ArrayList<String> groups,Context context,Button sure,Button cancel) {
		// TODO Auto-generated constructor stub
		this.childNodes=childNodes;
		this.context=context;
		this.groups=groups;
		this.sure=sure;
		this.cancel=cancel;
		//isVisable=new ArrayList<HashMap<Integer, Boolean>>();
		this.isChecked=new ArrayList<HashMap<Integer, Boolean>>();
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//确定删除弹框
				AlertDialog.Builder builder = new Builder(context);
				//设置对话框图标，可以使用自己的图片，Android本身也提供了一些图标供我们使用
				builder.setIcon(R.drawable.ic_launcher);
				//设置对话框标题
				builder.setTitle("提示信息");
				//设置对话框内的文本
				builder.setMessage("确定删除这些记录吗？");
				//设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				        @Override
				        public void onClick(DialogInterface dialog, int which) {
				        	dialog.dismiss();
				        	HashMap<String,ArrayList<AuthNode>> selects=new HashMap<String, ArrayList<AuthNode>>();
				        	for(int i=0;i<groups.size();i++){//分组
				        		ArrayList<AuthNode> authNodes=new ArrayList<AuthNode>();
				        		String groupname = groups.get(i);
				        		selects.put(groupname, authNodes);
				        		HashMap<Integer, Boolean> maps=isChecked.get(i);
				        		Iterator<Map.Entry<Integer, Boolean>> iterator=maps.entrySet().iterator();
				        		while(iterator.hasNext()){
				        			Map.Entry<Integer, Boolean> entry=iterator.next();
				        			if(entry.getValue()){//此项被选中
				        				int childpos=entry.getKey();
				        				authNodes.add(childNodes.get(groupname).get(childpos));
				        			}
				        		}
				        	}
				        	Info info=new Info();
				        	info.setFromUser(ApplicationVar.getId());
				        	info.setInfoType(EnumInfoType.DEL_AUTH_NOTE);
				        	info.setState(false);
				        	info.setAuthNodes(selects);//删除这些
				        	RTSClient.writeAndFlush(info);
				        }
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				        @Override
				        public void onClick(DialogInterface dialog, int which) {
				        	dialog.dismiss();
				        	
				        }
				});
				
				//使用builder创建出对话框对象
				AlertDialog dialog = builder.create();
				//显示对话框
				dialog.show();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
	        	isMulChoice=false;
	        	for(int i=0;i<groups.size();i++){
					String groupname=groups.get(i);
					int size=childNodes.get(groupname).size();
					HashMap<Integer, Boolean> maps = isChecked.get(i);
					for(int j=0;j<size;j++){
						maps.put(j, false);
					}
				}
				notifyDataSetChanged();
			}
		});
		
		
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return groups.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return childNodes.get(groups.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return groups.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childNodes.get(groups.get(groupPosition));
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView=LayoutInflater.from(context).inflate(R.layout.auth_history_group, null);
		}
		TextView groupnametv=(TextView) convertView.findViewById(R.id.groupname);
		ImageView groupIcon=(ImageView) convertView.findViewById(R.id.groupIcon);
		String groupname = groups.get(groupPosition);
		groupnametv.setText(groupname);
		
		//读取icon
		Drawable bitmap=BitmapDrawable.createFromPath(context.getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()
		+"//Icon"+"//"+groupname+".png");
		groupIcon.setBackground(bitmap);
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition,final int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView=LayoutInflater.from(context).inflate(R.layout.auth_history_child, null);
		}
		TextView start_time=(TextView) convertView.findViewById(R.id.start_time);
		TextView end_time=(TextView) convertView.findViewById(R.id.end_time);
		final CheckBox checkBox=(CheckBox)convertView.findViewById(R.id.check);
		String group = groups.get(groupPosition);
		AuthNode authNode=childNodes.get(group).get(childPosition);
		start_time.setText(authNode.getStart_time());
		end_time.setText(authNode.getEnd_time());
		if(isMulChoice){
			checkBox.setVisibility(View.VISIBLE);
			boolean ischecked=isChecked.get(groupPosition).get(childPosition);
			if(childPosition==0)
				Log.e("view",ischecked+"");
			checkBox.setChecked(ischecked);
		}else{
			checkBox.setVisibility(View.GONE);
		}
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isMulChoice){
					boolean ischecked=isChecked.get(groupPosition).get(childPosition);
					isChecked.get(groupPosition).put(childPosition, !ischecked);
					checkBox.setChecked(!ischecked);
					notifyDataSetChanged();
				}
			}
		});
		
		convertView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(!isMulChoice){
					isMulChoice=true;
					Log.e("很好","点击了");
					for(int i=0;i<groups.size();i++){
						String groupname=groups.get(i);
						int size=childNodes.get(groupname).size();
						HashMap<Integer, Boolean> maps = new HashMap<Integer, Boolean>();
						isChecked.add(maps);
						for(int j=0;j<size;j++){
							maps.put(j, false);
						}
					}
					notifyDataSetChanged();
				}
				return true;
			}
		});
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getHeaderState(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void configureHeader(View header, int groupPosition, int childPosition, int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroupClickStatus(int groupPosition, int status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getGroupClickStatus(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
