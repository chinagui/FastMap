package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.create;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	/**
	 * 日志记录类
	 */
	private Logger logger = Logger.getLogger(Command.class);

	/**
	 * 参数
	 */
	private String requester;

	/**
	 * 建筑物种别
	 */
	private String kind = "";

	private List<Integer> facePids;

	public String getKind() {
		return kind;
	}

	public List<Integer> getFacePids() {
		return facePids;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.CMGBUILDING;
	}

	public Command(JSONObject json, String requester) {

		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");

		if (data.containsKey("kind")) {

			this.kind = data.getString("kind");
		}
		
		JSONArray array = data.getJSONArray("facePids");

		facePids = new ArrayList<>();

		for (int i = 0; i < array.size(); i++) {

			int pid = Integer.valueOf(array.getString(i));

			if (!facePids.contains(pid)) {
				
				facePids.add(pid);
			}
		}
	}

}
