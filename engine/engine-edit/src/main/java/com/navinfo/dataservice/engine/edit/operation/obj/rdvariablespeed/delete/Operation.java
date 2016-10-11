package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
}
