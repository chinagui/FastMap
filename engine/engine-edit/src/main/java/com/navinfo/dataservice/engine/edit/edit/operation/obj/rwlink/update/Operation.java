package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RwLink updateLink;

	public Operation(Command command, RwLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getUpdateContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());

				return null;
			} else {

				boolean isChanged = updateLink.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
				}
			}
		}
		if (content.containsKey("names")) {

			JSONArray names = content.getJSONArray("names");

			this.saveNames(result, names);
		}
		return null;
	}
	
	/**
	 * 修改铁路link对name子表操作
	 * @param result 结果集
	 * @param names name信息
	 * @throws Exception
	 */
	private void saveNames(Result result, JSONArray names) throws Exception {

		for (int i = 0; i < names.size(); i++) {

			JSONObject nameJson = names.getJSONObject(i);

			if (nameJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						nameJson.getString("objStatus"))) {

					RwLinkName name = updateLink.linkNameMap.get(nameJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							nameJson.getString("objStatus"))) {
						result.insertObject(name, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							nameJson.getString("objStatus"))) {

						boolean isChanged = name.fillChangeFields(nameJson);

						if (isChanged) {
							result.insertObject(name, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RwLinkName name = new RwLinkName();

					name.Unserialize(nameJson);

					name.setLinkPid(this.updateLink.getPid());
					
					result.insertObject(name, ObjStatus.INSERT, updateLink.pid());

				}
			}

		}

	}
}