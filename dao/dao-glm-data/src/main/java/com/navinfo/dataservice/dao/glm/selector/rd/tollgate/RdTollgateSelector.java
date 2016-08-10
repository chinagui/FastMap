package com.navinfo.dataservice.dao.glm.selector.rd.tollgate;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: RdTollgateSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:18:52
 * @version: v1.0
 */
public class RdTollgateSelector extends AbstractSelector {

	private Connection conn;

	public RdTollgateSelector(Connection conn) throws Exception {
		super(RdTollgate.class, conn);
		this.conn = conn;
	}

}
