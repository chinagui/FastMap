package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gate.RdGateSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.hgwg.RdHgwgLimitSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneTopologySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

	private RdLink rdLinkBreakpoint;

	private JSONArray jaDisplayLink;

	public Process(AbstractCommand command) throws Exception {
		super(command);

		this.jaDisplayLink = new JSONArray();
	}

	public Process(Command command, Connection conn, Result result)
			throws Exception {
		super();
		this.setCommand(command);
		// 初始化检查参数
		this.initCheckCommand();
		this.setConn(conn);
		this.setResult(result);

		this.jaDisplayLink = new JSONArray();
	}

	@Override
	public boolean prepareData() throws Exception {

		try {
			RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

			this.rdLinkBreakpoint = (RdLink) linkSelector.loadById(this
					.getCommand().getLinkPid(), true);

			this.getCommand().setBreakLink(rdLinkBreakpoint);

			this.getResult().insertObject(rdLinkBreakpoint, ObjStatus.DELETE,
					rdLinkBreakpoint.pid());

			RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());

			RdNode sNode = (RdNode) nodeSelector.loadById(
					rdLinkBreakpoint.getsNodePid(), true);

			this.getCommand().setsNode(sNode);

			RdNode eNode = (RdNode) nodeSelector.loadById(
					rdLinkBreakpoint.geteNodePid(), true);

			this.getCommand().seteNode(eNode);

			// 获取此LINK上交限进入线
			List<RdRestriction> restrictions = new RdRestrictionSelector(
					this.getConn()).loadRdRestrictionByLinkPid(this
					.getCommand().getLinkPid(), true);

			this.getCommand().setRestrictions(restrictions);

			// 获取此LINK上交限退出线
			List<RdRestrictionDetail> details = new RdRestrictionDetailSelector(
					this.getConn()).loadDetailsByLinkPid(this.getCommand()
					.getLinkPid(), true);

			this.getCommand().setRestrictionDetails(details);

			// 获取LINK上交限经过线
			List<List<Entry<Integer, RdRestrictionVia>>> restrictVias = new RdRestrictionViaSelector(
					this.getConn()).loadRestrictionViaByLinkPid(this
					.getCommand().getLinkPid(), true);

			this.getCommand().setRestrictListVias(restrictVias);

			// 获取此LINK上车信进入线
			List<RdLaneConnexity> laneConnexitys = new RdLaneConnexitySelector(
					this.getConn()).loadRdLaneConnexityByLinkPid(this
					.getCommand().getLinkPid(), true);

			this.getCommand().setLaneConnexitys(laneConnexitys);

			// 获取此LINK上车信退出线
			List<RdLaneTopology> topos = new RdLaneTopologySelector(
					this.getConn()).loadToposByLinkPid(this.getCommand()
					.getLinkPid(), true);

			this.getCommand().setLaneTopologys(topos);

			// 获取LINK上车信经过线
			List<List<Entry<Integer, RdLaneVia>>> laneVias = new RdLaneViaSelector(
					this.getConn()).loadRdLaneViaByLinkPid(this.getCommand()
					.getLinkPid(), true);

			this.getCommand().setLaneVias(laneVias);

			// 获取link上的点限速
			List<RdSpeedlimit> limits = new RdSpeedlimitSelector(this.getConn())
					.loadSpeedlimitByLinkPid(this.getCommand().getLinkPid(),
							true);

			this.getCommand().setSpeedlimits(limits);

			// 获取以改LINK作为分歧进入线的分歧

			List<RdBranch> inBranchs = new RdBranchSelector(this.getConn())
					.loadRdBranchByInLinkPid(this.getCommand().getLinkPid(),
							true);

			this.getCommand().setInBranchs(inBranchs);

			// 获取已该LINK作为分歧退出线的分歧

			List<RdBranch> outBranchs = new RdBranchSelector(this.getConn())
					.loadRdBranchByOutLinkPid(this.getCommand().getLinkPid(),
							true);

			this.getCommand().setOutBranchs(outBranchs);

			// 获取该LINK为分歧经过线的BRANCH_VIA

			List<List<RdBranchVia>> branchVias = new RdBranchViaSelector(
					this.getConn()).loadRdBranchViaByLinkPid(this.getCommand()
					.getLinkPid(), true);

			this.getCommand().setBranchVias(branchVias);

			// 获取由该link组成的立交（RDGSC）
			RdGscSelector selector = new RdGscSelector(this.getConn());

			List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this
					.getCommand().getLinkPid(), "RD_LINK", true);

			this.getCommand().setRdGscs(rdGscList);

			// 获取由该link作为关联link的行政区划代表点
			AdAdminSelector adSelector = new AdAdminSelector(this.getConn());

			List<AdAdmin> adAdminList = adSelector.loadRowsByLinkId(this
					.getCommand().getLinkPid(), true);

			this.getCommand().setAdAdmins(adAdminList);

			// 获取由该link作为关联link的电子眼
			RdElectroniceyeSelector eleceyeSelector = new RdElectroniceyeSelector(
					this.getConn());
			List<RdElectroniceye> eleceyes = eleceyeSelector
					.loadListByRdLinkId(this.getCommand().getLinkPid(), true);
			this.getCommand().setEleceyes(eleceyes);

			// 获取由该link作为关联link的大门
			RdGateSelector gateSelector = new RdGateSelector(this.getConn());
			List<RdGate> gates = gateSelector.loadByLink(this.getCommand()
					.getLinkPid(), true);
			this.getCommand().setGates(gates);

			// 获取该link关联的分叉口提示
			RdSeSelector rdSeSelector = new RdSeSelector(this.getConn());
			List<RdSe> rdSes = rdSeSelector.loadRdSesWithLinkPid(this
					.getCommand().getLinkPid(), true);
			this.getCommand().setRdSes(rdSes);

			// 获取该link关联的减速带
			RdSpeedbumpSelector speedbumpSelector = new RdSpeedbumpSelector(
					this.getConn());
			List<RdSpeedbump> speedbumps = speedbumpSelector.loadByLinkPid(this
					.getCommand().getLinkPid(), true);
			this.getCommand().setRdSpeedbumps(speedbumps);

			// 获取该link关联的收费站
			RdTollgateSelector tollgateSelector = new RdTollgateSelector(
					this.getConn());
			List<RdTollgate> tollgates = tollgateSelector
					.loadRdTollgatesWithLinkPid(this.getCommand().getLinkPid(),
							true);
			this.getCommand().setRdTollgates(tollgates);

			// 获取该link关联的限高限重
			RdHgwgLimitSelector hgwgLimitSelector = new RdHgwgLimitSelector(
					this.getConn());
			List<RdHgwgLimit> hgwgLimits = hgwgLimitSelector.loadByLinkPid(this
					.getCommand().getLinkPid(), true);
			this.getCommand().setRdHgwgLimits(hgwgLimits);

			// 获取该link关联的里程桩
			RdMileagepileSelector mileagepileSelector = new RdMileagepileSelector(
					this.getConn());
			List<RdMileagepile> mileagepiles = mileagepileSelector
					.loadByLinkPid(this.getCommand().getLinkPid(), true);
			this.getCommand().setRdMileagepiles(mileagepiles);

			return true;

		} catch (SQLException e) {

			throw e;
		}

	}

	public String innerRun() throws Exception {
		String msg;
		try {

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = null;
			operation = new OpTopo(this.getCommand(), this.getConn(),
					this.rdLinkBreakpoint, jaDisplayLink);
			msg = operation.run(this.getResult());

			updataRelationObj();

			this.postCheck();
		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}
		return msg;
	}

	public List<RdLink> getInnerRun() throws Exception {
		try {

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = null;
			operation = new OpTopo(this.getCommand(), this.getConn(),
					this.rdLinkBreakpoint, jaDisplayLink);
			operation.run(this.getResult());

			updataRelationObj();

			this.postCheck();
		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}
		return this.getCommand().getNewLinks();
	}

	@Override
	public String run() throws Exception {
		log.info("START BEGIN BREAK RDLINK");
		long startTime = System.currentTimeMillis();

		String msg;
		try {
			if (!this.getCommand().isCheckInfect()) {
				this.getConn().setAutoCommit(false);
				log.info("PREPARE DATA BEGIN");
				this.prepareData();
				long preDataTime = System.currentTimeMillis();
				log.info("PREPARE DATA END");
				log.info("PREPARE DATA USE TIME   "
						+ String.valueOf(preDataTime - startTime));

				log.info("PREPARE CHECK BEGIN");
				String preCheckMsg = this.preCheck();
				long prCheckTime = System.currentTimeMillis();
				log.info("PREPARE CHECK END");
				log.info("PREPARE CHECK USE TIME   "
						+ String.valueOf(prCheckTime - preDataTime));
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				log.info("START MAIN RDLINK BREAK");
				IOperation operation = null;

				operation = new OpTopo(this.getCommand(), this.getConn(),
						this.rdLinkBreakpoint, jaDisplayLink);

				msg = operation.run(this.getResult());
				long mainRDLinkTime = System.currentTimeMillis();
				log.info("END MAIN RDLINK BREAK");
				log.info("MAIN RDLINK BREAK USE TIME  "
						+ String.valueOf(mainRDLinkTime - prCheckTime));
				log.info("START ATTRRELATION ");
				updataRelationObj();
				long attrrelationTime = System.currentTimeMillis();
				log.info("END  ATTRRELATION ");
				log.info("MAIN ATTRRELATION USE TIME  "
						+ String.valueOf(attrrelationTime - mainRDLinkTime));

				log.info("START RECORD  ");
				// 设置主pid值（primary key），用于web显示对应的node的属性面板
				if (!this.getCommand().getOperType().equals(OperType.DELETE)
						&& !this.getCommand().getObjType()
								.equals(ObjType.RDBRANCH)
						&& !this.getCommand().getObjType()
								.equals(ObjType.RDELECEYEPAIR)
						&& !this.getCommand().getObjType()
								.equals(ObjType.LUFACE)
						&& !this.getCommand().getObjType()
								.equals(ObjType.LCFACE)) {
					handleResult(this.getCommand().getObjType(), this
							.getCommand().getOperType(), this.getResult());
				}
				this.recordData();
				long recordTime = System.currentTimeMillis();
				log.info("END RECORD  ");
				log.info("RECODE USE TIME  "
						+ String.valueOf(recordTime - attrrelationTime));
				log.info("POST CHECK BEGIN");
				this.postCheck();
				log.info("POST CHECK BEGIN");
				this.getConn().commit();
				log.info("END BEGIN BREAK RDLINK");
				log.info("BREAK RDLINK TOTAL USE TIME   "
						+ String.valueOf(System.currentTimeMillis() - startTime));
			} else {

				this.prepareData();

				Map<String, List<Integer>> infects = new HashMap<>();

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

		if (this.getCommand().getPoint() != null) {
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
		}
		return super.preCheck();
	}

	@Override
	public String exeOperation() {
		return null;
	}

	/**
	 * 维护关联要素
	 * 
	 * @throws Exception
	 */
	private void updataRelationObj() throws Exception {

		long startRelationObjTime = System.currentTimeMillis();
		// 打断linkpid
		int oldLinkPid = this.getCommand().getLinkPid();

		OpRefRelationObj opRefRelationObj = new OpRefRelationObj(this.getConn());

		// 交限
		opRefRelationObj.handleRdRestriction(this.getResult(),
				this.rdLinkBreakpoint, this.getCommand().getNewLinks());

		long refRestrictTime = System.currentTimeMillis();
		log.info(" 交限 USE TIME  "
				+ String.valueOf(refRestrictTime - startRelationObjTime));
		// 分歧
		opRefRelationObj.handleRdBranch(this.getResult(),
				this.rdLinkBreakpoint, this.getCommand().getNewLinks());
		long refBranchTime = System.currentTimeMillis();
		log.info(" 分歧 USE TIME  "
				+ String.valueOf(refBranchTime - refRestrictTime));
		// 车信
		opRefRelationObj.handleRdLaneconnexity(this.getResult(),
				this.rdLinkBreakpoint, this.getCommand().getNewLinks());
		long refLaneConnexityTime = System.currentTimeMillis();
		log.info("车信USE TIME  "
				+ String.valueOf(refLaneConnexityTime - refBranchTime));
		// 限速
		OpRefSpeedlimit opRefSpeedlimit = new OpRefSpeedlimit(this.getCommand());
		opRefSpeedlimit.run(this.getResult());
		long refSpeedlimitTime = System.currentTimeMillis();
		log.info("限速USE TIME  "
				+ String.valueOf(refSpeedlimitTime - refLaneConnexityTime));
		// 立交
		OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand(),
				this.getConn());
		opRefRdGsc.run(this.getResult());
		long refRdGscTime = System.currentTimeMillis();
		log.info("立交 USE TIME  "
				+ String.valueOf(refRdGscTime - refSpeedlimitTime));

		// 行政区划
		OpRefAdAdmin opRefAdAdmin = new OpRefAdAdmin(this.getCommand());
		opRefAdAdmin.run(this.getResult());
		long refAdAdminTime = System.currentTimeMillis();
		log.info("立行政区划 USE TIME  "
				+ String.valueOf(refAdAdminTime - refRdGscTime));
		// 警示信息
		OpRefRdWarninginfo opRefRdWarninginfo = new OpRefRdWarninginfo(
				this.getConn());
		opRefRdWarninginfo.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());
		long refRdWarninginfoTime = System.currentTimeMillis();
		log.info("警示信息 USE TIME  "
				+ String.valueOf(refRdWarninginfoTime - refAdAdminTime));
		// 信号灯
		OpRefRdTrafficsignal ofOpRefRdTrafficsignal = new OpRefRdTrafficsignal(
				this.getConn());
		ofOpRefRdTrafficsignal.run(this.getResult(), oldLinkPid, this
				.getCommand().getNewLinks());
		long refRdTrafficsignalTime = System.currentTimeMillis();
		log.info("信号灯USE TIME  "
				+ String.valueOf(refRdTrafficsignalTime - refRdWarninginfoTime));

		// 电子眼
		OpRefRdElectroniceye opRefRdElectroniceye = new OpRefRdElectroniceye(
				this.getConn());
		opRefRdElectroniceye.run(this.getResult(), oldLinkPid, this
				.getCommand().getNewLinks());
		long refRdElectroniceyeTime = System.currentTimeMillis();
		log.info("电子眼 USE TIME  "
				+ String.valueOf(refRdElectroniceyeTime
						- refRdTrafficsignalTime));
		// 大门
		OpRefRdGate opRefRdGate = new OpRefRdGate(this.getConn());
		opRefRdGate.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());
		long refRdGateTime = System.currentTimeMillis();
		log.info("大门 USE TIME  "
				+ String.valueOf(refRdGateTime - refRdElectroniceyeTime));

		// 分岔路提示
		OpRefRdSe opRefRdSe = new OpRefRdSe(this.getConn());
		opRefRdSe.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());

		long refRdSeTime = System.currentTimeMillis();
		log.info("分岔路提示USE TIME  "
				+ String.valueOf(refRdSeTime - refRdGateTime));
		// 减速带
		OpRefRdSpeedbum opRefRdSpeedbum = new OpRefRdSpeedbum(this.getConn());
		opRefRdSpeedbum.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());
		long refRdSpeedbumTime = System.currentTimeMillis();
		log.info("减速带USE TIME  "
				+ String.valueOf(refRdSpeedbumTime - refRdSeTime));
		// 坡度
		OpRefRdSlope opRefRdSlope = new OpRefRdSlope(this.getConn());
		opRefRdSlope.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());
		long refRdSlopeTime = System.currentTimeMillis();
		log.info("坡度USE TIME  "
				+ String.valueOf(refRdSlopeTime - refRdSpeedbumTime));

		// 顺行
		opRefRelationObj.handleRdDirectroute(this.getResult(),
				this.rdLinkBreakpoint, this.getCommand().getNewLinks());
		long refRdDirectrouteTime = System.currentTimeMillis();
		log.info("顺行USE TIME  "
				+ String.valueOf(refRdDirectrouteTime - refRdSlopeTime));
		// CRF交叉点
		OpRefRdInter opRefRdInter = new OpRefRdInter(this.getConn());
		opRefRdInter.run(this.getResult(), this.rdLinkBreakpoint, this
				.getCommand().getNewLinks());

		long refRdInterTime = System.currentTimeMillis();
		log.info("CRF交叉点USE TIME  "
				+ String.valueOf(refRdInterTime - refRdDirectrouteTime));
		// CRF道路
		OpRefRdRoad opRefRdRoad = new OpRefRdRoad(this.getConn());
		opRefRdRoad.run(this.getResult(), this.rdLinkBreakpoint, this
				.getCommand().getNewLinks());
		long refRdRoadTime = System.currentTimeMillis();
		log.info("CRF道路USE TIME  "
				+ String.valueOf(refRdRoadTime - refRdInterTime));
		// CRF对象
		OpRefRdObject opRefRdObject = new OpRefRdObject(this.getConn());
		opRefRdObject.run(this.getResult(), this.rdLinkBreakpoint, this
				.getCommand().getNewLinks());

		long refRdObjectTime = System.currentTimeMillis();
		log.info("CRF对象USE TIME  "
				+ String.valueOf(refRdObjectTime - refRdRoadTime));
		// 收费站
		OpRefRdTollgate opRefRdTollgate = new OpRefRdTollgate(this.getConn());
		opRefRdTollgate.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());
		long refRdTollgateTime = System.currentTimeMillis();
		log.info("收费站USE TIME  "
				+ String.valueOf(refRdTollgateTime - refRdObjectTime));
		// 语音引导
		opRefRelationObj.handleRdVoiceguide(this.getResult(),
				this.rdLinkBreakpoint, this.getCommand().getNewLinks());
		long refRdVoiceguideTime = System.currentTimeMillis();
		log.info("语音引导USE TIME  "
				+ String.valueOf(refRdVoiceguideTime - refRdTollgateTime));
		// 可变限速

		OpRefRdVariableSpeed opRefRdVariableSpeed = new OpRefRdVariableSpeed(
				this.getConn());
		opRefRdVariableSpeed.run(this.getResult(), this.rdLinkBreakpoint, this
				.getCommand().getNewLinks());
		long refRdVariableSpeedTime = System.currentTimeMillis();
		log.info("可变限速USE TIME  "
				+ String.valueOf(refRdVariableSpeedTime - refRdVoiceguideTime));
		// POI被动维护
		OpRefPoi opRefPoi = new OpRefPoi(this.getConn());
		opRefPoi.run(this.getResult(), this.rdLinkBreakpoint, this.getCommand()
				.getNewLinks());
		long refPoiTime = System.currentTimeMillis();
		log.info("POI被动维护USE TIME  "
				+ String.valueOf(refPoiTime - refRdVariableSpeedTime));

		// 同一点同一线维护
		if (!this.getCommand().getOperationType().equals("sameLinkRepair")) {

			opRefRelationObj.handleSameLink(this.rdLinkBreakpoint,
					this.getCommand(), this.getResult());
		}
		long refSameTime = System.currentTimeMillis();
		log.info("同一点同一线维护USE TIME  "
				+ String.valueOf(refSameTime - refPoiTime));

		// 限高限重维护
		OpRefRdHgwgLimit refRdHgwgLimit = new OpRefRdHgwgLimit(getConn());
		refRdHgwgLimit.run(getResult(), getCommand().getLinkPid(), getCommand()
				.getNewLinks());
		long refHgwgTime = System.currentTimeMillis();
		log.info("限高限重USE TIME  " + String.valueOf(refHgwgTime - refSameTime));

		// 维护交叉口类link打断
		OpRefRdCross rdOpRefRdCross = new OpRefRdCross(getCommand(), getConn());
		rdOpRefRdCross.run(getResult());
		long refRdCrossTime = System.currentTimeMillis();
		log.info("交叉口USE TIME  " + String.valueOf(refRdCrossTime - refHgwgTime));
		// 维护详细车道信息
		OpRefRdLane opRefRdLane = new OpRefRdLane(this.getConn());
		opRefRdLane.run(this.getResult(), this.getCommand().getBreakLink(),
				this.getCommand().getNewLinks());
		long refRdLaneTime = System.currentTimeMillis();
		log.info("详细车道USE TIME    "
				+ String.valueOf(refRdLaneTime - refRdCrossTime));
		// 维护详细车道信息
		OpRefRdMileagepile opRefRdMileagepile = new OpRefRdMileagepile(
				this.getConn());
		opRefRdMileagepile.run(this.getResult(), oldLinkPid, this.getCommand()
				.getNewLinks());
		long refMileageTime = System.currentTimeMillis();
		log.info("里程桩USE TIME    "
				+ String.valueOf(refMileageTime - refRdLaneTime));
		OpRefRdTmcLocationLink opRefRdTmcLocationLink = new OpRefRdTmcLocationLink(
				this.getConn());
		opRefRdTmcLocationLink.run(this.getResult(), this.getCommand()
				.getBreakLink(), this.getCommand().getNewLinks());
		long refTmcLinkTime = System.currentTimeMillis();
		log.info("TMC USE TIME    "
				+ String.valueOf(refRdLaneTime - refTmcLinkTime));
	}

	public static void main(String[] args) {
		long l1 = System.currentTimeMillis();
		System.out.println(l1);

	}
}
