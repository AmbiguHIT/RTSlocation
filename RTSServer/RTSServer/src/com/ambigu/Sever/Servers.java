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
		// SelfSignedCertificate��һ�����ڹ��������Ϣ�Ĺ���������
		SelfSignedCertificate ssc;
		System.out.println("hello");
		try {
			ssc = new SelfSignedCertificate();
			// ��������Ȩ�ͷ���˽Կ
			SslContext sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
			EventLoopGroup bossGroup = new NioEventLoopGroup(1);
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap b = new ServerBootstrap();// �����������򣬷������˿�����������
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
						.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new SecureServerInitializer(sslCtx));

				b.bind(PORT).sync().channel().closeFuture().sync();
				// bind�󶨶˿ڣ�����һ��channnel
				// sync����future ֱ��future��Ϣ�ʹ����future
				// channel ��future��io��Ϣ������ϵʱ����һ��channel��
				// closefuture �����ϵ���Ϣ������Ϻ����»�ȡfuture
				// �����൱��һ��ѭ����������

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