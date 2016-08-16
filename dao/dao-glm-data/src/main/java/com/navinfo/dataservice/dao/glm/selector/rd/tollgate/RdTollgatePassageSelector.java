package com.navinfo.dataservice.dao.glm.selector.rd.tollgate;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgatePassage;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: RdTollgatePassageSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:50:57
 * @version: v1.0
 */
public class RdTollgatePassageSelector extends AbstractSelector {

	private Connection conn;

	public RdTollgatePassageSelector(Connection conn) throws Exception {
		super(RdTollgatePassage.class, conn);
		this.conn = conn;
	}

}
