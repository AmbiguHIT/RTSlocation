/*
 * 主要负责客户端的初始化
 */
package com.ambigu.client;


import java.net.ConnectException;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class SecureClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public SecureClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch){
        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.
        try {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(sslCtx.newHandler(ch.alloc(), RTSClient.HOST, RTSClient.PORT));

            // On top of the SSL handler, add the text line codec.
            ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
            pipeline.addLast(new DelimiterBasedFrameDecoder(10240*1024,true, delimiter));//10M
    		ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));

    		ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));

    		// and then business logic.
    		pipeline.addLast(DiscardClientHandler.getInstance());
			
		}  catch (Exception e) {
			// TODO: handle exception
		}

    }
}
