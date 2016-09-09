package com.navinfo.dataservice.engine.edit.operation.batch;

/**
 * 操作类型
 */
public enum BatchRuleType {
	BATCHDELURBAN,
	BATCHREGIONIDRDLINK,
	BATCHREGIONIDPOI,
	//根据BUA赋URBAN
	BATCHBUAURBAN,
	//在线批处理删除ZoneID
	BATCHDELZONEID,
	//在线批处理赋ZONEID
	BATCHZONEID
}
