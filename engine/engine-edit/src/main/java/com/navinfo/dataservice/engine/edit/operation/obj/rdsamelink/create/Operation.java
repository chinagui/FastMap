package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.vividsolutions.jts.geom.Geometry;

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

		create(result);

		return msg;
	}

	private void create(Result result) throws Exception {

		RdSameLink rdSameLink = new RdSameLink();

		rdSameLink.setPid(PidService.getInstance().applyRdSameLinkPid());

		JSONArray linkArray = this.command.getLinkArray();

		// 主pid
		int mainPid = 0;

		String mainTableName = "";

		// 从link。 key：linkpid；value：要素类型
		Map<Integer, ObjType> linkMap = new HashMap<>();

		// 设置子表
		for (int i = 0; i < linkArray.size(); i++) {

			JSONObject obj = linkArray.getJSONObject(i);

			int linkPid = obj.getInt("linkPid");

			ObjType objType = ObjType.valueOf(obj.getString("type"));

			String tableName = ReflectionAttrUtils
					.getTableNameByObjType(objType);

			int isMain = obj.getInt("isMain");

			if (isMain == 1) {
				mainPid = linkPid;

				mainTableName = tableName;
			} else {
				linkMap.put(linkPid, objType);
			}

			RdSameLinkPart sameLinkPart = new RdSameLinkPart();

			sameLinkPart.setGroupId(rdSameLink.getPid());

			sameLinkPart.setLinkPid(linkPid);

			sameLinkPart.setTableName(tableName);

			rdSameLink.getParts().add(sameLinkPart);
		}
		
		result.insertObject(rdSameLink, ObjStatus.INSERT, rdSameLink.getPid());
		
		// 更新坐标
		updateLinkGeo(mainPid, mainTableName.toUpperCase(), linkMap, result);
	}

	private void updateLinkGeo(int linkPid, String tableName,
			Map<Integer, ObjType> linkMap, Result result) throws Exception {

		RdSameLinkSelector selector = new RdSameLinkSelector(this.conn);

		Geometry linkGeometry = selector.getMainLinkGeometry(linkPid,
				tableName, false);

		if(linkGeometry==null)
		{
			return;
		}
		// 组装参数
		JSONObject updateContent = new JSONObject();

		updateContent.put("dbId", command.getDbId());

		JSONObject data = new JSONObject();

		data.put("interLinks", new JSONArray());

		data.put("interNodes", new JSONArray());

		JSONObject geometry = GeoTranslator.jts2Geojson(linkGeometry);

		data.put("geometry", geometry);

		updateContent.put("data", data);

		for (Map.Entry<Integer, ObjType> entry : linkMap.entrySet()) {

			ObjType type = entry.getValue();

			updateContent.put("objId", entry.getKey());

			repairLink(type, updateContent, result);
		}
	}

	/**
	 * 调用点的移动接口，维护点的坐标
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void repairLink(ObjType type, JSONObject updateContent,
			Result result) throws Exception {
		switch (type) {
		case LULINK:
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command luCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command(
					updateContent, command.getRequester());

			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process luProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process(
					luCommand, result, conn);

			luProcess.innerRun();
			break;
		case ADLINK:
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command adCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command(
					updateContent, command.getRequester());

			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process adProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process(
					adCommand, result, conn);

			adProcess.innerRun();
			break;
		case ZONELINK:
			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command zoneCommand = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command(
					updateContent, command.getRequester());

			com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process zoneProcess = new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process(
					zoneCommand, result, conn);

			zoneProcess.innerRun();
			break;

		default:
			break;
		}
	}

}
