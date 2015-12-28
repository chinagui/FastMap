package com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.FosEngine.edit.log.LogWriter;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity.RdLaneTopologySelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity.RdLaneViaSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionDetailSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionViaSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.FosEngine.edit.operation.IProcess;
import com.navinfo.dataservice.FosEngine.edit.operation.OperatorFactory;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private RdLink rdLinkBreakpoint;

	private JSONArray jaDisplayLink;

	private String postCheckMsg;

	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
				.getProjectId());

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

			return true;

		} catch (SQLException e) {

			throw e;
		}

	}

	@Override
	public String run() throws Exception {

		String msg;
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new OpTopo(command, conn,
					this.rdLinkBreakpoint, jaDisplayLink);

			msg = operation.run(result);

			OpRefRestrict opRefRes = new OpRefRestrict(command);

			opRefRes.run(result);

			this.recordData();

			this.postCheck();

			conn.commit();

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
