package com.navinfo.dataservice.api.metadata.iface;

import java.sql.Connection;
import java.util.Map;

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
	
	public Map<String,String> getChainMap(Connection conn) throws Exception;
	
	public Map<String,String> getKindCodeMap(Connection conn) throws Exception;
	
	public Map<String,String> getAdminMap(Connection conn) throws Exception;

	public Map<String,String> getCharacterMap(Connection conn) throws Exception;
}
