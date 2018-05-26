package com.ambigu.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.ambigu.rtslocation.ChatMsgEntity;
import com.ambigu.rtslocation.R;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.FaceConversionUtil;
import com.ant.liao.GifView;

/**
 * 
 ******************************************
 * @author 廖乃波
 * @文件名称 : ChatMsgAdapter.java
 * @创建时间 : 2013-1-27 下午02:33:16
 * @文件描述 : 消息数据填充起
 ******************************************
 */
public class ChatMsgAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<ChatMsgEntity> coll;
	private LayoutInflater mInflater;
	private Context context;

	public ChatMsgAdapter(Context context, List<ChatMsgEntity> coll) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		ChatMsgEntity entity = coll.get(position);

		if (entity.getMsgType()) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
				ImageView iv_userhead=(ImageView) convertView.findViewById(R.id.iv_userhead);
				Drawable bitmap=BitmapDrawable.createFromPath(context.getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()
				+"//Icon"+"//"+entity.getName()+".png");
				iv_userhead.setBackground(bitmap);
			} else {
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
				ImageView iv_userhead=(ImageView) convertView.findViewById(R.id.iv_userhead);
				Drawable bitmap=BitmapDrawable.createFromPath(context.getCacheDir().getAbsolutePath()+"//"+ApplicationVar.getId()
				+"//Icon"+"//"+ApplicationVar.getId()+".png");
				iv_userhead.setBackground(bitmap);
				GifView iv_send=(GifView)convertView.findViewById(R.id.iv_issend);
				switch (entity.getSendState()) {
				case 0://未发送
					iv_send.setBackgroundResource(R.drawable.tanhao);
					break;
				case 1://正在发送
					iv_send.setGifImage(R.drawable.img);
					break;
				case 2://发送完成
					iv_send.setBackground(null);//设置透明
					break;

				default:
					break;
				}
			}
			

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.getDate());
		SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context,
				entity.getText());
		viewHolder.tvContent.setText(spannableString);

		return convertView;
	}

	class ViewHolder {
		public TextView tvSendTime;
		public TextView tvContent;
		public boolean isComMsg = true;
	}

}