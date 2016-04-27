package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.create;

import java.sql.Connection;
import java.util.List;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminTree;

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

		JSONObject content = command.getContent();

		JSONArray array = new JSONArray(content.get("groupTree"));

		Gson gson = new Gson();

		AdAdminTree tree = gson.fromJson(array.getJSONObject(0).toString(), AdAdminTree.class);

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				return null;
			} else {
				handleAdAdminTree(tree, result);
			}
		}

		return null;
	}

	private void handleAdAdminTree(AdAdminTree tree, Result result) throws Exception {

		AdAdminGroup group = tree.getGroup();

		AdAdminPart part = tree.getPart();

		String groupType = null;

		String partType = null;

		int groupId = 0;

		if (group != null && group.getPid() != 0) {
			groupId = group.getPid();
		} else {
			groupId = PidService.getInstance().applyAdAdminGroupPid();
		}

		if (group != null && group.getObjType() != null) {
			groupType = group.getObjType().toUpperCase();

			if (ObjStatus.INSERT.toString().equals(groupType)) {
				result.insertObject(group, ObjStatus.INSERT,
						groupId);
			}
			
			if (ObjStatus.UPDATE.toString().equals(groupType)) {
				result.insertObject(group, ObjStatus.UPDATE,
						groupId);
			}
		}

		if (part != null && part.getObjType() != null) {
			partType = part.getObjType().toUpperCase();

			if (ObjStatus.INSERT.toString().equals(partType)) {
				result.insertObject(part, ObjStatus.INSERT,
						groupId);
			}

			if (ObjStatus.UPDATE.toString().equals(partType)) {
				result.insertObject(part, ObjStatus.UPDATE,
						groupId);
			}
		}

		List<AdAdminTree> treeList = tree.getChildren();

		for (AdAdminTree ad : treeList) {
			handleAdAdminTree(ad, result);
		}
	}
}
