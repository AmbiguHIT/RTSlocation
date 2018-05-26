package com.ambigu.client;

import javax.net.ssl.SSLException;

import com.ambigu.model.Info;
import com.google.gson.Gson;

import android.util.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class FileClient {
	static final String HOST = "10.240.44.29";
	static final int PORT = 8993;
	static Channel ch = null;
	static EventLoopGroup clientGroup = null;

	
	public static void init(){
		SslContext sslctx;
		try {
			sslctx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
			clientGroup = new NioEventLoopGroup();
			Bootstrap b = new Bootstrap();
			b.group(clientGroup).channel(NioSocketChannel.class).handler(new SecureClientInitializer(sslctx));
			ch = b.connect(HOST, PORT).sync().channel();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			Log.e("连接","连接失败");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("连接","连接失败");
		}
	}
	
	public static void writeAndFlush(Info info){
		if(ch==null) init();
		Gson gson=new Gson();
		Log.e("发送消息：",gson.toJson(info));
		String sendstr=gson.toJson(info)+"\t";//使用\t为消息的分隔符
		ch.writeAndFlush(sendstr);
	}
	
}
