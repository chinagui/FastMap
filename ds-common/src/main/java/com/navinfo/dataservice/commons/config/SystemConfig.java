package com.navinfo.dataservice.commons.config;

import com.navinfo.navicommons.config.MavenConfigMap;
import com.navinfo.navicommons.config.SystemXMLConfig;

import org.apache.log4j.Logger;

import java.util.TreeMap;


/**
 * 系统配置文件初始化
 * 
 * @author: liuqing Date: 11-5-6 Time: 下午2:46
 */
public class SystemConfig {
	private static Logger log = Logger.getLogger(SystemConfig.class);

	private static final String defaultConfigFile = "/com/navinfo/dataservice/commons/config/SystemConfig.xml";

	private static MavenConfigMap map = new MavenConfigMap();

	private static SystemConfig systemConfig = null;

	private SystemConfig() {
		appendConfigFile(defaultConfigFile);
		appendConfigFile("/SystemConfig.xml");
		
	}
	public synchronized static MavenConfigMap getSystemConfig() {
		if (systemConfig == null)
			systemConfig = new SystemConfig();

		return map;

	}
	public synchronized static SystemConfig getSystemConfigInstance(){
		
		if (systemConfig == null)
			systemConfig = new SystemConfig();

		return systemConfig;
	}
	/**
	 * 在系统配置文件的基础上，可以用其它任何配置文件进行覆盖
	 * 
	 * @param config
	 */
	public static void appendConfigFile(String config) {
		MavenConfigMap systemConfig = null;
		try {
			systemConfig = SystemXMLConfig.getSystemConfig(config);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		appendMavenConfigMap(systemConfig);
	}

	/**
	 * 可以使用其它配置map覆盖已有配置，此方法在用maven 插件方式运行程序时，可以使用此方法将pom.xml中的配置覆盖默认方法
	 * 
	 * @param configMap
	 */
	public static void appendMavenConfigMap(TreeMap configMap) {
		if (configMap != null) {
			map.putAll(configMap);
		}

	}
}
