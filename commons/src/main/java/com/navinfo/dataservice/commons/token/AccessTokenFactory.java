package com.navinfo.dataservice.commons.token;

import java.util.Date;

import oracle.spatial.geometry.DataException;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.util.MD5Utils;

public class AccessTokenFactory {
	public static AccessToken generate(long userId) {
		int expireSecond = SystemConfigFactory.getSystemConfig().getIntValue(
				"token.expire.second", 172800);//default expire time:2 days
		return generate(userId, expireSecond);
	}

	public static AccessToken generate(long userId, int expireSecond) {

		String n = StringUtils.leftPad(AccessTokenUtil.d2n(userId), 8, "0");

		Date date = new Date();

		long timestamp = date.getTime() + expireSecond * 1000;

		String tn = StringUtils.leftPad(AccessTokenUtil.d2n(timestamp), 8, "0");

		String md5 = MD5Utils.md5(n + tn).toUpperCase();

		String tokenString = n+tn+md5;
				
		AccessToken token = new AccessToken(userId, timestamp, tokenString);

		return token;
	}

	private static AccessToken parse(String tokenString) {
		
		if (tokenString != null && tokenString.length() == 48) {

			long userId = AccessTokenUtil.n2d(tokenString.substring(0, 8));

			long expireSecond = AccessTokenUtil.n2d(tokenString
					.substring(8, 16));

			return new AccessToken(userId, expireSecond, tokenString);
		}

		return null;
	}

	/**
	 * 验证通过，会刷新token最后一次活跃时间，否则爆出异常
	 * 
	 * @param token
	 * @throws TokenExpiredException
	 */
	private static void validate(AccessToken token)throws TokenExpiredException{
		
		if(token == null){
			throw new TokenExpiredException();
		}
		
		String userId =  StringUtils.leftPad(AccessTokenUtil.d2n(token.getUserId()),8,"0");
		
		String timestamp =  StringUtils.leftPad(AccessTokenUtil.d2n(token.getTimestamp()),8,"0");
		
		String md5 = MD5Utils.md5(userId + timestamp).toUpperCase();
		
		if(!md5.equals(token.getTokenString().substring(16))){
			throw new TokenExpiredException();
		}
		
		Date date = new Date(token.getTimestamp());
		
		if(date.before(new Date())){
			throw new TokenExpiredException();
		}
	}

	/**
	 * 验证通过，会刷新token最后一次活跃时间，否则爆出异常
	 * 
	 * @param token
	 * @throws TokenExpiredException
	 */
	public static AccessToken validate(String tokenString)
			throws TokenExpiredException {
		AccessToken token = parse(tokenString);
		validate(token);
		return token;
	}
	public static void main(String[] args) {
		try{
			AccessToken token = generate(1573,3153600);//一年
			validate(token);
			System.out.println(token.getTokenString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
