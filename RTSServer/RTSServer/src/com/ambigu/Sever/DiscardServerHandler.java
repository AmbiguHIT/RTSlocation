package com.ambigu.Sever;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ambigu.dataBaseDao.DataBaseHelper;
import com.ambigu.model.Info;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.InfoToJson;
import com.google.gson.Gson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;

public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

	static HashMap<String, Channel> channels = new HashMap<String, Channel>();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		System.out.println("here");
		System.out.println("已初始化，sessionID：" + ctx.pipeline().get(SslHandler.class).engine().getSession().getId());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		System.out.println("失去连接");
		channels.remove(ctx);
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		super.channelRead(ctx, msg);
		Gson gson = new Gson();
		System.out.println((String) msg);
		if (msg != null) {
			Info info = gson.fromJson((String) msg, Info.class);
			// 将通道放入hash表
			channels.put(info.getFromUser(), ctx.channel());
			Info reinfo = null;
			Iterator<Entry<String, Channel>> iterator = channels.entrySet().iterator();
			switch (info.getInfoType()) {
			case SEND_MES:
				if (info.getToUser().equals("所有人")) {
					while (iterator.hasNext()) {
						Map.Entry<String, Channel> entry = (Map.Entry<String, Channel>) iterator.next();
						String userid = (String) entry.getKey();
						if (!userid.equals(info.getFromUser())) {
							Channel channel = (Channel) entry.getValue();
							channel.writeAndFlush(info);
						}
					}
				} else {
					while (iterator.hasNext()) {
						Map.Entry<String, Channel> entry = (Map.Entry<String, Channel>) iterator.next();
						String userid = (String) entry.getKey();
						if (userid.equals(info.getToUser())) {
							Channel channel = (Channel) entry.getValue();
							channel.writeAndFlush(info);
						}
					}
				}
				break;
			case DEL_FRIEND:
				// serFrame.DelUser(info.getFromUser());
				// serFrame.updateText("用户" + info.getFromUser() +
				// "下线了");

				break;
			case SHARING_REQ:
				boolean f = true;
				while (iterator.hasNext()) {
					Map.Entry<String, Channel> entry = (Map.Entry<String, Channel>) iterator.next();
					String userid = (String) entry.getKey();
					if (userid.equals(info.getToUser())) {
						Channel channel = (Channel) entry.getValue();
						channel.writeAndFlush(info);
						f = false;
					}
				}
				if (f) {
					reinfo = new Info();
					reinfo.setToUser(info.getFromUser());
					reinfo.setState(false);
					reinfo.setInfoType(EnumInfoType.SHARING_RES);
					ctx.writeAndFlush(reinfo);
				}
				break;
			case SHARING_RES:
				while (iterator.hasNext()) {
					Map.Entry<String, Channel> entry = (Map.Entry<String, Channel>) iterator.next();
					String userid = (String) entry.getKey();
					if (userid.equals(info.getToUser())) {
						Channel channel = (Channel) entry.getValue();
						channel.writeAndFlush(InfoToJson.infoToJson(reinfo));
						new DataBaseHelper().DealDBHelper(info);
					}
				}
				break;
			case UPDATE_IP:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case TIME_SEARCH:

				break;
			case GET_FRIEND_AND_MSG:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				System.out.println(gson.toJson(reinfo));
				System.out.println();
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
		ctx.flush();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelWritabilityChanged(ctx);
		System.out.println("状态发生变化");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		System.out.println("断开");
		channels.remove(ctx);
		ctx.close();
		cause.getCause().printStackTrace();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}

	public DiscardServerHandler() {
		// TODO Auto-generated constructor stub
	}

}
