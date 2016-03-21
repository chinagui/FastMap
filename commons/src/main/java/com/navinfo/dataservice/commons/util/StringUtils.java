package com.navinfo.dataservice.commons.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 字符串帮助类
 */
public class StringUtils {

	/**
	 * 类的属性名转为数据库的列名 在大写字母前加下划线，并把大写转小写
	 * 
	 * @param fieldName
	 *            属性名
	 * @return 列名
	 */
	public static String toColumnName(String fieldName) {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < fieldName.length(); i++) {
			char c = fieldName.charAt(i);
			if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
				sb.append("_" + c);
			} else {
				sb.append(c);
			}
		}

		return sb.toString().toLowerCase();
	}

	/**
	 * 判断字符串是否相等
	 * 
	 * @param str1
	 *            字符串1
	 * @param str2
	 *            字符串2
	 * @return True 相等
	 */
	public static boolean isStringSame(String str1, String str2) {
		boolean flag = false;

		if (str1 != null && str2 != null && str1.equals(str2)) {
			flag = true;
		} else if (str1 == null && str2 == null) {
			flag = true;
		}

		return flag;
	}

	/**
	 * 获取当前时间，格式 "yyyyMMddHHmmss"
	 * 
	 * @return 时间字符串
	 */
	public static String getCurrentTime() {
		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		return sdf.format(date);
	}

	/**
	 * 获取当前天，格式 "yyyyMMdd"
	 * 
	 * @return 时间字符串
	 */
	public static String getCurrentDay() {
		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		return sdf.format(date);
	}
	
	public static void main(String[] args) {

		System.out.println(StringUtils.getCurrentTime());
	}
}
