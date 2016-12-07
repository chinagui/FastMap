package com.navinfo.dataservice.engine.edit.operation.obj.tmc.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		update(result);

		return null;
	}

	/**
	 * 更新
	 * 
	 * @param result
	 * @return
	 */
	private String update(Result result) throws Exception {

		int tmcLocationPid = this.command.getPid();

		RdTmclocation tmclocation = this.command.getRdTmclocation();

		result.setPrimaryPid(tmcLocationPid);

		JSONObject content = command.getUpdateContent();

		// 拓补操作更新主表属性信息
		updateTmcLinkAttribute(content, tmclocation, result);

		// 属性面板修改子表属性信息数据
		if (content.containsKey("links")) {
			JSONArray links = content.getJSONArray("links");

			this.updateLinks(result, tmclocation, links);
		}

		// 拓补操作更新子表信息
		Map<Integer, RdTmclocationLink> tmcLocationLinkMap = new HashMap<>();

		// 判断是否修改tmc匹配信息的方向：包含direct参数,并且不为-1;
		for (IRow row : tmclocation.getLinks()) {
			RdTmclocationLink tmcLink = (RdTmclocationLink) row;

			tmcLocationLinkMap.put(tmcLink.getLinkPid(), tmcLink);
		}

		// locDirect 取原始默认值，如果参数包含这个，代表修改这个方向
		int locDirect = ((RdTmclocationLink) tmclocation.getLinks().get(0)).getLocDirect();

		if (content.containsKey("locDirect")) {
			locDirect = content.getInt("locDirect");
		}

		JSONObject obj = content.getJSONObject("link");

		int linkPid = obj.getInt("linkPid");

		int direct = obj.getInt("direct");

		// 子表拓补修改
		JSONArray links = content.getJSONArray("linkPids");

		RdLinkSelector selector = new RdLinkSelector(conn);

		@SuppressWarnings("unchecked")
		List<IRow> linkList = selector.loadByIds((List<Integer>) JSONArray.toCollection(links, Integer.class), true,
				false);

		int inNodePid = 0;

		int outNodePid = 0;

		List<Integer> hasHandledLinkPids = new ArrayList<>();

		for (IRow row : linkList) {
			RdLink link = (RdLink) row;

			if (link.getPid() == linkPid) {
				// 1代表和link的划线方向相同，则下一条link判断方向关系以该link的eNode为判断依据
				if (direct == 1) {
					inNodePid = link.geteNodePid();

					outNodePid = link.getsNodePid();
				} else if (direct == 2) {
					// 2代表和link的划线方向相反，则下一条link判断方向关系以该link的sNode为判断依据
					inNodePid = link.getsNodePid();

					outNodePid = link.geteNodePid();
				}
				updateLocationLink(result,link.getPid(), direct, locDirect, tmcLocationLinkMap);
				hasHandledLinkPids.add(link.getPid());
				break;
			}
		}
		//向baseLink计算link方向
		for (IRow row : linkList) {
			RdLink link = (RdLink) row;
			if(!hasHandledLinkPids.contains(link.getPid()))
			{
				if (link.getsNodePid() == inNodePid) {
					// 如果作用方向和该link的起点到终点的划线方向一致，则赋值为'1'
					direct = 1;
					// 该link的终点作为下一个link的进入点
					inNodePid = link.geteNodePid();
				} else if (link.geteNodePid() == inNodePid) {
					// 如果作用方向和该link的起点到终点的划线方向相反，则赋值为'2'
					direct = 2;
					// 该link的起点作为下一个link的进入点
					inNodePid = link.getsNodePid();
				}
				updateLocationLink(result,link.getPid(), direct, locDirect, tmcLocationLinkMap);
				hasHandledLinkPids.add(link.getPid());
			}
		}
		//向baseLink往前计算link方向
		for (IRow row : linkList) {
			RdLink link = (RdLink) row;
			if(!hasHandledLinkPids.contains(link.getPid()))
			{
				if (link.geteNodePid() == outNodePid) {
					// 如果作用方向和该link的起点到终点的划线方向一致，则赋值为'1'
					direct = 1;
					// 该link的终点作为下一个link的进入点
					outNodePid = link.getsNodePid(); 
				} else if (link.getsNodePid() == outNodePid) {
					// 如果作用方向和该link的起点到终点的划线方向相反，则赋值为'2'
					direct = 2;
					// 该link的起点作为下一个link的进入点
					outNodePid = link.geteNodePid();
				}
				updateLocationLink(result,link.getPid(), direct, locDirect, tmcLocationLinkMap);
				hasHandledLinkPids.add(link.getPid());
			}
		}

		// map中的代表需要删除的
		for (RdTmclocationLink rdTmclocationLink : tmcLocationLinkMap.values()) {
			result.insertObject(rdTmclocationLink, ObjStatus.DELETE, rdTmclocationLink.getGroupId());
		}

		return null;
	}

	/**
	 * @param result
	 * @param tmclocation
	 * @param links
	 * @param tmcLocationLinkMap
	 * @param direct
	 * @throws Exception
	 */
	private void updateLocationLink(Result result, int linkPid, int direct, int locDirect,
			Map<Integer, RdTmclocationLink> tmcLocationLinkMap) throws Exception {

		RdTmclocationLink tmcLocationLink = null;

		if (tmcLocationLinkMap.containsKey(linkPid)) {
			tmcLocationLink = tmcLocationLinkMap.get(linkPid);
			JSONObject obj = new JSONObject();
			// 如果direct不是-1代表修改方向
			if (direct != -1) {
				obj.put("direct", direct);
			}
			if (locDirect != -1) {
				obj.put("locDirect", locDirect);
			}
			tmcLocationLink.fillChangeFields(obj);
			
			//删除需要更新的
			tmcLocationLinkMap.remove(linkPid);
		} else {
			tmcLocationLink = new RdTmclocationLink();

			tmcLocationLink.setLinkPid(linkPid);

			tmcLocationLink.setLocDirect(locDirect);

			tmcLocationLink.setDirect(direct);

			tmcLocationLink.setGroupId(command.getPid());
		}
		
		if(tmcLocationLink != null)
		{
			//rowId不为空代表是修改的对象是修改操作
			if(tmcLocationLink.changedFields().size() > 0 && tmcLocationLink.getRowId() != null)
			{
				result.insertObject(tmcLocationLink, ObjStatus.UPDATE, tmcLocationLink.getGroupId());
			}
			if(tmcLocationLink.getRowId() == null)
			{
				result.insertObject(tmcLocationLink, ObjStatus.INSERT, tmcLocationLink.getGroupId());
			}
			
		}
	}

	private void updateTmcLinkAttribute(JSONObject content, RdTmclocation tmclocation, Result result) {
		if (content.containsKey("objStatus")) {
			// 修改tmcId
			if (content.containsKey("tmcId")) {
				int tmcId = content.getInt("tmcId");

				if (tmclocation.getTmcId() != tmcId) {
					tmclocation.changedFields().put("tmcId", tmcId);
				}
			}
			// 修改loctable_id
			if (content.containsKey("loctableId")) {
				int loctableId = content.getInt("loctableId");

				if (tmclocation.getLoctableId() != loctableId) {
					tmclocation.changedFields().put("loctableId", loctableId);
				}
			}

			if (tmclocation.changedFields().size() > 0) {
				result.insertObject(tmclocation, ObjStatus.UPDATE, tmclocation.getPid());
			}
			// 修改tmc匹配主表数据
			if (tmclocation.changedFields().size() > 0) {
				result.insertObject(tmclocation, ObjStatus.UPDATE, tmclocation.pid());
			}
		}
	}

	/**
	 * 修改TMC子表信息（属性面板操作）
	 * 
	 * @param result
	 *            结果集
	 * @param locationLinkMap
	 * @param links
	 *            link信息
	 * @param tmcLocationLinkMap
	 * @param direct
	 * @throws Exception
	 */
	private void updateLinks(Result result, RdTmclocation tmclocation, JSONArray links) throws Exception {

		for (int i = 0; i < links.size(); i++) {

			JSONObject linkJson = links.getJSONObject(i);

			if (linkJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(linkJson.getString("objStatus"))) {

					RdTmclocationLink locationLink = tmclocation.linkMap.get(linkJson.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(linkJson.getString("objStatus"))) {
						result.insertObject(locationLink, ObjStatus.DELETE, tmclocation.getPid());

					} else if (ObjStatus.UPDATE.toString().equals(linkJson.getString("objStatus"))) {
						boolean flag = locationLink.fillChangeFields(linkJson);
						if (flag) {
							result.insertObject(locationLink, ObjStatus.UPDATE, locationLink.parentPKValue());
						}
					}
				} else {
					RdTmclocationLink locationLink = new RdTmclocationLink();

					locationLink.Unserialize(linkJson);

					locationLink.setGroupId(tmclocation.getPid());

					result.insertObject(locationLink, ObjStatus.INSERT, tmclocation.getPid());

				}
			}

		}
	}

	/**
	 * 打断link维护tmc:打断后的link加入tmc组成link中,原tmclocationlink删除
	 * 
	 * @param result
	 *            结果集
	 * @param oldLink
	 *            旧link
	 * @param newLinks
	 *            新link
	 */
	public void breakLinkUpdateTmc(Result result, RdLink oldLink, List<RdLink> newLinks) {
		List<IRow> tmcLocations = oldLink.getTmclocations();

		for (IRow row : tmcLocations) {
			RdTmclocation location = (RdTmclocation) row;

			for (IRow tmcLinkRow : location.getLinks()) {
				RdTmclocationLink tmcLink = (RdTmclocationLink) tmcLinkRow;

				if (tmcLink.getLinkPid() == oldLink.getPid()) {
					// 删除原link在tmclocationlink中的记录
					result.insertObject(tmcLink, ObjStatus.DELETE, tmcLink.getGroupId());

					int groupId = tmcLink.getGroupId();

					int locDirect = tmcLink.getLocDirect();

					int direct = tmcLink.getDirect();

					// 打断后新link加入tmclocaitonlink中
					for (RdLink newLink : newLinks) {
						RdTmclocationLink newLocationLink = new RdTmclocationLink();

						newLocationLink.setGroupId(groupId);

						newLocationLink.setDirect(direct);

						newLocationLink.setLocDirect(locDirect);

						newLocationLink.setLinkPid(newLink.getPid());

						result.insertObject(newLocationLink, ObjStatus.INSERT, groupId);
					}
					break;
				}
			}

		}
	}

	/**
	 * 删除link维护TMC：删除该tmc的组成link：如果组成link有且只有该link直接删除该tmc匹配信息
	 * 
	 * @param result
	 * @param oldLink
	 */
	public void deleteLinkUpdateTmc(Result result, List<RdLink> deleteLinks, List<Integer> linkPids) {

		for (RdLink oldLink : deleteLinks) {
			List<IRow> tmcLocations = oldLink.getTmclocations();

			for (IRow row : tmcLocations) {
				List<Integer> tmclocationLinkPids = new ArrayList<>();

				RdTmclocation location = (RdTmclocation) row;

				for (IRow linkRow : location.getLinks()) {
					RdTmclocationLink tmcLink = (RdTmclocationLink) linkRow;

					tmclocationLinkPids.add(tmcLink.getLinkPid());
				}

				// 如果删除的link包含了全部tmclocationlink,则删除tmclocation对象
				if (linkPids.containsAll(tmclocationLinkPids)) {
					result.insertObject(location, ObjStatus.DELETE, location.getPid());

					continue;
				}

				for (IRow tmcLinkRow : location.getLinks()) {
					RdTmclocationLink tmcLink = (RdTmclocationLink) tmcLinkRow;

					if (tmcLink.getLinkPid() == oldLink.getPid()) {
						// 删除原link在tmclocationlink中的记录
						result.insertObject(tmcLink, ObjStatus.DELETE, tmcLink.getGroupId());
						break;
					}
				}

			}
		}
	}

	/**
	 * 删除link、node
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteInfectRdTmc(List<RdLink> deleteLinks, List<Integer> linkPids) throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLink oldLink : deleteLinks) {
			List<IRow> tmcLocations = oldLink.getTmclocations();

			for (IRow row : tmcLocations) {
				List<Integer> tmclocationLinkPids = new ArrayList<>();

				RdTmclocation location = (RdTmclocation) row;

				for (IRow linkRow : location.getLinks()) {
					RdTmclocationLink tmcLink = (RdTmclocationLink) linkRow;

					tmclocationLinkPids.add(tmcLink.getLinkPid());
				}

				// 如果删除的link包含了全部tmclocationlink,则删除tmclocation对象
				if (linkPids.containsAll(tmclocationLinkPids)) {
					AlertObject alertObj = new AlertObject();

					alertObj.setObjType(location.objType());

					alertObj.setPid(location.getPid());

					alertObj.setStatus(ObjStatus.DELETE);

					if (!alertList.contains(alertObj)) {
						alertList.add(alertObj);
					}

					continue;
				}

				for (IRow tmcLinkRow : location.getLinks()) {
					RdTmclocationLink tmcLink = (RdTmclocationLink) tmcLinkRow;

					if (tmcLink.getLinkPid() == oldLink.getPid()) {
						// 删除原link在tmclocationlink中的记录
						AlertObject alertObj = new AlertObject();

						alertObj.setObjType(location.objType());

						alertObj.setPid(location.getPid());

						alertObj.setStatus(ObjStatus.UPDATE);

						if (!alertList.contains(alertObj)) {
							alertList.add(alertObj);
						}
						break;
					}
				}

			}
		}

		return alertList;
	}
}
