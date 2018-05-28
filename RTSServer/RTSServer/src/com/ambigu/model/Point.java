package com.ambigu.model;

import java.io.Serializable;

public class Point implements Serializable{
	/**
 * 
 */
private static final long serialVersionUID = 1L;
	private String lat, lng,speed,distance,city,direction;
	private String time,accuracy;

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Point(String lat, String lng, String time,String speed) {
		// TODO Auto-generated constructor stub
		this.lat = lat;
		this.lng = lng;
		this.time = time;
		this.speed = speed;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(String accuracy) {
		this.accuracy = accuracy;
	}
}