package com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {
	
	private Command command;
	
	private RdLink updateLink;
	
	private RdNode updateNode;
	
	private Check check;
	
	public Operation(Command command,RdLink updateLink,RdNode updateNode,Check check){
		this.command = command;
		
		this.updateLink = updateLink;
		
		this.updateNode = updateNode;
		
		this.check = check;
	}

	@Override
	public String run(Result result) throws Exception {
		
		this.updateLinkGeomtry(result);
		
		this.updateNodeGeometry(result);
		
		return null;
	}

	private void updateLinkGeomtry(Result result) throws Exception{
		
		Geometry geom = GeoTranslator.transform(updateLink.getGeometry(), 0.00001, 5);
		
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
		
		check.checkPointCoincide(ps);
		
		check.checkShapePointDistance(ps);
		
		JSONObject geojson = new JSONObject();
		
		geojson.put("type", "LineString");
		
		geojson.put("coordinates", ps);
		
		JSONObject updateContent = new JSONObject();
		
		updateContent.put("geometry", geojson);
		
		updateLink.fillChangeFields(updateContent);
		
		result.insertObject(updateLink, ObjStatus.UPDATE);
	}
	
	private void updateNodeGeometry(Result result) throws Exception{
		
		RdNode node = new RdNode();
		
		node.setPid(PidService.getInstance().applyNodePid());
		
		node.copy(updateNode);
		
		JSONObject geojson = new JSONObject();
		
		geojson.put("type", "Point");
		
		geojson.put("coordinates", new double[]{command.getLongitude(),command.getLatitude()});
		
		Geometry geo = GeoTranslator.geojson2Jts(geojson, 100000, 0);
		
		node.setGeometry(geo);
		
		node.setMesh(Integer.parseInt(MeshUtils.lonlat2Mesh(command.getLongitude(), command.getLatitude())));
		
		result.insertObject(node, ObjStatus.INSERT);
	}
	
}
