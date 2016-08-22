package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;

/**
 * 详细车道批量操作
 * 
 * @author 赵凯凯
 * 
 */
public class Operation implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());

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
		this.deleteRdLanes(result);
		return null;
	}

	/**
	 * 批量删除详细车道信息车道信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void deleteRdLanes(Result result) throws Exception {

		for (RdLane lane : this.command.getLanes()) {
			// 删除车道信息
			com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
					conn);
			operation.deleteRdLane(result, lane.getPid());
		}
	}

}