package com.ambigu.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String groupname;
	private boolean isChoose;
	private ArrayList<User> items;
	public String getGroupname() {
		return groupname;
	}
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}
	public ArrayList<User> getItems() {
		return items;
	}
	public void setItems(ArrayList<User> items) {
		this.items = items;
	}
	public boolean isChoose() {
		return isChoose;
	}
	public void setChoose(boolean isChoose) {
		this.isChoose = isChoose;
	}
}
