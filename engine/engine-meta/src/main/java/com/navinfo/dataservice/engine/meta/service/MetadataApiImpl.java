package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHmCheckSelector;
import com.navinfo.dataservice.engine.meta.engshort.ScEngshortSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConvertSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;

import net.sf.json.JSONObject;

/**
 * @author wangshishuai3966
 * 
 */
@Service("metadataApi")
public class MetadataApiImpl implements MetadataApi {
	
	@Override
	public int queryAdminIdByLocation(double longitude, double latitude)
			throws Exception {
		
		MeshSelector selector = new MeshSelector();
		
		return selector.getAdminIdByLocation(longitude, latitude);
		
	}
	


	@Override
	public void nameImport(String name, double longitude, double latitude,
			String rowkey) throws Exception {
		RdNameImportor nameImportor=new RdNameImportor();
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
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
		
		return null;
	}



}
