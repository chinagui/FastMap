package com.navinfo.navicommons.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.navicommons.config.SystemGlobals;

/**
 * user:liuqing
 */
public abstract class StringUtils {


    public static boolean isNotEmpty(String[] values) {
        return values != null && values.length > 0;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && value.trim().length() > 0;
    }
    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() < 1;
    }

    public static boolean toBoolean(String flag) {
        boolean view = false;
        if (org.apache.commons.lang.StringUtils.isNotBlank(flag)) {
            if (flag.toLowerCase().equals("true") || flag.equals("1")) {
                view = true;
            }
        }
        return view;
    }

    public static int booleanToInt(boolean flag) {
        int viewContent = 0;
        if (flag) {
            viewContent = 1;
        }
        return viewContent;
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
                String varValue = (String) pro.get(varName) == null ? "\"" + varName + " not fount\"" : (String) pro.get(varName);
                sb.replace(fIndex, fIndex + lIndex + 1, varValue);
            } else {
                String varName = sb.substring(start, lIndex);
                String varValue = (String) pro.get(varName) == null ? "\"" + varName + " not fount\"" : (String) pro.get(varName);
                sb.replace(fIndex, lIndex + 1, varValue);
                /*if(varName.equals("expTaskId")){
                        System.out.println(varValue);
                    }*/
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
     * 根据在SystemGlobals.properties配置的名字，把字符串转为map，以分开隔开的key－value对应
     *
     * @param propertiesName
     * @return
     */
    public static Map<String, String> string2Map(String propertiesName) {
        Map<String, String> proMap = new HashMap<String, String>();

        String properties = SystemGlobals.getValue(propertiesName);
        String[] propertieStr = properties.split(";");
        for (int i = 0; i < propertieStr.length; i++) {
            String key = (propertieStr[i].split(":"))[0];
            String value = (propertieStr[i].split(":"))[1];
            proMap.put(key, value);
        }

        return proMap;
    }
    
    /**
     * 移除字符串中的空字符，包括空格、制表符、换行、回车换行。
     * @param src
     * @return
     */
    public static String removeBlankChar(String src){
    	if(src!=null){
    		String dest = "";
    		Pattern p = Pattern.compile("\\s*|\t|\r|\n|\r\n");
    		Matcher m = p.matcher(src);
    		dest = m.replaceAll("");
    		return dest;
    	}
    	return null;
    }
    
    /**
     * 将集合内所有元素通过连接符组成字符串
     * @param coll
     * @param connector
     * @return
     */
	public static String collection2String(Collection<String> coll,String connector){
		String resultStr="";
		for(String str:coll){
			resultStr+=connector+str;
		}
		if(resultStr.length()>0){
			return resultStr.substring(connector.length());
		}else{
			return null;
		}
	}
	public static String collection2String(Collection<String> collection,String connector,Collection<String> filters){
		String resultStr="";
		for(String str:collection){
			if(filters.contains(str))continue;
			resultStr+=connector+str;
		}
		if(resultStr.length()>0){
			return resultStr.substring(connector.length());
		}else{
			return null;
		}
	}
    
    public static void main(String[] args) {
        String test = "select s from [mesh] and [mesh]  s[ss]s [task] from [pk]";
        Map pro = new HashMap();
        pro.put("mesh", "73");
        pro.put("task", "74");
        pro.put("pk", "75");
        System.out.println(expandVariables(test, pro, "[", "]"));
    }

}
