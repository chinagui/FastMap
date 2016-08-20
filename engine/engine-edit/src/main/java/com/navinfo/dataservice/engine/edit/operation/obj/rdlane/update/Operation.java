package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update;

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;

/**
 * @author zhaokk 修改车道信息
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}

	public Operation() {

	}

	@Override
	public String run(Result result) throws Exception {
		this.updateRdLane(result);
		return null;
	}

	/***
	 * 修改车道信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateRdLane(Result result) throws Exception {

		this.updateRdLane(result, command.getContent(),
				this.command.getRdLane());
	}

	public void updateRdLane(Result result, JSONObject content, RdLane rdLane)
			throws Exception {

		if (content.containsKey("objStatus")) {
			boolean isChanged = this.command.getRdLane().fillChangeFields(
					content);

			if (isChanged) {
				result.insertObject(rdLane, ObjStatus.UPDATE, rdLane.getPid());
			}

			if (content.containsKey("conditions")) {
				this.updateCondition(result, rdLane,
						content.getJSONArray("conditions"));
			}
		}
	}

	/***
	 * 字表详细车道的时间段和车辆限制表修改
	 * 
	 * @param result
	 * @param jsonArray
	 * @throws Exception
	 */
	private void updateCondition(Result result, RdLane rdLane,
			JSONArray jsonArray) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = jsonArray.iterator();
		JSONObject jsonCondition = null;
		RdLaneCondition condition = null;
		while (iterator.hasNext()) {
			jsonCondition = iterator.next();
			if (jsonCondition.containsKey("objStatus")) {
				String objStatus = jsonCondition.getString("objStatus");
				condition = rdLane.conditionMap.get(jsonCondition
						.getString("rowId"));
				if (condition == null) {
					throw new Exception("rowId="
							+ jsonCondition.getString("rowId")
							+ "的RdLaneCondition不存在");
				}
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {
					boolean isChange = condition
							.fillChangeFields(jsonCondition);
					if (isChange) {
						result.insertObject(condition, ObjStatus.UPDATE,
								rdLane.getPid());
					}
				}
			}
		}

	}

}
