package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcFaceTopoSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:49:29
 * @version: v1.0
 */
public class LcFaceTopoSelector extends AbstractSelector {

	public LcFaceTopoSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcFaceTopo.class, conn);
	}

}
