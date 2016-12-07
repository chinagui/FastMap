package com.navinfo.dataservice.dao.log;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;

public class LogOperation {

	private String opId;

	private String opDt;

	private int comSta;

	private String comDt;

	private int lockSta;

	private String actId;

	private String opSeq;

	public String getOpSeq() {
		return opSeq;
	}

	public void setOpSeq(String opSeq) {
		this.opSeq = opSeq;
	}

	public String getActId() {
		return actId;
	}

	public void setActId(String actId) {
		this.actId = actId;
	}

	private List<LogDetail> details = new ArrayList<LogDetail>();

	private LogDayRelease release;

	public LogOperation(String opId, int opSg) {
		this.opId = opId;

		this.opDt = StringUtils.getCurrentTime();
	}

	public LogDayRelease getRelease() {
		return release;
	}

	public void setRelease(LogDayRelease release) {
		this.release = release;
	}

	public int getComSta() {
		return comSta;
	}

	public void setComSta(int comSta) {
		this.comSta = comSta;
	}

	public String getComDt() {
		return comDt;
	}

	public void setComDt(String comDt) {
		this.comDt = comDt;
	}

	public int getLockSta() {
		return lockSta;
	}

	public void setLockSta(int lockSta) {
		this.lockSta = lockSta;
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public String getOpDt() {
		return opDt;
	}

	public void setOpDt(String opDt) {
		this.opDt = opDt;
	}

	public List<LogDetail> getDetails() {
		return details;
	}

	public void setDetails(List<LogDetail> details) {
		this.details = details;
	}

	public String tableName() {
		return "log_operation";
	}
}
