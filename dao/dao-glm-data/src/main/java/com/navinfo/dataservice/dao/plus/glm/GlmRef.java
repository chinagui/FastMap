package com.navinfo.dataservice.dao.plus.glm;

/** 
 * @ClassName: GlmRef
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: GlmRef.java
 */
public class GlmRef {
	protected String col;//参考字段
	protected String refTable;//参考的目标表名
	protected String refCol;//参考的目标表的字段
	protected boolean refMain;//是否参考的主表
	public String getCol() {
		return col;
	}
	public void setCol(String col) {
		this.col = col;
	}
	public String getRefTable() {
		return refTable;
	}
	public void setRefTable(String refTable) {
		this.refTable = refTable;
	}
	public String getRefCol() {
		return refCol;
	}
	public void setRefCol(String refCol) {
		this.refCol = refCol;
	}
	public boolean isRefMain() {
		return refMain;
	}
	public void setRefMain(boolean refMain) {
		this.refMain = refMain;
	}
}
