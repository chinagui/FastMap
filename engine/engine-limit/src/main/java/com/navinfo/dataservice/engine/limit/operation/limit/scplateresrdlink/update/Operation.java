package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdlink.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;

import net.sf.json.JSONObject;

public class Operation implements IOperation{

	private Command command;
	
	public Operation(Command command){
		this.command = command;
	}
	
	@Override
	public String run(Result result) throws Exception {

		JSONObject content = this.command.getContent();
		
		ScPlateresLink link = new ScPlateresLink();
		
		if(content.containsKey("objStatus") && content.getString("objStatus").equals(ObjStatus.UPDATE.toString())){
			boolean isChange = link.fillChangeFields(content);
			
			if(isChange){
				result.insertObject(link, ObjStatus.UPDATE, this.command.getGemetryId());
			}
		}
		return null;
	}

}
