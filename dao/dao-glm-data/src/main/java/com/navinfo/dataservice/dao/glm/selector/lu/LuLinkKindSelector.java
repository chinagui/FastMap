package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class LuLinkKindSelector extends AbstractSelector {
	
	public LuLinkKindSelector(Connection conn) throws Exception{
		super(LuLinkKind.class, conn);
	}

}
