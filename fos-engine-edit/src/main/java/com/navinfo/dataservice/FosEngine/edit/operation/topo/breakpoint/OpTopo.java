package com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint;

import java.sql.Connection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.vividsolutions.jts.geom.Point;

public class OpTopo implements IOperation {

	private Command command;

	private RdLink rdLinkBreakpoint;

	private JSONArray jaDisplayLink;

	private Connection conn;

	public OpTopo(Command command, Connection conn, RdLink rdLinkBreakpoint,
			JSONArray jaDisplayLink) {
		this.command = command;

		this.rdLinkBreakpoint = rdLinkBreakpoint;

		this.jaDisplayLink = jaDisplayLink;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		this.breakpoint(result);

		RdNode breakpoint = new RdNode();

		breakpoint.setPid(PidService.getInstance().applyNodePid());

		breakpoint.copy(command.getsNode());

		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getPoint().getX(),
				command.getPoint().getY() });

		breakpoint.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));

		result.insertObject(breakpoint, ObjStatus.INSERT);

		command.getLink1().seteNodePid(breakpoint.getPid());

		command.getLink2().setsNodePid(breakpoint.getPid());

		result.insertObject(command.getLink1(), ObjStatus.INSERT);

		result.insertObject(command.getLink2(), ObjStatus.INSERT);

		jaDisplayLink.add(command.getLink1().Serialize(ObjLevel.BRIEF));

		jaDisplayLink.add(command.getLink2().Serialize(ObjLevel.BRIEF));

		return jaDisplayLink.toString();
	}

	private void breakpoint(Result result) throws Exception {

		JSONObject geojson = GeoTranslator.jts2Geojson(rdLinkBreakpoint
				.getGeometry());

		Point point = command.getPoint();

		double lon = point.getX() * 100000;

		double lat = point.getY() * 100000;

		JSONArray ja1 = new JSONArray();

		JSONArray ja2 = new JSONArray();

		JSONArray jaLink = geojson.getJSONArray("coordinates");

		boolean hasFound = false;

		for (int i = 0; i < jaLink.size() - 1; i++) {

			JSONArray jaPS = jaLink.getJSONArray(i);

			if (i == 0) {
				ja1.add(jaPS);
			}

			JSONArray jaPE = jaLink.getJSONArray(i + 1);

			if (GeoTranslator.isIntersection(
					new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
					new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
					new double[] { lon, lat })) {
				ja1.add(new double[] { lon, lat });
				ja2.add(new double[] { lon, lat });
				hasFound = true;
			} else if (!hasFound) {
				ja1.add(jaPS);
			} else {
				ja2.add(jaPE);
			}

			if (i == jaLink.size() - 2) {
				ja2.add(jaPE);
			}
		}

		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}

		JSONObject geojson1 = new JSONObject();

		geojson1.put("type", "LineString");

		geojson1.put("coordinates", ja1);

		JSONObject geojson2 = new JSONObject();

		geojson2.put("type", "LineString");

		geojson2.put("coordinates", ja2);

		RdLink link1 = new RdLink();

		link1.setPid(PidService.getInstance().applyLinkPid());

		link1.copy(rdLinkBreakpoint);

		link1.setGeometry(GeoTranslator.geojson2Jts(geojson1));

		command.setLink1(link1);

		RdLink link2 = new RdLink();

		link2.setPid(PidService.getInstance().applyLinkPid());

		link2.copy(rdLinkBreakpoint);

		link2.setGeometry(GeoTranslator.geojson2Jts(geojson2));

		command.setLink2(link2);

	}

}
