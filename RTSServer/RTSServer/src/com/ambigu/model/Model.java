package com.ambigu.model;

import io.netty.channel.Channel;

public class Model {

	private Channel channel;
	private boolean isAuthLatlng=false;
	
	
	public Channel getChannel() {
		return channel;
	}


	public void setChannel(Channel channel) {
		this.channel = channel;
	}


	public boolean isAuthLatlng() {
		return isAuthLatlng;
	}


	public void setAuthLatlng(boolean isAuthLatlng) {
		this.isAuthLatlng = isAuthLatlng;
	}



}
