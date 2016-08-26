package com.navinfo.dataservice.control.row.batch;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

import net.sf.json.JSONObject;

public abstract class IBatch {

	
	public JSONObject run(IxPoi poi) {
		// 在具体检查规则中实现
		return null;
	}
}
