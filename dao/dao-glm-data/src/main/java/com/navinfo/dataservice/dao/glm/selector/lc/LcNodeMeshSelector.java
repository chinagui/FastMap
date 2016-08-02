package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.lc.LcNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcNodeMeshSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:53:12
 * @version: v1.0
 */
public class LcNodeMeshSelector extends AbstractSelector {

	public LcNodeMeshSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcNodeMesh.class, conn);
	}

}
