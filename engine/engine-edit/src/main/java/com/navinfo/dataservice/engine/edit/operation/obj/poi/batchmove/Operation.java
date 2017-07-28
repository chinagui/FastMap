package com.navinfo.dataservice.engine.edit.operation.obj.poi.batchmove;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

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
		IxPoiSelector poiSelector = new IxPoiSelector(this.conn);

		for (int i = 0; i < data.size(); i++) {
			JSONObject obj = data.getJSONObject(i);

			JSONObject moveObj = new JSONObject();
			JSONObject geoObj = new JSONObject();

			moveObj.put("dbId", this.command.getDbId());

			String locationStr = obj.get("location").toString();
			if(locationStr == null || locationStr.isEmpty()||locationStr.contains(",")==false){
				continue;
			}
			String[] location = locationStr.replace("[","" ).replace("]", "").split(",");
			
			String guideLocationStr = obj.get("guidePoint").toString();
			if(guideLocationStr == null || guideLocationStr.isEmpty()||guideLocationStr.contains(",")==false){
				continue;
			}
			String[] guideLocation = guideLocationStr.replace("[","" ).replace("]", "").split(",");

			geoObj.put("longitude", Double.valueOf(location[0]));
			geoObj.put("latitude", Double.valueOf(location[1]));
			geoObj.put("x_guide", Double.valueOf(guideLocation[0]));
			geoObj.put("y_guide", Double.valueOf(guideLocation[1]));
			geoObj.put("linkPid", obj.getInt("guideLink"));

			moveObj.put("objId", obj.getInt("pid"));
			moveObj.put("data", geoObj);

			com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command moveCommand = new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command(
					moveObj, this.command.getRequester());
			
			moveCommand.setIxPoi((IxPoi)poiSelector.loadById(obj.getInt("pid"), true));
			
			com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Operation moveOperation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Operation(
					moveCommand);
			
			moveOperation.run(result);

		}
		return null;
	}
}
