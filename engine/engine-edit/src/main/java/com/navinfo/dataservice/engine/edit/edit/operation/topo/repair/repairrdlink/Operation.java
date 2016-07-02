package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdGscOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;
	
	private Map<Integer, List<RdLink>> map;

	public Map<Integer, List<RdLink>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<RdLink>> map) {
		this.map = map;
	}

	public Operation(Connection conn, Command command) {

		this.conn = conn;

		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		Map<Integer, List<RdLink>> map = new HashMap<Integer, List<RdLink>>();
		List<RdLink> links = new ArrayList<RdLink>();
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(GeoTranslator.geojson2Jts(command.getLinkGeom()));
		if (meshes.size() == 1) {
			JSONObject content = new JSONObject();
			result.setPrimaryPid(this.command.getUpdateLink().getPid());
			content.put("geometry", command.getLinkGeom());
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(content);
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE, this.command.getLinkPid());
			}
			//拷贝原link，set属性
			RdLink link = new RdLink();
			link.copy(this.command.getUpdateLink());
			link.setGeometry(GeoTranslator.geojson2Jts(this.command.getLinkGeom(),100000,0));
			links.add(link);
		} else {
			Iterator<String> it = meshes.iterator();
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			Geometry g = GeoTranslator.transform(this.command.getUpdateLink().getGeometry(), 0.00001, 5);
			maps.put(g.getCoordinates()[0], this.command.getUpdateLink().getsNodePid());
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1], this.command.getUpdateLink().geteNodePid());
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = GeoTranslator.transform(MeshUtils.linkInterMeshPolygon(
						GeoTranslator.geojson2Jts(command.getLinkGeom()), MeshUtils.mesh2Jts(meshIdStr)), 1, 5);
				links.addAll(RdLinkOperateUtils.getCreateRdLinksWithMesh(geomInter, maps, result));

			}
			result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE, this.command.getLinkPid());
		}
		// 处理对立交的影响
		if (CollectionUtils.isNotEmpty(this.command.getGscList())) {
			handleEffectOnRdGsc(this.command.getGscList(), links, result);
		}
		map.put(this.command.getLinkPid(), links);
		this.map = map;
		return null;
	}
	
	public void breakLine(int sNodePid, int eNodePid,Result result) throws Exception {

		JSONArray coords = command.getLinkGeom().getJSONArray("coordinates");

		for (int i = 0; i < command.getInterLines().size(); i++) {
			// link的一个端点打断另外一根link
			JSONObject interLine = command.getInterLines().getJSONObject(i);
			JSONObject breakJson = new JSONObject();
			JSONObject data = new JSONObject();

			breakJson.put("objId", interLine.getInt("pid"));
			breakJson.put("dbId", command.getDbId());

			int nodePid = interLine.getInt("nodePid");
			if (nodePid == command.getUpdateLink().getsNodePid()) {
				data.put("breakNodePid", sNodePid);

				JSONArray coord = coords.getJSONArray(0);

				double lon = coord.getDouble(0);
				double lat = coord.getDouble(1);

				data.put("longitude", lon);
				data.put("latitude", lat);
			} else {
				data.put("breakNodePid", eNodePid);

				JSONArray coord = coords.getJSONArray(coords.size() - 1);

				double lon = coord.getDouble(0);
				double lat = coord.getDouble(1);

				data.put("longitude", lon);
				data.put("latitude", lat);
			}
			breakJson.put("data", data);
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrdpoint.Command(
					breakJson, breakJson.toString());
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrdpoint.Process(
					breakCommand, conn,result);
			breakProcess.innerRun();
		}
	}
	
	/**
	 * 处理对立交的影响
	 * @param gscList
	 * @param linkList
	 * @param result
	 * @throws Exception
	 */
	private void handleEffectOnRdGsc(List<RdGsc> gscList, List<RdLink> linkList, Result result) throws Exception {
		for (RdGsc gsc : gscList) {
			Geometry gscGeo = gsc.getGeometry();

			for (RdLink link : linkList) {
				Geometry linkGeo = link.getGeometry();

				if (gscGeo.distance(linkGeo) < 1) {
					List<IRow> gscLinkList = gsc.getLinks();

					if (gscLinkList.size() == 1) {
						RdGscLink gscLink = (RdGscLink) gscLinkList.get(0);

						gscLink.setLinkPid(link.getPid());

						// 计算立交点序号和起终点标识
						RdGscOperateUtils.calShpSeqNum(gscLink, gscGeo, linkGeo.getCoordinates());
						
						if (!gscLink.changedFields().isEmpty()) {
							result.insertObject(gscLink, ObjStatus.UPDATE, gsc.getPid());
						}
					}
				}
			}
		}
	}
}
