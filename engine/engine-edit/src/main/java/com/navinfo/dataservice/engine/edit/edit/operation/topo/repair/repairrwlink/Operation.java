package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrwlink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RwLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

    private Map<Integer,List<RwLink>> map ;
	public Map<Integer, List<RwLink>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<RwLink>> map) {
		this.map = map;
	}

	public Operation(Command command) {

		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {
		//修行修改线信息
		this.updateLink(result);
		return null;
	}
	/**
	 * 修改线的信息
	 * @param result
	 * @throws Exception
	 */
	private void updateLink(Result result) throws Exception {
		Map<Integer, List<RwLink>> map = new HashMap<Integer, List<RwLink>>();
		List<RwLink> links = new ArrayList<RwLink>();
		Set<String> meshes = CompGeometryUtil
				.geoToMeshesWithoutBreak(GeoTranslator.geojson2Jts(command
						.getLinkGeom()));
		if (meshes.size() == 1) {
			JSONObject content = new JSONObject();
			result.setPrimaryPid(this.command.getUpdateLink().getPid());
			content.put("geometry", command.getLinkGeom());
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(content);
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE,
						this.command.getLinkPid());
			}
			
			links.add(this.command.getUpdateLink());
		}
		else {
			Iterator<String> it = meshes.iterator();
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			Geometry g= GeoTranslator.transform(this.command.getUpdateLink().getGeometry(),0.00001,5);
			maps.put( g.getCoordinates()[0], this.command.getUpdateLink().getsNodePid());
			maps.put(g.getCoordinates()[g.getCoordinates().length-1], this.command.getUpdateLink().geteNodePid());
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = GeoTranslator.transform(MeshUtils
						.linkInterMeshPolygon(GeoTranslator
								.geojson2Jts(command.getLinkGeom()),
								MeshUtils.mesh2Jts(meshIdStr)), 1, 5);
				links.addAll(RwLinkOperateUtils.getCreateRwLinksWithMesh(geomInter, maps,result));

			}
			result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE, this.command.getLinkPid());
		}
		map.put(this.command.getLinkPid(), links);
		this.map = map;
	}
	
}
