package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class OpTopo implements IOperation {

	private Command command;

	private RdLink breakLink;

	private JSONArray jaDisplayLink;

	public OpTopo(Command command, Connection conn, RdLink rdLinkBreakpoint,
			JSONArray jaDisplayLink) {
		this.command = command;

		this.breakLink = rdLinkBreakpoint;

		this.jaDisplayLink = jaDisplayLink;

	}

	@Override
	public String run(Result result) throws Exception {

		this.breakpoint(result);

		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getPoint().getX(),
				command.getPoint().getY() });

		if (command.getBreakNodePid() == 0) {

			RdNode node = NodeOperateUtils.createRdNode(command.getPoint()
					.getX(), command.getPoint().getY());

			result.insertObject(node, ObjStatus.INSERT, node.pid());

			command.setBreakNodePid(node.getPid());

			command.setBreakNode(node);
		}
		

		result.setPrimaryPid(command.getBreakNodePid());

		command.getLink1().seteNodePid(command.getBreakNodePid());

		command.getLink2().setsNodePid(command.getBreakNodePid());

		result.insertObject(command.getLink1(), ObjStatus.INSERT, command
				.getLink1().pid());

		result.insertObject(command.getLink2(), ObjStatus.INSERT, command
				.getLink2().pid());

		jaDisplayLink.add(command.getLink1().Serialize(ObjLevel.BRIEF));

		jaDisplayLink.add(command.getLink2().Serialize(ObjLevel.BRIEF));

		return jaDisplayLink.toString();
	}

	private void breakpoint(Result result) throws Exception {

		JSONObject geojson = GeoTranslator.jts2Geojson(breakLink
				.getGeometry());

		Point point = command.getPoint();

		double lon = point.getCoordinate().x*100000;
		double lat = point.getCoordinate().y*100000;

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

			if (!hasFound) {

				// 打断点和形状点重合
				if (Math.abs(lon - jaPE.getDouble(0)) < 0.0000001
						&& Math.abs(lat - jaPE.getDouble(1)) < 0.0000001) {
					ja1.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					ja1.add(new double[] { lon, lat });
					ja2.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					if (i > 0) {
						ja1.add(jaPS);
					}

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

		JSONObject geojson1 = new JSONObject();

		geojson1.put("type", "LineString");

		geojson1.put("coordinates", ja1);

		JSONObject geojson2 = new JSONObject();

		geojson2.put("type", "LineString");

		geojson2.put("coordinates", ja2);

		RdLink link1 = new RdLink();

		link1.setPid(PidUtil.getInstance().applyLinkPid());
		
		link1.copy(breakLink);		

		link1.setGeometry(GeoTranslator.geojson2Jts(geojson1));

		double length1 = GeometryUtils.getLinkLength(GeoTranslator.transform(
				link1.getGeometry(), 0.00001, 5));

		link1.setLength(length1);

		command.setLink1(link1);

		RdLink link2 = new RdLink();

		link2.setPid(PidUtil.getInstance().applyLinkPid());
		
		link2.copy(breakLink);

		link2.setGeometry(GeoTranslator.geojson2Jts(geojson2));

		double length2 = GeometryUtils.getLinkLength(GeoTranslator.transform(
				link2.getGeometry(), 0.00001, 5));

		link2.setLength(length2);

		command.setLink2(link2);

	}
}