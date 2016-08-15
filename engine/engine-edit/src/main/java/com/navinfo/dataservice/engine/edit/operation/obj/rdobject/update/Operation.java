package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;

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
			updateObjectInter(result, content);
		}

		// road 子表

		if (content.containsKey("roads")) {
			updateObjectRoad(result, content);
		}

		// link子表
		if (content.containsKey("links")) {
			updateObjectLink(result, content);
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
	private void updateObjectInter(Result result, JSONObject content) throws Exception {
		JSONArray subObj = this.command.getInterArray();

		for (IRow inter : rdObject.getInters()) {
			RdObjectInter objectInter = (RdObjectInter) inter;
			if (subObj == null) {
				result.insertObject(objectInter, ObjStatus.DELETE, objectInter.getInterPid());
			} else if (!subObj.contains(objectInter.getInterPid())) {
				result.insertObject(objectInter, ObjStatus.DELETE, objectInter.getInterPid());
			} else {
				subObj.remove((Integer) objectInter.getInterPid());
			}
		}
		for (int i = 0; i < subObj.size(); i++) {

			RdObjectInter rdObjectInter = new RdObjectInter();

			rdObjectInter.setInterPid(subObj.getInt(i));

			rdObjectInter.setPid(rdObject.getPid());

			result.insertObject(rdObjectInter, ObjStatus.INSERT, rdObjectInter.getInterPid());
		}

	}

	/**
	 * 跟新rd_object_road子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateObjectRoad(Result result, JSONObject content) throws Exception {
		JSONArray subObj = this.command.getRoadArray();

		for (IRow road : rdObject.getRoads()) {
			RdObjectRoad objectRoad = (RdObjectRoad) road;

			if (subObj == null) {
				result.insertObject(objectRoad, ObjStatus.DELETE, objectRoad.getRoadPid());
			} else if (!subObj.contains(objectRoad.getRoadPid())) {
				result.insertObject(objectRoad, ObjStatus.DELETE, objectRoad.getRoadPid());
			} else {
				subObj.remove((Integer) objectRoad.getRoadPid());
			}
		}
		for (int i = 0; i < subObj.size(); i++) {

			RdObjectRoad rdObjectRoad = new RdObjectRoad();

			rdObjectRoad.setRoadPid(subObj.getInt(i));

			rdObjectRoad.setPid(rdObject.getPid());

			result.insertObject(rdObjectRoad, ObjStatus.INSERT, rdObjectRoad.getRoadPid());
		}

	}

	/**
	 * 更新link子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateObjectLink(Result result, JSONObject content) throws Exception {
		JSONArray subObj = this.command.getLinkArray();

		for (IRow link : rdObject.getLinks()) {

			RdObjectLink objLink = (RdObjectLink) link;

			if (subObj == null) {
				result.insertObject(objLink, ObjStatus.DELETE, objLink.getLinkPid());
			} else if (!subObj.contains(objLink.getLinkPid())) {
				result.insertObject(objLink, ObjStatus.DELETE, objLink.getLinkPid());
			} else {
				subObj.remove((Integer) objLink.getLinkPid());
			}
		}

		for (int i = 0; i < subObj.size(); i++) {

			RdObjectLink objLink = new RdObjectLink();

			objLink.setLinkPid(subObj.getInt(i));

			objLink.setPid(rdObject.getPid());

			result.insertObject(objLink, ObjStatus.INSERT, objLink.getLinkPid());
		}
	}
}
