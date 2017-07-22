package com.navinfo.dataservice.monitor.agent.job;

import java.io.Serializable;

/**
 * 任务配置实体类
 * @ClassName JobConfig
 * @author Han Shaoming
 * @date 2017年6月26日 上午11:53:24
 * @Description TODO
 */
public class JobConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name; //任务名称
	private boolean activity = true; //是否被激活
	private String className; //任务执行的类全名
	private String scanPeriod; //任务执行的定时表达式配置
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isActivity() {
		return activity;
	}
	public void setActivity(boolean activity) {
		this.activity = activity;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getScanPeriod() {
		return scanPeriod;
	}
	public void setScanPeriod(String scanPeriod) {
		this.scanPeriod = scanPeriod;
	}
	
	
	

}
