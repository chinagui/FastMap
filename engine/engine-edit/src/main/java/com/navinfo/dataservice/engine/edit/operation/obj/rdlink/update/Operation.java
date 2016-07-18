package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimitTruck;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkWalkstair;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;

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
				result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());

				return null;
			} else {

				boolean isChanged = updateLink.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
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
		
		int deleteCount = 0;
		
		int insertCount = 0;
		
		for (int i = 0; i < forms.size(); i++) {

			JSONObject formJson = forms.getJSONObject(i);

			if (formJson.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						formJson.getString("objStatus"))) {

					RdLinkForm form = updateLink.formMap.get(formJson
							.getString("rowId"));
					if(form == null)
					{
						throw new Exception("rowId为"+formJson
								.getString("rowId")+"的RdLinkForm不存在");
					}
					
					if (ObjStatus.DELETE.toString().equals(
							formJson.getString("objStatus"))) {
						result.insertObject(form, ObjStatus.DELETE, updateLink.pid());
						
						deleteCount++;

					} else if (ObjStatus.UPDATE.toString().equals(
							formJson.getString("objStatus"))) {

						boolean isChanged = form.fillChangeFields(formJson);

						if (isChanged) {
							result.insertObject(form, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkForm form = new RdLinkForm();

					form.Unserialize(formJson);
					
					form.setLinkPid(this.updateLink.getPid());
					
					form.setMesh(this.updateLink.getMeshId());

					result.insertObject(form, ObjStatus.INSERT, updateLink.pid());

					insertCount++;
				}
			}

		}
		
		if(insertCount==0 && deleteCount==updateLink.getForms().size()){
			//rd_link_form被清空时，自动添加一条
			
			RdLinkForm form = new RdLinkForm();
			
			form.setLinkPid(this.updateLink.getPid());
			
			form.setMesh(this.updateLink.getMeshId());
			
			result.insertObject(form, ObjStatus.INSERT, this.updateLink.pid());
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
						result.insertObject(limit, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							limitJson.getString("objStatus"))) {

						boolean isChanged = limit.fillChangeFields(limitJson);

						if (isChanged) {
							result.insertObject(limit, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkLimit limit = new RdLinkLimit();

					limit.Unserialize(limitJson);
					
					limit.setLinkPid(this.updateLink.getPid());
					
					limit.setMesh(this.updateLink.getMeshId());

					result.insertObject(limit, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkLimitTruck obj = new RdLinkLimitTruck();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkSpeedlimit obj = new RdLinkSpeedlimit();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkSidewalk obj = new RdLinkSidewalk();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkWalkstair obj = new RdLinkWalkstair();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkRtic obj = new RdLinkRtic();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkIntRtic obj = new RdLinkIntRtic();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = obj.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkZone obj = new RdLinkZone();

					obj.Unserialize(json);
					
					obj.setLinkPid(this.updateLink.getPid());
					
					obj.setMesh(this.updateLink.getMeshId());

					result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

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
						result.insertObject(name, ObjStatus.DELETE, updateLink.pid());

					} else if (ObjStatus.UPDATE.toString().equals(
							nameJson.getString("objStatus"))) {

						boolean isChanged = name.fillChangeFields(nameJson);

						if (isChanged) {
							result.insertObject(name, ObjStatus.UPDATE, updateLink.pid());
						}
					}
				} else {
					RdLinkName name = new RdLinkName();

					name.Unserialize(nameJson);

					name.setLinkPid(this.updateLink.getPid());
					
					result.insertObject(name, ObjStatus.INSERT, updateLink.pid());

				}
			}

		}

	}
}