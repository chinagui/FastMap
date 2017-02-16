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
	
	/**
	 * 处理变更内容
	 * 
	 * @param limitContent
	 * @param direct
	 * @return
	 */
	private JSONObject handleLimitContent(JSONObject content) {
		
		int direct = this.command.getDirect();
		
		if (direct == 2) {

			content.put("speedLimit", content.getInt("fromSpeedLimit"));

			content.put("limitSrc", content.getInt("fromLimitSrc"));

		}

		else if (direct == 3) {

			content.put("speedLimit", content.getInt("toSpeedLimit"));

			content.put("limitSrc", content.getInt("toLimitSrc"));
		}

		return content;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.addAll(this.command.getLinkPids());

		JSONObject limitContent = this.command.getSpeedLimitContent();
		
		limitContent = handleLimitContent(limitContent);

		if (limitContent.getInt("speedType") == 3) {

			msg = batchForDependent(result, linkPids, limitContent);

		} else {

			msg = batch(result, linkPids, limitContent);
		}

		return msg;
	}

	/**
	 * 根据linkpid获取link的map<linkpid，link对象>
	 * 
	 * @param pids
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * 根据linkpid获取link的普通线限速map<linkpid，普通线限速对象>
	 * 
	 * @param pids
	 * @return
	 * @throws Exception
	 */
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
	 * 批量编辑普通线限速
	 * 
	 * @param result
	 * @param linkPids
	 * @param limitContent
	 * @param direct
	 * @return
	 * @throws Exception
	 */
	private String batch(Result result, List<Integer> linkPids,
			JSONObject limitContent) throws Exception {

		Map<Integer, RdLinkSpeedlimit> limits = getSpeedlimits(linkPids);

		Map<Integer, RdLink> rdLinks = getRdLinks(linkPids);

		for (int i = 0; i < linkPids.size(); i++) {

			int linkPid = linkPids.get(i);

			if (!rdLinks.containsKey(linkPid)) {

				throw new Exception("RdLink:" + String.valueOf(linkPid)
						+ "不存在,终止本次操作");
			}			
			
			RdLink link = rdLinks.get(linkPid);
			
			int direct =link.getDirect();
			
			if (i == 0) {

				direct = this.command.getDirect();

			} else if (link.getDirect() == 1 || link.getDirect() == 0) {

				int preLinkPid = linkPids.get(i - 1);
				
				int preSNode = rdLinks.get(preLinkPid).getsNodePid();

				int preENode = rdLinks.get(preLinkPid).geteNodePid();
				
				int currSNode =link.getsNodePid();

				if (currSNode == preSNode || currSNode == preENode) {

					direct = 2;

				} else {

					direct = 3;
				}
			}

			SetSpeedLimit(result, linkPid, limits, direct, limitContent);
		}

		return null;
	}

	/**
	 * 设置普通线限速
	 * 
	 * @param result
	 * @param linkPid
	 * @param limits
	 * @param direct
	 * @param limitContent
	 * @throws Exception
	 */
	private void SetSpeedLimit(Result result, int linkPid,
			Map<Integer, RdLinkSpeedlimit> limits, int direct,
			JSONObject limitContent) throws Exception {

		if (!limits.containsKey(linkPid)) {

			throw new Exception("link:" + linkPid + "无普通限速");
		}

		RdLinkSpeedlimit limit = limits.get(linkPid);

		if (direct == 2) {
			if (limit.getFromSpeedLimit() != limitContent.getInt("speedLimit")) {
				limit.changedFields().put("fromSpeedLimit",
						limitContent.getInt("speedLimit"));
			}
			if (limit.getFromLimitSrc() != limitContent.getInt("limitSrc")) {
				limit.changedFields().put("fromLimitSrc",
						limitContent.getInt("limitSrc"));
			}

		} else if (direct == 3) {
			if (limit.getToSpeedLimit() != limitContent.getInt("speedLimit")) {
				limit.changedFields().put("toSpeedLimit",
						limitContent.getInt("speedLimit"));
			}
			if (limit.getToLimitSrc() != limitContent.getInt("limitSrc")) {
				limit.changedFields().put("toLimitSrc",
						limitContent.getInt("limitSrc"));
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

	/**
	 * 处理speedClass
	 * 
	 * @param limit
	 * @return
	 */
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

	/**
	 * 批量编辑条件线限速
	 * 
	 * @param result
	 * @param linkPids
	 * @param limitContent
	 * @param direct
	 * @return
	 * @throws Exception
	 */
	private String batchForDependent(Result result, List<Integer> linkPids,
			JSONObject limitContent) throws Exception {

		Map<Integer, List<RdLinkSpeedlimit>> limits = getSpeedlimitsForDependent(linkPids);

		Map<Integer, RdLink> rdLinks = getRdLinks(linkPids);

		for (int i = 0; i < linkPids.size(); i++) {

			int linkPid = linkPids.get(i);

			if (!rdLinks.containsKey(linkPid)) {

				throw new Exception("RdLink:" + String.valueOf(linkPid)
						+ "不存在,终止本次操作");
			}
			
			RdLink link = rdLinks.get(linkPid);

			int direct = link.getDirect();

			if (i == 0) {

				direct = this.command.getDirect();

			} else if (link.getDirect() == 1 || link.getDirect() == 0) {

				int preSNode = rdLinks.get(linkPids.get(i - 1)).getsNodePid();

				int preENode = rdLinks.get(linkPids.get(i - 1)).geteNodePid();

				int currSNode = link.getsNodePid();

				if (currSNode == preSNode || currSNode == preENode) {

					direct = 2;

				} else {

					direct = 3;
				}
			}
			
			SetSpeedLimitForDependent(result, linkPid,
					limits.get(linkPid), direct, limitContent);
		}

		return null;
	}

	/**
	 * 根据linkpid获取条件线限速 map<linkpid，条件线限速列表>
	 * 
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, List<RdLinkSpeedlimit>> getSpeedlimitsForDependent(
			List<Integer> pids) throws Exception {

		Map<Integer, List<RdLinkSpeedlimit>> limits = new HashMap<Integer, List<RdLinkSpeedlimit>>();

		List<IRow> rows = new AbstractSelector(RdLinkSpeedlimit.class, conn)
				.loadRowsByParentIds(pids, true);

		for (IRow row : rows) {

			RdLinkSpeedlimit limit = (RdLinkSpeedlimit) row;

			if (limit.getSpeedType() == 3) {
				if (!limits.containsKey(limit.getLinkPid())) {
					List<RdLinkSpeedlimit> speedlimits = new ArrayList<RdLinkSpeedlimit>();
					limits.put(limit.getLinkPid(), speedlimits);
				}

				limits.get(limit.getLinkPid()).add(limit);
			}
		}

		return limits;
	}

	/**
	 * 处理条件线限速
	 * 
	 * @param result
	 * @param linkPid
	 * @param limits
	 * @param direct
	 * @param limitContent
	 * @throws Exception
	 */
	private void SetSpeedLimitForDependent(Result result, int linkPid,
			List<RdLinkSpeedlimit> limits, int direct, JSONObject limitContent)
			throws Exception {
		
		if (limits == null) {
			insertDependentSpeedlimit(result, linkPid, direct, limitContent);
			return;
		}
		
		boolean hadLimit = false;

		// 更新
		for (RdLinkSpeedlimit speedlimit : limits) {

			if (speedlimit.getSpeedDependent() != limitContent
					.getInt("speedDependent")) {
				continue;
			}

			hadLimit = true;

			updataDependentSpeedlimit(result, limitContent, direct, speedlimit);
		}

		// 新增
		if (!hadLimit) {

			insertDependentSpeedlimit(result, linkPid, direct, limitContent);
		}
	}
	
	/**
	 * 更新条件限速
	 * @param result
	 * @param limitContent
	 * @param direct
	 * @param speedlimit
	 */
	private void updataDependentSpeedlimit(Result result,
			JSONObject limitContent, int direct, RdLinkSpeedlimit speedlimit) {

		int limitSrc = limitContent.getInt("limitSrc");

		int limitValue = limitContent.getInt("speedLimit");

		if (direct == 2) {

			if (speedlimit.getFromSpeedLimit() != limitValue) {

				speedlimit.changedFields().put("fromSpeedLimit", limitValue);

				limitSrc = 1;

				if (limitValue == 0) {

					limitSrc = 0;
				}
			}

			if (speedlimit.getFromLimitSrc() != limitSrc) {

				speedlimit.changedFields().put("fromLimitSrc", limitSrc);
			}

		} else if (direct == 3) {

			if (speedlimit.getToSpeedLimit() != limitValue) {

				speedlimit.changedFields().put("toSpeedLimit", limitValue);

				limitSrc = 1;

				if (limitValue == 0) {

					limitSrc = 0;
				}
			}

			if (speedlimit.getToLimitSrc() != limitSrc) {

				speedlimit.changedFields().put("toLimitSrc", limitSrc);
			}
		}

		if (limitContent.containsKey("timeDomain")) {

			String timeDomain = limitContent.getString("timeDomain");

			if (speedlimit.getTimeDomain()==null||!speedlimit.getTimeDomain().equals(timeDomain)) {

				speedlimit.changedFields().put("timeDomain", timeDomain);
			}
		}

		result.insertObject(speedlimit, ObjStatus.UPDATE,
				speedlimit.getLinkPid());
	}

	/**
	 * 新增条件限速
	 * @param result
	 * @param linkPid
	 * @param direct
	 * @param limitContent
	 */
	private void insertDependentSpeedlimit(Result result, int linkPid,
			int direct, JSONObject limitContent) {
		
		RdLinkSpeedlimit newLimit = new RdLinkSpeedlimit();

		newLimit.setLinkPid(linkPid);

		newLimit.setSpeedType(3);

		newLimit.setSpeedDependent(limitContent.getInt("speedDependent"));

		int limitValue = limitContent.getInt("speedLimit");

		int limitSrc = limitContent.getInt("limitSrc");

		if (direct == 2) {

			newLimit.setFromSpeedLimit(limitValue);

			newLimit.setFromLimitSrc(limitSrc);

		} else if (direct == 3) {

			newLimit.setToSpeedLimit(limitValue);

			newLimit.setToLimitSrc(limitSrc);
		}
		if (limitContent.containsKey("timeDomain")) {

			newLimit.setTimeDomain(limitContent.getString("timeDomain"));
		}

		result.insertObject(newLimit, ObjStatus.INSERT, linkPid);
	}

	
}
