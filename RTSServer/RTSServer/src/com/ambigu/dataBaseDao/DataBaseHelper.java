package com.ambigu.dataBaseDao;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ambigu.model.Info;
import com.ambigu.util.DBUtils;
import com.ambigu.util.EnumInfoType;
import com.ambigu.util.FileUtils;
import com.google.gson.Gson;
import com.mysql.jdbc.Connection;

public class DataBaseHelper {

	private ResultSet rs = null;

	public DataBaseHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Connection connection() {
		// 登记JDBC驱动程序
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			System.out.print("Class Not Found Exception");
		}

		// 链接URL
		String url = "jdbc:mysql://localhost:3306/rtsdatabase";
		Connection conn = null;

		try {
			conn = (Connection) DriverManager.getConnection(url, "root", "root");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	

	public Info DealDBHelper(Info info) {
		String str = null;
		String script = "";
		Info reinfo = null;
		PreparedStatement ptmt = null;
		try {
			Connection conn = connection();

			reinfo =new Info();
			switch (info.getInfoType()) {
			case LOGIN:// 请求登录
				script = "select * from userinfo where userid='" + info.getFromUser() + "' and password= '"
						+ info.getPwd() + "' and isOnLine = 0";
				ptmt = conn.prepareStatement(script);
				rs = ptmt.executeQuery(script);
				reinfo.setInfoType(EnumInfoType.LOGIN);
				reinfo.setToUser(info.getFromUser());
				try {
					if (rs.next()) {
						String iconurl=rs.getString(10);
						String base64Img=FileUtils.imageToBase64(iconurl);
						reinfo.setBirthday(rs.getString(5));
						reinfo.setAge(rs.getString(6));
						reinfo.setSex(rs.getString(7));
						reinfo.setEmail(rs.getString(8));
						reinfo.setAdress(rs.getString(9));
						reinfo.setIcon(base64Img);
						
						script="update userinfo set isOnLine = 1 where userid = '"+info.getFromUser()+"'";
						ptmt = conn.prepareStatement(script);
						ptmt.executeUpdate();						
						reinfo.setState(true);
					} else {
						reinfo.setState(false);
					}
				} catch (Exception e) {
					reinfo.setState(false);
				}
				return reinfo;
			case LOGIN_OUT:
				script="update userinfo set isOnLine = 0 where userid = '"+info.getFromUser()+"'";
				ptmt = conn.prepareStatement(script);
				ptmt.executeUpdate();
				reinfo=new Info();
				reinfo.setState(true);
				reinfo.setFromUser(info.getFromUser());
				reinfo.setInfoType(EnumInfoType.LOGIN_OUT);
				break;
			case REGESTER:// 请求注册
				script = "insert into userinfo(userid,password,sex,birthday,ip,age,iconurl,isOnLine)" + "values(?,?,?,?,?,?,?,?)";
				ptmt = conn.prepareStatement(script);
				ptmt.setString(1, info.getFromUser());
				ptmt.setString(2, info.getPwd());
				ptmt.setString(3, info.getSex());
				ptmt.setString(4, info.getBirthday());
				ptmt.setString(5, info.getIp());
				ptmt.setString(6, info.getAge());
				ptmt.setInt(8, 0);
				//生成图片URL
				if(info.getIcon()!=null){
					String imageurl="c://RTServerIcon//"+info.getFromUser()+"//"+info.getFromUser()+".png";
					ptmt.setString(7, imageurl);//url存入数据库
					System.out.println(imageurl);
					//将图片保存到本地
					FileUtils.base64ToFile(info.getIcon(), imageurl);
				}else{
					ptmt.setString(8, null);//url存入数据库
				}
				
				int f = ptmt.executeUpdate(); // 参数准备后执行语句
				ptmt.close();
				reinfo.setInfoType(EnumInfoType.REGESTER);
				reinfo.setToUser(info.getFromUser());
				if (f != -1) {
					reinfo.setState(true);
				} else
					reinfo.setState(false);
				return reinfo;
			case ADD_FRIEND:// 添加好友 3#uid#fid
				script = "insert into user_friends(userid,friendid,groupid,auth_allow)" + "values(?,?,?,?)";
				ptmt = conn.prepareStatement(script);
				ptmt.setString(1, info.getFromUser());
				ptmt.setString(2, info.getToUser());
				ptmt.setString(3, info.getGroup());
				ptmt.setBoolean(4, false);
				try {
					f = ptmt.executeUpdate(); // 参数准备后执行语句

					if(f!=-1){
						reinfo.setState(true);
					}else{
						reinfo.setState(false);
					}
				} catch (Exception e) {
					// TODO: handle exception
					reinfo.setState(false);
				}
				ptmt.close();
				reinfo.setInfoType(EnumInfoType.ADD_FRIEND);
				reinfo.setFromUser(info.getFromUser());
				reinfo.setToUser(info.getToUser());
				reinfo.setGroup(info.getGroup());
				script = "select iconurl from userinfo where userid='" + info.getToUser() + "'";

				ptmt = conn.prepareStatement(script);
				rs = ptmt.executeQuery(script);
				if(rs.next()){
					String imageurl=rs.getString(1);
					System.out.println(imageurl);
					//将图片保存到本地
					String icon=FileUtils.imageToBase64(imageurl);
					reinfo.setIcon(icon);
				}else{
					reinfo.setIcon(null);
				}
				return reinfo;
			case SEND_MES:// 添加好友 3#uid#fid
				script = "select * from mesinfo where userid='" + info.getFromUser() + "' and friendid= '"
						+ info.getToUser() + "'";
				ptmt = conn.prepareStatement(script);
				rs = ptmt.executeQuery(script);
				if (!rs.next()) {
					script = "insert into mesinfo(userid,friendid,mesid)" + "values(?,?,?)";
					String mesid =info.getFromUser()+"_"+info.getToUser();
					ptmt = conn.prepareStatement(script);
					ptmt.setString(1, info.getFromUser());
					ptmt.setString(2, info.getToUser());
					ptmt.setString(3,mesid);
					f = ptmt.executeUpdate(); // 参数准备后执行语句
					reinfo.setFromUser(info.getFromUser());
					reinfo.setInfoType(EnumInfoType.SEND_MES_RES);
					if (f == -1) {
						reinfo.setState(false);
					} else{
						script = "insert into message(mesid,time,message)" + "values(?,?,?)";
						ptmt = conn.prepareStatement(script);
						ptmt.setString(1, mesid);
						ptmt.setString(2, info.getTime());
						ptmt.setString(3, info.getContent());
						f = ptmt.executeUpdate(); // 参数准备后执行语句
						if (f == -1) {
							reinfo.setState(false);
						}else{
							reinfo.setState(true);
						}
					}
				}else{
					String mesid=rs.getString(3);
					script = "insert into message(mesid,time,message)" + "values(?,?,?)";
					ptmt = conn.prepareStatement(script);
					ptmt.setString(1, mesid);
					ptmt.setString(2, info.getTime());
					ptmt.setString(3, info.getContent());
					f = ptmt.executeUpdate(); // 参数准备后执行语句
					if (f == -1) {
						reinfo.setState(false);
					}else{
						reinfo.setState(true);
					}
				}
				return reinfo;
			case DEL_FRIEND:// 删除好友 4#id#pwd
				return DBUtils.delFriend(conn, info);
			case UPDATE_IP:// 更新ip 5#id#ip
				script = "update userinfo set ip='" + info.getIp() + "' where userid='" + info.getFromUser() + "'";
				System.out.println(script);
				ptmt = conn.prepareStatement(script);
				int f1 = ptmt.executeUpdate();
				reinfo.setInfoType(EnumInfoType.UPDATE_IP);
				reinfo.setToUser(info.getFromUser());
				if (f1 > 0) {
					reinfo.setState(true);
				} else
					reinfo.setState(false);
				return reinfo;
			case SHARING_REQ:// 请求共享 6#id#fid#lat#lng

				break;

			case SHARING_RES:// 写入数据库
				// infoid生成规则
				// uid$fid$time time:2018.04.20 15:31:22--->2018$04$20$15$31$22
				Gson gson=new Gson();
				String string=gson.toJson(info);
				System.out.println(string);
				if(info.isfirst()){//首次共享//插入sharinginfo表
					System.out.println("sharing_res:uid$fid$time----------------------------------------------------------------------");
					
					String infoid=info.getToUser()+"$"+info.getFromUser()+"$"+info.getTime().replace(" ", "$").replace("年", "$").replace("月", "$").replace("日", "$").replace(":", "$");
					script = "insert into sharinginfo(userid,friendid,start_time,start_point,infoid) values(?,?,?,?,?)";
					ptmt = conn.prepareStatement(script);
					ptmt.setString(1, info.getToUser());
					ptmt.setString(2, info.getFromUser());
					ptmt.setString(3, info.getTime());
					ptmt.setString(4, info.getCity_now());
					ptmt.setString(5, infoid);
					ptmt.execute();
					//插入sharing_message表
					script = "insert into sharing_message(infoid,time,isend,lat,lng) values(?,?,?,?,?)";
					ptmt = conn.prepareStatement(script);
					ptmt.setString(1, infoid);
					ptmt.setString(2, info.getTime());
					ptmt.setBoolean(3, false);
					ptmt.setString(4, info.getLat()+"");
					ptmt.setString(5, info.getLng()+"");
					ptmt.execute();
					reinfo.setToUser(info.getToUser());
					reinfo.setFromUser(info.getFromUser());
					reinfo.setInfoType(EnumInfoType.SHARING_RES);
					reinfo.setState(true);
				}else if(info.isend()){
					script = "select * from sharinginfo where userid='" + info.getToUser() + "' and friendid= '"
							+ info.getFromUser() + "'order by start_time desc limit 1";
					ptmt = conn.prepareStatement(script);
					rs = ptmt.executeQuery(script);
					try {
						if (rs.next()) {
							String infoid=rs.getString(8);
							//插入sharing_message表
							script = "insert into sharing_message(infoid,time,isend,lat,lng) values(?,?,?,?,?)";
							ptmt = conn.prepareStatement(script);
							ptmt.setString(1, infoid);
							ptmt.setString(2, info.getTime());
							ptmt.setBoolean(3, true);
							ptmt.setString(4, info.getLat()+"");
							ptmt.setString(5, info.getLng()+"");
							ptmt.execute();
							
							//插入sharinginfo表
							script = "update sharinginfo set end_time = ? ,end_point = ? ,distance_now = ?  where  infoid = ?";
							ptmt = conn.prepareStatement(script);
							ptmt.setString(1, info.getTime());
							ptmt.setString(2, info.getCity_now());
							ptmt.setString(3, info.getDistance()+"km");
							ptmt.setString(4, infoid);
							ptmt.executeUpdate();
							
							reinfo.setToUser(info.getToUser());
							reinfo.setFromUser(info.getFromUser());
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setState(true);
							
						} else {
							reinfo.setToUser(info.getToUser());
							reinfo.setFromUser(info.getFromUser());
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setState(false);
						}
						return reinfo;
					} catch (Exception e) {
						e.printStackTrace();
						reinfo.setToUser(info.getToUser());
						reinfo.setFromUser(info.getFromUser());
						reinfo.setInfoType(EnumInfoType.SHARING_RES);
						reinfo.setState(false);
						return reinfo;
					} 
				}else{//中间过程
					script = "select * from sharinginfo where userid='" + info.getToUser() + "' and friendid= '"
							+ info.getFromUser() + "'order by start_time desc limit 1";
					ptmt = conn.prepareStatement(script);
					rs = ptmt.executeQuery(script);
					try {
						if (rs.next()) {
							String infoid=rs.getString(8);
							//插入sharing_message表
							script = "insert into sharing_message(infoid,time,isend,lat,lng) values(?,?,?,?,?)";
							ptmt = conn.prepareStatement(script);
							ptmt.setString(1, infoid);
							ptmt.setString(2, info.getTime());
							ptmt.setBoolean(3, false);
							ptmt.setString(4, info.getLat()+"");
							ptmt.setString(5, info.getLng()+"");
							ptmt.execute();
							reinfo.setToUser(info.getToUser());
							reinfo.setFromUser(info.getFromUser());
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setState(true);
						} else {
							reinfo.setToUser(info.getToUser());
							reinfo.setFromUser(info.getFromUser());
							reinfo.setInfoType(EnumInfoType.SHARING_RES);
							reinfo.setState(false);
						}
						return reinfo;
					} catch (Exception e) {
						reinfo.setToUser(info.getToUser());
						reinfo.setFromUser(info.getFromUser());
						reinfo.setInfoType(EnumInfoType.SHARING_RES);
						reinfo.setState(false);
						return reinfo;
					}
				}
				break;

			case GET_FRIEND_AND_MSG:
				// 先查询在线人数
				reinfo=DBUtils.getFriendMessage(conn, info);
				return reinfo;
			case GET_SHARING_MES:
				reinfo=DBUtils.getSharingMessage(conn,info);
				return reinfo;
			case CHANGE_AUTH:
				reinfo=DBUtils.changeAuth(conn,info);
				return reinfo;
			case AUTH_LATLNG:
				reinfo=DBUtils.authLatlngWrite(info,conn);
				return reinfo;
			case GET_AUTH_LATLNG:
				return DBUtils.GetAuthLatlng(info, conn);
			case CLOSE_AUTH_LATLNG:
				return DBUtils.closeAuthLatlng(conn, info);
			case GET_SELF_AUTH_LATLNG:
				return DBUtils.getSelfAuthLocation(conn,info);
			case GET_AUTH_NOTE:
				return DBUtils.getAuthInfo(conn, info);
			case DEL_AUTH_NOTE:
				return DBUtils.delNoteInfo(conn, info);
			case DEL_SHARING_MES:
				return DBUtils.delSharingMessage(conn,info);
			case DEL_LOCATION_MES:
				return DBUtils.delLocationMessage(conn, info);
			case MODIFY_INFO:
				return DBUtils.modifyInfo(conn, info);
			case RESET_PWD:
				return DBUtils.modifyPwd(conn, info);
			case MODIFY_ICON:
				return DBUtils.modifyIcon(conn, info);
			default:
				break;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return reinfo;
	}

}
