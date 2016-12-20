package com.navinfo.dataservice.commons.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串帮助类
 */
public class StringUtils {

	public static String PlaceHolder = "$$$";

	public static boolean toBoolean(String flag) {
		boolean view = false;
		if (org.apache.commons.lang.StringUtils.isNotBlank(flag)) {
			if (flag.toLowerCase().equals("true") || flag.equals("1")) {
				view = true;
			}
		}
		return view;
	}

	/**
	 * 类的属性名转为数据库的列名 在大写字母前加下划线，并把大写转小写
	 * 
	 * @param fieldName
	 *            属性名
	 * @return 列名
	 */
	public static String toColumnName(String fieldName) {
		if (fieldName.equals("fccPid")) {
			return "pid";
		}
		if (fieldName.equals("open24h")) {
			return "open_24h";
		}
		if (fieldName.equals("level")) {
			return "\"LEVEL\"";
		}
		if(fieldName.equals("phone400")){
			return "phone_400";
		}
		if(fieldName.equals("current")){
			return "\"CURRENT\"";
		}
		if(fieldName.equals("mode")){
			return "\"MODE\"";
		}
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

	/**
	 * 移除字符串中的空字符，包括空格、制表符、换行、回车换行。
	 * 
	 * @param src
	 * @return
	 */
	public static String removeBlankChar(String src) {
		if (src != null) {
			String dest = "";
			Pattern p = Pattern.compile("\\s*|\t|\r|\n|\r\n");
			Matcher m = p.matcher(src);
			dest = m.replaceAll("");
			return dest;
		}
		return null;
	}

	/**
	 * 替换source中的${}值
	 *
	 * @param source
	 * @param pro
	 * @return
	 */
	public static String expandVariables(String source, Map<String, String> pro, String pre, String post) {
		String result = "";
		if (source == null) {
			return null;
		}
		int fIndex = source.indexOf(pre);

		if (fIndex == -1) {
			return source;
		}

		StringBuffer sb = new StringBuffer(source);

		while (fIndex > -1) {
			int lIndex = sb.indexOf(post);

			int start = fIndex + pre.length();

			if (fIndex == 0) {
				String varName = sb.substring(start, start + lIndex - pre.length());
				String varValue = (String) pro.get(varName) == null ? "\"" + varName + " not fount\""
						: (String) pro.get(varName);
				sb.replace(fIndex, fIndex + lIndex + 1, varValue);
			} else {
				String varName = sb.substring(start, lIndex);
				String varValue = (String) pro.get(varName) == null ? "\"" + varName + " not fount\""
						: (String) pro.get(varName);
				sb.replace(fIndex, lIndex + 1, varValue);
				/*
				 * if(varName.equals("expTaskId")){
				 * System.out.println(varValue); }
				 */
			}

			fIndex = sb.indexOf(pre);
		}

		result = sb.toString();

		return result;
	}

	public static String expandVariables(String source, Map<String, String> pro) {
		return expandVariables(source, pro, "${", "}");
	}

	/**
	 * int list 转string
	 * @param integers 
	 * @return
	 */
	public static String getInteStr(List<Integer> integers) {
		if (integers != null && integers.size() > 0) {
			return integers.toString().replace("[", "").replace("]", "");
		}
		return "";

	}

	/**
	 * 将逗号分割的int字符串转为对应的list
	 * 
	 * @param str
	 *            需要转为list的字符串："1,2,3"
	 * @return
	 */
	public static List<Integer> getIntegerListByStr(String str) {
		if (StringUtils.isNotEmpty(str)) {
			List<Integer> list = new ArrayList<Integer>();
			for (String tmp : str.split(",")) {
				list.add(Integer.parseInt(tmp));
			}
			return list;
		}
		return null;
	}
	public static boolean isNumeric(String str){
	    for(int i=str.length();--i>=0;){
	       int chr=str.charAt(i);
	       if(chr<48 || chr>57)
	          return false;
	    }
	    return true;
	 }
	public static String laneSpeedValue2KM(String laneSpeedValue) {

		String[] values = laneSpeedValue.split("\\|");

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				sb.append("|");
			}

			sb.append(Integer.valueOf(values[i]) / 10);
		}

		return sb.toString();
	}

	public static String laneSpeedValue2M(String laneSpeedValue) {

		String[] values = laneSpeedValue.split("\\|");

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				sb.append("|");
			}

			sb.append(Integer.valueOf(values[i]) * 10);
		}

		return sb.toString();
	}

	public static boolean isNotEmpty(String str) {
		return org.apache.commons.lang.StringUtils.isNotEmpty(str);
	}

	public static boolean isEmpty(String str) {
		return org.apache.commons.lang.StringUtils.isEmpty(str);
	}
	
	public static String cutSpecLength(String str,int len){
		if(str==null||str.length()<=len){
			return str;
		}
		return str.substring(0, len)+"......";
	}

	public static void main(String[] args) {

		/*
		 * if (column.equals("open24h")) { column = "open_24h"; } else if
		 * (column.equals("level")) { column = "\"LEVEL\""; } else {
		 * 
		 * column = StringUtils.toColumnName(column); }
		 */

		System.out.println(toColumnName("level"));
	}
}
