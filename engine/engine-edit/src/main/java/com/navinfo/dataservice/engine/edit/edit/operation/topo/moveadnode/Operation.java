package com.navinfo.dataservice.engine.edit.edit.operation.topo.moveadnode;

import java.sql.Connection;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.comm.util.operate.AdLinkOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
/**
 * @author zhaokk
 * 移动行政区划点操作类 
 * 移动行政区划点 点不会打断其它的行政区划线
 */
public class Operation implements IOperation {

	private Command command;

	private AdNode updateNode;
	
	private Connection conn;

	public Operation(Command command, AdNode updateNode,Connection conn) {
		this.command = command;
		this.updateNode = updateNode;
		this.conn  = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		result.setPrimaryPid(updateNode.getPid());
		this.updateNodeGeometry(result);
		this.updateLinkGeomtry(result);
		this.updateFaceGeomtry(result);
		return null;
	}
	/*
	 * 移动行政区划点修改对应的线的信息 
	 */
	private void updateLinkGeomtry(Result result) throws Exception {
		
		for (AdLink link : command.getLinks()) {
			 Geometry geo=AdLinkOperateUtils.caleLinkGeomertyForMvNode(link,updateNode.pid(),command.getLongitude(),command.getLatitude());
			 Set<String> meshes = MeshUtils.getInterMeshes(geo);
			 //修改线的几何属性
			 if (meshes.size() == 1){	 
			 }else{
				 
			 }
			 

			/*updateContent.put("geometry", geojson);
			updateContent.put("length", GeometryUtils.getLinkLength((GeoTranslator.geojson2Jts(geojson, 1, 5))));

			link.fillChangeFields(updateContent);
			
			result.insertObject(link, ObjStatus.UPDATE, link.pid());*/
		}
	}
	/*
	 * 移动行政区划点修改对应的点的信息 
	 */
	private void updateNodeGeometry(Result result) throws Exception {
		
	    //计算点的几何形状
		JSONObject geojson = new JSONObject();
		geojson.put("type", "Point");
		geojson.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });
		JSONObject updateNodeJson = new JSONObject();
	    //要移动点的project_id
		updateNodeJson.put("projectId", command.getProjectId());
		JSONObject data = new JSONObject();
		//移动点的新几何
		data.put("geometry", geojson);
		data.put("pid", updateNode.pid());
		data.put("objStatus", ObjStatus.UPDATE);
		updateNodeJson.put("data", data);
		
		//组装打断线的参数
		//保证是同一个连接
		com.navinfo.dataservice.engine.edit.edit.operation.obj.adnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.edit.operation.obj.adnode.update.Command(
				updateNodeJson, command.getRequester());
		com.navinfo.dataservice.engine.edit.edit.operation.obj.adnode.update.Process process = new com.navinfo.dataservice.engine.edit.edit.operation.obj.adnode.update.Process(
				updatecommand,result,conn);
		process.innerRun();
	}
	/*
	 * 移动行政区划点修改对应的的信息 
	 */
	private void updateFaceGeomtry(Result result) throws Exception {
		Geometry geomNode  = GeoTranslator.transform(updateNode.getGeometry(), 0.00001, 5);
		double lon =geomNode.getCoordinates()[0].x;
		double lat =geomNode.getCoordinates()[0].y;
		if(command.getFaces() != null && command.getFaces().size() > 0){
		for (AdFace face : command.getFaces()) {
		
			Geometry geomFace = GeoTranslator.transform(face.getGeometry(), 0.00001, 5);
			Coordinate[] cd = geomFace.getCoordinates();
			double[][] ps = new double[cd.length][2];
			double [][][] adPs = new double[1][cd.length][2];			
			for (int i = 0; i < cd.length; i++) {
				if (cd[i].x == lon&&cd[i].y== lat){
					cd[i].x = command.getLongitude();
					cd[i].y = command.getLongitude();
				}
			}
			for (int i = 0; i < cd.length; i++) {
				ps[i][0] = cd[i].x;

				ps[i][1] = cd[i].y;
			}
			adPs[0]  = ps;
			JSONObject geojson = new JSONObject();
			geojson.put("type", "Polygon");
			geojson.put("coordinates", adPs);
			JSONObject updateContent = new JSONObject();
			updateContent.put("geometry", geojson);
			updateContent.put("length", GeometryUtils.getLinkLength((GeoTranslator.geojson2Jts(geojson, 1, 5))));
			updateContent.put("area", GeometryUtils.getCalculateArea((GeoTranslator.geojson2Jts(geojson, 1, 5))));
			face.fillChangeFields(updateContent);
			result.insertObject(face, ObjStatus.UPDATE, face.pid());
			}
		}
	}
	
}
