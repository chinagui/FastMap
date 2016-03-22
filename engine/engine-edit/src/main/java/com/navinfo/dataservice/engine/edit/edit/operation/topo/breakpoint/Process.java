package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneTopologySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private RdLink rdLinkBreakpoint;

	private JSONArray jaDisplayLink;

	private String postCheckMsg;
	
	private Check check = new Check();

	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
				.getProjectId());

		this.jaDisplayLink = new JSONArray();
	}
	
	public Process(ICommand command, Connection conn) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = conn;

		this.jaDisplayLink = new JSONArray();
	}

	@Override
	public ICommand getCommand() {

		return command;
	}

	@Override
	public Result getResult() {

		return result;
	}

	@Override
	public boolean prepareData() throws Exception {

		try {
			RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

			this.rdLinkBreakpoint = (RdLink) linkSelector.loadById(
					command.getLinkPid(), true);

			result.insertObject(rdLinkBreakpoint, ObjStatus.DELETE);

			RdNodeSelector nodeSelector = new RdNodeSelector(conn);

			RdNode sNode = (RdNode) nodeSelector.loadById(
					rdLinkBreakpoint.getsNodePid(), true);

			command.setsNode(sNode);

			RdNode eNode = (RdNode) nodeSelector.loadById(
					rdLinkBreakpoint.geteNodePid(), true);

			command.seteNode(eNode);

			// 获取此LINK上交限进入线
			List<RdRestriction> restrictions = new RdRestrictionSelector(conn)
					.loadRdRestrictionByLinkPid(command.getLinkPid(), true);

			command.setRestrictions(restrictions);

			// 获取此LINK上交限退出线
			List<RdRestrictionDetail> details = new RdRestrictionDetailSelector(
					conn).loadDetailsByLinkPid(command.getLinkPid(), true);

			command.setRestrictionDetails(details);

			// 获取LINK上交限经过线
			List<List<RdRestrictionVia>> restrictVias = new RdRestrictionViaSelector(
					conn).loadRestrictionViaByLinkPid(command.getLinkPid(),
					true);
			
			command.setRestrictListVias(restrictVias);

			// 获取此LINK上车信进入线
			List<RdLaneConnexity> laneConnexitys = new RdLaneConnexitySelector(conn)
					.loadRdLaneConnexityByLinkPid(command.getLinkPid(), true);

			command.setLaneConnexitys(laneConnexitys);

			// 获取此LINK上车信退出线
			List<RdLaneTopology> topos = new RdLaneTopologySelector(
					conn).loadToposByLinkPid(command.getLinkPid(), true);

			command.setLaneTopologys(topos);

			// 获取LINK上车信经过线
			List<List<RdLaneVia>> laneVias = new RdLaneViaSelector(
					conn).loadRdLaneViaByLinkPid(command.getLinkPid(),
					true);
			
			command.setLaneVias(laneVias);
			
			//获取link上的点限速
			List<RdSpeedlimit> limits = new RdSpeedlimitSelector
					(conn).loadSpeedlimitByLinkPid(command.getLinkPid(), true);
			
			command.setSpeedlimits(limits);
			
			//获取以改LINK作为分歧进入线的分歧
			
			List<RdBranch> inBranchs = new RdBranchSelector(conn).loadRdBranchByInLinkPid(command.getLinkPid(), true);
			
			command.setInBranchs(inBranchs);
			
			//获取已该LINK作为分歧退出线的分歧
			
			List<RdBranch> outBranchs = new RdBranchSelector(conn).loadRdBranchByOutLinkPid(command.getLinkPid(), true);
			
			command.setOutBranchs(outBranchs);
			
			//获取该LINK为分歧经过线的BRANCH_VIA
			
			List<List<RdBranchVia>> branchVias = new RdBranchViaSelector(conn).loadRdBranchViaByLinkPid(command.getLinkPid(), true);
			
			command.setBranchVias(branchVias);
			
			if (command.getBreakNodePid() != 0){
				
				RdNode breakNode = (RdNode) nodeSelector.loadById(command.getBreakNodePid(), true);
				
				command.setBreakNode(breakNode);
			}

			return true;

		} catch (SQLException e) {

			throw e;
		}

	}
	
	public String runNotCommit() throws Exception {
		String msg;
		try {
				conn.setAutoCommit(false);
				this.prepareData();
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				IOperation operation = null;
				if (command.getBreakNodePid() == 0) {
					operation = new OpTopo(command, conn,
							this.rdLinkBreakpoint, jaDisplayLink);
				} else {
					RdNode breakNode = (RdNode) new RdNodeSelector(conn)
							.loadById(command.getBreakNodePid(), true);

					operation = new OpTopo(command, conn,
							this.rdLinkBreakpoint, jaDisplayLink, breakNode);
				}
				msg = operation.run(result);
				OpRefRestrict opRefRestrict = new OpRefRestrict(command);
				opRefRestrict.run(result);
				OpRefBranch opRefBranch = new OpRefBranch(command);
				opRefBranch.run(result);
				OpRefLaneConnexity opRefLaneConnexity = new OpRefLaneConnexity(
						command);
				opRefLaneConnexity.run(result);
				OpRefSpeedlimit opRefSpeedlimit = new OpRefSpeedlimit(command);
				opRefSpeedlimit.run(result);
				this.recordData();
				this.postCheck();
//				conn.commit();
		}
		catch (Exception e) {

			conn.rollback();

			throw e;
		}
		return msg;
	}

	@Override
	public String run() throws Exception {

		String msg;
		try {
			if (!command.isCheckInfect()) {
				conn.setAutoCommit(false);
				this.prepareData();
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				IOperation operation = null;
				if (command.getBreakNodePid() == 0) {
					operation = new OpTopo(command, conn,
							this.rdLinkBreakpoint, jaDisplayLink);
				} else {
					RdNode breakNode = (RdNode) new RdNodeSelector(conn)
							.loadById(command.getBreakNodePid(), true);

					operation = new OpTopo(command, conn,
							this.rdLinkBreakpoint, jaDisplayLink, breakNode);
				}
				msg = operation.run(result);
				OpRefRestrict opRefRestrict = new OpRefRestrict(command);
				opRefRestrict.run(result);
				OpRefBranch opRefBranch = new OpRefBranch(command);
				opRefBranch.run(result);
				OpRefLaneConnexity opRefLaneConnexity = new OpRefLaneConnexity(
						command);
				opRefLaneConnexity.run(result);
				OpRefSpeedlimit opRefSpeedlimit = new OpRefSpeedlimit(command);
				opRefSpeedlimit.run(result);
				this.recordData();
				this.postCheck();
				conn.commit();
			}else{
				Map<String,List<Integer>> infects = new HashMap<String,List<Integer>>();
				
				List<List<RdBranchVia>> branchVias = command.getBranchVias();
				
				List<Integer> infectList = new ArrayList<Integer>();
				
				for(List<RdBranchVia> listVias: branchVias){
					for(RdBranchVia via : listVias){
						infectList.add(via.getLinkPid());
					}
				}
				
				infects.put("RDBRANCHVIA", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdBranch branch: command.getInBranchs()){
					infectList.add(branch.getPid());
				}
				
				for(RdBranch branch: command.getOutBranchs()){
					infectList.add(branch.getPid());
				}
				
				infects.put("RDBRANCH", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdLaneConnexity laneConn: command.getLaneConnextys()){
					infectList.add(laneConn.getPid());
				}
				
				infects.put("RDLANECONNEXITY", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdLaneTopology topo: command.getLaneTopologys()){
					infectList.add(topo.getPid());
				}
				
				infects.put("RDLANETOPOLOGY", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(List<RdLaneVia> listVias: command.getLaneVias()){
					for(RdLaneVia via : listVias){
						infectList.add(via.getLinkPid());
					}
				}
				
				infects.put("RDLANEVIA", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdSpeedlimit limit : command.getSpeedlimits()){
					infectList.add(limit.getPid());
				}
				
				infects.put("RDSPEEDLIMIT", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdRestriction res: command.getRestrictions()){
					infectList.add(res.getPid());
				}
				
				infects.put("RDRESTRICTION", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(RdRestrictionDetail detail: command.getRestrictionDetails()){
					infectList.add(detail.getPid());
				}
				
				infects.put("RDRESTRICTIONDETAIL", infectList);
				
				infectList = new ArrayList<Integer>();
				
				for(List<RdRestrictionVia> vias: command.geListRestrictVias()){
					for(RdRestrictionVia via : vias){
						infectList.add(via.getLinkPid());
					}
				}
				
				infects.put("RDRESTRICTIONVIA", infectList);
				
				msg = JSONObject.fromObject(infects).toString();
				
			}

		} catch (Exception e) {

			conn.rollback();

			throw e;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {

			}
		}

		return msg;
	}

	@Override
	public boolean recordData() throws Exception {

		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;
	}

	@Override
	public String preCheck() throws Exception {
		
		check.checkIsCrossLink(conn, command.getLinkPid());

		Point breakPoint = command.getPoint();

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
	public void postCheck() throws Exception {

		// 对数据进行检查、检查结果存储在数据库，并存储在临时变量postCheckMsg中
	}

	@Override
	public String getPostCheck() throws Exception {

		return postCheckMsg;
	}

}
