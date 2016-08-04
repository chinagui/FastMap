package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.model.lu.LuFeature;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class LuFeatureSelector extends AbstractSelector {

	public LuFeatureSelector(Connection conn) throws Exception {
		super(LuFeature.class, conn);
	}

}
