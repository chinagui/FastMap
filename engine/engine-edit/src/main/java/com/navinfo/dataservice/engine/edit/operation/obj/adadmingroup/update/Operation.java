package com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update;

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
	 * 前台操作：判断目标节点（父节点）的group对象是否为null，为null时则给目标节点group的groupid赋值0，并打上新增标识；
	 * 判断目标节点（父节点）的group对象是否为null，为null时则给目标节点group的groupid赋值0，并打上新增标识；
	 * 更新拖拽节点的part对象的groupid为目标节点的group的groupid，如果拖拽节点的part对象没有状态，则把状态打上修改标识；
	 * 
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

		if (group != null && group.getGroupId() != 0) {
			groupId = group.getGroupId();
		} else {
			groupId = PidUtil.getInstance().applyAdAdminGroupPid();
		}

		// 在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改
		if (group != null && group.getObjType() != null) {
			groupType = group.getObjType().toUpperCase();

			if (ObjStatus.INSERT.toString().equals(groupType)) {
				group.setRegionIdUp(tree.getRegionId());
				result.insertObject(group, ObjStatus.INSERT, groupId);
			}

			if (ObjStatus.UPDATE.toString().equals(groupType)) {
				group.setRegionIdUp(tree.getRegionId());
				result.insertObject(group, ObjStatus.UPDATE, groupId);
			}
			if(ObjStatus.DELETE.toString().equals(groupType))
			{
				group.setRegionIdUp(tree.getRegionId());
				result.insertObject(group, ObjStatus.DELETE, groupId);
			}
		}

		// 在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改
		if (part != null && part.getObjType() != null) {
			partType = part.getObjType().toUpperCase();

			if (ObjStatus.INSERT.toString().equals(partType)) {
				part.setRegionIdDown(tree.getRegionId());
				result.insertObject(part, ObjStatus.INSERT, groupId);
			}

			if (ObjStatus.UPDATE.toString().equals(partType)) {
				result.insertObject(part, ObjStatus.UPDATE, groupId);
			}
			if(ObjStatus.DELETE.toString().equals(groupType))
			{
				result.insertObject(part, ObjStatus.DELETE, groupId);
			}
		}

		List<AdAdminTree> treeList = tree.getChildren();

		for (AdAdminTree ad : treeList) {
			handleAdAdminTree(ad, result);
		}
	}
}
