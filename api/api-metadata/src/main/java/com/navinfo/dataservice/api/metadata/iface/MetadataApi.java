package com.navinfo.dataservice.api.metadata.iface;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.model.Mesh4Partition;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.api.metadata.model.ScPointSpecKindcodeNewObj;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author wangshishuai3966
 *
 */
public interface MetadataApi {
	/**
	 * SELECT ADMINAREACODE, AREACODE FROM SC_POINT_ADMINAREA
	 * @return Map<String, List<String>> :key,AREACODE电话区号;value,ADMINAREACODE列表，对应的行政区划号列表
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointAdminareaContactMap() throws Exception;
	/**
	 * select pid,name from sc_point_nomingan_list
	 * @return List<String>: pid|name 所拼字符串列表
	 * @throws Exception
	 */
	public List<String> scPointNominganListPidNameList() throws Exception;
	/**
	 * select pid,name from sc_point_mingan_list
	 * @return List<String>: pid|name 所拼字符串列表
	 * @throws Exception
	 */
	public List<String> scPointMinganListPidNameList() throws Exception;
	/**
	 * select sensitive_word,sensitive_word2,kind_code,admincode,type from SC_SENSITIVE_WORDS
	 * @return Map<Integer, List<ScSensitiveWordsObj>>:key，type;value:ScSensitiveWordsObj列表
	 * @throws Exception
	 * type:1-POI 地址,2-POI 名称及其他
	 */
	public Map<Integer, List<ScSensitiveWordsObj>> scSensitiveWordsMap(int type) throws Exception;
	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=8
	 * @return 
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointKindNewChainKind8Map() throws Exception;
	/**
	 * select poikind,chain from SC_POINT_BRAND_FOODTYPE
	 * @return Map<String, List<String>> key:chain value:poikind列表
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointBrandFoodtypeChainKindMap() throws Exception;
	/**
	 * SELECT CHAIN_CODE FROM SC_POINT_CHAIN_CODE WHERE TYPE = 1
	 * @return
	 * @throws Exception
	 */
	public List<String> scPointChainCodeList() throws Exception;

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
	 * 转拼音，不转数字英文字母
	 * @param word
	 * @return
	 * @throws Exception
	 */
	public String pyConvertHz(String word) throws Exception;
	
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
	
	public Map<String, String> scPointNameckTypeHM6() throws Exception;
	
	public Map<String, String> scPointNameckTypeD1_2_3_4_8_11() throws Exception;
	
	public Map<String, String> scPointNameckTypeD4_10() throws Exception;
	/**
	 * SELECT ADMIN_CODE FROM SC_POINT_DEEP_PLANAREA
	 * @return List<String> ADMIN_CODE的列表
	 * @throws Exception
	 */
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
	 * 返回SC_POINT_NAMECK中“TYPE”=9的PRE_KEY
	 * @return List<String> pre_key列表
	 * @throws Exception
	 */
	public List<String> scPointNameckType9() throws Exception;
	
	public Map<String, List<String>> scPointSpecKindCodeType14() throws Exception;
	
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
	
	public String wordKind(String kindCode,String chain) throws Exception;

	/**
	 * 返回“TY_CHARACTER_EGALCHAR_EXT”表中数据。
	 * @return Map<String, List<String>> key:EXTENTION_TYPE value:CHARACTER字段列表
	 * @throws Exception
	 */
	public Map<String, List<String>> tyCharacterEgalcharExtGetExtentionTypeMap() throws Exception;

	/**
	 * 1.“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中，“ENG_H_U”、“ENG_H_L”、“DIGIT_H”、
	 * “SYMBOL_H”类型对应的“CHARACTER”字段的内容;
	 * 2.“TY_CHARACTER_EGALCHAR_EXT”表，和 “EXTENTION_TYPE ”字段里“SYMBOL_F”类型，
	 * 		2.1在全半角对照关系表中（TY_CHARACTER_FULL2HALF表）FULL_WIDTH字段一致，
	 * 找到FULL_WIDTH字段对应的半角“HALF_WIDTH”,且“HALF_WIDTH”字段非空
	 * 		2.2.如果“HALF_WIDTH”字段对应的半角字符为空，则FULL_WIDTH字段对应的全角字符也是拼音的合法字符
	 * @return List<String> 返回合法的所有半角字符列表
	 */
	public List<String> halfCharList() throws Exception;
	
	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<String, JSONObject> key:ft value:对应其它
	 * @throws Exception
	 */
	public Map<String, JSONObject> tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap() throws Exception;

	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<String, JSONObject> key:jt value:对应其它
	 * @throws Exception
	 */
	public Map<String, JSONObject> tyCharacterFjtHzCheckSelectorGetJtExtentionTypeMap()
			throws Exception;

