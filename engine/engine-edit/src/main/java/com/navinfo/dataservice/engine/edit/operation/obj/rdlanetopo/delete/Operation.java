package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.delete;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;

/***
 * 删除车道联通信息信息
 * 
 * @author zhaokk
 * 
 */
public class Operation implements IOperation {

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
		this.deleteRdLaneTopo(result);
		return null;
	}

	private void deleteRdLaneTopo(Result result) {
		if (this.command.getDetail() != null) {
			result.insertObject(this.command.getDetail(), ObjStatus.DELETE,
					this.command.getDetail().getPid());
		}
	}

	/***
	 * 通过车道信息删除车道联通信息
	 * 
	 * @param result
	 * @param lanePid
	 * @throws Exception
	 */
	public void deleteTopoForRdLane(Result result, int lanePid)
			throws Exception {
		List<IRow> details = new RdLaneTopoDetailSelector(conn).loadByLanePid(
				lanePid, true);
		if (details.size() > 0) {
			for (IRow row : details) {
				RdLaneTopoDetail detail = (RdLaneTopoDetail) row;
				result.insertObject(detail, ObjStatus.DELETE, detail.getPid());
			}
		}
	}

	/***
	 * 通过车道信息数组删除车道联通信息
	 * 
	 * @param result
	 * @param lanePid
	 * @throws Exception
	 */
	public void deleteTopoForRdLanes(Result result, List<Integer> lanePids) throws Exception {

		List<IRow> details = new RdLaneTopoDetailSelector(conn).loadByLanePids(
				lanePids, true);
		
		if (details.size() > 0) {
			for (IRow row : details) {
				RdLaneTopoDetail detail = (RdLaneTopoDetail) row;
				result.insertObject(detail, ObjStatus.DELETE, detail.getPid());
			}
		}
	
		
	}

}
