package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.lc.LcFaceName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: LcFaceNameSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:48:40
 * @version: v1.0
 */
public class LcFaceNameSelector extends AbstractSelector {

	public LcFaceNameSelector(Connection conn) throws InstantiationException, IllegalAccessException {
		super(LcFaceName.class, conn);
	}

}
