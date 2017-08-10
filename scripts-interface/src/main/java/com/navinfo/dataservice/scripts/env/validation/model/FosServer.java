package com.navinfo.dataservice.scripts.env.validation.model;

import java.util.HashMap;
import java.util.Map;

/** 
 * @ClassName: TomcatServer
 * @author xiaoxiaowen4127
 * @date 2017年8月9日
 * @Description: TomcatServer.java
 */
public class FosServer{
	protected String serverDir;
	protected String name;
	protected String type;
	protected String dubboConfigFile;
	protected Map<String,String> dubboConfig = new HashMap<String,String>();
	protected String sysConfigFile;
	protected Map<String,String> sysConfig = new HashMap<String,String>();
	public FosServer(String name,String type){
		this.name=name;
		this.type=type;
	}
	public String getServerDir() {
		return serverDir;
	}
	public void setServerDir(String serverDir) {
		this.serverDir = serverDir;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDubboConfigFile() {
		return dubboConfigFile;
	}
	public void setDubboConfigFile(String dubboConfigFile) {
		this.dubboConfigFile = dubboConfigFile;
	}
	public Map<String, String> getDubboConfig() {
		return dubboConfig;
	}
	public void addDubboConfig(String key, String value) {
		this.dubboConfig.put(key, value);
	}
	public void addDubboConfigs(Map<String,String> keyvalues) {
		this.dubboConfig.putAll(keyvalues);
	}
	public String getSysConfigFile() {
		return sysConfigFile;
	}
	public void setSysConfigFile(String sysConfigFile) {
		this.sysConfigFile = sysConfigFile;
	}
	public Map<String, String> getSysConfig() {
		return sysConfig;
	}
	public void addSysConfig(String key, String value) {
		this.sysConfig.put(key, value);
	}
	public void addSysConfigs(Map<String,String> keyvalues) {
		this.sysConfig.putAll(keyvalues);
	}
}