	/**
	 * SELECT DISTINCT POI_KIND, RATING, TOPCITY
	 *   FROM SC_POINT_SPEC_KINDCODE_NEW
	 *    WHERE TYPE = 2
	 * @return Map<String, ScPointSpecKindcodeNewObj> key:poi_kind
	 * @throws Exception
	 */
	public Map<String, ScPointSpecKindcodeNewObj> ScPointSpecKindcodeNewType2() throws Exception;
	
	public JSONObject tyCharacterEgalcharExt() throws Exception;
	
	public Map<String, Map<String, String>> scPointNameckTypeD6() throws Exception;
	
	public List<String> getAddrck(int type,String hmFlag) throws Exception;
	
	public Map<String, Map<String,String>> getAddrAdminMap() throws Exception;
	/**
	 * SELECT DISTINCT KIND_CODE FROM SC_POINT_POICODE_NEW WHERE MHM_DES LIKE '%D%' AND KIND_USE=1
	 * 大陆的kind列表
	 * @return List<String>：KIND_CODE列表
	 * @throws Exception
	 */
	public List<String> getKindCodeDList() throws Exception;

	/**
	 * select poiKind from SC_POINT_FOODTYP
	 * @return List<String> SC_POINT_FOODTYP的poikind列表
	 * @throws Exception
	 */
	public List<String> scPointFoodtypeKindList() throws Exception;

	/**
	 * select poikind,chain,foodType from SC_POINT_BRAND_FOODTYPE
	 * @return Map<String, String> key:poikind|chain value:foodType
	 * @throws Exception
	 */
	public Map<String, String> scPointBrandFoodtypeKindBrandMap() throws Exception;

	/**
	 * SELECT poikind,foodtype FROM SC_POINT_FOODTYPE WHERE MEMO='饮品'
	 * @return  Map<String, String> SC_POINT_FOODTYP的饮品的对应表：key：foodtype value:kind
	 * @throws Exception
	 */
	public Map<String, String> scPointFoodtypeDrinkMap() throws Exception;
	/**
	 * SELECT POIKIND, FOODTYPE, TYPE FROM SC_POINT_FOODTYPE
	 * @return Map<String, Map<String, String>> key:POIKIND value:Map<String, String> (key:FOODTYPE,value:TYPE)
	 * @throws Exception
	 */
	public Map<String, Map<String, String>> scPointFoodtypeFoodTypes() throws Exception;

	/**
	 * select PRE_KEY,CHAIN from SC_POINT_CHAIN_BRAND_KEY where hm_flag='D'
	 * @return Map<String, String> key:PRE_KEY value:CHAIN
	 * @throws Exception
	 */
	public Map<String, String> scPointChainBrandKeyDMap() throws Exception;
	
	/**
	 * 简体字查询
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public JSONObject getTyCharacterFjtHmCheckMap(Connection conn,int type) throws Exception;
	
	/**
	 * cp_meshlist,sc_partition_meshlist查询图幅相关
	 */
	public List<Mesh4Partition> listMeshes4Partition()throws Exception;
	/**
	 * cp_meshlist,sc_partition_meshlist查询图幅相关
	 */
	public List<Mesh4Partition> queryMeshes4PartitionByAdmincodes(Set<Integer> admincodes)throws Exception;
	
	/**
	 * 根据错别字获取行政区划管理表：SC_POINT_ ADMINAREA记录
	 * @param name
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public List<String> searchByErrorName(String name) throws Exception;
    public List<String> queryAdRack(int type) throws Exception;

}