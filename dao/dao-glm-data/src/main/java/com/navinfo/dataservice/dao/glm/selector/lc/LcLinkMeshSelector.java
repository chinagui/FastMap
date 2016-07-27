package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.lc.LcLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcLinkMeshSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:51:41
 * @version: v1.0
 */
public class LcLinkMeshSelector extends AbstractSelector {

	public LcLinkMeshSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcLinkMesh.class, conn);
	}

}
