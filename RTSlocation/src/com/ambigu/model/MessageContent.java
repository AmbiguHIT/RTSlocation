package com.ambigu.model;

import java.io.Serializable;

/**
 * 閻€劍鍩涘Ο鈥崇��
 * @author ganhang
 * 
 */
public class MessageContent implements Serializable{//娑撳孩妞傞梻鏉戭嚠鎼存梻娈戝☉鍫熶紖
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String content;//閸氼偅婀佺悰銊﹀剰閺冭绱濋弽鐓庣础娑擄拷"閹存垵銈絒瀵拷韫囧儩閸燂拷"
	private String time;
	private String fromUser;
	private String toUser;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	public String getToUser() {
		return toUser;
	}
	public void setToUser(String toUser) {
		this.toUser = toUser;
	}
}
