package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:27
 * @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn = null;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		RdTrafficsignal rdTrafficsignal = this.command.getRdTrafficsignal();

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());

				return null;
			} else {
				boolean isChanged = rdTrafficsignal.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
				}
			}
		}

		return null;
	}

	/**
	 * 打断link维护信号灯
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void breakRdLink(int linkPid, List<RdLink> newLinks, Result result) throws Exception {
		if (conn == null) {
			return;
		}

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		List<RdTrafficsignal> rdTrafficsignals = selector.loadByLinkPid(true, linkPid);

		if (CollectionUtils.isNotEmpty(rdTrafficsignals)) {
			for (RdTrafficsignal rdTrafficsignal : rdTrafficsignals) {
				for (RdLink link : newLinks) {

					if (link.getsNodePid() == rdTrafficsignal.getNodePid()
							|| link.geteNodePid() == rdTrafficsignal.getNodePid()) {

						rdTrafficsignal.changedFields().put("linkPid", link.getPid());

						result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
					}
				}
			}
		}
	}

	/**
	 * 修改道路方向维护信号灯关系
	 * 
	 * @param result
	 * @throws Exception
	 */
	public String updateRdCrossByModifyLinkDirect(RdLink updateLink, Result result) throws Exception {

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		int direct = (int) updateLink.changedFields().get("direct");

		List<RdTrafficsignal> trafficsignals = selector.loadByLinkPid(true, updateLink.getPid());

		if (CollectionUtils.isNotEmpty(trafficsignals)) {
			for (RdTrafficsignal rdTrafficsignal : trafficsignals) {
				int nodePid = rdTrafficsignal.getNodePid();
				// 道路由双方向修改为单方向（且修改的方向为路口的退出方向）时，该link上的信号灯应删除。
				if (updateLink.getDirect() == 1 && direct == 2 && updateLink.getsNodePid() == nodePid) {
					//return "请注意，修改道路方向，可能需要对下列路口维护信号灯信息：" + rdTrafficsignal.getPid();
					result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());
				}
				// 关联道路的方向为路口的进入方向，修改为路口的退出方向时，应删除该link上的信号灯。
				else if (updateLink.getDirect() == 2 && direct == 3) {
					//return "请注意，修改道路方向，可能需要对下列路口维护信号灯信息：" + rdTrafficsignal.getPid();
					result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());
				}
			}
		} 
//			else {
//			int sNodePid = updateLink.getsNodePid();
//
//			// 关联道路的方向为路口的退出方向，修改为路口的进入方向时，应创建该link上的信号灯。
//			List<RdTrafficsignal> rdTrafficsignals = selector.loadByNodeId(true, sNodePid);
//
//			if (CollectionUtils.isNotEmpty(rdTrafficsignals)) {
//				if (updateLink.getDirect() == 2 && (direct == 3 || direct == 1)) {
//					//return "请注意，修改道路方向，可能需要对下列路口LINK维护信号灯信息（LINK_PID）：" + updateLink.getPid();
//					result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());
//				}
//			}
//
//			int eNodePid = updateLink.getsNodePid();
//
//			// 关联道路的方向为路口的退出方向，修改为路口的进入方向时，应创建该link上的信号灯。
//			List<RdTrafficsignal> rdTrafficsignals2 = selector.loadByNodeId(true, eNodePid);
//
//			if (CollectionUtils.isNotEmpty(rdTrafficsignals2)) {
//				if (updateLink.getDirect() == 2 && (direct == 3 || direct == 1)) {
//					//return "请注意，修改道路方向，可能需要对下列路口LINK维护信号灯信息（LINK_PID）：" + updateLink.getPid();
//				}
//			}
//		}
		return "请注意，修改道路方向，可能需要对下列路口LINK维护信号灯信息（LINK_PID）："+ updateLink.getPid();
	}
}
