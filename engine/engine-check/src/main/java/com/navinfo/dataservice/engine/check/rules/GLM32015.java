package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 
 * 车道类型为超车道，危险信息不能为 “禁止超车”，否则报log
 * 
 */
public class GLM32015 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		// 超速车道对应的link的pid
		List<Integer> linkPids = new ArrayList<Integer>();

		// 超速车道
		List<RdLane> filterRdLanes = new ArrayList<RdLane>();

		for (IRow obj : checkCommand.getGlmList()) {

			if (obj instanceof RdLane) {

				RdLane lane = (RdLane) obj;

				// 根据模型文档：超车道只用于双向道路
				if (lane.getLaneDir() == 1) {
					continue;
				}

				int falg = (lane.getLaneType() >> 7) % 2;

				if (falg == 1 && !filterRdLanes.contains(lane)) {

					linkPids.add(lane.getLinkPid());

					filterRdLanes.add(lane);
				}
			}
		}

		if (filterRdLanes.size() == 0) {
			return;
		}

		RdWarninginfoSelector warninginfoSelector = new RdWarninginfoSelector(
				getConn());

		List<RdWarninginfo> warninginfos = warninginfoSelector.loadByLinks(
				linkPids, false);

		Map<Integer, List<RdWarninginfo>> warninginfoMap = new HashMap<Integer, List<RdWarninginfo>>();

		for (RdWarninginfo warninginfo : warninginfos) {

			if (warninginfo.getTypeCode().equals("22901")) {
				continue;
			}

			if (!warninginfoMap.containsKey(warninginfo.getLinkPid())) {

				warninginfoMap.put(warninginfo.getLinkPid(),
						new ArrayList<RdWarninginfo>());
			}

			warninginfoMap.get(warninginfo.getLinkPid()).add(warninginfo);
		}

		if (warninginfoMap.size() == 0) {
			return;
		}

		RdLinkSelector rdLinkSelector = new RdLinkSelector(getConn());

		List<IRow> rowLinks = rdLinkSelector.loadByIds(linkPids, false, false);

		Map<Integer, RdLink> linkMap = new HashMap<Integer, RdLink>();

		for (IRow rowLink : rowLinks) {

			RdLink rdLink = (RdLink) rowLink;

			linkMap.put(rdLink.getPid(), rdLink);
		}

		for (RdLane rdLane : filterRdLanes) {

			if (!warninginfoMap.containsKey(rdLane.getLinkPid())) {
				continue;
			}

			if (linkMap.containsKey(rdLane.getLinkPid())) {
				continue;
			}

			RdLink rdLink = linkMap.get(rdLane.getLinkPid());

			for (RdWarninginfo warninginfo : warninginfoMap.get(rdLane
					.getLinkPid())) {

				if ((rdLane.getLaneDir() == 2 && warninginfo.getNodePid() == rdLink
						.geteNodePid())
						|| (rdLane.getLaneDir() == 3 && warninginfo
								.getNodePid() == rdLink.getsNodePid())) {

					this.setCheckResult("", "[RD_LANE," + rdLane.pid() + "]", 0);
				}
			}
		}
	}
}
