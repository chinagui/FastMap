package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.area.ScPointDeepPlanarea;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterEgalcharExtCheckSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHmCheckSelector;
import com.navinfo.dataservice.engine.meta.engshort.ScEngshortSelector;
import com.navinfo.dataservice.engine.meta.kind.KindSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConvertSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.scEngshortList.ScEngshortList;
import com.navinfo.dataservice.engine.meta.scPointEngKeyWords.ScPointEngKeyWords;
import com.navinfo.dataservice.engine.meta.scPointNameck.ScPointNameck;
import com.navinfo.dataservice.engine.meta.scPointSpecKindcode.ScPointSpecKindcode;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcSelector;
import com.navinfo.dataservice.engine.meta.translate.ConvertUtil;
import com.navinfo.dataservice.engine.meta.translate.EngConverterHelper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author wangshishuai3966
 */
@Service("metadataApi")
public class MetadataApiImpl implements MetadataApi {

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

	public JSONObject getCharacterMap(Connection conn) throws Exception {
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
			result.put("character", getCharacterMap(conn));
			result.put("navicovpy", getNavicovpyMap(conn));
			result.put("engshort", getEngshortMap(conn));
			result.put("kind", getKindMap(conn));

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
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();

			TyCharacterEgalcharExtCheckSelector tyCharacterSelector = new TyCharacterEgalcharExtCheckSelector(conn);

			JSONObject characterMap = tyCharacterSelector.getCharacterMap();

			return characterMap;

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}

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
}
