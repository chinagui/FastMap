package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
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

	public Operation(Command command) {
		this.command = command;
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
}