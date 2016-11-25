package com.navinfo.dataservice.engine.editplus.model;

import java.util.Map;

import com.navinfo.dataservice.engine.editplus.operation.OperationType;

/** 
 * @ClassName: ChangeLog
 * @author xiaoxiaowen4127
 * @date 2016年11月21日
 * @Description: ChangeLog.java
 */
public class ChangeLog {
	protected OperationType opType=OperationType.INITIALIZE;//表记录的操作状态
	protected Map<String,Object> oldValues=null;//存储变化字段的旧值，key:col_name,value：旧值
	public ChangeLog(){
	}
	public ChangeLog(OperationType opType,Map<String,Object> oldValues){
		this.opType=opType;
		this.oldValues=oldValues;
	}
	public OperationType getOpType() {
		return opType;
	}
	public void setOpType(OperationType opType) {
		this.opType = opType;
	}
	public Map<String, Object> getOldValues() {
		return oldValues;
	}
	public void setOldValues(Map<String, Object> oldValues) {
		this.oldValues = oldValues;
	}
}
