package com.navinfo.dataservice.engine.meta.service;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.Mesh4Partition;
import com.navinfo.dataservice.api.metadata.model.MetadataMap;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.api.metadata.model.ScPointSpecKindcodeNewObj;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.area.ScPointDeepPlanarea;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterEgalcharExt;
import com.navinfo.dataservice.engine.meta.character.TyCharacterEgalcharExtCheckSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHmCheckSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHzCheckSelector;
import com.navinfo.dataservice.engine.meta.ciParaKindword.CiParaKindKeyword;
import com.navinfo.dataservice.engine.meta.engshort.ScEngshortSelector;
import com.navinfo.dataservice.engine.meta.kind.KindSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.level.LevelSelector;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConvertSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.scEngshortList.ScEngshortList;
import com.navinfo.dataservice.engine.meta.scFmControl.ScFmControl;
import com.navinfo.dataservice.engine.meta.scPartitionMeshlist.ScPartitionMeshlistSelector;
import com.navinfo.dataservice.engine.meta.scPointAddrAdmin.ScPointAddrAdmin;
import com.navinfo.dataservice.engine.meta.scPointAddrck.ScPointAddrck;
import com.navinfo.dataservice.engine.meta.scPointAdminarea.ScPointAdminarea;
import com.navinfo.dataservice.engine.meta.scPointBrandFoodtype.ScPointBrandFoodtype;
import com.navinfo.dataservice.engine.meta.scPointChainBrandKey.ScPointChainBrandKey;
import com.navinfo.dataservice.engine.meta.scPointChainCode.ScPointChainCode;
import com.navinfo.dataservice.engine.meta.scPointCode2Level.ScPointCode2Level;
import com.navinfo.dataservice.engine.meta.scPointEngKeyWords.ScPointEngKeyWords;
import com.navinfo.dataservice.engine.meta.scPointFocus.ScPointFocus;
import com.navinfo.dataservice.engine.meta.scPointFoodtype.ScPointFoodtype;
import com.navinfo.dataservice.engine.meta.scPointKindNew.ScPointKindNew;
import com.navinfo.dataservice.engine.meta.scPointKindRule.ScPointKindRule;
import com.navinfo.dataservice.engine.meta.scPointMinganList.ScPointMinganList;
import com.navinfo.dataservice.engine.meta.scPointNameck.ScPointNameck;
import com.navinfo.dataservice.engine.meta.scPointNominganList.ScPointNominganList;
import com.navinfo.dataservice.engine.meta.scPointPoiCodeNew.ScPointPoiCodeNew;
import com.navinfo.dataservice.engine.meta.scPointSpecKindcode.ScPointSpecKindcode;
import com.navinfo.dataservice.engine.meta.scSensitiveWords.ScSensitiveWords;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcSelector;
import com.navinfo.dataservice.engine.meta.translates.ConvertUtil;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;
import com.navinfo.dataservice.engine.meta.truck.TruckSelector;
import com.navinfo.dataservice.engine.meta.wordKind.WordKind;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangshishuai3966
 */
