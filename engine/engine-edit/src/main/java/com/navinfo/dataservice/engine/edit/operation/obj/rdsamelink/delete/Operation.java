package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
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

}
