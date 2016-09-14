package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;

public class Operation implements IOperation {

	private RdLaneConnexity lane;

	public Operation(Command command, RdLaneConnexity lane) {
		this.lane = lane;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(lane, ObjStatus.DELETE, lane.pid());

		return null;
	}

	/**
	 * 删除link对车信的更新影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateResInfectData(int linkPid, Connection conn) throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		List<RdLaneConnexity> lanes2 = selector.loadRdLaneConnexityByOutLinkPid(linkPid, true);

		List<RdLaneConnexity> outLinkUpdateLaneList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : lanes2) {
			List<IRow> topos = rdLaneConnexity.getTopos();

			if (topos.size() > 1) {
				outLinkUpdateLaneList.add(rdLaneConnexity);
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : outLinkUpdateLaneList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			alertList.add(alertObj);
		}

		return alertList;
	}

	/**
	 * 删除进入link对车信的删除影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteInLinkRdLaneConnexityInfectData(int linkPid, Connection conn) throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByLinkPid(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : lanes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}

	/**
	 * 删除退出link对车信的删除影响分析
	 * 
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteOutLinkRdLanConnexityInfectData(int linkPid, Connection conn) throws Exception {
		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		List<RdLaneConnexity> lanes2 = selector.loadRdLaneConnexityByOutLinkPid(linkPid, true);

		List<RdLaneConnexity> outLinkDeleteLaneList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : lanes2) {
			List<IRow> topos = rdLaneConnexity.getTopos();

			if (topos.size() == 1) {
				outLinkDeleteLaneList.add(rdLaneConnexity);
			}
		}
		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : outLinkDeleteLaneList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
