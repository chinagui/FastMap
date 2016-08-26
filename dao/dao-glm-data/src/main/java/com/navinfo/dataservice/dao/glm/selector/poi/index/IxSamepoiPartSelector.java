package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiPart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: IxSamepoiPartSelector.java
 * @Description: POI同一关系组成表
 * @author zhangyt
 * @date: 2016年8月26日 上午10:33:09
 * @version: v1.0
 */
public class IxSamepoiPartSelector extends AbstractSelector {

	public IxSamepoiPartSelector(Connection conn) {
		super(IxSamepoiPart.class, conn);
	}

}
