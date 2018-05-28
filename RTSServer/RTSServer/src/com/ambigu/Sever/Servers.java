package com.ambigu.Sever;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class Servers {

	static final int PORT = 8992;

	public static void main(String[] args) {
		// SelfSignedCertificate是一个用于管理可信消息的工厂管理者
		SelfSignedCertificate ssc;
		System.out.println("hello");
		try {
			ssc = new SelfSignedCertificate();
			// 工厂；授权和发给私钥
			SslContext sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
			EventLoopGroup bossGroup = new NioEventLoopGroup(1);
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap b = new ServerBootstrap();// 服务引导程序，服务器端快速启动程序
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
						.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new SecureServerInitializer(sslCtx));

				b.bind(PORT).sync().channel().closeFuture().sync();
				// bind绑定端口，创建一个channnel
				// sync监听future 直到future消息送达，返回future
				// channel 当future和io消息产生联系时返回一个channel。
				// closefuture 当以上的消息接受完毕后重新获取future
				// 以上相当于一个循环？？？？

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}

		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}