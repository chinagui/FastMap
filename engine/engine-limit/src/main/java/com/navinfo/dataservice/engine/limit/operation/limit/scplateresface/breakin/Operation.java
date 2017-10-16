package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.breakin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command = null;
	
	private Connection conn = null;

	public Operation(Command command,Connection conn) {

		this.command = command;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		
		List<JSONArray> breaklinks = this.breakpoint(this.command.getFace().getGeometry(),
				this.command.getBreakpoint());

		createNewLinks(result, breaklinks);

		for (ScPlateresFace newface : this.command.getNewFaces()) {
			result.insertObject(newface, ObjStatus.INSERT, newface.getGeometryId());
		}

		result.insertObject(this.command.getFace(), ObjStatus.DELETE, this.command.getFace().getGeometryId());

		return null;
	}

	private void createNewLinks(Result result, List<JSONArray> arrays) throws Exception {
		for (JSONArray array : arrays) {
			// 组装几何
			JSONObject geojson = new JSONObject();

			geojson.put("type", "LineString");

			geojson.put("coordinates", array);

			ScPlateresFace newface = new ScPlateresFace();

			// 申請pid
			newface.setGeometryId(PidApply.getInstance(conn)
					.pidForInsertGeometry(this.command.getFace().getGeometryId(), LimitObjType.SCPLATERESRDLINK));

			newface.setBoundaryLink(this.command.getFace().getBoundaryLink());

			newface.setGroupId(this.command.getFace().getGroupId());

			newface.setGeometry(GeoTranslator.geojson2Jts(geojson));

			this.command.getNewFaces().add(newface);

		}
	}

	private List<JSONArray> breakpoint(Geometry geometry, Point point) throws Exception {

		JSONObject geojson = GeoTranslator.jts2Geojson(geometry);
		List<JSONArray> arrays = new ArrayList<JSONArray>();
		double lon = point.getCoordinate().x * 100000;
		double lat = point.getCoordinate().y * 100000;
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		boolean hasFound = false;// 打断的点是否和形状点重合或者是否在线段上
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
			if (!hasFound) {
				// 打断点和形状点重合
				if (Math.abs(lon - jaPE.getDouble(0)) < 0.0000001 && Math.abs(lat - jaPE.getDouble(1)) < 0.0000001) {
					ja1.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) }, new double[] { lon, lat })) {
					ja1.add(new double[] { lon, lat });
					ja2.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					ja1.add(jaPE);
				}
			} else {
				ja2.add(jaPS);
			}
			if (i == jaLink.size() - 2) {
				ja2.add(jaPE);
			}
		}
		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}
		// 生成新的link
		arrays.add(ja1);
		arrays.add(ja2);
		return arrays;
	}
}
