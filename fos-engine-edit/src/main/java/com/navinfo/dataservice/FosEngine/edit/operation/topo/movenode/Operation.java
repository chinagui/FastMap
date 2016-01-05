package com.navinfo.dataservice.FosEngine.edit.operation.topo.movenode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {
	
	private Command command;
	
	private RdNode updateNode;
	
	public Operation(Command command,RdNode updateNode){
		this.command = command;
		
		this.updateNode = updateNode;
	}

	@Override
	public String run(Result result) throws Exception {

		this.updateNodeGeometry();
		
		result.insertObject(updateNode, ObjStatus.UPDATE);
		
		return null;
	}

	
	private void updateNodeGeometry() throws Exception{
		JSONObject geojson = new JSONObject();
		
		geojson.put("type", "Point");
		
		geojson.put("coordinates", new double[]{command.getLongitude(),command.getLatitude()});
		
		JSONObject updateContent = new JSONObject();
		
		updateContent.put("geometry", geojson);
		
		updateNode.fillChangeFields(updateContent);
	}
}
