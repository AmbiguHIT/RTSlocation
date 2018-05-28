package com.ambigu.model;

import java.io.Serializable;

/**
 * 闁烩偓鍔嶉崺娑樜熼垾宕囷拷锟�
 * @author ganhang
 * 
 */
public class MessageContent implements Serializable{//濞戞挸瀛╁鍌炴⒒閺夋埈鍤犻幖瀛樻⒒濞堟垵鈽夐崼鐔剁礀
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String content;//闁告凹鍋呭﹢浣烘偘閵婏箑鍓伴柡鍐啇缁辨繈寮介悡搴ｇ濞戞搫鎷�"闁瑰瓨鍨甸妶绲掔�殿噯鎷烽煫鍥у劑闁哥噦鎷�"
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
