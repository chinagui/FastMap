package com.navinfo.dataservice.commons.config;

import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


/**
 * 支持动态修改的配置文件类
 * 加入observer列表中的对象需要谨慎，一般随系统启动到关闭都存在的对象/线程才能加入，任务类内部/短线程一般不能加入
 * @author xiaoxiaowen4127
 *
 */
public class DynamicSystemConfig extends Observable  {
	private static Logger log = Logger.getLogger(DynamicSystemConfig.class);
	private static class SingletonHolder{
		private static final DynamicSystemConfig INSTANCE = new DynamicSystemConfig();
	}
	public static final DynamicSystemConfig getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private Map<String,String> dynamicConfigMap = new ConcurrentHashMap<String,String>();
	private DynamicSystemConfig(){
		loadParaConfig(null);
	}
	public void loadConfig(){
		loadParaConfig(null);
	}
	public void loadParaConfig(Set<String> keySet){
	}
	public void reloadParaConfig(Set<String> keySet ){
		log.info("加载系统参数");
		for (String key : keySet) {
			log.info("key====="+key);
		}
		loadParaConfig(keySet);
		setChanged();
		notifyObservers(keySet);
	}
	public String put(String key,String value){
		return dynamicConfigMap.put(key, value);
	}
	public void putAll(Map<String,String> map){
		dynamicConfigMap.putAll(map);
	}
	public String remove(String key){
		return dynamicConfigMap.remove(key);
	}
	public String getValue(String key){
		return dynamicConfigMap.get(key);
	}
	public Map<String,String> getAll(){
		return dynamicConfigMap;
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
    
    
    public static void main(String[] args) {
    	Map<String,String> dynamicConfigMap = new ConcurrentHashMap<String,String>();
    	dynamicConfigMap.put("kk", "");
    	
    	
	}
}
