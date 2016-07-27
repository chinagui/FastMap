package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;

public class Operation implements IOperation {

	private Command command;

	private RdWarninginfo rdWarninginfo;

	public Operation(Command command) {
		this.command = command;

		this.rdWarninginfo = this.command.getRdWarninginfo();

	}

	@Override
	public String run(Result result) throws Exception {

		String msg = update( result);

		return msg;
	}
	
	private String update(Result result)throws Exception
	{
		JSONObject content = command.getContent();

		if (!content.containsKey("objStatus")) {

			return null;
		}

		if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
			result.insertObject(rdWarninginfo, ObjStatus.DELETE,
					rdWarninginfo.pid());
			return null;
		}
		
		boolean isChanged = rdWarninginfo.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(rdWarninginfo, ObjStatus.UPDATE,
					rdWarninginfo.pid());
		}

		return null;
	}

}
