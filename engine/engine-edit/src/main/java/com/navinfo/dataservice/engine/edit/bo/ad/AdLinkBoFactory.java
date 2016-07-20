package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkMeshSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;

public class AdLinkBoFactory {
	private volatile static AdLinkBoFactory instance;

	public static AdLinkBoFactory getInstance() {
		if (instance == null) {
			synchronized (OperatorFactory.class) {
				if (instance == null) {
					instance = new AdLinkBoFactory();
				}
			}
		}
		return instance;
	}

	private AdLinkBoFactory() {

	}
	
	public AdLinkBo create(Connection conn, int linkPid, boolean isLock) throws Exception{
		AdLinkBo bo = new AdLinkBo();
		
		AdLinkSelector linkSelector = new AdLinkSelector(conn);
		
		bo.adLink = (AdLink) linkSelector.loadById(linkPid, isLock);
		
		bo.sNode = AdNodeBoFactory.getInstance().create(conn, bo.adLink.getsNodePid(), isLock);
		
		bo.eNode = AdNodeBoFactory.getInstance().create(conn, bo.adLink.geteNodePid(), isLock);
		
		AdLinkMeshSelector meshSelector = new AdLinkMeshSelector(conn);
		
		bo.meshes = meshSelector.loadByLinkPid(linkPid, isLock);
		
		return bo;
	}
}
