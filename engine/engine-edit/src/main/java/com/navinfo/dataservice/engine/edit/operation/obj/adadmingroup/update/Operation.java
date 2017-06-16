package com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update;

import com.google.gson.Gson;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminTree;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Connection;
import java.util.List;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		AdAdminTree tree = new Gson().fromJson(command.getContent().get("groupTree").toString(), AdAdminTree.class);
		this.handleAdAdminTree(tree, result, tree.getGroup().pid());
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
	private void handleAdAdminTree(AdAdminTree tree, Result result, int groupId) throws Exception {
		AdAdminGroup group = tree.getGroup();
		AdAdminPart part = tree.getPart();

		// 在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改
		if (group != null) {
            groupId = group.pid();

		    if (group.getObjType() != null) {
                String groupType = group.getObjType().toUpperCase();

                if (ObjStatus.INSERT.toString().equals(groupType)) {
                    groupId = PidUtil.getInstance().applyAdAdminGroupPid();
                    group.setPid(groupId);
                    group.setRegionIdUp(tree.getRegionId());
                    result.insertObject(group, ObjStatus.INSERT, groupId);
                } else if (ObjStatus.UPDATE.toString().equals(groupType)) {
                    group.changedFields().put("regionIdUp", tree.getRegionId());
                    result.insertObject(group, ObjStatus.UPDATE, groupId);
                } else if (ObjStatus.DELETE.toString().equals(groupType)) {
                    group.setRegionIdUp(tree.getRegionId());
                    result.insertObject(group, ObjStatus.DELETE, groupId);
                }
            }
		}

		// 在循环遍历过程中，给ObjType赋值的的树中的节点需要进行修改
		if (part != null && part.getObjType() != null) {
			String partType = part.getObjType().toUpperCase();

			if (ObjStatus.INSERT.toString().equals(partType)) {
			    part.setGroupId(groupId);
				part.setRegionIdDown(tree.getRegionId());
				result.insertObject(part, ObjStatus.INSERT, groupId);
			}

			if (ObjStatus.UPDATE.toString().equals(partType)) {
				part.changedFields().put("groupId", groupId);
				part.setGroupId(0);
				result.insertObject(part, ObjStatus.UPDATE, groupId);
			}

			if(ObjStatus.DELETE.toString().equals(partType)) {
				result.insertObject(part, ObjStatus.DELETE, groupId);
			}

		}
		//递归查询子节点，递归调用该方法直到子节点为空
		List<AdAdminTree> treeList = tree.getChildren();
		if(CollectionUtils.isNotEmpty(treeList)) {
			for (AdAdminTree ad : treeList) {
				handleAdAdminTree(ad, result, groupId);
			}
		} else {
			return;
		}
	}
}
