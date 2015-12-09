package com.navinfo.navicommons.config;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-4-24
 * Time: 下午12:24
 * To change this template use File | Settings | File Templates.
 */
public class DefaultProperties implements Config {

    public String getValue(String key) {
        return SystemGlobals.getValue(key);
    }

    public String getValue(String key, String defaultValue) {

        return SystemGlobals.getValue(key, defaultValue);
    }

    public Integer getIntValue(String key) {
        return SystemGlobals.getIntValue(key);


    }

    public Integer getIntValue(String key, int defaultValue) {
        return SystemGlobals.getIntValue(key, defaultValue);


    }

    public Object get(Object key) {
       throw new UnsupportedOperationException();
    }

	public void put(String key, String value) {
		 throw new UnsupportedOperationException();
		
	}

	public Boolean getBooleanValue(String key) {
        String value = getValue(key);
        if (value == null || value.length() == 0)
            return false;
        else
            return value.toUpperCase().equals("TRUE");


    }
}
