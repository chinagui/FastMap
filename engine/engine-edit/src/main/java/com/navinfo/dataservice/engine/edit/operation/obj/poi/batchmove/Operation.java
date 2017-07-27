package com.navinfo.dataservice.engine.edit.operation.obj.poi.batchmove;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {
	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONArray data = this.command.getContent();

		for (int i = 0; i < data.size(); i++) {
			JSONObject obj = data.getJSONObject(i);

			JSONObject moveObj = new JSONObject();
			JSONObject geoObj = new JSONObject();

			moveObj.put("dbId", this.command.getDbId());

			double[] location = (double[]) obj.get("location");
			double[] guideLocation = (double[]) obj.get("guidePoint");

			geoObj.put("longitude", location[0]);
			geoObj.put("latitude", location[1]);
			geoObj.put("x_guide", guideLocation[0]);
			geoObj.put("y_guide", guideLocation[1]);

			moveObj.put("objId", obj.get("pid"));
			moveObj.put("data", geoObj);
			moveObj.put("linkPid", obj.get("guideLink"));

			com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command moveCommand = new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command(
					moveObj, this.command.getRequester());
			com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Operation moveOperation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Operation(
					moveCommand);
			moveOperation.run(result);

		}
		return null;
	}
}
