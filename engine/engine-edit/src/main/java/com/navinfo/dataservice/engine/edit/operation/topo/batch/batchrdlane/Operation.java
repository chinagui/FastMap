package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;

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

	public Operation(Command command) {
		this.command = command;
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
			int laneDir = 1;
			List<RdLane> lanes = new ArrayList<RdLane>();
			RdLink link = (RdLink) this.command.getLinks().get(i);
			// 计算link上原有的车道信息
			lanes = this.caleRdLanesForDir(i, laneDir, link);
			if (lanes.size() > 0) {
				for (int m = 0; m < this.command.getLaneInfos().size(); m++) {
					JSONObject jsonLaneInfo = this.command.getLaneInfos()
							.getJSONObject(m);
					if (i == 0) {
						if (jsonLaneInfo.getInt("lanePid") == 0) {
							this.createRdLane(result, link.getPid(), laneDir);
						} else {
							for (RdLane lane : lanes) {

								if (jsonLaneInfo.getInt("lanePid") == lane
										.getPid()) {
									// 修改Rdlane
									com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation();
									operation.updateRdLane(result,
											jsonLaneInfo, lane);
								} else {
									// 删除RDLANE
									com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
											conn);
									operation.deleteRdLane(result,
											lane.getPid());
								}
							}

						}

					} else {
						for (RdLane lane : lanes) {
							// 删除rdlane
							com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
									conn);
							operation.deleteRdLane(result, lane.getPid());
						}
						// 新增rdlane
						this.createRdLane(result, link.getPid(), laneDir);

					}
				}

			} else {
				this.createRdLane(result, link.getPid(), laneDir);
			}

		}
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
	public void caleRdLinesForRdLink(Result result, RdLink link, int flag)
			throws Exception {
		if (link != null) {
			// 加载link车道信息
			List<RdLane> lanes = new RdLaneSelector(conn).loadByLink(
					link.getPid(), 1, true);
			if (flag == 1) {
				if (lanes.size() > 0) {
					for (RdLane lane : lanes) {
						// 删除车道信息
						com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
								conn);
						operation.deleteRdLane(result, lane.getPid());
					}
				}
			} else {
				if (lanes.size() <= 0) {
					if (link.getLaneLeft() != 0
							&& link.getLaneWidthRight() != 0) {
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
									this.createRdlane(result, link.getPid(),
											i + 1, 2,
											(link.getLaneNum() + 1) / 2);
									this.createRdlane(result, link.getPid(),
											i + 1, 3,
											(link.getLaneNum() + 1) / 2);

								}
							}
							if (link.getDirect() == 1) {
								for (int i = 0; i < link.getLaneNum(); i++) {

									this.createRdlane(result, link.getPid(),
											i + 1, 1, link.getLaneNum());

								}
							}

						}
					}
				}
			}

		}
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
		rdLane.setPid(PidService.getInstance().applyRdLanePid());
		rdLane.setLinkPid(linkPid);
		rdLane.setSeqNum(seqNum);
		rdLane.setLaneDir(laneDir);
		rdLane.setLaneNum(laneNum);
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
			JSONObject jsonLaneInfo = this.command.getLaneInfos()
					.getJSONObject(m);
			RdLane lane = new RdLane();
			if (jsonLaneInfo.getInt("lanePid") != 0) {
				lane = (RdLane) new RdLaneSelector(conn).loadById(
						jsonLaneInfo.getInt("lanePid"), true, true);
			}

			lane.setPid(PidService.getInstance().applyRdLanePid());
			lane.setLinkPid(linkPid);
			lane.setLaneNum(this.command.getLaneInfos().size());
			lane.setSeqNum(m + 1);
			lane.setLaneDir(laneDir);
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
			if (jsonLaneInfo.containsKey("contditions")) {
				List<IRow> conditionRows = new ArrayList<IRow>();
				for (int i = 0; i < jsonLaneInfo.getJSONArray("contditions")
						.size(); i++) {
					JSONObject conditionObject = jsonLaneInfo.getJSONArray(
							"contditions").getJSONObject(i);
					RdLaneCondition condition = new RdLaneCondition();

					if (conditionObject.containsKey("direction")) {
						condition.setDirection(conditionObject
								.getInt("direction"));
					}
					if (conditionObject.containsKey("vehicleTime")) {
						condition.setVehicleTime(conditionObject
								.getString("vehicleTime"));
					}
					if (conditionObject.containsKey("vehicle")) {
						condition
								.setVehicle(conditionObject.getLong("vehicle"));
					}
					if (conditionObject.containsKey("directionTime")) {
						condition.setDirectionTime(conditionObject
								.getString("directionTime"));
					}
					conditionRows.add(condition);
				}

				lane.setConditions(conditionRows);
			}

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
	private List<RdLane> caleRdLanesForDir(int i, int laneDir, RdLink link)
			throws Exception {

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
				if (preLink.getsNodePid() == link.getsNodePid()
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

		return lanes;
	}
}