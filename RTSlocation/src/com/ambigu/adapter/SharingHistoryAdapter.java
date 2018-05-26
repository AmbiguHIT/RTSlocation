package com.ambigu.adapter;

import java.util.ArrayList;

import com.ambigu.model.SharingHistoryOfPerson;
import com.ambigu.model.SingleSharingHistoryInfo;
import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
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

public class SharingHistoryAdapter extends BaseExpandableListAdapter implements HeaderAdapter {
	private ArrayList<SharingHistoryOfPerson> sharingHistorys;
	private Context context;
	private PinnedHeaderExpandableListView listView;
	private LayoutInflater inflater;

	public SharingHistoryAdapter(ArrayList<SharingHistoryOfPerson> sharingHistorys, Context context,
			PinnedHeaderExpandableListView listView) {
		this.sharingHistorys = sharingHistorys;
		this.context = context;
		this.listView = listView;
		inflater = LayoutInflater.from(this.context);
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
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
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

		return view;
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

}
