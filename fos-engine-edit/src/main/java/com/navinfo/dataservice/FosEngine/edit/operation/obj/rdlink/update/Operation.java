package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimitTruck;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkRtic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkWalkstair;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkZone;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdLink updateLink;

	public Operation(Command command, RdLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getUpdateContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.getDelObjects().add(updateLink);

				return null;
			} else {

				boolean isChanged = updateLink.fillChangeFields(content);

				if (isChanged) {
					result.getUpdateObjects().add(updateLink);
				}
			}
		}

		if (content.containsKey("forms")) {
			JSONArray forms = content.getJSONArray("forms");

			this.saveForms(result, forms);
		}

		if (content.containsKey("limits")) {
			JSONArray limits = content.getJSONArray("limits");

			this.saveLimits(result, limits);
		}

		if (content.containsKey("names")) {

			JSONArray names = content.getJSONArray("names");

			this.saveNames(result, names);
		}

		if (content.containsKey("limitTrucks")) {

			JSONArray array = content.getJSONArray("limitTrucks");

			this.saveLimitTrucks(result, array);
		}

		if (content.containsKey("speedlimits")) {

			JSONArray array = content.getJSONArray("speedlimits");

			this.saveSpeedlimits(result, array);
		}

		if (content.containsKey("sidewalks")) {

			JSONArray array = content.getJSONArray("sidewalks");

			this.saveSidewalks(result, array);
		}

		if (content.containsKey("walkstairs")) {

			JSONArray array = content.getJSONArray("walkstairs");

			this.saveWalkstairs(result, array);
		}

		if (content.containsKey("rtics")) {

			JSONArray array = content.getJSONArray("rtics");

			this.saveRtics(result, array);
		}

		if (content.containsKey("intRtics")) {

			JSONArray array = content.getJSONArray("intRtics");

			this.saveIntRtics(result, array);
		}

		if (content.containsKey("zones")) {

			JSONArray array = content.getJSONArray("zones");

			this.saveZones(result, array);
		}

		return null;
	}

	private void saveForms(Result result, JSONArray forms) throws Exception {
		for (int i = 0; i < forms.size(); i++) {

			JSONObject formJson = forms.getJSONObject(i);

			if (formJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						formJson.getString("objStatus"))) {

					RdLinkForm form = updateLink.formMap.get(formJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							formJson.getString("objStatus"))) {
						result.getDelObjects().add(form);

					} else if (ObjStatus.UPDATE.toString().equals(
							formJson.getString("objStatus"))) {

						boolean isChanged = form.fillChangeFields(formJson);

						if (isChanged) {
							result.getUpdateObjects().add(form);
						}
					}
				} else {
					RdLinkForm form = new RdLinkForm();

					form.Unserialize(formJson);
					
					form.setLinkPid(this.updateLink.getPid());
					
					form.setMesh(updateLink.mesh());

					result.getAddObjects().add(form);

				}
			}

		}

	}

	private void saveLimits(Result result, JSONArray limits) throws Exception {

		for (int i = 0; i < limits.size(); i++) {

			JSONObject limitJson = limits.getJSONObject(i);

			if (limitJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						limitJson.getString("objStatus"))) {

					RdLinkLimit limit = updateLink.limitMap.get(limitJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							limitJson.getString("objStatus"))) {
						result.getDelObjects().add(limit);

					} else if (ObjStatus.UPDATE.toString().equals(
							limitJson.getString("objStatus"))) {

						boolean isChanged = limit.fillChangeFields(limitJson);

						if (isChanged) {
							result.getUpdateObjects().add(limit);
						}
					}
				} else {
					RdLinkLimit limit = new RdLinkLimit();

					limit.Unserialize(limitJson);
					
					limit.setLinkPid(this.updateLink.getPid());
					
					limit.setMesh(updateLink.mesh());

					result.getAddObjects().add(limit);

				}
			}

		}
	}

	private void saveLimitTrucks(Result result, JSONArray array)
			throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkLimitTruck obj = updateLink.limitTruckMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkLimitTruck obj = new RdLinkLimitTruck();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveSpeedlimits(Result result, JSONArray array)
			throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkSpeedlimit obj = updateLink.speedlimitMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkSpeedlimit obj = new RdLinkSpeedlimit();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveSidewalks(Result result, JSONArray array) throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkSidewalk obj = updateLink.sidewalkMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkSidewalk obj = new RdLinkSidewalk();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveWalkstairs(Result result, JSONArray array)
			throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkWalkstair obj = updateLink.walkstairMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkWalkstair obj = new RdLinkWalkstair();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveRtics(Result result, JSONArray array) throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkRtic obj = updateLink.rticMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkRtic obj = new RdLinkRtic();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveIntRtics(Result result, JSONArray array) throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkIntRtic obj = updateLink.intRticMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkIntRtic obj = new RdLinkIntRtic();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveZones(Result result, JSONArray array) throws Exception {

		for (int i = 0; i < array.size(); i++) {

			JSONObject json = array.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdLinkZone obj = updateLink.zoneMap.get(json
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.getDelObjects().add(obj);

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.getUpdateObjects().add(obj);
						}
					}
				} else {
					RdLinkZone obj = new RdLinkZone();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(updateLink.mesh());

					result.getAddObjects().add(obj);

				}
			}

		}

	}

	private void saveNames(Result result, JSONArray names) throws Exception {

		for (int i = 0; i < names.size(); i++) {

			JSONObject nameJson = names.getJSONObject(i);

			if (nameJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						nameJson.getString("objStatus"))) {

					RdLinkName name = updateLink.nameMap.get(nameJson
							.getString("rowId"));

					if (ObjStatus.DELETE.toString().equals(
							nameJson.getString("objStatus"))) {
						result.getDelObjects().add(name);

					} else if (ObjStatus.UPDATE.toString().equals(
							nameJson.getString("objStatus"))) {

						boolean isChanged = name.fillChangeFields(nameJson);

						if (isChanged) {
							result.getUpdateObjects().add(name);
						}
					}
				} else {
					RdLinkName name = new RdLinkName();

					name.Unserialize(nameJson);

					name.setLinkPid(this.updateLink.getPid());
					
					name.setMesh(updateLink.mesh());
					
					result.getAddObjects().add(name);

				}
			}

		}

	}
}
