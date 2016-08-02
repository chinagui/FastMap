package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class LuFaceNameSelector extends AbstractSelector {

	public LuFaceNameSelector(Connection conn) throws Exception {
		super(LuFaceName.class, conn);
	}

}
