package com.ambigu.util;

import com.ambigu.model.Info;
import com.google.gson.Gson;

public class InfoToJson {

	public static String infoToJson(Info info) {
		// TODO Auto-generated constructor stub
		Gson gson =new Gson();
		return gson.toJson(info);
	}

}
