package com.navinfo.dataservice.engine.edit.operation.obj.tmc.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;

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

		if (content.containsKey("objStatus")) {
			//判断是否修改tmc匹配信息的方向：包含isChangedDirect参数，并且该参数值为1代表修改
			if (content.containsKey("isChangedDirect") && content.getInt("isChangedDirect") == 1) {
				for (IRow row : tmclocation.getLinks()) {
					RdTmclocationLink tmcLink = (RdTmclocationLink) row;
					
					int tmcDirect = tmcLink.getDirect();
					//修改方向关系，将原来的1值改为2,2值该为1即可
					tmcLink.changedFields().put("direct", tmcDirect == 1? 2:1);

					result.insertObject(tmcLink, ObjStatus.UPDATE, tmcLocationPid);
				}
			}
			boolean isChanged = tmclocation.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(tmclocation, ObjStatus.UPDATE, tmclocation.pid());
			}
		}
		//修改子表数据
		if (content.containsKey("links")) {

			JSONArray links = content.getJSONArray("links");

			this.updateLinks(result, tmclocation, links);
		}
		return null;
	}

	/**
	 * 修改TMC子表信息
	 * 
	 * @param result
	 *            结果集
	 * @param links
	 *            link信息
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

						boolean isChanged = locationLink.fillChangeFields(linkJson);

						if (isChanged) {
							result.insertObject(locationLink, ObjStatus.UPDATE, tmclocation.getPid());
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
}
