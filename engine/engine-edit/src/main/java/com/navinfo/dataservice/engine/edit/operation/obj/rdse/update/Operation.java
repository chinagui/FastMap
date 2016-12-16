package com.navinfo.dataservice.engine.edit.operation.obj.rdse.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;

/**
 * @author zhangyt
 * @Title: Operation.java
 * @Description: TODO
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
		boolean isChange = command.getRdSe().fillChangeFields(
				command.getContent());
		if (isChange) {
			result.insertObject(command.getRdSe(), ObjStatus.UPDATE, command
					.getRdSe().pid());
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
	public String breakRdSe(Result result, int oldLink, List<RdLink> newLinks)
			throws Exception {
		RdSeSelector selector = new RdSeSelector(this.conn);
		// 查询所有与被删除RdLink关联的分叉口提示
		List<RdSe> rdSes = selector.loadRdSesWithLinkPid(oldLink, true);
		// 循环处理每一个分叉口提示
		for (RdSe rdSe : rdSes) {
			// 分叉口提示的进入点的Pid
			int nodePid = rdSe.getNodePid();
			if (oldLink == rdSe.getInLinkPid()) {
				for (RdLink link : newLinks) {
					if (nodePid == link.getsNodePid()
							|| nodePid == link.geteNodePid()) {
						// 如果新生成线的终点的Pid与分叉口提示的nodePid相等
						// 则该新生成线为进入线，修改进入线Pid
						rdSe.changedFields().put("inLinkPid", link.pid());
						break;
					}
				}
			} else {
				for (RdLink link : newLinks) {
					if (nodePid == link.getsNodePid()
							|| nodePid == link.geteNodePid()) {
						// 如果新生成线的起点的Pid与分叉口提示的nodePid相等
						// 则该新生成线为退出线，修改退出线Pid
						rdSe.changedFields().put("outLinkPid", link.pid());
						break;
					}
				}
			}
			for (RdLink link : newLinks)
				// 将需要修改的分叉口提示放入结果集中
				result.insertObject(rdSe, ObjStatus.UPDATE, rdSe.pid());
		}
		return null;
	}
	
	/**
	 * 分离节点
	 * 
	 * @param link
	 * @param nodePid
	 * @param rdlinks
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.add(nodePid);

		departNode(link, nodePids, rdlinks, result);
	}

	/**
	 * 分离节点
	 * 
	 * @param link
	 * @param nodePid
	 * @param rdlinks
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, List<Integer> nodePids,
			List<RdLink> rdlinks, Result result) throws Exception {

		int linkPid = link.getPid();

		// 跨图幅处理的link为进入线的分岔口
		Map<Integer, RdSe> seInLink = null;

		// 跨图幅处理的link为退出线的分岔口
		Map<Integer, RdSe> seOutLink = null;

		if (rdlinks != null && rdlinks.size() > 1) {

			seInLink = new HashMap<Integer, RdSe>();

			seOutLink = new HashMap<Integer, RdSe>();
		}

		RdSeSelector selector = new RdSeSelector(this.conn);

		// 在link上的RdSe
		List<RdSe> ses = selector.loadRdSesWithLinkPid(linkPid, true);

		for (int nodePid : nodePids) {
			
			for (RdSe se : ses) {
				
				if (se.getNodePid() == nodePid) {

					result.insertObject(se, ObjStatus.DELETE, se.getPid());

				} else if (seInLink != null && se.getInLinkPid() == linkPid) {

					seInLink.put(se.getPid(), se);

				} else if (seOutLink != null && se.getOutLinkPid() == linkPid) {

					seOutLink.put(se.getPid(), se);
				}
			}

			if (seOutLink == null || seInLink == null) {

				return;
			}

			int connectNode = link.getsNodePid() == nodePid ? link
					.geteNodePid() : link.getsNodePid();

			for (RdLink rdlink : rdlinks) {

				if (rdlink.getsNodePid() != connectNode
						&& rdlink.geteNodePid() != connectNode) {

					continue;
				}

				for (RdSe se : seInLink.values()) {

					se.changedFields().put("inLinkPid", rdlink.getPid());

					result.insertObject(se, ObjStatus.UPDATE, se.pid());
				}

				for (RdSe se : seOutLink.values()) {

					se.changedFields().put("outLinkPid", rdlink.getPid());

					result.insertObject(se, ObjStatus.UPDATE, se.pid());
				}
			}
		}
	}

}