@Service("metadataApi")
public class MetadataApiImpl implements MetadataApi {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	/**
	 * 多源导入时，批level
	 * @param jsonObj
	 * @return
	 * @throws Exception
	 */
	@Override
	public String getLevelForMulti(JSONObject jsonObj) throws Exception{
		Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            LevelSelector selector = new LevelSelector(conn);
			String res = selector.getLevelForMulti(jsonObj);
            return res;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
	}
	/**
	 * 获取众包truck
	 * @param jsonObj
	 * @return
	 * @throws Exception
	 */
	@Override
	public int getCrowdTruck(String kindCode) throws Exception{
		Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            TruckSelector selector = new TruckSelector(conn);
			int truck = selector.getCrowdTruck(kindCode);
            return truck;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
	}
	/**
	 * SELECT FOODTYPE,FOODTYPENAME FROM SC_POINT_FOODTYPE
	 * @return  Map<String, String> key：foodtype value:FOODTYPENAME
	 * @throws Exception
	 */
	public Map<String, String> getFoodtypeNameMap() throws Exception{
		return ScPointFoodtype.getInstance().getFoodtypeNameMap();
	}
	/**
	 * SELECT DISTINCT CHAIN_CODE,CHAIN_NAME FROM SC_POINT_CHAIN_CODE
	 * @return	Map<String,String> key:CHAIN_CODE,value:CHAIN_NAME
	 * @throws Exception
	 */
	public Map<String,String> getChainNameMap() throws Exception{
		return ScPointChainCode.getInstance().getChainNameMap();
	}
	/**
	 * SELECT KIND_ID, KEYWORD FROM CI_PARA_KIND_KEYWORD
	 * @return Map<String, List<String>> key:kind_id,value:keyword
	 * @throws Exception
	 */
	@Override
	public Map<String, List<String>> ciParaKindKeywordMap() throws Exception{
		return CiParaKindKeyword.getInstance().ciParaKindKeywordMap();
	}
	/**
	 * SELECT ADMINAREACODE, AREACODE FROM SC_POINT_ADMINAREA
	 * @return Map<String, List<String>> :key,AREACODE电话区号;value,ADMINAREACODE列表，对应的行政区划号列表
	 * @throws Exception
	 */
	@Override
	public Map<String, List<String>> scPointAdminareaContactMap() throws Exception{
		return ScPointAdminarea.getInstance().scPointAdminareaContactMap();
	}
	/**
	 * 查询省市区名称
	 * @return Map<String, List<String>> :key,省市区;value,对应的名称列表
	 * @throws Exception
	 */
	@Override
	public Map<String, List<String>> scPointAdminareaDataMap() throws Exception{
		return ScPointAdminarea.getInstance().scPointAdminareaDataMap();
	}
	/**
	 * 查询省市区名称
	 * @return Map<String, Map<String,String>> :key,AdminId;value,对应的名称列表
	 * @throws Exception
	 */
	@Override
	public Map<String, Map<String,String>> scPointAdminareaByAdminId() throws Exception{
		return ScPointAdminarea.getInstance().scPointAdminareaByAdminId();
	}
	/**
	 * select pid,name from sc_point_nomingan_list
	 * @return List<String>: pid|name 所拼字符串列表
	 * @throws Exception
	 */
	@Override
	public List<String> scPointNominganListPidNameList() throws Exception{
		return ScPointNominganList.getInstance().scPointNominganListPidNameList();
	}
	/**
	 * select pid,name from sc_point_mingan_list
	 * @return List<String>: pid|name 所拼字符串列表
	 * @throws Exception
	 */
	@Override
	public List<String> scPointMinganListPidNameList() throws Exception{
		return ScPointMinganList.getInstance().scPointMinganListPidNameList();
	}
	/**
	 * select sensitive_word,sensitive_word2,kind_code,admincode,type from SC_SENSITIVE_WORDS
	 * @return Map<Integer, List<ScSensitiveWordsObj>>:key，type;value:ScSensitiveWordsObj列表
	 * @throws Exception
	 */
	@Override
	public Map<Integer, List<ScSensitiveWordsObj>> scSensitiveWordsMap() throws Exception{
		return ScSensitiveWords.getInstance().scSensitiveWordsMap();
	}
	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=8
	 * @return 
	 * @throws Exception
	 */
	@Override
	public Map<String, List<String>> scPointKindNewChainKind8Map() throws Exception{
		return ScPointKindNew.getInstance().scPointKindNewChainKind8Map();
	}
	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=5
	 * @return 
	 * @throws Exception
	 */
	@Override
	public List<Map<String, String>> scPointKindNewChainKind5Map() throws Exception{
		return ScPointKindNew.getInstance().scPointKindNewChainKind5Map();
	}
	/**
	 * SELECT R_KIND, POIKIND FROM SC_POINT_KIND_NEW WHERE TYPE=6
	 * @return 
	 * @throws Exception
	 */
	@Override
	public List<Map<String, String>> scPointKindNewChainKind6Map() throws Exception{
		return ScPointKindNew.getInstance().scPointKindNewChainKind6Map();
	}
	/**
	 * SELECT * FROM SC_POINT_KIND_NEW WHERE TYPE=5
	 * @return 
	 * @throws Exception
	 */
	@Override
	public List<Map<String, String>> scPointKindNew5List() throws Exception{
		return ScPointKindNew.getInstance().scPointKindNew5List();
	}
	/**
	 * select poikind,chain from SC_POINT_BRAND_FOODTYPE
	 * @return Map<String, List<String>> key:chain value:poikind列表
	 * @throws Exception
	 */
	@Override
	public Map<String, List<String>> scPointBrandFoodtypeChainKindMap() throws Exception{
		return ScPointBrandFoodtype.getInstance().scPointBrandFoodtypeChainKindMap();
	}
	/**
	 * SELECT CHAIN_CODE FROM SC_POINT_CHAIN_CODE WHERE TYPE = 1
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String> scPointChainCodeList() throws Exception{
		return ScPointChainCode.getInstance().scPointChainCodeList();
	}
	/**
	 * select PRE_KEY,CHAIN from SC_POINT_CHAIN_BRAND_KEY where hm_flag='D'
	 * @return Map<String, String> key:PRE_KEY value:CHAIN
	 * @throws Exception
	 */
	@Override
	public Map<String, String> scPointChainBrandKeyDMap() throws Exception{
		return ScPointChainBrandKey.getInstance().scPointChainBrandKeyDMap();
	}
	/**
	 * SELECT poikind,foodtype FROM SC_POINT_FOODTYPE WHERE MEMO='饮品'
	 * @return  Map<String, String> SC_POINT_FOODTYP的饮品的对应表：key：foodtype value:kind
	 * @throws Exception
	 */
	@Override
	public Map<String, String> scPointFoodtypeDrinkMap() throws Exception{
		return ScPointFoodtype.getInstance().scPointFoodtypeDrinkMap();
	}
	/**
	 * SELECT POIKIND, FOODTYPE, TYPE FROM SC_POINT_FOODTYPE
	 * @return Map<String, Map<String, String>> key:POIKIND value:Map<String, String> (key:FOODTYPE,value:TYPE)
	 * @throws Exception
	 */
	@Override
	public Map<String, Map<String, String>> scPointFoodtypeFoodTypes() throws Exception{
		return ScPointFoodtype.getInstance().scPointFoodtypeFoodTypes();
	}
	/**
	 * select poikind,chain,foodType from SC_POINT_BRAND_FOODTYPE
	 * @return Map<String, String> key:poikind|chain value:foodType
	 * @throws Exception
	 */
	@Override
	public Map<String, String> scPointBrandFoodtypeKindBrandMap() throws Exception{
		return ScPointBrandFoodtype.getInstance().scPointBrandFoodtypeKindBrandMap();
	}
	/**
	 * select poiKind from SC_POINT_FOODTYP
	 * @return List<String> SC_POINT_FOODTYP的poikind列表
	 * @throws Exception
	 */
	@Override
	public List<String> scPointFoodtypeKindList() throws Exception{
		return ScPointFoodtype.getInstance().scPointFoodtypeKindList();
	}
	
