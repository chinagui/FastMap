package com.navinfo.dataservice.commons.token;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

public class AccessTokenFactory {
	public static AccessToken generate(long userId){
		int expireSecond = SystemConfigFactory.getSystemConfig().getIntValue("token.expire.second",86400);
		return generate(userId,expireSecond);
	}
	public static AccessToken generate(long userId,int expireSecond){
		return null;
	}
	//FIXME 这里需要后续实现具体的解析逻辑
	public static AccessToken parse(String tokenString){
		long userId=2;
		long expireSecond=864000000L;
		return new AccessToken(userId,expireSecond,new TokenGenerateLock());
	}
	/**
	 * 验证通过，会刷新token最后一次活跃时间，否则爆出异常
	 * @param token
	 * @throws TokenExpiredException
	 */
	public static void validate(AccessToken token)throws TokenExpiredException{
		
	}
	/**
	 * 验证通过，会刷新token最后一次活跃时间，否则爆出异常
	 * @param token
	 * @throws TokenExpiredException
	 */
	public static AccessToken validate(String tokenString)throws TokenExpiredException{
		AccessToken token = parse(tokenString);
		validate(token);
		return token;
	}
}
