package com.navinfo.dataservice.engine.edit.operation.topo.topobreakin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.voiceguide.RdVoiceguideSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Check {
	private Connection conn;
	private RdRestrictionSelector restrictionSelector;
	private RdLaneConnexitySelector laneSelector;
	private RdVoiceguideSelector rdVoiceSelector;
	private RdBranchSelector branchSelector;
	private RdDirectrouteSelector directrouteSelector;

	public Check(Connection conn) {
		this.conn = conn;

		restrictionSelector = new RdRestrictionSelector(conn);
		laneSelector = new RdLaneConnexitySelector(conn);
		rdVoiceSelector = new RdVoiceguideSelector(conn);
		branchSelector = new RdBranchSelector(conn);
		directrouteSelector = new RdDirectrouteSelector(conn);

	}

	/**
	 * 检查需打断link是否有立交信息
	 * 
	 * @param command
	 * @throws Exception
	 */
	public void checkHasGSCinLine(Command command) throws Exception {
		RdGscSelector selector = new RdGscSelector(conn);

		for (int linkPid : command.getLinkPids()) {
			List<RdGsc> gsces = selector.loadRdGscLinkByLinkPid(linkPid, "RD_LINK", false);
			if (gsces.size() == 0) {
				continue;
			}

			for (RdGsc gsc : gsces) {
				Geometry geo = GeoTranslator.transform(gsc.getGeometry(), 0.00001, 5);
				Coordinate coor = geo.getCoordinate();
				if (GeoTranslator.isPointEquals(coor.x, coor.y, command.getBreakPoint().getX(),
						command.getBreakPoint().getY()) == true) {
					throw new Exception("选中的打断点处存在立交关系，请重新选择打断位置！");
				}
			} // for
		} // for
	}

	/**
	 * 检查需打断link中是否有车信，交限，语音引导，分歧，顺行，自然语音引导的经过线
	 * 
	 * @param command
	 * @throws Exception
	 */
	public void checkLineRelationWithPassLine(Command command) throws Exception {
		for (int linkPid : command.getLinkPids()) {
			List<RdRestriction> restriction = restrictionSelector.loadByLink(linkPid, 3, false);
			if (restriction.size() != 0) {
				throw new Exception("选中的打断点处存在交限经过线信息，请重新选择打断位置");
			}

			List<RdLaneConnexity> laneConnexity = laneSelector.loadByLink(linkPid, 3, false);
			if (laneConnexity.size() != 0) {
				throw new Exception("选中的打断点处存在车信经过线信息，请重新选择打断位置");
			}

			Set<Integer> rdVoiceguide = rdVoiceSelector.loadPidByLink(linkPid, 3);
			if (rdVoiceguide.size() != 0) {
				throw new Exception("选中的打断点处存在语音引导经过线信息，请重新选择打断位置");
			}

			List<RdBranch> rdBranch = branchSelector.loadRdBranchByViaLinkPid(linkPid, false);
			if (rdBranch.size() != 0) {
				throw new Exception("选中的打断点处存在分歧经过线信息，请重新选择打断位置");
			}

			List<RdDirectroute> directroute = directrouteSelector.loadByLinkPid(linkPid, 3, false);
			if (directroute.size() != 0) {
				throw new Exception("选中的打断点处存在顺行经过线信息，请重新选择打断位置");
			}
		} // for
	}

	/**
	 * 检查需打断link中是否有车信，交限，语音引导，分歧，顺行，自然语音引导的进入线和退出线
	 * 
	 * @param command
	 * @param conn
	 * @throws Exception
	 */
	public void checkLineRelationWithInOutLine(Command command) throws Exception {
		List<RdRestriction> restrictionIn = restrictionSelector.loadByLinks(command.getLinkPids(), 1, false);
		List<RdRestriction> restrictionOut = restrictionSelector.loadByLinks(command.getLinkPids(), 2, false);
		for (RdRestriction restriction : restrictionIn) {
			if (restrictionOut.contains(restriction)) {
				throw new Exception("选中的打断点处存在交限进入退出线信息，请重新选择打断位置");
			}
		}

		List<RdLaneConnexity> laneConnexityIn = laneSelector.loadByLinks(command.getLinkPids(), 1, false);
		List<RdLaneConnexity> laneConnexityOut = laneSelector.loadByLinks(command.getLinkPids(), 2, false);
		for (RdLaneConnexity laneConnexity : laneConnexityIn) {
			if (laneConnexityOut.contains(laneConnexity)) {
				throw new Exception("选中的打断点处存在车信进入退出线信息，请重新选择打断位置");
			}
		}

		List<RdVoiceguide> rdVoiceguideIn = rdVoiceSelector.loadByLinkPids(command.getLinkPids(), 1, false);
		List<RdVoiceguide> rdVoiceguideOut = rdVoiceSelector.loadByLinkPids(command.getLinkPids(), 2, false);
		for (RdVoiceguide voiceguid : rdVoiceguideIn) {
			if (rdVoiceguideOut.contains(voiceguid)) {
				throw new Exception("选中的打断点处存在语音引导进入退出线信息，请重新选择打断位置");
			}
		}

		List<RdBranch> rdBranchIn = branchSelector.loadByLinks(command.getLinkPids(), 1, false);
		List<RdBranch> rdBranchOut = branchSelector.loadByLinks(command.getLinkPids(), 2, false);
		for (RdBranch branch : rdBranchIn) {
			if (rdBranchOut.contains(branch)) {
				throw new Exception("选中的打断点处存在分歧进入退出线信息，请重新选择打断位置");
			}
		}

		List<RdDirectroute> directrouteIn = directrouteSelector.loadByLinks(command.getLinkPids(), 1, false);
		List<RdDirectroute> directrouteOut = directrouteSelector.loadByLinks(command.getLinkPids(), 2, false);
		for (RdDirectroute route : directrouteIn) {
			if (directrouteOut.contains(route)) {
				throw new Exception("选中的打断点处存在顺行进入退出线信息，请重新选择打断位置");
			}
		}
	}

	/**
	 * 前检查：两条RDLink不能首尾点一致，如果自动打断造成两条link首尾一致，则不允许打断
	 * 
	 * @param noNeedBreakLinks
	 *            不需要打断的links（需要参与检查）
	 * @param insertObjs
	 *            打断后新生成的对象（link需要参与检查）
	 * @throws Exception
	 */
	public void CheckTopoBreakHasSameSEnode(List<IRow> noNeedBreakLinks, List<IRow> insertObjs) throws Exception {
		List<RdLink> breakLinks = new ArrayList<>();

		for (IRow row : noNeedBreakLinks) {
			if (row.objType() != ObjType.RDLINK) {
				continue;
			}
			breakLinks.add((RdLink) row);
		}
		for (IRow row : insertObjs) {
			if (row.objType() != ObjType.RDLINK) {
				continue;
			}
			breakLinks.add((RdLink) row);
		}

		for (int i = 0; i < breakLinks.size(); i++) {
			for (int j = i + 1; j < breakLinks.size(); j++) {
				if ((breakLinks.get(i).getsNodePid() == breakLinks.get(j).getsNodePid()
						&& breakLinks.get(i).geteNodePid() == breakLinks.get(j).geteNodePid())
						|| (breakLinks.get(i).getsNodePid() == breakLinks.get(j).geteNodePid()
								&& breakLinks.get(i).geteNodePid() == breakLinks.get(j).getsNodePid())) {
					throw new Exception("两条RDLink不能首尾点一致，如果自动打断造成两条link首尾一致，则不允许打断!");
				}
			} // for j
		} // for i
	}

}
