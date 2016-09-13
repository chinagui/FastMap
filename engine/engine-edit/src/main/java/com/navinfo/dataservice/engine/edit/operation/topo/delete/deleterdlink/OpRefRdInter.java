/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @ClassName: OpRefTrafficsignal
 * @author Zhang Xiaolong
 * @date 2016年7月21日 下午5:03:21
 * @Description: TODO
 */
public class OpRefRdInter {

	private Connection conn;

	private Command command;

	public OpRefRdInter(Command	command)
	{
		this.command = command;
	}

	public OpRefRdInter(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, RdLink link) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation(
				this.conn);
		rdinterOperation.deleteByLink(link, result);

		return null;
	}
	
	/**
	 * 删除link对大门的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteRdGateInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdGate rdGate : command.getRdGates()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdGate.objType());

			alertObj.setPid(rdGate.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
