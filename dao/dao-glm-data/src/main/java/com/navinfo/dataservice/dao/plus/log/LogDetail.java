package com.navinfo.dataservice.dao.plus.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.navicommons.database.sql.RunnableSQL;

/** 
 * @ClassName: LogDetail
 * @author xiaoxiaowen4127
 * @date 2016年11月9日
 * @Description: LogDetail.java
 */
public class LogDetail {
	protected String rowId;
	protected String opId;
	protected String obNm;
	protected long obPid;
	protected String tbNm;
	protected String old;
	protected String New;
	protected String fdLst;
	protected OperationType opTp;
	protected String tbRowId;
	protected int desSta;
	protected Date desDt;

	protected List<LogDetailGrid> grids;
	
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public String getOpId() {
		return opId;
	}
	public void setOpId(String opId) {
		this.opId = opId;
	}
	public String getObNm() {
		return obNm;
	}
	public void setObNm(String obNm) {
		this.obNm = obNm;
	}
	public long getObPid() {
		return obPid;
	}
	public void setObPid(long obPid) {
		this.obPid = obPid;
	}
	public String getTbNm() {
		return tbNm;
	}
	public void setTbNm(String tbNm) {
		this.tbNm = tbNm;
	}
	public String getOld() {
		return old;
	}
	public void setOld(String old) {
		this.old = old;
	}
	public String getNew() {
		return New;
	}
	public void setNew(String new1) {
		New = new1;
	}
	public String getFdLst() {
		return fdLst;
	}
	public void setFdLst(String fdLst) {
		this.fdLst = fdLst;
	}
	public OperationType getOpTp() {
		return opTp;
	}
	public void setOpTp(OperationType operationType) {
		this.opTp = operationType;
	}
	public String getTbRowId() {
		return tbRowId;
	}
	public void setTbRowId(String tbRowId) {
		this.tbRowId = tbRowId;
	}
	public int getDesSta() {
		return desSta;
	}
	public void setDesSta(int desSta) {
		this.desSta = desSta;
	}
	public Date getDesDt() {
		return desDt;
	}
	public void setDesDt(Date desDt) {
		this.desDt = desDt;
	}
	public List<LogDetailGrid> getGrids() {
		return grids;
	}
	public void setGrids(List<LogDetailGrid> grids) {
		this.grids = grids;
	}

	public List<RunnableSQL> toSql(){
		List<RunnableSQL> sqlList = new ArrayList<RunnableSQL>();
		//log_detail
		RunnableSQL sql = new RunnableSQL();
		sql.setSql("INSERT INTO LOG_DETAIL (ROW_ID,OP_ID,OB_NM,OB_PID,TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID) VALUES (?,?,?,?,?,?,?,?,?,?)");
		List<Object> columnValues = new ArrayList<Object>();
		columnValues.add(rowId);
		columnValues.add(opId);
		columnValues.add(obNm);
		columnValues.add(obPid);
		columnValues.add(tbNm);
		columnValues.add(old);
		columnValues.add(New);
		columnValues.add(fdLst);
		columnValues.add(opTp);
		columnValues.add(tbRowId);
		sql.setArgs(columnValues);
		sqlList.add(sql);
		for(LogDetailGrid logDetailGrid:grids){
			sqlList.add(logDetailGrid.toSql());
		}
		return sqlList;
	}
}
