package com.navinfo.dataservice.commons.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.commons.exception.ConfigParseException;


/**
 * 支持动态修改的配置文件类
 * 加入observer列表中的对象需要谨慎，一般随系统启动到关闭都存在的对象/线程才能加入，任务类内部/短线程一般不能加入
 * @author xiaoxiaowen4127
 *
 */
public class DynamicSystemConfig extends Observable implements SystemConfig {
	private static Logger log = Logger.getLogger(DynamicSystemConfig.class);
	private static class SingletonHolder{
		private static final DynamicSystemConfig INSTANCE = new DynamicSystemConfig();
	}
	public static final DynamicSystemConfig getInstance(){
		return SingletonHolder.INSTANCE;
	}

	private static final String defaultConfigFile = "/com/navinfo/dataservice/commons/config/SystemConfig.xml";
	private Map<String,String> dynamicConfigMap = new ConcurrentHashMap<String,String>();
	private DynamicSystemConfig(){
		loadRawConfig(defaultConfigFile);
		loadRawConfig("/SystemConfig.xml");
		loadDynamicConfig(null);
	}
	private void loadRawConfig(String configFile){
		//加载管理库的信息
		InputStream is = null;
        log.debug("parse file " + configFile);
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            if (is == null) {
                is = DynamicSystemConfig.class.getResourceAsStream(configFile);
            }

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String key = element.getName();
                String value = element.getTextTrim();
                dynamicConfigMap.put(key, value);
            }
        } catch (Exception e) {
        	log.error(e.getMessage());
            throw new ConfigParseException("读取文件" + configFile + "错误", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
        }
	}
	public void loadDynamicConfig(Set<String> keySet){
		//
	}
	public void reloadParaConfig(Set<String> keySet ){
		log.info("加载系统参数");
		for (String key : keySet) {
			log.info("key====="+key);
		}
		loadDynamicConfig(keySet);
		setChanged();
		notifyObservers(keySet);
	}
	public String put(String key,String value){
		return dynamicConfigMap.put(key, value);
	}
	public void putAll(Map<String,String> map){
		dynamicConfigMap.putAll(map);
	}
	
	public String getValue(String key){
		return dynamicConfigMap.get(key);
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
