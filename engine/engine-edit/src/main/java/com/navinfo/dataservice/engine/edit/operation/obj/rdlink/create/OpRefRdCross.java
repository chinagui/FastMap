package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;

/**
 * 如果原始link为路口内组成link，分割link新生成的NODE应加入路口子点中，
 * 如果此路口的信号灯字段为“无路口红绿灯”或“有行人红绿灯”，则不影响信号灯记录； 如果此路口的信号灯字段为“有路口红绿灯”，则程序自动维护信号灯记录，
 * 具体维护原则见“修改道路路口节点”中“增加节点导致增加组成link”部分对信号灯的维护；
 * 
 * @ClassName: OpRefRdCross
 * @author Zhang Xiaolong
 * @date 2016年10月31日 上午10:01:58
 * @Description: TODO
 */
public class OpRefRdCross implements IOperation {

	private Connection conn;

	private List<RdLink> links;

	public OpRefRdCross(Connection connection, List<RdLink> links) {

		this.conn = connection;

		this.links = links;
	}

	@Override
	public String run(Result result) throws Exception {

		// 针对挂接两个node点为都是路口点位的要维护link形态为交叉口内link
		List<Integer> hasHandledLinks = handleLinksForCorss(links, result);

		List<IRow> addRows = result.getAddObjects();

		Map<Integer, List<Integer>> crossNodeMap = new HashMap<>();

		RdCrossSelector selector = new RdCrossSelector(conn);

		for (IRow row : addRows) {
			if (row instanceof RdCrossNode) {
				RdCrossNode crossNode = (RdCrossNode) row;

				if (crossNodeMap.containsKey(crossNode.getPid())) {
					crossNodeMap.get(crossNode.getPid()).add(crossNode.getNodePid());
				} else {
					List<Integer> crossNodeList = new ArrayList<>();

					crossNodeList.add(crossNode.getNodePid());

					crossNodeMap.put(crossNode.getPid(), crossNodeList);
				}
			}
		}

		List<RdTrafficsignal> insertTraffsignals = new ArrayList<>();

		for (Map.Entry<Integer, List<Integer>> entry : crossNodeMap.entrySet()) {
			int crossPid = entry.getKey();

			RdCross cross = (RdCross) selector.loadById(crossPid, true);

			List<Integer> crossNodePidList = entry.getValue();

			List<Integer> allCrossNodePidList = new ArrayList<>();

			allCrossNodePidList.addAll(crossNodePidList);

			for (IRow row : cross.getNodes()) {
				RdCrossNode crossNode = (RdCrossNode) row;

				allCrossNodePidList.add(crossNode.getNodePid());
			}

			for (RdLink link : links) {
				// 交叉口内link。已经处理过的不再处理
				if (!hasHandledLinks.contains(link.getPid()) && allCrossNodePidList.contains(link.getsNodePid())
						&& allCrossNodePidList.contains(link.geteNodePid())) {
					RdLinkForm form = (RdLinkForm) link.getForms().get(0);

					form.setFormOfWay(50);

					// 将link记录到路口的组成link中
					RdCrossLink crossLink = new RdCrossLink();

					crossLink.setPid(cross.getPid());

					crossLink.setLinkPid(link.getPid());

					result.insertObject(crossLink, ObjStatus.INSERT, crossLink.getPid());
				} else if (cross.getSignal() == 1) {
					// 有红绿灯信号维护红绿灯
					for (Integer crossNodePid : crossNodePidList) {
						// link的起点活终点都需要建立红绿灯（交叉口内link除外）
						if (link.getsNodePid() == crossNodePid || link.geteNodePid() == crossNodePid) {
							RdTrafficsignal signal = new RdTrafficsignal();

							signal.setPid(PidUtil.getInstance().applyRdTrafficsignalPid());

							signal.setLinkPid(link.getPid());

							// 默认为受控制
							signal.setFlag(1);

							signal.setNodePid(crossNodePid);

							insertTraffsignals.add(signal);
						}
					}
				}
			}
		}

		for (RdTrafficsignal signal : insertTraffsignals) {
			result.insertObject(signal, ObjStatus.INSERT, signal.getPid());
		}

		return null;
	}

	/**
	 * 针对新增的link处理挂接两个node点都是同一个路口点的形态问题
	 * 
	 * @param links
	 * @param result
	 * @param selector
	 * @throws Exception
	 */
	private List<Integer> handleLinksForCorss(List<RdLink> links, Result result) throws Exception {
		List<Integer> hasHandledLink = new ArrayList<>();

		RdCrossNodeSelector nodeSelector = new RdCrossNodeSelector(conn);

		for (RdLink link : links) {
			int sNodePid = link.getsNodePid();

			RdCrossNode sCrossNode = (RdCrossNode) nodeSelector.loadByNodeId(sNodePid, true);

			int eNodePid = link.geteNodePid();

			RdCrossNode eCrossNode = (RdCrossNode) nodeSelector.loadByNodeId(eNodePid, true);

			// 如果起点和终点是同一路口点，则link形态为交叉口内link
			if (!hasHandledLink.contains(link.getPid()) && sCrossNode != null && eCrossNode != null
					&& sCrossNode.getPid() == eCrossNode.getPid()) {
				RdLinkForm form = (RdLinkForm) link.getForms().get(0);

				form.setFormOfWay(50);

				hasHandledLink.add(link.getPid());
				// 将link记录到路口的组成link中
				RdCrossLink crossLink = new RdCrossLink();

				crossLink.setPid(sCrossNode.getPid());

				crossLink.setLinkPid(link.getPid());

				result.insertObject(crossLink, ObjStatus.INSERT, crossLink.getPid());
			}
		}

		return hasHandledLink;
	}
}
