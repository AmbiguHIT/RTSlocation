package com.ambigu.rtslocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ambigu.adapter.ChatMsgAdapter;
import com.ambigu.client.DiscardClientHandler;
import com.ambigu.client.RTSClient;
import com.ambigu.listener.OnMessageListener;
import com.ambigu.listener.OnMessageSendState;
import com.ambigu.model.Info;
import com.ambigu.model.MessageContent;
import com.ambigu.model.MessageOfPerson;
import com.ambigu.util.ApplicationVar;
import com.ambigu.util.DateUtil;
import com.ambigu.util.EnumInfoType;
import com.ambigu.view.FaceRelativeLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MessageActivity extends Activity implements OnClickListener, OnMessageListener,OnMessageSendState {

	private Button mBtnSend;

	private EditText mEditTextContent;

	private ListView mListView;

	private ChatMsgAdapter mAdapter;

	private String toUser;

	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_chat);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initView();
		initData();
	}

	public void initView() {
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setTag(1);
		mBtnSend.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		DiscardClientHandler.getInstance().setOnMessageListener(this);
		DiscardClientHandler.getInstance().setOnMessageSendState(this);
	}

	public void initData() {
		Intent intent = getIntent();
		toUser = intent.getExtras().getString("name");
		Info info = (Info) intent.getExtras().getSerializable("info");
		ArrayList<MessageOfPerson> friendTalkContent = info.getFriendTalkContent();
		for (int i = 0; i < friendTalkContent.size(); i++) {
			MessageOfPerson mop = friendTalkContent.get(i);
			if (toUser.equals(mop.getToUser())) {
				ArrayList<MessageContent> messageContents = mop.getMessageContents();
				for (int j = 0; j < messageContents.size(); j++) {
					ChatMsgEntity entity = new ChatMsgEntity();
					MessageContent messageContent=messageContents.get(j);
					entity.setDate(messageContent.getTime());
					entity.setText(messageContent.getContent());
					entity.setSendState(2);
					
					if(messageContent.getFromUser().equals(toUser)){
						entity.setMsgType(true);
						entity.setName(toUser);
					}else{
						entity.setName(messageContent.getFromUser());
						entity.setMsgType(false);
					}
					mDataArrays.add(entity);
				}
			}
		}

		mAdapter = new ChatMsgAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			switch ((Integer) v.getTag()) {
			case 1:
				View view = findViewById(R.id.ll_facechoose);
				if (view.getVisibility() == View.VISIBLE) {
					view.setVisibility(View.GONE);
				} else {
					view.setVisibility(View.VISIBLE);
				}
				break;
			case 2:
				send();
				break;
			default:
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView()) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void send() {
		String contString = mEditTextContent.getText().toString();
		if (contString.length() > 0) {
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setDate(getDate());
			entity.setMsgType(false);
			entity.setText(contString);
			entity.setSendState(1);
			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();
			mEditTextContent.setText("");
			mListView.setSelection(mListView.getCount() - 1);

			// 将此信息发送给对方并保存给数据库
			Info info = new Info();
			info.setContent(contString);
			info.setToUser(toUser);
			info.setTime(DateUtil.getTime());
			info.setFromUser(ApplicationVar.getId());
			info.setInfoType(EnumInfoType.SEND_MES);
			RTSClient.writeAndFlush(info);
		}
	}

	private String getDate() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH) + 1);
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));
		if (Integer.parseInt(mins) >= 0 && Integer.parseInt(mins) <= 9)
			mins = "0" + mins;
		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins);

		return sbBuffer.toString();
	}

	@Override
	public void notifyStateChanged(Info info) {
		// TODO Auto-generated method stub
		ChatMsgEntity entity = new ChatMsgEntity();
		entity.setDate(getDate());
		entity.setMsgType(true);
		entity.setText(info.getContent());

		mDataArrays.add(entity);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getCount() - 1);
	}

	@Override
	public void sendMessageState(Info info) {
		// TODO Auto-generated method stub
		ChatMsgEntity entity = mDataArrays.get(mDataArrays.size()-1);
		if(info.isState()){
			entity.setSendState(2);
		}else{
			entity.setSendState(0);
		}
		mAdapter.notifyDataSetChanged();
	}

}
