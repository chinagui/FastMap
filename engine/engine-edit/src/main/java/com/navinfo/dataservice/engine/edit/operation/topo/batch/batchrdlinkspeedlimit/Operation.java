package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlinkspeedlimit;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.addAll(this.command.getLinkPids());

		JSONObject limitContent = this.command.getSpeedLimitContent();

		int direct = this.command.getDirect();

		msg = batch(result, linkPids, limitContent, direct);

		return msg;
	}

	private Map<Integer, RdLink> getRdLinks(List<Integer> pids)
			throws Exception {

		Map<Integer, RdLink> rdLinks = new HashMap<Integer, RdLink>();

		List<IRow> rows = new AbstractSelector(RdLink.class, conn).loadByIds(
				pids, true, false);

		for (IRow row : rows) {

			RdLink link = (RdLink) row;

			rdLinks.put(link.getPid(), link);
		}

		return rdLinks;
	}

	private Map<Integer, RdLinkSpeedlimit> getSpeedlimits(List<Integer> pids)
			throws Exception {

		Map<Integer, RdLinkSpeedlimit> limits = new HashMap<Integer, RdLinkSpeedlimit>();

		List<IRow> rows = new AbstractSelector(RdLinkSpeedlimit.class, conn)
				.loadRowsByParentIds(pids, true);

		for (IRow row : rows) {

			RdLinkSpeedlimit limit = (RdLinkSpeedlimit) row;

			if (limit.getSpeedType() == 0) {

				limits.put(limit.getLinkPid(), limit);
			}
		}

		return limits;
	}

	/**
	 * 
	 * @param limitContent
	 * @param direct
	 * @return limitInfo[3] 0：限速值；1：限速来源；2：限速等级标记
	 */
	private int[] getLimitInfo(JSONObject limitContent, int direct) {

		int[] limitInfo = new int[2];

		if (direct == 2) {

			limitInfo[0] = limitContent.getInt("fromSpeedLimit");

			limitInfo[1] = limitContent.getInt("fromLimitSrc");
		}

		else if (direct == 3) {

			limitInfo[0] = limitContent.getInt("toSpeedLimit");

			limitInfo[1] = limitContent.getInt("toLimitSrc");
		}

		return limitInfo;
	}

	private String batch(Result result, List<Integer> linkPids,
			JSONObject limitContent, int direct) throws Exception {

		// limitInfo[4] 0：限速值；1：限速来源；
		int[] limitInfo = getLimitInfo(limitContent, direct);

		Map<Integer, RdLinkSpeedlimit> limits = getSpeedlimits(linkPids);

		Map<Integer, RdLink> rdLinks = getRdLinks(linkPids);

		for (int i = 0; i < linkPids.size(); i++) {

			int linkPid = linkPids.get(i);

			if (i == 0) {
				if (rdLinks.get(linkPid).getDirect() != 1
						&& rdLinks.get(linkPid).getDirect() != direct) {

					throw new Exception("");
				}

				SetSpeedLimit(result, linkPid, limits, direct, limitInfo);

				continue;
			}

			if (!rdLinks.containsKey(linkPids.get(i))) {

				throw new Exception("RdLink:" + String.valueOf(linkPid)
						+ "不存在,终止本次操作");
			}

			RdLink link = rdLinks.get(linkPid);

			if (link.getDirect() == 1) {

				int preLinkPid = linkPids.get(i - 1);

				if (link.getsNodePid() == rdLinks.get(preLinkPid).getsNodePid()
						|| link.getsNodePid() == rdLinks.get(preLinkPid)
								.geteNodePid()) {

					SetSpeedLimit(result, linkPid, limits, 2, limitInfo);
				} else {
					SetSpeedLimit(result, linkPid, limits, 3, limitInfo);
				}
			}

			if (link.getDirect() == 2 || link.getDirect() == 3) {

				SetSpeedLimit(result, linkPid, limits, link.getDirect(),
						limitInfo);
			}
		}

		return null;
	}

	private void SetSpeedLimit(Result result, int linkPid,
			Map<Integer, RdLinkSpeedlimit> limits, int direct, int[] info) throws Exception{
	
		if (!limits.containsKey(linkPid)) {

			throw new Exception("link:" + linkPid + "无普通限速");
		}

		RdLinkSpeedlimit limit = limits.get(linkPid);

		if (direct == 2) {
			if (limit.getFromSpeedLimit() != info[0]) {
				limit.changedFields().put("fromSpeedLimit", info[0]);
			}
			if (limit.getFromLimitSrc() != info[1]) {
				limit.changedFields().put("fromLimitSrc", info[1]);
			}

		} else if (direct == 3) {
			if (limit.getToSpeedLimit() != info[0]) {
				limit.changedFields().put("toSpeedLimit", info[0]);
			}
			if (limit.getToLimitSrc() != info[1]) {
				limit.changedFields().put("toLimitSrc", info[1]);
			}
		}

		handleSpeedClassWork(limit);

		int speedClass = getspeedClass(limit);

		if (limit.getSpeedClass() != speedClass) {
			limit.changedFields().put("speedClass", speedClass);
		}
		
		result.insertObject(limit, ObjStatus.UPDATE, linkPid);		
	}

	/**
	 * 限速值改变维护普通限速
	 * 
	 * @param limit
	 */
	private void handleSpeedClassWork(RdLinkSpeedlimit limit) {

		int fromValue = limit.getFromSpeedLimit();

		int toValue = limit.getToSpeedLimit();

		boolean fromChange = limit.changedFields()
				.containsKey("fromSpeedLimit");

		int fromChangeValue = 0;

		if (fromChange) {
			fromChangeValue = (int) limit.changedFields().get("fromSpeedLimit");
		}

		boolean toChange = limit.changedFields().containsKey("toSpeedLimit");

		int toChangeValue = 0;

		if (toChange) {
			toChangeValue = (int) limit.changedFields().get("toSpeedLimit");
		}

		if (limit.getSpeedClassWork() == 1) {

			// 如果link的两侧限速值均为“0”，并且等级赋值标识为“程序赋值”，将link的一侧限速值修改为非0后，该link的等级赋值标识维护为“手工赋值”；
			// 如果link两侧的限速值均为非0，并且等级赋值标识为“”程序赋值”，将非0修改为非0，则等级赋值标识赋值为“手工赋值”；
			if ((fromValue == 0 && toValue == 0)
					|| (fromValue != 0 && limit.getToSpeedLimit() != 0)) {

				if (fromChange && fromChangeValue != 0) {

					limit.changedFields().put("speedClassWork", 0);

				} else if (toChange && toChangeValue != 0) {

					limit.changedFields().put("speedClassWork", 0);
				}
			}
			// 如果link的一侧限速值为“0”，一侧为非0，并且等级赋值标识为“程序赋值”，如果将0改为非0时，则等级赋值标识维护为“手工赋值
			if ((fromValue != 0 && toValue == 0 && toChange && toChangeValue != 0)
					|| (toValue != 0 && fromValue == 0 && fromChange && fromChangeValue != 0)) {

				limit.changedFields().put("speedClassWork", 0);
			}
		}

		if (limit.getSpeedClassWork() == 0) {

			// 如果link一侧的限速值为0，另一侧限速值为非0，并且等级赋值标识为“手工赋值”；如果将非0修改为0，则等级赋值标识维护为“程序赋值”；
			if ((fromValue != 0 && toValue == 0 && fromChange && fromChangeValue == 0)
					|| (toValue != 0 && fromValue == 0 && toChange && toChangeValue == 0)) {

				limit.changedFields().put("speedClassWork", 1);
			}
		}
	}

	private int getspeedClass(RdLinkSpeedlimit limit) {
		int toSpeedValue = limit.getToSpeedLimit();
		int fromSpeedValue = limit.getFromSpeedLimit();

		if (limit.changedFields().containsKey("toSpeedLimit")) {
			toSpeedValue = (Integer) limit.changedFields().get("toSpeedLimit");
		}
		if (limit.changedFields().containsKey("fromSpeedLimit")) {
			fromSpeedValue = (Integer) limit.changedFields().get(
					"fromSpeedLimit");
		}

		int speedValue = 0;

		if (toSpeedValue == 0 && fromSpeedValue == 0) {

			return speedValue;
		}

		if (toSpeedValue < fromSpeedValue) {

			speedValue = toSpeedValue;

		} else {

			speedValue = fromSpeedValue;
		}

		if (toSpeedValue == 0) {

			speedValue = fromSpeedValue;
		}
		if (fromSpeedValue == 0) {

			speedValue = toSpeedValue;
		}

		if (1300 < speedValue) {
			return 1;
		}
		if (1001 <= speedValue && speedValue <= 1300) {
			return 2;
		}
		if (901 <= speedValue && speedValue <= 1000) {
			return 3;
		}
		if (701 <= speedValue && speedValue <= 900) {
			return 4;
		}
		if (501 <= speedValue && speedValue <= 700) {
			return 5;
		}
		if (301 <= speedValue && speedValue <= 500) {
			return 6;
		}
		if (110 <= speedValue && speedValue <= 300) {
			return 7;
		}
		if (speedValue < 110) {
			return 8;
		}

		return 0;
	}

}
