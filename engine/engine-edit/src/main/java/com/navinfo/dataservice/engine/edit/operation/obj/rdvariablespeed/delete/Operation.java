package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;

public class Operation implements IOperation {

	private RdVariableSpeed variableSpeed;

	private Connection conn;

	public Operation(RdVariableSpeed variableSpeed) {

		this.variableSpeed = variableSpeed;

	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.pid());

		return null;
	}

	/**
	 * @param linkPid
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLink(RdLink link, Result result) throws Exception {
		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		List<RdVariableSpeed> variableSpeedList = selector.loadRdVariableSpeedByLinkPid(link.getPid(), true);

		for (RdVariableSpeed rdVariableSpeed : variableSpeedList) {
			result.insertObject(rdVariableSpeed, ObjStatus.DELETE, rdVariableSpeed.getPid());
		}

		List<RdVariableSpeed> variableSpeed = selector.loadRdVariableSpeedByViaLinkPid(link.getPid(), true);

		for(RdVariableSpeed speed : variableSpeed)
		{
			List<IRow> viaList = speed.getVias();
			int selectSeqNum = 0;
			for (IRow via : viaList) {
				RdVariableSpeedVia speedVia = (RdVariableSpeedVia) via;
				int viaSeqNum = speedVia.getSeqNum();
				//找到接续线的序号，大于该序号的link都需要删除
				if(speedVia.getLinkPid() == link.getPid())
				{
					selectSeqNum = viaSeqNum;
					result.insertObject(speedVia, ObjStatus.DELETE, speedVia.getVspeedPid());
				}
				else if(selectSeqNum != 0 && selectSeqNum<viaSeqNum)
				{
					result.insertObject(speedVia, ObjStatus.DELETE, speedVia.getVspeedPid());
				}
			}
			
		}
	}

	/**
	 * 删除link对可变限速的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdVariableInfectData(int linkPid, Connection conn) throws Exception {

		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		List<RdVariableSpeed> variableSpeedList = selector.loadRdVariableSpeedByLinkPid(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdVariableSpeed rdVariableSpeed : variableSpeedList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdVariableSpeed.objType());

			alertObj.setPid(rdVariableSpeed.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
	
	/**
	 * 删除link对可变限速的更新影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateRdVariableInfectData(int linkPid, Connection conn) throws Exception {

		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		List<RdVariableSpeedVia> viaList = selector.loadRdVariableSpeedVia(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdVariableSpeedVia rdVariableSpeedVia : viaList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(ObjType.RDVARIABLESPEED);

			alertObj.setPid(rdVariableSpeedVia.getVspeedPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除links时 维护可变限速
	 * @param linkPids
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLinks(List<Integer> linkPids, Result result) throws Exception {

		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		Map<Integer, RdVariableSpeed> delVariableSpeed = new HashMap<>();

		for (int linkPid : linkPids) {

			List<RdVariableSpeed> variableSpeeds = selector.loadRdVariableSpeedByLinkPid(linkPid, true);

			for (RdVariableSpeed variableSpeed : variableSpeeds) {
				delVariableSpeed.put(variableSpeed.getPid(), variableSpeed);
			}
		}

		//被删link作为进入线、退出线，删除link删除可变限速
		for (RdVariableSpeed variableSpeed : delVariableSpeed.values()) {

			result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.getPid());
		}

		Map<Integer, RdVariableSpeed> updateVariableSpeed = new HashMap<>();

		for (int linkPid : linkPids) {

			List<RdVariableSpeed> variableSpeeds = selector.loadRdVariableSpeedByViaLinkPid(linkPid, true);

			for (RdVariableSpeed slope : variableSpeeds) {

				if (delVariableSpeed.containsKey(slope.getPid())) {

					continue;
				}

				updateVariableSpeed.put(slope.getPid(), slope);
			}
		}

		for (RdVariableSpeed variableSpeed : updateVariableSpeed.values()) {

			int minSeqNum = Integer.MAX_VALUE;

			List<RdVariableSpeedVia> vias = new ArrayList<>();

			for (IRow row : variableSpeed.getVias()) {

				RdVariableSpeedVia via = (RdVariableSpeedVia) row;

				vias.add(via);

				if (!linkPids.contains(via.getLinkPid())) {

					continue;
				}

				if (minSeqNum > via.getSeqNum()) {

					minSeqNum = via.getSeqNum();
				}
			}

			//被删link作为接续线，删除最小接续线以及后续接续线
			for (RdVariableSpeedVia via : vias) {

				if (via.getSeqNum() >= minSeqNum) {

					result.insertObject(via, ObjStatus.DELETE, variableSpeed.getPid());
				}
			}
		}
	}
}
