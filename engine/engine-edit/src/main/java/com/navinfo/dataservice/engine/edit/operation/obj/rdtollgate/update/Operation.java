package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update;

import java.util.Iterator;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgatePassage;
import com.navinfo.dataservice.dao.pidservice.PidService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:41:31
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		RdTollgate tollgate = this.command.getTollgate();
		JSONObject content = this.command.getContent();
		boolean isChange = tollgate.fillChangeFields(content);
		if (isChange) {
			result.insertObject(tollgate, ObjStatus.UPDATE, tollgate.pid());
		}

		if (content.containsKey("passages")) {
			updatePassage(result, content.getJSONArray("passages"));
		}

		if (content.containsKey("names")) {
			updateName(result, content.getJSONArray("names"));
		}

		return null;
	}

	private void updatePassage(Result result, JSONArray array) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = array.iterator();
		RdTollgatePassage passage = null;
		JSONObject jsonPassage = null;
		while (iterator.hasNext()) {
			jsonPassage = iterator.next();
			if (jsonPassage.containsKey("objStatus")) {
				String objStatus = jsonPassage.getString("objStatus");
				passage = this.command.getTollgate().tollgatePassageMap.get(jsonPassage.getString("rowId"));
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {
					boolean isChange = passage.fillChangeFields(jsonPassage);
					if (isChange) {
						result.insertObject(passage, ObjStatus.UPDATE, passage.getPid());
					}
				} else if (ObjStatus.DELETE.toString().equals(objStatus)) {
					result.insertObject(passage, ObjStatus.DELETE, passage.getPid());
				}
			} else {
				passage = new RdTollgatePassage();
				passage.setPid(this.command.getTollgate().getPid());
				result.insertObject(passage, ObjStatus.INSERT, passage.getPid());
			}
		}

	}

	private void updateName(Result result, JSONArray array) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = array.iterator();
		RdTollgateName name = null;
		JSONObject jsonName = null;
		while (iterator.hasNext()) {
			jsonName = iterator.next();
			if (jsonName.containsKey("objStatus")) {
				String objStatus = jsonName.getString("objStatus");
				name = this.command.getTollgate().tollgateNameMap.get(jsonName.getString("rowId"));
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {
					boolean isChange = name.fillChangeFields(jsonName);
					if (isChange) {
						result.insertObject(name, ObjStatus.UPDATE, name.getNameId());
					}
				} else if (ObjStatus.DELETE.toString().equals(objStatus)) {
					result.insertObject(name, ObjStatus.DELETE, name.getNameId());
				}
			} else {
				name = new RdTollgateName();
				name.setNameId(PidService.getInstance().applyRdTollgateNamePid());
				name.setPid(this.command.getTollgate().getPid());
				result.insertObject(name, ObjStatus.INSERT, name.getNameId());
			}
		}
	}

}
