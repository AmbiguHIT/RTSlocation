/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ambigu.Sever;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ambigu.dataBaseDao.DataBaseHelper;
import com.ambigu.model.Info;
import com.ambigu.model.Model;
import com.ambigu.model.ReqScheme;
import com.ambigu.util.DBUtils;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.InfoToJson;
import com.google.gson.Gson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;

/**
 * Handles a server-side channel.
 */
public class SecureServerHandler extends SimpleChannelInboundHandler<String> {

	static HashMap<String, Model> channels = new HashMap<String, Model>();

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		System.out.println("失去连接");
		channels.remove(ctx);
		ctx.close();
	}


    @Override
    public void channelActive(final ChannelHandlerContext ctx) {//客户端启动时调用该方法
    	
//    	System.out.println("channelactive!");

		System.out.println("here");
		System.out.println("已初始化，sessionID：" + ctx.pipeline().get(SslHandler.class).engine().getSession().getId());

    }
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {//每次发送消息时 调用该方法
        // Send the received message to all channels but the current one.
		Gson gson = new Gson();
		if (msg != null) {
			Info info = gson.fromJson(msg, Info.class);
			//System.out.println(InfoToJson.infoToJson(info));
			String userid=info.getFromUser();
			// 将通道放入hash表
			Model model;
			if(channels.get(userid)==null){
				model=new Model();
				model.setChannel(ctx.channel());
				channels.put(info.getFromUser(), model);
				
			}else{
				model=channels.get(userid);
				model.setChannel(ctx.channel());
			}
			Info reinfo = null;
			Iterator<Entry<String, Model>> iterator = channels.entrySet().iterator();
			switch (info.getInfoType()) {
			case LOGIN:
				reinfo=new DataBaseHelper().DealDBHelper(info);
				System.out.println(gson.toJson(reinfo));
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case REGESTER:
				reinfo=new DataBaseHelper().DealDBHelper(info);
				System.out.println(gson.toJson(reinfo));
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case SEND_MES:
				if (info.getToUser().equals("所有人")) {
					while (iterator.hasNext()) {
						Map.Entry<String, Model> entry = (Map.Entry<String, Model>) iterator.next();
						userid = (String) entry.getKey();
						if (!userid.equals(info.getFromUser())) {
							Channel channel = ((Model) entry.getValue()).getChannel();
							channel.writeAndFlush(info);
						}
					}
				} else {
					while (iterator.hasNext()) {
						Map.Entry<String, Model> entry = (Map.Entry<String, Model>) iterator.next();
						userid = (String) entry.getKey();
						if (userid.equals(info.getToUser())) {
							Channel channel = ((Model) entry.getValue()).getChannel();
							channel.writeAndFlush(InfoToJson.infoToJson(info)+"\t");
							reinfo = new DataBaseHelper().DealDBHelper(info);
							//ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");//回复发送方
						}
					}
				}
				break;
			case ADD_FRIEND:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				System.out.println(InfoToJson.infoToJson(reinfo));
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case SHARING_REQ:
				boolean f = true;
				while (iterator.hasNext()) {
					Map.Entry<String, Model> entry =  (Map.Entry<String, Model>) iterator.next();
					userid = (String) entry.getKey();
					//System.out.println(userid);
					if (userid.equals(info.getToUser())) {
						Channel channel = ((Model) entry.getValue()).getChannel();
						channel.writeAndFlush(InfoToJson.infoToJson(info)+"\t");
						System.out.println("已发送");
						f = false;
						break;
					}
				}
				if (f) {
					reinfo = new Info();
					reinfo.setToUser(info.getFromUser());
					reinfo.setState(false);
					reinfo.setReqScheme(ReqScheme.SHARE_PARTY);
					reinfo.setInfoType(EnumInfoType.SHARING_RES);
					reinfo.setfirst(info.isfirst());
					System.out.println("失败"+InfoToJson.infoToJson(reinfo));
					ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				}
				break;
			case SHARING_RES:
				while (iterator.hasNext()) {
					Map.Entry<String, Model> entry = (Map.Entry<String, Model>) iterator.next();
					userid = (String) entry.getKey();
					if (userid.equals(info.getToUser())) {
						Channel channel = ((Model) entry.getValue()).getChannel();
						channel.writeAndFlush(InfoToJson.infoToJson(info)+"\t");
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
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case GET_SHARING_MES:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case CHANGE_AUTH:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
			break;
			case AUTH_LATLNG:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				if(reinfo.isState()){
					info.setInfoType(EnumInfoType.GET_AUTH_LATLNG);//要转变消息类型才能正确接收
					DBUtils.writeAuthLatLngToOthers(info, channels, (new DataBaseHelper()).connection());
				}
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case GET_AUTH_LATLNG:
				//置标志位
				model.setAuthLatlng(info.isAuthLatlng());
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				System.out.println(InfoToJson.infoToJson(info));
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case CLOSE_AUTH_LATLNG:
				//置标志位
				model.setAuthLatlng(false);
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				System.out.println(InfoToJson.infoToJson(info));
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				break;
			case GET_SELF_AUTH_LATLNG:
			case GET_AUTH_NOTE:
			case DEL_FRIEND:
			case DEL_LOCATION_MES:
			case DEL_SHARING_MES:
			case DEL_AUTH_NOTE:
				reinfo = (new DataBaseHelper()).DealDBHelper(info);
				ctx.writeAndFlush(InfoToJson.infoToJson(reinfo)+"\t");
				System.out.println(InfoToJson.infoToJson(reinfo));
				break;
			default:
				break;
			}
		}
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println("断开");
		channels.remove(ctx);
		ctx.close();
		cause.printStackTrace();
    }
}
