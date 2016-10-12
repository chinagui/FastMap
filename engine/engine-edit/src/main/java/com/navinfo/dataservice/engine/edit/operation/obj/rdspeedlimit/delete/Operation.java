package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;

public class Operation implements IOperation {

	private RdSpeedlimit speedlimit;

	public Operation(Command command, RdSpeedlimit speedlimit) {
		this.speedlimit = speedlimit;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(speedlimit, ObjStatus.DELETE, speedlimit.pid());

		return null;
	}
	
	/**
	 * 删除link对限速的更新影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getUpdateRdSpeedLimitInfectData(int linkPid,Connection conn) throws Exception {
		
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPid(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdSpeedlimit limit : limits) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(limit.objType());

			alertObj.setPid(limit.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