	/**
	 * SELECT DISTINCT KIND_CODE FROM SC_POINT_POICODE_NEW WHERE MHM_DES LIKE '%D%' AND KIND_USE=1
	 * 大陆的kind列表
	 * @return List<String>：KIND_CODE列表
	 * @throws Exception
	 */
	@Override
	public List<String> getKindCodeDList() throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			KindCodeSelector selector=new KindCodeSelector(conn);
			return selector.getKindCodeDList();
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	@Override
	public int queryAdminIdByLocation(double longitude, double latitude) throws Exception {

		MeshSelector selector = new MeshSelector();

		return selector.getAdminIdByLocation(longitude, latitude);

	}

	@Override
	public void nameImport(String name, JSONObject gLocation, String rowkey, String sourceType) throws Exception {
		RdNameImportor nameImportor = new RdNameImportor();
		nameImportor.importName(name, gLocation, rowkey, sourceType);

	}

	private JSONObject getChainMap(Connection conn) throws Exception {
		ChainSelector chainSelector = new ChainSelector(conn);
		return chainSelector.getChainMap();
	}

	private JSONObject getKindCodeMap(Connection conn) throws Exception {
		KindCodeSelector kindCodeSelector = new KindCodeSelector(conn);
		return kindCodeSelector.getKindCodeMap();
	}

	

	@Override
	public JSONObject getTyCharacterFjtHmCheckMap(Connection conn,int type) throws Exception {
		TyCharacterFjtHmCheckSelector tyCharacterFjtHmCheckSelector = new TyCharacterFjtHmCheckSelector(conn);
		return tyCharacterFjtHmCheckSelector.getCharacterMap(type);
	}

