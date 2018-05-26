package com.ambigu.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SharingHistoryOfPerson implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fromUser;
	private String toUser;
	private ArrayList<SingleSharingHistoryInfo> sharingHistoryInfos;
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
	public ArrayList<SingleSharingHistoryInfo> getSharingHistoryInfos() {
		return sharingHistoryInfos;
	}
	public void setSharingHistoryInfos(ArrayList<SingleSharingHistoryInfo> sharingHistoryInfos) {
		this.sharingHistoryInfos = sharingHistoryInfos;
	}
}
