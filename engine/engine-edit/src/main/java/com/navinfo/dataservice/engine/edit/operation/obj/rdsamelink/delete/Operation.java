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
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
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

	/**
	 * a) 目标link串上的同一线关系全部删除
	 * b) 目标link串中间点上挂接的线的同一线关系全部删除
	 * @param sNodePid
	 * @param eNodePid
	 * @param targetLinks
	 * @param result
	 * @throws Exception
	 */
	public void deleteByUpDownPartLink(int sNodePid, int eNodePid,
			List<RdLink> targetLinks, Result result) throws Exception {
		
		//关联linkPid
		List<Integer> relationLinkPids = new ArrayList<Integer>();
	
		//目标link之间的挂接node
		List<Integer> nodePids= new ArrayList<Integer>();
		
		for (RdLink link : targetLinks) {
			
			if (!relationLinkPids.contains(link.getPid())) {
				
				relationLinkPids.add(link.getPid());
			}

			if (!nodePids.contains(link.getsNodePid())) {

				nodePids.add(link.getsNodePid());
			}

			if (!nodePids.contains(link.geteNodePid())) {

				nodePids.add(link.geteNodePid());
			}

			// 过滤端点
			if (nodePids.contains(sNodePid)) {
				nodePids.remove(sNodePid);
			}

			if (nodePids.contains(eNodePid)) {
				nodePids.remove(eNodePid);
			}
		}

		if (nodePids.size() > 0) {
			
			RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

			List<RdLink> links = linkSelector.loadByNodePids(nodePids,
					true);

			for (RdLink link : links) {

				if (!relationLinkPids.contains(link.getPid())) {
					
					relationLinkPids.add(link.getPid());
				}
			}
		}
		
		List<Integer> groupIds = new ArrayList<Integer>();

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		for (int linkPid : relationLinkPids) {
		
			RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(
					linkPid, "RD_LINK", true);

			if (sameLinkPart == null) {
				
				continue;
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

				alertList.add(alertObj);

			} else {

				AlertObject alertObj = new AlertObject();

				alertObj.setObjType(sameLink.objType());

				alertObj.setPid(sameLink.getPid());

				alertObj.setStatus(ObjStatus.UPDATE);

				alertList.add(alertObj);
			}
		}
		return alertList;
	}
}
