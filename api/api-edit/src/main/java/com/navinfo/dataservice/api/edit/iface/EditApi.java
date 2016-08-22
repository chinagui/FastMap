package com.navinfo.dataservice.api.edit.iface;

import net.sf.json.JSONObject;

/** 
* @ClassName: EditApi 
* @author Xiao Xiaowen 
* @date 2016年6月8日 下午1:30:21 
* @Description: TODO
*  
*/
public interface EditApi {
	
	public void columnSave(JSONObject dataObj) throws Exception; 
}
