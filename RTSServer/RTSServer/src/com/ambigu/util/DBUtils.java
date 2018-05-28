package com.ambigu.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ambigu.model.AuthNode;
import com.ambigu.model.Group;
import com.ambigu.model.Info;
import com.ambigu.model.MessageContent;
import com.ambigu.model.MessageOfPerson;
import com.ambigu.model.Model;
import com.ambigu.model.Point;
import com.ambigu.model.ShareMessage;
import com.ambigu.model.ShareMessageOfPerson;
import com.ambigu.model.User;
import com.google.gson.Gson;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import io.netty.channel.Channel;

public class DBUtils {
	// 做db的事情
	public DBUtils() {
		// TODO Auto-generated constructor stub
	}

	public static Info getSharingInfo(Connection conn, Info info) {
		Info reinfo = null;
		reinfo = new Info();
		ArrayList<ShareMessageOfPerson> shareMessageOfPersons = new ArrayList<ShareMessageOfPerson>();
		String script = "select sharinginfo.userid,sharinginfo.friendid,sharinginfo.start_time,sharinginfo.end_time,"
				+ "sharinginfo.start_point,sharinginfo.end_point,sharinginfo.infoid from sharinginfo where sharinginfo.userid ='"
				+ info.getFromUser() + "' or sharinginfo.friendid = '" + info.getFromUser() + "'";
		PreparedStatement ptmt;
		try {
			ShareMessageOfPerson shareMessageOfPerson = null;// 每个人的共享信息
			ArrayList<ShareMessage> shareMessages = null;// 每个人的共享信息
			ArrayList<Point> latlngList = null;
			ShareMessage shareMessage = null;
			ptmt = conn.prepareStatement(script);
			ResultSet rs = ptmt.executeQuery();
			String oldToUser = "";
			while (rs.next()) {// 每一条共享记录
				if (!oldToUser.equals(rs.getString(1)) && !oldToUser.equals(rs.getString(2))) {// 创建新的Person
					shareMessageOfPerson = new ShareMessageOfPerson();
					if (info.getFromUser().equals(rs.getString(1))) {
						shareMessageOfPerson.setFromUser(rs.getString(1));
						shareMessageOfPerson.setToUser(rs.getString(2));
						shareMessageOfPerson.setFromMe(true);
					} else {
						shareMessageOfPerson.setFromUser(rs.getString(2));
						shareMessageOfPerson.setToUser(rs.getString(1));
						shareMessageOfPerson.setFromMe(false);
					}
					shareMessages = new ArrayList<ShareMessage>();
					shareMessageOfPerson.setShareMessages(shareMessages);
					shareMessageOfPersons.add(shareMessageOfPerson);
				}
				shareMessage = new ShareMessage();

				// 更新shareMessage
				shareMessage.setFromUser(rs.getString(1));
				shareMessage.setToUser(rs.getString(2));
				shareMessage.setStart_time(rs.getString(3));
				shareMessage.setEnd_time(rs.getString(4));
				shareMessage.setStart_point(rs.getString(5));
				shareMessage.setEnd_point(rs.getString(6));

				latlngList = new ArrayList<Point>();
				String infoid = rs.getString(7);
				script = "select sharing_message.time,sharing_message.isend,sharing_message.lat,sharing_message.lng,"
						+ "sharing_message.distance,sharing_message.speed from sharing_message where infoid = '"
						+ infoid + "'";
				ptmt = conn.prepareStatement(script);
				ResultSet rss = ptmt.executeQuery();
				while (rss.next()) {
					Point point = new Point(rss.getString(3), rss.getString(4), rss.getString(1), rss.getString(6));
					point.setDistance(rss.getString(5));
					latlngList.add(point);
				}
				shareMessage.setLatlngList(latlngList);
				shareMessages.add(shareMessage);
				// 更新oldToUser
				if (info.getFromUser().equals(rs.getString(1))) {
					oldToUser = rs.getString(2);
				} else {
					oldToUser = rs.getString(1);
				}
			}
			reinfo.setState(true);
		} catch (Exception e) {
			// TODO: handle exception
			reinfo.setState(false);
		}
		reinfo.setShareMessageOfPersons(shareMessageOfPersons);
		return reinfo;
	}

