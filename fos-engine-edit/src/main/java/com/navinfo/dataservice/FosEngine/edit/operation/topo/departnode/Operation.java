package com.navinfo.dataservice.FosEngine.edit.operation.topo.departnode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {
	
	private Command command;
	
	private RdLink updateLink;
	
	private RdNode updateNode;
	
	public Operation(Command command,RdLink updateLink,RdNode updateNode){
		this.command = command;
		
		this.updateLink = updateLink;
		
		this.updateNode = updateNode;
	}

	@Override
	public String run(Result result) throws Exception {
		
		this.updateLinkGeomtry();
		
		this.updateNodeGeometry();
		
		result.insertObject(updateLink, ObjStatus.UPDATE);
		
		result.insertObject(updateNode, ObjStatus.UPDATE);
		
		return null;
	}

	private void updateLinkGeomtry() throws Exception{
		
		Geometry geom = updateLink.getGeometry();
		
		Coordinate[] cs = geom.getCoordinates();
		
		double[][] ps = new double[cs.length][2];
		
		for(int i=0;i<cs.length;i++){
			ps[i][0] = cs[i].x;
			
			ps[i][1] = cs[i].y;
		}
		
		if (updateLink.getsNodePid() == command.getNodePid()){
			ps[0][0] = command.getLongitude();
			
			ps[0][1] = command.getLatitude();
		}else{
			ps[ps.length-1][0] = command.getLongitude();
			
			ps[ps.length-1][1] = command.getLatitude();
		}
		
		JSONObject geojson = new JSONObject();
		
		geojson.put("type", "LineString");
		
		geojson.put("coordinates", ps);
		
		JSONObject updateContent = new JSONObject();
		
		updateContent.put("geometry", geojson);
		
		updateLink.fillChangeFields(updateContent);
		
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
