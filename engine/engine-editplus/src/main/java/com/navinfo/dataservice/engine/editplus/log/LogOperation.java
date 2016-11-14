package com.navinfo.dataservice.engine.editplus.log;

import java.util.List;

import com.navinfo.navicommons.database.sql.RunnableSQL;

/** 
 * @ClassName: LogOperation
 * @author xiaoxiaowen4127
 * @date 2016年11月9日
 * @Description: LogOperation.java
 */
public class LogOperation {
	
	protected List<LogDetail> details;
	
	/**
	 * 模型生成写入表的sql
	 * @return
	 */
	public List<RunnableSQL> generateSql(){
		
		return null;
	}
	
	/**
	 * 用履历生成刷库的sql
	 * @return
	 */
	public List<RunnableSQL> getFlushSql(){
		
		return null;
	}
}
