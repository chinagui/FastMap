package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONObject;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.ZoneLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;
    private Map<Integer,List<ZoneLink>> map ;
	public Map<Integer, List<ZoneLink>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<ZoneLink>> map) {
		this.map = map;
	}

	public Operation(Connection conn, Command command) {

		this.conn = conn;

		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {
		//修行修改线信息
		this.updateLink(result);
		//修行修改面信息
		this.updateFace(result);
		return null;
	}
	/**
	 * 修改线的信息
	 * @param result
	 * @throws Exception
	 */
	private void updateLink(Result result) throws Exception {
		Map<Integer, List<ZoneLink>> map = new HashMap<Integer, List<ZoneLink>>();
		List<ZoneLink> links = new ArrayList<ZoneLink>();
		Set<String> meshes = CompGeometryUtil
				.geoToMeshesWithoutBreak(GeoTranslator.geojson2Jts(command
						.getLinkGeom()));
		if (meshes.size() == 1) {
			JSONObject content = new JSONObject();
			result.setPrimaryPid(this.command.getUpdateLink().getPid());
			content.put("geometry", command.getLinkGeom());
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(content);
			
			//需要往map中加入的对象，拷贝command.getUpdateLink()
			ZoneLink link = new ZoneLink();
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE,
						this.command.getLinkPid());
				
				link.copy(this.command.getUpdateLink());
				
				link.setGeometry(GeoTranslator.geojson2Jts(command
						.getLinkGeom(),100000,0));
			}
			
			links.add(link);
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
				links.addAll(ZoneLinkOperateUtils.getCreateZoneLinksWithMesh(geomInter, maps,result));

			}
			result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE, this.command.getLinkPid());
		}
		map.put(this.command.getLinkPid(), links);
		this.map = map;
	}
	/**
	 * 修改面的信息
	 * @param result
	 * @throws Exception
	 */
	private void updateFace(Result result) throws Exception{
		if (command.getFaces() != null && command.getFaces().size() > 0) {
			for (ZoneFace face : command.getFaces()) {
				boolean flag = false;
				List<ZoneLink> links = new ArrayList<ZoneLink>();
				for (IRow iRow : face.getFaceTopos()) {
					ZoneFaceTopo obj = (ZoneFaceTopo) iRow;
				    if(this.map.containsKey(obj.getLinkPid())){
				    	if(this.map.get(obj.getLinkPid()).size() > 1){
				    		flag =true;
						}
				    	links.addAll(this.map.get(obj.getLinkPid()));
				    }else{
				    	links.add((ZoneLink) new ZoneLinkSelector(conn).loadById(
							obj.getLinkPid(), true));
				    }
					
					result.insertObject(obj, ObjStatus.DELETE, face.getPid());
					
				}
				if(flag){
					//如果跨图幅需要重新生成面并且删除原有面信息
					com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation(result);
					List<IObj> objs = new ArrayList<IObj>();
					objs.addAll(links);
					opFace.createFaceByZoneLink(objs);
					result.insertObject(face, ObjStatus.DELETE, face.getPid());
				}
				else{
					//如果不跨图幅只需要维护面的行政几何
					com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation(result,face);
					opFace.reCaleFaceGeometry(links);
				}
				
		}
	
		}
	}
	
}
