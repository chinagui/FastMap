package com.navinfo.dataservice.commons.config;

import java.util.HashMap;
import java.util.Observer;

/** 
 * 注意：非线程安全
* @ClassName: KeyValueConfig 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午5:23:37 
* @Description: TODO
*/
public class KeyValueConfig implements SystemConfig {

	private HashMap<String,String> maps = new HashMap<String,String>();

	public String getValue(String key){
		return maps.get(key);
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

	public String put(String key,String value){
		return maps.put(key, value);
	}
	
	public void addObserver(Observer o){
		//do nothing.
	}
	public void deleteObserver(Observer o){
		//do nothing.
	}
	
	public static void main(String[] args){
		KeyValueConfig config = new KeyValueConfig();
	}

}
