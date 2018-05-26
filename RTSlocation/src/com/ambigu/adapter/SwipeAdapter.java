
package com.ambigu.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.FileUtils;
import com.ambigu.util.RTSMessage;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwipeAdapter extends BaseAdapter {

	private Context mContext = null;
	private  HashMap<String,RTSMessage> data;

	private int mRightWidth = 0;

	public SwipeAdapter(Context ctx,  HashMap<String,RTSMessage> data, int rightWidth) {
		mContext = ctx;
		this.data = data;
		mRightWidth = rightWidth;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
			holder = new ViewHolder();
			holder.item_left = (RelativeLayout) convertView.findViewById(R.id.item_left);
			holder.item_right = (RelativeLayout) convertView.findViewById(R.id.item_right);

			holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_msg = (TextView) convertView.findViewById(R.id.tv_msg);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

			holder.item_right_txt = (TextView) convertView.findViewById(R.id.item_right_txt);
			convertView.setTag(R.id.tag_holder,holder);
		} else {// 有直接获得ViewHolder
			holder = (ViewHolder) convertView.getTag(R.id.tag_holder);
		}

		LinearLayout.LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		holder.item_left.setLayoutParams(lp1);
		LinearLayout.LayoutParams lp2 = new LayoutParams(mRightWidth, LayoutParams.MATCH_PARENT);
		holder.item_right.setLayoutParams(lp2);
		Iterator<Entry<String, RTSMessage>> iterator=data.entrySet().iterator();
		int i=0;
		RTSMessage msg = null;
		while(iterator.hasNext()){
			Map.Entry<String, RTSMessage> entry=(Entry<String, RTSMessage>) iterator.next();
			if(i==position){
				msg = (RTSMessage) entry.getValue();
				Log.e("position",position+"");
			}
			i++;
		}
		if(msg!=null){
			convertView.setTag(R.id.tag_titile, msg.getTitle());
			holder.tv_title.setText(msg.getTitle());
			holder.tv_msg.setText(msg.getMsg());
			holder.tv_time.setText(msg.getTime());

			Drawable bitmap=BitmapDrawable.createFromPath(mContext.getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()
			+"//Icon"+"//"+msg.getTitle()+".png");
			if(bitmap!=null){
				//缓存并显示
				holder.iv_icon.setBackground(bitmap);
			}else{
				holder.iv_icon.setBackgroundResource(R.drawable.nonepic);
			}
		}

		holder.item_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onRightItemClick(v, position);
				}
			}
		});
		return convertView;
	}

	static class ViewHolder {
		RelativeLayout item_left;
		RelativeLayout item_right;

		TextView tv_title;
		TextView tv_msg;
		TextView tv_time;
		ImageView iv_icon;

		TextView item_right_txt;
	}

	/**
	 * 单击事件监听�?
	 */
	private onRightItemClickListener mListener = null;

	public void setOnRightItemClickListener(onRightItemClickListener listener) {
		mListener = listener;
	}

	public interface onRightItemClickListener {
		void onRightItemClick(View v, int position);
	}
}
