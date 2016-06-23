package com.navinfo.dataservice.engine.meta.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;

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

}
