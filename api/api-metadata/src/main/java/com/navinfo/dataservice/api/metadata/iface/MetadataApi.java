package com.navinfo.dataservice.api.metadata.iface;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;

import net.sf.json.JSONArray;
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
	
	public String searchKindName(String kindcode) throws Exception;
	
	public String[] pyConvert(String word) throws Exception;
	
	/**
	 * 根据瓦片渲染TMC_POINT
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryTmcPoint(int x, int y, int z, int gap) throws Exception;
	
	/**
	 * 根据瓦片渲染TMC_LINE
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryTmcLine(int x, int y, int z, int gap) throws Exception;
	
	public JSONObject getCharacterMap() throws Exception;
	
	public JSONObject searchByAdminCode(String admincode) throws Exception;
	/**
	 * 需要按照顺序进行key值替换名称，所以用list，按照key长度存放。
	 * 获取sc_Point_Nameck元数据库表中type=1的大陆的记录列表
	 * @return
	 * @throws Exception
	 */
	public List<ScPointNameckObj> scPointNameckTypeD1() throws Exception;
	
	public Map<String, String> scPointNameckTypeD10() throws Exception;
	
	/**
	 * 返回SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’的PRE_KEY, RESULT_KEY
	 * @return Map<String,String> key:PRE_KEY,value:RESULT_KEY
	 * @throws Exception
	 */
	public Map<String, String> scPointNameckTypeD4() throws Exception;
	
	public Map<String, String> scPointNameckTypeD3() throws Exception;
	
	public Map<String, String> scPointNameckTypeD5() throws Exception;
	
	public Map<String, String> scPointNameckTypeD7() throws Exception;
	
	public List<String> getDeepAdminCodeList() throws Exception;

	/**
	 * 转英文
	 * @param word
	 * @return
	 * @throws Exception
	 */
	public String convertEng(String word) throws Exception;
	
	public Map<String, String> scPointSpecKindCodeType8() throws Exception;
	/**
	 * 重要分类判断方法
	 * 传入poi的kindCode和chain，返回boolean，是否为重要分类
	 * @param kindCode
	 * @param chain
	 * @return true重要分类，false 非重要分类
	 * @throws Exception
	 */
	public boolean judgeScPointKind(String kindCode,String chain) throws Exception;

	public Map<String, String> scPointEngKeyWordsType1() throws Exception;
	
	public Map<String, String> scEngshortListMap() throws Exception;


	public String convFull2Half(String word) throws Exception;
}