package com.navinfo.dataservice.engine.editplus.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.operation.OperationResult;

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
	public static List<LogOperation> generate(OperationResult results)throws Exception{
		String geoOpId = UuidUtils.genUuid();//先生成一个几何的统一uuid,如果有设计到几何变化,使用该uuid
		List<LogOperation> logs = new ArrayList<LogOperation>();
		if(results.getAddObjs().size()>0){
			logs.addAll(generate(results.getAddObjs(),geoOpId));
		}
		if(results.getDelObjs().size()>0){
			logs.addAll(generate(results.getDelObjs(),geoOpId));
		}
		if(results.getUpdateObjs().size()>0){
			logs.addAll(generate(results.getUpdateObjs(),geoOpId));
		}
		return logs;
	}
	private static List<LogOperation> generate(Collection<BasicObj> objs,String geoOpId)throws Exception{
		//add
		for(BasicObj obj:objs){
			//主对象表
			
			//子对象
			Map<Class<? extends BasicObj>,List<BasicObj>> subobjs = obj.childObjs();
			while(objs!=null&&objs.size()>0){
				
			}
			//子表
			
		}
		
		
		
		return null;
	}

	private static List<LogOperation> generate(BasicObj obj,String geoOpId)throws Exception{
		List<LogOperation> logs = new ArrayList<LogOperation>();
		//主对象表
		
		//子表
		//子对象

		return null;
	}
	
}
