package com.navinfo.dataservice.engine.editplus.operation;

/** 
 * INSERTR_DELETE是新增后删除，主表才会有这个状态，子表新增后删除，直接物理删除了
 * @ClassName: OperationType
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: OperationType.java
 */
public enum OperationType {
	INSERT, DELETE, UPDATE,INSERT_DELETE,INITIALIZE;
}
