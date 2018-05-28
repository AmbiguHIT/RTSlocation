package com.ambigu.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.ambigu.util.EnumInfoType;

/**
 * ��Ϣ����
 * 
 * @author hgy
 *
 */
public class Info implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fromUser;// ��Ϣ������
	private String toUser;// ����˭
	private EnumInfoType infoType;// ��Ϣ����
	private boolean state;// ����״̬
	private boolean req_stop_share;// true---����ֹͣ����(��ʱinfotypeӦΪSHARING_REQ)
	private String content;// ��������
	private String ip;// ����IP
	private String time;// ϵͳʱ��
	private int drivingstate;// �г�״̬ 0---��ֹ��1--��ͣ��else--��ʻ״̬
	private ReqScheme reqScheme;
	
	//������Ϣ
	private String pwd;// ����
	private String sex;// �Ա�
	private String age;// ����
	private String birthday;// ����
	private String email;
	private String adress;
	private String icon;
	

	// �����б�
	private HashMap<String, Group> friendsList;// ȫ�������б�
	private ArrayList<MessageOfPerson> friendTalkContent;// ���������¼
	private String group;
	private boolean isAuthLatlng=false;

	// ������Ϣ
	private ArrayList<ShareMessageOfPerson> shareMessageOfPersons;// ���������¼
	private boolean isfirst;
	private boolean isend;
	private int groupPos;
	private int childPos;
	private boolean isFromMe=false;

	public int getGroupPos() {
		return groupPos;
	}

	public void setGroupPos(int groupPos) {
		this.groupPos = groupPos;
	}

	public int getChildPos() {
		return childPos;
	}

	public void setChildPos(int childPos) {
		this.childPos = childPos;
	}

	// ��ʻ��Ϣ
	private double speed;// �ٶ�
	private double lat;// ��γ��
	private double lng;// ��γ��
	private String city_now;// ��γ��
	private String distance;// ��γ��
	private float accuracy;
	private float direction;

	private DrivingScheme driving;
	
	//Ȩ�޹���
	private HashMap<String,ArrayList<AuthNode>> authNodes;

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public ArrayList<MessageOfPerson> getFriendTalkContent() {
		return friendTalkContent;
	}

	public void setFriendTalkContent(ArrayList<MessageOfPerson> friendTalkContent) {
		this.friendTalkContent = friendTalkContent;
	}

	public HashMap<String, Group> getFriendsList() {
		return friendsList;
	}

	public void setFriendsList(HashMap<String, Group> friendsList) {
		this.friendsList = friendsList;
	}

	public boolean isReq_stop_share() {
		return req_stop_share;
	}

	public void setReq_stop_share(boolean req_stop_share) {
		this.req_stop_share = req_stop_share;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
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

	public EnumInfoType getInfoType() {
		return infoType;
	}

	public void setInfoType(EnumInfoType infoType) {
		this.infoType = infoType;
	}

	public Info(String fromUser, String toUser, String sendTime, double lat, double lng, EnumInfoType infoType,
			boolean state) {
		super();
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.lat = lat;
		this.lng = lng;
		this.infoType = infoType;
		this.state = state;
	}

	@Override
	public String toString() {
		return "[{\"fromUser\":\"" + fromUser + "\", \"toUser\":\"" + toUser + "\", \"lat\":\"" + lat + "\", \"lat\":\""
				+ lng + "\", \"state\":\"" + state + "\", \"time\":\"" + time + "\", \"ip\":\"" + ip
				+ "\", \"infoType\":\"" + infoType + "\"}]";
	}

	public Info() {
		super();
		this.fromUser = "";
		this.toUser = "";
		this.lat = 0;
		this.lng = 0;
		this.infoType = null;
		this.state = false;
		this.req_stop_share = false;
		this.content = "";
		this.ip = "";
		this.drivingstate=2;
	}

	public int getDrivingstate() {
		return drivingstate;
	}

	public void setDrivingstate(int drivingstate) {
		this.drivingstate = drivingstate;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public boolean isfirst() {
		return isfirst;
	}

	public void setfirst(boolean isfirst) {
		this.isfirst = isfirst;
	}

	public boolean isend() {
		return isend;
	}

	public void setend(boolean isend) {
		this.isend = isend;
	}

	public String getCity_now() {
		return city_now;
	}

	public void setCity_now(String city_now) {
		this.city_now = city_now;
	}

	public ReqScheme getReqScheme() {
		return reqScheme;
	}

	public void setReqScheme(ReqScheme reqScheme) {
		this.reqScheme = reqScheme;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	public ArrayList<ShareMessageOfPerson> getShareMessageOfPersons() {
		return shareMessageOfPersons;
	}

	public void setShareMessageOfPersons(ArrayList<ShareMessageOfPerson> shareMessageOfPersons) {
		this.shareMessageOfPersons = shareMessageOfPersons;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public boolean isAuthLatlng() {
		return isAuthLatlng;
	}

	public void setAuthLatlng(boolean isAuthLatlng) {
		this.isAuthLatlng = isAuthLatlng;
	}

	public void setDrivingScheme(DrivingScheme driving) {
		// TODO Auto-generated method stub
		this.driving=driving;
	}
	
	public DrivingScheme getDrivingScheme() {
		// TODO Auto-generated method stub
		return driving;
	}

	public HashMap<String,ArrayList<AuthNode>> getAuthNodes() {
		return authNodes;
	}

	public void setAuthNodes(HashMap<String,ArrayList<AuthNode>> authNodes) {
		this.authNodes = authNodes;
	}

	public boolean isFromMe() {
		return isFromMe;
	}

	public void setFromMe(boolean isFromMe) {
		this.isFromMe = isFromMe;
	}
}
