package com.navinfo.dataservice.expcore.target;


import org.apache.log4j.Logger;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.commons.log.JobLogger;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-5-10 Time: 下午4:38
 * 数据导出的抽象方法，导出数据到不同的载体可以继承此方法
 */
@Deprecated
public abstract class AbstractExportTarget implements ExportTarget {

	protected Logger log = Logger.getLogger(getClass());

	public AbstractExportTarget() {
		log = JobLogger.getLogger(log);
	}
	
	public void init(String gdbVerison)throws ExportException{
	}
	public abstract void release(boolean destroyTarget);
}
