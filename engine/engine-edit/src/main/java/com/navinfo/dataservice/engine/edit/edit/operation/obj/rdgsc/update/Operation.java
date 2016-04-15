package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RdGsc rdGsc;


	public Operation(Command command, RdGsc rdGsc) {
		this.command = command;

		this.rdGsc = rdGsc;

	}
	
	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());

				return null;
			} else {

				boolean isChanged = rdGsc.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(rdGsc, ObjStatus.UPDATE, rdGsc.pid());
				}
			}
		}

		if (content.containsKey("linkObjs")) {
			JSONArray links = content.getJSONArray("linkObjs");

			for (int i = 0; i < links.size(); i++) {

				JSONObject json = links.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

						RdGscLink link = rdGsc.rdGscLinkMap.get(json.getString("rowId"));

						if (link == null) {
							throw new Exception("rowId=" + json.getString("rowId") + "的rd_cross_link不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
							result.insertObject(link, ObjStatus.DELETE, rdGsc.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

							boolean isChanged = link.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(link, ObjStatus.UPDATE, rdGsc.pid());
							}
						}
					}
				}

			}
		}

		return null;
	}
}
