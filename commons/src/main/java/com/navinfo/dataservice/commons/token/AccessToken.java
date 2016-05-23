package com.navinfo.dataservice.commons.token;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

/** 
* @ClassName: AccessToken
* @author Xiao Xiaowen 
* @date 2016年5月23日 下午18:21:51 
* @Description: TODO
*/
public class AccessToken {
	private long userId;
	private int expireSecond;
	private long lastestActiveSecond;
	private String tokenString;
	AccessToken(long userId,int expireSecond,TokenGenerateLock lock){
		this.userId=userId;
		this.expireSecond=expireSecond;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getExpireSecond() {
		return expireSecond;
	}
	public void setExpireSecond(int expireSecond) {
		this.expireSecond = expireSecond;
	}
	public long getLastestActiveSecond() {
		return lastestActiveSecond;
	}
	public void setLastestActiveSecond(long lastestActiveSecond) {
		this.lastestActiveSecond = lastestActiveSecond;
	}
	public String getTokenString() {
		return tokenString;
	}
	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
}
