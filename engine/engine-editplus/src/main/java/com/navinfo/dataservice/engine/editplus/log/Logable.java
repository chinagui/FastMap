package com.navinfo.dataservice.engine.editplus.log;

import java.util.Map;

import com.navinfo.dataservice.engine.editplus.glm.NonGeoPidException;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;

/** 
 * @ClassName: Logable
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: Logable.java
 */
public interface Logable {

	public OperationType getOpType();
	public Map<String, Object> getOldValues();
	public String tableName();
	public long getObjPid();
	public long getGeoPid()throws NonGeoPidException,Exception;
	public String getObjType();
	public String getGeoType();
}
