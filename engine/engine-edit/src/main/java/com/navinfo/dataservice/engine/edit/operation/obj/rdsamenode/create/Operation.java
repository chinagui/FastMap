package com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		createRdSameNode(result);

		return msg;
	}

	/**
	 * @param nodeLinkPidMap
	 * @throws Exception
	 */
	private void createRdSameNode(Result result) throws Exception {

		RdSameNode rdSameNode = new RdSameNode();

		rdSameNode.setPid(PidService.getInstance().applyRdSameNodePid());

		JSONArray nodeArray = this.command.getNodeArray();

		// 主点pid
		int mainNodePid = 0;

		String mainTableName = "";

		Map<Integer, ObjType> nodeMap = new HashMap<>();

		// 设置子表rd_samenode_part
		for (int i = 0; i < nodeArray.size(); i++) {
			JSONObject obj = nodeArray.getJSONObject(i);

			int nodePid = obj.getInt("nodePid");

			// 将类似RDNODE转为表名"RD_NODE"
			ObjType objType = ObjType.valueOf(obj.getString("type"));

			String tableName = ReflectionAttrUtils.getTableNameByObjType(objType);

			int isMain = obj.getInt("isMain");

			if (isMain == 1) {
				mainNodePid = nodePid;

				mainTableName = tableName;
			} else {
				nodeMap.put(nodePid, objType);
			}

			RdSameNodePart sameNodePart = new RdSameNodePart();

			sameNodePart.setGroupId(rdSameNode.getPid());

			sameNodePart.setNodePid(nodePid);

			sameNodePart.setTableName(tableName);

			rdSameNode.getParts().add(sameNodePart);
		}

		//更新点的坐标
		updateNodeGeo(mainNodePid, mainTableName.toUpperCase(), nodeMap,result);
		
		result.insertObject(rdSameNode, ObjStatus.INSERT, rdSameNode.getPid());
	}

	/**
	 * 维护点的坐标
	 * 
	 * @param mainNodePid
	 * @param nodeMap
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void updateNodeGeo(int mainNodePid, String tableName, Map<Integer,ObjType > nodeMap,Result result) throws Exception {
		if (mainNodePid != 0 && StringUtils.isNotEmpty(tableName)) {
			RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);
			
			Geometry nodeGeo = sameNodeSelector.getGeoByNodePidAndTableName(mainNodePid, tableName, true);
			
			//组装参数
			JSONObject updateContent = new JSONObject();
			
			updateContent.put("dbId", command.getDbId());

			JSONObject data = new JSONObject();

			data.put("longitude", nodeGeo.getCoordinate().x);

			data.put("latitude", nodeGeo.getCoordinate().y);

			updateContent.put("data", data);
			
			for(Map.Entry<Integer,ObjType > entry : nodeMap.entrySet())
			{
				int nodePid = entry.getKey();
				
				ObjType type = entry.getValue();
				
				updateContent.put("objId", nodePid);
				
				switch (type) {
				case RDNODE:
					com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(
							updateContent, command.getRequester());
					com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process process = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process(
							updatecommand, result, conn);
					process.innerRun();
					break;
				case ADNODE:
					com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command adCommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command(
							updateContent, command.getRequester());
					com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process adProcess = new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process(
							adCommand, result, conn);
					adProcess.innerRun();
					break;
				case ZONENODE:
					com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command zoneCommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command(
							updateContent, command.getRequester());
					com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process zoneProcess = new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process(
							zoneCommand, result, conn);
					zoneProcess.innerRun();
					break;
				case RWNODE:
					com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command rwCommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command(
							updateContent, command.getRequester());
					com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process rwProcess = new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process(
							rwCommand, result, conn);
					rwProcess.innerRun();
					break;
				case LUNODE:
					com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command luCommand = new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command(
							updateContent, command.getRequester());
					com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process luProcess = new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process(
							luCommand, result, conn);
					luProcess.innerRun();
					break;
				default:
					break;
				}
			}
		}
	}
}
