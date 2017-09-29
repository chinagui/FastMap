package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdlink.create;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;

import net.sf.json.JSONArray;

public class Operation implements IOperation{

	private Command command;
	
	private Connection conn;
	
	public Operation(Command command,Connection conn){
		this.command = command;
		this.conn = conn;
	}
	
	@Override
	public String run(Result result) throws Exception {
		
		JSONArray array = this.command.getLinks();
		
		for(int i = 0; i < array.size(); i++){
			
			ScPlateresLink link = new ScPlateresLink();
			
			String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(), LimitObjType.SCPLATERESLINK, i);
			
			link.setGeometryId(geomId);
			link.setGroupId(this.command.getGroupId());
			link.setGeometry(this.command.getGeo());
			
			result.insertObject(link, ObjStatus.INSERT, geomId);	
		}
		
		return null;
	}

}
