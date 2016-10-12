package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;

public class Operation implements IOperation {

	private Command command;

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

		msg = delete(result, command.getDirectroute());

		return msg;
	}

	public String delete(Result result, RdDirectroute directroute) {

		result.insertObject(directroute, ObjStatus.DELETE, directroute.pid());

		return null;
	}

	public void deleteByLink(RdLink oldLink, Result result) throws Exception {

		if (conn == null) {
			return;
		}

		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		List<RdDirectroute> directroutes = selector.loadByInOutLink(oldLink.getPid(), true);
		// 删除link做进入线或退出线的顺行
		for (RdDirectroute directroute : directroutes) {

			delete(result, directroute);
		}

		directroutes = selector.loadByPassLink(oldLink.getPid(), true);

		for (RdDirectroute directroute : directroutes) {

			DeletePassLink(directroute, oldLink, result);
		}
	}

	/**
	 * 根据路口pid删除路口关系的顺行
	 * 
	 * @param crossPid
	 * @param result
	 * @throws Exception
	 */
	public void deleteByCross(int crossPid, Result result) throws Exception {
		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		List<RdDirectroute> directroutes = selector.getRestrictionByCrossPid(crossPid, true);

		for (RdDirectroute directroute : directroutes) {
			delete(result, directroute);
		}
	}

	private void DeletePassLink(RdDirectroute directroute, RdLink oldLink, Result result) throws Exception {

		if (directroute.getVias().size() == 0) {

			return;
		}

		// 需要删除的经过线组
		Set<Integer> deleteGroupId = new HashSet<Integer>();

		// 所有经过线组
		Set<Integer> sumGroupId = new HashSet<Integer>();

		for (IRow row : directroute.getVias()) {

			RdDirectrouteVia via = (RdDirectrouteVia) row;

			if (via.getLinkPid() == oldLink.pid()) {
				deleteGroupId.add(via.getGroupId());
			}

			sumGroupId.add(via.getGroupId());
		}

		// 需要删除的经过线组数与总经过线组数一致，删除该顺行
		if (sumGroupId.size() == deleteGroupId.size()) {

			delete(result, directroute);

		} else {
			// 按组删除经过线
			for (IRow rowVia : directroute.getVias()) {

				RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

				if (deleteGroupId.contains(via.getGroupId())) {

					result.insertObject(via, ObjStatus.DELETE, directroute.pid());
				}
			}
		}
	}

	/**
	 * 删除link对顺行的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdDirectrouteInfectData(int linkPid, Connection conn) throws Exception {

		RdDirectrouteSelector directrouteSelector = new RdDirectrouteSelector(conn);

		List<RdDirectroute> directroutes = directrouteSelector.loadByInOutLink(linkPid, true);

		directroutes.addAll(directrouteSelector.loadByPassLink(linkPid, true));

		List<AlertObject> alertList = new ArrayList<>();

		for (RdDirectroute directroute : directroutes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(directroute.objType());

			alertObj.setPid(directroute.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除路口对顺行的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossDirectoureInfectData(int crossPid) throws Exception {

		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		List<RdDirectroute> directroutes = selector.getRestrictionByCrossPid(crossPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdDirectroute directroute : directroutes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(directroute.objType());

			alertObj.setPid(directroute.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
