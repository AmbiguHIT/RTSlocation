package com.ambigu.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ShareMessageOfPerson implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String toUser;
	private String fromUser;
	private boolean isFromMe=false;
	private ArrayList<ShareMessage> shareMessages;
	public String getToUser() {
		return toUser;
	}
	public void setToUser(String toUser) {
		this.toUser = toUser;
	}
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	public ArrayList<ShareMessage> getShareMessages() {
		return shareMessages;
	}
	public void setShareMessages(ArrayList<ShareMessage> shareMessages) {
		this.shareMessages = shareMessages;
	}
	public boolean isFromMe() {
		return isFromMe;
	}
	public void setFromMe(boolean isFromMe) {
		this.isFromMe = isFromMe;
	}
}
