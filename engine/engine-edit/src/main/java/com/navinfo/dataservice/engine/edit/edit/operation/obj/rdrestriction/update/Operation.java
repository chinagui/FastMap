package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdrestriction.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdRestriction restrict;

	public Operation(Command command, RdRestriction restrict) {
		this.command = command;

		this.restrict = restrict;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 判断是否存在交限进入线
		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.getDelObjects().add(restrict);
				
				return null;
			} else {

				boolean isChanged = restrict.fillChangeFields(content);

				if (isChanged) {
					result.getUpdateObjects().add(restrict);
				}
			}
		}

		if (content.containsKey("details")) {
			JSONArray details = content.getJSONArray("details");

			for (int i = 0; i < details.size(); i++) {

				JSONObject detailJson = details.getJSONObject(i);

				if (detailJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString()
							.equals(detailJson.getString("objStatus"))) {

						RdRestrictionDetail detail = restrict.detailMap
								.get(detailJson.getInt("pid"));
						
						if (detail == null){
							throw new Exception("detailId="+detailJson.getInt("pid")+"的交限detail不存在");
						}

						if (ObjStatus.DELETE.toString().equals(detailJson
								.getString("objStatus"))) {
							result.getDelObjects().add(detail);
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(detailJson
								.getString("objStatus"))) {

							boolean isChanged = detail
									.fillChangeFields(detailJson);

							if (isChanged) {
								result.getUpdateObjects().add(detail);
							}
						}
					} else {
						RdRestrictionDetail detail = new RdRestrictionDetail();
						
						detail.Unserialize(detailJson);
						
						detail.setPid(PidService.getInstance().applyRestrictionDetailPid());
						
						detail.setRestricPid(restrict.getPid());
						
						detail.setMesh(restrict.mesh());
						
						result.getAddObjects().add(detail);
						
						continue;
					}
				}

				if (detailJson.containsKey("conditions")) {
					
					int detailId = detailJson.getInt("pid");

					JSONArray conds = detailJson.getJSONArray("conditions");

					for (int j = 0; j < conds.size(); j++) {
						JSONObject cond = conds.getJSONObject(j);

						if (!cond.containsKey("objStatus")) {
							throw new Exception(
									"传入请求内容格式错误，conditions不存在操作类型objType");
						}

						if (!ObjStatus.INSERT.toString()
								.equals(cond.getString("objStatus"))) {

							RdRestrictionCondition condition = restrict.conditionMap.get(cond.getString("rowId"));
							
							if (condition == null){
								throw new Exception("rowId="+cond.getString("rowId")+"的交限condition不存在");
							}

							if (ObjStatus.DELETE.toString().equals(cond
									.getString("objStatus"))) {
								result.getDelObjects().add(condition);
								
							} else if (ObjStatus.UPDATE.toString().equals(cond
									.getString("objStatus"))) {

								boolean isChanged = condition
										.fillChangeFields(cond);

								if (isChanged) {
									result.getUpdateObjects().add(condition);
								}
							}
						} else {
							RdRestrictionCondition condition = new RdRestrictionCondition();
							
							condition.Unserialize(cond);
							
							condition.setDetailId(detailId);
							
							condition.setMesh(restrict.mesh());
							
							result.getAddObjects().add(condition);
							
						}
					
					}

				}
			}
		}

		return null;
	}

}
