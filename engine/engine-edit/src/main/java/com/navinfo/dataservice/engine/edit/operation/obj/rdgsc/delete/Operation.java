package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

/**
 * 
* @Title: Operation.java 
* @Description: 删除立交操作类
* @author 张小龙   
* @date 2016年4月18日 下午3:06:03 
* @version V1.0
 */
public class Operation implements IOperation {

	private RdGsc rdGsc;

	public Operation(Command command, RdGsc rdGsc) {

		this.rdGsc = rdGsc;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());
				
		return null;
	}

}
