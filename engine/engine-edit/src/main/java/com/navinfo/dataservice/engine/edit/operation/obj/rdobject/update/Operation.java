package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectName;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:27
 * @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	private RdObject rdObject;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
		this.rdObject = this.command.getRdObject();
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 不编辑主表信息

		// inter子表
		if (content.containsKey("inters")) {
			updateObjectInter(result);
		}

		// road 子表
		if (content.containsKey("roads")) {
			updateObjectRoad(result);
		}

		// link子表
		if (content.containsKey("links")) {
			updateObjectLink(result);
		}

		// name子表
		if (content.containsKey("names")) {
			updateObjectName(result);
		}

		result.setPrimaryPid(rdObject.parentPKValue());

		return null;
	}

	/**
	 * 跟新rd_object_inter子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateObjectInter(Result result) throws Exception {
		JSONArray subObj = this.command.getInterArray();

		for (IRow inter : rdObject.getInters()) {
			RdObjectInter objectInter = (RdObjectInter) inter;
			if (subObj == null) {
				result.insertObject(objectInter, ObjStatus.DELETE, objectInter.getPid());
			} else if (!subObj.contains(objectInter.getInterPid())) {
				result.insertObject(objectInter, ObjStatus.DELETE, objectInter.getPid());
			} else {
				subObj.remove((Integer) objectInter.getInterPid());
			}
		}

		List<Integer> pidList = new ArrayList<>();

		for (int i = 0; i < subObj.size(); i++) {

			int interPid = subObj.getInt(i);

			if (!pidList.contains(interPid)) {
				RdObjectInter rdObjectInter = new RdObjectInter();

				rdObjectInter.setInterPid(interPid);

				rdObjectInter.setPid(rdObject.getPid());

				result.insertObject(rdObjectInter, ObjStatus.INSERT, rdObjectInter.getPid());

				pidList.add(interPid);
			}
		}

	}

	/**
	 * 跟新rd_object_road子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateObjectRoad(Result result) throws Exception {
		JSONArray subObj = this.command.getRoadArray();

		for (IRow road : rdObject.getRoads()) {
			RdObjectRoad objectRoad = (RdObjectRoad) road;

			if (subObj == null) {
				result.insertObject(objectRoad, ObjStatus.DELETE, objectRoad.getPid());
			} else if (!subObj.contains(objectRoad.getRoadPid())) {
				result.insertObject(objectRoad, ObjStatus.DELETE, objectRoad.getPid());
			} else {
				subObj.remove((Integer) objectRoad.getRoadPid());
			}
		}

		List<Integer> pidList = new ArrayList<>();

		for (int i = 0; i < subObj.size(); i++) {

			int roadPid = subObj.getInt(i);

			if (!pidList.contains(roadPid)) {
				RdObjectRoad rdObjectRoad = new RdObjectRoad();

				rdObjectRoad.setRoadPid(subObj.getInt(i));

				rdObjectRoad.setPid(rdObject.getPid());

				result.insertObject(rdObjectRoad, ObjStatus.INSERT, rdObjectRoad.getPid());

				pidList.add(roadPid);
			}
		}

	}

	/**
	 * 更新link子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateObjectLink(Result result) throws Exception {
		JSONArray subObj = this.command.getLinkArray();

		if (subObj == null) {
			throw new Exception("link参数不对");
		}

		for (IRow link : rdObject.getLinks()) {

			RdObjectLink objLink = (RdObjectLink) link;

			if (!subObj.contains(objLink.getLinkPid())) {
				result.insertObject(objLink, ObjStatus.DELETE, objLink.getPid());
			} else {
				subObj.remove((Integer) objLink.getLinkPid());
			}
		}

		List<Integer> pidList = new ArrayList<>();

		for (int i = 0; i < subObj.size(); i++) {

			int linkPid = subObj.getInt(i);

			if (!pidList.contains(linkPid)) {
				RdObjectLink objLink = new RdObjectLink();

				objLink.setLinkPid(subObj.getInt(i));

				objLink.setPid(rdObject.getPid());

				result.insertObject(objLink, ObjStatus.INSERT, objLink.getPid());

				pidList.add(linkPid);
			}
		}
	}

	/**
	 * 更新link子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateObjectName(Result result) throws Exception {
		JSONArray names = this.command.getNameArray();

		for (int i = 0; i < names.size(); i++) {

			JSONObject nameJson = names.getJSONObject(i);

			if (nameJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(nameJson.getString("objStatus"))) {

					RdObjectName name = rdObject.nameMap.get(nameJson.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(nameJson.getString("objStatus"))) {
						result.insertObject(name, ObjStatus.DELETE, rdObject.pid());

					} else if (ObjStatus.UPDATE.toString().equals(nameJson.getString("objStatus"))) {

						boolean isChanged = name.fillChangeFields(nameJson);

						if (isChanged) {
							result.insertObject(name, ObjStatus.UPDATE, rdObject.pid());
						}
					}
				} else {
					RdObjectName name = new RdObjectName();

					name.Unserialize(nameJson);

					name.setPid(rdObject.getPid());

					name.setNameId(PidUtil.getInstance().applyAdAdminNamePid());

					result.insertObject(name, ObjStatus.INSERT, name.getPid());

				}
			}

		}
	}

	/**
	 * 根据road更新CRF对象：如果属于对象的link制作成了CRF
	 * Road，则需要讲CRF对象link子表数据删除，CRF对象road子表新增一条数据
	 * 
	 * @param road
	 *            road对象
	 * @param result
	 *            结果集
	 * @throws Exception
	 */
	public void updateRdObjectForRdRoad(RdRoad road, Result result) throws Exception {
		List<IRow> roadLinks = road.getLinks();

		List<Integer> linkPidList = new ArrayList<>();

		for (IRow row : roadLinks) {
			RdRoadLink roadLink = (RdRoadLink) row;

			linkPidList.add(roadLink.getLinkPid());
		}

		RdObjectSelector selector = new RdObjectSelector(conn);

		Map<String, RdObject> rdObjectMap = selector.loadRdObjectByPidAndType(StringUtils.getInteStr(linkPidList),
				ObjType.RDLINK, true);

		for (Map.Entry<String, RdObject> entry : rdObjectMap.entrySet()) {
			String tmpPids = entry.getKey();

			List<Integer> tmpPidList = StringUtils.getIntegerListByStr(tmpPids);

			RdObject rdObject = entry.getValue();

			List<IRow> links = rdObject.getLinks();

			// 是否需要将road的link升级
			boolean updateRdObject = false;

			for (IRow row : links) {
				RdObjectLink objLink = (RdObjectLink) row;

				for (Integer tmpPid : tmpPidList) {
					if (objLink.getLinkPid() == tmpPid) {
						result.insertObject(objLink, ObjStatus.DELETE, objLink.getPid());
						updateRdObject = true;
					}
				}
			}

			if (updateRdObject) {
				List<IRow> roads = rdObject.getRoads();

				List<Integer> roadPids = new ArrayList<>();

				for (IRow row : roads) {
					RdObjectRoad roadObject = (RdObjectRoad) row;

					roadPids.add(roadObject.getRoadPid());
				}

				// 传入的road如果之前在crf对象中时不做处理，不在的话加入crf对象的road子表中
				if (!roadPids.contains(road.getPid())) {
					RdObjectRoad objRoad = new RdObjectRoad();

					objRoad.setPid(rdObject.getPid());

					objRoad.setRoadPid(road.getPid());

					result.insertObject(objRoad, ObjStatus.INSERT, objRoad.getPid());
				}
			}
		}
	}

	/**
	 * 根据inter更新CRF对象：如果属于对象的link制作成了CRF
	 * inter，则需要讲CRF对象link子表数据删除，CRF对象inter子表新增一条数据
	 * 
	 * @param inter
	 *            inter对象
	 * @param result
	 *            结果集
	 * @throws Exception
	 */
	public void updateRdObjectForRdInter(RdInter inter, Result result) throws Exception {
		List<IRow> interLinks = inter.getLinks();

		List<Integer> linkPidList = new ArrayList<>();

		for (IRow row : interLinks) {
			RdInterLink interLink = (RdInterLink) row;

			linkPidList.add(interLink.getLinkPid());
		}

		RdObjectSelector selector = new RdObjectSelector(conn);

		Map<String, RdObject> rdObjectMap = selector.loadRdObjectByPidAndType(StringUtils.getInteStr(linkPidList),
				ObjType.RDLINK, true);

		for (Map.Entry<String, RdObject> entry : rdObjectMap.entrySet()) {
			String tmpPids = entry.getKey();

			List<Integer> tmpPidList = StringUtils.getIntegerListByStr(tmpPids);

			RdObject rdObject = entry.getValue();

			List<IRow> links = rdObject.getLinks();

			// 是否需要将road的link升级
			boolean updateRdObject = false;

			for (IRow row : links) {
				RdObjectLink objLink = (RdObjectLink) row;

				for (Integer tmpPid : tmpPidList) {
					if (objLink.getLinkPid() == tmpPid) {
						result.insertObject(objLink, ObjStatus.DELETE, objLink.getPid());
						updateRdObject = true;
					}
				}
			}

			if (updateRdObject) {
				List<IRow> inters = rdObject.getInters();

				List<Integer> interPids = new ArrayList<>();

				for (IRow row : inters) {
					RdObjectInter interObject = (RdObjectInter) row;

					interPids.add(interObject.getInterPid());
				}

				// 传入的road如果之前在crf对象中时不做处理，不在的话加入crf对象的road子表中
				if (!interPids.contains(inter.getPid())) {
					RdObjectRoad objRoad = new RdObjectRoad();

					objRoad.setPid(rdObject.getPid());

					objRoad.setRoadPid(inter.getPid());

					result.insertObject(objRoad, ObjStatus.INSERT, objRoad.getPid());
				}
			}
		}
	}

	/**
	 * 打断link维护CRF对象组成link关系
	 * 
	 * @param oldLink
	 *            旧link对象
	 * @param newLinks
	 *            打断后新的link对象
	 * @param result
	 *            结果集
	 * @throws Exception
	 */
	public void breakRdObjectLink(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
		RdObjectSelector selector = new RdObjectSelector(conn);

		String linkPid = String.valueOf(oldLink.getPid());

		Map<String, RdObject> rdObjMap = selector.loadRdObjectByPidAndType(linkPid, ObjType.RDLINK, true);

		RdObject rdObject = rdObjMap.get(linkPid);

		// CRF对象不为null,需要维护rd_object_link,将新的links加入该表
		if (rdObject != null) {
			List<IRow> links = rdObject.getLinks();

			for (IRow row : links) {
				RdObjectLink objLink = (RdObjectLink) row;

				if (objLink.getLinkPid() == oldLink.getPid()) {
					result.insertObject(objLink, ObjStatus.DELETE, objLink.getPid());
				}
			}

			for (RdLink link : newLinks) {
				RdObjectLink objLink = new RdObjectLink();

				objLink.setLinkPid(link.getPid());

				objLink.setPid(rdObject.getPid());

				result.insertObject(objLink, ObjStatus.INSERT, objLink.getPid());
			}
		}
	}

	/**
	 * 如果给CRF Inter中增加link，但是该link属于某一CRFO，则编辑之后，该CRF Inter属于该CRFO
	 * 
	 * @param linkPidList
	 *            新加入inter的linkpid集合
	 * @param rdInter
	 *            CRFI对象
	 * @throws Exception
	 */
	public void updateRdObject(Result result, List<Integer> linkPidList, RdInter rdInter) throws Exception {
		RdObjectSelector rdObjectSelector = new RdObjectSelector(conn);

		Map<String, RdObject> objMap = rdObjectSelector.loadRdObjectByPidAndType(StringUtils.getInteStr(linkPidList),
				ObjType.RDLINK, true);

		if (objMap.size() > 1) {
			throw new Exception("所选的LINK不能属于不同的CRFO");
		} else if (objMap.size() == 1) {
			String objKeys = objMap.entrySet().iterator().next().getKey();

			RdObject object = objMap.entrySet().iterator().next().getValue();

			List<IRow> interList = object.getInters();

			boolean isObjInter = false;

			for (IRow row : interList) {
				RdObjectInter objInter = (RdObjectInter) row;

				if (objInter.getInterPid() == rdInter.getPid()) {
					isObjInter = true;

					break;
				}
			}
			
			List<IRow> objLinks = object.getLinks();
			
			String linkPids[] = objKeys.split(",");
			
			for(int i = 0;i<linkPids.length;i++)
			{
				int linkPid = Integer.parseInt(linkPids[i]);
				
				//删除原来的rdobjlink关系，该关系移入rdobjinter关系中
				for(IRow row : objLinks)
				{
					RdObjectLink objLink = (RdObjectLink) row;
					
					if(objLink.getLinkPid() == linkPid)
					{
						result.insertObject(objLink, ObjStatus.DELETE, objLink.getPid());
						
						break;
					}
				}
				
			}
			
			//如果inter原来属于CRFO，则objInter关系不变，不属于的，需要把inter加入objinter关系中
			if(!isObjInter)
			{
				RdObjectInter objInter = new RdObjectInter();
				
				objInter.setPid(object.getPid());
				
				objInter.setInterPid(rdInter.getPid());
				
				result.insertObject(objInter, ObjStatus.INSERT, objInter.getPid());
			}
		}
	}
}
