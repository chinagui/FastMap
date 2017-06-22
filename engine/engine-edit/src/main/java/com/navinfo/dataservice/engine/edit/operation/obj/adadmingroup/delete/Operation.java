package com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete;

import java.sql.Connection;
import java.util.List;



import com.google.gson.Gson;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminTree;


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

		Gson gson = new Gson();

		AdAdminTree tree = gson.fromJson(content.get("groupTree").toString(), AdAdminTree.class);

		handleAdAdminTree(tree, result);

		return null;
	}
	
	/**
	 * 循环遍历树中的节点状态，根据状态调用对应的处理方式
	 * @param tree
	 * @param result
	 * @throws Exception
	 */
	private void handleAdAdminTree(AdAdminTree tree, Result result) throws Exception {

		AdAdminGroup group = tree.getGroup();

		AdAdminPart part = tree.getPart();

		String groupType = null;

		String partType = null;

		int groupId = 0;

		if (group != null && group.pid() != 0) {
			groupId = group.pid();
		} else {
			groupId = PidUtil.getInstance().applyAdAdminGroupPid();
		}
		
		//在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改,删除中删除某一个group需要将其下的所有子层级的
		//group和part打上删除标识，前台保证，后台做检查？
		if (group != null && group.getObjType() != null) {
			groupType = group.getObjType().toUpperCase();

			if (ObjStatus.DELETE.toString().equals(groupType)) {
				result.insertObject(group, ObjStatus.DELETE, groupId);
			}
		}

		//在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改
		if (part != null && part.getObjType() != null) {
			partType = part.getObjType().toUpperCase();

			if (ObjStatus.DELETE.toString().equals(partType)) {
				result.insertObject(part, ObjStatus.DELETE, groupId);
			}
		}

		List<AdAdminTree> treeList = tree.getChildren();

		for (AdAdminTree ad : treeList) {
			handleAdAdminTree(ad, result);
		}
	}
}
