package com.ambigu.adapter;

import java.util.ArrayList;

import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.FileUtils;
import com.ambigu.view.PinnedHeaderExpandableListView;
import com.ambigu.view.PinnedHeaderExpandableListView.HeaderAdapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PinnedHeaderExpandableAdapter extends BaseExpandableListAdapter implements HeaderAdapter {
	private ArrayList<ArrayList<String>> childrenData;
	private ArrayList<ArrayList<String>> childrenImg;
	private ArrayList<String> groupData;
	private Context context;
	private PinnedHeaderExpandableListView listView;
	private LayoutInflater inflater;

	public PinnedHeaderExpandableAdapter(ArrayList<ArrayList<String>> childrenData, ArrayList<String> groupData,ArrayList<ArrayList<String>> childrenImg, Context context,
			PinnedHeaderExpandableListView listView) {
		this.groupData = groupData;
		this.childrenData = childrenData;
		this.context = context;
		this.childrenImg=childrenImg;
		this.listView = listView;
		inflater = LayoutInflater.from(this.context);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childrenData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = createChildrenView();
		}
		//设置tag
		view.setTag(R.id.tag_titile, childrenData.get(groupPosition).get(childPosition));
		view.setTag(R.id.tag_group, groupData.get(groupPosition));
		view.setTag(R.id.tag_child, childrenData.get(groupPosition).get(childPosition));
		
		ImageView img=(ImageView)view.findViewById(R.id.groupIcon);
		TextView text = (TextView) view.findViewById(R.id.childto);
		text.setText(childrenData.get(groupPosition).get(childPosition));
		//缓存并显示
		if(childrenImg.get(groupPosition).get(childPosition)!=null){
			FileUtils.base64ToFile(childrenImg.get(groupPosition).get(childPosition), 
					context.getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()+"//Icon"
							+"//"+childrenData.get(groupPosition).get(childPosition)+".png");
		}
		Drawable bitmap=BitmapDrawable.createFromPath(context.getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()
		+"//Icon"+"//"+childrenData.get(groupPosition).get(childPosition)+".png");
		img.setBackground(bitmap);
		
		//view.add
		view.setTag(childrenData.get(groupPosition).get(childPosition));
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(groupData.size()==0) return 0;
		Log.e("size",groupData.size()+"");
		return childrenData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
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

		ImageView iv = (ImageView) view.findViewById(R.id.groupIcon);

		if (isExpanded) {
			iv.setImageResource(R.drawable.opend);
		} else {
			iv.setImageResource(R.drawable.closed);
		}

		TextView text = (TextView) view.findViewById(R.id.groupto);
		text.setText(groupData.get(groupPosition));
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
		return inflater.inflate(R.layout.child, null);
	}

	private View createGroupView() {
		return inflater.inflate(R.layout.group, null);
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
	public void configureHeader(View header, int groupPosition, int childPosition, int alpha) {
		String groupData = this.groupData.get(groupPosition);
		((TextView) header.findViewById(R.id.groupto)).setText(groupData);

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
	
}
