package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: IxSamepoi.java
 * @Description: POI同一关系主表
 * @author zhangyt
 * @date: 2016年8月26日 上午10:31:58
 * @version: v1.0
 */
public class IxSamepoiSelector extends AbstractSelector {

	public IxSamepoiSelector(Connection conn) {
		super(IxSamepoi.class, conn);
	}

}
