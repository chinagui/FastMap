package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.api.metadata.model.ScPointSpecKindcodeNewObj;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.area.ScPointDeepPlanarea;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterEgalcharExt;
import com.navinfo.dataservice.engine.meta.character.TyCharacterEgalcharExtCheckSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHmCheckSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHzCheckSelector;
import com.navinfo.dataservice.engine.meta.engshort.ScEngshortSelector;
import com.navinfo.dataservice.engine.meta.kind.KindSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConvertSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.scEngshortList.ScEngshortList;
import com.navinfo.dataservice.engine.meta.scPointAddrAdmin.ScPointAddrAdmin;
import com.navinfo.dataservice.engine.meta.scPointAddrck.ScPointAddrck;
import com.navinfo.dataservice.engine.meta.scPointAdminarea.ScPointAdminarea;
import com.navinfo.dataservice.engine.meta.scPointBrandFoodtype.ScPointBrandFoodtype;
import com.navinfo.dataservice.engine.meta.scPointChainBrandKey.ScPointChainBrandKey;
import com.navinfo.dataservice.engine.meta.scPointChainCode.ScPointChainCode;
import com.navinfo.dataservice.engine.meta.scPointEngKeyWords.ScPointEngKeyWords;
import com.navinfo.dataservice.engine.meta.scPointFoodtype.ScPointFoodtype;
import com.navinfo.dataservice.engine.meta.scPointKindNew.ScPointKindNew;
import com.navinfo.dataservice.engine.meta.scPointMinganList.ScPointMinganList;
import com.navinfo.dataservice.engine.meta.scPointNameck.ScPointNameck;
import com.navinfo.dataservice.engine.meta.scPointNominganList.ScPointNominganList;
import com.navinfo.dataservice.engine.meta.scPointSpecKindcode.ScPointSpecKindcode;
import com.navinfo.dataservice.engine.meta.scSensitiveWords.ScSensitiveWords;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcSelector;
import com.navinfo.dataservice.engine.meta.translate.ConvertUtil;
import com.navinfo.dataservice.engine.meta.translate.EngConverterHelper;
import com.navinfo.dataservice.engine.meta.wordKind.WordKind;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author wangshishuai3966
 */
@Service("metadataApi")
public class MetadataApiImpl implements MetadataApi {
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
	public void nameImport(String name, double longitude, double latitude, String rowkey) throws Exception {
		RdNameImportor nameImportor = new RdNameImportor();
		nameImportor.importName(name, longitude, latitude, rowkey);

	}

	public JSONObject getChainMap(Connection conn) throws Exception {
		ChainSelector chainSelector = new ChainSelector(conn);
		return chainSelector.getChainMap();
	}

	public JSONObject getKindCodeMap(Connection conn) throws Exception {
		KindCodeSelector kindCodeSelector = new KindCodeSelector(conn);
		return kindCodeSelector.getKindCodeMap();
	}

	public JSONObject getAdminMap(Connection conn) throws Exception {
		ScPointAdminArea areaSelector = new ScPointAdminArea(conn);
		return areaSelector.getAdminMap();
	}

	@Override
	public JSONObject getTyCharacterFjtHmCheckMap(Connection conn) throws Exception {
		TyCharacterFjtHmCheckSelector tyCharacterFjtHmCheckSelector = new TyCharacterFjtHmCheckSelector(conn);
		return tyCharacterFjtHmCheckSelector.getCharacterMap();
	}

	public JSONObject getNavicovpyMap(Connection conn) throws Exception {
		PinyinConvertSelector pinyinConvertSelector = new PinyinConvertSelector(conn);
		return pinyinConvertSelector.getNavicovpyMap();
	}

	public JSONObject getEngshortMap(Connection conn) throws Exception {
		ScEngshortSelector scEngshortSelector = new ScEngshortSelector(conn);
		return scEngshortSelector.getEngShortMap();
	}

	public JSONObject getKindMap(Connection conn) throws Exception {
		KindSelector selector = new KindSelector(conn);
		return selector.getKinkMap();
	}

	@Override
	public String[] pyConvert(String word) throws Exception {
		PinyinConverter py = new PinyinConverter();

		String[] result = py.convert(word);

		return result;
	}

	@Override
	public JSONObject getMetadataMap() throws Exception {
		JSONObject result = new JSONObject();
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();

			result.put("chain", getChainMap(conn));
			result.put("kindCode", getKindCodeMap(conn));
			result.put("admin", getAdminMap(conn));
			result.put("character", getTyCharacterFjtHmCheckMap(conn));
			result.put("navicovpy", getNavicovpyMap(conn));
			result.put("engshort", getEngshortMap(conn));
			result.put("kind", getKindMap(conn));
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
	public String convertEng(String word) throws Exception {
		
		 EngConverterHelper converterHelper = new EngConverterHelper();
         String result = converterHelper.chiToEng(word);
		return result;
	}
	
	@Override
	public Map<String, String> scPointSpecKindCodeType8() throws Exception {
		// TODO Auto-generated method stub
		return ScPointSpecKindcode.getInstance().scPointSpecKindCodeType8();
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
		 return ConvertUtil.convFull2Half(word);
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
	public Map<String, Map<String,String>> getAddrAdminMap() throws Exception {
		return ScPointAddrAdmin.getInstance().scEngshortListMap();
	}
	
	@Override
	public Map<String, String> scPointNameckTypeHM6() throws Exception {
		return ScPointNameck.getInstance().scPointNameckTypeHM6();
	}
	
	@Override
	public List<Map<String, Object>> searchByErrorName(String name) throws Exception {
		return ScPointAdminarea.getInstance().searchByErrorName(name);
	}

}
