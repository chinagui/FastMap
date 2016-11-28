package com.navinfo.dataservice.dao.plus.log;

import java.util.ArrayList;
import java.util.Date;
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
	protected int opSg;
	protected Date opDt;
	protected List<LogDetail> details;
	
	public Date getOpDt() {
		return opDt;
	}
	public void setOpDt(Date opDt) {
		this.opDt = opDt;
	}
	public int getOpSg() {
		return opSg;
	}
	public void setOpSg(int opSg) {
		this.opSg = opSg;
	}
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
		
		List<RunnableSQL> sqlList = new ArrayList<RunnableSQL>();
		//log_detail
		RunnableSQL sql = new RunnableSQL();
		sql.setSql("INSERT INTO LOG_OPERATION (OP_ID,US_ID,OP_CMD,OP_DT,OP_SG) VALUES (?,?,?,?,?)");
		List<Object> columnValues = new ArrayList<Object>();
		columnValues.add(opId);
		columnValues.add(usId);
		columnValues.add(opCmd);
		columnValues.add(opDt);
		columnValues.add(opSg);
		sql.setArgs(columnValues);
		sqlList.add(sql);
		for(LogDetail logDetail:details){
			sqlList.addAll(logDetail.toSql());
		}
		return sqlList;
	}
	
	/**
	 * 用履历生成刷库的sql
	 * @return
	 */
	public List<RunnableSQL> getFlushSql(){
		
		return null;
	}
}
