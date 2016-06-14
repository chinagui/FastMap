package com.navinfo.dataservice.commons.util;

import java.util.UUID;

/** 
* @ClassName: UuidUtils 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午2:45:52 
* @Description: TODO
*/
public class UuidUtils {
	/**
	 * 生成一个uuid,去除连字符，32位字符
	 * 
	 * @return
	 */
	public static String genUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
