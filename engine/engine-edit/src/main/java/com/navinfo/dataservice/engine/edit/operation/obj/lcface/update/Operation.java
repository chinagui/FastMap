package com.navinfo.dataservice.engine.edit.operation.obj.lcface.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceName;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月30日 上午9:49:23
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getContent();
		LcFace face = command.getFace();

		if (content.containsKey("objStatus")) {
			boolean isChanged = face.fillChangeFields(content);
			if (isChanged) {
				result.insertObject(face, ObjStatus.UPDATE, face.pid());
			}
		}

		if (content.containsKey("names")) {
			JSONArray names = content.getJSONArray("names");
			this.updateNames(result, names, face);
		}

		return null;
	}

	private void updateNames(Result result, JSONArray names, LcFace face) throws Exception {
		for (int i = 0; i < names.size(); i++) {
			JSONObject nameJson = names.getJSONObject(i);
			if (nameJson.containsKey("objStatus")) {
				if (!ObjStatus.INSERT.toString().equals(nameJson.getString("objStatus"))) {
					LcFaceName name = face.lcFaceNameMap.get(nameJson.getString("rowId"));
					if (ObjStatus.DELETE.toString().equals(nameJson.getString("objStatus"))) {
						result.insertObject(name, ObjStatus.DELETE, face.pid());
					} else if (ObjStatus.UPDATE.toString().equals(nameJson.getString("objStatus"))) {
						boolean isChanged = name.fillChangeFields(nameJson);
						if (isChanged) {
							result.insertObject(name, ObjStatus.UPDATE, face.pid());
						}
					}
				} else {
					LcFaceName name = new LcFaceName();
					name.Unserialize(nameJson);
					name.setFacePid(face.pid());
					result.insertObject(name, ObjStatus.INSERT, face.pid());
				}
			}
		}
	}

}
