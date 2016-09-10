package com.navinfo.dataservice.dao.glm.selector.rd.tollgate;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: RdTollgateNameSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:50:57
 * @version: v1.0
 */
public class RdTollgateNameSelector extends AbstractSelector {

	private Connection conn;

	public RdTollgateNameSelector(Connection conn) throws Exception {
		super(RdTollgateName.class, conn);
		this.conn = conn;
	}

}
