package com.ambigu.util;

/**
 * SEND_INFO UPDATE_USER_LIST WELCOME
 * 
 * @author hgy
 *
 */
public enum EnumInfoType {
	LOGIN(0), REGESTER(1), SEND_MES(2), DEL_FRIEND(3), ADD_FRIEND(4), UPDATE_IP(5), SHARING_REQ(6), SHARING_RES(
			7), TIME_SEARCH(8), GET_FRIEND_AND_MSG(9), SEND_MES_RES(10), GET_SHARING_MES(11), CHANGE_AUTH(12), 
	AUTH_LATLNG(13),GET_AUTH_LATLNG(14),CLOSE_AUTH_LATLNG(15),GET_SELF_AUTH_LATLNG(16),GET_AUTH_NOTE(17),DEL_AUTH_NOTE(18){
				@Override
				public boolean isRest() {
					return true;
				}
			};

	private int value;

	private EnumInfoType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public boolean isRest() {
		return false;
	}
}