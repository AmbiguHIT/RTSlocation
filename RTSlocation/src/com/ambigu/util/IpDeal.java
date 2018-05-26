package com.ambigu.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpDeal {
	public static boolean isIp(String ip) {
		if (ip.length() < 7 || ip.length() > 15)
			return false; // 如果长度不符合条件 返回false
		// 2 尝试按.符号进行拆分 拆分结果应该是4段
		String[] arr = ip.split("\\.");
		if (arr.length != 4)
			return false; // 如果拆分结果不是4个字串 返回false
		// 3 查看拆分到的每一个子字符串，应该都是纯数字
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < arr[i].length(); j++) {
				char temp = arr[i].charAt(j);
				if (!(temp > '0' && temp < '9'))
					return false; // 如果某个字符不是数字就返回false
			}
		}
		// 4 对拆分结果转成整数 判断 应该是0到255之间的整数
		for (int i = 0; i < 4; i++) {
			int temp = Integer.parseInt(arr[i]);
			if (temp < 0 || temp > 255)
				return false; // 如果某个数字不是0到255之间的数 就返回false
		}
		// 5 经过各种磨砺之后 挺过来了！！！返回true
		return true;
	}

	/**
	 * 获取ip地址
	 * 
	 * @return
	 */
	public static String getHostIP() {

		String hostIp = null;
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			InetAddress ia = null;
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					ia = ias.nextElement();
					if (ia instanceof Inet6Address) {
						continue;// skip ipv6
					}
					String ip = ia.getHostAddress();
					if (!"127.0.0.1".equals(ip)) {
						hostIp = ia.getHostAddress();
						break;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return hostIp;

	}
}
