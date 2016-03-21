package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.create;

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
import com.navinfo.dataservice.engine.edit.comm.util.AdminUtils;
import com.navinfo.dataservice.engine.edit.edit.model.IRow;
import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNodeMesh;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.IProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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

		List<Geometry> geomList = null;

		List<JSONObject> seNodeList = new ArrayList<JSONObject>();

		if (command.getCatchLinks().size() > 0) {
			geomList = this.splitsLink(result,command.getCatchLinks(),
					command.getGeometry(),seNodeList);

		} 
		if(command.getCatchLinks().size()==0 || geomList.size()==0){
			geomList = new ArrayList<Geometry>();

			geomList.add(GeoTranslator.geojson2Jts(command.getGeometry()));

			JSONObject se = new JSONObject();

			if (0 == command.getsNodePid()) {

				Coordinate point = geomList.get(0).getCoordinates()[0];

				RdNode node = createNode(point.x, point.y);

				result.insertObject(node, ObjStatus.INSERT);

				se.put("s", node.getPid());
			} else {
				se.put("s", command.getsNodePid());
			}

			if (0 == command.geteNodePid()) {

				Coordinate point = geomList.get(0).getCoordinates()[geomList
						.get(0).getCoordinates().length - 1];

				RdNode node = createNode(point.x, point.y);

				result.insertObject(node, ObjStatus.INSERT);

				se.put("e", node.getPid());
			} else {
				se.put("e", command.geteNodePid());
			}
			
			seNodeList.add(se);
		}

		String msg = null;

		for (int index=0;index<geomList.size();index++) {
			
			Geometry geo = geomList.get(index);
			Set<String> meshes = this.getLinkInterMesh(geo);
			if (meshes.size() == 1) {
				RdLink link = new RdLink();

				int meshId = Integer.parseInt(meshes.iterator().next());

				link.setMesh(meshId);

				link.setMeshId(meshId);

				link.setPid(PidService.getInstance().applyLinkPid());

				result.setPrimaryPid(link.getPid());

				double linkLength = GeometryUtils.getLinkLength(geo);

				check.checkLinkLength(linkLength);

				link.setLength(linkLength);

				link.setGeometry(GeoTranslator.transform(geo, 100000, 0));

				link.setOriginLinkPid(link.getPid());

				link.setWidth(55);

				link.setsNodePid(seNodeList.get(index).getInt("s"));
				
				link.seteNodePid(seNodeList.get(index).getInt("e"));

				setLinkChildren(link);

				link.setKind(command.getKind());

				link.setLaneNum(command.getLaneNum());
				
				AdminUtils.SetAdminInfo4Link(link, conn);

				result.insertObject(link, ObjStatus.INSERT);
			} else {

				Iterator<String> it = meshes.iterator();

				while (it.hasNext()) {
					String meshIdStr = it.next();

					Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo,
							MeshUtils.mesh2Jts(meshIdStr));

					if ("LineString".equals(geomInter.getGeometryType())) {

						RdLink link = new RdLink();

						int meshId = Integer.parseInt(meshIdStr);

						link.setMesh(meshId);

						link.setMeshId(meshId);

						link.setPid(PidService.getInstance().applyLinkPid());

						result.setPrimaryPid(link.getPid());

						link.setGeometry(GeoTranslator.transform(geomInter,
								100000, 0));

						double linkLength = GeometryUtils.getLinkLength(geomInter);

						check.checkLinkLength(linkLength);

						link.setLength(linkLength);

						link.setOriginLinkPid(link.getPid());

						link.setWidth(55);

						link.setsNodePid(seNodeList.get(index).getInt("s"));
						
						link.seteNodePid(seNodeList.get(index).getInt("e"));

						link.setKind(command.getKind());

						link.setLaneNum(command.getLaneNum());
						
						AdminUtils.SetAdminInfo4Link(link, conn);

						setLinkChildren(link);

						result.insertObject(link, ObjStatus.INSERT);

					}
				}

			}
		}
//		this.breakLine();
		
		return msg;
	}

	/**
	 * 维护link的子表
	 * 
	 * @param link
	 */
	private void setLinkChildren(RdLink link) {

		RdLinkForm form = new RdLinkForm();

		form.setLinkPid(link.getPid());

		form.setMesh(link.mesh());

		List<IRow> forms = new ArrayList<IRow>();

		forms.add(form);

		link.setForms(forms);

		RdLinkSpeedlimit speedlimit = new RdLinkSpeedlimit();

		speedlimit.setMesh(link.mesh());

		speedlimit.setLinkPid(link.getPid());

		List<IRow> speedlimits = new ArrayList<IRow>();

		speedlimits.add(speedlimit);

		link.setSpeedlimits(speedlimits);

	}

	/**
	 * 创建一个rdnode
	 * 
	 * @param x
	 *            经度
	 * @param y
	 *            纬度
	 * @return rdnode
	 * @throws Exception
	 */
	private RdNode createNode(double x, double y) throws Exception {

		RdNode node = new RdNode();

		node.setPid(PidService.getInstance().applyNodePid());

		node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y),100000,0));

		node.setMesh(Integer.parseInt(MeshUtils.lonlat2Mesh(x, y)));

		RdNodeForm form = new RdNodeForm();

		form.setNodePid(node.getPid());
		
		form.setMesh(node.mesh());

		List<IRow> forms = new ArrayList<IRow>();

		forms.add(form);

		node.setForms(forms);

		RdNodeMesh mesh = new RdNodeMesh();
		
		mesh.setNodePid(node.getPid());

		mesh.setMeshId(node.mesh());

		List<IRow> meshes = new ArrayList<IRow>();

		meshes.add(mesh);

		node.setMeshes(meshes);

		return node;
	}

	private Set<String> getLinkInterMesh(Geometry linkGeom) throws Exception {
		Set<String> set = new HashSet<String>();

		Coordinate[] cs = linkGeom.getCoordinates();

		for (Coordinate c : cs) {
			set.add(MeshUtils.lonlat2Mesh(c.x, c.y));
		}

		return set;
	}

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

			RdNode node = createNode(x, y);

			result.insertObject(node, ObjStatus.INSERT);

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

					RdNode node = createNode(x, y);

					result.insertObject(node, ObjStatus.INSERT);

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

				RdNode node = createNode(x, y);

				result.insertObject(node, ObjStatus.INSERT);

				se.put("e", node.getPid());
				
				seNodeList.add(se);
			}
		}

		return geoms;
	}

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
				ICommand breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Command(
						breakJson, breakJson.toString());
				com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process(
						breakCommand, conn);
				breakProcess.runNotCommit();
			}
		}
	}

}
