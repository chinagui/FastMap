package com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Check {

	private Command command;

	public Check(Command command) {
		this.command = command;
	}

	public void checkNode(Connection conn) throws Exception {
		JSONArray nodeArray = this.command.getNodeArray();

		if (nodeArray == null) {
			throw new Exception("同一点关系组成node不能为空");
		} else if (nodeArray.size() < 2) {
			throw new Exception("同一点关系组成node不能少于2个");
		}

		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);

		// key：tableName,value:nodePid
		Map<String, StringBuilder> nodePids = new HashMap<>();

		for (int i = 0; i < nodeArray.size(); i++) {
			JSONObject obj = nodeArray.getJSONObject(i);

			int nodePid = obj.getInt("nodePid");

			String tableName = ReflectionAttrUtils.getTableNameByObjType(ObjType.valueOf(obj.getString("type")));

			if (nodePids.get(tableName) != null) {
				nodePids.get(tableName).append("," + nodePid);
			} else {
				nodePids.put(tableName, new StringBuilder().append(nodePid));
			}

			RdSameNodePart sameNodePart = sameNodeSelector.loadByNodePidAndTableName(nodePid, tableName, true);

			if (sameNodePart != null) {
				throw new Exception("node点：" + nodePid + "已经存在同一关系，不能重复创建");
			}
		}
	}

}
