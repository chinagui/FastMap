package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcLinkKindSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:51:02
 * @version: v1.0
 */
public class LcLinkKindSelector extends AbstractSelector {

	public LcLinkKindSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcLink.class, conn);
	}

}
