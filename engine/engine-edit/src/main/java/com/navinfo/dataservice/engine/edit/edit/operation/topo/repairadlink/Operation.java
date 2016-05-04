package com.navinfo.dataservice.engine.edit.edit.operation.topo.repairadlink;

import java.sql.Connection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.comm.util.operate.NodeOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	private AdLink updateLink;

	private AdNode enode;

	private AdNode snode;
	
	private Connection conn;

	public Operation(Connection conn, Command command, AdLink updateLink,
			AdNode snode, AdNode enode, Check check) {

		this.conn = conn;

		this.command = command;

		this.updateLink = updateLink;

		this.enode = enode;

		this.snode = snode;

	}
	
	@Override
	public String run(Result result) throws Exception {

		JSONObject content = new JSONObject();

		result.setPrimaryPid(updateLink.getPid());

		content.put("geometry", command.getLinkGeom());

		// 判断端点有没有移动，如果移动，则需要分离节点
		
		JSONArray coords = command.getLinkGeom().getJSONArray("coordinates");

		JSONArray scoord = coords.getJSONArray(0);

		double slon = scoord.getDouble(0);
		double slat = scoord.getDouble(1);
		
		JSONArray ecoord = coords.getJSONArray(coords.size()-1);

		double elon = ecoord.getDouble(0);
		double elat = ecoord.getDouble(1);
		
		Geometry geo = GeoTranslator.transform(updateLink.getGeometry(), 0.00001, 5);
		
		Coordinate[] oldCoords = geo.getCoordinates();
		
		boolean sNodeDepart = checkDepartSNode(oldCoords[0], slon, slat);
		
		boolean eNodeDepart = checkDepartENode(oldCoords[oldCoords.length-1], elon, elat);
		
		com.navinfo.dataservice.engine.edit.edit.operation.topo.departadnode.Process departProcess = null;
		
		if(sNodeDepart || eNodeDepart){
			
			JSONObject json = new JSONObject();
			
			json.put("projectId", command.getProjectId());
			
			JSONObject data = new JSONObject();
			
			data.put("linkPid", updateLink.getPid());
			
			if(sNodeDepart){
				data.put("sNodePid", updateLink.getsNodePid());
				
				data.put("slon", slon);
				
				data.put("slat", slat);
			}
			
			if(eNodeDepart){
				data.put("eNodePid", updateLink.geteNodePid());
				
				data.put("elon", elon);
				
				data.put("elat", elat);
			}
			
			json.put("data", data);
			
			com.navinfo.dataservice.engine.edit.edit.operation.topo.departadnode.Command departCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.departadnode.Command(
					json, json.toString());
			
			departProcess =  new com.navinfo.dataservice.engine.edit.edit.operation.topo.departadnode.Process(departCommand, conn);
	
			departProcess.prepareData();
			
			departProcess.recordData();
			
		}
		
		if (command.getInterLines().size() == 0
				&& command.getInterNodes().size() == 0) {
			//没有挂接到别的link或node上
			
			if(sNodeDepart){ //需要新增节点
				
				AdNode node = NodeOperateUtils.createAdNode(slon, slat);
				
				content.put("sNodePid", node.getPid());
				
				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}
			
			if(eNodeDepart){
				
				AdNode node = NodeOperateUtils.createAdNode(elon, elat);
				
				content.put("eNodePid", node.getPid());
				
				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}
			
		} else if ((command.getInterLines().size() == 1 || command
				.getInterLines().size() == 2)
				&& command.getInterNodes().size() == 0) {
			//只挂接到link上
			
			int sNodePid = updateLink.getsNodePid();
			
			int eNodePid = updateLink.geteNodePid();
			
			if(sNodeDepart){ //需要新增节点
				
				AdNode node = NodeOperateUtils.createAdNode(slon, slat);
				
				content.put("sNodePid", node.getPid());
				
				sNodePid=node.getPid();
				
				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}
			
			if(eNodeDepart){
				
				AdNode node = NodeOperateUtils.createAdNode(slon, slat);
				
				content.put("eNodePid", node.getPid());
				
				eNodePid = node.getPid();
				
				result.insertObject(node, ObjStatus.INSERT, node.pid());
			}

			this.breakLine(sNodePid , eNodePid,result);

		} else if (command.getInterLines().size() == 0
				&& (command.getInterNodes().size() == 1 || command
						.getInterNodes().size() == 2)) {
			// link的一个端点挂接到另外一组link的端点
			for (int i = 0; i < command.getInterNodes().size(); i++) {
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);

				int nodePid = mountNode.getInt("nodePid");

				int pid = mountNode.getInt("pid");

				if (nodePid == updateLink.getsNodePid()) {
					content.put("sNodePid", pid);

					if(!sNodeDepart){ //如果是分离节点，需要保留该node，否则不保留
						result.insertObject(snode, ObjStatus.DELETE, snode.pid());
					}
				} else {
					content.put("eNodePid", pid);

					if(!eNodeDepart){ //如果是分离节点，需要保留该node，否则不保留
						result.insertObject(enode, ObjStatus.DELETE, enode.pid());
					}
				}
			}

		} else if (command.getInterLines().size() == 1
				&& command.getInterNodes().size() == 1) {
			// link的一个端点打断另外一根link、link的一个端点挂接到另外一组link的端点

			for (int i = 0; i < command.getInterNodes().size(); i++) {
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);

				int nodePid = mountNode.getInt("nodePid");

				int pid = mountNode.getInt("pid");

				if (nodePid == updateLink.getsNodePid()) {
					content.put("sNodePid", pid);

					if(!sNodeDepart){ //如果是分离节点，需要保留该node，否则不保留
						result.insertObject(snode, ObjStatus.DELETE, snode.pid());
					}
				} else {
					content.put("eNodePid", pid);

					if(!eNodeDepart){ //如果是分离节点，需要保留该node，否则不保留
						result.insertObject(enode, ObjStatus.DELETE, enode.pid());
					}
				}
			}
			
			int sNodePid = updateLink.getsNodePid();
			
			int eNodePid = updateLink.geteNodePid();
			
			if(content.containsKey("eNodePid")){
				if(sNodeDepart){ //需要新增节点
					
					AdNode node = NodeOperateUtils.createAdNode(slon, slat);
					
					content.put("sNodePid", node.getPid());
					
					sNodePid=node.getPid();
					
					result.insertObject(node, ObjStatus.INSERT, node.pid());
				}
			}
			else{
				if(eNodeDepart){
					
					AdNode node = NodeOperateUtils.createAdNode(elon, elat);
					
					content.put("eNodePid", node.getPid());
					
					eNodePid = node.getPid();
					
					result.insertObject(node, ObjStatus.INSERT, node.pid());
				}
			}
			
			this.breakLine(sNodePid, eNodePid,result);
		} else {
			// 错误请求
		}

		boolean isChanged = updateLink.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
		}

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
			breakJson.put("projectId", command.getProjectId());

			int nodePid = interLine.getInt("nodePid");
			if (nodePid == updateLink.getsNodePid()) {
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
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Command(
					breakJson, breakJson.toString());
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint.Process(
					breakCommand,result, conn);
			breakProcess.innerRun();
		}
	}

	private boolean checkDepartSNode(Coordinate oldPoint, double lon, double lat) throws Exception{
		
		if (lon != oldPoint.x || lat != oldPoint.y) {
			//移动了几何，如果挂接了多条link，需要分离节点
			
			AdNodeSelector selector = new AdNodeSelector(conn);
			
			int count = selector.loadAdLinkCountOnNode(updateLink.getsNodePid());
			
			if(count>1){
				return true;
			}
		}
		
		return false;
	}
	
	private boolean checkDepartENode(Coordinate oldPoint, double lon, double lat) throws Exception{

		if (lon != oldPoint.x || lat != oldPoint.y) {
			//移动了几何，如果挂接了多条link，需要分离节点
			
			AdNodeSelector selector = new AdNodeSelector(conn);
			
			int count = selector.loadAdLinkCountOnNode(updateLink.geteNodePid());
			
			if(count>1){
				return true;
			}
		}
		
		return false;
	}
		
}
