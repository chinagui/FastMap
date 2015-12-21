package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdspeedlimit.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.comm.service.PidService;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdSpeedlimit limit;

	public Operation(Command command, RdSpeedlimit limit) {
		this.command = command;

		this.limit = limit;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.getDelObjects().add(limit);
				
				return null;
			} else {

				boolean isChanged = limit.fillChangeFields(content);

				if (isChanged) {
					result.getUpdateObjects().add(limit);
				}
			}
		}

		return null;
	}

}
