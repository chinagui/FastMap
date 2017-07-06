package com.navinfo.dataservice.engine.edit.operation.topo.topobreakin;

import java.sql.Connection;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.topo.topobreakin.Command;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;
	private Connection conn;
	private RdNodeSelector nodeSelector;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
		nodeSelector = new RdNodeSelector(conn);
	}

	@Override
	public String run(Result result) throws Exception{
		 return this.breakLine(result);
	}

	private String breakLine(Result result) throws Exception {
		String msgResult = "";
		for (int linkPid : this.command.getLinkPids()) {
			JSONObject breakLinkJson = getBreaksPara(linkPid);

			com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
					breakLinkJson, breakLinkJson.toString());
			com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
					breakCommand, conn, result);
			String msg = breakProcess.innerRun();
			updateBreakPoinNodePid(msg);
				
			msgResult += msg;
		}
		return msgResult;
	}

	/**
	 * 组装每一条link打断需要的数据
	 * 
	 * @param linkPid
	 * @return
	 */
	private JSONObject getBreaksPara(int linkPid) throws Exception {
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", linkPid);
		breakJson.put("dbId", this.command.getDbId());

		JSONObject data = new JSONObject();
		JSONArray breakNodes = new JSONArray();

		if (this.command.getBreakNodePid() != 0) {
			data.put("breakNodePid", this.command.getBreakNodePid());
			data.put("longitude", this.command.getBreakPoint().getX());
			data.put("latitude", this.command.getBreakPoint().getY());

			//输入有nodePid，取nodePid处为打断点；输入无nodePid，第一次打断后记录nodePid，仍取输入点几何为打断点
			Coordinate coor = null;
			if (this.command.getIsModifyGeo() == false) {
				RdNode rdNode = (RdNode) nodeSelector.loadById(this.command.getBreakNodePid(), false);
				Geometry nodeGeo = GeoTranslator.transform(rdNode.getGeometry(), 0.00001, 5);
				coor = nodeGeo.getCoordinate();
			} else {
				coor = new Coordinate((double) Math.round(this.command.getBreakPoint().getX() * 100000) / 100000,
						(double) Math.round(this.command.getBreakPoint().getY() * 100000) / 100000);
			}

			JSONObject breakObj = new JSONObject();
			breakObj.put("longitude", coor.x);
			breakObj.put("latitude", coor.y);
			breakObj.put("breakNodePid", this.command.getBreakNodePid());
			breakObj.put("operate", OperType.TOPOBREAK);
			breakNodes.add(breakObj);
			data.put("breakNodes", breakNodes);
		} else {
			data.put("longitude", this.command.getBreakPoint().getX());
			data.put("latitude", this.command.getBreakPoint().getY());
		}

		breakJson.put("data", data);
		return breakJson;
	}

	/**
	 * 没有打断nodePid，第一次打断后，给打断点赋nodePid，记录该nodePid，用于剩余link打断
	 * @param msg
	 */
	private void updateBreakPoinNodePid(String msg) {
		if (this.command.getBreakNodePid() == 0 && msg != null && msg.isEmpty() == false) {
			JSONArray array = JSONArray.fromObject(msg);
			if (array.size() != 2) {
				return;
			}
			JSONObject firstLink = array.getJSONObject(0);
			JSONObject secondLink = array.getJSONObject(1);

			int firstLinkSNode = firstLink.getInt("sNodePid");
			int firstLinkENode = firstLink.getInt("eNodePid");

			int secondLinkSNode = secondLink.getInt("sNodePid");
			int secondLinkENode = secondLink.getInt("eNodePid");

			if (firstLinkSNode == secondLinkENode) {
				this.command.setBreakNodePid(firstLinkSNode);
			}
			if (firstLinkENode == secondLinkSNode) {
				this.command.setBreakNodePid(firstLinkENode);
			}
			this.command.setIsModifyGeo(true);
		}
	}
}