	public static Info getSharingMessage(Connection conn, Info info) {
		Info reinfo = new Info();
		reinfo = getSharingInfo(conn, info);

		// 整理
		ShareMessageOfPerson shareMessageOfPerson = new ShareMessageOfPerson();
		ArrayList<ShareMessageOfPerson> shareMessageOfPersons = reinfo.getShareMessageOfPersons();
		System.out.println(shareMessageOfPersons.size());
		for (int i = 0; i < shareMessageOfPersons.size(); i++) {
			shareMessageOfPerson = shareMessageOfPersons.get(i);
			for (int j = i + 1; j < shareMessageOfPersons.size(); j++) {
				ShareMessageOfPerson smp = shareMessageOfPersons.get(j);
				if (smp.getFromUser().equals(shareMessageOfPerson.getFromUser())
						&& smp.getToUser().equals(shareMessageOfPerson.getToUser())) {
					ArrayList<ShareMessage> shareMessages = smp.getShareMessages();
					shareMessageOfPerson.getShareMessages().addAll(shareMessages);
					shareMessageOfPersons.remove(j);
					break;
				}
			}

		}

		// 排序
		for (int i = 0; i < shareMessageOfPersons.size(); i++) {
			Collections.sort(shareMessageOfPersons.get(i).getShareMessages(), new Comparator<ShareMessage>() {
				@Override
				public int compare(ShareMessage m1, ShareMessage m2) {
					return m1.getStart_time().compareTo(m2.getStart_time());
				}

			});
		}

		// 对每一点进行排序
		for (int i = 0; i < shareMessageOfPersons.size(); i++) {
			ArrayList<ShareMessage> shareMessages = shareMessageOfPersons.get(i).getShareMessages();
			for (int j = 0; j < shareMessages.size(); j++)
				Collections.sort(shareMessages.get(j).getLatlngList(), new Comparator<Point>() {
					@Override
					public int compare(Point p1, Point p2) {
						return p1.getTime().compareTo(p2.getTime());
					}

				});
		}

		reinfo.setToUser(info.getToUser());
		reinfo.setFromUser(info.getFromUser());
		reinfo.setInfoType(EnumInfoType.GET_SHARING_MES);
		return reinfo;
	}

