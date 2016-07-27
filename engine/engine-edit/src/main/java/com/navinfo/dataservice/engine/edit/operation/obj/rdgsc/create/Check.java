package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.vividsolutions.jts.geom.Geometry;

public class Check {

	private Connection conn;

	public Check(Connection conn) {
		this.conn = conn;
	}

	public boolean checkIsHasGsc(Geometry gscGeo, Map<Integer, RdGscLink> map) throws Exception {
		boolean flag = false;

		RdGscSelector selector = new RdGscSelector(conn);

		List<Integer> linkPidList = new ArrayList<>();

		for (RdGscLink rdGscLink : map.values()) {
			if (!linkPidList.contains(rdGscLink.getLinkPid())) {
				linkPidList.add(rdGscLink.getLinkPid());
			}
		}

		List<RdGsc> rdGscList = selector.loadRdGscByInterLinkPids(linkPidList, false);

		for (RdGsc gsc : rdGscList) {
			if (gsc.getGeometry().equals(gscGeo)) {
				flag = true;
				break;
			}
		}

		return flag;
	}

	/**
	 * 立交检查
	 * 
	 * @param gscGeo
	 * @throws Exception
	 */
	public void checkGsc(Geometry gscGeo, Map<Integer, RdGscLink> map) throws Exception {

		if (gscGeo == null || gscGeo.isEmpty()) {
			throw new Exception("矩形框和线没有相交点");
		}

		if (gscGeo.getNumGeometries() != 1) {
			throw new Exception("矩形框内线的交点有多个");
		}

		// 检查线上该点位是否存在立交
		List<Integer> linkPidList = new ArrayList<>();

		for (RdGscLink rdGscLink : map.values()) {
			if (!linkPidList.contains(rdGscLink.getLinkPid())) {
				linkPidList.add(rdGscLink.getLinkPid());
			}
		}

		boolean flag = RdGscOperateUtils.checkIsHasGsc(gscGeo, linkPidList, conn);

		if (flag) {
			throw new Exception("同一点位不能重复创建立交");
		}
	}

	/**
	 * 检查立交的组成线是否正确
	 * 
	 * @param rowMap
	 * @throws Exception
	 */
	public void checkGscLink(Geometry gscGeo, Map<Integer, IRow> rowMap) throws Exception {

		List<Integer> rdNodePidList = new ArrayList<>();

		List<Integer> rwNodePidList = new ArrayList<>();

		RdNodeSelector rdNodeSelector = new RdNodeSelector(conn);
		
		RwNodeSelector rwNodeSelector = new RwNodeSelector(conn);

		for (IRow row : rowMap.values()) {
			if (row instanceof RdLink) {
				RdLink link = (RdLink) row;

				int sNodePid = link.getsNodePid();

				int eNodePid = link.geteNodePid();

				if (rdNodePidList.contains(sNodePid)) {
					RdNode node = (RdNode) rdNodeSelector.loadById(sNodePid, false);

					if (node.getGeometry().equals(gscGeo)) {
						throw new Exception("不能在两条联通的线的node点处创建立交");
					}
				}
				if (rdNodePidList.contains(eNodePid)) {
					RdNode node = (RdNode) rdNodeSelector.loadById(eNodePid, false);

					if (node.getGeometry().equals(gscGeo)) {
						throw new Exception("不能在两条联通的线的node点处创建立交");
					}
				}
				rdNodePidList.add(sNodePid);
				rdNodePidList.add(eNodePid);
			}
			if (row instanceof RwLink) {
				RwLink link = (RwLink) row;

				int sNodePid = link.getsNodePid();

				int eNodePid = link.geteNodePid();

				if (rwNodePidList.contains(sNodePid)) {
					RwNode node = (RwNode) rwNodeSelector.loadById(sNodePid, false);

					if (node.getGeometry().equals(gscGeo)) {
						throw new Exception("不能在两条联通的线node点处创建立交");
					}
				}
				if (rwNodePidList.contains(eNodePid)) {
					RwNode node = (RwNode) rwNodeSelector.loadById(eNodePid, false);

					if (node.getGeometry().equals(gscGeo)) {
						throw new Exception("不能在两条联通的线node点处创建立交");
					}
				}
				rwNodePidList.add(sNodePid);
				rwNodePidList.add(eNodePid);
			}
		}
	}
}
