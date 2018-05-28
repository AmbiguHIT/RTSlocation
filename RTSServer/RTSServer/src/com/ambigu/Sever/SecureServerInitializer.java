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
public class SecureServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;

	public SecureServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// Add SSL handler first to encrypt and decrypt everything.
		// In this example, we use a bogus certificate in the server side
		// and accept any invalid certificates in the client side.
		// You will need something more complicated to identify both
		// and server in the real world.
		pipeline.addLast(sslCtx.newHandler(ch.alloc()));

		// On top of the SSL handler, add the text line codec.
        ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
        pipeline.addLast(new DelimiterBasedFrameDecoder(10240*1024,true, delimiter));//10M
		ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));

		ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));

		// and then business logic.
		pipeline.addLast(new SecureServerHandler());
	}
}
