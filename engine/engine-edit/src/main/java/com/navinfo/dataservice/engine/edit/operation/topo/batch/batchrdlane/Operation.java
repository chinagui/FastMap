package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
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
		if (this.command.getLanes().size() > 0) {
			for (RdLane lane : this.command.getLanes()) {
				if (lane.getPid() == 0) {
					lane.setPid(PidService.getInstance().applyRdLanePid());
					result.insertObject(lane, ObjStatus.INSERT, lane.getPid());
				} else {
					for (RdLane rdLane : command.getSourceLanes()) {
						if (lane.getPid() == rdLane.getPid()) {
							boolean flag = false;
							if (lane.getArrowDir() != rdLane.getArrowDir()) {
								rdLane.changedFields().put("arrowDir",
										lane.getArrowDir());
								flag = true;
							}
							if (lane.getSeqNum() != rdLane.getSeqNum()) {
								rdLane.changedFields().put("seqNum",
										lane.getSeqNum());
								flag = true;
							}
							if (lane.getLaneNum() != command.getLaneNum()) {
								rdLane.changedFields().put("laneNum",
										command.getLaneNum());
								flag = true;
							}
							if (flag) {
								result.insertObject(rdLane, ObjStatus.UPDATE,
										rdLane.getPid());
							}
						} else {
							result.insertObject(rdLane, ObjStatus.DELETE,
									rdLane.getPid());
						}
					}
				}
			}
		} else {
			if (this.command.getSourceLanes().size() > 0) {
				for (RdLane rdLane : this.command.getSourceLanes()) {
					result.insertObject(rdLane, ObjStatus.DELETE,
							rdLane.getPid());
				}
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
					// 删除车道信息
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

	public static void main(String[] args) {
		System.out.println(1 / 2);
	}
}