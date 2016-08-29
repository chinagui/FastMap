package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.batch.util.IBatch;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

import net.sf.json.JSONObject;

public class BatchProcess {

	public BatchProcess() {
		
	}
	
	/**
	 * 执行批处理
	 * @param classNames
	 * @param poi
	 * @throws Exception
	 */
	public void execute(String classNames,JSONObject json,Connection conn) throws Exception {
		JSONObject poiObj = new JSONObject();
		try {
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi poi = (IxPoi) ixPoiSelector.loadById(json.getInt("objId"), true, true);
			
			String[] classes = classNames.split(",");
			JSONObject result = new JSONObject();
			for (String className:classes) {
				className = "com.navinfo.dataservice.control.row.batch." + className;
				IBatch obj = (IBatch) Class.forName(className).newInstance();
				JSONObject data = obj.run(poi,conn,json);
				result.putAll(data);
			}
			if (result.size()>0) {
				result.put("pid", poi.getPid());
				result.put("rowId", poi.getRowId());
				poi.fillChangeFields(result);
				poiObj.put("poi", poi.Serialize(null));
				poiObj.put("pid", poi.getPid());
				poiObj.put("type", "IXPOI");
				poiObj.put("command", "BATCH");
				poiObj.put("dbId", json.getInt("dbId"));
			}
			
			EditApi apiService=(EditApi) ApplicationContextUtil.getBean("editApi");
			apiService.run(poiObj);
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	
}
