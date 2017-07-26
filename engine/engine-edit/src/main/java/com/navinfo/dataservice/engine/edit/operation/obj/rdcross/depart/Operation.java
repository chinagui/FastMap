package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/11 0011.
 */
public class Operation {
	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 维护上下线分离对RdCross的影响
	 *
	 * @param links
	 *            分离线
	 * @param leftLinks
	 *            分离后左线
	 * @param rightLinks
	 *            分离后右线
	 * @param result
	 *            结果集
	 * @return
	 * @throws Exception
	 */
	public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks,
			Result result) throws Exception {
		RdCrossSelector selector = new RdCrossSelector(conn);
		// 1.路口点为目标link的经过点
		List<Integer> nodePids = CalLinkOperateUtils.calNodePids(links);
		if (!nodePids.isEmpty()) {
			List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(nodePids, new ArrayList<Integer>(), true);
			for (RdCross cross : crosses) {
				result.insertObject(cross, ObjStatus.DELETE, cross.pid());
				for (IRow row : cross.getNodes()) {
					nodePids.add(((RdCrossNode) row).getNodePid());
				}
			}
		}

		// 2.维护分离link的交叉口道路形态
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> allDelLinks = linkSelector.loadByNodePids(nodePids, false);
		for (RdLink link : allDelLinks) {
			RdLink leftLink = leftLinks.get(link.pid());
			RdLink rightLink = rightLinks.get(link.pid());
			if (null != leftLink) {
				this.updateLinkForm(leftLink, result);
			}
			if (null != rightLink) {
				this.updateLinkForm(rightLink, result);
			}
			if (null == leftLink && null == rightLink) {
				link = (RdLink) linkSelector.loadById(link.pid(), true);
				if (isTargetLink(link, links))
					continue;
				this.updateLinkForm(link, result);
			}
		}

		//一个link进行上下线分离维护RdCross
		if (links.size() == 1) {

			List<RdLink> newRdLinks = new ArrayList<>();

			newRdLinks.addAll(leftLinks.values());

			newRdLinks.addAll(rightLinks.values());

			RdLink delLink = links.get(0);

			oneLinkForRdCross(delLink, selector, newRdLinks, result);
		}

		return "";
	}

	/**
	 * 一个link进行上下线分离维护RdCross
	 * @param delLink
	 * @param selector
	 * @param newRdlinks
	 * @throws Exception
	 */
	private void oneLinkForRdCross(RdLink delLink, RdCrossSelector selector, List<RdLink> newRdlinks, Result result) throws Exception {

		List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(null, Arrays.asList(delLink.getPid()), true);

		for (RdCross cross : crosses) {

			Set<Integer> crossNodePids = new HashSet<>();

			for (IRow row : cross.getNodes()) {

				RdCrossNode crossNode = (RdCrossNode) row;

				crossNodePids.add(crossNode.getNodePid());
			}

			//link的任意端点不是路口的组成node
			if (!crossNodePids.contains(delLink.getsNodePid()) ||
					!crossNodePids.contains(delLink.geteNodePid())) {

				continue;
			}
			for (RdLink newlink:newRdlinks) {

				Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(GeoTranslator.transform(newlink.getGeometry(), GeoTranslator.dPrecisionMap, 5));

				if (meshes.size()>1)
				{
					throw new Exception("一根link上线下分离操作：该link的两端点同为一个路口的组成node，不支持跨图幅进行上下线分离");
				}
			}

			RdCrossLink delCrossLink = null;

			for (IRow row : cross.getLinks()) {

				RdCrossLink crossLink = (RdCrossLink) row;

				if (crossLink.getLinkPid() == delLink.getPid()) {

					delCrossLink = crossLink;
				}
			}

			if (delCrossLink == null) {

				continue;
			}

			result.insertObject(delCrossLink, ObjStatus.DELETE, delCrossLink.getPid());

			//跨图幅新增的nodePid
			Set<Integer> newCrossNodePids = new HashSet<>();

			for (RdLink newLink : newRdlinks) {

				RdCrossLink crossLink = new RdCrossLink();

				crossLink.setPid(cross.getPid());

				crossLink.setLinkPid(newLink.getPid());

				result.insertObject(crossLink, ObjStatus.INSERT, crossLink.getPid());

				//将跨图幅新增node作为路口组成node
				if (newRdlinks.size() < 3) {

					continue;
				}
				if (!crossNodePids.contains(delLink.getsNodePid())) {

					newCrossNodePids.add(delLink.getsNodePid());
				}
				if (!crossNodePids.contains(delLink.geteNodePid())) {

					newCrossNodePids.add(delLink.getsNodePid());
				}
			}

			for (int newNodePid : newCrossNodePids) {

				RdCrossNode node = new RdCrossNode();

				node.setPid(cross.getPid());

				node.setNodePid(newNodePid);

				node.setIsMain(0);

				result.insertObject(node, ObjStatus.INSERT, node.getPid());
			}
		}
	}

	private void updateLinkForm(RdLink link, Result result) {
		for (IRow row : link.getForms()) {
			RdLinkForm form = (RdLinkForm) row;
			if (form.getFormOfWay() == 50) {
				if (link.status() == ObjStatus.INSERT) {
					if (link.getForms().size() == 1) {
						form.setFormOfWay(1);
					}
				} else if (link.status() == ObjStatus.UPDATE) {
					if (link.getForms().size() == 1) {
						form.changedFields().put("formOfWay", 1);
						result.insertObject(form, ObjStatus.UPDATE, form.parentPKValue());
					}
					else
					{
						result.insertObject(form, ObjStatus.DELETE, form.parentPKValue());
					}
				}
			}
		}
	}

	private boolean isTargetLink(RdLink link, List<RdLink> links) {
		boolean result = false;
		for (RdLink l : links) {
			if (link.pid() == l.pid())
				return true;
		}
		return result;
	}
}
