package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.OpRefRelationObj;

import net.sf.json.JSONObject;

/**
 * 详细车道批量操作
 * 
 * @author 赵凯凯
 * 
 */
public class Operation implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;
	private Connection conn;
	private RdLinkForm form;
	private RdLinkLimit limit;
	private RdLink link;
	private RdLaneConnexity connexity;
	private List<String> lanInfos;
	private int passageNum;
	private int linkDirect;
	// 车道的方向
	private int laneDirect;

	public int getLaneDirect() {
		return laneDirect;
	}

	public void setLaneDirect(int laneDirect) {
		this.laneDirect = laneDirect;
	}

	public int getLinkDirect() {
		return linkDirect;
	}

	public void setLinkDirect(int linkDirect) {
		this.linkDirect = linkDirect;
	}

	public int getKindFlag() {
		return kindFlag;
	}

	private RdTollgate tollgate;

	public RdTollgate getTollgate() {
		return tollgate;
	}

	public void setTollgate(RdTollgate tollgate) {
		this.tollgate = tollgate;
	}

	public int getPassageNum() {
		return passageNum;
	}

	public void setPassageNum(int passageNum) {
		this.passageNum = passageNum;
	}

	public List<String> getLanInfos() {
		return lanInfos;
	}

	public void setLanInfos(List<String> lanInfos) {
		this.lanInfos = lanInfos;
	}

	public RdLaneConnexity getConnexity() {
		return connexity;
	}

	public void setConnexity(RdLaneConnexity connexity) {
		this.connexity = connexity;
	}

	public RdLink getLink() {
		return link;
	}

	public void setLink(RdLink link) {
		this.link = link;
	}

	private int kindFlag;// link种别修改标识 kind 1新增 2删除
	private int formCrossFlag;// 交叉口link判断 1 新增 2 修改 3 删除

	public int getFormCrossFlag() {
		return formCrossFlag;
	}

	public void setFormCrossFlag(int formCrossFlag) {
		this.formCrossFlag = formCrossFlag;
	}

	private int laneNum;// 总车道数
	private int laneLeft;// 左车道数
	private int laneRight;// 右车道数

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public int getLaneLeft() {
		return laneLeft;
	}

	public void setLaneLeft(int laneLeft) {
		this.laneLeft = laneLeft;
	}

	public int getLaneRight() {
		return laneRight;
	}

	public void setLaneRight(int laneRight) {
		this.laneRight = laneRight;
	}

	public int isKindFlag() {
		return kindFlag;
	}

	public void setKindFlag(int kindFlag) {
		this.kindFlag = kindFlag;
	}

	public RdLinkForm getForm() {
		return form;
	}

	public void setForm(RdLinkForm form) {
		this.form = form;
	}

	public RdLinkLimit getLimit() {
		return limit;
	}

	public void setLimit(RdLinkLimit limit) {
		this.limit = limit;
	}

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		this.createRdLanes(result);
		return null;
	}

	/**
	 * 新增车道信息
	 * 
	 * @param result
	 * @throws Exception
	 */

	private void createRdLanes(Result result) throws Exception {

		for (int i = 0; i < this.command.getLinks().size(); i++) {

			List<RdLane> lanes = new ArrayList<RdLane>();
			RdLink link = (RdLink) this.command.getLinks().get(i);
			Map<Integer, RdLane> map = new HashMap<Integer, RdLane>();
			Map<Integer, List<RdLane>> mapLane = new HashMap<Integer, List<RdLane>>();
			// 计算link上原有的车道信息
			mapLane = this.caleRdLanesForDir(i, link);
			lanes = mapLane.values().iterator().next();
			int laneDir = mapLane.keySet().iterator().next();
			if (lanes.size() > 0) {
				// 第一条link 的车道信息是当前编辑的车道信息 需要走则增删改流程
				if (i == 0) {
					this.caleRdlanes(lanes, link.getPid(), map, laneDir, result);
				}
				// 如果不是当前编辑的link车道信息（默认为第一条link
				// 1.先删掉link上原有的车道信息，再按照当前输入的车道信息新增车道
				else {
					for (RdLane lane : lanes) {
						com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
								conn);
						operation.deleteRdLane(result, lane);
					}
					this.createRdLane(result, link.getPid(), laneDir);
				}

			} // 如果原有link上没有详细车道信息直接新增
			else {
				this.createRdLane(result, link.getPid(), laneDir);
			}

		}
	}

	/***
	 * 批量维护一条link上的车道信息
	 * 
	 * @param lanes
	 *            原有详细车道信息
	 * @param map
	 *            存入车道pid和车道信息
	 * @param laneDir
	 *            车道方向
	 * @param result
	 * @throws Exception
	 */
	private void caleRdlanes(List<RdLane> lanes, int linkPid, Map<Integer, RdLane> map, int laneDir, Result result)
			throws Exception {

		for (RdLane lane : lanes) {
			map.put(lane.getPid(), lane);
		}
		// 获取当前传入的车道信息
		for (int m = 0; m < this.command.getLaneInfos().size(); m++) {
			JSONObject jsonLaneInfo = this.command.getLaneInfos().getJSONObject(m);
			// 判断此车道信息是否存在
			if (map.containsKey(jsonLaneInfo.getInt("pid"))) {
				// 如果传入的车道数和库中车道数一样不予处理
				if (jsonLaneInfo.size() == 1
						&& map.get(jsonLaneInfo.getInt("pid")).getLaneNum() == this.command.getLaneInfos().size()) {
					continue;
				}
				// 如果存在车道数，且传入的车道总数和原有库中的不一致，走修改功能
				jsonLaneInfo.put("laneNum", this.command.getLaneInfos().size());
				com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation();
				operation.updateRdLane(result, jsonLaneInfo, map.get(jsonLaneInfo.getInt("pid")));
				// 移除已经修改的车道信息
				map.remove(jsonLaneInfo.getInt("pid"));
			}
			// 如果传入的车道信息pid为0 则新增车道信息
			if (jsonLaneInfo.getInt("pid") == 0) {
				this.createRdLane(result, jsonLaneInfo, linkPid, laneDir);
			}
		}
		// 库中原有，传入值中没有的车道信息，全部删除。
		if (map.size() > 0) {
			for (int key : map.keySet()) {
				// 删除RDLANE
				com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
						conn);
				operation.deleteRdLane(result, map.get(key));
			}
		}

	}

	/***
	 * 新增详细车道信息
	 * 
	 * @param result
	 * @param jsonLaneInfo
	 * @param linkPid
	 * @param laneDir
	 * @throws Exception
	 */
	private void createRdLane(Result result, JSONObject jsonLaneInfo, int linkPid, int laneDir) throws Exception {
		RdLane lane = new RdLane();
		if (jsonLaneInfo.getInt("pid") != 0) {
			lane = (RdLane) new RdLaneSelector(conn).loadById(jsonLaneInfo.getInt("pid"), true, true);
		}
		// 申请PId
		int lanePid = PidUtil.getInstance().applyRdLanePid();
		lane.setPid(lanePid);
		lane.setLinkPid(linkPid);
		// 车道总数
		lane.setLaneNum(this.command.getLaneInfos().size());
		// 车道方向
		lane.setLaneDir(laneDir);
		// 车道序号
		if (jsonLaneInfo.containsKey("seqNum")) {
			lane.setSeqNum(jsonLaneInfo.getInt("seqNum"));
		} // 箭头方向
		if (jsonLaneInfo.containsKey("arrowDir")) {
			lane.setArrowDir(jsonLaneInfo.getString("arrowDir"));

		}
		// 中央隔离带
		if (jsonLaneInfo.containsKey("centerDivider")) {
			lane.setCenterDivider(jsonLaneInfo.getInt("centerDivider"));
		} // 车道标识
		if (jsonLaneInfo.containsKey("laneForming")) {
			lane.setLaneForming(jsonLaneInfo.getInt("laneForming"));
		}
		// 车道类型
		if (jsonLaneInfo.containsKey("laneType")) {
			lane.setLaneType(jsonLaneInfo.getInt("laneType"));
		}
		// 车道分离带
		if (jsonLaneInfo.containsKey("laneDivider")) {
			lane.setLaneDivider(jsonLaneInfo.getInt("laneDivider"));
		}
		// 车道条件子表详细车道的时间段和车辆限制表
		if (jsonLaneInfo.containsKey("conditions")) {
			List<IRow> conditionRows = new ArrayList<IRow>();
			for (int i = 0; i < jsonLaneInfo.getJSONArray("conditions").size(); i++) {
				JSONObject conditionObject = jsonLaneInfo.getJSONArray("conditions").getJSONObject(i);
				RdLaneCondition condition = new RdLaneCondition();
				condition.setLanePid(lanePid);
				// 车道方向
				if (conditionObject.containsKey("direction")) {
					condition.setDirection(conditionObject.getInt("direction"));
				}
				// 车道限制时间
				if (conditionObject.containsKey("vehicleTime")) {
					condition.setVehicleTime(conditionObject.getString("vehicleTime"));
				}
				// 车辆类型
				if (conditionObject.containsKey("vehicle")) {
					condition.setVehicle(conditionObject.getLong("vehicle"));
				}
				// 方向时间段
				if (conditionObject.containsKey("directionTime")) {
					condition.setDirectionTime(conditionObject.getString("directionTime"));
				}
				conditionRows.add(condition);

			}

			lane.setConditions(conditionRows);

		}
		lane.setSrcFlag(2);
		result.insertObject(lane, ObjStatus.INSERT, lane.getPid());

	}

	/***
	 * 创建车道信息
	 * 
	 * @param result
	 * @param linkPid
	 * @param seqNum
	 * @param laneDir
	 * @param laneNum
	 * @throws Exception
	 */
	private void createRdlane(Result result, int linkPid, int seqNum, int laneDir, int laneNum) throws Exception {
		RdLane rdLane = new RdLane();
		rdLane.setPid(PidUtil.getInstance().applyRdLanePid());
		rdLane.setLinkPid(linkPid);
		rdLane.setSeqNum(seqNum);
		rdLane.setLaneDir(laneDir);
		rdLane.setLaneNum(laneNum);
		// 车道来源赋值为程序
		rdLane.setSrcFlag(2);
		result.insertObject(rdLane, ObjStatus.INSERT, rdLane.getPid());
	}

	/***
	 * 新增详细车道 车信
	 * 
	 * @param result
	 * @param linkPid
	 * @param seqNum
	 * @param laneDir
	 * @param laneNum
	 * @param arrowDir
	 * @throws Exception
	 */
	private void createRdlane(Result result, int linkPid, int seqNum, int laneDir, int laneNum, String arrowDir)
			throws Exception {
		RdLane rdLane = new RdLane();
		rdLane.setPid(PidUtil.getInstance().applyRdLanePid());
		rdLane.setLinkPid(linkPid);
		rdLane.setSeqNum(seqNum);
		rdLane.setLaneDir(laneDir);
		rdLane.setLaneNum(laneNum);
		rdLane.setSrcFlag(2);
		rdLane.setArrowDir(arrowDir);
		result.insertObject(rdLane, ObjStatus.INSERT, rdLane.getPid());
	}

	/***
	 * 根据属性传值加载RDLANE信息
	 * 
	 * @param result
	 * @param linkPid
	 * @param laneDir
	 * @throws Exception
	 */
	private void createRdLane(Result result, int linkPid, int laneDir) throws Exception {
		for (int m = 0; m < this.command.getLaneInfos().size(); m++) {
			this.createRdLane(result, this.command.getLaneInfos().getJSONObject(m), linkPid, laneDir);
		}

	}

	/***
	 * 按照方向加载link上对应的车道信息
	 * 
	 * @param i
	 * @param laneDir
	 * @param link
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, List<RdLane>> caleRdLanesForDir(int i, RdLink link) throws Exception {
		int laneDir = 1;
		Map<Integer, List<RdLane>> map = new HashMap<Integer, List<RdLane>>();
		List<RdLane> lanes = new ArrayList<RdLane>();
		if (link.getDirect() == 2 || link.getDirect() == 3) {
			lanes = new RdLaneSelector(conn).loadByLink(link.getPid(), 0, true);
		}
		if (link.getDirect() == 1) {
			if (i == 0) {
				laneDir = this.command.getLaneDir();
				lanes = new RdLaneSelector(conn).loadByLink(link.getPid(), this.command.getLaneDir(), true);
			} else {
				RdLink preLink = (RdLink) this.command.getLinks().get(i - 1);
				if (preLink.getsNodePid() == link.geteNodePid() || preLink.geteNodePid() == link.getsNodePid()) {
					lanes = new RdLaneSelector(conn).loadByLink(link.getPid(), 2, true);
					laneDir = 2;
				} else {
					laneDir = 3;
					lanes = new RdLaneSelector(conn).loadByLink(link.getPid(), 3, true);

				}
			}
		}
		map.put(laneDir, lanes);
		return map;
	}

	/***
	 * 新建link对详细车道的维护 1、当link新增时，如果link的种别为8级及以上种别，则根据link的车道数生成link上的详细车道。
	 * 
	 * @param links
	 * @param result
	 * @throws Exception
	 */
	public void caleLanesforCreateRdLinks(List<RdLink> links, Result result) throws Exception {
		for (RdLink link : links) {
			this.createRdLaneForLinkRLanes(result, link, false);
		}
	}

	/***
	 * 1.LINK种类变动维护原则 2.当link种别由非引导道路变为引导道路时，则视该link为新增link，按link新增原则进行详细车道维护。
	 * 2.当link种别由引导道路变为非引导道路时，则视该link为删除link，按link删除原则进行详细车道维护
	 * 
	 * @author zhaokk
	 * @param result
	 */
	public void caleRdLinesForRdLinkKind(Result result) throws Exception {

		int linkPid = this.getLink().getPid();

		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(linkPid, 0, true);
		if (this.getKindFlag() == 2) {
			if (lanes.size() > 0) {
				for (RdLane lane : lanes) {
					// 删除车道信息
					com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
							conn);
					operation.deleteRdLane(result, lane);
				}
			}
		} else {
			// 新增车道信息
			if (lanes.size() <= 0) {
				OpRefRelationObj operation = new OpRefRelationObj(conn, result);

				Map<Integer, List<Integer>> laneInfoList = operation.getRdlaneSelector().getLaneInfoByLinkPid(linkPid,
						OpRefRelationObj.LINK_TOLLGATE_21, null);
				operation.updateByLevel(OpRefRelationObj.LINK_TOLLGATE_21, laneInfoList, null);
			}
		}

	}

	/***
	 * 单变双时，需要确定link是有顺变双，还是逆变双。如果是顺变双， 那么需要将原车道线改为顺，
	 * 然后再根据link逆方向车道数（或者是总车道数/2求得逆方向车道数）再补充逆方向车道
	 * 如果由双方向改为单方向，程序自动将不可通行方向上的详细车道信息删除，如果有联通关系也一起删除
	 * 
	 * @param result
	 * @throws Exception
	 */
	public void caleRdlinesForRdlinkDirect(Result result) throws Exception {
		// link方向修改为双方向
		if (this.getLinkDirect() == 1) {
			// 默认的新增详细车道的方向，具体看在link的那个方向上新增，重新赋值
			int laneDirect = 1;

			List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 0, true);
			if (this.getLink().getDirect() == 2) {
				for (RdLane lane : lanes) {
					this.updateLaneForDir(result, lane, 2);
				}
				// 已有的车道方向是2，如果要新增则在3方向上新增
				laneDirect = 3;
			}
			if (this.getLink().getDirect() == 3) {
				for (RdLane lane : lanes) {
					this.updateLaneForDir(result, lane, 3);
				}
				// 已有的车道方向是3，如果要新增则在2方向上新增
				laneDirect = 2;
			}
			if (lanes.size() != 2) {
				OpRefRelationObj operation = new OpRefRelationObj(conn, result);

				operation.setLaneDirect(laneDirect);

				Map<Integer, List<Integer>> laneInfoList = operation.getRdlaneSelector()
						.getLaneInfoByLinkPid(this.getLink().getPid(), OpRefRelationObj.LINK_TOLLGATE_21, null);
				operation.updateByLevel(OpRefRelationObj.LINK_TOLLGATE_21, laneInfoList, null);
			}

		}
		// 修改link方向为单方向
		if (this.getLinkDirect() == 2 || this.getLinkDirect() == 3) {
			List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 0, true);
			for (RdLane lane : lanes) {
				if (lane.getLaneDir() == 2) {
					if (this.getLinkDirect() == 2) {
						this.updateLaneForDir(result, lane, 1);
					} else {
						this.deleteLane(result, lane);

					}
				}
				if (lane.getLaneDir() == 3) {
					if (this.getLinkDirect() == 3) {
						this.updateLaneForDir(result, lane, 1);
					} else {
						this.deleteLane(result, lane);
					}
				}
			}
		}

	}

	/***
	 * 根据link总车道数和左右车道数维护
	 * 
	 * @param result
	 * @throws Exception
	 */
	public void caleRdLinesForLaneNum(Result result) throws Exception {
		RdLink link = new RdLink();
		link.setPid(this.getLink().getPid());
		link.setLaneNum(this.getLaneNum());
		link.setLaneLeft(this.getLaneLeft());
		link.setLaneRight(this.getLaneRight());
		this.createRdLaneForLinkRLanes(result, link, true);
	}

	/***
	 * 当link上交叉口内link属性添加或删除时，需要该link上详细车道进行维护
	 * 1.当link上添加交叉口内link属性时，则该link上详细车道数按1车道进行维护 。
	 * 2.当link上删除交叉口内link属性时，则该link上详细车道数按link的车道数进行维护。
	 * 
	 * @author zhaokk
	 * @param result
	 */
	public void caleRdLinesForRdLinkCross(Result result) throws Exception {
		if (this.getFormCrossFlag() == 2) {
			this.createRdLaneForLinkRLanes(result, link, true);
		} else {
			// 加载单方向link上详细车道信息
			if (this.getLink().getDirect() == 2 || this.getLink().getDirect() == 3) {
				List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 1, true);
				// 维护单方向link信息
				this.createAndDellanesOfCross(result, lanes, 1);
			}
			if (this.getLink().getDirect() == 1) {
				// 加载顺方向的车道信息
				List<RdLane> lanes2 = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 2, true);
				// 加载逆方向的车道信息
				List<RdLane> lanes3 = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 3, true);
				// 维护顺逆方向的车道信息
				this.createAndDellanesOfCross(result, lanes2, 2);
				this.createAndDellanesOfCross(result, lanes3, 3);

			}
		}
	}

	/***
	 * link交叉口属性变更维护车道信息
	 * 
	 * @author zhaokk
	 * @param result
	 * @param lanes
	 *            车道信息
	 * @param laneDir
	 *            车道方向
	 * @throws Exception
	 */
	private void createAndDellanesOfCross(Result result, List<RdLane> lanes, int laneDir) throws Exception {
		if (lanes.size() == 0) {
			// 车道方向 为 laneDir
			// 车道总数 1
			// 车道序号1
			this.createRdlane(result, this.getLink().getPid(), 1, laneDir, 1);
		} else {
			for (int i = 0; i < lanes.size(); i++) {
				// 如果只有一个车道信息 不做处理
				if (lanes.size() == 1) {
					break;
				}
				// 如果车道数大于1 保留第一条车道 修改车道总数 删除掉多余的车道数
				if (i == 0 && lanes.size() > 1) {
					this.updateLaneForAttr(result, lanes.get(i), 1);
					continue;
				}
				this.deleteLane(result, lanes.get(i));
			}
		}
	}

	/****
	 * link车辆类型限制变更 1、 当link上添加或删除车辆类型限制时，或进行车辆类型或时间段变更时，需要进行详细车道维护 2、
	 * 当link上添加车辆类型及时间段时，则该link上对应方向上所有车道均添加该车辆类型限制及时间
	 * 3、当link上删除车辆类型及时间时，则该link上对应方向上所有车道均删除该车辆类型限制及时间
	 * 4、当link车辆类型或时间段变更时，则该link上对应该方向上所有车道更新为link上车辆类型及时间 说明：
	 * ①在进行车道车辆类型更新时，如果Rd_lane_Condtion中不存在记录的，需要先增加对应车道的记录，再添加车辆类型及时间
	 * ②在进行车道类型删除更新时，如果删除车辆类型或时间段后，RD_LANE_CONDTION中该车道的方向时间段及车辆类型均为空，
	 * 则该RD_LANE_CONDTION记录需要删除。
	 * 
	 * @throws Exception
	 */
	public void refRdLaneForRdlinkLimit(Result result, List<RdLane> lanes, int flag) throws Exception {
		for (RdLane lane : lanes) {
			List<IRow> rows = lane.getConditions();
			// 需要修改
			if (flag == 2) {
				for (IRow row : rows) {
					RdLaneCondition condition = (RdLaneCondition) row;
					if (!condition.getDirectionTime().equals(this.getLimit().getTimeDomain())) {
						row.changedFields().put("vehicleTime", this.getLimit().getTimeDomain());
					}
					if (condition.getVehicle() != this.getLimit().getVehicle()) {
						row.changedFields().put("vehicle", this.getLimit().getVehicle());
					}
					result.insertObject(row, ObjStatus.UPDATE, lane.getPid());
				}
			}
			// 需要删除
			else if (flag == 3) {
				for (IRow row : rows) {
					result.insertObject(row, ObjStatus.DELETE, lane.getPid());
				}
			}
			// 需要新增
			else if (flag == 1) {
				RdLaneCondition condition = new RdLaneCondition();
				condition.setLanePid(lane.getPid());
				condition.setVehicleTime(this.getLimit().getTimeDomain());
				condition.setVehicle(this.getLimit().getVehicle());
				result.insertObject(condition, ObjStatus.INSERT, lane.getPid());
			}
		}
	}

	/***
	 * 1、 当link属性新增或删除“公交车专用道”或“步行街”属性时，对应维护该link上车道的车辆类型 2、当link属性新增“公交车专用道”属性时
	 * ，则该link上所有车道，赋值车辆类型为“允许公交车”车辆类型；当link属性删除“公交车专用道”属性时，则该link上所有车道
	 * ，均删除车辆类型记录
	 * 3、当link属性新增“步行街”属性时，则该link上所有车道，赋值车辆类型为“允许步行者，配送卡车，急救车”；当删除“步行街
	 * ”属性时，则该link上所有车道删除车辆类型记录 说明： ①该业务原则中所说的所有车道，包含顺逆两个方向的车道。
	 * ②在进行车道类型删除更新时，如果删除车辆类型或时间段后
	 * ，RD_LANE_CONDTION中该车道的方向时间段及车辆类型均为空，则该RD_LANE_CONDTION记录需要删除。
	 * 
	 * @param result
	 * @param link
	 * @param lanes
	 * @param form
	 * @throws Exception
	 */
	public void refRdLaneForRdlinkForm(Result result, int formOfWay) throws Exception {
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 0, true);
		for (RdLane lane : lanes) {
			List<IRow> rows = lane.getConditions();
			if (rows.size() > 0) {
				for (IRow row : rows) {
					RdLaneCondition condition = (RdLaneCondition) row;
					if (this.getFormCrossFlag() == 2) {
						if (formOfWay == 20) {
							if (condition.getVehicle() != 2147483786L) {
								condition.changedFields().put("vehicle", 2147483786L);
							}
						}
						if (formOfWay == 22) {
							if (condition.getVehicle() != 2147484160L) {
								condition.changedFields().put("vehicle", 2147484160L);
							}
						}
						result.insertObject(condition, ObjStatus.UPDATE, lane.getPid());
					} else if (this.getFormCrossFlag() == 3) {
						result.insertObject(condition, ObjStatus.DELETE, lane.getPid());
					}
				}

			} else{
				RdLaneCondition newLaneCondition = new RdLaneCondition();

				newLaneCondition.setLanePid(lane.getPid());

				if (formOfWay == 20) {
					newLaneCondition.setVehicle(2147483786L);
				} else if (formOfWay == 22) {
					newLaneCondition.setVehicle(2147484160L);
				}
				result.insertObject(newLaneCondition, ObjStatus.INSERT, lane.getPid());
			}
		}

	}

	/***
	 * 1、 当车信的车道数或车信的转向箭头发生变更时，需要对详细车道进行维护。
	 * 2、当车信车道数发生变更时，参考车信的车道数对详细车道记录及物理车道数进行维护。
	 * 3、当车信转向箭头发生变更时，则参考车信的转向箭头，对详细车道转向箭头属性进行维护。
	 * 4、当车信车道数与转向箭头同时发生变化时，则先进行车道记录及物理车道数变更，再进行对应车道的转向箭头的变更
	 * 
	 * @param result
	 * @throws Exception
	 */
	public void refRdLaneForRdLaneconnexity(Result result, int laneDir) throws Exception {
		int linkPid = this.getConnexity().getInLinkPid();
		List<String> laneInfos = this.getLanInfos();
		int nodePid = this.getConnexity().getNodePid();

		int direct = calRdLaneDir(linkPid, nodePid, laneDir, 0);

		List<RdLane> lanes = new RdLaneSelector(this.conn).loadByLink(linkPid, direct, true);

		if (lanes.size() >= laneInfos.size()) {
			for (int i = laneInfos.size(); i < lanes.size(); i++) {
				this.deleteLane(result, lanes.get(i));
			}
			for (int i = 0; i < laneInfos.size(); i++) {
				if (laneInfos.get(i) != lanes.get(i).getArrowDir()) {
					this.createRdlane(result, linkPid, laneInfos.size(), laneDir, i + 1, laneInfos.get(i));
				} else {
					if (laneInfos.size() != lanes.size()) {
						this.updateLaneForAttr(result, lanes.get(i), laneInfos.size());
					}
				}

			}

		} else {
			for (int i = lanes.size(); i < laneInfos.size(); i++) {
				this.createRdlane(result, linkPid, laneInfos.size(), laneDir, i + 1, laneInfos.get(i));
			}
			for (int i = 0; i < lanes.size(); i++) {
				if (!laneInfos.get(i).contains(lanes.get(i).getArrowDir())) {
					this.deleteLane(result, lanes.get(i));
				} else {
					if (laneInfos.size() != lanes.size()) {
						this.updateLaneForAttr(result, lanes.get(i), laneInfos.size());
					}
				}

			}

		}

	}

	/***
	 * 删除车道信息
	 * 
	 * @param result
	 * @param lanePid
	 * @throws Exception
	 */
	private void deleteLane(Result result, RdLane lane) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
				conn);
		operation.deleteRdLane(result, lane);
	}

	/***
	 * 通过车信修改车道信息
	 * 
	 * @param result
	 * @param lane
	 * @param seqNum
	 * @throws Exception
	 */
	private void updateLaneForAttr(Result result, RdLane lane, int seqNum) throws Exception {
		lane.changedFields().put("seqNum", seqNum);
		result.insertObject(lane, ObjStatus.UPDATE, lane.getPid());
	}

	/***
	 * 通过方向修改车道信息
	 * 
	 * @param result
	 * @param lane
	 * @param seqNum
	 * @throws Exception
	 */
	private void updateLaneForDir(Result result, RdLane lane, int laneDir) throws Exception {
		lane.changedFields().put("laneDir", laneDir);
		result.insertObject(lane, ObjStatus.UPDATE, lane.getPid());
	}

	/***
	 * 1、 当收费站新增或删除或收费站通道数发生变更时，需要进行详细车道维护。 2、
	 * 当收费站新增时且收费站通道数不为0时，收费站挂接的进入link和退出link需要按收费站的通道数维护详细车道 3、
	 * 当收费站删除时，原收费站挂接的link需要按link的车道数进行详细车道的维护，详细维护原则如下：
	 * 当link的左右车道数不为0时，则按照左车道数更新该link逆方向详细车道数
	 * ，按照右车道数更新该link顺方向的详细车道数；当link的左右车道数为0时
	 * ，则按照总车道数生成该link车道数。如果link为单方向，则详细车道物理车道数
	 * =link总车道数，如果link为双方向，则详细车道单侧的物理车道数
	 * link总车道数/2，如果总车道数为奇数时，则为(link总车道数+1)/2。
	 * 
	 * @param result
	 * @throws Exception
	 */
	public void refRdLaneForTollgate(Result result) throws Exception {
		int inLinkPid = this.getTollgate().getInLinkPid();
		int outLinkPid = this.getTollgate().getOutLinkPid();
		int nodePid = this.getTollgate().getNodePid();
		int laneDir = 1;
		int inDirect = calRdLaneDir(inLinkPid, nodePid, laneDir, 0);
		List<RdLane> inLanes = new RdLaneSelector(this.conn).loadByLink(inLinkPid, inDirect, true);
		int outDirect = calRdLaneDir(outLinkPid, nodePid, laneDir, 1);
		List<RdLane> outLanes = new RdLaneSelector(this.conn).loadByLink(outLinkPid, outDirect, true);

		this.calefRdLaneForTollgate(result, inLanes, inLinkPid, inDirect);
		this.calefRdLaneForTollgate(result, outLanes, outLinkPid, outDirect);

	}

	/***
	 * 收费站对详细车道的维护
	 * 
	 * @param result
	 * @param lanes
	 *            车道信息
	 * @param linkPid
	 *            进入退出link
	 * @param laneDir
	 *            车道方向
	 * @throws Exception
	 */
	private void calefRdLaneForTollgate(Result result, List<RdLane> lanes, int linkPid, int laneDir) throws Exception {
		if (this.getPassageNum() != lanes.size()) {
			if (this.getPassageNum() > lanes.size()) {
				for (int i = lanes.size(); i < this.getPassageNum(); i++) {
					this.createRdlane(result, linkPid, i + 1, laneDir, this.getPassageNum());
				}
				for (int i = 0; i < lanes.size(); i++) {
					this.updateLaneForAttr(result, lanes.get(i), this.getPassageNum());
				}
			}
			if (this.getPassageNum() < lanes.size()) {
				for (int i = this.getPassageNum(); i < lanes.size(); i++) {
					this.deleteLane(result, lanes.get(i));
				}
				for (int i = 0; i < this.getPassageNum(); i++) {
					this.updateLaneForAttr(result, lanes.get(i), this.getPassageNum());
				}
			}
		}
	}

	private int calRdLaneDir(int linkPid, int nodePid, int laneDir, int flag) throws Exception {
		int direct = 1;

		RdLink link = (RdLink) new RdLinkSelector(this.conn).loadById(linkPid, true, false);
		if (link.getDirect() == 1) {
			if (nodePid == link.geteNodePid()) {
				if (flag == 1) {
					direct = 3;
				} else {
					direct = 2;
				}
			} else {
				if (flag == 1) {
					direct = 2;
				} else {
					direct = 3;
				}
			}
		}
		return direct;
	}

	/***
	 * 打断link维护详细车道信息
	 * 
	 * @param linkPid打断link
	 * @param links
	 *            生成的新link
	 * @param result
	 * @throws Exception
	 */
	public void breakRdLink(RdLink link, List<RdLink> links, Result result) throws Exception {
		// 线修行移动分离不是跨图幅不用维护
		if (links.size() == 1) {
			return;
		}
		boolean flag = false;
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> sLinks = linkSelector.loadByNodePid(link.getsNodePid(), false);
		List<RdLink> eLinks = linkSelector.loadByNodePid(link.geteNodePid(), false);
		if (sLinks.size() < 3 && eLinks.size() < 3) {
			flag = true;
		}
		// 加载原有Link上的车道信息
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(link.getPid(), 0, true);
		// 删除原有车道信息
		for (RdLane lane : lanes) {
			result.insertObject(lane, ObjStatus.DELETE, lane.getPid());
		}
		for (RdLink rdLink : links) {
			for (RdLane lane : lanes) {
				// 设置车道的link信息
				RdLane rdLane = new RdLane();
				rdLane.copy(lane);
				if (lane.getArrowDir() != "o" && lane.getArrowDir() != "9") {
					if (rdLink.getsNodePid() == link.getsNodePid() || rdLink.getsNodePid() == link.geteNodePid()
							|| rdLink.geteNodePid() == link.getsNodePid()
							|| rdLink.geteNodePid() == link.geteNodePid()) {
						if (flag) {
							rdLane.setArrowDir("9");
						}

					} else {
						rdLane.setArrowDir("9");
					}
				}
				rdLane.setLinkPid(rdLink.getPid());
				// 申请车道pid
				int lanePid = PidUtil.getInstance().applyRdLanePid();
				rdLane.setPid(lanePid);
				// 加载详细车道的时间段和车辆限制表信息
				if (rdLane.getConditions().size() > 0) {
					for (IRow row : rdLane.getConditions()) {
						RdLaneCondition condition = (RdLaneCondition) row;
						condition.setLanePid(lanePid);
					}
				}
				result.insertObject(rdLane, ObjStatus.INSERT, rdLane.getPid());
			}
		}

	}

	/**
	 * 根据link上车道总数 左右车道数生成车道信息
	 * 详细原则如下：当link的左右车道数不为0时，则按照左车道数更新该link逆方向详细车道数，按照右车道数更新该link顺方向的详细车道数；
	 * 当link的左右车道数为0时，则按照总车道数生成该link车道数。
	 * 如果link为单方向，则详细车道物理车道数=link总车道数，如果link为双方向
	 * ，则详细车道单侧的物理车道数=link总车道数/2，如果总车道数为奇数时，则为(link总车道数+1)/2。
	 * 其余属性赋默认值，车道限制不生成记录。
	 * 
	 * @author zhaokk
	 * @param laneFlag
	 *            是否忽略原有link车道的影响
	 * @param result
	 * @throws Exception
	 */
	private void createRdLaneForLinkRLanes(Result result, RdLink link, boolean laneFlag) throws Exception {
		// 加载link上原有左右车道信息
		List<RdLane> leftLanes = new ArrayList<RdLane>();
		List<RdLane> rightLanes = new ArrayList<RdLane>();
		// 如果link的左右车道不为0 按照link的左右车道维护详细车道信息
		if (link.getLaneLeft() != 0 && link.getLaneWidthRight() != 0) {
			// 如果修改link的左右车道信息 或者link是修改不是新增 加载原有link上左右车道信息
			if (laneFlag) {
				leftLanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 3, true);
				rightLanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 2, true);
			}
			// 维护左车道信息
			this.caleRdlanesForNum(result, leftLanes, link, link.getLaneLeft(), 3);
			// 维护有车道信息
			this.caleRdlanesForNum(result, rightLanes, link, link.getLaneRight(), 2);
		}
		// 如果link的左右车道为0按照link的总车道数维护信息车道信息
		else {
			if (link.getLaneNum() != 0) {
				// 如果link方向为双方向 需要按照总车道维护左右车道信息
				if (link.getDirect() == 1) {
					// 如果修改link的左右车道信息 或者link是修改不是新增 加载原有link上左右车道信息
					if (laneFlag) {

						leftLanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 3, true);
						rightLanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 2, true);

					}
					// 维护左车道信息
					this.caleRdlanesForNum(result, leftLanes, link, (link.getLaneNum() + 1) / 2, 3);
					// 维护右车道信息
					this.caleRdlanesForNum(result, rightLanes, link, (link.getLaneNum() + 1) / 2, 2);
					// 如果原有link为单方向 忽略左右车道信息 生成车道方向为无
					if (link.getDirect() == 2 || link.getDirect() == 3) {
						List<RdLane> lanes = new ArrayList<RdLane>();
						if (laneFlag) {
							lanes = new RdLaneSelector(conn).loadByLink(this.getLink().getPid(), 0, true);
						}
						// 生成车道方向为无的车道
						this.caleRdlanesForNum(result, lanes, link, link.getLaneNum(), 1);

					}

				}

			}
		}
	}

	/***
	 * 根据原有车道 左右 数和当前车道数 维护link上的车道信息
	 * 
	 * @param result
	 * @param lanes
	 *            原有车道信息
	 * @param link
	 * @param currentNum
	 *            当前车道总数
	 * @param laneDir
	 *            车道方向
	 * @throws Exception
	 */
	private void caleRdlanesForNum(Result result, List<RdLane> lanes, RdLink link, int currentNum, int laneDir)
			throws Exception {
		// 如果原有车道数 大于当前车道数 相等不予处理：
		// 1.按照当前车道数数量更新原有车道总数
		// 2.多余的车道信息删除掉
		if (lanes.size() > currentNum) {
			for (int i = 0; i < lanes.size(); i++) {
				if (i + 1 <= currentNum) {
					// 更新车道信息
					this.updateLaneForAttr(result, lanes.get(i), currentNum);
				} else {
					// 删除车道信息
					this.deleteLane(result, lanes.get(i));
				}

			}
		}
		// 如果原有车道数 小于于当前车道数 相等不予处理：
		// 1.按照当前车道数数量更新原有车道总数
		// 2.新增少于的车道信息
		if (lanes.size() < currentNum) {
			for (int i = 0; i < currentNum; i++) {
				if (i + 1 <= lanes.size()) {
					// 更新车道信息
					this.updateLaneForAttr(result, lanes.get(i), currentNum);
				} else {
					// 创建车道信息
					this.createRdlane(result, link.getPid(), i + 1, laneDir, currentNum);
				}

			}
		}

	}

}