package com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create;

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

		// 解析前台传递的树型json为AdAdminTree对象
		AdAdminTree tree = gson.fromJson(content.get("groupTree").toString(), AdAdminTree.class);

		handleAdAdminTree(tree, result);

		return null;
	}

	/**
	 * 循环遍历树中的节点状态，根据状态调用对应的处理方式
	 * 前台逻辑：如果父节点没有group对象，需要给父节点创建group对象并给groupid赋值0还要打上新增标识;
	 * 新增的叶节点的regionid为地图上选择的代表点的regionid，name为选择代表点的name（无名称时赋值无），
	 * group对象为null，part对象的groupid为父节点的group的groupid，rowId为空，添加新增标识；
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

		// 当在没有子节点的节点添加层级的时候，需要新增ad_admin_group,pid前台传递的是默认值0，后台需要申请重新赋值
		if (group != null && group.pid() != 0) {
			groupId = group.pid();
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
		}

		// 在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改
		if (part != null && part.getObjType() != null) {
			partType = part.getObjType().toUpperCase();

			if (ObjStatus.INSERT.toString().equals(partType)) {
				part.setRegionIdDown(tree.getRegionId());
				result.insertObject(part, ObjStatus.INSERT, groupId);
			}

			if (ObjStatus.UPDATE.toString().equals(partType)) {
				part.setRegionIdDown(tree.getRegionId());
				result.insertObject(part, ObjStatus.UPDATE, groupId);
			}
		}

		List<AdAdminTree> treeList = tree.getChildren();

		for (AdAdminTree ad : treeList) {
			handleAdAdminTree(ad, result);
		}
	}
}
