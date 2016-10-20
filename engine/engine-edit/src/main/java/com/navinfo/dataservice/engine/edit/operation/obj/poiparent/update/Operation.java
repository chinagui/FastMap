package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

public class Operation implements IOperation {

	private Command command;

	/**
	 * 被选父poi，已经作为poiParent的父子关系列表
	 */
	List<IRow> parentsByParent;
	/**
	 * 被选子poi，已经作为PoiChildren的父子关系列表
	 */
	List<IRow> parentsByChildren;

	private Connection conn;
	
	public Operation(Command command, List<IRow> parentsByParent,
			List<IRow> parentsByChildren,Connection conn) {
		this.command = command;

		this.parentsByParent = parentsByParent;

		this.parentsByChildren = parentsByChildren;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		removeParent(result);

		createParent(result);

		return msg;
	}

	private void removeParent(Result result) throws Exception {

		if (parentsByChildren == null || parentsByChildren.size() == 0) {
			return;
		}

		for (IRow rowParent : parentsByChildren) {
			IxPoiParent parent = (IxPoiParent) rowParent;

			List<IRow> poiChildrens = parent.getPoiChildrens();

			if (poiChildrens.size() == 1) {
				result.insertObject(parent, ObjStatus.DELETE,
						command.getObjId());

				continue;
			}

			for (IRow rowChildren : poiChildrens) {
				IxPoiChildren children = (IxPoiChildren) rowChildren;

				if (children.getChildPoiPid() == command.getObjId()) {
					result.insertObject(children, ObjStatus.DELETE,
							command.getObjId());

					break;
				}
			}
		}
	}

	public String createParent(Result result) throws Exception {

		String msg = null;

		if (parentsByParent != null && parentsByParent.size() > 1) {
			throw new Exception("该poi有多个父，操作终止");
		}

		IxPoiParent parent = null;

		IxPoiChildren children = new IxPoiChildren();

		children.setChildPoiPid(this.command.getObjId());

		// /被选父poi 已经建立父子关系
		if (parentsByParent != null && parentsByParent.size() == 1) {
			parent = (IxPoiParent) parentsByParent.get(0);

			children.setGroupId(parent.getPid());

			result.insertObject(children, ObjStatus.INSERT,
					children.getChildPoiPid());

		}
		// /被选父poi 无父子关系
		if (parentsByParent == null || parentsByParent.size() == 0) {
			parent = new IxPoiParent();

			parent.setPid(PidUtil.getInstance().applyPoiGroupId());

			parent.setParentPoiPid(this.command.getParentPid());

			children.setGroupId(parent.getPid());

			parent.getPoiChildrens().add(children);

			result.insertObject(parent, ObjStatus.INSERT,
					parent.getParentPoiPid());
		}

		result.setPrimaryPid(this.command.getObjId());
		
		if (CollectionUtils.isNotEmpty(result.getAddObjects()) || CollectionUtils.isNotEmpty(result.getUpdateObjects())
				|| CollectionUtils.isNotEmpty(result.getDelObjects())) {
			// 修改poi主表时间
			IxPoiSelector selector = new IxPoiSelector(conn);
			
			IxPoi ixPoi = (IxPoi) selector.loadById(this.command.getObjId(), true,true);
			
			ixPoi.changedFields().put("uDate", StringUtils.getCurrentTime());

			result.insertObject(ixPoi, ObjStatus.UPDATE, ixPoi.pid());
		}
		
		return msg;
	}

}
