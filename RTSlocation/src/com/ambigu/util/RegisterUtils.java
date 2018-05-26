package com.ambigu.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUtils {

	public static boolean validateEmailAddress(String email) {
		String regEx = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
				+ "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
				+ "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
				+ "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
		if(email==null) return true;//允许不输入邮箱
		Matcher matcherObj = Pattern.compile(regEx).matcher(email);

		if (matcherObj.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 *1.密码必须至少有6个字符。
     *2.密码只能包括字母和数字。
     *3.密码必须至少有2个数字。
	 * 
	 * */
	
	public static boolean validatePassword(String pwd){
	        if(pwd.length() < 6) {  
	            return false;  
	        } else {  
	            int numberCounter = 0;  
	            for(int i = 0; i < pwd.length(); i++) {  
	                char c = pwd.charAt(i);  
	                if(!Character.isLetterOrDigit(c)) {  
	                    return false;  
	                }  
	                if(Character.isDigit(c)) {  
	                    numberCounter++;  
	                }  
	            }  
	            if(numberCounter < 2) {  
	                return false;  
	            }  
	        }  
	        return true;  

	}
}