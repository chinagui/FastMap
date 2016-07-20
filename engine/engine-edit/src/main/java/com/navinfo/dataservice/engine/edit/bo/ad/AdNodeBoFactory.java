package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;

public class AdNodeBoFactory {
	private volatile static AdNodeBoFactory instance;

	public static AdNodeBoFactory getInstance() {
		if (instance == null) {
			synchronized (OperatorFactory.class) {
				if (instance == null) {
					instance = new AdNodeBoFactory();
				}
			}
		}
		return instance;
	}

	private AdNodeBoFactory() {

	}
	
	public AdNodeBo create(Connection conn, int nodePid, boolean isLock) throws Exception{
		AdNodeBo bo = new AdNodeBo();
		
		AdNodeSelector nodeSelector = new AdNodeSelector(conn);
		
		bo.adNode = (AdNode) nodeSelector.loadById(nodePid, isLock);
		
		return bo;
	}
}
