package com.navinfo.dataservice.expcore.target;


import org.apache.log4j.Logger;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.commons.log.DSJobLogger;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-5-10 Time: 下午4:38
 * 数据导出的抽象方法，导出数据到不同的载体可以继承此方法
 */
public abstract class AbstractExportTarget implements ExportTarget {

	protected Logger log = Logger.getLogger(getClass());
	private boolean newTarget;

	public AbstractExportTarget(boolean newTarget) {
		log = DSJobLogger.getLogger(log);
		this.newTarget=newTarget;
	}
	
	public boolean isNewTarget(){
		return newTarget;
	};
	public void init(String gdbVerison)throws ExportException{
		installGdbModel(gdbVerison);
	}
	public abstract void release(boolean destroyTarget);
	public abstract void installGdbModel(String gdbVersion)throws ExportException;
}
