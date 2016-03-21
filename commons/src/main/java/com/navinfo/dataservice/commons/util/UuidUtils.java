package com.navinfo.dataservice.commons.util;

import java.util.Date;
import java.util.UUID;

/**
 * uuid的帮助类
 */
public class UuidUtils {

	/**
	 * 生成一个uuid
	 * 
	 * @return
	 */
	public static String genUuid() {
		String s = UUID.randomUUID().toString().toUpperCase();

		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
				+ s.substring(19, 23) + s.substring(24);
	}



}
