package com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.engine.edit.comm.util.AdminUtils;
import com.navinfo.dataservice.engine.edit.comm.util.OperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
/**
 * @author zhaokk
 * 创建行政区划线参数基础类 
 */
public class Operation implements IOperation {

	private Command command;

	private Check check;
	
	private Connection conn;

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;
		List<Geometry> geomList = new ArrayList<Geometry>();
		List<JSONObject> seNodeList = new ArrayList<JSONObject>();
		//如果创建行政区划线有对应的挂接AD_NODE和ADFACE
		//执行打断线处理逻辑
		if (command.getCatchLinks().size() > 0) {
			geomList = this.splitsLink(result,command.getCatchLinks(),
					command.getGeometry(),seNodeList);

		} 
		//如果创建行政区划线没有对应的挂接AD_NODE和ADFACE
		//创建对应的ADNODE
		if(command.getCatchLinks().size()==0 || geomList.size()==0){
			this.createAdNode(result, geomList,seNodeList);
		}
		//创建行政区划线信息
		for (int index=0;index<geomList.size();index++) {
			Geometry geo = geomList.get(index);
			Set<String> meshes = this.getLinkInterMesh(geo);
			//行政区划线在图廓线内
			if (meshes.size() == 1) {
				AdLink link = new AdLink();
				int meshId = Integer.parseInt(meshes.iterator().next());
				link.setMesh(meshId);
				link.setPid(PidService.getInstance().applyAdLinkPid());
				result.setPrimaryPid(link.getPid());
				double linkLength = GeometryUtils.getLinkLength(geo);
				link.setLength(linkLength);
				link.setGeometry(GeoTranslator.transform(geo, 100000, 0));
				link.setStartNodePid(seNodeList.get(index).getInt("s"));
				link.setEndNodePid(seNodeList.get(index).getInt("e"));
				setLinkChildren(link);
				result.insertObject(link, ObjStatus.INSERT, link.pid());
			} 
			//行政区划线在图廓线外
			else {
				Iterator<String> it = meshes.iterator();

				while (it.hasNext()) {
					String meshIdStr = it.next();

					Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo,
							MeshUtils.mesh2Jts(meshIdStr));

					if ("LineString".equals(geomInter.getGeometryType())) {
						AdLink link = new AdLink();
						int meshId = Integer.parseInt(meshIdStr);
						link.setMesh(meshId);
						link.setPid(PidService.getInstance().applyLinkPid());
						result.setPrimaryPid(link.getPid());
						link.setGeometry(GeoTranslator.transform(geomInter,
								100000, 0));
						double linkLength = GeometryUtils.getLinkLength(geomInter);
						check.checkLinkLength(linkLength);
						link.setLength(linkLength);
						link.setStartNodePid(seNodeList.get(index).getInt("s"));
						link.setEndNodePid(seNodeList.get(index).getInt("e"));
						setLinkChildren(link);
						result.insertObject(link, ObjStatus.INSERT, link.pid());

					}
				}

			}
		}

		return msg;
	}

	/**
	 *
	 * 
	 * @param link创建对应的ADNODE
	 */
	
	private void createAdNode(Result result,List<Geometry> geomList,List<JSONObject> seNodeList) throws Exception{
		geomList.add(GeoTranslator.geojson2Jts(command.getGeometry()));

		JSONObject se = new JSONObject();
        //创建起始点信息
		if (0 == command.getsNodePid()) {

			Coordinate point = geomList.get(0).getCoordinates()[0];

			AdNode node = OperateUtils.createAdNode(point.x, point.y);

			result.insertObject(node, ObjStatus.INSERT, node.pid());

			se.put("s", node.getPid());
		} else {
			se.put("s", command.getsNodePid());
		}
		//创建终止点信息
		if (0 == command.geteNodePid()) {

			Coordinate point = geomList.get(0).getCoordinates()[geomList
					.get(0).getCoordinates().length - 1];

			AdNode node = OperateUtils.createAdNode(point.x, point.y);

			result.insertObject(node, ObjStatus.INSERT, node.pid());

			se.put("e", node.getPid());
		} else {
			se.put("e", command.geteNodePid());
		}
		seNodeList.add(se);
	
	}

	/*
	 * 维护link的子表 AD_LINK_MESH
	 * 
	 * @param link
	 */
	private void setLinkChildren(AdLink link) {

		AdLinkMesh mesh = new AdLinkMesh();

		mesh.setLinkPid(link.getPid());

		mesh.setMesh(link.mesh());

		List<IRow> meshes = new ArrayList<IRow>();

		meshes.add(mesh);

		link.setMeshes(meshes);
	}

	/*
	 * 根据几何属性获取图幅列表信息
	 * 
	 * @param link
	 */
	private Set<String> getLinkInterMesh(Geometry linkGeom) throws Exception {
		Set<String> set = new HashSet<String>();

		Coordinate[] cs = linkGeom.getCoordinates();

		for (Coordinate c : cs) {
			set.add(MeshUtils.lonlat2Mesh(c.x, c.y));
		}

		return set;
	}
	/*
	 * AD_LINK分割具体操作
	 * 
	 * @param JSONArray
	 */
	private List<Geometry> splitsLink(Result result, JSONArray catchLinks,
			JSONObject geometry, List<JSONObject> seNodeList) throws Exception {

		List<Geometry> geoms = new ArrayList<Geometry>();

		JSONArray coordinates = geometry.getJSONArray("coordinates");

		JSONObject tmpGeom = new JSONObject();

		tmpGeom.put("type", "LineString");

		JSONArray tmpCs = new JSONArray();

		tmpCs.add(coordinates.get(0));

		int p = 0;

		int pc = 1;

		if (tmpCs.getJSONArray(0).getDouble(0) == catchLinks.getJSONObject(0).getDouble("lon")
				&& tmpCs.getJSONArray(0).getDouble(1) == catchLinks.getJSONObject(0).getDouble(
						"lat")) {
			p = 1;
		}

		JSONObject se = new JSONObject();

		if (0 == command.getsNodePid()) {

			double x = coordinates.getJSONArray(0).getDouble(0);

			double y = coordinates.getJSONArray(0).getDouble(1);

			AdNode node = OperateUtils.createAdNode(x, y);

			result.insertObject(node, ObjStatus.INSERT, node.pid());

			se.put("s", node.getPid());
			
			if (p == 1 && catchLinks.getJSONObject(0).containsKey("linkPid")){
				catchLinks.getJSONObject(0).put("breakNode", node.getPid());
			}
		} else {
			se.put("s", command.getsNodePid());
		}

		while (p < catchLinks.size() && pc < coordinates.size()) {
			tmpCs.add(coordinates.getJSONArray(pc));

			if (coordinates.getJSONArray(pc).getDouble(0) == catchLinks
					.getJSONObject(p).getDouble("lon")
					&& coordinates.getJSONArray(pc).getDouble(1) == catchLinks
							.getJSONObject(p).getDouble("lat")) {

				tmpGeom.put("coordinates", tmpCs);

				geoms.add(GeoTranslator.geojson2Jts(tmpGeom));

				if (catchLinks.getJSONObject(p).containsKey("nodePid")) {
					se.put("e", catchLinks.getJSONObject(p).getInt("nodePid"));
					
					seNodeList.add(se);
					
					se = new JSONObject();
					
					se.put("s", catchLinks.getJSONObject(p).getInt("nodePid"));
				}else{
					double x = catchLinks
							.getJSONObject(p).getDouble("lon");

					double y = catchLinks
							.getJSONObject(p).getDouble("lat");

					AdNode node = OperateUtils.createAdNode(x, y);

					result.insertObject(node, ObjStatus.INSERT, node.pid());

					se.put("e", node.getPid());
					
					seNodeList.add(se);
					
					se = new JSONObject();
					
					se.put("s", node.getPid());
					
					catchLinks
					.getJSONObject(p).put("breakNode", node.getPid());
				}

				tmpGeom = new JSONObject();

				tmpGeom.put("type", "LineString");

				tmpCs = new JSONArray();

				tmpCs.add(coordinates.getJSONArray(pc));

				p++;
			}

			pc++;
		}

		if (tmpCs.size() > 1) {
			tmpGeom.put("coordinates", tmpCs);

			geoms.add(GeoTranslator.geojson2Jts(tmpGeom));
			
			if (command.geteNodePid() !=0){
				se.put("e", command.geteNodePid());
				
				seNodeList.add(se);
			}else{
				double x = tmpCs.getJSONArray(tmpCs.size() -1).getDouble(0);

				double y = tmpCs.getJSONArray(tmpCs.size() -1).getDouble(1);

				AdNode node = OperateUtils.createAdNode(x, y);

				result.insertObject(node, ObjStatus.INSERT, node.pid());

				se.put("e", node.getPid());
				
				seNodeList.add(se);
			}
		}

		return geoms;
	}
	/*
	 * AD_LINK打断具体操作
	 * 
	 * @param JSONArray
	 */
	public void breakLine() throws Exception {
		for (int i = 0; i < command.getCatchLinks().size(); i++) {
			JSONObject json = command.getCatchLinks().getJSONObject(i);
			if (json.containsKey("breakNode")) {
				JSONObject breakJson = new JSONObject();
				breakJson.put("objId", json.getInt("linkPid"));
				breakJson.put("projectId", command.getProjectId());
				JSONObject data = new JSONObject();
				data.put("breakNodePid", json.getInt("breakNode"));
				data.put("longitude", json.getDouble("lon"));
				data.put("latitude", json.getDouble("lat"));
				breakJson.put("data", data);
				ICommand breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Process(breakCommand, conn);
				breakProcess.run();
			}
		}
	}
	
	public void createLink(){
		
		
	}

}
