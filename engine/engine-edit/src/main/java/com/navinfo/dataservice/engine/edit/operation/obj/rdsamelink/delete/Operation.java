package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn = null;

	public Operation(Command command) {

		this.command = command;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = delete(result, command.getRdSameLink());

		return msg;
	}

	private String delete(Result result, RdSameLink sameLink) {

		result.insertObject(sameLink, ObjStatus.DELETE, sameLink.pid());

		return null;
	}

	public void deleteByLink(IRow deleteLink, Result result) throws Exception {

		String tableName = deleteLink.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(
				deleteLink.parentPKValue(), tableName, true);

		if (sameLinkPart == null) {
			return;
		}

		RdSameLink sameLink = (RdSameLink) sameLinkSelector.loadById(
				sameLinkPart.getGroupId(), true);

		if (sameLink.getParts().size() < 3) {

			delete(result, sameLink);

		} else {

			result.insertObject(sameLinkPart, ObjStatus.DELETE,
					sameLinkPart.getGroupId());

		}

	}

	public void deleteByUpDownPartLink(List<RdLink> deleteLinks, Result result)
			throws Exception {

		List<Integer> groupIds = new ArrayList<Integer>();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		for (RdLink deleteLink : deleteLinks) {

			String tableName = deleteLink.parentTableName().toUpperCase();

			RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(
					deleteLink.parentPKValue(), tableName, true);

			if (sameLinkPart == null) {
				return;
			}

			if (!groupIds.contains(sameLinkPart.getGroupId())) {

				groupIds.add(sameLinkPart.getGroupId());
			}

		}

		List<IRow> sameLinkRows = sameLinkSelector.loadByIds(groupIds, true,
				true);

		for (IRow row : sameLinkRows) {

			RdSameLink sameLink = (RdSameLink) row;

			delete(result, sameLink);
		}

	}
	
	/**
	 * 删除link对同一线的影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteLinkSameLinkInfectData(RdLink link,Connection conn) throws Exception {
		
		String tableName = link.parentTableName().toUpperCase();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		List<AlertObject> alertList = new ArrayList<>();
		
		RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(
				link.parentPKValue(), tableName, true);

		if(sameLinkPart != null)
		{
			RdSameLink sameLink = (RdSameLink) sameLinkSelector.loadById(
					sameLinkPart.getGroupId(), true);

			if (sameLink.getParts().size() < 3) {
				AlertObject alertObj = new AlertObject();

				alertObj.setObjType(sameLink.objType());

				alertObj.setPid(sameLink.getPid());

				alertObj.setStatus(ObjStatus.DELETE);

				if(!alertList.contains(alertObj))
				{
					alertList.add(alertObj);
				}

			} else {

				AlertObject alertObj = new AlertObject();

				alertObj.setObjType(sameLink.objType());

				alertObj.setPid(sameLink.getPid());

				alertObj.setStatus(ObjStatus.UPDATE);

				if(!alertList.contains(alertObj))
				{
					alertList.add(alertObj);
				}
			}
		}
		return alertList;
	}
}