	private JSONObject getNavicovpyMap(Connection conn) throws Exception {
		PinyinConvertSelector pinyinConvertSelector = new PinyinConvertSelector(conn);
		return pinyinConvertSelector.getNavicovpyMap();
	}

	private JSONObject getEngshortMap(Connection conn) throws Exception {
		ScEngshortSelector scEngshortSelector = new ScEngshortSelector(conn);
		return scEngshortSelector.getEngShortMap();
	}

	private JSONObject getKindMap(Connection conn) throws Exception {
		KindSelector selector = new KindSelector(conn);
		return selector.getKinkMap();
	}

	@Override
	public String[] pyConvert(String word) throws Exception {
		PinyinConverter py = new PinyinConverter();
		//String[] result = py.convert(word);

        String[] result = py.pyVoiceConvert(word, null, null, null);
        CollectionUtils.reverseArray(result);
		return result;
	}
	
	@Override
	public MetadataMap getMetadataMap() throws Exception {
		MetadataMap result = new MetadataMap();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			result.setChain((Map<String,String>) getChainMap(conn));
			result.setKindCode((Map<String,String>) getKindCodeMap(conn));
			result.setAdmin((Map<String,String>) getAdminMap(conn));
			result.setCharacter((Map<String,String>) getTyCharacterFjtHmCheckMap(conn,0));
			result.setKind((Map<String,String>) getKindMap(conn));
			
			result.setEngshort((Map<String,String>) getEngshortMap(conn));
			result.setNavicovpy((Map<String,List<String>>) getNavicovpyMap(conn));
			result.setNameUnifyShort(scPointNameckTypeD1_2_3_4_8_11());
			result.setChishort(scPointNameckTypeD4_10());
			result.setAliasName(scPointNameckTypeD4());

			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}
	
	@Override
	public JSONObject getMetadataMap2() throws Exception {
		JSONObject result = new JSONObject();
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();

			result.put("engshort", getEngshortMap(conn));
			result.put("navicovpy", getNavicovpyMap(conn));
			result.put("nameUnifyShort", scPointNameckTypeD1_2_3_4_8_11());
			result.put("chishort", scPointNameckTypeD4_10());
			result.put("aliasName", scPointNameckTypeD4());

			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}

