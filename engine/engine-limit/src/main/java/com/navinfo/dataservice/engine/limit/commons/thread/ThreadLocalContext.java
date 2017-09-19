package com.navinfo.dataservice.engine.limit.commons.thread;

import  com.navinfo.dataservice.engine.limit.commons.database.oracle.ConnectionRegister;
import org.apache.log4j.Logger;

/**
 * 子线程无法获取主线程的ThreadLocal中的值，所以构造一个对象，用于向子线程传递ThreadLocal中保存的对象
 * 
 * @author LiuQing
 * 
 */
public class ThreadLocalContext {

	private Logger log;
	private String vmTaskId;

	private String mianThreadId = java.util.UUID.randomUUID().toString();

	public ThreadLocalContext(Logger log, String vmTaskId) {
		super();
		this.log = log;
		this.vmTaskId = vmTaskId;
	}

	public ThreadLocalContext(Logger log) {
		super();
		this.log = log;
		this.vmTaskId = ConnectionRegister.getVmTaskId();
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	public String getVmTaskId() {
		return vmTaskId;
	}

	public void setVmTaskId(String vmTaskId) {
		this.vmTaskId = vmTaskId;
	}

	public String getMianThreadId() {
		return mianThreadId;
	}

	public void setMianThreadId(String mianThreadId) {
		this.mianThreadId = mianThreadId;
	}

}