	public static Info getFriendMessage(Connection conn, Info info) {
		Info reinfo = new Info();
		String script = "select user_friends.friendid,user_friends.groupid,userinfo.iconurl,user_friends.auth_allow from user_friends,userinfo where user_friends.friendid=userinfo.userid and userinfo.isOnLine='1' "
				+ "and user_friends.userid ='" + info.getFromUser() + "' order by groupid asc;";
		try {

			PreparedStatement ptmt = conn.prepareStatement(script);
			ResultSet rs = ptmt.executeQuery();
			Group group = null;
			HashMap<String, Group> groups = new HashMap<String, Group>();
			ArrayList<User> users = null;
			while (rs.next()) {
				String groupid = rs.getString(2);
				if (groups.get(groupid) == null) {// 在线没有改分组
					group = new Group();
					users = new ArrayList<User>();
					group.setGroupname(groupid);
					group.setItems(users);
					groups.put(groupid, group);
					User user = new User();
					user.setUserid(rs.getString(1));
					user.setOnLine(true);
					String path = rs.getString(3);
					String base64str = FileUtils.imageToBase64(path);
					user.setIcon(base64str);
					user.setChoose(rs.getBoolean(4));
					users.add(user);
				} else {
					users = groups.get(groupid).getItems();
					User user = new User();
					user.setUserid(rs.getString(1));
					user.setOnLine(true);
					String path = rs.getString(3);
					String base64str = FileUtils.imageToBase64(path);
					user.setIcon(base64str);
					user.setChoose(rs.getBoolean(4));
					users.add(user);
				}
			}
			// 先查询不在线人数
			script = "select user_friends.friendid,user_friends.groupid,userinfo.iconurl,user_friends.auth_allow from user_friends,userinfo where user_friends.friendid=userinfo.userid and userinfo.isOnLine='0' "
					+ "and user_friends.userid ='" + info.getFromUser() + "' order by groupid asc;";
			ptmt = conn.prepareStatement(script);
			rs = ptmt.executeQuery();
			while (rs.next()) {
				String groupid = rs.getString(2);
				if (groups.get(groupid) == null) {// 在线没有改分组
					// 设置group的check
					if (group != null) {
						ArrayList<User> users2 = group.getItems();
						boolean ff = false;
						for (User user : users2) {
							if (!user.isChoose()) {
								ff = false;
								break;
							}
							ff = true;
						}
						group.setChoose(ff);
					}
					group = new Group();
					users = new ArrayList<User>();

					group.setGroupname(groupid);
					group.setItems(users);
					groups.put(groupid, group);
					User user = new User();
					user.setUserid(rs.getString(1));
					String path = rs.getString(3);
					String base64str = FileUtils.imageToBase64(path);
					user.setIcon(base64str);
					user.setOnLine(false);
					user.setChoose(rs.getBoolean(4));
					users.add(user);
				} else {
					users = groups.get(groupid).getItems();
					User user = new User();
					user.setUserid(rs.getString(1));
					user.setOnLine(false);
					String path = rs.getString(3);
					String base64str = FileUtils.imageToBase64(path);
					user.setIcon(base64str);
					user.setChoose(rs.getBoolean(4));
					users.add(user);
				}
			}

			// 得到talk信息
			script = "select userinfo.userid,friendid,message,time from mesinfo,message,userinfo where mesinfo.mesid =message.mesid and userinfo.userid=mesinfo.userid and mesinfo.userid ='"
					+ info.getFromUser() + "'";
			ptmt = conn.prepareStatement(script);
			rs = ptmt.executeQuery();
			ArrayList<MessageOfPerson> friendTalkContent = new ArrayList<MessageOfPerson>();
			MessageOfPerson messageOfPerson = null;
			MessageContent messageContent = null;
			ArrayList<MessageContent> messageContents = null;
			String oldname = "";
			try {
				while (rs.next()) {
					String name = rs.getString(2);
					if (!name.equals(oldname)) {
						messageOfPerson = new MessageOfPerson();
						messageContents = new ArrayList<MessageContent>();
						messageOfPerson.setMessageContents(messageContents);
						friendTalkContent.add(messageOfPerson);
						messageOfPerson.setFromUser(rs.getString(1));
						messageOfPerson.setToUser(name);
						oldname = name;
					}
					messageContent = new MessageContent();
					messageContent.setContent(rs.getString(3));
					messageContent.setTime(rs.getString(4));
					messageContent.setFromUser(rs.getString(1));
					messageContent.setToUser(name);
					messageContents.add(messageContent);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			for (int i = 0; i < friendTalkContent.size(); i++) {
				MessageOfPerson messageOfPerson2 = friendTalkContent.get(i);
				script = "select message,time from mesinfo,message where mesinfo.mesid =message.mesid and mesinfo.userid ='"
						+ messageOfPerson2.getToUser() + "' and mesinfo.friendid = '" + messageOfPerson2.getFromUser()
						+ "'";
				messageContents = messageOfPerson2.getMessageContents();
				ptmt = conn.prepareStatement(script);
				rs = ptmt.executeQuery();
				while (rs.next()) {
					messageContent = new MessageContent();
					messageContent.setContent(rs.getString(1));
					messageContent.setTime(rs.getString(2));
					messageContent.setFromUser(messageOfPerson2.getToUser());
					messageContent.setToUser(messageOfPerson2.getFromUser());
					messageContents.add(messageContent);
				}
			}

			// 将每个人的信息按时间排序 时间精确到毫秒
			for (int i = 0; i < friendTalkContent.size(); i++) {
				Collections.sort(friendTalkContent.get(i).getMessageContents(), new Comparator<MessageContent>() {

					@Override
					public int compare(MessageContent m1, MessageContent m2) {
						return m1.getTime().compareTo(m2.getTime());
					}

				});

			}

			// 设置好友信息
			reinfo.setFriendsList(groups);
			reinfo.setToUser(info.getFromUser());
			reinfo.setFriendTalkContent(friendTalkContent);
			reinfo.setState(true);
			reinfo.setInfoType(EnumInfoType.GET_FRIEND_AND_MSG);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			reinfo.setState(false);
		}
		return reinfo;
	}

	public static Info changeAuth(Connection conn, Info info) {
		Info reinfo = new Info();
		HashMap<String, Group> friendMap = info.getFriendsList();
		String myid = info.getFromUser();
		reinfo.setInfoType(EnumInfoType.CHANGE_AUTH);
		reinfo.setToUser(info.getFromUser());

		// 是否全部清除
		if (friendMap == null)// 全部清除
		{
			String script = "update user_friends set auth_allow='" + 0 + "' where userid='" + myid + "'";
			int f1;
			try {
				PreparedStatement ptmt = conn.prepareStatement(script);
				f1 = ptmt.executeUpdate();
				if (f1 > 0) {
					reinfo.setState(true);
				} else {
					reinfo.setState(false);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				reinfo.setState(false);
			}
			reinfo.setFriendsList(null);
			return reinfo;
		}
		boolean f = false;
		Iterator<Map.Entry<String, Group>> iterator = friendMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Group> entry = iterator.next();
			Group group = (Group) entry.getValue();
			ArrayList<User> users = group.getItems();
			for (User user : users) {
				int i = 0;
				if (user.isChoose())
					i = 1;
				String script = "update user_friends set auth_allow='" + i + "' where userid='" + myid
						+ "' and friendid='" + user.getUserid() + "' and groupid='" + group.getGroupname() + "'";
				int f1;
				try {
					PreparedStatement ptmt = conn.prepareStatement(script);
					f1 = ptmt.executeUpdate();
					if (f1 > 0) {
						f = true;
					} else if (!f) {
						f = false;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (!f) {
						f = false;
					}
					continue;
				}
			}
		}
		reinfo.setState(f);
		reinfo.setFriendsList(new HashMap<String, Group>());
		return reinfo;
	}

	public static Info writeAuthLatLngToOthers(Info info, HashMap<String, Model> channels, Connection conn) {
		Info reinfo = new Info();
		System.out.println("here1");
		Iterator<Map.Entry<String, Model>> iterator = channels.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Model> entry = iterator.next();
			String userid = (String) entry.getKey();
			boolean isAuth = ((Model) entry.getValue()).isAuthLatlng();
			System.out.println(userid + " " + isAuth);
			if (isAuth) {// 对数据库查询，该人是否有权限查看自己信息
				String script = "select * from user_friends where auth_allow='" + 1 + "' and userid='"
						+ info.getFromUser() + "' and friendid='" + userid + "'";
				try {
					PreparedStatement ptmt = conn.prepareStatement(script);
					ResultSet rs = ptmt.executeQuery();
					if (rs.next()) {// 允许共享共享
						info.setState(true);
						Channel channel = ((Model) entry.getValue()).getChannel();
						Gson gson = new Gson();
						System.out.println("here");
						channel.writeAndFlush(gson.toJson(info) + "\t");
					}

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		return reinfo;
	}

	// 得到权限共享经纬度
	public static Info GetAuthLatlng(Info info, Connection conn) {
		Info reinInfo = new Info();
		// 先查看权限是否拥有
		String script = "select * from user_friends where auth_allow='" + 1 + "' and userid='" + info.getToUser()
				+ "' and friendid='" + info.getFromUser() + "'";
		try {
			PreparedStatement ptmt = conn.prepareStatement(script);
			ResultSet rs = ptmt.executeQuery();
			if (rs.next()) {// 允许共享
				script = "select * from authlocation where userid='" + info.getToUser() + "'";
				Statement st = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rss = st.executeQuery(script);
				if (rss.last()) {// 获取最后一条信息
					String time = rss.getString(2);// 时间
					reinInfo.setTime(time);
					reinInfo.setFromUser(info.getToUser());
					reinInfo.setToUser(info.getFromUser());
					reinInfo.setLat(Double.parseDouble(rss.getString(3)));
					reinInfo.setLng(Double.parseDouble(rss.getString(4)));
					reinInfo.setSpeed(Double.parseDouble(rss.getString(5)));
					reinInfo.setCity_now(rss.getString(6));
					reinInfo.setDistance(rss.getString(7));

					// 更新数据库，表示该人查看过信息
					script = "insert into auth_note(userid,friendid,start_time,end_time) values(?,?,?,?)";
					ptmt = conn.prepareStatement(script);
					ptmt.setString(1, info.getFromUser());
					ptmt.setString(2, info.getToUser());
					ptmt.setString(3, info.getTime());
					ptmt.setString(4, 0 + "");
					int f = ptmt.executeUpdate();
					if (f > 0) {
						reinInfo.setState(true);
					} else {
						reinInfo.setState(false);
					}

				} else {
					reinInfo.setState(false);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			reinInfo.setState(false);
		}
		reinInfo.setInfoType(EnumInfoType.GET_AUTH_LATLNG);
		return reinInfo;
	}

	public static Info closeAuthLatlng(Connection conn, Info info) {
		Info reinfo = new Info();
		String script = "update auth_note set end_time ='" + info.getTime() + "' where userid ='" + info.getFromUser()
				+ "' and friendid='" + info.getToUser() + "'" + "order by start_time desc limit 1";
		PreparedStatement ptmt;
		try {
			ptmt = conn.prepareStatement(script);
			int f = ptmt.executeUpdate();
			if (f > 0) {
				reinfo.setState(true);
			} else {
				reinfo.setState(false);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			reinfo.setState(false);
		}
		reinfo.setInfoType(EnumInfoType.CLOSE_AUTH_LATLNG);
		return reinfo;
	}

	public static Info authLatlngWrite(Info info, Connection conn) {
		Info reinfo = new Info();
		String script = "insert into authlocation(userid,time,lat,lng,accuracy,direction,speed,distance,city) values(?,?,?,?,?,?,?,?,?)";
		PreparedStatement ptmt;
		try {
			ptmt = conn.prepareStatement(script);
			ptmt.setString(1, info.getFromUser());
			ptmt.setString(2, info.getTime());
			ptmt.setString(3, info.getLat() + "");
			ptmt.setString(4, info.getLng() + "");
			ptmt.setString(5, info.getAccuracy() + "");
			ptmt.setString(6, info.getDirection() + "");
			ptmt.setString(7, info.getSpeed() + "");
			ptmt.setString(8, info.getDistance());
			ptmt.setString(9, info.getCity_now());
			ptmt.execute();
			reinfo.setFromUser(info.getFromUser());
			reinfo.setInfoType(EnumInfoType.AUTH_LATLNG);
			reinfo.setState(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			reinfo.setInfoType(EnumInfoType.AUTH_LATLNG);
			reinfo.setState(true);
		}

		return reinfo;
	}

	public static Info getSelfAuthLocation(Connection conn, Info info) {
		Info reinfo = new Info();
		String script = "select * from authlocation where userid ='" + info.getFromUser() + "'";
		try {
			PreparedStatement ptmt = conn.prepareStatement(script);
			ResultSet rs = ptmt.executeQuery();
			String oldTime = "";// 这里判断是同一天时会报异常

			// 用到的变量初始化
			ShareMessage shareMessage = null;
			ArrayList<Point> latlngList = null;
			ArrayList<ShareMessage> shareMessages = new ArrayList<ShareMessage>();
			ShareMessageOfPerson shareMessageOfPerson = new ShareMessageOfPerson();
			ArrayList<ShareMessageOfPerson> shareMessageOfPersons = new ArrayList<ShareMessageOfPerson>();
			shareMessageOfPerson.setShareMessages(shareMessages);
			shareMessageOfPerson.setFromUser(info.getFromUser());
			shareMessageOfPersons.add(shareMessageOfPerson);
			reinfo.setShareMessageOfPersons(shareMessageOfPersons);
			boolean isFirst = true;
			String end_point = null;
			while (rs.next()) {
				String time = rs.getString(2);
				if (!DateUtil.isOneInfo(oldTime, time, 20)) {// 不是同一次共享
					// 生成新的信息
					if (!isFirst) {
						shareMessage.setEnd_point(rs.getString(8));
						shareMessage.setEnd_time(rs.getString(2));
					}
					isFirst = false;
					shareMessage = new ShareMessage();
					shareMessages.add(shareMessage);
					shareMessage.setStart_point(rs.getString(8));
					shareMessage.setStart_time(rs.getString(2));
					latlngList = new ArrayList<Point>();
					shareMessage.setLatlngList(latlngList);
				}
				Point point = new Point(rs.getString(3), rs.getString(4), time, rs.getString(7));
				point.setDistance(rs.getString(9));
				latlngList.add(point);
				oldTime = time;
				end_point = rs.getString(8);
			}
			if (shareMessage != null) {
				shareMessage.setEnd_point(end_point);
				shareMessage.setEnd_time(oldTime);
			}
			reinfo.setState(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			reinfo.setState(false);
		}
		reinfo.setInfoType(EnumInfoType.GET_SELF_AUTH_LATLNG);
		reinfo.setToUser(info.getFromUser());
		Gson gson = new Gson();
		System.out.println(gson.toJson(reinfo));
		return reinfo;
	}

	public static Info getAuthInfo(Connection conn, Info info) {
		Info reinfo = new Info();
		String script = "select * from auth_note where friendid ='" + info.getFromUser() + "'";
		PreparedStatement ptmt;
		try {
			ptmt = conn.prepareStatement(script);
			ResultSet rs = ptmt.executeQuery();
			HashMap<String, ArrayList<AuthNode>> childNodes = new HashMap<String, ArrayList<AuthNode>>();
			ArrayList<AuthNode> authNodes = null;
			String oldname = "";
			while (rs.next()) {
				String name = rs.getString(1);
				System.out.println(name);
				if (!oldname.equals(name)) {
					authNodes = new ArrayList<AuthNode>();
					childNodes.put(name, authNodes);
				}
				AuthNode authNode = new AuthNode();
				authNode.setUserid(rs.getString(2));
				authNode.setFriendid(rs.getString(1));
				authNode.setStart_time(rs.getString(3));
				authNode.setEnd_time(rs.getString(4));
				if (authNodes != null)
					authNodes.add(authNode);
				else
					throw new Exception("authNodes为空");
				oldname = name;
			}
			reinfo.setAuthNodes(childNodes);
			reinfo.setFromUser(info.getFromUser());
			reinfo.setInfoType(EnumInfoType.GET_AUTH_NOTE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reinfo;
	}

	public static Info delNoteInfo(Connection conn, Info info) {
		Info reinfo = new Info();
		HashMap<String, ArrayList<AuthNode>> authNodes = info.getAuthNodes();// 删除列表中的所有信息
		String script = "";
		PreparedStatement ptmt = null;
		Iterator<Map.Entry<String, ArrayList<AuthNode>>> iterator = authNodes.entrySet().iterator();
		String friendid = info.getFromUser();
		try {
			conn.setAutoCommit(false);
			boolean ff = true;
			while (iterator.hasNext()) {
				Map.Entry<String, ArrayList<AuthNode>> entry = iterator.next();
				String userid = entry.getKey();
				ArrayList<AuthNode> nodes = entry.getValue();
				script = "delete from auth_note where userid = ? and friendid = ?"
						+ " and start_time = ? and end_time = ?";
				ptmt = conn.prepareStatement(script);
				for (int i = 0; i < nodes.size(); i++) {
					ptmt.setString(1, userid);
					ptmt.setString(2, friendid);
					ptmt.setString(3, nodes.get(i).getStart_time());
					ptmt.setString(4, nodes.get(i).getEnd_time());
					ptmt.addBatch();
				}
				ptmt.executeBatch();
				reinfo.setState(true);
				conn.commit();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			reinfo.setState(false);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 修改成自动提交
		}
		reinfo.setInfoType(EnumInfoType.DEL_AUTH_NOTE);
		reinfo.setToUser(info.getFromUser());
		return reinfo;
	}
	
	public static Info delFriend(Connection conn,Info info) {
		Info reinfo=new Info();
		boolean f=false;
		String script="delete from user_friends where userid = '"+info.getFromUser()+"' and friendid = '"+info.getToUser()+"'";
		try {
			PreparedStatement ptmt=conn.prepareStatement(script);
			ptmt.executeUpdate();
			f=true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			f=false;
		}
		if(f){
			f=deleteRealativeSharing(conn,info);
		}
		if(f){
			f=deleteRealativeMessage(conn,info);
		}
		if(f) reinfo.setState(true);
		else reinfo.setState(false);
		reinfo.setGroup(info.getGroup());
		reinfo.setInfoType(EnumInfoType.DEL_FRIEND);
		reinfo.setToUser(info.getFromUser());
		reinfo.setFromUser(info.getToUser());
		return reinfo;
	}
	
	public static Info delSharingMessage(Connection conn,Info info){
		Info reinfo=new Info();
		String userid="",friendid="";
		String script="";
		if(info.isFromMe()){
			userid=info.getFromUser();
			friendid=info.getToUser();
		}else{
			userid=info.getToUser();
			friendid=info.getFromUser();
		}
		String time=info.getTime();
		String infoid=userid+"$"+friendid+"$"+time.replace(" ", "$").replace("年", "$").replace(":", "$").replace("月", "$").replace("日", "$");
		//删除share_message
		script="delete from sharing_message where infoid = '"+infoid+"'";
		boolean f=true;
		try {
			PreparedStatement ptmt=conn.prepareStatement(script);
			ptmt.executeUpdate();
		} catch (SQLException e) {
			// TODO: handle exception
			f=false;
		}
		if(f){
			script="delete from sharinginfo where userid = '"+userid+"' and friendid = '"+friendid+"' and infoid = '"+infoid+"'";
			try {
				PreparedStatement ptmt=conn.prepareStatement(script);
				ptmt.executeUpdate();
			} catch (SQLException e) {
				// TODO: handle exception
				f=false;
			}
		}
		if(f) reinfo.setState(true);
		else reinfo.setState(false);
		reinfo.setGroupPos(info.getGroupPos());
		reinfo.setChildPos(info.getChildPos());
		reinfo.setFromUser(userid);
		reinfo.setInfoType(EnumInfoType.DEL_SHARING_MES);
		return reinfo;
	}
	
	private static boolean deleteRealativeSharing(Connection conn,Info info){
		boolean f=true;
		String script="delete from sharing_message where infoid like '"+info.getToUser()+"$"+info.getFromUser()+"%' or "
					+ "infoid like '"+info.getFromUser()+"$"+info.getToUser()+"%'";
		System.out.println(script);
		try {
			PreparedStatement ptmt=conn.prepareStatement(script);
			ptmt.executeUpdate();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			f=false;
		}
		if(f){
			script="delete from sharinginfo where infoid like '"+info.getToUser()+"$"+info.getFromUser()+"%' or "
					+ "infoid like '"+info.getFromUser()+"$"+info.getToUser()+"%'";
			System.out.println(script);
			try {
				PreparedStatement ptmt=conn.prepareStatement(script);
				ptmt.executeUpdate();
			} catch (SQLException e) {
				// TODO: handle exception
				e.printStackTrace();
				f=false;
			}
		}
		
		return f;
	}
	
	private static boolean deleteRealativeMessage(Connection conn,Info info){
		boolean f=true;
		String script="delete from message where mesid like '"+info.getToUser()+"_"+info.getFromUser()+"%' or "
				+ "mesid like '"+info.getFromUser()+"_"+info.getToUser()+"%'";
		System.out.println(script);
		try {
			PreparedStatement ptmt=conn.prepareStatement(script);
			ptmt.executeUpdate();
		} catch (SQLException e) {
			// TODO: handle exception
			f=false;
		}
		if(f){
			script="delete from mesinfo where mesid like '"+info.getToUser()+"_"+info.getFromUser()+"%' or "
					+ "mesid like '"+info.getFromUser()+"_"+info.getToUser()+"%'";
			System.out.println(script);
			try {
				PreparedStatement ptmt=conn.prepareStatement(script);
				ptmt.executeUpdate();
			} catch (SQLException e) {
				// TODO: handle exception
				f=false;
			}
		}
		
		return f;
	}
	
	public static Info delLocationMessage(Connection conn,Info info){
		Info reinfo=new Info();
		String userid = info.getFromUser();
		String script="";
		String time=info.getTime();
		String maxTime=DateUtil.oneInfoMaxDate(time, 20);
		System.out.println(maxTime);
		script="select * from authlocation where userid = '"+userid+"' order by time asc";
		try {
			PreparedStatement ptmt=conn.prepareStatement(script);
			ResultSet rs=ptmt.executeQuery();
			boolean f=false;
			while(rs.next()){
				String now=rs.getString(2);
				if(!f){
					if(!rs.getString(2).equals(time)){
						continue;
					}
					if(rs.getString(2).equals(time)){
						f=true;
						script="delete from authlocation where userid = '"+userid+"' and time = '"+now+"'";
						ptmt=conn.prepareStatement(script);
						ptmt.executeUpdate();
						continue;
					}
				}
				if(f){
					if(DateUtil.isOneInfo(time, now, 20)){
						script="delete from authlocation where userid = '"+userid+"' and time = '"+now+"'";
						ptmt=conn.prepareStatement(script);
						ptmt.executeUpdate();
					}else {
						break;
					}
				}
			}
			reinfo.setState(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			reinfo.setState(false);
		}
		reinfo.setFromUser(info.getFromUser());
		reinfo.setInfoType(EnumInfoType.DEL_LOCATION_MES);
		return reinfo;
	}

}
