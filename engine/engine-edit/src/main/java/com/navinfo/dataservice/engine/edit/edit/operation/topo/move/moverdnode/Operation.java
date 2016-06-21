package com.navinfo.dataservice.engine.edit.edit.operation.topo.move.moverdnode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	private RdNode updateNode;

	public Operation(Command command, RdNode updateNode) {
		this.command = command;

		this.updateNode = updateNode;
	}

	@Override
	public String run(Result result) throws Exception {

		result.setPrimaryPid(updateNode.getPid());
		
		this.updateNodeGeometry(result);
		
		this.updateLinkGeomtry(result);

		return null;
	}

	private void updateLinkGeomtry(Result result) throws Exception {

		for (RdLink link : command.getLinks()) {

			Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);

			Coordinate[] cs = geom.getCoordinates();

			double[][] ps = new double[cs.length][2];

			for (int i = 0; i < cs.length; i++) {
				ps[i][0] = cs[i].x;

				ps[i][1] = cs[i].y;
			}

			if (link.getsNodePid() == command.getNodePid()) {
				ps[0][0] = command.getLongitude();

				ps[0][1] = command.getLatitude();
			} else {
				ps[ps.length - 1][0] = command.getLongitude();

				ps[ps.length - 1][1] = command.getLatitude();
			}

			JSONObject geojson = new JSONObject();

			geojson.put("type", "LineString");

			geojson.put("coordinates", ps);

			JSONObject updateContent = new JSONObject();

			updateContent.put("geometry", geojson);

			link.fillChangeFields(updateContent);

			result.insertObject(link, ObjStatus.UPDATE, link.pid());
		}
	}

	private void updateNodeGeometry(Result result) throws Exception {
		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });

		JSONObject updateContent = new JSONObject();

		updateContent.put("geometry", geojson);

		updateNode.fillChangeFields(updateContent);
		
		result.insertObject(updateNode, ObjStatus.UPDATE, updateNode.pid());
	}
}
