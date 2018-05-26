package com.ambigu.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SingleSharingHistoryInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String icon;
	private String start_time;
	private String end_time;
	private String fromUser;
	private String toUser;
	private String start_point;
	private String end_point;
	private String distance;
	private ArrayList<Point> latlngList;
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
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
	public String getStart_point() {
		return start_point;
	}
	public void setStart_point(String start_point) {
		this.start_point = start_point;
	}
	public String getEnd_point() {
		return end_point;
	}
	public void setEnd_point(String end_point) {
		this.end_point = end_point;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
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
	public ArrayList<Point> getLatlngList() {
		return latlngList;
	}
	public void setLatlngList(ArrayList<Point> latlngList) {
		this.latlngList = latlngList;
	}
	
}
