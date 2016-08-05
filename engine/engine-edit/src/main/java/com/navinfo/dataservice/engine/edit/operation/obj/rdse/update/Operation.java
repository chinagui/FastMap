package com.navinfo.dataservice.engine.edit.operation.obj.rdse.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:51:14
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		boolean isChange = command.getRdSe().fillChangeFields(command.getContent());
		if (isChange) {
			result.insertObject(command.getRdSe(), ObjStatus.UPDATE, command.getRdSe().pid());
		}
		return null;
	}

	/**
	 * 根据被删除的RdLink的Pid、新生成的RdLink<br>
	 * 维护原RdLink上关联的分叉口提示
	 * 
	 * @param result
	 *            待处理的结果集
	 * @param oldLink
	 *            被删除RdLink的Pid
	 * @param newLinks
	 *            新生成的RdLink的集合
	 * @return
	 * @throws Exception
	 */
	public String breakRdSe(Result result, int oldLink, List<RdLink> newLinks) throws Exception {
		RdSeSelector selector = new RdSeSelector(this.conn);
		// 查询所有与被删除RdLink关联的分叉口提示
		List<RdSe> rdSes = selector.loadRdSesWithLinkPid(oldLink, true);
		// 循环处理每一个分叉口提示
		for (RdSe rdSe : rdSes) {
			// 分叉口提示的进入点的Pid
			int nodePid = rdSe.getNodePid();
			for (RdLink link : newLinks) {
				// 如果新生成线的起点的Pid与分叉口提示的nodePid相等
				// 则该新生成线为退出线，修改退出线Pid
				if (nodePid == link.getsNodePid()) {
					rdSe.changedFields().put("outLinkPid", link.pid());
					break;
					// 如果新生成线的终点的Pid与分叉口提示的nodePid相等
					// 则该新生成线为进入线，修改进入线Pid
				} else if (nodePid == link.geteNodePid()) {
					rdSe.changedFields().put("inLinkPid", link.pid());
					break;
				}
			}
			// 将需要修改的分叉口提示放入结果集中
			result.insertObject(rdSe, ObjStatus.UPDATE, rdSe.pid());
		}
		return null;
	}

}
