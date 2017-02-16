package com.navinfo.dataservice.api.edit.iface;

import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName: EditApi 
* @author Xiao Xiaowen 
* @date 2016年6月8日 下午1:30:21 
* @Description: TODO
*  
*/
public interface EditApi {
	public long applyPid(String tableName,int count)throws Exception;
	/**
	 * 编辑接口（包含要素的新增、修改、删除、移动、修行、打断、父子关系）
	 * @param dataObj
	 * @return
	 * @throws Exception
	 */
	public JSONObject run(JSONObject dataObj) throws Exception; 
	
	/**
	 * 采集端批处理
	 * @param dataObj
	 * @throws Exception
	 */
	public void runBatch(JSONObject dataObj) throws Exception;
	
	public List<Integer> getGridIdListBySubtaskIdFromLog(Integer dbId,Integer subtaskId);
}
