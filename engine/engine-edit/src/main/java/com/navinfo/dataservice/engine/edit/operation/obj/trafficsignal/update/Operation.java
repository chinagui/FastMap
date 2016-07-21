package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;

import net.sf.json.JSONObject;

/**
 * 
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:27 
* @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
		
		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());

				return null;
			} else {

				boolean isChanged = rdTrafficsignal.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
				}
			}
		}

		return null;
	}

}
