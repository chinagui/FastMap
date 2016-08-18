package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.character.TyCharacterFjtHmCheckSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;

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



	@Override
	public Map<String, String> getChainMap(Connection conn) throws Exception {
		ChainSelector chainSelector = new ChainSelector();
		return chainSelector.getChainMap();
	}



	@Override
	public Map<String, String> getKindCodeMap(Connection conn) throws Exception {
		KindCodeSelector kindCodeSelector = new KindCodeSelector(conn);
		return kindCodeSelector.getKindCodeMap();
	}



	@Override
	public Map<String, String> getAdminMap(Connection conn) throws Exception {
		ChainSelector chainSelector = new ChainSelector(conn);
		return chainSelector.getChainMap();
	}



	@Override
	public Map<String, String> getCharacterMap(Connection conn) throws Exception {
		TyCharacterFjtHmCheckSelector tyCharacterFjtHmCheckSelector = new TyCharacterFjtHmCheckSelector(conn);
		return tyCharacterFjtHmCheckSelector.getCharacterMap();
	}




	

}
