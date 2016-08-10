package com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

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

		result.insertObject(command.getRdSameNode(), ObjStatus.DELETE, command.getPid());
		
		//删除存在同一线关系的同一点，则同时删除同一线关系
		//TODO
		return null;
	}
	
	/**
	 * 删除线维护同一关系
	 * @param linkPid 
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLink(RdLink link, Result result) throws Exception {
		
	}
}
