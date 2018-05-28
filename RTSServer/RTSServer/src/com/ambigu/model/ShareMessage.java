package com.ambigu.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author hgy 每次当被共享方停止共享时，会捎带此消息给服务器，服务器会存储下来
 *         每次当共享方请求共享方停止共享时，共享方也需要在回复中捎带此消息给服务器
 */
public class ShareMessage implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// 与时间对应的消息

	

	private String message;
	private String toUser;
	private String fromUser;
	private String start_time;
	private String end_time;
	private String start_point;
	private String end_point;
	private ArrayList<Point> latlngList;

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
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

}
