package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneTopologySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

	private RdLink rdLinkBreakpoint;

	private JSONArray jaDisplayLink;

	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);

		this.jaDisplayLink = new JSONArray();
	}

	public Process(AbstractCommand command, Connection conn) throws Exception {
		super(command);

		this.setConn(conn);

		this.jaDisplayLink = new JSONArray();
	}

	@Override
	public boolean prepareData() throws Exception {

		try {
			RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

			this.rdLinkBreakpoint = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);

			this.getResult().insertObject(rdLinkBreakpoint, ObjStatus.DELETE, rdLinkBreakpoint.pid());

			RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());

			RdNode sNode = (RdNode) nodeSelector.loadById(rdLinkBreakpoint.getsNodePid(), true);

			this.getCommand().setsNode(sNode);

			RdNode eNode = (RdNode) nodeSelector.loadById(rdLinkBreakpoint.geteNodePid(), true);

			this.getCommand().seteNode(eNode);

			// 获取此LINK上交限进入线
			List<RdRestriction> restrictions = new RdRestrictionSelector(this.getConn())
					.loadRdRestrictionByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setRestrictions(restrictions);

			// 获取此LINK上交限退出线
			List<RdRestrictionDetail> details = new RdRestrictionDetailSelector(this.getConn())
					.loadDetailsByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setRestrictionDetails(details);

			// 获取LINK上交限经过线
			List<List<Entry<Integer, RdRestrictionVia>>> restrictVias = new RdRestrictionViaSelector(this.getConn())
					.loadRestrictionViaByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setRestrictListVias(restrictVias);

			// 获取此LINK上车信进入线
			List<RdLaneConnexity> laneConnexitys = new RdLaneConnexitySelector(this.getConn())
					.loadRdLaneConnexityByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setLaneConnexitys(laneConnexitys);

			// 获取此LINK上车信退出线
			List<RdLaneTopology> topos = new RdLaneTopologySelector(this.getConn())
					.loadToposByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setLaneTopologys(topos);

			// 获取LINK上车信经过线
			List<List<Entry<Integer, RdLaneVia>>> laneVias = new RdLaneViaSelector(this.getConn())
					.loadRdLaneViaByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setLaneVias(laneVias);

			// 获取link上的点限速
			List<RdSpeedlimit> limits = new RdSpeedlimitSelector(this.getConn())
					.loadSpeedlimitByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setSpeedlimits(limits);

			// 获取以改LINK作为分歧进入线的分歧

			List<RdBranch> inBranchs = new RdBranchSelector(this.getConn())
					.loadRdBranchByInLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setInBranchs(inBranchs);

			// 获取已该LINK作为分歧退出线的分歧

			List<RdBranch> outBranchs = new RdBranchSelector(this.getConn())
					.loadRdBranchByOutLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setOutBranchs(outBranchs);

			// 获取该LINK为分歧经过线的BRANCH_VIA

			List<List<RdBranchVia>> branchVias = new RdBranchViaSelector(this.getConn())
					.loadRdBranchViaByLinkPid(this.getCommand().getLinkPid(), true);

			this.getCommand().setBranchVias(branchVias);

			// 获取由该link组成的立交（RDGSC）
			RdGscSelector selector = new RdGscSelector(this.getConn());

			List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "RD_LINK", true);

			this.getCommand().setRdGscs(rdGscList);

			// 获取由该link作为关联link的行政区划代表点
			AdAdminSelector adSelector = new AdAdminSelector(this.getConn());

			List<AdAdmin> adAdminList = adSelector.loadRowsByLinkId(this.getCommand().getLinkPid(), true);

			this.getCommand().setAdAdmins(adAdminList);

			return true;

		} catch (SQLException e) {

			throw e;
		}

	}

	public String runNotCommit() throws Exception {
		String msg;
		try {
			this.getConn().setAutoCommit(false);
			this.prepareData();
			String preCheckMsg = this.preCheck();
			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			IOperation operation = null;
			operation = new OpTopo(this.getCommand(), this.getConn(), this.rdLinkBreakpoint, jaDisplayLink);
			msg = operation.run(this.getResult());
			OpRefRestrict opRefRestrict = new OpRefRestrict(this.getCommand());
			opRefRestrict.run(this.getResult());
			OpRefBranch opRefBranch = new OpRefBranch(this.getCommand());
			opRefBranch.run(this.getResult());
			OpRefLaneConnexity opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand());
			opRefLaneConnexity.run(this.getResult());
			OpRefSpeedlimit opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
			opRefSpeedlimit.run(this.getResult());
			OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand(), this.getConn());
			opRefRdGsc.run(this.getResult());
			OpRefAdAdmin opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
			opRefAdAdmin.run(this.getResult());
			this.recordData();
			this.postCheck();
			// conn.commit();
		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}
		return msg;
	}

	@Override
	public String run() throws Exception {

		String msg;
		try {
			if (!this.getCommand().isCheckInfect()) {
				this.getConn().setAutoCommit(false);
				this.prepareData();
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				IOperation operation = null;
				operation = new OpTopo(this.getCommand(), this.getConn(), this.rdLinkBreakpoint, jaDisplayLink);
				msg = operation.run(this.getResult());
				OpRefRestrict opRefRestrict = new OpRefRestrict(this.getCommand());
				opRefRestrict.run(this.getResult());
				OpRefBranch opRefBranch = new OpRefBranch(this.getCommand());
				opRefBranch.run(this.getResult());
				OpRefLaneConnexity opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand());
				opRefLaneConnexity.run(this.getResult());
				OpRefSpeedlimit opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
				opRefSpeedlimit.run(this.getResult());
				OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand(), this.getConn());
				opRefRdGsc.run(this.getResult());
				OpRefAdAdmin opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
				opRefAdAdmin.run(this.getResult());
				this.recordData();
				this.postCheck();
				this.getConn().commit();
			} else {
				Map<String, List<Integer>> infects = new HashMap<String, List<Integer>>();

				List<List<RdBranchVia>> branchVias = this.getCommand().getBranchVias();

				List<Integer> infectList = new ArrayList<Integer>();

				for (List<RdBranchVia> listVias : branchVias) {
					for (RdBranchVia via : listVias) {
						infectList.add(via.getLinkPid());
					}
				}

				infects.put("RDBRANCHVIA", infectList);

				infectList = new ArrayList<Integer>();

				for (RdBranch branch : this.getCommand().getInBranchs()) {
					infectList.add(branch.getPid());
				}

				for (RdBranch branch : this.getCommand().getOutBranchs()) {
					infectList.add(branch.getPid());
				}

				infects.put("RDBRANCH", infectList);

				infectList = new ArrayList<Integer>();

				for (RdLaneConnexity laneConn : this.getCommand().getLaneConnextys()) {
					infectList.add(laneConn.getPid());
				}

				infects.put("RDLANECONNEXITY", infectList);

				infectList = new ArrayList<Integer>();

				for (RdLaneTopology topo : this.getCommand().getLaneTopologys()) {
					infectList.add(topo.getPid());
				}

				infects.put("RDLANETOPOLOGY", infectList);

				infectList = new ArrayList<Integer>();

				for (List<Entry<Integer, RdLaneVia>> listVias : this.getCommand().getLaneVias()) {
					for (Entry<Integer, RdLaneVia> entry : listVias) {
						infectList.add(entry.getKey());
					}
				}

				infects.put("RDLANEVIA", infectList);

				infectList = new ArrayList<Integer>();

				for (RdSpeedlimit limit : this.getCommand().getSpeedlimits()) {
					infectList.add(limit.getPid());
				}

				infects.put("RDSPEEDLIMIT", infectList);

				infectList = new ArrayList<Integer>();

				for (RdRestriction res : this.getCommand().getRestrictions()) {
					infectList.add(res.getPid());
				}

				infects.put("RDRESTRICTION", infectList);

				infectList = new ArrayList<Integer>();

				for (RdRestrictionDetail detail : this.getCommand().getRestrictionDetails()) {
					infectList.add(detail.getPid());
				}

				infects.put("RDRESTRICTIONDETAIL", infectList);

				infectList = new ArrayList<Integer>();

				for (List<Entry<Integer, RdRestrictionVia>> vias : this.getCommand().geListRestrictVias()) {
					for (Entry<Integer, RdRestrictionVia> entry : vias) {
						infectList.add(entry.getKey());
					}
				}

				infects.put("RDRESTRICTIONVIA", infectList);

				infectList = new ArrayList<Integer>();

				for (RdGsc rdGsc : this.getCommand().getRdGscs()) {
					infectList.add(rdGsc.getPid());
				}

				infects.put("RDGSC", infectList);

				infectList = new ArrayList<Integer>();

				for (AdAdmin adAdmin : this.getCommand().getAdAdmins()) {
					infectList.add(adAdmin.getPid());
				}

				infects.put("ADADMIN", infectList);

				msg = JSONObject.fromObject(infects).toString();

			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		} finally {
			try {
				this.getConn().close();
			} catch (Exception e) {

			}
		}

		return msg;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkIsCrossLink(this.getConn(), this.getCommand().getLinkPid());

		Point breakPoint = this.getCommand().getPoint();

		int lon = (int) (breakPoint.getX() * 100000);

		int lat = (int) (breakPoint.getY() * 100000);

		Coordinate[] cs = rdLinkBreakpoint.getGeometry().getCoordinates();

		if (cs[0].x == lon && cs[0].y == lat) {
			return "不能在端点进行打断";
		}

		if (cs[cs.length - 1].x == lon && cs[cs.length - 1].y == lat) {
			return "不能在端点进行打断";
		}

		return null;
	}

	@Override
	public String exeOperation() {
		// TODO Auto-generated method stub
		return null;
	}

}
