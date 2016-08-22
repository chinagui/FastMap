package com.navinfo.dataservice.engine.edit.service;

import com.navinfo.dataservice.api.edit.iface.EditApi;

import net.sf.json.JSONObject;

/**
 * 
 * @author wangdongbin
 *
 */
public class EditApiImpl implements EditApi {
	
	/**
	 * 精编作业保存
	 * add by wangdongbin
	 */
	@Override
	public void columnSave(JSONObject dataObj) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command command =
			new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(
					dataObj, new String());
		com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process process =
			new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(
					command);
		process.run();
	}

}
