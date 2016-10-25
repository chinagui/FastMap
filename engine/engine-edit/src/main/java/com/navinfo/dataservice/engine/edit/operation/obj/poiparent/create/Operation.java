package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;

/**
 * 
 * @Title: Operation.java
 * @Description: 新增poi父子关系操作类
 * @author 鹿尧
 * @date 2016年6月17日
 * @version V1.0
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	List<IRow> parents;

	public Operation(Command command, List<IRow> parents, Connection conn) {
		this.command = command;

		this.conn = conn;

		this.parents = parents;

	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		if (parents != null && parents.size() > 1) {
			throw new Exception("该poi有多个父，操作终止");
		}

		IxPoiParent parent = null;

		IxPoiChildren children = new IxPoiChildren();

		children.setChildPoiPid(this.command.getObjId());

		// /被选父poi 已经建立父子关系
		if (parents != null && parents.size() == 1) {
			parent = (IxPoiParent) parents.get(0);

			children.setGroupId(parent.getPid());

			result.insertObject(children, ObjStatus.INSERT, children.getChildPoiPid());

		}
		// /被选父poi 无父子关系
		if (parents == null || parents.size() == 0) {
			parent = new IxPoiParent();

			parent.setPid(PidUtil.getInstance().applyPoiGroupId());

			parent.setParentPoiPid(this.command.getParentPid());

			children.setGroupId(parent.getPid());

			parent.getPoiChildrens().add(children);

			result.insertObject(parent, ObjStatus.INSERT, parent.getParentPoiPid());
		}

		result.setPrimaryPid(this.command.getObjId());

		return msg;
	}

}
