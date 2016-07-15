package com.navinfo.dataservice.engine.edit.bo.ad;

import net.sf.json.JSONObject;

/** 
 * @ClassName: EditService
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: EditService.java
 */
public class EditService {
	
	public JSONObject runCmd(int dbId,String opType,String objType,JSONObject data)throws Exception{
		AbstractOperator op = OperatorFactory.getInstance().create(opType, objType, data);
		return OperatorRunner.run(dbId, op);
	}
}
