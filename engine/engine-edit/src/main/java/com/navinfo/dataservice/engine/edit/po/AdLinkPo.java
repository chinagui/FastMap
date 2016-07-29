package com.navinfo.dataservice.engine.edit.po;

import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: AdLinkPo
 * @author xiaoxiaowen4127
 * @date 2016年7月22日
 * @Description: AdLinkPo.java
 */
public class AdLinkPo extends BasicPo {
	private int pid;

	private int sNodePid;

	private int eNodePid;

	private int kind = 1;

	private int form = 1;

	private Geometry geometry;

	private double length;

	private int scale;

	private int editFlag = 1;

	private String rowId;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public void setsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public void seteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
}
