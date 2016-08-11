package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:17:21
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;
	
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}
	
	public Operation(Connection conn){
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		result.insertObject(this.command.getRdTollgate(), ObjStatus.DELETE, this.command.getPid());
		return null;
	}

	/**
	 * 根据删除线的PID查询出与之相关的收费站并删除
	 * 
	 * @param result
	 *            存放待处理的结果集
	 * @param linkPids
	 *            将删除线的PID
	 * @return
	 * @throws Exception
	 */
	public String deleteRdTollgate(Result result, List<Integer> linkPids) throws Exception {
		RdTollgateSelector selector = new RdTollgateSelector(this.conn);
		for (Integer linkPid : linkPids) {
			List<RdTollgate> rdTollgates = selector.loadRdTollgatesWithLinkPid(linkPid, true);
			for (RdTollgate rdTollgate : rdTollgates) {
				result.insertObject(rdTollgate, ObjStatus.DELETE, rdTollgate.pid());
			}
		}
		return null;
	}
}
