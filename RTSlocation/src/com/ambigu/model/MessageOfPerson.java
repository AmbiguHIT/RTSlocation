package com.ambigu.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.drawable.Icon;
import android.media.Image;

public class MessageOfPerson implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<MessageContent> messageContents;
	private String fromUser;
	private String toUser;
	private String toUserIcon;
	public ArrayList<MessageContent> getMessageContents() {
		return messageContents;
	}
	public void setMessageContents(ArrayList<MessageContent> messageContents) {
		this.messageContents = messageContents;
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
	public String getToUserIcon() {
		return toUserIcon;
	}
	public void setToUserIcon(String toUserIcon) {
		this.toUserIcon = toUserIcon;
	}

}
