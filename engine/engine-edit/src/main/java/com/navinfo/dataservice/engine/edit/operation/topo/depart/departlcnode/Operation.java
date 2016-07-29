package com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	private LcLink updateLink;

	private Check check;

	public Operation(Command command, LcLink updateLink, Check check) {
		this.command = command;
		this.updateLink = updateLink;
		this.check = check;
	}

	@Override
	public String run(Result result) throws Exception {
		this.updateLinkGeomtry(result);
		this.updateNodeGeometry(result);
		this.updateFaceGeometry(result);
		return null;
	}

	private void updateLinkGeomtry(Result result) throws Exception {
		Geometry geom = GeoTranslator.transform(updateLink.getGeometry(), 0.00001, 5);
		Coordinate[] cs = geom.getCoordinates();
		double[][] ps = new double[cs.length][2];
		for (int i = 0; i < cs.length; i++) {
			ps[i][0] = cs[i].x;
			ps[i][1] = cs[i].y;
		}
		if (command.getsNodePid() > 0) {
			ps[0][0] = command.getSlon();
			ps[0][1] = command.getSlat();
		}
		if (command.geteNodePid() > 0) {
			ps[ps.length - 1][0] = command.getElon();
			ps[ps.length - 1][1] = command.getElat();
		}
		check.checkPointCoincide(ps);
		check.checkShapePointDistance(ps);
		JSONObject geojson = new JSONObject();
		geojson.put("type", "LineString");
		geojson.put("coordinates", ps);
		JSONObject updateContent = new JSONObject();
		updateContent.put("geometry", geojson);
		updateLink.fillChangeFields(updateContent);
		result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
	}

	private void updateNodeGeometry(Result result) throws Exception {
		if (command.getsNodePid() > 0) {
			RdNode node = NodeOperateUtils.createRdNode(command.getSlon(), command.getSlat());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
		}
		if (command.geteNodePid() > 0) {
			RdNode node = NodeOperateUtils.createRdNode(command.getElon(), command.getElat());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
		}

	}

	private void updateFaceGeometry(Result result) throws Exception {
		if (command.getsNodePid() > 0) {
			RdNode node = NodeOperateUtils.createRdNode(command.getSlon(), command.getSlat());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
		}
		if (command.geteNodePid() > 0) {
			RdNode node = NodeOperateUtils.createRdNode(command.getElon(), command.getElat());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
		}
	}

}
