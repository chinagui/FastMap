package com.navinfo.dataservice.api.metadata.iface;

import net.sf.json.JSONObject;

/**
 * @author wangshishuai3966
 *
 */
public interface MetadataApi {

	public int queryAdminIdByLocation(double longitude, double latitude)
			throws Exception;
	
	
	/**
	 * @Description:名称导入，将名称写入元数据库
	 * @param name
	 * @param longitude
	 * @param latitude
	 * @param rowkey
	 * @author: y
	 * @time:2016-6-28 下午2:49:30
	 */
	
	public void nameImport(String name,double longitude, double latitude,String rowkey)throws Exception ;
	
	public JSONObject getMetadataMap() throws Exception;
	
	public String[] pyConvert(String word) throws Exception;
}
