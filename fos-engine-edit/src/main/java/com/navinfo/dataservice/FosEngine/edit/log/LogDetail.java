package com.navinfo.dataservice.FosEngine.edit.log;

public class LogDetail {

	private String opId;

	private String obNm;

	private String obPk;

	private int obPid;

	private int opbTp;

	private int obTp;

	private String opDt;

	private String tbNm;

	private String oldValue;

	private String newValue;

	private String fdLst;

	private int opTp;

	private String rowId;

	private int isCk;
	
	private String tbRowId;
	
	private int meshId;
	
	private int gridId;
	
	
	public LogDetail() {

	}

	
	public String getTbRowId() {
		return tbRowId;
	}



	public void setTbRowId(String tbRowId) {
		this.tbRowId = tbRowId;
	}



	public int getMeshId() {
		return meshId;
	}



	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}



	public int getGridId() {
		return gridId;
	}



	public void setGridId(int gridId) {
		this.gridId = gridId;
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

	public String getObPk() {
		return obPk;
	}

	public void setObPk(String obPk) {
		this.obPk = obPk;
	}

	public int getObPid() {
		return obPid;
	}

	public void setObPid(int obPid) {
		this.obPid = obPid;
	}

	public int getOpbTp() {
		return opbTp;
	}

	public void setOpbTp(int opbTp) {
		this.opbTp = opbTp;
	}

	public int getObTp() {
		return obTp;
	}

	public void setObTp(int obTp) {
		this.obTp = obTp;
	}

	public String getOpDt() {
		return opDt;
	}

	public void setOpDt(String opDt) {
		this.opDt = opDt;
	}

	public String getTbNm() {
		return tbNm;
	}

	public void setTbNm(String tbNm) {
		this.tbNm = tbNm;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getFdLst() {
		return fdLst;
	}

	public void setFdLst(String fdLst) {
		this.fdLst = fdLst;
	}

	public int getOpTp() {
		return opTp;
	}

	public void setOpTp(int opTp) {
		this.opTp = opTp;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public int getIsCk() {
		return isCk;
	}

	public void setIsCk(int isCk) {
		this.isCk = isCk;
	}

	public String tableName() {
		return "log_detail";
	}

}
