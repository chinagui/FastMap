package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcNode.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:52:29
 * @version: v1.0
 */
public class LcNodeSelector extends AbstractSelector {

	public LcNodeSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcNodeSelector.class, conn);
	}

}
