package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;

/**
 * 
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:02 
* @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;
	
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}
	
	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(command.getRdObject(), ObjStatus.DELETE, command.getPid());
		
		return null;
	}
	
	/**
	 * 根据CRF组成要素的类型删除对应的CRF主表或者子表数据
	 * 
	 * @param pidList
	 *            组成要素的pid集合（同一个类型要素）
	 * @param type
	 *            组成要素的类型
	 * @param result
	 *            结果集
	 * @throws Exception
	 */
	public void deleteByType(List<Integer> pidList, ObjType type, Result result) throws Exception {
		RdObjectSelector selector = new RdObjectSelector(conn);

		String pids = StringUtils.getInteStr(pidList);

		Map<String, RdObject> objMap = selector.loadRdObjectByPidAndType(pids, type, true);

		for (Map.Entry<String, RdObject> entry : objMap.entrySet()) {
			String tmpPids = entry.getKey();

			List<Integer> tmpPidList = StringUtils.getIntegerListByStr(tmpPids);

			RdObject rdObject = entry.getValue();

			List<IRow> links = rdObject.getLinks();

			List<IRow> roads = rdObject.getRoads();

			List<IRow> inters = rdObject.getInters();
			switch (type) {
			case RDLINK:
				List<Integer> linksLinkPidList = new ArrayList<>();
				for(IRow row : links)
				{
					RdObjectLink link = (RdObjectLink) row;
					
					linksLinkPidList.add(link.getLinkPid());
				}
				if (CollectionUtils.isEmpty(links)) {
					return;
				} else if (CollectionUtils.isEmpty(roads) && CollectionUtils.isEmpty(inters)
						&& tmpPidList.containsAll(linksLinkPidList)) {
					result.insertObject(rdObject, ObjStatus.DELETE, rdObject.getPid());
					return;
				} else {
					for (IRow row : links) {

						RdObjectLink objLink = (RdObjectLink) row;

						for (int tmpPid : tmpPidList) {
							if (objLink.getLinkPid() == tmpPid) {
								result.insertObject(objLink, ObjStatus.DELETE, objLink.getPid());
							}
						}
					}
				}
				break;
			case RDINTER:
				List<Integer> interPidList = new ArrayList<>();
				for(IRow row : inters)
				{
					RdObjectInter inter = (RdObjectInter) row;
					
					interPidList.add(inter.getInterPid());
				}
				if (CollectionUtils.isEmpty(inters)) {
					return;
				} else if (CollectionUtils.isEmpty(roads) && CollectionUtils.isEmpty(links) && tmpPidList.containsAll(interPidList)) {
					result.insertObject(rdObject, ObjStatus.DELETE, rdObject.getPid());
					return;
				} else {
					for (IRow row : inters) {

						RdObjectInter objInter = (RdObjectInter) row;

						for (int tmpPid : tmpPidList) {
							if (objInter.getInterPid() == tmpPid) {
								result.insertObject(objInter, ObjStatus.DELETE, objInter.getPid());
							}
						}
					}
				}
				break;
			case RDROAD:
				List<Integer> roadPidList = new ArrayList<>();
				for(IRow row : roads)
				{
					RdObjectRoad road = (RdObjectRoad) row;
					
					roadPidList.add(road.getRoadPid());
				}
				if (CollectionUtils.isEmpty(roads)) {
					return;
				} else if (CollectionUtils.isEmpty(inters) && CollectionUtils.isEmpty(links) && tmpPidList.containsAll(roadPidList)) {
					result.insertObject(rdObject, ObjStatus.DELETE, rdObject.getPid());
					return;
				} else {
					for (IRow row : roads) {

						RdObjectRoad objRoad = (RdObjectRoad) row;

						for (int tmpPid : tmpPidList) {
							if (objRoad.getRoadPid() == tmpPid) {
								result.insertObject(objRoad, ObjStatus.DELETE, objRoad.getPid());
							}
						}
					}
				}
				break;
			default:
				break;

			}
		}
	}
}
