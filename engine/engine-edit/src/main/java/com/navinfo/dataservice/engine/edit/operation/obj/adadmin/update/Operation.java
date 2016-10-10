package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;

public class Operation implements IOperation {

	private Command command;

	private AdAdmin adAdmin;

	public Operation(Command command, AdAdmin adAdmin) {
		this.command = command;

		this.adAdmin = adAdmin;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(adAdmin, ObjStatus.DELETE, adAdmin.pid());

				return null;
			} else {

				boolean isChanged = adAdmin.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(adAdmin, ObjStatus.UPDATE, adAdmin.pid());
				}
			}
		}

		if (content.containsKey("names")) {
			JSONArray names = content.getJSONArray("names");

			for (int i = 0; i < names.size(); i++) {

				JSONObject json = names.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

						AdAdminName row = adAdmin.adAdminNameMap.get(json.getString("rowId"));

						if (row == null) {
							throw new Exception("rowId=" + json.getString("rowId") + "的AdAdminName不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
							result.insertObject(row, ObjStatus.DELETE, adAdmin.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

							boolean isChanged = row.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(row, ObjStatus.UPDATE, adAdmin.pid());
							}
						}
					} else {
						AdAdminName row = new AdAdminName();

						row.Unserialize(json);

						row.setPid(PidUtil.getInstance().applyAdAdminNamePid());

						row.setRegionId(adAdmin.getPid());

						row.setMesh(adAdmin.mesh());

						result.insertObject(row, ObjStatus.INSERT, adAdmin.pid());
					}
				}
			}
		}

		return null;
	}

	/**
	 * 删除link对行政区划代表点的更新影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateAdminInfectData(int linkPid, Connection conn) throws Exception {

		AdAdminSelector selector = new AdAdminSelector(conn);

		List<AdAdmin> adAdminList = selector.loadRowsByLinkId(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (AdAdmin adAdmin : adAdminList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(adAdmin.objType());

			alertObj.setPid(adAdmin.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

}
