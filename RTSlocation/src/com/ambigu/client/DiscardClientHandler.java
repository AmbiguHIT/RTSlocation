package com.ambigu.client;

import com.ambigu.listener.OnAcquireAuthLatLngListener;
import com.ambigu.listener.OnAddFriendListener;
import com.ambigu.listener.OnAuthChangedListener;
import com.ambigu.listener.OnGetSelfLocationListener;
import com.ambigu.listener.OnMessageListener;
import com.ambigu.listener.OnMessageSendState;
import com.ambigu.listener.OnReceiveSharingMessageListener;
import com.ambigu.listener.OnRegisterListener;
import com.ambigu.listener.OnSharingMessageListener;
import com.ambigu.listener.OnSharingResListener;
import com.ambigu.listener.onLoginResult;
import com.ambigu.model.Info;
import com.ambigu.rtslocation.MainActivity;
import com.google.gson.Gson;

import android.os.Message;
import android.util.Log;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DiscardClientHandler extends SimpleChannelInboundHandler<String> {

	private onLoginResult loginInterface;
	private OnRegisterListener onRegisterListener;
	private OnMessageListener onMessageListener;
	private OnAddFriendListener onAddFriendListener;
	private OnMessageSendState onMessageSendState;
	private OnSharingMessageListener onSharingMessageListener;
	private OnAuthChangedListener onAuthChangedListener;
	private OnAuthChangedListener clearAuthListener;
	private OnAcquireAuthLatLngListener onAcquireAuthLatLngListener;
	private OnReceiveSharingMessageListener onReceiveSharingMessageListener;
	private OnSharingResListener onSharingResListener;
	private OnGetSelfLocationListener onGetSelfLocationListener;

	private DiscardClientHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, String msg) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		Info reqInfo = gson.fromJson(msg, Info.class);
		switch (reqInfo.getInfoType()) {
		case REGESTER:
			onRegisterListener.callbackToRegister(reqInfo);
			break;
		case SEND_MES:
			onMessageListener.notifyStateChanged(reqInfo);
			break;
		case SHARING_REQ:
			Log.e("回复信息", msg);
			onReceiveSharingMessageListener.dealMessage(reqInfo);
			onSharingResListener.getSharingRes(reqInfo);// 这里是监听被共享方要求结束
			break;
		case SHARING_RES:
			onSharingResListener.getSharingRes(reqInfo);
			break;
		case UPDATE_IP:
			break;
		case GET_FRIEND_AND_MSG:
			Message _msg3 = new Message();
			_msg3.obj = reqInfo;
			MainActivity.initAdapter(_msg3);
			break;
		case ADD_FRIEND:
			onAddFriendListener.onAddFriendState(reqInfo);
			break;
		case LOGIN:
			loginInterface.callbackToLogin(reqInfo);
			break;
		case SEND_MES_RES:
			onMessageSendState.sendMessageState(reqInfo);
			break;
		case GET_SHARING_MES:
			onSharingMessageListener.onGetSharingMessage(reqInfo);
			break;
		case CHANGE_AUTH:
			if (reqInfo.getFriendsList() != null)
				onAuthChangedListener.notifyAuthChanged(reqInfo);
			else
				clearAuthListener.notifyAuthChanged(reqInfo);
			break;
		case GET_AUTH_LATLNG:
		case CLOSE_AUTH_LATLNG:
			Log.e("回复信息", msg);
			onAcquireAuthLatLngListener.OnAcquireAuthLatLng(reqInfo);
			break;
		case GET_SELF_AUTH_LATLNG:
			onGetSelfLocationListener.showSelfLocation(reqInfo);
			break;
		default:
			break;
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
		Log.e("连接", "连接失败");
	}

	public void setLoginInterface(onLoginResult loginInterface) {
		this.loginInterface = loginInterface;

	}

	public void setOnSharingResListener(OnSharingResListener onSharingResListener) {
		this.onSharingResListener = onSharingResListener;
	}

	public void setOnMessageSendState(OnMessageSendState onMessageSendState) {
		this.onMessageSendState = onMessageSendState;
	}

	public void setOnRegisterListener(OnRegisterListener onRegisterListener) {
		this.onRegisterListener = onRegisterListener;
	}

	public void setOnMessageListener(OnMessageListener onMessageListener) {
		this.onMessageListener = onMessageListener;
	}

	public void setOnAddFriendListener(OnAddFriendListener onAddFriendListener) {
		this.onAddFriendListener = onAddFriendListener;
	}

	public void setOnSharingMessageListener(OnSharingMessageListener onSharingMessageListener) {
		this.onSharingMessageListener = onSharingMessageListener;
	}

	public void setOnAuthChangedListener(OnAuthChangedListener onAuthChangedListener) {
		this.onAuthChangedListener = onAuthChangedListener;
	}

	public void setOnClearAuthListener(OnAuthChangedListener onAuthChangedListener) {
		this.clearAuthListener = onAuthChangedListener;
	}

	public void setOnAcquireAuthLatLngListener(OnAcquireAuthLatLngListener onAcquireAuthLatLngListener) {
		this.onAcquireAuthLatLngListener = onAcquireAuthLatLngListener;
	}

	public void setOnReceiveSharingMessageListener(OnReceiveSharingMessageListener onReceiveSharingMessageListener) {
		this.onReceiveSharingMessageListener = onReceiveSharingMessageListener;
	}

	public static DiscardClientHandler getInstance() {
		return SingleDiscardClientHandler.INSTANCE;
	}

	private static class SingleDiscardClientHandler {
		public static final DiscardClientHandler INSTANCE = new DiscardClientHandler();
	}

}