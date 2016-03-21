package com.navinfo.navicommons.config;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-4-24
 * Time: 下午12:24
 * To change this template use File | Settings | File Templates.
 */
public class MavenConfigMap extends TreeMap implements Config {
	
	public static final String TRUE="TRUE";
	
	public static final String FALSE="FALSE";


    public MavenConfigMap(Map m) {
        if (m != null)
            putAll(m);
    }

    public MavenConfigMap() {

    }


    public String getValue(String key) {
        return (String) this.get(key);
    }

    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return defaultValue;
        else
            return value;
    }

    public Integer getIntValue(String key) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return null;
        else
            return Integer.valueOf(value);


    }

    public Integer getIntValue(String key, int defaultValue) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return defaultValue;
        else
            return Integer.valueOf(value);


    }

    public Boolean getBooleanValue(String key) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return false;
        else
            return value.toUpperCase().equals("TRUE");


    }

	public void put(String key, String value) {
		super.put(key, value);
		
	}


}
