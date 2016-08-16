package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class LuLinkMeshSelector extends AbstractSelector {
	
	public LuLinkMeshSelector(Connection conn) throws Exception{
		super(LuLinkMesh.class, conn);
	}

}
