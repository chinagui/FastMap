package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.lc.LcFeature;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcFeatureSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:53:46
 * @version: v1.0
 */
public class LcFeatureSelector extends AbstractSelector {

	public LcFeatureSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcFeature.class, conn);
	}

}
