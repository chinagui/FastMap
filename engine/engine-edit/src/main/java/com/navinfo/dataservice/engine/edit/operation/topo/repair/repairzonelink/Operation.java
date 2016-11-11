package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;
	private Map<Integer, List<ZoneLink>> map;

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
		// 修行修改线信息
		this.updateLink(result);
		// 修行修改面信息
		this.updateFace(result);
		return null;
	}

	/**
	 * 修改线的信息
	 * 
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
			Geometry geo = GeoTranslator.geojson2Jts(command.getLinkGeom());
			double length = 0;
			if (null != geo)
				length = GeometryUtils.getLinkLength(geo);
			content.put("length", length);
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(
					content);

			// 需要往map中加入的对象，拷贝command.getUpdateLink()
			ZoneLink link = new ZoneLink();
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(),
						ObjStatus.UPDATE, this.command.getUpdateLink().getPid());
				link.setPid(this.command.getUpdateLink().getPid());
				link.copy(this.command.getUpdateLink());

				link.setGeometry(GeoTranslator.geojson2Jts(
						command.getLinkGeom(), 100000, 0));
			}

			links.add(link);
		} else {
			Iterator<String> it = meshes.iterator();
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			Geometry g = GeoTranslator.transform(this.command.getUpdateLink()
					.getGeometry(), 0.00001, 5);
			maps.put(g.getCoordinates()[0], this.command.getUpdateLink()
					.getsNodePid());
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1],
					this.command.getUpdateLink().geteNodePid());
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(GeoTranslator
						.geojson2Jts(command.getLinkGeom()), GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),1,5));
				if(geomInter instanceof GeometryCollection)
				{
					int geoNum = geomInter.getNumGeometries();
					for (int i = 0; i < geoNum; i++) {
						Geometry subGeo = geomInter.getGeometryN(i);
						if (subGeo instanceof LineString) {
							subGeo = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(subGeo), 1, 5);

							links.addAll(ZoneLinkOperateUtils.getCreateZoneLinksWithMesh(subGeo, maps, result, this.command.getUpdateLink()));
						}
					}
				}
				else
				{
					geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);

					links.addAll(ZoneLinkOperateUtils.getCreateZoneLinksWithMesh(geomInter, maps, result,this.command.getUpdateLink()));
				}
			}
			result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE,
					this.command.getUpdateLink().getPid());
		}

		updataRelationObj(links, result);

		map.put(this.command.getLinkPid(), links);
		this.map = map;
	}

	/**
	 * 修改面的信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateFace(Result result) throws Exception {
		if (command.getFaces() != null && command.getFaces().size() > 0) {
			for (ZoneFace face : command.getFaces()) {
				boolean flag = false;
				List<ZoneLink> links = new ArrayList<ZoneLink>();
				for (IRow iRow : face.getFaceTopos()) {
					ZoneFaceTopo obj = (ZoneFaceTopo) iRow;
					if (this.map.containsKey(obj.getLinkPid())) {
						if (this.map.get(obj.getLinkPid()).size() > 1) {
							flag = true;
						}
						links.addAll(this.map.get(obj.getLinkPid()));
					} else {
						links.add((ZoneLink) new ZoneLinkSelector(conn)
								.loadById(obj.getLinkPid(), true));
					}

					result.insertObject(obj, ObjStatus.DELETE, face.getPid());

				}
				if (flag) {
					// 如果跨图幅需要重新生成面并且删除原有面信息
					com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation(
							result);
					List<IObj> objs = new ArrayList<IObj>();
					objs.addAll(links);
					opFace.createFaceByZoneLink(objs);
					result.insertObject(face, ObjStatus.DELETE, face.getPid());
				} else {
					// 如果不跨图幅只需要维护面的行政几何
					com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Operation(
							result, face);
					opFace.reCaleFaceGeometry(links);
				}

			}

		}
	}

	/**
	 * 维护关联要素
	 * 
	 * @throws Exception
	 */
	private void updataRelationObj(List<ZoneLink> links, Result result)
			throws Exception {

		if (links.size() == 1) {
			if (!this.command.getOperationType().equals("innerRun")) {
				// 维护同一线
				com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation samelinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
						this.conn);

				samelinkOperation.repairLink(links.get(0),
						this.command.getRequester(), result);
			}
		}
	}
}
