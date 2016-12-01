package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create;

import java.sql.Connection;
import java.util.Map;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;

public class Check {

	private Command command;

	public Check(Command command) {
		this.command = command;
	}

	/**
	 * @param conn
	 * @throws Exception
	 */
	public void hasRdObject(Connection conn) throws Exception {
		RdObjectSelector selector = new RdObjectSelector(conn);

		String linkPids = JsonUtils.getStringValueFromJSONArray(this.command.getLinkArray());

		Map<String, RdObject> linkObjMap = selector.loadRdObjectByPidAndType(linkPids, ObjType.RDLINK, true);

		if (linkObjMap.size() > 0) {
			throw new Exception("所选link已经存在CRFO对象");
		}

		String interPids = JsonUtils.getStringValueFromJSONArray(this.command.getInterArray());

		Map<String, RdObject> interObjMap = selector.loadRdObjectByPidAndType(interPids, ObjType.RDINTER, true);

		if (interObjMap.size() > 0) {
			throw new Exception("所选inter已经存在CRFO对象");
		}

		String roadPids = JsonUtils.getStringValueFromJSONArray(this.command.getRoadArray());

		Map<String, RdObject> roadObjMap = selector.loadRdObjectByPidAndType(roadPids, ObjType.RDROAD, true);

		if (roadObjMap.size() > 0) {
			throw new Exception("所选road已经存在CRFO对象");
		}
	}
}
