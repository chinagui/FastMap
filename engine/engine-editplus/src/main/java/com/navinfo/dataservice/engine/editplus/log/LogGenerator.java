package com.navinfo.dataservice.engine.editplus.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.UuidUtils;

/** 
 * @ClassName: LogGenerator
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: LogGenerator.java
 */
public class LogGenerator {
	
	/**
	 * 根据编辑结果生成履历模型对象
	 * @param results
	 * @return
	 * @throws Exception
	 */
	public static List<LogOperation> generate(Collection<Logable> rows,boolean isOneOperation)throws Exception{
		String geoOpId = UuidUtils.genUuid();//先生成一个几何的统一uuid,如果有设计到几何变化,使用该uuid
		List<LogOperation> logs = new ArrayList<LogOperation>();
		if(rows!=null&&rows.size()>0){
			for(Logable row:rows){
				LogOperation op = new LogOperation();
				
//				if(row.isGeoChanged()){
//					op.setOpId(geoOpId);
//				}else{
//					op.setOpId(UuidUtils.genUuid());
//				}
			}
		}
		return logs;
	}

	
}
