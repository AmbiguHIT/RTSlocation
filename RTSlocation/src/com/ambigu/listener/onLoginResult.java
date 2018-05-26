package com.ambigu.listener;

import com.ambigu.model.Info;

public interface onLoginResult {
	//客户端获得信息进行回调
	void callbackToLogin(Info info);
}
