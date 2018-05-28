package com.ambigu.model;

import java.io.Serializable;

public class AuthNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userid;
	private String friendid;
	private String start_time;
	private String end_time;
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getFriendid() {
		return friendid;
	}
	public void setFriendid(String friendid) {
		this.friendid = friendid;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	
}
