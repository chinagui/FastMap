package com.navinfo.dataservice.commons.token;

public class TokenExpiredException extends Exception {

	@Override
	public String getMessage() {
		
		return "身份验证失败，请重新登录";
	}

}
