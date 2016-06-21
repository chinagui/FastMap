package com.navinfo.dataservice.commons.token;


/** 
* @ClassName: AccessToken
* @author Xiao Xiaowen 
* @date 2016年5月23日 下午18:21:51 
* @Description: TODO
*/
public class AccessToken {
	private long userId;
	private long timestamp;
	private String tokenString;
	AccessToken(long userId,long timestamp,String tokenString){
		this.userId=userId;
		this.timestamp=timestamp;
		this.tokenString=tokenString;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	public String getTokenString() {
		return tokenString;
	}
	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
}
