package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class LuNodeMeshSelector extends AbstractSelector{

	public LuNodeMeshSelector(Connection conn) throws Exception{
		super(LuNodeMesh.class, conn);
	}

}
