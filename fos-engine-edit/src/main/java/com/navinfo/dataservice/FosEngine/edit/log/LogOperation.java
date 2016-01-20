package com.navinfo.dataservice.FosEngine.edit.log;

import java.util.ArrayList;
import java.util.List;

public class LogOperation {

	private String opId;

	private String usId;

	private String opCmd;

	private String opDt;

	private int opSg;

	private List<LogDetail> details = new ArrayList<LogDetail>();

	public LogOperation() {

	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public String getUsId() {
		return usId;
	}

	public void setUsId(String usId) {
		this.usId = usId;
	}

	public String getOpCmd() {
		return opCmd;
	}

	public void setOpCmd(String opCmd) {
		this.opCmd = opCmd;
	}

	public String getOpDt() {
		return opDt;
	}

	public void setOpDt(String opDt) {
		this.opDt = opDt;
	}

	public int getOpSg() {
		return opSg;
	}

	public void setOpSg(int opSg) {
		this.opSg = opSg;
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
