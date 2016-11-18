package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
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

	private int flag;

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
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
	@SuppressWarnings("unchecked")
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

				if (i == 0) {
					for (RdLane lane : lanes) {
						map.put(lane.getPid(), lane);
					}
					for (int m = 0; m < this.command.getLaneInfos().size(); m++) {
						JSONObject jsonLaneInfo = this.command.getLaneInfos()
								.getJSONObject(m);
						if (map.containsKey(jsonLaneInfo.getInt("pid"))) {
							if (jsonLaneInfo.size() == 1
									&& map.get(jsonLaneInfo.getInt("pid"))
											.getLaneNum() == this.command
											.getLaneInfos().size()) {
								continue;
							}
							jsonLaneInfo.put("laneNum", this.command
									.getLaneInfos().size());
							com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation();
							operation.updateRdLane(result, jsonLaneInfo,
									map.get(jsonLaneInfo.getInt("pid")));
							map.remove(jsonLaneInfo.getInt("pid"));
						}
						if (jsonLaneInfo.getInt("pid") == 0) {
							this.createRdLane(result, jsonLaneInfo,
									link.getPid(), laneDir);
						}
					}
					if (map.size() > 0) {
						for (int key : map.keySet()) {
							// 删除RDLANE
							com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
									conn);
							operation.deleteRdLane(result, map.get(key));
						}
					}

				} else {
					for (RdLane lane : lanes) {
						com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
								conn);
						operation.deleteRdLane(result, lane);
					}
					this.createRdLane(result, link.getPid(), laneDir);
				}

			} else {
				this.createRdLane(result, link.getPid(), laneDir);
			}

		}
	}

	private void createRdLane(Result result, JSONObject jsonLaneInfo,
			int linkPid, int laneDir) throws Exception {
		RdLane lane = new RdLane();
		if (jsonLaneInfo.getInt("pid") != 0) {
			lane = (RdLane) new RdLaneSelector(conn).loadById(
					jsonLaneInfo.getInt("pid"), true, true);
		}
		int lanePid = PidUtil.getInstance().applyRdLanePid();
		lane.setPid(lanePid);
		lane.setLinkPid(linkPid);
		lane.setLaneNum(this.command.getLaneInfos().size());

		lane.setLaneDir(laneDir);
		if (jsonLaneInfo.containsKey("seqNum")) {
			lane.setSeqNum(jsonLaneInfo.getInt("seqNum"));
		}
		if (jsonLaneInfo.containsKey("arrowDir")) {
			lane.setArrowDir(jsonLaneInfo.getString("arrowDir"));

		}
		if (jsonLaneInfo.containsKey("centerDivider")) {
			lane.setCenterDivider(jsonLaneInfo.getInt("centerDivider"));
		}
		if (jsonLaneInfo.containsKey("laneForming")) {
			lane.setLaneForming(jsonLaneInfo.getInt("laneForming"));
		}
		if (jsonLaneInfo.containsKey("laneType")) {
			lane.setLaneType(jsonLaneInfo.getInt("laneType"));
		}
		if (jsonLaneInfo.containsKey("laneDivider")) {
			lane.setLaneDivider(jsonLaneInfo.getInt("laneDivider"));
		}
		// 车道限速
		if (jsonLaneInfo.containsKey("")) {
		}
		if (jsonLaneInfo.containsKey("conditions")) {
			List<IRow> conditionRows = new ArrayList<IRow>();
			for (int i = 0; i < jsonLaneInfo.getJSONArray("conditions").size(); i++) {
				JSONObject conditionObject = jsonLaneInfo.getJSONArray(
						"conditions").getJSONObject(i);
				RdLaneCondition condition = new RdLaneCondition();
				condition.setLanePid(lanePid);

				if (conditionObject.containsKey("direction")) {
					condition.setDirection(conditionObject.getInt("direction"));
				}
				if (conditionObject.containsKey("vehicleTime")) {
					condition.setVehicleTime(conditionObject
							.getString("vehicleTime"));
				}
				if (conditionObject.containsKey("vehicle")) {
					condition.setVehicle(conditionObject.getLong("vehicle"));
				}
				if (conditionObject.containsKey("directionTime")) {
					condition.setDirectionTime(conditionObject
							.getString("directionTime"));
				}
				conditionRows.add(condition);

			}

			lane.setConditions(conditionRows);

		}
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
	private void createRdlane(Result result, int linkPid, int seqNum,
			int laneDir, int laneNum) throws Exception {
		RdLane rdLane = new RdLane();
		rdLane.setPid(PidUtil.getInstance().applyRdLanePid());
		rdLane.setLinkPid(linkPid);
		rdLane.setSeqNum(seqNum);
		rdLane.setLaneDir(laneDir);
		rdLane.setLaneNum(laneNum);
		result.insertObject(rdLane, ObjStatus.INSERT, rdLane.getPid());
	}

	private void createRdlane(Result result, int linkPid, int seqNum,
			int laneDir, int laneNum, String arrowDir) throws Exception {
		RdLane rdLane = new RdLane();
		rdLane.setPid(PidUtil.getInstance().applyRdLanePid());
		rdLane.setLinkPid(linkPid);
		rdLane.setSeqNum(seqNum);
		rdLane.setLaneDir(laneDir);
		rdLane.setLaneNum(laneNum);
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
	private void createRdLane(Result result, int linkPid, int laneDir)
			throws Exception {
		for (int m = 0; m < this.command.getLaneInfos().size(); m++) {
			this.createRdLane(result, this.command.getLaneInfos()
					.getJSONObject(m), linkPid, laneDir);
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
	private Map<Integer, List<RdLane>> caleRdLanesForDir(int i, RdLink link)
			throws Exception {
		int laneDir = 1;
		Map<Integer, List<RdLane>> map = new HashMap<Integer, List<RdLane>>();
		List<RdLane> lanes = new ArrayList<RdLane>();
		if (link.getDirect() == 2 || link.getDirect() == 3) {
			lanes = new RdLaneSelector(conn).loadByLink(link.getPid(), 0, true);
		}
		if (link.getDirect() == 1) {
			if (i == 0) {
				laneDir = this.command.getLaneDir();
				lanes = new RdLaneSelector(conn).loadByLink(link.getPid(),
						this.command.getLaneDir(), true);
			} else {
				RdLink preLink = (RdLink) this.command.getLinks().get(i - 1);
				if (preLink.getsNodePid() == link.geteNodePid()
						|| preLink.geteNodePid() == link.getsNodePid()) {
					lanes = new RdLaneSelector(conn).loadByLink(link.getPid(),
							2, true);
					laneDir = 2;
				} else {
					laneDir = 3;
					lanes = new RdLaneSelector(conn).loadByLink(link.getPid(),
							3, true);

				}
			}
		}
		map.put(laneDir, lanes);
		return map;
	}

	/***
	 * 1.LINK种类变动维护原则 2.当link种别由非引导道路变为引导道路时，则视该link为新增link，按link新增原则进行详细车道维护。
	 * 3.当link种别由引导道路变为非引导道路时，则视该link为删除link，按link删除原则进行详细车道维护。 新增原则：
	 * 如果link的种别为8级及以上种别，则根据link的车道数生成link上的详细车道。
	 * 详细原则如下：当link的左右车道数不为0时，则按照左车道数更新该link逆方向详细车道数，
	 * 按照右车道数更新该link顺方向的详细车道数；当link的左右车道数为0时，
	 * 则按照总车道数生成该link车道数。如果link为单方向，则详细车道物理车道数=link总车道数，
	 * 如果link为双方向，则详细车道单侧的物理车道数=link总车道数/2，如果总车道数为奇数时，则为(link总车道数+1)/2。
	 * 其余属性赋默认值，车道限制不生成记录
	 * 
	 * @author zhaokk
	 * @param result
	 * @param link
	 * @param flag
	 *            0 create 1 delete
	 * @throws Exception
	 */
	public void caleRdLinesForRdLinkKind(Result result) throws Exception {
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink()
				.getPid(), 1, true);
		if (this.getFlag() == 1) {
			if (lanes.size() > 0) {
				for (RdLane lane : lanes) {
					// 删除车道信息
					com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
							conn);
					operation.deleteRdLane(result, lane);
				}
			}
		} else {
			if (lanes.size() <= 0) {
				if (link.getLaneLeft() != 0 && link.getLaneWidthRight() != 0) {
					for (int i = 0; i < link.getLaneLeft(); i++) {
						this.createRdlane(result, link.getPid(), i + 1, 3,
								link.getLaneLeft());

					}
					for (int i = 0; i < link.getLaneRight(); i++) {
						this.createRdlane(result, link.getPid(), i + 1, 2,
								link.getLaneRight());

					}

				} else {
					if (link.getLaneNum() != 0) {
						if (link.getDirect() == 2 || link.getDirect() == 3) {
							for (int i = 0; i < (link.getLaneNum() + 1) / 2; i++) {
								this.createRdlane(result, link.getPid(), i + 1,
										2, (link.getLaneNum() + 1) / 2);
								this.createRdlane(result, link.getPid(), i + 1,
										3, (link.getLaneNum() + 1) / 2);

							}
						}
						if (link.getDirect() == 1) {
							for (int i = 0; i < link.getLaneNum(); i++) {

								this.createRdlane(result, link.getPid(), i + 1,
										1, link.getLaneNum());

							}
						}

					}
				}
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
	public void refRdLaneForRdlinkLimit(Result result) throws Exception {
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink()
				.getPid(), this.getLimit().getLimitDir(), true);

		for (RdLane lane : lanes) {
			List<IRow> rows = lane.getConditions();
			for (IRow row : rows) {
				result.insertObject(row, ObjStatus.DELETE, lane.getPid());
			}
			if (this.getFlag() == 1) {
				RdLaneCondition condition = new RdLaneCondition();
				condition.setLanePid(lane.getPid());
				condition.setVehicleTime(this.getLimit().getTimeDomain());
				condition.setVehicle(this.getLimit().getVehicle());
				result.insertObject(condition, ObjStatus.UPDATE, lane.getPid());
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
	public void refRdLaneForRdlinkForm(Result result) throws Exception {
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(this.getLink()
				.getPid(), 1, true);
		for (RdLane lane : lanes) {
			List<IRow> rows = lane.getConditions();
			if (rows.size() > 0) {
				for (IRow row : rows) {
					RdLaneCondition condition = (RdLaneCondition) row;
					if (this.getFlag() == 1) {
						if (this.getForm().getFormOfWay() == 20) {
							if (condition.getVehicle() != 2147483786L) {
								condition.changedFields().put("vehicle",
										2147483786L);

							}

						}
						if (this.getForm().getFormOfWay() == 22) {
							if (condition.getVehicle() != 2147484160L) {
								condition.changedFields().put("vehicle",
										2147484160L);
							}
						}
						result.insertObject(condition, ObjStatus.UPDATE,
								lane.getPid());
					}
					if (this.getFlag() == 2) {
						result.insertObject(condition, ObjStatus.DELETE,
								lane.getPid());
					}
				}
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
	public void refRdLaneForRdLaneconnexity(Result result) throws Exception {
		int linkPid = this.getConnexity().getInLinkPid();
		List<String> laneInfos = this.getLanInfos();
		int nodePid = this.getConnexity().getNodePid();
		int laneDir = 1;

		List<RdLane> lanes = this.caleRdLanesForDir(linkPid, nodePid, laneDir,
				0);

		if (lanes.size() >= laneInfos.size()) {
			for (int i = laneInfos.size(); i < lanes.size(); i++) {
				this.deleLaneForAttr(result, lanes.get(i));
			}
			for (int i = 0; i < laneInfos.size(); i++) {
				if (laneInfos.get(i) != lanes.get(i).getArrowDir()) {
					this.createRdlane(result, linkPid, laneInfos.size(),
							laneDir, i + 1, laneInfos.get(i));
				} else {
					if (laneInfos.size() != lanes.size()) {
						this.updateLaneForAttr(result, lanes.get(i),
								laneInfos.size());
					}
				}

			}

		} else {
			for (int i = lanes.size(); i < laneInfos.size(); i++) {
				this.createRdlane(result, linkPid, laneInfos.size(), laneDir,
						i + 1, laneInfos.get(i));
			}
			for (int i = 0; i < lanes.size(); i++) {
				if (laneInfos.get(i) != lanes.get(i).getArrowDir()) {

					this.deleLaneForAttr(result, lanes.get(i));

				} else {
					if (laneInfos.size() != lanes.size()) {
						this.updateLaneForAttr(result, lanes.get(i),
								laneInfos.size());

					}
				}

			}

		}

	}

	/***
	 * 通过车信删除车道信息
	 * 
	 * @param result
	 * @param lanePid
	 * @throws Exception
	 */
	private void deleLaneForAttr(Result result, RdLane lane) throws Exception {
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
	private void updateLaneForAttr(Result result, RdLane lane, int seqNum)
			throws Exception {
		lane.changedFields().put("seqNum", seqNum);
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
	 * =link总车道数/2，如果总车道数为奇数时，则为(link总车道数+1)/2。其余属性赋默认值，车道限制不生成记录。 4、
	 * 当收费站通道数变更时，则根据收费站通道数对收费站进入link和退出link进行详细车道数维护。
	 * 
	 * @param result
	 * @throws Exception
	 */
	public void refRdLaneForTollgate(Result result) throws Exception {
		int inLinkPid = this.getTollgate().getInLinkPid();
		int outLinkPid = this.getTollgate().getOutLinkPid();
		int nodePid = this.getTollgate().getNodePid();
		int laneDir = 1;

		List<RdLane> inLanes = this.caleRdLanesForDir(inLinkPid, nodePid,
				laneDir, 0);
		List<RdLane> outLanes = this.caleRdLanesForDir(outLinkPid, nodePid,
				laneDir, 1);
		this.calefRdLaneForTollgate(result, inLanes, inLinkPid, laneDir);
		this.calefRdLaneForTollgate(result, outLanes, outLinkPid, laneDir);

	}

	private void calefRdLaneForTollgate(Result result, List<RdLane> lanes,
			int linkPid, int laneDir) throws Exception {
		if (this.getPassageNum() != lanes.size()) {
			if (this.getPassageNum() > lanes.size()) {
				for (int i = lanes.size(); i < this.getPassageNum(); i++) {
					this.createRdlane(result, linkPid, i + 1, laneDir,
							this.getPassageNum());
				}
				for (int i = 0; i < lanes.size(); i++) {
					this.updateLaneForAttr(result, lanes.get(i),
							this.getPassageNum());
				}
			}
			if (this.getPassageNum() < lanes.size()) {
				for (int i = this.getPassageNum(); i < lanes.size(); i++) {
					this.deleLaneForAttr(result, lanes.get(i));
				}
				for (int i = 0; i < this.getPassageNum(); i++) {
					this.updateLaneForAttr(result, lanes.get(i),
							this.getPassageNum());
				}
			}
		}
	}

	private List<RdLane> caleRdLanesForDir(int linkPid, int nodePid,
			int laneDir, int flag) throws Exception {
		RdLink link = (RdLink) new RdLinkSelector(this.conn).loadById(linkPid,
				true, false);
		if (link.getDirect() == 1) {
			if (nodePid == link.geteNodePid()) {
				if (flag == 1) {
					laneDir = 3;
				} else {
					laneDir = 2;
				}
			} else {
				if (flag == 1) {
					laneDir = 2;
				} else {
					laneDir = 3;
				}
			}
		}
		return new RdLaneSelector(this.conn).loadByLink(linkPid, laneDir, true);
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
	public void breakRdLink(int linkPid, List<RdLink> links, Result result)
			throws Exception {
		// 加载原有Link上的车道信息
		List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(linkPid, 0,
				true);
		// 删除原有车道信息
		for (RdLane lane : lanes) {
			result.insertObject(lane, ObjStatus.DELETE, lane.getPid());
		}
		for (RdLink link : links) {
			for (RdLane lane : lanes) {
				// 设置车道的link信息
				RdLane rdLane = new RdLane();
				rdLane.copy(lane);
				lane.setLinkPid(link.getPid());
				// 申请车道pid
				int lanePid = PidUtil.getInstance().applyRdLanePid();
				lane.setPid(lanePid);
				// 加载详细车道的时间段和车辆限制表信息
				if (lane.getConditions().size() > 0) {
					for (IRow row : lane.getConditions()) {
						RdLaneCondition condition = (RdLaneCondition) row;
						condition.setLanePid(lanePid);
					}
				}
				result.insertObject(rdLane, ObjStatus.INSERT, lane.getPid());
			}
		}

	}

	public static void main(String[] args) {
		Map<Integer, List<RdLane>> map = new HashMap<Integer, List<RdLane>>();
		List<RdLane> lanes = new ArrayList<RdLane>();
		RdLane lane = new RdLane();
		lane.setPid(11101);
		RdLane lane1 = new RdLane();
		lane1.setPid(11102);
		lanes.add(lane);
		lanes.add(lane1);
		map.put(1, lanes);
		lanes = map.values().iterator().next();
		System.out.println(lanes.get(1).getPid());
	}

}