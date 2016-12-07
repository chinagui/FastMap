package com.navinfo.dataservice.dao.plus.model.basic;

/** 
 * INSERTR_DELETE是新增后删除，主表才会有这个状态，子表新增后删除，直接物理删除了
 * PRE_DELETED是已删除的数据，有了这个状态，已删除的数据就可以流转到下一个操作环节了
 * @ClassName: OperationType
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: OperationType.java
 */
public enum OperationType {
	INSERT, DELETE, UPDATE,INSERT_DELETE,PRE_DELETED,INITIALIZE;
	
	public static OperationType getOperationType(int opTp){
		if(opTp==3){
			return OperationType.UPDATE;
		}else if(opTp==1){
			return OperationType.INSERT;
		}else if(opTp==2){
			return OperationType.DELETE;
		}else if(opTp==4){
			return OperationType.INSERT_DELETE;
		}else if(opTp==5){
			return OperationType.PRE_DELETED;
		}
		return OperationType.INITIALIZE;
	}
}
