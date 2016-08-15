package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 下午1:43:16
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Connection conn;

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		boolean isChange = this.command.getSpeedbump().fillChangeFields(this.command.getContent());
		if (isChange) {
			result.insertObject(this.command.getSpeedbump(), ObjStatus.UPDATE, this.command.getSpeedbump().pid());
		}
		return null;
	}

	/**
	 * 打断、移动、修形时维护减速带信息
	 * 
	 * @param result
	 *            结果集
	 * @param oldLinkPid
	 *            被打断RdLinkPid
	 * @param newLinks
	 *            打断后生成的RdLinkPid
	 * @return
	 * @throws Exception
	 */
	public String breakSpeedbump(Result result, int oldLinkPid, List<RdLink> newLinks) throws Exception {
		RdSpeedbumpSelector selector = new RdSpeedbumpSelector(this.conn);
		// 查询出将要被影响的减速带信息
		List<RdSpeedbump> speedbumps = selector.loadByLinkPid(oldLinkPid, true);
		for (RdSpeedbump speedbump : speedbumps) {
			int nodePid = speedbump.getNodePid();
			// 根据进入点PID判断新的减速带进入线，并更新进入线
			for (RdLink link : newLinks) {
				if (link.getsNodePid() == nodePid || link.geteNodePid() == nodePid) {
					speedbump.changedFields().put("linkPid", link.pid());
					break;
				}
			}
			result.insertObject(speedbump, ObjStatus.UPDATE, speedbump.pid());
		}

		return null;
	}

}
