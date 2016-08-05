package com.navinfo.dataservice.dao.glm.selector.rd.speedbump;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: RdSpeedbumpSelector.java
 * @Description: 减速带查询
 * @author zhangyt
 * @date: 2016年8月5日 下午1:58:34
 * @version: v1.0
 */
public class RdSpeedbumpSelector extends AbstractSelector {

	public RdSpeedbumpSelector(Connection conn) throws Exception {
		super(RdSpeedbump.class, conn);
	}

}