	@Override
	public JSONArray queryTmcPoint(int x, int y, int z, int gap) throws Exception {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();

			TmcSelector selector = new TmcSelector(conn);

			List<SearchSnapshot> list = selector.queryTmcPoint(x, y, z, gap);

			if (CollectionUtils.isNotEmpty(list)) {
				JSONArray array = JSONArray.fromObject(list, JsonUtils.getJsonConfig());

				return array;
			} else {
				return new JSONArray();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	@Override
	public JSONArray queryTmcLine(int x, int y, int z, int gap) throws Exception {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();

			TmcSelector selector = new TmcSelector(conn);

			List<SearchSnapshot> list = selector.queryTmcLine(x, y, z, gap);

			if (CollectionUtils.isNotEmpty(list)) {
				JSONArray array = JSONArray.fromObject(list, JsonUtils.getJsonConfig());

				return array;
			} else {
				return new JSONArray();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	@Override
	public JSONObject getCharacterMap() throws Exception {

		TyCharacterEgalcharExtCheckSelector tyCharacterSelector = new TyCharacterEgalcharExtCheckSelector();

		return tyCharacterSelector.getCharacterMap();

	}
	
	
	@Override
	public JSONObject searchByAdminCode(String admincode) throws Exception {
		
		ScPointAdminArea scPoint = new ScPointAdminArea();
		return scPoint.searchByAdminCode(admincode);
	}
	
	@Override
	public JSONObject getProvinceAndCityByAdminCode(String admincode) throws Exception {
		
		ScPointAdminArea scPoint = new ScPointAdminArea();
		return scPoint.getProvinceAndCityByAdminCode(admincode);
	}
	
	@Override
	public String searchKindName(String kindcode) throws Exception {
		KindCodeSelector kind = new KindCodeSelector();
		return kind.searchKindName(kindcode);
	}
	/**
	 * 需要按照顺序进行key值替换名称，所以用list，按照key长度存放。
	 * 获取sc_Point_Nameck元数据库表中type=1的大陆的记录列表
	 */
	@Override
	public List<ScPointNameckObj> scPointNameckTypeD1() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD1();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeD10() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD10();
	}
	
	/**
	 * 返回SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’的PRE_KEY, RESULT_KEY
	 * @return Map<String,String> key:PRE_KEY,value:RESULT_KEY
	 * @throws Exception
	 */
	@Override
	public Map<String, String> scPointNameckTypeD4() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD4();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeD3() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD3();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeD5() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD5();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeD7() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD7();
	}
	@Override
	public Map<String, String> scPointNameckTypeD1_2_3_4_8_11() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD1_2_3_4_8_11();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeD4_10() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckTypeD4_10();
	}
	/**
	 * SELECT ADMIN_CODE FROM SC_POINT_DEEP_PLANAREA
	 * @return List<String> ADMIN_CODE的列表
	 * @throws Exception
	 */
	@Override
	public List<String> getDeepAdminCodeList() throws Exception {
		ScPointDeepPlanarea deepPlanarea = new ScPointDeepPlanarea();
		return deepPlanarea.getDeepAdminCodeList();
	}
	
	@Override
	public String convertEng(String word, String admin) {
        EnglishConvert convert = new EnglishConvert();
        convert.setAdminCode(admin);

        return convert.convert(word);
	}
	
	@Override
	public Map<String, String> scPointSpecKindCodeType8() throws Exception {
		// TODO Auto-generated method stub
		return ScPointSpecKindcode.getInstance().scPointSpecKindCodeType8();
	}
	
	@Override
	public Map<String, String> scPointSpecKindCodeType15() throws Exception {
		// TODO Auto-generated method stub
		return ScPointSpecKindcode.getInstance().scPointSpecKindCodeType15();
	}

	@Override
	public Map<String, List<String>> scPointSpecKindCodeType7() throws Exception {
		// TODO Auto-generated method stub
		return ScPointSpecKindcode.getInstance().scPointSpecKindCodeType7();
	}
	
	@Override
	public Map<String, List<String>>  scPointSpecKindCodeType14() throws Exception {
		// TODO Auto-generated method stub
		return ScPointSpecKindcode.getInstance().scPointSpecKindCodeType14();
	}

	/**
	 * 重要分类判断方法
	 * 传入poi的kindCode和chain，返回boolean，是否为重要分类
	 * @param kindCode
	 * @param chain
	 * @return true重要分类，false 非重要分类
	 * @throws Exception
	 */
	@Override
	public boolean judgeScPointKind(String kindCode, String chain) throws Exception {
		return ScPointSpecKindcode.getInstance().judgeScPointKind(kindCode, chain);
	}
	
	/**
	 * 返回TYPE=1时地址关键字翻译对照MAP
	 */
	@Override
	public Map<String, String> scPointEngKeyWordsType1() throws Exception {
		// TODO Auto-generated method stub
		return ScPointEngKeyWords.getInstance().scPointEngKeyWordsType1();
	}

	@Override
	public Map<String, String> scEngshortListMap() throws Exception {
		return ScEngshortList.getInstance().scEngshortListMap();
	}

	@Override
	public String convFull2Half(String word) throws Exception {
		 return ConvertUtil.convertFull2Half(word);
	}
	/**
	 * 返回“TY_CHARACTER_EGALCHAR_EXT”表中数据。
	 * @return Map<String, List<String>> key:EXTENTION_TYPE value:CHARACTER字段列表
	 * @throws Exception
	 */
	@Override
	public Map<String, List<String>> tyCharacterEgalcharExtGetExtentionTypeMap()
			throws Exception {
		return TyCharacterEgalcharExt.getInstance().getExtentionTypeMap();
	}
	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<String, JSONObject> key:ft value:对应其它
	 * @throws Exception
	 */
	@Override
	public Map<String, JSONObject> tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap()
			throws Exception {
		return TyCharacterFjtHzCheckSelector.getInstance().getFtExtentionTypeMap();
	}
	
	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<Integer,Map<String, String>> key:convert value:Map<String, String> ft:jt
	 * @throws Exception
	 */
	@Override
	public Map<Integer,Map<String, String>> tyCharacterFjtHzConvertFtMap()
			throws Exception {
		return TyCharacterFjtHzCheckSelector.getInstance().tyCharacterFjtHzConvertFtMap();
	}
	
	
	/**
	 * 返回“TY_CHARACTER_FJT_HZ”表中数据。
	 * @return Map<String, JSONObject> key:jt value:对应其它
	 * @throws Exception
	 */
	@Override
	public Map<String, JSONObject> tyCharacterFjtHzCheckSelectorGetJtExtentionTypeMap()
			throws Exception {
		return TyCharacterFjtHzCheckSelector.getInstance().getJtExtentionTypeMap();
	}

	@Override
	public String wordKind(String kindCode,String chain) throws Exception {
		return WordKind.getInstance().getWordKind(kindCode, chain);
	}
	/**
	 * 1.“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中，“ENG_H_U”、“ENG_H_L”、“DIGIT_H”、
	 * “SYMBOL_H”类型对应的“CHARACTER”字段的内容;
	 * 2.“TY_CHARACTER_EGALCHAR_EXT”表，和 “EXTENTION_TYPE ”字段里“SYMBOL_F”类型，
	 * 		2.1在全半角对照关系表中（TY_CHARACTER_FULL2HALF表）FULL_WIDTH字段一致，
	 * 找到FULL_WIDTH字段对应的半角“HALF_WIDTH”,且“HALF_WIDTH”字段非空
	 * 		2.2.如果“HALF_WIDTH”字段对应的半角字符为空，则FULL_WIDTH字段对应的全角字符也是拼音的合法字符
	 * @return List<String> 返回合法的所有半角字符列表
	 */
	@Override
	public List<String> halfCharList() throws Exception {
		// TODO Auto-generated method stub
		return TyCharacterEgalcharExt.getInstance().getHalfCharList();
	}
	/**
	 * SELECT DISTINCT POI_KIND, RATING, TOPCITY
	 *   FROM SC_POINT_SPEC_KINDCODE_NEW
	 *    WHERE TYPE = 2
	 * @return Map<String, ScPointSpecKindcodeNewObj> key:poi_kind
	 * @throws Exception
	 */
	@Override
	public Map<String, ScPointSpecKindcodeNewObj> ScPointSpecKindcodeNewType2()
			throws Exception {
		// TODO Auto-generated method stub
		return ScPointSpecKindcode.getInstance().scPointSpecKindCodeType2();
	}
	
	
	/**
	 * 返回SC_POINT_NAMECK中“TYPE”=9且HM_FLAG<>’HM’的PRE_KEY
	 * @return List<String> pre_key列表
	 * @throws Exception
	 */
	@Override
	public List<String> scPointNameckType9() throws Exception {
		// TODO Auto-generated method stub
		return ScPointNameck.getInstance().scPointNameckType9();
	}

	@Override
	public JSONObject tyCharacterEgalcharExt() throws Exception {
		TyCharacterEgalcharExtCheckSelector tyCharacterSelector = new TyCharacterEgalcharExtCheckSelector();
		return tyCharacterSelector.getCheckMap();
	}

	@Override
	public Map<String, Map<String, String>> scPointNameckTypeD6() throws Exception {
		return ScPointNameck.getInstance().scPointNameckTypeD6();
	}

	@Override
	public List<String> getAddrck(int type, String hmFlag) throws Exception {
		ScPointAddrck addrck = new ScPointAddrck();
		return addrck.getAddrckList(type, hmFlag);
	}
	
	@Override
	public List<String> queryAdRack(int type) throws Exception {
		ScPointAddrck addrck = new ScPointAddrck();
		return addrck.queryAdRack(type);
	}

	@Override
	public Map<String, Map<String,String>> getAddrAdminMap() throws Exception {
		return ScPointAddrAdmin.getInstance().addrAdminMap();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeHM6() throws Exception {
		return ScPointNameck.getInstance().scPointNameckTypeHM6();
	}
	
	@Override
	public List<String> searchByErrorName(String name) throws Exception {
		return ScPointAdminarea.getInstance().searchByErrorName(name);
	}

	/**
	 * cp_meshlist,sc_partition_meshlist查询图幅相关
	 */
	public List<Mesh4Partition> listMeshes4Partition()throws Exception{
		ScPartitionMeshlistSelector scPartitionMeshlist = new ScPartitionMeshlistSelector();
		return scPartitionMeshlist.listMeshes4Partition();
	}
	/**
	 * cp_meshlist,sc_partition_meshlist查询图幅相关
	 */
	public List<Mesh4Partition> queryMeshes4PartitionByAdmincodes(Set<Integer> admincodes)throws Exception{
		ScPartitionMeshlistSelector scPartitionMeshlist = new ScPartitionMeshlistSelector();
		return scPartitionMeshlist.queryMeshes4PartitionByAdmincodes(admincodes);
	}
	
	/**
	 * sc_partition_meshlist查询关闭的图幅
	 */
	public List<Integer> getCloseMeshs(List<Integer> meshs)throws Exception{
		ScPartitionMeshlistSelector scPartitionMeshlist = new ScPartitionMeshlistSelector();
		return scPartitionMeshlist.getCloseMeshs(meshs);
	}
	
	/**
	 * sc_partition_meshlist查询关闭的图幅
	 */
	public List<Integer> getMeshsFromPartition(List<Integer> meshs,int openFlag,int action)throws Exception{
		ScPartitionMeshlistSelector scPartitionMeshlist = new ScPartitionMeshlistSelector();
		return scPartitionMeshlist.getMeshsFromPartition(meshs,openFlag,action);
	}
	
	/**
     * sc_point_poicode_new.KIND_USE= 1
     * @author Han Shaoming
     * @return Map<String, Integer> key:KIND_CODE value:KIND_USE
     * @throws Exception
     */
	@Override
	public Map<String, Integer> searchScPointPoiCodeNew() throws Exception {
		return ScPointPoiCodeNew.getInstance().searchScPointPoiCodeNew();
	}
	
	/**
     * sc_point_poicode_new.KIND_USE= 1
     * @author Han Shaoming
     * @return Map<String,String> key:KIND_CODE,value:KIND_NAME
     * @throws Exception
     */
	@Override
	public Map<String, String> getKindNameByKindCode() throws Exception {
		return ScPointPoiCodeNew.getInstance().getKindNameByKindCode();
	}
	
	/**
     * SC_POINT_FOCUS.TYPE=2
     * @author Han Shaoming
     * @return Map<String, Integer> key:POI_NUM value:TYPE
     * @throws Exception
     */
	@Override
	public Map<String, Integer> searchScPointFocus(String poiNum) throws Exception {
		return ScPointFocus.getInstance().searchScPointFocus(poiNum);
	}
	
	/**
     * SC_FM_CONTROL
     * @author Han Shaoming
     * @return Map<String, Integer> key:KIND_CODE value:PARENT
     * @throws Exception
     */
	@Override
	public Map<String, Integer> searchScFmControl(String kindCode) throws Exception {
		return ScFmControl.getInstance().searchScFmControl(kindCode);
	}
	/**
	 * SELECT POI_KIND,POI_KIND_NAME,TYPE FROM SC_POINT_KIND_RULE WHERE TYPE IN(1,2,3)
	 * @return 
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> scPointKindRule() throws Exception{
		return ScPointKindRule.getInstance().scPointKindRule();
	}
	/**
	 * SELECT POI_KIND,POI_KIND_NAME,TYPE FROM SC_POINT_KIND_RULE WHERE TYPE IN(5)
	 * 
	 * @return Map<String, String> key:POI_KIND;value:POI_KIND_NAME
	 * @throws Exception
	 */
	@Override
	public Map<String, String> scPointKindRule5() throws Exception{
		return ScPointKindRule.getInstance().scPointKindRule5();
	}
	/**
	 * SELECT KIND_CODE,NEW_POI_LEVEL FROM SC_POINT_CODE2LEVEL
	 * 
	 * @returnList Map<String, List<String>> key:KIND_CODE,value:NEW_POI_LEVEL
	 * @throws Exception
	 */
	@Override
	public Map<String, String> scPointCode2Level() throws Exception{
		return ScPointCode2Level.getInstance().scPointCode2Level();
	}
	@Override
	public Map<String, String> scPointCode2LevelOld() throws Exception{
		return ScPointCode2Level.getInstance().scPointCode2LevelOld();
	}
	@Override
	public JSONObject getAdminMap() throws Exception {
		ScPointAdminArea areaSelector = new ScPointAdminArea();
		return areaSelector.getAdminMap();
	}
	private JSONObject getAdminMap(Connection conn) throws Exception {
		ScPointAdminArea areaSelector = new ScPointAdminArea(conn);
		return areaSelector.getAdminMap();
	}

	
	/**
	 * @Title: pyConvert
	 * @Description: 调用cop函数转拼音
	 * @param word   待翻译的词  必填
	 * @param adminId	行政区划号  选填  默认 null
	 * @param isRdName  是否是道路名: "1"/是  ; "0"/否; 默认 null
	 * @return 
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月22日  
	 */
	@Override
	public String pyConvert(String word, String adminId, String isRdName) throws Exception {
		PinyinConverter py = new PinyinConverter();

		String result = py.pyConvert(word, adminId, isRdName);

		return result;
	}
	/**
	 * @Title: voiceConvert
	 * @Description: 调用cop函数转语音
	 * @param word   待翻译的词  必填
	 * @param phonetic 带翻译词的发音  选填  默认 null
	 * @param adminId	行政区划号  选填  默认 null
	 * @param isRdName  是否是道路名: "1"/是  ; "0"/否; 默认 null
	 * @return 
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月22日  
	 */
	@Override
	public String voiceConvert(String word, String phonetic, String adminId, String isRdName) throws Exception {
		PinyinConverter py = new PinyinConverter();

		String result = py.voiceConvert(word, phonetic, adminId, isRdName);

		return result;
	}
	
	/**
	 * @Title: pyVoiceConvert
	 * @Description: 调用cop函数转拼音及语音
	 * @param word   待翻译的词  必填
	 * @param phonetic 带翻译词的发音  选填  默认 null
	 * @param adminId	行政区划号  选填  默认 null
	 * @param isRdName  是否是道路名: "1"/是  ; "0"/否; 默认 null
	 * @return 
	 			result[0] = resultSet.getString("phonetic");
				result[1] = resultSet.getString("voicefile");
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月22日  
	 */
	@Override
	public String[] pyVoiceConvert(String word, String phonetic, String adminId, String isRdName) throws Exception {
		PinyinConverter py = new PinyinConverter();

		String[] result = py.pyVoiceConvert(word, phonetic, adminId, isRdName);

		return result;
	}
	/**
	 * @Title: engConvert
	 * @Description: 调用cop函数转英文
	 * @param word   待翻译的词  必填
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月22日  
	 */
	@Override
	public String engConvert(String word, String adminId) throws Exception {
		PinyinConverter py = new PinyinConverter();

		String result = py.engConvert(word, adminId);

		return result;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.metadata.iface.MetadataApi#getScPointTruckList()
	 */
	@Override
	public List<Map<String, Object>> getScPointTruckList() throws Exception {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT T.KIND,T.CHAIN,T.TYPE,T.TRUCK    ");
			sb.append("   FROM SC_POINT_TRUCK T                 ");
			
			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>> (){
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("kind", rs.getString("KIND"));
						map.put("chain", rs.getString("CHAIN"));
						map.put("type", rs.getString("TYPE"));
						map.put("truck", rs.getString("TRUCK"));
						result.add(map);
					}
					return result;
				}	
	    	};				

	    	return run.query(conn, sb.toString(), rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//获取重要POI的PID
	@Override
	public List<String> queryImportantPid() throws SQLException {
		return ScPointFieldAttentionPoi.getInstance().queryImportantPid();
	}
	
	/**
	 * 根据多源poi置信度范围检索对应pid
	 * @param 范围最小值
	 * @param 范围最大值
	 * @return List<pid>
	 * 
	 * */
	@Override
	public List<Integer> queryReliabilityPid(int minNumber, int maxNumber) throws SQLException {
		return ScQueryReliabilityPid.getInstance().ScQueryReliabilityPid(minNumber, maxNumber);
	}
	@Override
	public Map<String,Integer> queryEditMethTipsCode() throws SQLException {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String sql = " select code,D_EDIT_METH from sc_tips_code t ";
			
			ResultSetHandler<Map<String,Integer>> rsHandler = new ResultSetHandler<Map<String,Integer>> (){
				public Map<String,Integer> handle(ResultSet rs) throws SQLException {
					Map<String,Integer> map = new HashMap<>();
					while(rs.next()){
						map.put(rs.getString(1), rs.getInt(2));
					}
					return map;
				}	
	    	};				

	    	return run.query(conn, sql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	

}
