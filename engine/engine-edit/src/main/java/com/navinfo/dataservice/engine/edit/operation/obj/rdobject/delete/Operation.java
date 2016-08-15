package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.mysql.fabric.xmlrpc.base.Array;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;

/**
 * 
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:02 
* @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;
	
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}
	
	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(command.getRdObject(), ObjStatus.DELETE, command.getPid());
		
		return null;
	}
}
