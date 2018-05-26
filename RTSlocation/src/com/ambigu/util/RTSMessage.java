package com.ambigu.util;

public class RTSMessage {
	private String icon;
	private String title;
	private String msg;
	private String time;

	public RTSMessage() {

	}

	public RTSMessage(String title, String msg, String time) {
		this.title = title;
		this.msg = msg;
		this.time = time;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
