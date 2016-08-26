package com.navinfo.dataservice.control.row.batch;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

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
	public void execute(String classNames,IxPoi poi,int dbId) throws Exception {
		JSONObject poiObj = new JSONObject();
		try {
			String[] classes = classNames.split(",");
			JSONObject result = new JSONObject();
			for (String className:classes) {
				IBatch obj = (IBatch) Class.forName(className).newInstance();
				JSONObject data = obj.run(poi);
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
				poiObj.put("dbId", dbId);
			}
			
			EditApi apiService=(EditApi) ApplicationContextUtil.getBean("editApi");
			apiService.run(poiObj);
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	
}
