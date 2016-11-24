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
	protected String opId;
	protected long usId;
	protected String opCmd;
	
	protected List<LogDetail> details;
	
	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public long getUsId() {
		return usId;
	}

	public void setUsId(long usId) {
		this.usId = usId;
	}

	public String getOpCmd() {
		return opCmd;
	}

	public void setOpCmd(String opCmd) {
		this.opCmd = opCmd;
	}

	public List<LogDetail> getDetails() {
		return details;
	}

	public void setDetails(List<LogDetail> details) {
		this.details = details;
	}

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
